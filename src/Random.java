import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author rakeshgururaj Parameters: args[0] - Path to the filename containing
 *         the Cache File (Large/small trace) args[1] - S: Size of cache, the
 *         number of words that can be stored in the cache args[2] - N: The
 *         set-associativity of the cache, the number of sets in the cache
 *         args[3] - L: The line length, the number of words in a line
 */
public class Random {
	int cacheSize;
	int setSize;
	int lineLength;
	int indexLength;
	int tagLength;
	int wordLength = 2;
	int numberOfLines;
	int hitRate = 0;
	int missRate = 0;
	int ReadLineCycles = 11; // Number of cycles it takes to read a line from memory to cache.
	int ReadWordCycles = 10; // Number of cycles it takes to read a word from memory to cache.
	int WriteLineCycles = 15; // Number of cycles it takes to write a line from cache to memory.
	int WriteWordCycles = 12; // Number of cycles it takes to write a word from cache to memory.
	int totalCycles = 0;

	String fileNameString;
	String choice;

	double k = Double.MAX_VALUE;

	boolean writeType = false;

	List<String> hitMissArray = new ArrayList<String>();

	private LinkedHashMap<String, List<LinkedHashMap<String, Block>>> cacheMap;
	private LinkedHashMap<String, List<LinkedHashMap<String, Block>>> mainMemory;

	public Random(int cacheSize, int setSize, int lineLength, String fileName) {
		this.cacheSize = cacheSize;
		this.setSize = setSize;
		this.lineLength = lineLength;
		this.fileNameString = fileName;
	}

	public Random() {

	}

