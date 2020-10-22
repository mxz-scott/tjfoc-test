#!/bin/sh
# author:chenxu

    cd /root/zll/permission/sdk
    tmux send -t perm_sdk 'cd /root/zll/permission/sdk' ENTER
    tmux send -t perm_sdk './httpservice' ENTER

