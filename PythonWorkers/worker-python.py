import uuid
import redis
import torch
import torch.nn as nn
import pandas as pd
from PIL import Image
from torchvision import datasets, models, transforms
from torch.utils.data import Dataset, DataLoader
import json
import alluxio
import os
from alluxio import option, wire
import time
import socket
from dotenv import load_dotenv
load_dotenv()

# First setup the dictionaries to mapp from chars to positions.
characters = list("""ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,;.!?:'\"/\\|_@#$%^&*~`+-=<>()[]{}""")
character_to_integer = dict((c, i) for i, c in enumerate(characters))
integer_to_character = dict((i, c) for i, c in enumerate(characters))


def one_hot_encode(target, character_set_len:int):
    one_hot = list()
    for value in target:
        #first make everything a 0
        L = [0 for _ in range(character_set_len)]
        # now add one at the position of this character
        L[value] = 1
        one_hot.append(L)
    return one_hot


class SMILESDataSet(Dataset):
    def __init__(self, smiles_file, train=True, max_str_len = 100, transform=None, target_transform=None):
        super(SMILESDataSet, self).__init__()
        self.smiles_file = smiles_file
        self.max_str_len = max_str_len
        if(train == True):
            self.size = 200000
        else:
            self.size = 50000
        self.transform = transform
        self.target_transform = target_transform
        #character set
        self.characters = list("""ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,;.!?:'\"/\\|_@#$%^&*~`+-=<>()[]{}""")
        self.character_to_integer = dict((c, i) for i, c in enumerate(characters))
        self.integer_to_character = dict((i, c) for i, c in enumerate(characters))

        #open csv
        self.smiles_csv = pd.read_csv(smiles_file)
        self.smiles_csv = self.smiles_csv[self.smiles_csv.apply(lambda x: len(x['CanonicalSMILES']) <= 100, axis=1)]
        #sample data based on size
        self.smiles_data =  self.smiles_csv.sample(n=self.size).reset_index()


    def __len__(self):
        return self.size

    def __getitem__(self, idx):
        #print("Index: ", idx)
        #print("Row: \n", self.smiles_data.iloc[idx])
        X = self.smiles_data.iloc[idx, 3] # Canonical smiles
        #print("X: ", X)
        y = self.smiles_data.iloc[idx, 5] # molecular weight
        #print("y: ", y)
        label = torch.tensor(y, dtype=torch.float32)
        #label = torch.FloatTensor(y)
        data = self.one_hot_encode(X, len(self.characters))
        data = torch.tensor(data, dtype=torch.float32)
        #data = torch.FloatTensor(data)
        #print("data.shape: ", data.shape)
        data = data.transpose(0, 1)
        return data, label

    def get_info(self, index):
        X1 = self.smiles_data.iloc[index, 0]
        X2 = self.smiles_data.iloc[index, 1]
        X3 = self.smiles_data.iloc[index, 2]
        X4 = self.smiles_data.iloc[index, 3]
        return X1, X2, X3, X4

    def one_hot_encode(self, target, character_set_len:int):
        integer_encoded = [self.character_to_integer[char] for char in target]
        one_hot = list()
        for value in integer_encoded:
            #first make everything a 0
            L = [0 for _ in range(character_set_len)]
            # now add one at the position of this character
            L[value] = 1
            one_hot.append(L)
        one_hot = self.zero_pad(one_hot, self.max_str_len, character_set_len)
        return one_hot

    def zero_pad(self, one_hot, max_str_len, character_set_len):
        L = [0 for _ in range(character_set_len)]
        while (len(one_hot) < max_str_len):
            one_hot.append(L)
        return one_hot


