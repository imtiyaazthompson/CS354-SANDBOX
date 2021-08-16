import java.io.BufferedReader;
import java.util.Random;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
	
	private ServerSocket server;
	private int sessionID;
	static HashMap<String,ServerThread> online;
	
	
	/**
	 * @param port
	 */
	public Server(int port) {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (IllegalArgumentException i) {
			System.err.println(i.getMessage());
			System.exit(1);
		}
		
		// Every Session starts off with a randomized session id
		sessionID = new Random().nextInt(9999);
	}
	
	
	/**
	 * @return
	 */
	public Socket acceptConnection() {
		Socket newConnection = null;
		try {
			newConnection = server.accept();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		return newConnection;
	}
	
	
	/**
	 * @param name Nickname of an already connected client
	 * @return
	 * 
	 * Resolve nickname duplication by issuing the attempted duplicate with an appended number
	 */
	private String resolveDuplicates(String name) {
		return (name + "#" + sessionID++);
	}
	
	
	/**
	 * @param conn Socket used to communicate with the recently connected client
	 * @throws IOException
	 * 
	 * Await the client's initial message that contains the Nickname they connect with
	 * Check to see if a client with the same nickname is already connected, if so, then resolve
	 */
	private String initConnection(Socket conn) throws IOException {
		// Client makes first contact immediately after connecting
		// Client should specify nickname before attempting to connect so an auto message is sent
		
		Message initialContact = (Message) new ObjectInputStream(conn.getInputStream()).readObject();
		String nickname = initialContact.getMessage();
		if (online.containsKey(nickname)) {
			nickname = this.resolveDuplicates(nickname);
		}
		
		new ObjectOutputStream(conn.getOutputStream()).writeObject(new Message(nickname));
		return nickname;
	}
	
	public static void main(String[] args) {
		Server s = null;
		try {
			s = new Server(Integer.parseInt(args[0]));
		} catch (Exception e) {
			System.out.println("Usage: java Server <port_number>");
		}
		
		// Start accepting connections
		while (true) {
			Socket connection = s.acceptConnection();
			String clientName = null;
			try {
				clientName = s.initConnection(connection);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
			
			// Generate corresponding Thread Class
			ServerThread st = new ServerThread(connection);
			
			// Add to clients online
			online.put(clientName, st);
			
			// Start the communication thread for the Thread Class
			st.start();
		}
	}
}
