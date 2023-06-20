#!/usr/bin/env python
# coding: utf-8

# In[19]:


import math
import os
import shutil
import subprocess

import ctc
import ctc.rpc
from ctc.toolbox import pl_utils
import polars as pl
import matplotlib.pyplot as plt
import toolplot
import numpy as np
import toolstr


# In[20]:


pl_utils.set_column_display_width()


# In[21]:


data_path = '/mnt/share/data/paradigm/transfers/00000000/*.parquet'
data_path = os.path.expanduser(data_path)


# In[27]:


df = pl.scan_parquet(data_path)


# In[28]:


df.schema


# In[81]:


df2 = df.select(['block_number','from_address','value']).limit(3)
df3 = pl_utils.binary_columns_to_prefix_hex(df2.collect())
df3.schema


# In[52]:


df3.select([pl.col('value').cast(pl.UInt64).alias('v0'),pl.col('value')])


# In[83]:


df3['value'].apply(lambda x: str(int(x, 16)))


# In[78]:


df4 = df3['value'].apply(lambda x: float(int(x, 16)))
df4


# In[92]:


df3.select(pl.col('value').apply(lambda x: float(int(x, 16))).filter(pl.col('value') > 10))


# In[33]:


int("0x208686e75e903bc000000001231232130",16)


# In[ ]:




