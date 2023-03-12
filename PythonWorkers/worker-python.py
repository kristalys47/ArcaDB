import uuid
import redis
import torch
from PIL import Image
from torchvision import datasets, models, transforms
import json
import alluxio
import os
from alluxio import option, wire
import time
import socket

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

hostname = socket.gethostname()
ip = socket.gethostbyname(hostname)

REDIS_HOST = os.getenv('REDIS_HOST')
REDIS_PORT = os.getenv('REDIS_PORT')

ALLUXIO_DATA_HOST = os.getenv('ALLUXIO_DATA_HOST')
ALLUXIO_DATA_PORT = os.getenv('ALLUXIO_DATA_PORT')

ALLUXIO_CACHE_HOST = os.getenv('ALLUXIO_CACHE_HOST')
ALLUXIO_CACHE_PORT = os.getenv('ALLUXIO_CACHE_PORT')


r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT)
client = alluxio.Client(ALLUXIO_DATA_HOST, ALLUXIO_DATA_PORT)
cache = alluxio.Client(ALLUXIO_CACHE_HOST, ALLUXIO_CACHE_PORT)
NOT_LOADED = "not_loaded"

def write_result(results, id):


def gender_clasiffication_pytorch(plan, gender_model, selection, relation):
    json_results = {}
    json_meta = plan["files"]
    list = []
    transforms_val = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    start = time.time() * 1000
    for n in json_meta:
        with client.open("/" + relation + "/" + n, "r") as f:
            im = f.read()
            with open("img.jpg", "wb") as f2:
                f2.write(im)

        img = Image.open("img.jpg")
        img_trans = transforms_val(img)
        img_trans = img_trans.to(device)

        gender_model.to(device)

        with torch.no_grad():
            gender_model.eval()
            output = gender_model(img_trans.unsqueeze(0))
            _, pred = torch.max(output, 1)
        if pred == int(selection):
            list.append(n)
    end = time.time() * 1000
    json_results["result"] = list

    id = str(uuid.uuid4())
    file_name = '/results/results_' + id + '.json'
    # opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
    # with cache.open(file_name, 'w', opt, ) as alluxio_file:
    #     json.dump(json_results, alluxio_file)

    jsonResponse = {}
    jsonResponse["planType"] =  "inference"
    jsonResponse["file"] = file_name
    jsonResponse["result"] = list
    jsonResponse["buckets"] = plan["buckets"]
    jsonResponse["relation"] = relation
    r.rpush("structured", str(jsonResponse))
    print("TIME_LOG: Classifier " + str(ip) + " " + str(start) + " " + str(end) + " " + str(end - start))


def start(models):
    encoding = "utf-8"
    task = r.blpop("semistructured", 0)[1].decode(encoding)
    # type, file_location, modelname, property, boolean, array
    print("Have a plan!")
    json_plan = json.loads(task)
    attribute = json_plan["filter"].split("=")
    if  attribute[0] == "gender":
        if models["gender"] == NOT_LOADED:
            with client.open("/models/gender/pytorch_gender.pth", "r") as f:
                os.makedirs("saved_model/gender/", exist_ok=True)
                with open("saved_model/gender/pytorch_gender.pth", "wb") as lf:
                    lf.write(f.read())
            models["gender"] = torch.load("saved_model/gender/pytorch_gender.pth", map_location=device)
            print("Model is loaded")
        gender_clasiffication_pytorch(json_plan, models["gender"], attribute[1], attribute[0])

models = {}
models["gender"] = NOT_LOADED
print(ip, " ", REDIS_HOST, " ", REDIS_PORT)
while True:
    start(models)
