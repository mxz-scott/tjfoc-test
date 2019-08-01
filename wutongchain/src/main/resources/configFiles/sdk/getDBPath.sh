#/bin/bash

cp /root/zll/chain2.0.1/sdk/conf/config.toml /root/zll/chain2.0.1/sdk/test.toml
sed -i 's/^[ \t]*//g' /root/zll/chain2.0.1/sdk/test.toml
sed -i '/^#.*/d' /root/zll/chain2.0.1/sdk/test.toml
cat /root/zll/chain2.0.1/sdk/test.toml |grep -E 'DBPath' |sed 's/.*\"\(.*\)\".*/\1/g'

#rm -f test.toml
