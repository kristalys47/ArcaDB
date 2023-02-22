import cv2
import redis
import json
import alluxio
import numpy as np
from keras.models import Sequential, Model, load_model
from alluxio import option
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

# start()

testModel()