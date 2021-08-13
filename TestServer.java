import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class TestServer {
	
	public static void main(String[] args) throws IOException {
		// Create ServerSocket and bind to port to listen for incoming connections
		ServerSocket server = null;
		try {
			server = new ServerSocket(Integer.parseInt(args[0]));
		} catch (Exception e) {
			System.err.println("Usage: java TestServer <port_number>");
			System.exit(1);
		}
		
		// Create a socket to communicate with connected clients after a succesful connection
		// server.accept() waits until a client connects
		System.out.println("Awaiting connection...");
		Socket connectedClient = server.accept();
		
		// Notification of a connected client
		System.out.printf("Client [%s] CONNECTED\n", connectedClient.getInetAddress());
		
		// Wrap socket i/o stream in Reader/Writer
		// Clients Output is directed to Server Input
		// Servers Output is directed to Client Input
		try (BufferedReader input = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
			 PrintWriter output = new PrintWriter(connectedClient.getOutputStream(),true)) {
			// Server initiates first contact
			output.println("Message: ");
			
			// Then server constantly listens to client
			// Only null when terminated or garbaged
			String inLine, outLine;
			while ((inLine = input.readLine()) != null) {
				// Echo client's message
				System.out.printf("%s: %s", connectedClient.getInetAddress(),inLine);
				
				// Prompt for more messages
				// Use println, to active buffer auto-flush
				output.println("Message: ");
				
				// Termination Flag
				if (inLine.equals("/close")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server Closing...");
	}
}
