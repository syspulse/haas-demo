package io.syspulse.haas.ingest.icp.flow.rpc3

import com.typesafe.scalalogging.Logger

import io.syspulse.skel.Ingestable

// {
//   "block": {
//     "block_identifier": {
//       "index": 7305956,
//       "hash": "9e066d9f7a5751384a42b1ba8b867db6bdf756c1cccf2cc2bc5ce2828b98b6eb"
//     },
//     "parent_block_identifier": {
//       "index": 7305955,
//       "hash": "c65e002051838a109fb909b8ca5ebd188d13fa3c2f7f11986f8cdacb6efbd67d"
//     },
//     "timestamp": 1701271554039,
//     "transactions": [
//       {
//         "transaction_identifier": {
//           "hash": "4f9b3f3f26216d26bf4e82cdd422317f1ffeaefaa3c58f8b7a902afd0e9697a0"
//         },
//         "operations": [
//           {
//             "operation_identifier": {
//               "index": 0
//             },
//             "type": "TRANSACTION",
//             "status": "COMPLETED",
//             "account": {
//               "address": "c01e0d829f8bf50c958fb3dd3dc446d7dcb5efb3f18edad7fc28f3797b58ca26"
//             },
//             "amount": {
//               "value": "-1172550830",
//               "currency": {
//                 "symbol": "ICP",
//                 "decimals": 8
//               }
//             }
//           },
//           {
//             "operation_identifier": {
//               "index": 1
//             },
//             "type": "TRANSACTION",
//             "status": "COMPLETED",
//             "account": {
//               "address": "6b1eda99402172c967bf05f7f77a013a60fedbb635c4c09502fed5abd514a99b"
//             },
//             "amount": {
//               "value": "1172550830",
//               "currency": {
//                 "symbol": "ICP",
//                 "decimals": 8
//               }
//             }
//           },
//           {
//             "operation_identifier": {
//               "index": 2
//             },
//             "type": "FEE",
//             "status": "COMPLETED",
//             "account": {
//               "address": "c01e0d829f8bf50c958fb3dd3dc446d7dcb5efb3f18edad7fc28f3797b58ca26"
//             },
//             "amount": {
//               "value": "-10000",
//               "currency": {
//                 "symbol": "ICP",
//                 "decimals": 8
//               }
//             }
//           }
//         ],
//         "metadata": {
//           "block_height": 7305956,
//           "memo": 10545568565151939000,
//           "timestamp": 1701271554039099400                          
//         }
//       }
//     ]
//   }
// }

case class IcpRpcBlockIdentifier(
  index:Long,
  hash:String,    
)

case class IcpRpcTransactionIdentifier(
  hash:String,
)

case class IcpRpcTransactionOperationIdentifier(
  index:Long,
)

case class IcpRpcAccount(
  address:String,
)

case class IcpRpcCurrency(
  symbol:String,
  decimals:Int
)

case class IcpRpcAmount(
  value:String,
  currency:IcpRpcCurrency
)

case class IcpRpcMetadata(
  block_height:Long,
  memo:BigInt,
  timestamp:Long
)

case class IcpRpcTransactionOperation(
  operation_identifier:IcpRpcTransactionOperationIdentifier,
  `type`:String,
  status:String,
  account: IcpRpcAccount,
  amount: IcpRpcAmount
)

case class IcpRpcTransaction(
  transaction_identifier:IcpRpcTransactionIdentifier,
  operations:Seq[IcpRpcTransactionOperation],
  metadata: IcpRpcMetadata,
) extends Ingestable

case class IcpRpcBlockBlock(
  block_identifier: IcpRpcBlockIdentifier,
  parent_block_identifier: IcpRpcBlockIdentifier,
  timestamp: Long,
  transactions:Seq[IcpRpcTransaction]
  
)  extends Ingestable

case class IcpRpcBlock(block:IcpRpcBlockBlock) extends Ingestable
