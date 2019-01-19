import os
import sys
import re
from pandas import DataFrame, ExcelWriter, Series

log_directory = sys.argv[1]
output_name = sys.argv.get(2, 'output.xlsx')
values = dict()

for file in os.listdir(log_directory):
    if not file.endswith('conf.log'):
        print(f"bad file {file}")
        continue
    key = file.split(".")[0].replace("len", "")
    lines = open(log_directory + file, 'r').readlines()
    values[key] = []
    for line in lines:
        energy = re.search("Energy: \d+\.\d+", line)
        if energy is not None:
            values[key].append(energy.group().replace("Energy: ", ""))

writer = ExcelWriter(output_name)
frame = DataFrame(dict([(int(k), Series(v)) for k, v in values.items()]))
frame = frame.reindex(sorted(frame.columns), axis=1)
frame.to_excel(writer, 'Sheet 1')
writer.save()
