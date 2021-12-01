all: server client

clean:
	rm -rf bin

server: src/CalcServer.java
	javac -d bin src/CalcServer.java

client: src/CalcClient.java
	javac -d bin src/CalcClient.java

runserver:
	java -cp bin CalcServer

runclient:
	java -cp bin CalcClient