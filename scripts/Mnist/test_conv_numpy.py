import numpy as np

input_file_path = "/home/Network/Sarda/input_0"
weight_file_path = "/home/Network/Sarda/weight1_0"
bias_file_path = "/home/Network/Sarda/bias1_0"
output_file_path = "/home/Network/Sarda/outputlayer1_0"


input_matrix = np.loadtxt(input_file_path, delimiter=" ", dtype=np.float64).reshape((128, 1, 28, 28))
weight_matrix = np.loadtxt(weight_file_path, delimiter=" ", dtype=np.float64)
bias_matrix = np.loadtxt(bias_file_path, delimiter=" ", dtype=np.float64)
output_matrix = np.loadtxt(output_file_path, delimiter=" ", dtype=np.float64)[0].reshape(1, 980)


B = 1
iw = 28
ih = 28
f = 2
Din = 1
Dout = 5
P = 0
S = 2

ow 	= int(((iw-f+2*P)//S)+1)
oh	= int(((ih-f+2*P)//S)+1)


temp1 = input_matrix.reshape((128, 784))[0]
temp2 = np.zeros((Din * f * f * ow * oh * B))
# print(temp1)

loc_input = 0
loc_output = 0

for i in range(B):
    for j in range(oh):
        for k in range(ow):
            loc_output = i * ow * oh + j * ow + k
            for l in range(Din):
                loc_input = (i * (iw + 2 * P) * (ih + 2 * P) * Din + l * (iw + 2 * P) * (ih + 2 * P) + j * S * (iw + 2 * P) + k * S)
                for a in range(f):
                    for b in range(f):
                        temp2[(l * f * f + a * f + b) * ow * oh * B + loc_output] = temp1[loc_input + a*(iw+2*P) + b]


temp2 = temp2.reshape(Din * f * f, ow * oh * B)
# print(temp2)

temp3 = weight_matrix.dot(temp2)
bias_matrix = bias_matrix.reshape(5, 1)
result = temp3 + bias_matrix
result = result.reshape(1, 980)
result = np.where(result > 0, result, 0)

sub = result - output_matrix
mask = np.abs(sub) > 1
positions = np.argwhere(mask)
values = sub[mask]
count = values.size
print(positions)
print(values)
print(count)
