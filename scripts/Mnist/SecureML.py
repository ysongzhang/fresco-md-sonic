import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets, transforms
from torch.utils.data import DataLoader
import numpy as np

import os

train_loader = DataLoader(datasets.MNIST("./", train=True, transform=transforms.ToTensor(), download=True), batch_size=128, shuffle=True)
test_loader = DataLoader(datasets.MNIST("./", train=False, transform=transforms.ToTensor(), download=True), batch_size=128, shuffle=True)

print(f"Training images {len(train_loader.dataset)}, Test images {len(test_loader.dataset)}")


class mnist_model(nn.Module):
  def __init__(self):
    super(mnist_model, self).__init__()
    self.layer1 = nn.Linear(784, 128, bias=True)
    self.layer2 = nn.Linear(128, 128, bias=True)
    self.layer3 = nn.Linear(128, 10, bias=True)
    # self.act = nn.Hardtanh()
    self.act = nn.ReLU()

  def forward(self, x):
    return self.layer3(self.act(self.layer2(self.act(self.layer1(x)))))

  def output(self, x):
    out1 = self.act(self.layer1(x))
    out2 = self.act(self.layer2(out1))
    out3 = self.layer3(out2)
    return out1, out2, out3
  
def get_acc(model, loader):
  correct = 0
  total = 0
  for img, label in loader:
    correct += torch.sum(torch.argmax(model(img.view(-1, 784)), -1).cpu() == label).item()
    total += len(img)
  return 100*correct/total
  

model = mnist_model()
print(model)

epochs = 15
lr = 0.1

optimizer = optim.SGD(model.parameters(), lr=lr)
criterion = nn.CrossEntropyLoss()
lrs = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, epochs)

for e in range(epochs):
  print("lr", optimizer.param_groups[0]["lr"])
  for img, label in train_loader:
    out = model(img.view(-1, 784))
    
    optimizer.zero_grad()
    loss = criterion(out, label)
    loss.backward()
    optimizer.step()
  lrs.step()
  print(f"Epoch {e}, training accuracy {get_acc(model, train_loader)}, test accuracy {get_acc(model, test_loader)}")


params = [(name, p.data.cpu().numpy()) for (name, p) in model.named_parameters()]
for (name, p) in params:
    print(f"Layer {name.split('.')[0]}, type {name.split('.')[1]}, shape {p.shape}")

path = "/home/Network/SecureML/"
np.savetxt(fname=path+"weight1", delimiter=" ", X=params[0][1].tolist())
np.savetxt(fname=path+"bias1", delimiter=" ",  X=params[1][1].tolist())
np.savetxt(fname=path+"weight2", delimiter=" ", X=params[2][1].tolist())
np.savetxt(fname=path+"bias2", delimiter=" ", X=params[3][1].tolist())
np.savetxt(fname=path+"weight3", delimiter=" ", X=params[4][1].tolist())
np.savetxt(fname=path+"bias3", delimiter=" ", X=params[5][1].tolist())


acc = []
num = 1
for img, label in test_loader:
    print(num)
    correct = torch.sum(torch.argmax(model(img.view(-1, 784)), -1).cpu() == label).item()
    total = len(img)
    print(100*correct/total)
    acc.append(100*correct/total)

    path_tmp = path + "batch" + str(num) + "/"
    np.savetxt(fname=path_tmp+"input", delimiter=" ", X=img.view(-1, 784).tolist())
    np.savetxt(fname=path_tmp+"label", delimiter=" ", fmt='%d', X=label.tolist())
    np.savetxt(fname=path_tmp+"outputlayer1", delimiter=" ", X=model.output(img.view(-1, 784))[0].tolist())
    np.savetxt(fname=path_tmp+"outputlayer2", delimiter=" ", X=model.output(img.view(-1, 784))[1].tolist())
    np.savetxt(fname=path_tmp+"outputlayer3", delimiter=" ", X=model.output(img.view(-1, 784))[2].tolist())

    if num > 4:
       break
    else:
      num = num + 1

np.savetxt(fname=path+"clear_acc", delimiter=" ", X=acc)
