import java.util.Hashtable;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.io.IOException;

public class Pinger
{
	public static boolean isClient;

	public static int localPort;
	public static String hostName;
	public static int remotePort;
	public static int packetCount;

	// DEBUG stuff
	final static boolean DEBUG = false;
	final static double FAIL_CHANCE = 0; 

	public static void main(String args[])
	{
		processArgs( args );
		
		if ( isClient ) {
			// CLIENT MODE - uses -l, -h, -r, -c
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
		DatagramSocket socket = null;
		InetAddress localIP = null;
		InetAddress remoteIP = null;

		int sent = 0;
		int received = 0;
		int lost = 0;
		long sendTime = 0;
		long tripTime = 0;
		int minTripTime = 0;
		int avgTripTime = 0;
		int maxTripTime = 0;
		
		try {			
			// get IP address by hostname
			remoteIP = InetAddress.getByName(hostName);
		}
		catch ( IOException e) {
			System.out.println( "ERROR: Can't get host address, " + e);
		}
		
		try {
			if(DEBUG) 
				System.out.println("connecting to: " + remoteIP.toString() + " on port " + localPort);
			
			socket = new DatagramSocket( localPort );
		} catch (IOException e) {
			System.out.println("ERROR: Can't create socket, " + e);
		}
		
		for (int packetNum = 0; packetNum < packetCount; packetNum++) {
			// send packet
			ByteBuffer message = ByteBuffer.allocate(12);
			//sendTime = System.currentTimeMillis();

			// copy 4 byte sequence number and 8 byte timestamp into message
			message.putInt(0, packetNum);
			message.putLong( 4, sendTime = System.currentTimeMillis() );
			if (DEBUG)
				System.out.println("Packet " + message.getInt(0) + " sent at time " + message.getLong(4));

			try {
				DatagramPacket pingPacket = new DatagramPacket(message.array(), message.array().length, remoteIP, remotePort);
				socket.send(pingPacket);
				sent++;
			} catch (IOException e) {
				System.out.println("ERROR: Failed to send packet");
				continue;
			}

			DatagramPacket returnPacket = new DatagramPacket(new byte[12], 12);

			// receive packet
			try {
				socket.setSoTimeout(1000); // wait for 1 second before error
				socket.receive(returnPacket);
				received++;
				tripTime = System.currentTimeMillis() - sendTime;
				
			} catch (IOException e) {
				System.out.println("Didn't receive packet " + packetNum);
				lost++;
				continue;
			}
			
			ByteBuffer returnData = ByteBuffer.allocate(12);
			returnData.put(returnPacket.getData());
			tripTime = System.currentTimeMillis() - sendTime;
			System.out.println("size=" + returnData.array().length + " from=" + returnPacket.getAddress() + 
					" seq=" + returnData.getInt(0) + " rtt=" + tripTime + " ms");

			// try to sleep because threads are terrible
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// take care of trip time comparisons
			if (tripTime < minTripTime || minTripTime == 0) {
				minTripTime = (int)tripTime;
			}
			if (tripTime > maxTripTime) {
				maxTripTime = (int)tripTime;
			}
			avgTripTime += tripTime;
		}
		
		if ( received != 0 ) { 
			avgTripTime /= received;
		
			System.out.println("sent=" + sent + " received=" + received + 
					" lost=" + (100 - ( int )( ( ( double )received / ( double )sent ) * 100.0 ) ) + 
					"% rtt min/avg/max=" + minTripTime + "/" + avgTripTime + "/" + maxTripTime + "ms");
		}
		else {
			System.out.println("sent=" + sent + " received=" + received + 
					" lost=" + (100 - ( int )( ( ( double )received / ( double )sent ) * 100.0 ) ) + "%");
		}
	}
	
	static void ServerMode( ) {
		DatagramSocket socket = null;
		InetAddress clientAddress = null;
		Random random = new Random();
		
		long receivedTime = 0;
		InetAddress senderIP = null;
		int packetNum = 0;
		
		try {
			socket = new DatagramSocket( localPort );
		} catch (IOException e) {
			System.out.println("ERROR: Couldn't create socket, " + e);
		}
		
		System.out.println( "Waiting for packets..." );
		while(true) {
			DatagramPacket receivedPacket = new DatagramPacket(new byte[12], 12);
			
			try {
				socket.receive(receivedPacket);
			} catch (IOException e) {
				System.out.println("ERROR: Couldn't receive packet, " + e);
			}
			receivedTime = System.currentTimeMillis();
			
			// 30% chance the packet will be dropped
			if ( random.nextDouble() < FAIL_CHANCE ) {
				
				if ( DEBUG )
					System.out.println ( "Packet dropped" );
				
				continue;
			}
			
			// Send reply.
	        clientAddress = receivedPacket.getAddress();
	        int clientPort = receivedPacket.getPort();
	        byte[] buffer = receivedPacket.getData();
	        DatagramPacket reply = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
	        
	        try {
	        	socket.send(reply);
			} catch (IOException e) {
				System.out.println("ERROR: Could not reply, " + e);
			}
	         
	        ByteBuffer data = ByteBuffer.allocate(12);
	        data.put(buffer);
	        System.out.println("time=" + receivedTime + " from=" + clientAddress.getHostAddress() + " seq=" + data.getInt(0));
		}
	}

	
	public static void processArgs(String[] args) 
	{
		if ( DEBUG ) {
			System.out.println("Processing Command Line Arguments\n");
		}
		
		// confirm a valid number of arguments was entered
		if (args.length != 2 && args.length != 8) {
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}
		
		for ( int index = 0; index < args.length; index++) {
			switch ( args[index] ) {
				case "-l":
					localPort = Integer.parseInt( args[index + 1] );
					break;
				case "-h":
					hostName = args[index + 1];
					break;
				case "-r":
					remotePort = Integer.parseInt( args[index + 1] );
					break;
				case "-c":
					packetCount = Integer.parseInt( args[index + 1] );
					isClient = true;
					break;
			}
		}
	}
}
