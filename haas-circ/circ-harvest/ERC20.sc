import java.math.BigInteger

object ERC20 {
  def toHex(s:String) = new BigInt(s.drop(2),16)
  def toStr(a:Array[Byte]) = a.map("%02X" format _).mkString

  // curl -X POST --data '{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984", "data":"0x70a082310000000000000000000000000b88516a6d22bf8e0d3657effbd41577c5fd4cb7"}, "latest"],"id":67}' -H "Content-Type: application/json" http://127.0.0.1:8545/
  // balanceOf("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984","0x41653c7d61609d856f29355e404f310ec4142cfb")
  def balanceOf(token:String,addr:String) = {
    val data = "0x70a08231000000000000000000000000" + addr.drop(2)
    val id = 3
    val jsonData = s"""{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "${token}", "data":"${data}"}, "latest"],"id":${id}}"""
    val res = requests.post(geth,data = jsonData, headers=Map("Content-Type"->"application/json"))
    val value = toHex(ujson.read(res.text).obj("result").str)
    value
  }

  def totalSupply(token:String) = {
    val data = "0x18160ddd000000000000000000000000" + token.drop(2)
    val id = 3
    val jsonData = s"""{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "${token}", "data":"${data}"}, "latest"],"id":${id}}"""
    val res = requests.post(geth,data = jsonData, headers=Map("Content-Type"->"application/json"))
    val value = toHex(ujson.read(res.text).obj("result").str)
    value
  }

  def name(token:String) = {
    val data = "0x06fdde03"
    val id = 3
    val jsonData = s"""{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "${token}", "data":"${data}"}, "latest"],"id":${id}}"""
    val res = requests.post(geth,data = jsonData, headers=Map("Content-Type"->"application/json"))
    val value = new String(toHex(ujson.read(res.text).obj("result").str).toByteArray.drop(32))
    value
  }

  def symbol(token:String) = {
    val data = "0x95d89b41"
    val id = 3
    val jsonData = s"""{"jsonrpc":"2.0","method":"eth_call","params":[{"to": "${token}", "data":"${data}"}, "latest"],"id":${id}}"""
    val res = requests.post(geth,data = jsonData, headers=Map("Content-Type"->"application/json"))
    val value = new String(toHex(ujson.read(res.text).obj("result").str).toByteArray.drop(32))
    value
  }
}
