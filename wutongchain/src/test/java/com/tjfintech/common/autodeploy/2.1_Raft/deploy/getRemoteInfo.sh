#! /usr/bin/expect -f

set ip [lindex $argv 0]
set key [lindex $argv 1]
set command1 [lindex $argv 2]
set command2 [lindex $argv 3]

set passwd root
 
#设置保存的文件名及打开文件
set ofile "temp/${key}_$ip"
set output [open $ofile "w"]


spawn ssh root@$ip

expect {
   "(yes/no)" {send "yes\r"; exp_continue}
   "password:" {send "$passwd\r"}
}
#expect "password:"
#send "root\r"
expect "#"
#send "cd /root/zll/ate/wtchain1/\r"
send "$command1/\r"
expect "#"
#send "./wtpeer init\r"
send "$command2\r"
expect -re "${key}(.*)"
set outcome $expect_out(0,string)
puts $output $ip$outcome

send "exit\r"
expect EOF
#interact
