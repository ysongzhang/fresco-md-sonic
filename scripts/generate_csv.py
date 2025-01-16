import csv

def generate_csv_with_zeros(rows, cols, output_file):
    with open(output_file, mode='w', newline='') as file:
        writer = csv.writer(file)
        
        for _ in range(rows):
            writer.writerow(range(cols))
    print(f"CSV file '{output_file}' completed.")

if __name__ == "__main__":
    rows = 1
    cols = 784

    output_file = "/home/fresco-md-sonic/networks/Test/csv/input.csv"
    generate_csv_with_zeros(rows, cols, output_file)
