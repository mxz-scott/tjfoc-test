#!/bin/sh
    #参数1为 tmux session名称 参数2为执行目录 sdk或者节点部署目录 参数3为执行文件启动命令
    #tmux send -t M2 'cd /root/zll/chain2.0.1/peer' ENTER
    #tmux send -t M2 './Mp' ENTER
    $1 'cd '$2'' ENTER
    $1 './'$3'' ENTER
