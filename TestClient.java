import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
	public static void main(String[] args) {
		// Create Socket bound to remote IP and port to start connection with server
		// Then communicate
		Socket socket = null;
		try {
			socket = new Socket(args[0],Integer.parseInt(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Wrap socket i/o streams in Reader/Writer
		// Client's Output is directed to Server Input
		// Server's Output is directed to Client Input
		try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
		) {
			// Receive first contact from Server
			// Client always setup to receive first
			String inLine, outLine;
			Scanner sc = new Scanner(System.in);
			while ((inLine = input.readLine()) != null) {
				// Echo server response
				System.out.print(inLine);
				
				// Send request to server, data
				// First get user message to send
				String message = sc.nextLine();
				
				if (message.equals("/close")) {
					// Terminate connection
					output.printf("%s\n", message);
					break;
				}
				
				// Send to server, and repeat loop
				output.printf("%s\n", message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Client Closing...");
	}
}
