### input data
### both dates are day in format yyyy-mm-dd
### chain is name of blockchain, for ex. 'ethereum', 'optimism'

# from_date_input = '2022-12-31'
# to_date_input = '2023-03-31'
# token_id = 'solidly' #'uniswap'
# token_addresses = {'ethereum': '0x777172D858dC1599914a1C4c6c9fC48c99a60990'}#{'ethereum': '0x1f9840a85d5af5bf1d1762f925bdaddc4201f984',
#                   #'optimism': '0x6fd9d7ad17242c41f7131d257212c54a0e816691'}
# custom_locks = {'ethereum': {'0x77730ed992d286c53f3a0838232c3957daeaaf73': 'veSOLID'}}#{'ethereum': {'0x99C9fc46f92E8a1c0deC1b1747d010903E884bE1': 'Optimism Bridge'},
#                # 'optimism': {}}

try: holders_max = holders_max
except NameError: holders_max = 50

from datetime import *
from decimal import *
import csv
import io
import json

zero = 0
bucket_name = "haas-data-dev"

from_date = date.fromisoformat(from_date_input)
to_date = date.fromisoformat(to_date_input)

# normailse input. addresses to lower case
token_addresses = {chain: str(address.lower()) for chain, address in token_addresses.items()}

for chain, locks in custom_locks.items():
    locks = {a.lower(): tag for a, tag in locks.items()}
    custom_locks[chain] = locks

### ---------------- HOLDERS ---------------- ###
###-----------------
from pyspark.sql import SparkSession
from datetime import *
from dateutil.relativedelta import *
from decimal import Decimal
spark = (
        SparkSession
        .builder
        .config('spark.jars.packages', 'org.apache.hadoop:hadoop-aws:3.2.2')
        .config('spark.hadoop.fs.s3a.impl', 'org.apache.hadoop.fs.s3a.S3AFileSystem')
    .getOrCreate()
    )

schema = "timestamp long, block_number long, token_address string, sender_address string, receiver_address string, value decimal(38, 0), transaction_hash string, log_index int"
holders_schema = "address string, quantity decimal(38, 0)"

###-----------------
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

###-----------------
### get all transers for period
# current rate 500,000 - 1,000,000 ERC20 transfers per day.
# no more than 1428 transfers per block, ~7k blocks per day.
# 1.4k * 7k = max ~10,000,000 transfers per day theoretically

# specify token and date
#token_address = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984";
#from_date = date.fromisoformat(from_date_input)
#to_date = date.fromisoformat(to_date_input)

#input_data = [{'chain': chain, 'token_address': token_address, 'from_date': from_date, 'to_date': to_date}]

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

#input_data

