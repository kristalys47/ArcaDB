import cv2
import redis
import json
import alluxio
import numpy as np
import os
from keras.models import Sequential, Model, load_model
from alluxio import option
r = redis.Redis(host='localhost', port=6379)
client = alluxio.Client('136.145.77.107', 39999)

def imgClas(directory, property, boolean):
    print(100)

def start():
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    # type, file_location, function_name, property, boolean
    print(task)
    json_dic = json.loads(task)

    print(json_dic)

    if json_dic["plan"]:
        array = json_dic["plan"]
        for n in array:
            print(n)

    list = client.ls("/image")

    for n in list:
        print(n)

def testModel():
    model = load_model('saved_model/model')
    model.load_weights('saved_model/weights')
    print('Model Loaded!')
    im = cv2.imread("archive/img_align_celeba/img_align_celeba/000001.jpg")
    im = cv2.resize(cv2.cvtColor(im, cv2.COLOR_BGR2RGB), (178, 218)).astype(np.float32) / 255.0
    im = np.expand_dims(im, axis=0)
    result = model.predict(im)
    print(result)

def createMetadata():
    meta = {}
    meta["files"] = os.listdir("archive/img_align_celeba/img_align_celeba/")
    json_data = json.dumps(meta)

    with open('metadata.json', 'w') as f:
        f.write(json_data)


def startLocal():
    meta_file = "metafile.json"
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    # type, file_location, function_name, property, boolean
    print(task)
    # json_dic = json.loads(task)
    #
    # print(json_dic)
    #
    # if json_dic["plan"]:
    #     array = json_dic["plan"]
    #     for n in array:
    #         print(n)
    #
    # list = client.ls("/image")
    json_meta = {}

    with open("metadata.json", "r") as f:
         json_meta = json.load(f)

    json_results = {}
    list = []

    model = load_model('saved_model/model')
    model.load_weights('saved_model/weights')



    for n in json_meta["files"]:
        print(n)
        im = cv2.imread("archive/img_align_celeba/img_align_celeba/" + n)
        im = cv2.resize(cv2.cvtColor(im, cv2.COLOR_BGR2RGB), (178, 218)).astype(np.float32) / 255.0
        im = np.expand_dims(im, axis=0)
        result = model.predict(im)
        prediction = np.argmax(result)
        if prediction == 0:
            list.append(n)

    json_results["result"] = list
    with open("results.json") as f:
        f.write(json_results)

startLocal()
# testModel()
