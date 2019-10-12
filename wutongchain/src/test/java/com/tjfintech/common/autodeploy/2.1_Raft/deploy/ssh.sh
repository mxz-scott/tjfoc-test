#! /usr/bin/expect -f

set ip [lindex $argv 0]
set command [lindex $argv 1]
set passwd root

spawn ssh root@$ip
expect {
   "(yes/no)" {send "yes\r"; exp_continue}
   "password:" {send "$passwd\r"}
}
#expect "password:"
#send "root\r"
expect "#"
send "$command\r"
expect "#"
#set timeout 50
send "exit\r"
expect EOF
#interact