###-----------------
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
    
    
def pandas_store_holders(chain, token_address, holders, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders"
    df = pd.DataFrame(holders.items(), columns=['address', 'quantity'])
    df['quantity'] = df['quantity'].astype('int64')
    dataframe_to_s3(s3, df, bucket_name, path, 'csv')
    
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

def get_transfers_for_day(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f's3a://haas-data-dev/data/dev/{chain}/raw/csv/transfers/{date_str}/*'
    df = spark.read.schema(schema).csv(path, inferSchema=True)
    token_df = df.where((df.token_address == token_address)).sort("block_number").select("*");
    data = token_df.rdd.collect()
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


###--------------
### processing
def processing(chain, token_address, from_date, to_date):
    #load holdes for pev day
    holders = {};
    previous_day = from_date - timedelta(days=1)
    holders = read_holders_for_date(chain, token_address, previous_day)
    print(previous_day, len(holders))

    current_day = from_date
    print(datetime.now(), "Starting processing for", token_address, "amount of days:", 1 + (to_date - from_date).days)
    while current_day <= to_date:
        #print(datetime.now(), "Loading transfers", current_day)
        transfers = get_transfers_for_day(chain, token_address, current_day)

        #print(datetime.now(), "Procecssing transfers", current_day)
        holders = process_transactions(holders, transfers)
        holders = dict(sorted(holders.items(), key=lambda item: item[1], reverse=True))
        #print(datetime.now(), "Holders amount:", len(holders), "Holders values sum():", sum(holders.values()))
        
        csv_to_s3(chain, token_address, current_day, holders)

        current_day = current_day + timedelta(days=1)

    print(datetime.now(), "End of proccessing.")

### ------------------- CIRCULATION SUPPLY ------------------- ###
### ----------------------------
from datetime import *
from decimal import *
import csv
import io
import json
from botocore.errorfactory import ClientError

from botocore.errorfactory import ClientError
import boto3
s3 = boto3.client('s3')
print("Boto3 ready.")

### ----------------------------
### Datamodel
from dataclasses import dataclass, asdict
from typing import List

@dataclass
class MultichainValue:
    total: int
    dstr: map

@dataclass
class Holder:
    addr: str
    r: float #ratio
    tv: MultichainValue #totalValue
    cat: str = None
    tags: List[str] = None

@dataclass
class MultichainCirculatonSupply:
    token_id: str
    token_address: map
    ts: int #timestamp
    price: float
    totalSupply: MultichainValue
    circSupply: MultichainValue
    categories: map
    locks: map 
    #inflation24h: float
    holders: MultichainValue # total holders amount
    holdersGrowth: MultichainValue # total holders change 24h
    uniqueHoldersUp: MultichainValue
    uniqueHoldersDown: MultichainValue
    topHolders: List[Holder]
    
@dataclass
class CirculationSupply:
    tokenAddress: str
    timestamp: int
    price: float
    totalSupply: int
    circulatingSupply: int
    categories: map
    locks: list 
    inflation: float
    totalHolders: int
    totalHoldersChange: int
    uniqueHoldersDown: int
    uniqueHoldersUp: int
    topHolders: List[Holder]

### ----------------------------
### load holders 
def read_holders_for_date(chain, token_address, day):
    holders_schema = "address string, quantity string"
    day_str = day.strftime('%Y/%m/%d')
    holders_path = f"data/dev/{chain}/token/{token_address}/holders/{day_str}/holders.csv"
    
    holders = {}
    if not file_exist(bucket_name, holders_path):
        return holders
    
    s3.head_object(Bucket=bucket_name, Key=holders_path)    
    obj = s3.get_object(Bucket = bucket_name, Key = holders_path)
    lines = obj['Body'].read().decode("utf-8")
    buf = io.StringIO(lines)
    holders_data = list(csv.DictReader(buf))
    for holder in holders_data:
        if int(holder["quantity"]) > 0:
            holders[holder["address"]] = int(holder["quantity"])
    return holders

def read_chains_holders_for_date(token_addresses, day):
    chain_holders = {}
    
    for chain, address in token_addresses.items():
        holders = read_holders_for_date(chain, address, day)
        chain_holders[chain] = holders;
        #print(chain, address, len(chain_holders[chain]))
        
    return chain_holders

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
    
def store_to_s3(date, mcs: MultichainCirculatonSupply):
    date_str = date.strftime('%Y/%m/%d')
    supply_analysis_path = f'data/dev/supply/{token_id}/{date_str}/supply.json'
    
    s3.put_object(Bucket=bucket_name, Key=supply_analysis_path, Body=json.dumps(
        asdict(mcs, dict_factory=lambda x: {k: v for (k, v) in x if v is not None})))


### ----------------------------
### processing circ supply
def get_compound_holders(chain_holders):
    compound_holders = None
    
    for chain, holders in chain_holders.items():
        if compound_holders is None:
            compound_holders = holders.copy()
            continue
        
        multichain_holders = 0
        for address, value in holders.items():
            if compound_holders.get(address, 0) != 0:
                #print(f'{address}, total: {compound_holders.get(address)}, {chain}: {value}')
                multichain_holders += 1
            compound_holders[address] = compound_holders.get(address, 0) + value
        print(f'Multichain holders: {multichain_holders}')
    return compound_holders

def get_total_supply(chain_holders):
    total_supply = 0;
    chain_distribution = {};
    
    for chain, holders in chain_holders.items():
        chain_supply = sum(holders.values())
        total_supply = total_supply + chain_supply
        chain_distribution[chain] = chain_supply
    
    return MultichainValue(total_supply, chain_distribution)

    
def get_circulating_supply(chain_holders, locks, total_supply):
    total_locked_supply = 0
    chain_distribution = {};
    
    for chain, locks in locks.items():
        chain_locked = 0
        for address, tag in locks.items():
            chain_locked = chain_locked + chain_holders.get(chain, {}).get(address, 0)
        
        total_locked_supply = total_locked_supply + chain_locked
        chain_distribution[chain] = chain_locked

    total_circ_supply = total_supply.total - total_locked_supply
    for chain, locked in chain_distribution.items():
        chain_distribution[chain] = total_supply.dstr[chain] - chain_distribution[chain] 
    
    return MultichainValue(total_circ_supply, chain_distribution)

def get_category_distribution(holders, labels):
    categories_distribution = {}
    for k, v in labels.items():
        address = k;
        category = v[0];
        if address in holders:
            if category not in categories_distribution:
                categories_distribution[category] = 0
            categories_distribution[category] = categories_distribution[category] + holders[address]
            
    return categories_distribution

def get_multichain_category_distribution(chain_holders, labels):
    ctg = get_category_distribution(chain_holders["ethereum"], labels)
    mctg = {category: MultichainValue(amount, {"ethereum" : amount}) for category, amount in ctg.items()}
    return mctg

def get_total_holders(compound_holders, chain_holders):
    chain_total_holders = {}
    
    for chain, holders in chain_holders.items():
        chain_total_holders[chain] = len(holders)
    
    return MultichainValue(len(compound_holders), chain_total_holders)
    
def get_total_holders_change(total_holders, prev_total_holders, chain_holders, prev_chain_holders):
    total_change = len(total_holders) - len(prev_total_holders)
    chain_distribution = {}
    
    for chain, holders in chain_holders.items():
        chain_distribution[chain] = len(holders) - len(prev_chain_holders.get(chain, {}))
    
    return MultichainValue(total_change, chain_distribution)
    
def get_unique_holders_up(compound_holders, prev_compound_holders, chain_holders, prev_chain_holders):
    total_unique_holders_up = len(compound_holders.keys() - prev_compound_holders.keys())
    chain_distribution = {}
    
    for chain, holders in chain_holders.items():
        chain_distribution[chain] = len(holders.keys() - prev_chain_holders.get(chain, {}).keys())
    
    return MultichainValue(total_unique_holders_up, chain_distribution)

def get_unique_holders_down(compound_holders, prev_compound_holders, chain_holders, prev_chain_holders):
    total_unique_holders_down = 0 
    if prev_compound_holders:
        total_unique_holders_down = len(prev_compound_holders.keys() - compound_holders.keys())
    chain_distribution = {}
    
    for chain, holders in chain_holders.items():
        chain_unique_holders_down = 0 
        if prev_compound_holders:
            chain_unique_holders_down = len(prev_chain_holders.get(chain, {}).keys() - holders.keys())
            
        chain_distribution[chain] = chain_unique_holders_down
    
    return MultichainValue(total_unique_holders_down, chain_distribution)

def get_top_holders(compound_holders, chain_holders, labels):
    top_holders_amount = holders_max
    top_holders = dict(sorted(compound_holders.items(), key=lambda item: item[1], reverse=True)[:top_holders_amount])
    
    top_holders_objects = []
    for address, total_value in top_holders.items():
        
        chains_disctribution = {}
        for chain, holders in chain_holders.items():
            if holders.get(address):
                chains_disctribution[chain] = holders.get(address)
                
        holder_value = MultichainValue(total_value, chains_disctribution)
        
        category = None
        tags = None
        if address in labels:
            category = labels[address][0]
            tags = [labels[address][1]]
        
        holder_obj = Holder(address, 0, holder_value, category, tags)
        top_holders_objects.append(holder_obj)
    
    return top_holders_objects

def get_locks_obj(custom_locks, chain_holders):
    locks_obj = {}
    for chain, c_locks in custom_locks.items():
        chain_locks = {}
        for address, tag in c_locks.items():
            chain_locks[address] = {'tag': tag, 'value': chain_holders.get(chain, {}).get(address, 0)}
        locks_obj[chain] = chain_locks
    return locks_obj    


### ----------------------------
### circsupply processing
def process_multichain_circulation_supply(from_date, to_date, token_id, token_addresses, custom_locks):
    current_day = from_date
    labels = get_labels();

    print(datetime.now(), "| LOG:", "Starting processing for amount of days:", (to_date - from_date).days + 1)
    while current_day <= to_date:
        print(datetime.now(), "| LOG:", current_day)
        timestamp = int(datetime.combine(current_day, time.max).timestamp() * 1000)
        price = get_price(token_id, current_day)


        chain_holders = read_chains_holders_for_date(token_addresses, current_day);
        prev_chain_holders = read_chains_holders_for_date(token_addresses, (current_day - timedelta(days=1)));

        compound_holders = get_compound_holders(chain_holders)
        prev_compound_holders = get_compound_holders(prev_chain_holders)

        total_supply = get_total_supply(chain_holders);

        circulating_supply = get_circulating_supply(chain_holders, custom_locks, total_supply); 
        locks = get_locks_obj(custom_locks, chain_holders)

        category_distribution = get_multichain_category_distribution(chain_holders, labels);

        total_holders = get_total_holders(compound_holders, chain_holders);
        total_holders_change = get_total_holders_change(compound_holders, prev_compound_holders,
                                                        chain_holders, prev_chain_holders)

        unique_holders_up = get_unique_holders_up(compound_holders, prev_compound_holders,
                                                  chain_holders, prev_chain_holders)
        unique_holders_down = get_unique_holders_down(compound_holders, prev_compound_holders,
                                                      chain_holders, prev_chain_holders)

        top_holders = get_top_holders(compound_holders, chain_holders, labels);

        supply = MultichainCirculatonSupply(token_id,
                                            token_addresses,
                                            timestamp,
                                            price,
                                            total_supply,
                                            circulating_supply,
                                            category_distribution,
                                            locks,
                                            total_holders,
                                            total_holders_change,
                                            unique_holders_up,
                                            unique_holders_down,
                                            top_holders)

        #store and increment to next day
        store_to_s3(current_day, supply)
        current_day = current_day + timedelta(days=1)

### COMPOUND PROCESSING
def is_holders_exist(chain, token_address, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f"data/dev/{chain}/token/{token_address}/holders/{date_str}/holders.csv"
    return file_exist(bucket_name, path)

def is_supply_exist(token_id, date):
    date_str = date.strftime('%Y/%m/%d')
    path = f'data/dev/supply/{token_id}/{date_str}/supply.json'
    return file_exist(bucket_name, path)

holders_not_exists = {}
for chain, address in token_addresses.items():
    if not is_holders_exist(chain, address, from_date) or not is_holders_exist(chain, address, to_date):
        holders_not_exists[chain] = address

print(datetime.now(), "| LOG:", 'Calculating holders for:', holders_not_exists)
#loop trough holders if none
for chain, token_address in holders_not_exists.items():
    processing(chain, token_address, from_date, to_date)

# calculate supply
print(datetime.now(), "| LOG:", 'Calculating supply.')
process_multichain_circulation_supply(from_date, to_date, token_id, token_addresses, custom_locks)
print(datetime.now(), "| LOG:", 'Finished.')

# ========================================================================================================
bucket = 'haas-data-dev'
path = 'data/dev/ethereum'
token = token_address
limit = 10000
output_file = "supply.json"

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
