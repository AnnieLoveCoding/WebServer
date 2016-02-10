/**
* Assignment 1
* Tyler Stone
**/
// package webserver;
import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 *
 * This is the main FTP client. Study the code and figure out what 
 * each function does before adding to it. You only need to add code 
 * wherever you see a '?'
 *
 * @author Giovani
 * 
 */
public class FtpClient {

    final static String CRLF = "\r\n";
    private boolean FILEZILLA = false;		// Enabled if connecting to FILEZILLA Server
    private boolean DEBUG = false;		// Debug Flag
    private Socket controlSocket = null;
    private BufferedReader controlReader = null;
    private DataOutputStream controlWriter = null;
    private String currentResponse;

    /*
     * Constructor
     */
    public FtpClient() {
    }

    /*
     * Connect to the FTP server
     * @param username: the username you use to login to your FTP session
     * @param password: the password associated with the username
     */
    public void connect(String username, String password) {
        try {
            // establish the control socket
        	this.controlSocket = new Socket("localhost",21);

            // get references to the socket input and output streams
        	InputStream is = this.controlSocket.getInputStream();
   		 	OutputStream os = this.controlSocket.getOutputStream();
   		 	InputStreamReader isr = new InputStreamReader(is);
   		 	this.controlReader = new BufferedReader(isr);
   		 	this.controlWriter = new DataOutputStream(os);

            // check if the initial connection response code is OK
            if (checkResponse(220)) {
                System.out.println("Succesfully connected to FTP server");
            } else {System.out.println("ERROR: Failed connecting to FTP server");}

            if (FILEZILLA) {
                for (int i = 0; i < 2; i++) {
                    currentResponse = controlReader.readLine();
                    if (DEBUG) {
                        System.out.println("Current FTP response: " + currentResponse);
                    }
                }
            }

            // send user name and password to ftp server
            this.controlWriter.writeBytes("USER "+username + CRLF);
            if (checkResponse(331)) {
                System.out.println("Succesfully accepted Username");
            } else {System.out.println("ERROR: Failed to accept Username");}
            this.controlWriter.writeBytes("PASS "+password + CRLF);
            if (checkResponse(230)) {
                System.out.println("Succesfully accepted Password. Welcome!");
            } else {System.out.println("ERROR: Failed to accept Password");}


        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException: " + ex);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    /*
     * Retrieve the file from FTP server after connection is established
     * @param file_name: the name of the file to retrieve
     */
    public void getFile(String file_name) {
    	System.out.println("Getting file...");
	int data_port = 20; // initialize the data port        
	try {
            // change to current (root) directory first
		System.out.println("Trying... Changing to root directory");
            sendCommand("CWD /" + CRLF,250);
            System.out.println("Now in root.");
            // set to passive mode and retrieve the data port number from response
            System.out.println("Setting to passive...");
            this.currentResponse = sendCommand("PASV" + CRLF,227);
            System.out.println("On passive");
            data_port = extractDataPort(this.currentResponse);
            System.out.println("Port extracted from passive response");

            // connect to the data port 
            System.out.println("creating data socket+reader...");
            Socket data_socket = new Socket("localhost",data_port);
            DataInputStream data_reader = new DataInputStream(data_socket.getInputStream());
            System.out.println("Successfully created socket+reader.");

            // download file from ftp server
            System.out.println("Downloading file...");
            sendCommand("RETR Users/TylerStone/FtpFiles/" + file_name + CRLF,150);

            // check if the transfer was succesful
            checkResponse(226);
            System.out.println("Download was in fact successful.");

            // Write data on a local file
            System.out.println("Creating local file...");
            createLocalFile(data_reader, file_name);
            System.out.println("Done. File Transfer Complete");

        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException: " + ex);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    /*
     * Close the FTP connection
     */
    public void disconnect() {
        try {
            controlReader.close();
            controlWriter.close();
            controlSocket.close();
            System.out.println("Successfully Disconnected.");
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    /*
     * Send ftp command 
     * @param command: the full command line to send to the ftp server
     * @param expected_code: the expected response code from the ftp server
     * @return the response line from the ftp server after sending the command
     */
    private String sendCommand(String command, int expected_response_code) {
        String response = "";
        try {
            // send command to the ftp server
            controlWriter.writeBytes(command);

            // get response from ftp server
            response = controlReader.readLine();
            if (DEBUG) {
                System.out.println("Current FTP response: " + response);
            }

            // check validity of response  
            if (!response.startsWith(String.valueOf(expected_response_code))) {
                throw new IOException(
                        "Bad response: " + response);
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
        return response;
    }

    /*
     * Check the validity of the ftp response, the response code should
     * correspond to the expected response code
     * @param expected_code: the expected ftp response code
     * @return response status: true if successful code
     */
    private boolean checkResponse(int expected_code) {
        boolean response_status = true;
        try {
            currentResponse = controlReader.readLine();
            if (DEBUG) {
                System.out.println("Current FTP response: " + currentResponse);
            }
            if (!currentResponse.startsWith(String.valueOf(expected_code))) {
                response_status = false;
                throw new IOException(
                        "Bad response: " + currentResponse);
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
        return response_status;
    }

    /*
     * Given the complete ftp response line of setting data transmission mode
     * to passive, extract the port to be used for data transfer
     * @param response_line: the ftp response line
     * @return the data port number 
     */
    private int extractDataPort(String response_line) {
        int data_port = 0;
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(response_line);
        String[] str = new String[6];
        if (matcher.find()) {
            str = matcher.group(1).split(",");
        }
        if (DEBUG) {
            System.out.println("Port integers: " + str[4] + "," + str[5]);
        }
        data_port = Integer.valueOf(str[4]) * 256 + Integer.valueOf(str[5]);
        if (DEBUG) {
            System.out.println("Data Port: " + data_port);
        }
        return data_port;
    }

    /*
     * Create the file locally after retreiving data over the FTP data stream.
     * @param dis: the data input stream 
     * @param file_name: the name of the file to create 
     */
    private void createLocalFile(DataInputStream dis, String file_name) {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        try {
            FileOutputStream fos = new FileOutputStream(new File(file_name));
            while ((bytes = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes);
            }
            dis.close();
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException" + ex);
        } catch (IOException ex){
            System.out.println("IOException: " + ex);
        } 
    }
}

