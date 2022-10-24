import requests
import sys, json
import os

# api-endpoint
URL = "http://136.145.77.80:7271/database/query"


# sending get request and saving the response as response object
for n in range (70, 181, 10):
    json = {
        "mode": 3,
        "buckets": n,
        "query": "select * from part, lineitem where lineitem.\"01\" = part.\"00\"",
        "query1": "select * from lineitem, orders where lineitem.\"00\" = orders.\"00\""
        }
    print(n)
    for n in range(3):
        os.system("sh clear.sh")
        r = requests.get(url = URL, json = json)
        print(r.elapsed)
        sys.stdout.flush()
