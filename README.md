# ethos-crash-restarter

## the Runable Jar file 

build/libs/gs-scheduling-tasks-0.1.0.jar

## The Execute Shell Script

* runECR.sh



### You Need to change

java -DfixedRate=5000 -DfilePath=/STS/Text.txt -DinitialDelay=1000 -DminFileCount=5 -jar build/libs/gs-scheduling-tasks-0.1.0.jar
 
* fixedRate = {check time / millisecond} 
* filePath = {the miner log file / you can cmd show log to watch log path }