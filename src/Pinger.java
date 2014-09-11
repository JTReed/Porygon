import java.util.Hashtable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Pinger
{
	public static boolean isClient; //

	public static int localPort;
	public static String hostName;
	public static int remotePort;
	public static int packetCount;

	// DEBUG stuff
	final static boolean DEBUG = true;

	public static void main(String args[]) throws InterruptedException
	{
		// HashTable accessible with keys -l, -h, -r, -c
		Hashtable<String, String> arguments = processArgs(args);

		localPort = Integer.parseInt(arguments.get("-l"));
		if (isClient) {
			// CLIENT MODE - uses -l, -h, -r, -c
			if (DEBUG)
				System.out.println("Starting up in Client Mode");

			DatagramSocket socket = null;
			InetAddress localIP = null;
			InetAddress remoteIP = null;

			int sent = 0;
			int received = 0;
			double lost = 0.0;
			long sendTime = 0;
			double tripTime = 0.0;
			double minTripTime, avgTripTime, maxTripTime = 0.0;

			hostName = arguments.get("-h");
			remotePort = Integer.parseInt(arguments.get("-r"));
			packetCount = Integer.parseInt(arguments.get("-c"));

			try {
				// get IP address by hostname
				localIP = InetAddress.getLocalHost();
				remoteIP = InetAddress.getByName(hostName);
				
				if(DEBUG) System.out.println("remote: " + remoteIP.toString() + " on port " + remotePort);
				
				socket = new DatagramSocket(remotePort, remoteIP);
			} catch (IOException e) {
				System.out.println("ERROR: Can't create socket, " + e);
			}

			
			/*try { 
				socket.bind(socket.getRemoteSocketAddress()); 
			} 
			catch(IOException e) { 
				System.out.println("ERROR: Can't bind, " + e); 
			}*/
			 
	
			/*try {
				socket.connect(remoteIP, remotePort);
			} catch (IllegalArgumentException e) {
				System.out.println("ERROR: Can't connect, " + e);
			}*/
	
			for (int packetNum = 0; packetNum < packetCount; packetNum++) {
				// send packet here
				ByteBuffer message = ByteBuffer.allocate(12);
				sendTime = System.currentTimeMillis();
	
				// copy 4 byte sequence number and 8 byte timestamp into message
				message.putInt(0, packetNum);
				message.putLong(4, sendTime);
				if (DEBUG)
					System.out.println("Packet " + message.getInt(0) + " sent at time " + message.getLong(4));
	
				try {
					DatagramPacket pingPacket = new DatagramPacket(message.array(), 12, remoteIP, remotePort);
					socket.send(pingPacket);
				} catch (IOException e) {
					System.out.println("ERROR: Failed to send packet");
				}
	
				DatagramPacket returnPacket = new DatagramPacket(new byte[12], 12);
	
				try {
					socket.setSoTimeout(1000); // wait for 1 second timeout
					socket.receive(returnPacket);
	
					ByteBuffer returnedMessage = ByteBuffer.allocate(12);
					returnedMessage.put(returnPacket.getData());
					System.out.println("Received packet " + returnedMessage.getInt(0) + " with send time " + returnedMessage.getLong(4));
				} catch (IOException e) {
					System.out.println("ERROR: couldn't receive packet, " + e);
				}
	
				Thread.sleep(1000);
			}

		} else {
			// SERVER MODE - uses -l
			if (DEBUG)
				System.out.println("Starting up in Server Mode");
		}

	}

	public static Hashtable<String, String> processArgs(String[] args)
	{
		if (DEBUG)
			System.out.println("Processing Command Line Arguments\n");

		// confirm a valid number of arguments was entered
		if (args.length != 2 && args.length != 8) {
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}

		Hashtable<String, String> arguments = new Hashtable<>();

		for (int index = 0; index < args.length; index += 2) {
			arguments.put(args[index], args[index + 1]);

			// confirm that only accepted commands are entered
			if (!(args[index].equals("-l") || args[index].equals("-r") || args[index].equals("-h") || args[index].equals("-c"))) {
				System.out.println("ERROR: invalid argument");
				System.exit(1);
			}

			if (args[index].equals("-c")) {
				isClient = true;
			}

			// confirm that this is the correct number of arguments for each mode
			if ((isClient && arguments.size() != 4) && (!isClient && arguments.size() != 1)) {
				System.out.println("ERROR: missing or additional arguments");
				System.exit(1);
			}
		}

		return arguments;
	}
}
