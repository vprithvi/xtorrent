rm *.log
pkill java
cd bin
rm -r {1..6}/*
rm *.log
cp ../*.cfg .
java -ea StartRemotePeers
sleep 3
cat *.log
grep -B 15 -i connect.java *.log
ls {1..6}