	public void initialize() {
		calculateActualSizes();
		cacheMap = new LinkedHashMap<String, List<LinkedHashMap<String, Block>>>((int) Math.pow(2, indexLength),
				(float) 0.1, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, List<LinkedHashMap<String, Block>>> eldest) {
				return this.size() > (Math.pow(2, indexLength));
			}
		};
		mainMemory = new LinkedHashMap<String, List<LinkedHashMap<String, Block>>>((int) Math.pow(2, indexLength),
				(float) 0.1, true);
	}

	public void run() throws FileNotFoundException {

		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter your simulation option \n 1. Cache write policy \n 2. Simulate prefetching:");
		int choice = 1;// scanner.nextInt();

		System.out.println("Enter the value for 'k' : ");
		k = 100;// scanner.nextDouble(); // Parameter k - the number of lines that needs to be
				// considered in the Cache trace.

		System.out.println(
				"Enter the type of Write. \n 1. Type true for Write Back with Write allocate \n 2. Type false for Write-through with no-write-allocate");
		writeType = scanner.nextBoolean();

		File inputFile = new File(fileNameString);
		Scanner scannerFile = new Scanner(inputFile);
		// mainMemory = MemoryLoader.loadMainMemory(scannerFile,k);
		writeBack(scannerFile); // Call the LRU logic here and Switch to the option you need.

		switch (choice) {
		case 1: // Display the Hit Ratio
			System.out.println("Maximum number of words stored in Cache: " + cacheSize);
			System.out.println("Associativity is " + setSize + "-way associativity");
			System.out.println("HitRate: " + hitRate);
			System.out.println("Miss Rate: " + missRate);
			break;

		case 2: // Display the simulation output (Array of hits or miss)
			System.out.println("Maximum number of words stored in Cache: " + cacheSize);
			System.out.println("Associativity is " + setSize + "-way associativity");
			System.out.println("HitRate: " + hitRate);
			System.out.println("Miss Rate: " + missRate);
			System.out.println("The output array is: " + hitMissArray.toString().replaceAll(",", ""));
			break;

		default:
			System.err.println("Please select a valid option.");
			scanner.close();

		}
	}

	/**
	 * Populate the values of tag_bits, index_bits from cache size(input parameter)
	 * and set the value to relevant variables.
	 */
	private void calculateActualSizes() {
		indexLength = (int) Math.getExponent((cacheSize / lineLength) / setSize);
		numberOfLines = (int) (cacheSize / lineLength);
		lineLength = (int) Math.getExponent(lineLength);
	}

	/**
	 * This methods converts the hexadecimal address to binary address
	 * 
	 * @param address
	 * @return address in the binary format
	 */
	private String convertToBinary(String address) {
		return new BigInteger(address, 16).toString(2);
	}

	/**
	 * This method splits tag_bits and index_bits from the main address
	 * 
	 * @param data
	 * @return Tag address and Index address
	 */
	public String[] calculateAddresses(String data) {
		String addresses[] = new String[5];
		String splitAddress[] = data.split("\\s+");

		String binAddressString = convertToBinary(splitAddress[0]);
		String tagAddress = binAddressString.substring(0,
				binAddressString.length() - (wordLength + lineLength + indexLength));
		String indexAddress = binAddressString.substring(tagAddress.length(),
				binAddressString.length() - (wordLength + lineLength));
		String lineAddress = binAddressString.substring(tagAddress.length() + indexAddress.length(),
				binAddressString.length() - (lineLength));
		String wordAddress = binAddressString
				.substring(tagAddress.length() + indexAddress.length() + lineAddress.length());
		addresses[0] = tagAddress;
		addresses[1] = indexAddress;
		addresses[2] = lineAddress;
		addresses[3] = wordAddress;
		addresses[4] = splitAddress[1];
		return addresses;
	}

	/**
	 * This method contains the logic for LRU cache
	 * 
	 * @param scanner
	 * 
	 */
	private void writeBack(Scanner scanner) {
		int counter = 0;
		while (scanner.hasNextLine() && counter < k) {

			String[] addressesString = calculateAddresses(scanner.nextLine());
			String tag = addressesString[0];
			String index = addressesString[1];
			String line = addressesString[2];
			String word = addressesString[3];
			String instruction = addressesString[4];

			boolean hit = false;
			List<LinkedHashMap<String, Block>> list = new ArrayList<LinkedHashMap<String, Block>>();
			LinkedHashMap<String, Block> tempMap = new LinkedHashMap<String, Block>();
//			if (counter == 8) {
//				System.out.println("Stop");
//			}
			if (cacheMap.containsKey(index)) {
				list = cacheMap.get(index);
				for (LinkedHashMap<String, Block> tagList : list) {
					if (tagList.containsKey(tag)) {
						// System.out.println("hit");

						if (instruction.equals("S")) {
							tagList.get(tag).dirty = 1;
						}
						list.remove(tagList);
						list.add(tagList);

						// cacheMap.put(index, list);
						hit = true;
						hitMissArray.add("H");
						hitRate += 1;
						break;
					}

				}

				if (!hit) {

					cacheMissWriteBlock(list, tempMap, tag, index, line, instruction);

				}
			} else {

				cacheMissWriteBlock(list, tempMap, tag, index, line, instruction);
			}
			printLinkedMap(counter);
			counter++;
		}

	}

	private void cacheMissWriteBlock(List<LinkedHashMap<String, Block>> cacheList, LinkedHashMap<String, Block> tempMap,
			String tag, String index, String line, String instruction) {

		List<LinkedHashMap<String, Block>> list;

		LinkedHashMap<String, Block> removedEntry = new LinkedHashMap<String, Block>();
		int dirtyBit = 0;
		hitMissArray.add("M");
		missRate += 1;

		tempMap.put(tag, new Block(0, line));
		list = mainMemory.get(index);

		if (list != null) {
			list.add(tempMap);
			cacheList.add(tempMap);
			if (instruction.equals("S")) {
				totalCycles += WriteWordCycles;
			}
			mainMemory.put(index, list);

			if (cacheList.size() > setSize && writeType) {
				removedEntry = cacheList.get(0);
				dirtyBit = ((Block) removedEntry.values().toArray()[0]).dirty;

				if (dirtyBit == 1) {
					totalCycles += WriteLineCycles;
					// mainMemory.put(index, list);

				}
				cacheList.remove(0);
				// cacheList.add(tempMap);
				totalCycles += ReadLineCycles;
				cacheMap.put(index, cacheList);

			} else if (writeType || !writeType && instruction.equals("L")) {
				totalCycles += ReadLineCycles;
				cacheMap.put(index, cacheList);
			}

		} else {
			list = new ArrayList<LinkedHashMap<String, Block>>();
			list.add(tempMap);
			cacheList.add(tempMap);
			if (instruction.equals("S")) {
				totalCycles += WriteWordCycles;
			}
			mainMemory.put(index, list);
			if (cacheList.size() > setSize && writeType
					|| cacheList.size() > setSize && !writeType && instruction.equals("L")) {
				removedEntry = cacheList.remove(0);
				// list.add(tempMap);
				totalCycles += ReadLineCycles;
				cacheMap.put(index, cacheList);

			} else if (writeType || !writeType && instruction.equals("L")) {
				totalCycles += ReadLineCycles;
				cacheMap.put(index, cacheList);
			}

		}
	}

	/**
	 * This method is to display all the contents of the cache.
	 * 
	 * @param counter
	 */
	private void printLinkedMap(int counter) {
		System.out.println("Record ID = " + (counter + 1));
		for (Map.Entry<String, List<LinkedHashMap<String, Block>>> e : cacheMap.entrySet()) {
			for (LinkedHashMap<String, Block> e1 : e.getValue())
				System.out.println(e.getKey() + " = " + e.getValue());
			System.out.println("xxxxxxxxx");
		}
		System.out.println("total Cycle:" + totalCycles);
		System.out.println("---------------------------------------------------------");

	}

	private void writeThrough(Scanner scanner) {
		int counter = 0;
		while (scanner.hasNextLine() && counter < k) {

			String[] addressesString = calculateAddresses(scanner.nextLine());
			String tag = addressesString[0];
			String index = addressesString[1];
			String line = addressesString[2];
			String word = addressesString[3];
			String instruction = addressesString[4];

			boolean hit = false;
			List<LinkedHashMap<String, Block>> list = new ArrayList<LinkedHashMap<String, Block>>();
			LinkedHashMap<String, Block> tempMap = new LinkedHashMap<String, Block>();

			if (cacheMap.containsKey(index)) {
				list = cacheMap.get(index);
				for (LinkedHashMap<String, Block> tagList : list) {
					if (tagList.containsKey(tag)) {
						// System.out.println("hit");

						if (instruction.equals("S")) {
							totalCycles += WriteWordCycles;

						}
						list.remove(tagList);
						list.add(tagList);
						mainMemory.put(index, list);
						// cacheMap.put(index, list);
						hit = true;
						hitMissArray.add("H");
						hitRate += 1;
						break;
					}

				}

				if (!hit) {

					cacheMissThrough(list, tempMap, tag, index, line, instruction);
				}
			} else {

				cacheMissThrough(list, tempMap, tag, index, line, instruction);
			}
			printLinkedMap(counter);
			counter++;
		}
	}

	private void cacheMissThrough(List<LinkedHashMap<String, Block>> cacheList, LinkedHashMap<String, Block> tempMap,
			String tag, String index, String line, String instruction) {

		List<LinkedHashMap<String, Block>> list;

		hitMissArray.add("M");
		missRate += 1;

		tempMap.put(tag, new Block(0, line));
		list = mainMemory.get(index);
		if(list == null) {
			list = new ArrayList<LinkedHashMap<String, Block>>();
		}
		list.add(tempMap);
		cacheList.add(tempMap);
		if (instruction.equals("S")) {
			totalCycles += WriteWordCycles;
		}
		mainMemory.put(index, list);
		if (instruction.equals("L")) {
			if (cacheList.size() > setSize) {
				cacheList.remove(0);
			}

			totalCycles += ReadLineCycles;
			cacheMap.put(index, cacheList);
		
		}
	}

//	
//	public class Block{
//		
//	}
}

