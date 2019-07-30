#/bin/bash

cp conf/config.toml test.toml
sed -i 's/^[ \t]*//g' test.toml
sed -i '/^#.*/d' test.toml
cat test.toml |grep -E 'DBPath' |sed 's/.*\"\(.*\)\".*/\1/g'

rm -f test.toml
