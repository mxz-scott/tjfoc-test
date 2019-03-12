#!/bin/bash

rm -rf ~/.ssh/{known_hosts,id_rsa*}
ssh-keygen -t rsa -n '' -f ~/.ssh/id_rsa

#ssh-copy-id -i 10.1.3.240


apt install expect -y

#for i in {240,240}                                         
#do
expect << EOF
spawn ssh-copy-id 10.1.3.240    
expect "(yes/no)?" {send "yes\r"}
expect "password:" {send "root\r"}
expect "#" {send "exit\r"}
EOF
#done

