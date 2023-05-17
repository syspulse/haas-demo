package io.syspulse.haas.token.store

import scala.util.Try
import scala.util.{Success,Failure}
import scala.collection.immutable

import com.typesafe.scalalogging.Logger

import io.syspulse.haas.core.Token
import io.syspulse.haas.core.Token.ID

import os._

import spray.json._
import DefaultJsonProtocol._
import io.syspulse.haas.ingest.coingecko.CoinInfo
import io.syspulse.haas.ingest.coingecko.CoinGeckoTokenJson
import io.syspulse.haas.ingest.coingecko._
import io.syspulse.haas.core.DataSource
import io.syspulse.haas.core.TokenBlockchain
import io.syspulse.skel.store.StoreDir

import io.syspulse.haas.serde.TokenJson._
import io.syspulse.haas.token.server.Tokens
import io.syspulse.haas.core.TokenLocks

class TokenStoreDir(dir:String = "store/") extends StoreDir[Token,ID](dir) with TokenStore {
  import CoinGeckoTokenJson._

  val store = new TokenStoreMem()

  def all:Seq[Token] = store.all
  def size:Long = store.size
  override def +(t:Token):Try[TokenStoreDir] = super.+(t).flatMap(_ => store.+(t)).map(_ => this)

  override def del(id:ID):Try[TokenStoreDir] = super.del(id).flatMap(_ => store.del(id)).map(_ => this)
  override def ?(id:ID):Try[Token] = store.?(id)

  def search(txt:Seq[String],from:Option[Int],size:Option[Int]):Tokens = store.search(txt,from,size)
  def search(txt:String,from:Option[Int],size:Option[Int]):Tokens = store.search(txt,from,size)

  def ???(from:Option[Int],size:Option[Int]):Tokens = store.???(from,size)

  def scan(txt:String,from:Option[Int],size:Option[Int]):Tokens = store.scan(txt,from,size)  
  def grep(txt:String,from:Option[Int],size:Option[Int]):Tokens = store.grep(txt,from,size)
  def typing(txt:String,from:Option[Int],size:Option[Int]):Tokens = store.typing(txt,from,size)

  override def update(id:ID, symbol:Option[String] = None, name:Option[String] = None, addr: Option[String] = None,
             cat:Option[List[String]] = None, icon:Option[String] = None, dcml:Option[Int] = None,
             contracts:Option[Seq[TokenBlockchain]] = None, locks:Option[Seq[TokenLocks]] = None):Try[Token] = 
    store.update(id,symbol,name,addr,cat,icon,dcml,contracts,locks).flatMap(t => writeFile(t))

  // load in Coingecko format
  loadCoingGecko(dir)

  // preload
  load(dir,hint = "\"cat\"")

  def loadCoingGecko(dir:String) = {
    val storeDir = os.Path(dir,os.pwd)
    log.info(s"Loading dir store: ${storeDir}")

    loading = true
    val vv = os.walk(storeDir)
      .filter(_.toIO.isFile())
      .map(f => {
        log.info(s"Loading file: ${f}")
        os.read(f)
      })
      .map(fileData => fileData.split("\n").map { data =>
        try {
          if(data.contains("asset_platform_id")) {
            val cg = data.parseJson.convertTo[CoinInfo]
            log.debug(s"coin=${cg}")
            Seq(cg.toToken)
          } else 
            // skip
            Seq()
        } catch {
          case e:Exception => 
            log.error(s"could not parse data: ${data}",e); 
            Seq()
        }
      })
      .flatten // file
      .flatten // files

    vv.foreach(v => this.+(v))

    loading = false

    log.info(s"Loaded store: ${size}")
  }

}