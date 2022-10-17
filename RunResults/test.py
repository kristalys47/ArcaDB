import requests

# api-endpoint
URL = "http://136.145.77.80:7271/database/query"
json = {
    "mode": 3,
    "buckets": 60,
    "query": "select * from part, lineitem where lineitem.\"01\" = part.\"00\"",
    "query1": "select * from lineitem, orders where lineitem.\"00\" = orders.\"00\""
}

# sending get request and saving the response as response object
for n in range(1):
    r = requests.get(url = URL, json = json)
    print(r.elapsed)
