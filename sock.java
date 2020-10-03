package site;

//import
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class sock {
    //make socket
	ServerSocket sock;
	//public class implementation - establish client and socket on instance creation
	public sock() {
        //catch exceptions
		try{
            //make socket
			sock = new ServerSocket(6789);
		}
		catch(IOException e) {
            //error checking
			System.out.println("Couldn't make socket on port");
		}
		//constantly loop to accept new instances through the socket
        while (true) {
            try {
            	//receive the data passed via the socket
            	Socket client = sock.accept();    
            	ServeThread sThread = new ServeThread(client);
            	//starts thread and re-iterates loop- doesn't wait for response completion
            	sThread.start();
            }
            catch(IOException e) {
            	System.out.println("E code 1");
            }
        }
	}
	
    public static void main( String[] args ) throws Exception {
    	System.out.println("Working Directory = " + System.getProperty("user.dir"));    	
        //call class obj
        new sock();
    }
    
    //Implementation of a thread handling a request response
    static class ServeThread extends Thread{
    	Socket myClient;
    	
    	//receive reference to the client data
    	ServeThread(Socket client) {
    		myClient = client;
    	}
    	
    	//default thread execution
    	public void run() {    			
    		try {
    			//read buffer from client's input stream
    			BufferedReader buffer = new BufferedReader(new InputStreamReader(myClient.getInputStream()));
    			
    			//interpret the requested filepath
                String path = buffer.readLine().split(" ")[1];
                System.out.println("Serving: \"" + path + "\"");
                
                //construct proper host path
                Path fp = Paths.get("");
                String userDir = System.getProperty("user.dir") + "\\src";
                fp = Paths.get(userDir + path);
                if ("/".equals(path))
                	fp = Paths.get(fp.toString() + "/kalindex.html");
                
                //grab the file if there and respond
                if (Files.exists(fp))
                    respond(myClient, "200 OK", Files.probeContentType(fp), Files.readAllBytes(fp));                 
                //serve 301
                else if ("/old.html".equals(path))
                	respond(myClient, "301 Moved Permanently", "text/html", "<h1>301 File has moved</h1>".getBytes());
                //serve 404
                else
                    respond(myClient, "404 Not Found", "text/html", "<h1>404 File not found</h1>".getBytes());
    		}catch (Exception e) {System.out.println("E code 2");}
    	}
            
        //response call    	
        public void respond(Socket client, String status, String type, byte[] contents) throws IOException {
        	//create response
            OutputStream response = client.getOutputStream();
            
            //write response in proper http 1.1 format
            response.write(("HTTP/1.1 \r\n" + status).getBytes());
            response.write(("ContentType: " + type + "\r\n").getBytes());
            response.write("\r\n".getBytes());
            response.write(contents);
            response.write("\r\n\r\n".getBytes());
            response.flush();
            //end connection
            client.close();
        }	
    }
}