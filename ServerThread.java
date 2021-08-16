import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {

	ObjectInputStream input;
	ObjectOutputStream output;
	Boolean isOnline;
	
	
	/**
	 * @param conn Socket used to communicate with corresponding client
	 * 
	 * Set this Connection Thread's I/O streams and flag this corresponding client as online
	 */
	public ServerThread(Socket conn) {
		// Wrap sockets i/o streams for incoming and outgoing message
		try {
			input = new ObjectInputStream(conn.getInputStream());
			output = new ObjectOutputStream(conn.getOutputStream());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		isOnline = true;
	}
	
	
	/**
	 * @param message Message text
	 * @param recipient The nickname of the Recipient
	 * @param sender The nickname of the Sender
	 * @return Message
	 * 
	 * Create a new whisper message
	 */
	private Message formulateWhisper(String message, String recipient, String sender) {
		return new Message(message,recipient,sender);
	}
	
	
	/**
	 * @param message Message Text
	 * @param sender The nickname of the Sender
	 * @return Message
	 * 
	 * Create a new broadcast message
	 */
	private Message formulateBroadcast(String message, String sender) {
		return new Message(message,sender,false);
	}
	
	
	public void run() {
		// Initiate contact (prompt) with current online clients
		output.writeObject(new Message(Server.online));
		
		// Wait for input (reply) from client
		try {
			// Extrapolate the message and message type from the object
			Message message;
			while ((message = (Message) input.readObject()) != null) {
				// Extrapolate Message particulars: message, recipient and sender
				String messageStr = message.getMessage();
				String recipient = message.getRecipient();
				String sender = message.getSender();
				
				// Send only to the single recipient if the message is a whisper
				if (message.isWhisper()) {
					// Look up the recipient for the Message
					ServerThread receiver = Server.online.get(recipient);
					
					// If the client has sent the exit command, stop the thread
					if (message.isTerminated()) {
						break;
					}
					
					// Write message to corresponding clients socket's input stream
					// which corresponds to the receiver socket, which is of type ServerThread
					Message sendMessage = formulateWhisper(messageStr,recipient,sender);
					receiver.output.writeObject(sendMessage);
				} else {
					Message sendMessage = formulateBroadcast(messageStr,sender);
					
					// Iterate through keys of hashmap and send message to everyone
					Server.online.forEach((k,v) -> v.output.writeObject(sendMessage));
				}
				
				// Maintain contact with this Threads corresponding client
				// So that the communication loop remains operational until DC or failure
				output.writeObject(new Message(Server.online));
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
	}
}
