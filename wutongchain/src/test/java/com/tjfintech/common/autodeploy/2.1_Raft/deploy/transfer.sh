#! /usr/bin/expect -f

set src [lindex $argv 0]
set ip [lindex $argv 1]
set dest [lindex $argv 2]
set user [lindex $argv 3]
set passwd [lindex $argv 4]

#set user root
#set passwd root


spawn scp -r $src $user@$ip:$dest
expect {
   "(yes/no)" {send "yes\r"; exp_continue}
   "password:" {send "$passwd\r"}
}
#expect "password:"
#send "root\r"
set timeout 50
send "exit\r"
expect EOF
#interact



