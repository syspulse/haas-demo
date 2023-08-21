import ibis
from ibis import _
ibis.options.interactive = True

t = ibis.read_parquet("ethereum__transactions__10861674_to_10861674.parquet")
t.select(["value","from_address"]).filter(t["value"].cast("float") >= 4.0e19)

v = b'W\xafy\xbd\xfbO\x86\xbc\x81\xb2\x86\x07\x87\xde#\x88s\x8e&o'
a = "0x57af79bdfb4f86bc81b2860787de2388738e266f"
a2 = bytes.fromhex(a[2:])
t.select(["value","from_address"]).filter(t["from_address"] != a2)

#t.to_pandas()['from_address'].apply(lambda x:bytes.hex(x))

p = t.to_pandas()
p['addr'] = p['from_address'].apply(lambda x:"0x"+bytes.hex(x))
p[['addr','value']]