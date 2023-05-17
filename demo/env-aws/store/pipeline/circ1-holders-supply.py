### Input data
# dates in format yyyy-mm-dd
# map of custom locks {string address, string tag}
# token_id in coingeeko

# token_address = "0x777172d858dc1599914a1c4c6c9fc48c99a60990";
# token_id = "solidlydex";
# from_date_input = "2022-12-31"
# to_date_input = "2023-02-28"
# custom_locks = {
#     '0x77730ed992d286c53f3a0838232c3957daeaaf73': 'veSOLID'
# }

try: holders_max = holders_max
except NameError: holders_max = 50

from datetime import *
from decimal import *
import csv
import io
import json

token_address = token_addresses['ethereum']
try: custom_locks = custom_locks['ethereum']
except: custom_locks = {}

chain = 'ethereum'
token_address = token_address.lower()
chain = chain.lower()
token_id = token_id.lower()
from_date = date.fromisoformat(from_date_input) #date(2021, 5, 24) #included
to_date = date.fromisoformat(to_date_input) #date(2022, 1, 1) #included

custom_locks = {str(address.lower()): tag for address, tag in custom_locks.items()}

from pyspark.sql import SparkSession
from dateutil.relativedelta import *
from decimal import Decimal
spark = (SparkSession.builder
         .config('spark.jars.packages', 'org.apache.hadoop:hadoop-aws:3.2.2')
         .config('spark.hadoop.fs.s3a.impl', 'org.apache.hadoop.fs.s3a.S3AFileSystem')
         .getOrCreate()
        )

zero = 0
bucket_name = "haas-data-dev"
schema = "timestamp long, block_number long, token_address string, sender_address string, receiver_address string, value decimal(38, 0), transaction_hash string, log_index int"
holders_schema = "address string, quantity decimal(38, 0)"

##### HOLDERS #####
### get all transers for period
# current rate 500,000 - 1,000,000 ERC20 transfers per day.
# no more than 1428 transfers per block, ~7k blocks per day.
# 1.4k * 7k = max ~10,000,000 transfers per day theoretically

# specify token and date
#token_address = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984";
from_date = date.fromisoformat(from_date_input)
to_date = date.fromisoformat(to_date_input)

input_data = [{'chain': chain, 'token_address': token_address, 'from_date': from_date, 'to_date': to_date}]

#input_data = [#{'token_address': '0x777172D858dC1599914a1C4c6c9fC48c99a60990', 'from_date': '2022-12-31', 'to_date': to_date}, # solidly
#              {'token_address': '0x777172d858dc1599914a1c4c6c9fc48c99a60990', 'from_date': '2022-12-31', 'to_date': to_date}, # balancer
#              #{'token_address': '0x249ca82617ec3dfb2589c4c17ab7ec9765350a18', 'from_date': '2022-12-07', 'to_date': to_date}, # verse
#              #{'token_address': '0xdefa4e8a7bcba345f687a2f1456f5edd9ce97202', 'from_date': '2021-04-12', 'to_date': to_date} # kyber
#             ]

#input_data = [{'token_address': '0x777172D858dC1599914a1C4c6c9fC48c99a60990', 'from_date': '2022-12-31', 'to_date': to_date},
#              {'token_address': '0x1f9840a85d5af5bf1d1762f925bdaddc4201f984', 'from_date': '2020-09-14', 'to_date': to_date},
#              {'token_address': '0xde52a16c6db7914e6953e27a7df4ce6a315f8b45', 'from_date': '2021-06-13', 'to_date': to_date},
#              {'token_address': '0xfa5047c9c78b8877af97bdcb85db743fd7313d4a', 'from_date': '2020-11-01', 'to_date': to_date},
#              {'token_address': '0xd502f487e1841fdc805130e13eae80c61186bc98', 'from_date': '2021-09-13', 'to_date': to_date}
#             ]

input_data

### s3 client
import importlib.util

spec = importlib.util.find_spec("boto3")
if spec is None:
    print("Installing boto3.")
    sc.install_pypi_package("boto3")
else:
    print("Boto3 already installed.")
    
from botocore.errorfactory import ClientError
import boto3
s3 = boto3.client('s3')
print("Boto3 ready.")

spec = importlib.util.find_spec("pandas")
if spec is None:
    print("Installing pandas.")
    sc.install_pypi_package("pandas==1.2.5")
else:
    print("Pandas already installed.")
    
import pandas as pd
print("Pandas ready.")



### store to s3
import io
import csv

def csv_to_s3(chain, token_address, date, holders):
    date_str = date.strftime('%Y/%m/%d')
    holders = dict(sorted(holders.items(), key=lambda item: item[1], reverse=True))
    path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders.csv"
    stream = io.StringIO()
    writer = csv.DictWriter(stream, fieldnames = ["address", "quantity"])
    writer.writeheader()
    for key, value in holders.items():
        writer.writerow({'address': key, 'quantity': value})
    csv_string_object = stream.getvalue()
    s3.put_object(Bucket=bucket_name, Key=path, Body=csv_string_object)

def dataframe_to_s3(s3_client, df, bucket_name, filepath, format):
    print(df['quantity'].dtype)
    
    if format == 'parquet':
        out_buffer = io.BytesIO()
        df.to_parquet(out_buffer, index=False)

    elif format == 'csv':
        out_buffer = io.StringIO()
        df.to_csv(out_buffer, index=False)

    s3_client.put_object(Bucket=bucket_name, Key=f'{filepath}.{format}', Body=out_buffer.getvalue())
    
from pyspark.sql.functions import col
from pyspark.sql.functions import when

def get_transfers_for_day(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f's3a://haas-data-dev/data/dev/{chain}/raw/csv/transfers/{date_str}/*'
    try: 
        df = spark.read.schema(schema).csv(path, inferSchema=True)
        token_df = df.where((df.token_address == token_address)).select('sender_address', 'receiver_address', 'value');
        data = token_df.rdd.collect()
    except:
        data = []
    return data

from pyspark.sql.types import *
from pyspark.sql import Row

def process_transactions(holders, transfers):
    if transfers is None:
        transfers = []
    
    for transfer in transfers:
        sender_address = transfer["sender_address"]
        if sender_address not in holders:
            holders[sender_address] = zero
            
        holders[sender_address] = holders[sender_address] - int(transfer["value"]);
        if holders[sender_address] == zero:
            del holders[sender_address]
        
        receiver_address = transfer["receiver_address"]
        if receiver_address not in holders:
            holders[receiver_address] = zero 
            
        holders[receiver_address] = holders[receiver_address] + int(transfer["value"]);
        if holders[receiver_address] == zero:
            del holders[receiver_address]
        
    return holders
          
### load holders 
def read_holders_for_date(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    holders = {}
    holders_path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders.csv"
    try:
        s3.head_object(Bucket=bucket_name, Key=holders_path)
    except ClientError:
        print("No holders file for day", date)
        return holders
    obj = s3.get_object(Bucket = bucket_name, Key = holders_path)
    lines = obj['Body'].read().decode("utf-8")
    buf = io.StringIO(lines)
    holders_data = list(csv.DictReader(buf))
    for holder in holders_data:
        holders[holder["address"]] = int(holder["quantity"])
    return holders

def read_transfers_for_day(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f'data/dev/{chain}/raw/csv/transfers/{date_str}/'
    transfers = {}
    try:
        s3.head_object(Bucket=bucket_name, Key=path)
    except ClientError:
        print("No transfers folder for day", date)
        return transfers
    
    files = s3.list_objects(Bucket=bucket_name, Prefix=path)
    #for file in files:
        #print(files)
        
    return transfers



# processing
def processing(chain, token_address, from_date, to_date):
    #load holdes for pev day
    holders = {};
    previous_day = from_date - timedelta(days=1)
    holders = read_holders_for_date(chain, token_address, previous_day)
    print(datetime.now(), 'LOG |', 'Amount of holders for previous day:', previous_day, len(holders))

    current_day = from_date
    start_time = datetime.now()
    print(start_time, 'LOG |', "Calculate holders for", chain, token_address, "from", from_date, "to", to_date)
    print(datetime.now(), 'LOG |', "Amount of days:", 1 + (to_date - from_date).days)
    while current_day <= to_date:
        #print(datetime.now(), "Loading transfers", current_day)
        transfers = get_transfers_for_day(chain, token_address, current_day)
        holders = process_transactions(holders, transfers)
         
        holders = dict(sorted(holders.items(), key=lambda item: item[1], reverse=True))
        #print(datetime.now(), "Holders amount:", len(holders), "Holders values sum():", sum(holders.values()))
        
        csv_to_s3(chain, token_address, current_day, holders)

        current_day = current_day + timedelta(days=1)
    end_time = datetime.now()
    print(end_time, 'LOG |', "End of proccessing.")
    print(datetime.now(), 'LOG |', 'Holders calculation took', (end_time - start_time).total_seconds(), 'seconds')

##### SUPPLY ######
from datetime import *
from decimal import *
import csv
import io
import json

null_address = "0x0000000000000000000000000000000000000000"
holders_schema = "address string, quantity string"
bucket_name = "haas-data-dev"



### load holders 
def read_holders_for_date(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    holders = {}
    holders_path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders.csv"
    if not file_exist(bucket_name, holders_path):
        return holders
    
    s3.head_object(Bucket=bucket_name, Key=holders_path)    
    obj = s3.get_object(Bucket = bucket_name, Key = holders_path)
    lines = obj['Body'].read().decode("utf-8")
    buf = io.StringIO(lines)
    holders_data = list(csv.DictReader(buf))
    for holder in holders_data:
        holders[holder["address"]] = int(holder["quantity"])
    return holders

### load labels
def get_labels():
    labels_path = "data/dev/label/csv/2022/11/01/labels.csv"
    labels_fields = ["id", "source", "created_at", "address", "category", "description"]
    ## read from s3
    obj = s3.get_object(Bucket = bucket_name, Key = labels_path)
    lines = obj['Body'].read().decode("utf-8")
    buf = io.StringIO(lines)
    labels_data = list(csv.DictReader(buf, fieldnames=labels_fields))
    # filter 'Contracts' actegory
    labels_data = list(filter(lambda entry: entry["category"] != "Contracts", labels_data))
    #print(len(labels_data))
    ## distinct to {address:(category,description)} map
    labels = {label["address"] : (label["category"],label["description"]) for label in labels_data}
    #print(len(labels))
    return labels

### load labels
def get_price(token_id, date):
    ## read from s3
    date_str = date.strftime('%Y/%m/%d')
    date_timestamp = int(datetime.combine(date, time.min).timestamp() * 1000);
    path = f'data/dev/coingecko/raw/json/price/{token_id}/{date_str}/price-usd-{date_timestamp}.json'
    
    if not file_exist(bucket_name, path):
        return 0
    
    obj = s3.get_object(Bucket = bucket_name, Key = path)
    file_content = obj['Body'].read().decode("utf-8")
    json_content = json.loads(file_content)
    price_date = datetime.fromtimestamp(json_content['ts']//1000).date()
    if (price_date != date):
        raise ValueError('Requested date', date, 'different from price date', price_date)
    return json_content['v']

def file_exist(bucket, path):
    try:
        s3.head_object(Bucket=bucket, Key=path)
        return True
    except ClientError:
        #print("File doesn't exist:", bucket + "/" + path)
        return False
    
    
def process(chain, token_address, token_id, from_date, to_date):
    holders = {}
    prev_day = {}
    labels = get_labels()

    for address, tag in custom_locks.items():
        labels[address] = ['Lock', tag]
        
    current_day = from_date
    prev_circ_supply = None
    print(datetime.now(), "Starting processing", token_id, token_address, "amount of days:", (to_date - from_date).days + 1)
    while current_day <= to_date:
        #print(current_day)
        holders = read_holders_for_date(chain, token_address, current_day)
        if not holders:
            current_day = current_day + timedelta(days=1)
            continue
        #print(len(holders))

        # get holders for previous period
        prev_day = current_day - timedelta(days=1)
        prev_holders = read_holders_for_date(chain, token_address, prev_day)
        #print(len(prev_holders))

        ### circulation supply calculation
        #sort by quantity
        holders = dict(sorted(holders.items(), key=lambda item: item[1], reverse=True))

        #remove null address
        holders.pop(null_address, 0)
        prev_holders.pop(null_address, 0)

        eod_date = datetime.combine(current_day, time.max)
        #print(eod_date)

        price = get_price(token_id, current_day);

        total_supply = sum(holders.values());
        #print("Total " + str(total_supply))

        active_locks = {}
        for address in custom_locks:
            if address in holders:
                active_locks[address] = holders[address]
        #print(active_locks)

        locked_supply = sum(active_locks.values())
        #print("Locked " + str(locked_supply))

        categories_distribution = {}
        for k, v in labels.items():
            address = k;
            category = v[0];
            if address in holders:
                if category not in categories_distribution:
                    categories_distribution[category] = 0
                categories_distribution[category] = categories_distribution[category] + holders[address]

        lock_amount = categories_distribution.get('Lock')
        if lock_amount is None:
            lock_amount = 0
        hole_amount = categories_distribution.get('Black Holes')
        if hole_amount is None:
            hole_amount = 0
        locked_supply = lock_amount + hole_amount

        circulating_supply = total_supply - locked_supply
        #print("Circulating " + str(circulating_supply))

        if prev_holders and prev_circ_supply is None:
            prev_total_supply = sum(prev_holders.values());
            prev_active_locks = {}
            for address in custom_locks:
                if address in prev_holders:
                    prev_active_locks[address] = prev_holders[address]
            prev_locked_supply = sum(prev_active_locks.values())
            prev_circ_supply = prev_total_supply - prev_locked_supply

        circulating_supply_change_percentage = 0;
        if prev_circ_supply:
            circulating_supply_change_percentage = round((circulating_supply - prev_circ_supply)/prev_circ_supply, 4);

        total_prev_holders = len(prev_holders)
        #print("Total prev holders ", total_prev_holders)

        total_holders = len(holders)
        #print("Total holders ", total_holders)

        total_holders_growth = total_holders - total_prev_holders
        #print("Total holders growth", total_holders_growth)

        # get new hodlers present in current dataset, but don't exist in prev dataset
        new_unique_holders = len(holders.keys() - prev_holders.keys())
        #print("New holders ", new_holders)

        # get those who exists in prev holders, but doesn't exist in curent holders - those who sold, flippers
        lost_unique_holders = 0;
        if prev_holders:
            lost_unique_holders = len(prev_holders.keys() - holders.keys())
        #print("Holders sold ", holders_sold)

        amount = holders_max
        top_holders = {}
        for k,v in holders.items():    
            top_holders[k] = v
            amount = amount - 1;
            if (amount == 0):
                break       
        #print("Top holders amount", len(top_holders))

        categories_distribution = {}
        for k, v in labels.items():
            address = k;
            category = v[0];
            if address in holders:
                if category not in categories_distribution:
                    categories_distribution[category] = 0
                categories_distribution[category] = categories_distribution[category] + holders[address]

                #print(address, category)  
        #print("Categories Distribution", categories_distribution)

        if total_supply == 0:
            print("WARN: total_supply=",total_supply)
            total_supply = -1.0
        
        active_locks_objects = []
        for key, value in active_locks.items():
            category = None
            tags = None
            if key in labels:
                category = labels[key][0]
                tags = [labels[key][1]]
            obj = {"addr": key, "value": value, "r": round(value/total_supply, 4)}
            if category:
                obj["cat"] = category
            if tags:
                obj["tags"] = tags
            active_locks_objects.append(obj)

        top_holders_objects = []
        for key, value in top_holders.items():
            category = None
            tags = None
            if key in labels:
                category = labels[key][0]
                tags = [labels[key][1]]

            obj = {"addr": key, "value": value, "r": round(value/total_supply, 4)}
            if category:
                obj["cat"] = category
            if tags:
                obj["tags"] = tags
            top_holders_objects.append(obj)

        circ_json = {}
        circ_json["tokenAddress"] = token_address
        circ_json["timestamp"] = int(eod_date.timestamp())
        circ_json["price"] = price;
        circ_json["totalSupply"] = total_supply
        circ_json["circulatingSupply"] = circulating_supply
        circ_json["inflation"] = circulating_supply_change_percentage
        circ_json["locks"] = active_locks_objects
        circ_json["totalHolders"] = total_holders
        circ_json["totalHoldersChange"] = total_holders_growth
        circ_json["uniqueHoldersUp"] = new_unique_holders
        circ_json["uniqueHoldersDown"] = lost_unique_holders
        circ_json["topHolders"] = top_holders_objects
        circ_json["categories"] = categories_distribution


        ### store report to json file on s3
        path = "data/dev/ethereum/token/" + token_address + "/circulating-supply/" + current_day.strftime('%Y/%m/%d') + "/circulating_supply.json"

        #print(circ_json)
        s3.put_object(Bucket=bucket_name, Key=path, Body=json.dumps(circ_json))

        prev_circ_supply = circulating_supply
        current_day = current_day + timedelta(days=1)

    print(datetime.now(), "Done.")

print(chain, token_address, token_id, from_date, to_date, custom_locks)

### COMPOUND PROCESSING
def is_holders_exist(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders.csv"
    return file_exist(bucket_name, path)

def is_supply_exist(token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f'data/dev/ethereum/token/{token_address}/circulating-supply/{date_str}/circulating_supply.json'
    return file_exist(bucket_name, path)

holders_exists = is_holders_exist(chain, token_address, from_date) and is_holders_exist(chain, token_address, to_date)
supply_exists = is_supply_exist(token_address, from_date) and is_supply_exist(token_address, to_date)

print('Holders exists', holders_exists)
print('Supply exists', supply_exists)
if not holders_exists:
    print('Holders & supply recalculate')
    processing(chain, token_address, from_date, to_date)
    process(chain, token_address, token_id, from_date, to_date)
else:
    if not supply_exists:
        print('Supply recalculate', supply_exists)
        process(chain, token_address, token_id, from_date, to_date)


#========================================================================================================

# calculate supply
#print(datetime.now(), "| LOG:", 'Calculating supply.')
#process(chain, token_address, token_id, from_date, to_date)
#print(datetime.now(), "| LOG:", 'Finished.')


# ========================================================================================================
bucket = 'haas-data-dev'
path = 'data/dev/ethereum'
token = token_address
limit = 10000
output_file = "circulating_supply.json"

import re
import boto3
s3_session = boto3.Session()
s3 = s3_session.resource('s3')
s3_bucket = s3.Bucket(bucket)
s3_client = s3_session.client('s3')

input_path = f'{path}/token/{token}/circulating-supply'
output_path = f'{path}/supply/token/{token}'

output = []

for file in s3_bucket.objects.filter(Prefix=input_path).limit(limit):
    if file.key.endswith('circulating_supply.json'):
        print(file.key)
        body = file.get()['Body'].read()
        output.append(body)
        nl = 0x0a
        nl = nl.to_bytes(1,"big")
        output.append(nl)

object = s3.Object(bucket, f'{output_path}/{output_file}')
json = b"".join(output).decode("utf-8")
object.put(Body=json)
