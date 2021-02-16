
public class Program {
	public static void main(String[] args) {
		try {
			/*Parameters:
				args[0] - Path to the filename containing the Cache File (Large/small trace)
				args[1] - S: Size of cache, the number of words that can be stored in the cache
				args[2] - N: The set-associativity of the cache, the number of sets in the cache
				args[3] - L: The line length, the number of words in a line */
			
			if (args.length == 4) { 
				System.out.println("Cache Simulator starting....");
				System.out.println("Initial size of cache is 0");
				Application application = new Application(Integer.parseInt(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3]),args[0]);
				application.initialize();
				application.run();
			} else {
				System.err.println(
						"Please enter valid number of arguments: Filename | cachesize | setsize | linelength.");
			}
		}
		catch(Exception e) {
			System.out.println("An exception occured while simulating.Please find the details below\n"+e.getMessage());
		}
		
	}
}
