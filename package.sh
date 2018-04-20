#########################################################################
# File Name: package.sh
# Author: ma6174
# mail: ma6174@163.com
# Created Time: Fri 09 Feb 2018 03:36:05 PM CST
#########################################################################
#!/bin/bash
mvn package; cd target; tar zxf *.gz; cd .. 
