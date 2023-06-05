#!/usr/bin/env python
# coding: utf-8

# In[1]:


import torch
import torch.nn
import pandas as pd


# In[ ]:





# First setup the dictionaries to mapp from chars to positions.
characters = list("""ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,;.!?:'\"/\\|_@#$%^&*~`+-=<>()[]{}""")
print(characters)
print(len(characters))
character_to_integer = dict((c, i) for i, c in enumerate(characters))
integer_to_character = dict((i, c) for i, c in enumerate(characters))
print(character_to_integer)
print(integer_to_character)
# Let us no look at how a SMILE string gets mapped to indices

# In[3]:


test_str = "[2H]C1=C(C(=C(C(=C1NC(=O)C)[2H])[2H])O)[2H]"
integer_encoded = [character_to_integer[char] for char in test_str]
print(integer_encoded)
print(len(integer_encoded))


# Let's create a one-hot encoding function

# In[4]:


def one_hot_encode(target, character_set_len:int):
    one_hot = list()
    for value in target:
        #first make everything a 0
        L = [0 for _ in range(character_set_len)]
        # now add one at the position of this character
        L[value] = 1
        one_hot.append(L)
    return one_hot


# Test one-hot encoding

# In[5]:


one_hot=one_hot_encode(integer_encoded,len(characters))
print(one_hot)
print(len(one_hot)) #43 one-hot vectors of size 94
print(len(one_hot[0]))


# Create a data set from the smiles data. The data has 1,129,199 rows. The smiles data has string with average size of 56, max size of 1329. But there are 1,061,957 row with size less than 100. So, we will use that becuase it is 94% of the data.

# In[6]:


from torch.utils.data import Dataset
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
        #filter out those lines with canonical smiles string longer than 100
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





# Create a data set and training set and setup their data loaders
# batch_size = 10
#

# In[7]:


from torch.utils.data import DataLoader
smiles_file = "./master_corpus2.csv"
training_data = SMILESDataSet(smiles_file, train=True)
print("training data len: ", training_data.__len__())
test_data = SMILESDataSet(smiles_file, train=False)
print("test data len: ", test_data.__len__())

batch_size = 64
train_dataloader = DataLoader(training_data, batch_size=batch_size, shuffle=False)
test_dataloader = DataLoader(test_data, batch_size=batch_size, shuffle=False)


# Lets see a few items from the training set

# In[8]:


train_features, train_labels = next(iter(train_dataloader))
print("train_features.shape", train_features.shape)
print("train_labels.shape", train_labels.shape)
C = list("""ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,;.!?:'\"/\\|_@#$%^&*~`+-=<>()[]{}""")
print("len(C): " , len(C))
print("train features[0]", train_features[0] )
print("train labels[0[]]", train_labels[0] )


# Now the create then NN model

# In[9]:


import torch.nn as nn

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

model =  CharacterLevelCNN(128, 94, 64)
print("train_features,shape: ", train_features.shape)
out = model(train_features)
#M = out.view(10, -1)
print("out.shape: ", out.shape)
#print("M.shape: ", M.shape)



# Look at the network

# In[10]:


from torchinfo import summary
summary(model, input_size=train_features.shape, device='cpu', col_names=['input_size', 'output_size',
                                                                               'num_params'])


# setup device

# In[11]:


device = "cuda" if torch.cuda.is_available() else "mps" if torch.backends.mps.is_available() else "cpu"
print("device: ", device)
model = model.to(device)


# Setup cost criterion and optimizer

# In[12]:


#Cost function
criterion = torch.nn.MSELoss().to(device)

optimizer = torch.optim.Adam(model.parameters())


# Training loop

# In[13]:


#arrat to keep loss after each iteration
loss_list = []
# number of iterations
epochs = 20


# In[ ]:





# In[14]:


for e  in range(epochs):
    running_loss = 0.
    last_lost = 0.
    for i, data in enumerate(train_dataloader):
        X, Y = data
        X = X.to(device)
        Y = Y.to(device)
        #setup optimizer to zero grandients
        optimizer.zero_grad()
        # Make predictions for the all the examples in X (vectorization)
        Y_pred = model.forward(X)
        # now calculate the loss
        Y = Y.unsqueeze(1)
        loss = criterion(Y_pred, Y)
        #append the lost to the list
        #loss_list.append(loss.item())
        #back propagation step
        loss.backward()
        #parameter update
        optimizer.step()

        running_loss += loss.item()
        if i % 1000 == 999:
            last_loss = running_loss / 1000 # loss per batch
            loss_list.append(last_loss)
            print('  batch {} loss: {}'.format(i + 1, last_loss))
            #tb_x = epoch_index * len(training_loader) + i + 1
            #tb_writer.add_scalar('Loss/train', last_loss, tb_x)
            running_loss = 0.
    # print diagnostic data
    #print('{}, \t{}, \t{}'.format(i, loss.item(), [param.data for param in model.parameters()]))

    #with torch.no_grad():




# In[15]:


import matplotlib.pyplot as plt
plt.plot(loss_list, 'r')
plt.tight_layout()
plt.grid('True', color='y')
plt.xlabel("Epochs/Iterations")
plt.ylabel("Loss")
plt.show()


# Save the model

# In[16]:


PATH = './mymodelkrFIXED.pth'
torch.save(model, PATH)


# In[ ]:
