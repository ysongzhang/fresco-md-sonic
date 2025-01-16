import torch
import numpy as np

def load_txt_to_tensor(filepath, shape=None):
    data = np.loadtxt(filepath, delimiter=" ", dtype=np.float32)
    if shape:
        data = data.reshape(shape)
    return torch.tensor(data, dtype=torch.float32)

input_file = "/home/Network/Sarda/input_0" 
kernel_file = "/home/Network/Sarda/weight1_0"
bias_file = "/home/Network/Sarda/bias1_0"
cmp_file = "/home/Network/Sarda/outputlayer1_0"

input_tensor = load_txt_to_tensor(input_file, shape=(128, 1, 28, 28))  # (batch, channel, height, width)

kernel_tensor = load_txt_to_tensor(kernel_file, shape=(5, 1, 2, 2))  # (out_channels, in_channels, kernel_h, kernel_w)
print(kernel_tensor[1])

bias_tensor = load_txt_to_tensor(bias_file, shape=(5,))  # (out_channels,)

import torch.nn.functional as F

output = F.conv2d(input_tensor, weight=kernel_tensor, bias=bias_tensor, stride=2, padding=0)
output = F.relu(output)
output = output.view(-1, 980)
out1 = output.data.numpy()[0]


print("The shape of the input image:", input_tensor.shape)
print("The shape of the conv kernel:", kernel_tensor.shape)
print("The shape of the conv output:", output.shape)
print("The shape of the conv output:", out1.shape)

# print(out1)
output_matrix = np.loadtxt(cmp_file, delimiter=" ")[0].reshape(1, 980)
sub = out1 - output_matrix
mask = np.abs(sub) > 1
positions = np.argwhere(mask)
values = sub[mask]
count = values.size
print(positions)
print(values)
print(count)
