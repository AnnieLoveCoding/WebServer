/**
* Assignment 1
* Tyler Stone
**/
// package webserver;
import java.io.* ;
import java.net.* ;
import java.util.* ;
final class HttpRequest implements Runnable
{
	final static String CRLF = "\r\n";
	 Socket socket;
	 // Constructor
	 public HttpRequest(Socket socket) throws Exception
	 {
	 this.socket = socket;
	 }
	 // Implement the run() method of the Runnable interface.
	 public void run()
	 {
		 try {
			 processRequest();
		 } catch (Exception e) {
			 System.out.println(e);
		 }
	 }
	 private void processRequest() throws Exception
	 {
		// Get a reference to the socket's input and output streams.
		 InputStream is = this.socket.getInputStream();
		 OutputStream os = this.socket.getOutputStream();
		 DataOutputStream dos = new DataOutputStream(os);
		 // Set up input stream filters.
		 InputStreamReader isr = new InputStreamReader(is);
		 BufferedReader br = new BufferedReader(isr);
		 
		// Get the request line of the HTTP request message.
		 String requestLine = br.readLine();
		 // Display the request line.
		 System.out.println();
		 System.out.println(requestLine);
		 
		// Get and display the header lines.
				 String headerLine = null;
				 while ((headerLine = br.readLine()).length() != 0) {
				  System.out.println(headerLine);
				 }
		 
		// Extract the filename from the request line.
		 StringTokenizer tokens = new StringTokenizer(requestLine);
		 tokens.nextToken(); // skip over the method, which should be "GET"
		 String fileName = tokens.nextToken();
		 // Prepend a "." so that file request is within the current directory.
		 fileName = "." + fileName;
		 
		// Open the requested file.
		 FileInputStream fis = null;
		 boolean fileExists = true;
		 try {
		  fis = new FileInputStream(fileName);
		 } catch (FileNotFoundException e) {
		  fileExists = false;
		 }
		 
		// Construct the response message.
		 String statusLine = null;
		 String contentTypeLine = null;
		 String entityBody = null;
		 if (fileExists) {
		  statusLine = "HTTP/1.1 200 OK";
		  contentTypeLine = "Content-type: " +contentType(fileName) + CRLF;
		 } else {
			// if the file requested is any type other than a text (.txt) file, report
			 // error to the web client
			 if (true || !contentType(fileName).equalsIgnoreCase("text/plain")) {
				 statusLine = "HTTP/1.1 404 Not Found";
				 contentTypeLine = "Content-type: text/html" + CRLF;
				 entityBody = "<HTML>" +
						 "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
						 "<BODY>404 ERROR: The document you were looking for was not found</BODY></HTML>";
			 } //THIS PART OF THE LAB REQUIRED US CREATE AN FTP EXTENSION TO THE SERVER TO ACT AS AN
			 //FTP SERVER IF THE REQUESTED FILE ENDED WITH .txt
			 /**else { // else retrieve the text (.txt) file from your local FTP server
				 statusLine = "HTTP/1.1 200 OK";
				 contentTypeLine = "Content-type: text/plain" + CRLF;
				 // create an instance of ftp client
				 FtpClient ftp = new FtpClient();
				 // connect to the ftp server
				 ftp.connect("TylerStone", "SecretSauce");
				 // retrieve the file from the ftp server, remember you need to
				 // first upload this file to the ftp server under your user
				 // ftp directory
				 ftp.getFile(fileName);
				 // disconnect from ftp server
				 ftp.disconnect();
				 // assign input stream to read the recently ftp-downloaded file
				 fis = new FileInputStream(fileName);
				 }**/ 
		 }
		 
		// Send the status line.
		 dos.writeBytes(statusLine);
		 // Send the content type line.
		 dos.writeBytes(contentTypeLine);
		 // Send a blank line to indicate the end of the header lines.
		 dos.writeBytes(CRLF);
		 
		// Send the entity body.
		 if (fileExists) {
		  sendBytes(fis, dos);
		  fis.close();
		 } else {
			 if (!contentType(fileName).equalsIgnoreCase("text/plain")) {
				dos.writeBytes(entityBody);
			 } else {
				 sendBytes(fis, dos);
			 } 
		 }
		 
		// Close streams and socket.
		 dos.close();
		 os.close();
		 br.close();
		 isr.close();
		 is.close();
		 this.socket.close();
	 }
	
	 private static void sendBytes(FileInputStream fis, OutputStream os)
			 throws Exception
			 {
			  // Construct a 1K buffer to hold bytes on their way to the socket.
			  byte[] buffer = new byte[1024];
			  int bytes = 0;
			  // Copy requested file into the socket's output stream.
			  while((bytes = fis.read(buffer)) != -1 ) {
			  os.write(buffer, 0, bytes);
			  }
			 }
	
	private static String contentType(String fileName)
	{
	 if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
	 return "text/html";
	 }
	 if(fileName.endsWith(".gif")) {
	 return "image/gif";
	 }
	 if(fileName.endsWith(".jpg")) {
	 return "image/jpeg";
	 }
	 if(fileName.endsWith(".txt") || fileName.endsWith(".java")|| fileName.endsWith(".sh")|| fileName.equals("makefile")) {
		 return "text/plain";
		 }
	 return "application/octet-stream";
	}
}