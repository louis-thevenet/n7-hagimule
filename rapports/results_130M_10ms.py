import json
import pandas as pd
import matplotlib.pyplot as plt

dataframes = []
for i in range(1, 12):
    file_path = f"results_130M_{i}.json"
    with open(file_path, 'r') as file:
        data = json.load(file)
        df = pd.DataFrame(data['results'])
        dataframes.append(df)

df = pd.concat(dataframes)

plt.figure(figsize=(10, 6))
plt.plot(range(1,12), df['mean'], marker='o')
plt.errorbar(range(1,12), df['mean'], yerr=df['stddev'], fmt='None', ecolor='red', capsize=5)
plt.title('Average Download Times')
plt.xlabel('Daemons')
plt.ylabel('Average Time (seconds)')
plt.grid(True)
plt.show()

