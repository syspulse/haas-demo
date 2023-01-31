   
var t = null;
switch(token_address) {
    case "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984": t = "UNI"; break;
    case "0xdac17f958d2ee523a2206206994597c13d831ec7": t = "USDT"; break;
    case "0xb7277a6e95992041568d9391d09d0122023778a2": t = "USDC"; break;
}
if(t != null ) {
   t + " : " + value / 10e9 + ": " + from_address + " -> " + to_address; 
} else null
