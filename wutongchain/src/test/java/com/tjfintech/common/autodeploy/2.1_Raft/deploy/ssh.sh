#! /usr/bin/expect -f

set ip [lindex $argv 0]
set command [lindex $argv 1]
set user [lindex $argv 2]
set passwd [lindex $argv 3]

#set user root
#set passwd root

spawn ssh $user@$ip
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



