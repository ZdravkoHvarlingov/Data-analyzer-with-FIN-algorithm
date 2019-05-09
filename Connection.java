import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class Connection extends Thread {

	private Socket clientSocket;
	
	public Connection(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		String uniqueId = UUID.randomUUID().toString();
		
		try {
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream("Server" + File.separator + "log" + uniqueId));
			
			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
			byte[] buff = new byte[1024];
			long length = inputStream.readLong();
			int bytesRead = 0;
			long allBytesRead = 0;
			while(allBytesRead < length) {
				bytesRead = inputStream.read(buff);
				allBytesRead += bytesRead;
				bufferedOutputStream.write(buff, 0, bytesRead);
			}
			bufferedOutputStream.flush();
			
			System.out.println("File received from: PORT: " + clientSocket.getPort() + ", IP: " + clientSocket.getInetAddress());		
			bufferedOutputStream.close();
			
			String resultFileName = ServerUtils.runAlgo("Server" + File.separator + "log" + uniqueId, uniqueId);
			
			BufferedInputStream fileToSendStream = new BufferedInputStream(
					new FileInputStream(resultFileName));
			
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			length = new File(resultFileName).length();
			outputStream.writeLong(length);
			while((bytesRead = fileToSendStream.read(buff)) > 0) {
				outputStream.write(buff, 0, bytesRead);
			}
			fileToSendStream.close();
			System.out.println("File sent to: PORT: " + clientSocket.getPort() + ", IP: " + clientSocket.getInetAddress());
			
			clientSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
