import torch
import torch.nn as nn
import torch.optim as optim
import torchvision
from PIL import Image
from torchvision import datasets, models, transforms
import numpy as np
import time
import os

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu") # device objecttra

transforms_val = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
])


model = torch.load("pytorch_gender.pth")
model.to(device)


start_time = time.time()


img = Image.open("gender_classification_dataset/Training/female/182491.jpg.jpg")
img_trans = transforms_val(img)
img_trans = img_trans.to(device)
print("img_trans: ", img_trans.shape)
with torch.no_grad():
    model.eval()
    output = model(img_trans.unsqueeze(0))
    print(output)
    # _, preds = torch.max(outputs, 1)
