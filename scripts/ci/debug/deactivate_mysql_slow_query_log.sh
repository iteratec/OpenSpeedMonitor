#!/usr/bin/env bash
mysql -uroot -p${mysql_root_passwd} -e "SET @@global.slow_query_log=0;" # disables slow query log