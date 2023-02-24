import uuid

import cv2
import redis
import json
import alluxio
import numpy as np
import os
from keras.models import Sequential, Model, load_model
from alluxio import option, wire

r = redis.Redis(host='136.145.77.83', port=6379)
client = alluxio.Client('136.145.77.107', 39999)


def imgClas(directory, property, boolean):
    print(100)


def start():
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    # type, file_location, function_name, property, boolean
    print(task)
    json_dic = json.loads(task)

    json_meta = {}

    with open("metadata.json", "r") as f:
        json_meta = json.load(f)

    json_results = {}
    list = []

    model = load_model('saved_model/model')
    model.load_weights('saved_model/weights')
    print("Loaded model!")
    with client.open("/image/metadata.json", "r") as f:
        json_meta = json.load(f)

    for n in json_meta["files"]:
        print(n)
        im = ''
        with client.open("/image/" + n, "r") as f:
            im = f.read()

        nparr = np.frombuffer(im, np.uint8)
        im = cv2.imdecode(nparr, flags=1)
        im = cv2.resize(cv2.cvtColor(im, cv2.COLOR_BGR2RGB), (178, 218)).astype(np.float32) / 255.0
        im = np.expand_dims(im, axis=0)
        result = model.predict(im)
        prediction = np.argmax(result)
        if prediction == 0:
            list.append(n)

    json_results["result"] = list

    id = str(uuid.uuid4())
    cache = alluxio.Client('136.145.77.83', 39999)
    opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH)
    with cache.open('/results_' + id + '.json', 'w', opt) as alluxio_file:
        json.dump(json_results, alluxio_file)

    r.rpush("donePython", '/results_' + id + '.json')


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


def check_results():
    cache = alluxio.Client('136.145.77.83', 39999)
    with cache.open("/alluxio-file", "r") as f:
        json_meta = json.load(f)
        print(json_meta)


start()
# startLocal()
# testModel()
