IFS=$'\n' 
for line in $(cat example_results.txt); do
    LENGTH=$(echo $line | awk '{ print $1 }')
    ENERGY=$(echo $line | awk '{ print $3 }')

    sed -e "s/\$L/$LENGTH/g" -e "s/\$E/$ENERGY/g" config.tpl > src/main/resources/len${LENGTH}.conf
done
