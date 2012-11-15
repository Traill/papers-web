package generators;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.mail.Session;
import javax.mail.Transport;

public class MailSender {    
    private Session session;
    private Transport transport;
    private String from;
    private String subject;
    private String text;
    private String dest;
    
    public MailSender(String from, String subject, String text, String dest){
        this.from = from;
        this.subject = subject;
        this.text = text;
        this.dest = dest;
    }    
    
    public String send() {
    	// declaration section:
    	// smtpClient: our client socket
    	// os: output stream
    	// is: input stream
    	        Socket smtpSocket = null;  
    	        DataOutputStream os = null;
    	        BufferedReader is = null;
    	 
    	// Initialization section:
    	// Try to open a socket on port 25
    	// Try to open input and output streams
    	        try {
    	            smtpSocket = new Socket("mail.epfl.ch", 25);
    	            os = new DataOutputStream(smtpSocket.getOutputStream());
      	          	is = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
    	        } catch (UnknownHostException e) {
    	            System.err.println("Don't know about host: hostname");
    	        } catch (IOException e) {
    	            System.err.println("Couldn't get I/O for the connection to: hostname");
    	        }
    	// If everything has been initialized then we want to write some data
    	// to the socket we have opened a connection to on port 25
    	    if (smtpSocket != null && os != null && is != null) {
    	            try {
    	// The capital string before each colon has a special meaning to SMTP
    	// you may want to read the SMTP specification, RFC1822/3
    	        os.writeBytes("HELO\n");    
                os.writeBytes("MAIL FROM: " + this.from + "\n");
                os.writeBytes("RCPT TO: " + this.dest + "\n");
                os.writeBytes("DATA\n");
                os.writeBytes("From: " + this.from + "\n");
                os.writeBytes("Subject: " + this.subject + "\n");
                os.writeBytes(this.text + "\n"); // message body
                os.writeBytes("\n.\n");
                os.writeBytes("QUIT");
    	// keep on reading from/to the socket till we receive the "Ok" from SMTP,
    	// once we received that then we want to break.
                
    	// clean up:
    	// close the output stream
    	// close the input stream
    	// close the socket
    	        os.close();
                is.close();
                smtpSocket.close(); 
                return "true";
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    	    
    	   return "false";
    }
}
