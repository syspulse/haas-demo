#!/usr/bin/env python
# coding: utf-8

# In[4]:


import numpy as np
import pandas as pd
import os


# In[7]:


data_path = '/mnt/share/data/paradigm/transfers/00000000/ethereum_native_transfers__v1_0_0__00000000_to_00199999.parquet'
data_path = os.path.expanduser(data_path)


# In[80]:


df = pd.read_parquet(data_path)


# In[81]:


df.dtypes


# In[82]:


def binary_to_hex(binary):
    hex_string = ''.join(format(byte, '02x') for byte in binary)
    return "0x"+hex_string


# In[83]:


def binary_to_bigint(binary):
    hex_string = ''.join(format(byte, '02x') for byte in binary)
    return int("0x"+hex_string,16)


# In[84]:


df["v"] = df["value"].apply(binary_to_bigint)


# In[98]:


df2 = df[df["v"] < 31337]


# In[100]:


df2["v"].count()


# In[ ]:




