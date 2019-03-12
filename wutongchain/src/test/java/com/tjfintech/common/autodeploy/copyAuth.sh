#！/bin/bash

apt install expect -y

for i in {246,247,162}
do
expect << EOF
spawn scp authorized_keys  10.1.3.$i:~/.ssh/
expect "(yes/no)?" {send "yes\r"}
expect "password:" {send "root\r"}
expect "#" {send "exit\r"}
EOF
done

