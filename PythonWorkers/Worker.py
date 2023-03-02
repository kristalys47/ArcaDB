import uuid
import cv2
import redis
import json
import alluxio
import numpy as np
import os
from keras.models import Sequential, Model, load_model
from alluxio import option, wire
import time
import socket

hostname = socket.gethostname()
ip = socket.gethostbyname(hostname)

r = redis.Redis(host='136.145.77.83', port=6379)
client = alluxio.Client('136.145.77.107', 39999)
cache = alluxio.Client('136.145.77.83', 39999)
NOT_LOADED = "not_loaded"



def gender_clasiffication(plan, gender_model):
    json_results = {}
    json_meta = plan["files"]
    list = []
    # i = 0
    start = time.time() * 1000
    for n in json_meta:
        # print("Progress: {:2} ".format(i/len(json_meta)*100))
        # i = i + 1
        im = ''
        with client.open("/image/" + n, "r") as f:
            im = f.read()

        nparr = np.frombuffer(im, np.uint8)
        im = cv2.imdecode(nparr, flags=1)
        im = cv2.resize(cv2.cvtColor(im, cv2.COLOR_BGR2RGB), (178, 218)).astype(np.float32) / 255.0
        im = np.expand_dims(im, axis=0)
        result = gender_model.predict(im, verbose=0)
        prediction = np.argmax(result)
        if prediction == 0:
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
            with client.open("/models/gender/gender.h5", "r") as f:
                os.makedirs("saved_model/gender/", exist_ok=True)
                with open("saved_model/gender/gender.h5", "wb") as lf:
                    lf.write(f.read())
            models["gender"] = load_model('saved_model/gender/gender.h5', compile=False)
            print("Model is loaded")
        gender_clasiffication(json_plan, models["gender"])

models = {}
models["gender"] = NOT_LOADED
print(ip)
while True:
    start(models)
