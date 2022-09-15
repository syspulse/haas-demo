TOKEN=${1:-uniswap}

curl -X 'GET' \
  "https://api.coingecko.com/api/v3/coins/$TOKEN?localization=false&tickers=false&market_data=false&community_data=false&developer_data=false&sparkline=false" \
  -H 'accept: application/json'

