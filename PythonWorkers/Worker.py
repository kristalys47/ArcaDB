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
gender_model = ''



def gender_clasiffication(plan):
    gender_model = load_model('saved_model/gender.h5', compile=False)
    # with client.open("/image/metadata.json", "r") as f:
    #     json_meta = json.load(f)
    print("Model is loaded")
    json_results = {}
    json_meta = plan["files"]
    list = []
    i = 0
    for n in json_meta:
        print("Progress: {:2} ".format(i/len(json_meta)*100))
        i = i + 1
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

    json_results["result"] = list

    id = str(uuid.uuid4())
    cache = alluxio.Client('136.145.77.83', 39999)
    opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
    with cache.open('/results/results_' + id + '.json', 'w', opt, ) as alluxio_file:
        json.dump(json_results, alluxio_file)

    jsonResponse = {}
    jsonResponse["file"] = '/results/results_' + id + '.json'
    jsonResponse["status"] = "Completed"

    r.rpush("donePython", jsonResponse)


def start():
    encoding = "utf-8"
    task = r.blpop("python", 0)[1].decode(encoding)
    print("Recieved plan: " + task)
    # type, file_location, modelname, property, boolean, array

    json_dic = json.loads(task)
    json_plan = json_dic["plan"]
    if json_plan["model"] == "gender":
        print("Gender model selected")
        gender_clasiffication(json_plan)

while True:
    start()
