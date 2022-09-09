//
// This scrpit must be run with spark-shell or Almond Spark Shell ! (does not work from plain Ammonite)

// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

import $file.Tokens,Tokens.Tokens
import $file.ERC20,ERC20.ERC20

case class BlockSupply(block:Long,totalHolders:Long,totalSupply:Double)
case class TotalSupply(totalContract:Double,totalHolders:Double)

case class BlockTransfer(addr:String,value:Double)

object TotalSupply{ implicit val rw: RW[TotalSupply] = macroRW }

// several transfers may happen inside one block
// this function aggregates all transfers for an address into a singel BlockTransfer fot his address
// usually this is not a case and could be grearty optimized:
// bts.size == 2 -> no need to process
// if grouped address has only 1 BlockTransfer, don't do any fold
def foldBlockTransfer(bts: Array[BlockTransfer]):Array[BlockTransfer] = { 
  if(bts.size == 2) return bts

  bts.groupBy(_.addr).map{ case(addr,bts) => { if(bts.size==1) bts.head else {val valueAggr = bts.foldLeft(0.0)( _ + _.value); BlockTransfer(addr,valueAggr) }} }.toArray
}

@main
def main(input:String="./UNI-1000.csv", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  val ss = SparkSession.builder().appName("circ-total").config("spark.master", "local").config("default.parallelism",1).config("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain").getOrCreate()
  import ss.implicits._

  val ts0 = Instant.now

  val df = ss.read.option("header", "false").format("com.databricks.spark.csv").option("inferSchema", "true").csv(input).toDF("token_address","from_address","to_address","value","transaction_hash","log_index","block_number")
  df.printSchema()

  // sorted by Block Number !
  val accountBalanceDelta = df.select("from_address","to_address","value","block_number").sort("block_number").flatMap(r => Seq((r.getInt(3),r.getString(0),r.getDecimal(2).negate.toBigInteger),(r.getInt(3),r.getString(1),r.getDecimal(2).toBigInteger)))
  
  val accountBalanceDeltaCollected = accountBalanceDelta.collect
  // iterate and accumulate a list of supplies 
  // don't do groupBy because will require another sorting which is heavy !
  var supply: List[BlockSupply] = List()
  var lastBlock: Long = 0
  var totalHolders = 0L
  var totalSupply = 0.0
  for(i <- 0 to accountBalanceDeltaCollected.size) { 
    val a = accountBalanceDeltaCollected(i); 
    if(lastBlock == 0L || lastBlock != a._3) 
      supply = supply :+ BlockSupply() 
  }

  // Attention: Reduced !
  val accountBalanceDeltaBlock = accountBalanceDelta.collect.groupBy(r => r._1)

  // transfers per block
  val blockTransfers = accountBalanceDeltaBlock.map{ case(block,transfers) => block -> transfers.map(t => BlockTransfer(t._2,t._3.doubleValue)) }
  
  // aggregate transfer if multiple times in a block
  val blockTransfersAggregated = blockTransfers.map{ case(block,bts) => block -> foldBlockTransfer(bts)}

  // aggregate per block
  val blockSupply = blockTransfersAggregated.map{ case(block,bts) => BlockSupply()}

  val holdersBalance = holdersAll.collect.map( r => (r.get(0).toString,balanceOf(Tokens.UNI,r.get(0).toString)))
  
  // need to resort because balance is different now
  val holders = holdersBalance.map( h => Holder(h._1,h._2.doubleValue / decimals)).sortBy(- _.value).take(25).toList

  holders.foreach{ println _ }
  
  //df.write.option("maxRecordsPerFile", batch).option("compression", "gzip").mode("overwrite").format(codec).save(output);
  
  val ts1 = Instant.now
  val elapsed = Duration.between(ts0, ts1)
  println(s"Elapsed time: ${elapsed.toMinutes} min (${elapsed.toSeconds} sec)")

  // val circ = Circulation(holders)
  // val circJson = write(circ)
  // println(circJson)
}