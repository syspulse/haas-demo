import pandas as pd
import os, json
import glob
#from pathlib import Path

files = glob.glob("./*.json")
dfs = []

# df = pd.json_normalize(json.loads(open("dopex.json").read()))
# df.loc[0,:]

for file in files:
  with open(file) as f:
    data = f.read()
    if(data.startswith('{"status":{"error_code":429') == False) : 
      json_data = pd.json_normalize(json.loads(data))
      dfs.append(json_data)

df = pd.concat(dfs, sort=False)

df.loc[0:1]

df.groupby(['asset_platform_id'])['asset_platform_id'].count()

df_eth = df[df['asset_platform_id'] == 'ethereum']

df_eth[['symbol','contract_address']]

df_eth[['symbol','links.homepage','contract_address']]