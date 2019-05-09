import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	final static int PORT = 6666;
	
	public static void main(String []args) throws IOException {
		
		new File("Server").mkdirs();
		
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(PORT);
		
		System.out.println("Server started. Waiting for connections...");
		
		while(true) {
			Socket clientSocket = serverSocket.accept();
			
			Connection connection = new Connection(clientSocket);
			connection.start();			
		}
	}
}
