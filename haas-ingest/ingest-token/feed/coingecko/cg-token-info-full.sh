TOKEN=${1:-uniswap}

curl -X 'GET' \
  "https://api.coingecko.com/api/v3/coins/$TOKEN" \
  -H 'accept: application/json'

