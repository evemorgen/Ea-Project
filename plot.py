import sys
import os

import plotly.plotly as py
import plotly.graph_objs as go

if not os.path.exists(os.path.expanduser('~/.plotly/.credentials')):
    print("""
        Please fill `~/.plotly/.credentials` file with your credentials
        {
            "username": "XXX",
            "api_key": "SuperAPIKey"
        }
        Find your API key at: https://plot.ly/settings/api#/
    """)
    sys.exit(1)

series_num = 0
series = {}

for value in sys.stdin:
    if '-----' in value:
        series_num += 1
        continue
    if series_num not in series:
        series[series_num] = []
    time, val = value.split(",")
    series[series_num].append((time, float(val)))

print("data loaded, generating plot")
# Create a trace
data = []
for serie in series:
    values = series[serie]
    x = [x for x, y in values]
    y = [y for x, y in values]
    data.append(go.Scatter(y=y, mode='lines'))
    print(serie, " done")

py.plot(data, filename='scatter-mode123')
