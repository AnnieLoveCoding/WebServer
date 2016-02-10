JCC = javac # define a makefile variable for the java compiler
JFLAGS = -g
default: WebServer.class HttpRequest.class FtpClient.class
WebServer.class: WebServer.java
	$(JCC) $(JFLAGS) WebServer.java
HttpRequest.class: HttpRequest.java
	$(JCC) $(JFLAGS) HttpRequest.java
FtpClient.class: FtpClient.java
	$(JCC) $(JFLAGS) FtpClient.java
clean: 
	$(RM) *.class