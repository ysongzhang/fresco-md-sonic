import csv

input_file = "/home/fresco-md-sonic/networks/LeNet-5/txt/batch5/outputlayer5"
output_file = "/home/fresco-md-sonic/networks/LeNet-5/csv/batch5/output.csv"

with open(input_file, 'r') as txt_file:
    with open(output_file, 'w', newline='') as csv_file:
        csv_writer = csv.writer(csv_file)
        
        for line in txt_file:
            row = line.strip().split()
            csv_writer.writerow(row)

print("Completed!")