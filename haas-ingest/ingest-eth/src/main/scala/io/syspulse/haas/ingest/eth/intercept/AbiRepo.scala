package io.syspulse.haas.ingest.eth.intercept

import com.typesafe.scalalogging.Logger

// ATTENTION
import scala.util.Try
import scala.util.Success

import codegen.Decoder
import codegen.AbiDefinition
import os._

abstract class ContractAbi(addr:String,abi:Seq[AbiDefinition]) {
  override def toString = s"${getClass().getSimpleName()}(${addr},${abi.size})"
}

case class ERC20Abi(addr:String,abi:Seq[AbiDefinition],funcTo:String, funcValue:String, tokenName:String="") 
  extends ContractAbi(addr,abi) 
{
  def name:String = tokenName
  override def toString = s"${getClass().getSimpleName()}(${addr},${abi.size},${name},${funcTo},${funcValue})"
}

abstract class AbiRepo {
  protected val log = Logger(s"${this.getClass()}")

  def load():Map[String,ERC20Abi]
}

class AbiRepoFiles(dir:String) extends AbiRepo {

  def load():Map[String,ERC20Abi] = {
    log.info(s"scanning ABI: ${dir}")
    val abis = os.list(os.Path(dir,os.pwd)).flatMap( f => {        
      val (name,addr) = f.last.split("[-.]").toList match {
        case name :: addr :: _ => (name,addr.toLowerCase())
        case name :: Nil => (name,"")
        case _ => ("","")
      }

      if(!addr.isEmpty()) {
        val abi = Decoder.loadAbi(scala.io.Source.fromFile(f.toString).getLines().mkString("\n"))
        if(abi.size != 0) {
          log.info(s"${f}: ${abi.size}")

          val functionName = "transfer"
          // find function names
          val function = abi.filter(d => d.isFunction).find(_.name.get == functionName)
          if(function.isDefined) {

            val parToName = function.get.inputs.get(0).name
            val parValueName = function.get.inputs.get(1).name
            Some(ERC20Abi( addr, abi, parToName, parValueName, name ))

          } else {
            log.warn(s"${f}: could not find function: '${functionName}'")
            None
          }
        } else {
          log.warn(s"${f}: failed to load: ${abi}")
          None
        }
      }
      else {
        log.warn(s"${f}: could not determine addr")
        None
      }
    })

    // map of addr -> TokenAbi
    val erc20s = abis.map(ta => ta.addr -> ta).toMap
    log.info(s"ABI: ${erc20s}")
    erc20s
  }
}

class AbiRepos {
  var repos:List[AbiRepo] = List()
  def withRepo(repo:AbiRepo):AbiRepos = {
    repos = repos :+ repo
    this
  }
  def load():Map[String,ERC20Abi] = {
    repos.foldLeft(Map[String,ERC20Abi]())( (m,r)  => m ++ r.load() )
  }
}

object AbiRepo {
  val FUNC_HASH_SIZE = "0x12345678".size

  def build() = new AbiRepos()
  
}

