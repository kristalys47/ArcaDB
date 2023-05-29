import torch
import torch.nn as nn
import pandas as pd
from torch.utils.data import DataLoader
from torch.utils.data import Dataset
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
