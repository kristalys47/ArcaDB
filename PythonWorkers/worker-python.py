import uuid
import redis
import torch
import torch.nn as nn
import torch.optim as optim
import torchvision
from PIL import Image
from torchvision import datasets, models, transforms
import json
import alluxio
import numpy as np
import os
from alluxio import option, wire
import time
import socket

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

hostname = socket.gethostname()
ip = socket.gethostbyname(hostname)

r = redis.Redis(host='136.145.77.83', port=6379)
client = alluxio.Client('136.145.77.107', 39999)
cache = alluxio.Client('136.145.77.83', 39999)
NOT_LOADED = "not_loaded"



def gender_clasiffication_pytorch(plan, gender_model):
    json_results = {}
    json_meta = plan["files"]
    list = []
    transforms_val = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])
    # i = 0
    start = time.time() * 1000
    for n in json_meta:
        # print("Progress: {:2} ".format(i/len(json_meta)*100))
        # i = i + 1
        im = ''
        with client.open("/image/" + n, "r") as f:
            im = f.read()
            with open("img.jpg", "wb") as f2:
                f2.write(im)

        nparr = np.frombuffer(im, np.uint8)

        img = Image.fromarray(nparr)
        img_trans = transforms_val(img)
        img_trans = img_trans.to(device)

        gender_model.to(device)

        with torch.no_grad():
            gender_model.eval()
            output = gender_model(img_trans.unsqueeze(0))
            _, pred = torch.max(output, 1)
            print(output + " " + pred)
        if pred == 0:
            list.append(n)
    end = time.time() * 1000
    json_results["result"] = list

    id = str(uuid.uuid4())
    file_name = '/results/results_' + id + '.json'
    opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
    with cache.open(file_name, 'w', opt, ) as alluxio_file:
        json.dump(json_results, alluxio_file)


    jsonResponse = {}
    jsonResponse["file"] = file_name
    jsonResponse["status"] = "Completed"
    r.rpush("donePython", str(jsonResponse))
    print("TIME_LOG: Classifier " + str(ip) + " " + str(start) + " " + str(end) + " " + str(end - start))


def start(models):
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    print("Recieved plan: " + task)
    # type, file_location, modelname, property, boolean, array

    json_dic = json.loads(task)
    json_plan = json_dic["plan"]
    print(json_plan["model"])
    if json_plan["model"] == "gender":
        print("Gender model selected")
        if models["gender"] == NOT_LOADED:
            with client.open("/models/gender/pytorch_gender.pth", "r") as f:
                os.makedirs("saved_model/gender/", exist_ok=True)
                with open("saved_model/gender/pytorch_gender", "wb") as lf:
                    lf.write(f.read())
            models["gender"] = torch.load("pytorch_gender.pth")
            print("Model is loaded")
        gender_clasiffication_pytorch(json_plan, models["gender"])

models = {}
models["gender"] = NOT_LOADED
print(ip)
while True:
    start(models)
