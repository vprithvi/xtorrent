git pull
rm *.log
rm *.class
pkill java
cd src
javac *.java
mv *.class ../.
cd ..
java StartRemotePeers
