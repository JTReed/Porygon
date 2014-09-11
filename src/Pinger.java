import java.util.*;
import java.net.*;


public class Pinger
{
	public static boolean isClient; //
	
	public static int localPort;
	public static String hostName;
	public static int hostPort;
	public static int packetCount;
	
	// DEBUG stuff
	final static boolean DEBUG = true;
	
	public static void main(String args[])
	{
		// HashTable accessible with keys -l, -h, -r, -c
		Hashtable<String, String> arguments =  processArgs(args);
		
		if(isClient) {
			// CLIENT MODE
		}
		else {
			// SERVER MODE
		}
		
		
	}
	
	public static Hashtable<String, String> processArgs(String[] args)
	{
		if(DEBUG) System.out.println("Processing Command Line Arguments\n");
		
		// confirm a valid number of arguments was entered
		if(args.length != 2 && args.length != 8)
		{
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}
		
		String[][] argu = new String[args.length / 2][2];
		Hashtable<String, String> arguments = new Hashtable<>();
		
		
		for(int index = 0; index < args.length; index += 2) {
			arguments.put(args[index], args[index + 1]);
			
			// confirm that only accepted commands are entered
			if( !(args[index].equals("-l") ||
					args[index].equals("-r") ||
					args[index].equals("-h") ||
					args[index].equals("-c"))) 
				{
					System.out.println("ERROR: invalid argument");
					System.exit(1);
				}
			
			if(args[index].equals("-c"))	{
				isClient = true;
			}
			
			// confirm that this is the correct number of arguments for each mode
			if((isClient && arguments.size() != 4) && (!isClient && arguments.size() != 1)) {
				System.out.println("ERROR: missing or additional arguments");
				System.exit(1);
			}
		}
			
		return arguments;
	}	
}
