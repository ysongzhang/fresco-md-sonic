import csv

def generate_csv_with_zeros(rows, cols, output_file):
    with open(output_file, mode='w', newline='') as file:
        writer = csv.writer(file)
        
        for _ in range(rows):
            writer.writerow([0] * cols)
    print(f"CSV file '{output_file}' completed.")

if __name__ == "__main__":
    inputSize = 84
    outputSize = 200

    weights_rows = outputSize
    weights_cols = inputSize
    bias_rows = outputSize
    bias_cols = 1

    output_weights_file = "/home/fresco-md-sonic/networks/LeNet-5_Cifar10/weight5.csv"
    output_bias_file = "/home/fresco-md-sonic/networks/LeNet-5_Cifar10/bias5.csv"
    generate_csv_with_zeros(weights_rows, weights_cols, output_weights_file)
    generate_csv_with_zeros(bias_rows, bias_cols, output_bias_file)
