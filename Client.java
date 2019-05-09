import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	final static int PORT = 6666;
	final static String SERVER_IP = "95.111.16.147";
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new File("Client").mkdirs();
		
		Scanner in = new Scanner(System.in);
		System.out.println("What is the name of the log file?");
		String logName = in.nextLine();
		
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(
					new FileInputStream(logName));
		} catch (Exception e) {
			System.out.println("Problem with the specified file!");
			in.close();
			return;
		}
		
		Socket clientSocket = new Socket(SERVER_IP, PORT);

		byte[] buff = new byte[1024];
		int bytesRead = 0;
		
		DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
		long length = new File(logName).length();
		outputStream.writeLong(length);
		while((bytesRead = bufferedInputStream.read(buff)) > 0) {
			outputStream.write(buff, 0, bytesRead);
		}
		bufferedInputStream.close();
		
		System.out.println("What would you like the name of the result file to be?");
		System.out.println("File will be saved in folder Client");
		String resultFileName = in.nextLine();
		
		BufferedOutputStream resultFileStream = new BufferedOutputStream(
				new FileOutputStream("Client" + File.separator + resultFileName));
		DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
		length = inputStream.readLong();
		long allBytesRead = 0;
		while(allBytesRead < length) {
			bytesRead = inputStream.read(buff);
			allBytesRead += bytesRead;
			resultFileStream.write(buff, 0, bytesRead);
		}
		resultFileStream.flush();
		resultFileStream.close();
		
		System.out.println("File received and saved successfuly");
		
		clientSocket.close();
		in.close();
	}

}
