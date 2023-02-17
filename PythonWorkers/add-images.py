
import redis

r = redis.Redis(host='136.145.77.83', port=6379)

r.flushall()

tables = ["images"]
r.rpush("images", "//images/")
