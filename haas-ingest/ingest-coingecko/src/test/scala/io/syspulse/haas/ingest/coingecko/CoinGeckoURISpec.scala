package io.syspulse.haas.ingest.coingecko

import org.scalatest.{Ignore}
import org.scalatest.wordspec.{ AnyWordSpec}
import org.scalatest.matchers.should.{ Matchers}
import org.scalatest.flatspec.AnyFlatSpec

import java.time._
import io.jvm.uuid._
import io.syspulse.skel.util.Util
import scala.util.Success

class UriSpec extends AnyWordSpec with Matchers {
  
  "CoinGeckoURI" should {
    
    "url ('coingecko://') -> 'https://api.coingecko.com/api/v3'" in {      
      val u = CoinGeckoURI("coingecko://")
      u.uri should === ("https://api.coingecko.com/api/v3")
    }

    "url ('coingecko://') -> 'https://api.coingecko.com/api/v3/tokens?k1=v1&k2=v2'" in {      
      val u = CoinGeckoURI("coingecko://","/tokens?k1=v1&k2=v2")
      u.uri should === ("https://api.coingecko.com/api/v3/tokens?k1=v1&k2=v2")
    }

    "url ('coingecko://NEW-API.coingecko.com/') -> 'https://NEW-API.coingecko.com/api/v3'" in {
      val u = CoinGeckoURI("coingecko://NEW-API.coingecko.com")
      u.uri should === ("https://NEW-API.coingecko.com/api/v3")
    }

    "url ('coingecko://KEY@') -> 'https://pro-api.coingecko.com/api/v3?x_cg_pro_api_key=KEY'" in {
      val u = CoinGeckoURI("coingecko://KEY@")
      u.uri should === ("https://pro-api.coingecko.com/api/v3?x_cg_pro_api_key=KEY")
    }

    "url ('coingecko://{USER}@') -> 'https://pro-api.coingecko.com/api/v3?x_cg_pro_api_key=<user>'" in {
      val apiKey = sys.env("USER")
      val u = CoinGeckoURI("coingecko://{USER}@")
      u.uri should === (s"https://pro-api.coingecko.com/api/v3?x_cg_pro_api_key=${apiKey}")
    }

    "url ('coingecko://{USER}@') with apiSuffix -> 'https://pro-api.coingecko.com/api/v3/prices?k1=v1&x_cg_pro_api_key=<user>'" in {
      val apiKey = sys.env("USER")
      val u = CoinGeckoURI("coingecko://{USER}@","/prices?k1=v1")
      u.uri should === (s"https://pro-api.coingecko.com/api/v3/prices?k1=v1&x_cg_pro_api_key=${apiKey}")
    }
    
    
  }
}
