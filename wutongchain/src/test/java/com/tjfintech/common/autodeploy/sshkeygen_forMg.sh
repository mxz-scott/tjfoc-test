#!/bin/bash

rm -rf ~/.ssh/{known_hosts,id_rsa*}
ssh-keygen -t rsa -n '' -f ~/.ssh/id_rsa
#touch  ~/.ssh/authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
