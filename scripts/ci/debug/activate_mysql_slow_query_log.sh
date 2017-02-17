#!/usr/bin/env bash
mysql -uroot -p${mysql_root_passwd} -e "SET @@global.long_query_time=${mysql_long_query_time};" # in seconds
mysql -uroot -p${mysql_root_passwd} -e "SET @@global.slow_query_log_file=${mysql_slow_query_log_file};"
mysql -uroot -p${mysql_root_passwd} -e "SET @@global.slow_query_log=1;" # enables slow query log