
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
		/*
		// confirm 
		if(args.length == 8) {
			isClient = true;
			System.out.println("Client Mode");
		} 
		else if(args.length != 2) {
			System.out.println("ERROR: missing or additional arguments");
		}
		else {
			System.out.println("Server Mode");
		}
		*/
		String[][] arguments =  processArgs(args);
		
		
	}
	
	public static String[][] processArgs(String[] args)
	{
		if(DEBUG) System.out.println("Processing Command Line Arguments");
		
		if(args.length != 2 && args.length != 8)
		{
			System.out.println("ERROR: missing or additional arguments");
			System.exit(1);
		}
		
		String[][] arguments = new String[args.length / 2][2];
		for(int row = 0; row < arguments.length; row++) {
			for(int col = 0; col < 2; col++) {
				arguments[row][col] = args[row * 2 + col].toString();
				
				if(col == 0) {
					if( !(arguments[row][col].equals("-l") ||
						arguments[row][col].equals("-r") ||
						arguments[row][col].equals("-h") ||
						arguments[row][col].equals("-c"))) 
					{
						System.out.println("ERROR: invalid argument " + arguments[row][col]);
						System.exit(1);
					}
					
					if(arguments[row][col].equals("-c"))	{
						isClient = true;
					}
				}
					
				if(DEBUG) System.out.print(arguments[row][col] + ((col == 0) ? " ":"\n"));
			}
		}
		
		if(DEBUG) System.out.println("\n" + ((isClient) ? "Client":"Server") + " Mode");
		
		return arguments;
	}	
}
