rm *.log
pkill java
cd bin
rm *.log
cp ../*.cfg .
java -ea StartRemotePeers
sleep 3
cat *.log
grep -i connect *.log
