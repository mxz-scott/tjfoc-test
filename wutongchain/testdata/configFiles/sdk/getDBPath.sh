#/bin/bash

cp $1conf/config.toml $1test.toml
sed -i 's/^[ \t]*//g' $1test.toml
sed -i '/^#.*/d' $1test.toml
cat $1test.toml |grep -E 'DBPath' |sed 's/.*\"\(.*\)\".*/\1/g'

#rm -f test.toml
