(for i in {1..15}; do cat logs/${i}_dump.log | (grep -o 'Merit .\+' | awk '{ print $7 "," $5}' | sed -e 's/,$//g' && echo "----------"); done;) > out
cat out | python plot.py
rm out
