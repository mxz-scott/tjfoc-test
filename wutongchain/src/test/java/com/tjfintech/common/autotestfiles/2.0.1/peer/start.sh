#!/bin/sh
    
   # cd /root/zll/permission/peer
   # tmux send -t perm -keys C-r
    rm -rf *db
    tmux send -t perm 'cd /root/zll/permission/peer' ENTER
    tmux send -t perm './peer start -c' ENTER

