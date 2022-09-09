// Does not work, use --predef circ-imports.sc
//import $file.`circ-imports`, `circ-imports`._

import $file.Tokens,Tokens.Tokens
import $file.ERC20,ERC20.ERC20

@main
def main(input:String="./UNI-1000.csv", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(Tokens.UNI)
  
  println(ERC20.name(Tokens.UNI))
  println(ERC20.symbol(Tokens.UNI))
  println("%.2f".format(new BigInt(ERC20.totalSupply(Tokens.UNI)).doubleValue / 1e18))
  println("%.2f".format(new BigInt(ERC20.balanceOf(Tokens.UNI,"0x47173b170c64d16393a52e6c480b3ad8c302ba1e")).doubleValue / 1e18))
}