package io.syspulse.haas.ingest.vechain.flow.rpc

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.Ingestable

// {
//   "id": "0x28da657acacbfcedad0df83c4b2f342d590c8b215693bf029c8e3d54fde0a36c",
//   "chainTag": 74,
//   "blockRef": "0x010901c4fcac03a7",
//   "expiration": 720,
//   "clauses": [
//     {
//       "to": "0x0000000000000000000000000000456e65726779",
//       "value": "0x0",
//       "data": "0xa9059cbb000000000000000000000000e26da590bb456bb1ca808e72e4f9adb9187481290000000000000000000000000000000000000000000000aab665c6398b54b832"
//     }
//   ],
//   "gasPriceCoef": 10,
//   "gas": 50373,
//   "origin": "0x4c8acd3b3ef8e1cd73bb3ae080711afc17f2efb9",
//   "delegator": null,
//   "nonce": "0x67560771a40cf9e0",
//   "dependsOn": null,
//   "size": 193,
//   "meta": {
//     "blockID": "0x010901c56854e0599c3685aff15c0e8163473250655d8b29005461a1de3d3b7f",
//     "blockNumber": 17367493,
//     "blockTimestamp": 1704201180
//   }
// }

case class RpcClause( 
  to: String,
  value: String,
  data: String
)

case class RpcTxMeta( 
  blockID: String,
  blockNumber: Long,
  blockTimestamp: Long
)

case class RpcTx(
  id: String,
  chainTag: Int,
  blockRef: String,
  expiration: Int,
  clauses: Seq[RpcClause],
  gasPriceCoef: Int,
  gas: Long,
  origin: String,
  delegator: Option[String],
  nonce: String,
  dependsOn: Option[String],
  size: Int,
  meta: Option[RpcTxMeta],
    
)  extends Ingestable


// {
//   "number": 17367493,
//   "id": "0x010901c56854e0599c3685aff15c0e8163473250655d8b29005461a1de3d3b7f",
//   "size": 813,
//   "parentID": "0x010901c4fcac03a75a278acae1248d96a6d5e67332679c76ad1e9dde54b108ef",
//   "timestamp": 1704201180,
//   "gasLimit": 30000000,
//   "beneficiary": "0x06be21f954639532c479adb45f61788cfb0669ae",
//   "gasUsed": 78710,
//   "totalScore": 1693401508,
//   "txsRoot": "0xf49388cc60ced987deb0194c4befa2038a572387c29dca3f3749fb1c9217bae2",
//   "txsFeatures": 1,
//   "stateRoot": "0x517d462c57dc7f40c5035d3182cfdb618ad0a4e30b80b9ae3c4665e1a3c1ae56",
//   "receiptsRoot": "0x085e001d79e2fc0820cccd04454a3c8b15e3b37f7e3363173130ffd11bc061fa",
//   "com": true,
//   "signer": "0x22d80f7f8103c12d2c059ad6b8df4f4dbd5f91e7",
//   "isTrunk": true,
//   "isFinalized": false,
//   "transactions": [
//     "0x28da657acacbfcedad0df83c4b2f342d590c8b215693bf029c8e3d54fde0a36c",
//     "0xbfb79e96eb3106eac5e0341e1a7bb84f55e747746cf4319fc5b0e5654cb2bd9d",
//     "0x8e2e41511dd443b7b9ca6ef5d677fb05e4e7fdde7cf52cb4c878fd5020048eca"
//   ]
// }

case class RpcBlock(  
  number: Long,
  id: String,
  size: Int,
  parentID: String,
  timestamp: Long,
  gasLimit: Long,
  beneficiary: String,
  gasUsed: Long,
  totalScore: Long,
  txsRoot: String,
  txsFeatures: Int,
  stateRoot: String,
  receiptsRoot: String,
  com: Boolean,
  signer: String,
  isTrunk: Boolean,
  isFinalized: Boolean,
  transactions: Seq[String]
    
)  extends Ingestable


case class RpcBlockTx(  
  number: Long,
  id: String,
  size: Int,
  parentID: String,
  timestamp: Long,
  gasLimit: Long,
  beneficiary: String,
  gasUsed: Long,
  totalScore: Long,
  txsRoot: String,
  txsFeatures: Int,
  stateRoot: String,
  receiptsRoot: String,
  com: Boolean,
  signer: String,
  isTrunk: Boolean,
  isFinalized: Boolean,
  transactions: Seq[RpcTx]
    
)  extends Ingestable