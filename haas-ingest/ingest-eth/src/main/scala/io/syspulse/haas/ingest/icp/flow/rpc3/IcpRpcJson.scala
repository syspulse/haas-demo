package io.syspulse.haas.ingest.icp.flow.rpc3

import com.typesafe.scalalogging.Logger

// ATTENTION
import io.syspulse.skel.service.JsonCommon
import spray.json._
import spray.json.{DefaultJsonProtocol,NullOptions}

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

case class IcpBlockIdentifier(
  index:Long,
  hash:String,    
)

case class IcpTransactionIdentifier(
  hash:String,
)

case class IcpTransactionOperationIdentifier(
  index:Long,
)

case class IcpAccount(
  address:String,
)

case class IcpCurrency(
  symbol:String,
  decimals:Int
)

case class IcpAmount(
  value:String,
  currency:IcpCurrency
)

case class IcpMetadata(
  block_height:Long,
  memo:BigInt,
  timestamp:Long
)

case class IcpTransactionOperation(
  operation_identifier:IcpTransactionOperationIdentifier,
  `type`:String,
  status:String,
  account: IcpAccount,
  amount: IcpAmount
)

case class IcpTransaction(
  transaction_identifier:IcpTransactionIdentifier,
  operations:Seq[IcpTransactionOperation],
)

case class IcpBlockBlock(
  block_identifier: IcpBlockIdentifier,
  parent_block_identifier: IcpBlockIdentifier,
  timestamp: Long,
  transactions:Seq[IcpTransaction]
  
)  extends Ingestable

case class IcpBlock(block:IcpBlockBlock) extends Ingestable


object IcpRpcJson extends JsonCommon with NullOptions with ProductFormatsInstances {
  import DefaultJsonProtocol._

  implicit val jf_icp_rpc_b_id = jsonFormat2(IcpBlockIdentifier)
  implicit val jf_icp_rpc_t_id = jsonFormat1(IcpTransactionIdentifier)
  implicit val jf_icp_rpc_t_op_id = jsonFormat1(IcpTransactionOperationIdentifier)
  implicit val jf_icp_rpc_acc = jsonFormat1(IcpAccount)
  implicit val jf_icp_rpc_curr = jsonFormat2(IcpCurrency)
  implicit val jf_icp_rpc_amount = jsonFormat2(IcpAmount)
  implicit val jf_icp_rpc_meta = jsonFormat3(IcpMetadata)
  implicit val jf_icp_rpc_t_op = jsonFormat5(IcpTransactionOperation)  

  implicit val jf_icp_rpc_t = jsonFormat2(IcpTransaction) 
  implicit val jf_icp_rpc_b = jsonFormat4(IcpBlockBlock)
  implicit val jf_icp_rpc_block = jsonFormat1(IcpBlock)
    
}
