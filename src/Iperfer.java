import java.util.Date;
import java.util.Hashtable;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

public class Iperfer
{
	public static boolean isClient; //

	public static String hostName;
	public static int port;
	public static double time;

	// DEBUG stuff
	final static boolean DEBUG = true;

	public static void main(String args[])
	{
		processArgs( args );

		if ( isClient ) {

			if (DEBUG)
				System.out.println("Starting up in Client Mode");

			ClientMode( );
		} 
		else {
			// SERVER MODE - uses -l
			if (DEBUG)
				System.out.println("Starting up in Server Mode");

			ServerMode( );
		}


	}

	static void ClientMode( ) {

		int bytesSent = 0;
		Socket socket = null;
		DataOutputStream outToServer = null; 

		try {			
			socket = new Socket("localhost", port);
			outToServer = new DataOutputStream(socket.getOutputStream());
			System.out.println( "output stream: " + outToServer.toString() );
		} 

		catch(SocketException e){
			System.err.println("No host or port for given connection, " + e);
		}

		catch(IOException e){
			System.err.println("I/O Error on host, " + e);
		}

		byte[] arr = new byte[1024];

		System.out.println(arr.toString());
		long startTime = System.currentTimeMillis();


		while (System.currentTimeMillis() - startTime < time*1000) {

			if (DEBUG)
				System.out.println("In while loop \n startTime: " + startTime + "\n current time: " 
						+ System.currentTimeMillis() + "\n requested time: " + time*1000);

			try {
				outToServer.write(arr, 0, 1024);
				bytesSent = bytesSent + 1024;
			} 

			catch (IOException e) {
				System.out.println("ERROR: Failed to send packet");
			}

		}//end of while loop

		try {
			socket.close();
		} 

		catch (IOException e) {
			System.err.println("I/O Error on host");
		}

		System.out.println("sent=" + bytesSent/1024 + "KB" + " rate=" 
				+ ((bytesSent/1024)/128)/time);
	}

	static void ServerMode( ) {
		int read = 0;
		int totalRead = 1;
		char[] arr = new char[1024];
		ServerSocket welcomeSocket;
		try {
			
			welcomeSocket = new ServerSocket(port);
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			long startTime = System.currentTimeMillis();

			while(read != -1)
			{
				try {
					read = inFromClient.read(arr, 0, 1024);
					totalRead += read;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("received=" + totalRead/1024 + " KB rate=" 
					+ ((totalRead/1024)/128)/(System.currentTimeMillis()/(time*1000)));
		} 
		catch (IOException e1) {
			//TODO: add statem.
		}
	}


	public static void processArgs(String[] args) 
	{
		if ( DEBUG ) {
			System.out.println("Processing Command Line Arguments\n");
		}

		// confirm a valid number of arguments was entered
		if (args.length != 3 && args.length != 7) {
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}

		for ( int index = 0; index < args.length; index++) {
			switch ( args[index] ) {
			case "-p":
				port = Integer.parseInt( args[index + 1] );
				break;
			case "-h":
				hostName = args[index + 1];
				break;
			case "-t":
				time = Integer.parseInt( args[index + 1] );
				break;
			case "-c":
				isClient = true;
				break;
			}
		}
	}
}