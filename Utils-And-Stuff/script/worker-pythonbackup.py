import uuid
import redis
import torch
import torch.nn
import pandas as pd
from PIL import Image
from torchvision import datasets, models, transforms
from torch.utils.data import Dataset
import json
import alluxio
import os
from alluxio import option, wire
import time
import socket
from dotenv import load_dotenv
load_dotenv()

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

hostname = socket.gethostname()
ip = socket.gethostbyname(hostname)

REDIS_HOST = os.getenv('REDIS_HOST')
REDIS_PORT = int(os.getenv('REDIS_PORT'))

ALLUXIO_DATA_HOST = os.getenv('ALLUXIO_DATA_HOST')
ALLUXIO_DATA_PORT = int(os.getenv('ALLUXIO_DATA_PORT'))

ALLUXIO_CACHE_HOST = os.getenv('ALLUXIO_CACHE_HOST')
ALLUXIO_CACHE_PORT = int(os.getenv('ALLUXIO_CACHE_PORT'))


r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT)
client = alluxio.Client(ALLUXIO_DATA_HOST, ALLUXIO_DATA_PORT)
cache = alluxio.Client(ALLUXIO_CACHE_HOST, ALLUXIO_CACHE_PORT)
NOT_LOADED = "not_loaded"


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

def smile_predictions(plan, model1, selection, relation):
    json_results = {}
    json_meta = plan["files"]
    list = {}
    for n in json_meta:
        with client.open("/" + relation + "/" + n, "r") as f:
            file = f.read()
            with open("dataset.csv", "w") as f2:
                f2.write(file)
        model1.to(device)
        training_data = SMILESDataSet("dataset.csv", train=True)
        train_dataloader = DataLoader(training_data, batch_size=1, shuffle=False)


        for i, data in enumerate(train_dataloader):
            X, Y = data
            X = X.to(device)

            with torch.no_grad():
                model1.eval()
                output = model1(X)
                # _, pred = torch.max(output, 1)
                # print(pred)
                pred = output.item()
                print(pred)
                X1, X2, X3, X4 = training_data.get_info(i)
                record = [X1, X2, X3, X4, pred]
                list[record[0]] = record
    json_results["result"] = list


def start(models):
    ## TODO: Add Entity filtering for easier access
    ## TODO: Add un equal thingies here...
    encoding = "utf-8"
    task = r.blpop("semistructured", 0)[1].decode(encoding)
    # type, file_location, modelname, property, boolean, array
    print("Have a plan!")
    json_plan = json.loads(task)
    attribute = json_plan["filter"].replace("(", "").replace(")", "").split("=")
    print(attribute)
    if  attribute[0] == "gender":
        if models["gender"] == NOT_LOADED:
            with client.open("/models/gender/pytorch_gender.pth", "r") as f:
                os.makedirs("saved_model/gender/", exist_ok=True)
                with open("saved_model/gender/pytorch_gender.pth", "wb") as lf:
                    lf.write(f.read())
            models["gender"] = torch.load("saved_model/gender/pytorch_gender.pth", map_location=device)
            print("Model is loaded")
        gender_clasiffication_pytorch(json_plan, models["gender"], attribute[1], json_plan["relation"])

    if  attribute[0] == "smile":
        if models["smile"] == NOT_LOADED:
            with client.open("/models/smile/mymodelkrFIXED.pth", "r") as f:
                os.makedirs("saved_model/smile/", exist_ok=True)
                with open("saved_model/smile/mymodelkrFIXED.pth", "wb") as lf:
                    lf.write(f.read())
            models["smile"] = torch.load("saved_model/smile/mymodelkrFIXED.pth", map_location=device)
            print("Model is loaded")
        gender_clasiffication_pytorch(json_plan, models["smile"], attribute[1], json_plan["relation"])


models = {}
models["gender"] = NOT_LOADED
models["smile"] = NOT_LOADED
print(ip, " ", REDIS_HOST, " ", REDIS_PORT)
while True:
    start(models)