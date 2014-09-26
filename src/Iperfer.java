import java.net.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class Iperfer {
	public static boolean isClient; //

	public static String hostName;
	public static int port;
	public static double time;

	// DEBUG stuff
	final static boolean DEBUG = false;

	public static void main(String args[]) {
		processArgs(args);

		if (isClient) {

			if (DEBUG)
				System.out.println("Starting up in Client Mode");

			ClientMode();
		} else {
			// SERVER MODE - uses -l
			if (DEBUG)
				System.out.println("Starting up in Server Mode");

			ServerMode();
			
		}

	}

	static void ClientMode() {

		int bytesSent = 0;
		Socket socket = null;
		DataOutputStream outToServer = null;

		try {
			socket = new Socket(hostName, port);
			outToServer = new DataOutputStream(socket.getOutputStream());

			byte[] arr = new byte[1024];

			long startTime = System.currentTimeMillis();

			while (System.currentTimeMillis() - startTime < time * 1000) {

				if (DEBUG)
					System.out.println("In while loop \n startTime: "
							+ startTime + "\n current time: "
							+ System.currentTimeMillis()
							+ "\n requested time: " + time * 1000);

				outToServer.write(arr, 0, 1024);
				bytesSent = bytesSent + 1024;

			}// end of while loop

			socket.close();
		}// end try
		catch (SocketException e) {
			System.err.println("No host or port for given connection");
		} catch (IOException e) {
			System.err.println("I/O Error on host");
		}

		int sent = bytesSent/1024;
		double Mbps = bytesSent/1024/128/time;
		BigDecimal bd = new BigDecimal(Mbps);
		bd = bd.setScale(3, bd.ROUND_HALF_UP);

		System.out.println("sent=" + sent + " KB rate=" + bd.toString() + " Mbps");
	}

	static void ServerMode() {
		
		int read = 0;
		int totalRead = 1;
		char[] arr = new char[1024];
		ServerSocket welcomeSocket;
		try {

			welcomeSocket = new ServerSocket(port);
						
			boolean started = false;
			long startTime = 0;
			while(true){
			
				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()));
				read = 0;
				started = false;
				totalRead = 1;
				while (read != -1) {
					read = inFromClient.read(arr, 0, 1024);
					if( !started ) {
						startTime = System.currentTimeMillis();
						started = true;
					}
					totalRead += read;

				}
				totalRead += 1;
				time = (System.currentTimeMillis() - startTime) / 1000;
		
				int received = totalRead/1024;
				double Mpbs = totalRead/1024/128/time;
				BigDecimal bd = new BigDecimal(Mpbs);
				bd = bd.setScale(3, bd.ROUND_HALF_UP);
	
				System.out.println("received=" + received + " KB rate="	+ bd.toString() + " Mbps" );
				
			}//end while loop
		}//end try

		catch (IOException e1) {
			System.err.println("I/O Error on host");
		}
	}

	public static void processArgs(String[] args) {
		if (DEBUG) {
			System.out.println("Processing Command Line Arguments\n");
		}

		// confirm a valid number of arguments was entered
		if (args.length != 3 && args.length != 7) {
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}

		for (int index = 0; index < args.length; index++) {
			switch (args[index]) {
			case "-p":
				port = Integer.parseInt(args[index + 1]);
				break;
			case "-h":
				hostName = args[index + 1];
				break;
			case "-t":
				time = Double.parseDouble(args[index + 1]);
				break;
			case "-c":
				isClient = true;
				break;
			}
		}
	}
}
