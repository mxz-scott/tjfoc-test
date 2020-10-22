#! /bin/bash

name=$1

file_name=$2

mysql -uroot -proot <<EOF 
drop database if exists $1;

create database if not exists $1;

use ${name};

source ${file_name};

EOF