class CharacterLevelCNN(nn.Module):
    def __init__(self, input_length, input_dim, n_conv_filters, n_fc_neurons=128, kernel_size=7, padding=0):
        super(CharacterLevelCNN, self).__init__()

        self.conv1 = nn.Sequential(nn.Conv1d(input_dim, n_conv_filters, kernel_size, stride=1, padding=padding), nn.ReLU(),
                                   nn.MaxPool1d(3))
        self.conv2 = nn.Sequential(nn.Conv1d(n_conv_filters, n_conv_filters, kernel_size, stride=1, padding=padding), nn.ReLU(),
                                   nn.MaxPool1d(3))
        self.conv3 = nn.Sequential(nn.Conv1d(n_conv_filters, n_conv_filters, kernel_size=3, stride=1, padding=padding), nn.ReLU())
        self.conv4 = nn.Sequential(nn.Conv1d(n_conv_filters, n_conv_filters, kernel_size=3, stride=1, padding=padding), nn.ReLU())
        self.conv5 = nn.Sequential(nn.Conv1d(n_conv_filters, n_conv_filters, kernel_size=3, stride=1, padding=padding), nn.ReLU())
        self.fc1 = nn.Sequential(nn.Linear(input_length, n_fc_neurons), nn.ReLU(), nn.Dropout(0.2))
        self.fc2 = nn.Sequential(nn.Linear(n_fc_neurons, 64), nn.ReLU(),nn.Dropout(0.2))
        self.fc3 = nn.Sequential(nn.Linear(64, 32), nn.ReLU(),nn.Dropout(0.2))
        self.fc4 = nn.Sequential(nn.Linear(32, 1))

    def forward(self, X):
        output = self.conv1(X)
        output = self.conv2(output)
        output = self.conv3(output)
        output = self.conv4(output)
        output = self.conv5(output)
        output = output.view(output.shape[0], -1)
        output = self.fc1(output)
        output = self.fc2(output)
        output = self.fc3(output)
        output = self.fc4(output)

        return output













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
    start = time.time() * 1000
    for n in json_meta:
        with client.open("/" + relation + "/" + n, "r") as f:
            file = f.read()
            with open("dataset.csv", "wb") as f2:
                f2.write(file)
        id = str(uuid.uuid4())
        ## TODO hard coded name

        # file_name = '/results/smiles/results_' + id + '.json'
        # opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
        # with cache.open(file_name, 'w', opt) as alluxio_file:
        #     # json_results.dump(json_results, alluxio_file)
        #     alluxio_file.write(json.dumps(json_results))

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
                X1, X2, X3, X4 = training_data.get_info(i)
                record = [X1, X2, X3, X4, pred]
                list[record[0]] = record
    json_results["result"] = list

    file_name = '/results/smiles/results_' + id + '.json'
    opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
    with cache.open(file_name, 'w', opt) as alluxio_file:
        # json_results.dump(json_results, alluxio_file)
        alluxio_file.write(str(json_results))



    # opt = alluxio.option.CreateFile(write_type=wire.WRITE_TYPE_CACHE_THROUGH, recursive=True)
    # with cache.open(file_name, 'w', opt, ) as alluxio_file:
    #     json_results.dump(json_results, alluxio_file)


    r.rpush("done", "\nSuccessful: " + str(ip))
    end = time.time() * 1000
    print("TIME_LOG: Classifier " + str(ip) + " " + str(start) + " " + str(end) + " " + str(end - start))



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

    if  attribute[0] == "molecular_weight":
        if models["molecular_weight"] == NOT_LOADED:
            with client.open("/models/molecular_weight/mymodelkrFIXED.pth", "r") as f:
                os.makedirs("saved_model/molecular_weight/", exist_ok=True)
                with open("saved_model/molecular_weight/mymodelkrFIXED.pth", "wb") as lf:
                    lf.write(f.read())
            models["molecular_weight"] = torch.load("saved_model/molecular_weight/mymodelkrFIXED.pth", map_location=device)
            print("Model is loaded")
        smile_predictions(json_plan, models["molecular_weight"], attribute[1], json_plan["relation"])


models = {}
models["gender"] = NOT_LOADED
models["molecular_weight"] = NOT_LOADED
print(ip, " ", REDIS_HOST, " ", REDIS_PORT)
while True:
    start(models)
