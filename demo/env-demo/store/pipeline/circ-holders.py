### input data
## both dates are day in format yyyy-mm-dd
## chain is name of blockchain, for ex. 'ethereum', 'optimism'

'''
from_date_input = '2021-05-24'
to_date_input = '2023-02-28'
token_address = '0x6123b0049f904d730db3c36a31167d9d4121fa6b'
chain = 'ethereum'
'''

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

zero = 0
bucket_name = "haas-data-dev"
schema = "timestamp long, block_number long, token_address string, sender_address string, receiver_address string, value decimal(38, 0), transaction_hash string, log_index int"
holders_schema = "address string, quantity decimal(38, 0)"

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

# get transfers by pandas
s3_resource = boto3.resource('s3')
bucket = s3_resource.Bucket(bucket_name)    
def pandas_get_tansfers_for_day(chain, token_address, day):
    columns = ["token_address", "sender_address", "receiver_address", "value", "transaction_hash", "log_index", "block_number", "timestamp"]
    date_str = day.strftime('%Y/%m/%d')
    p = f"data/dev/{chain}/raw/csv/transfers/{date_str}"
    prefix_objs = bucket.objects.filter(Prefix="data/dev/{chain}/raw/csv/transfers/" + day.strftime('%Y/%m/%d'))
    
    csvs = []
    for obj in prefix_objs:
        if obj.key.endswith('.csv'):
            csvs.append(obj)
    #print(datetime.now(), "objects loadedd")
    prefix_df = []      
    for obj in csvs:
        body = obj.get()['Body'].read()
        temp = pd.read_csv(io.BytesIO(body), encoding='utf8', header=0, names=columns)
        #print(datetime.now(), "file readed")
        prefix_df.append(temp)
    #print(datetime.now(), "files readed")
    df = pd.concat(prefix_df)
    #print(datetime.now(), "dataframe created")
    df = df.drop_duplicates();
    #fliter
    df = df[df["token_address"] == token_address]
    #print(datetime.now(), "dataframe filtered")
    df = df.sort_values(by=["block_number"])
    #print(datetime.now(), "dataframe sorted")
    return df.to_dict('records')

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

# processing
def processing(chain, token_address, from_date, to_date):
    #load holdes for pev day
    holders = {}
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

for ind in input_data:
    processing(ind['chain'], ind['token_address'].lower(), ind['from_date'], ind['to_date'])
