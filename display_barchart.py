import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("viewpopularitycount.csv")
df = df.sort_values(by="Popularity Count", ascending=False)
plt.figure(figsize=(10, 6))
plt.bar(df["Item Name"], df["Popularity Count"])
plt.xlabel("Item Name")
plt.ylabel("Popularity Count")
plt.title("Popularity Count of Items")
plt.xticks(rotation=45, ha="right")
plt.tight_layout()

plt.show()
