import java.util.*;

public class MemoryLoader {
	static Application app = new Application();

	public static LinkedHashMap<String, List<LinkedHashMap<String, Block>>> loadMainMemory(Scanner scanner, double k) {

		LinkedHashMap<String, List<LinkedHashMap<String, Block>>> mainMemory = new LinkedHashMap<String, List<LinkedHashMap<String, Block>>>();
		int counter = 0;
		while (scanner.hasNextLine() && counter < k) {
			String[] instruction = scanner.nextLine().split("\\s+");
			if (instruction[1].equals("S")) {
				String[] addressesString = app.calculateAddresses(scanner.nextLine());

				List<LinkedHashMap<String, Block>> list;
				LinkedHashMap<String, Block> tempMap;
				if (mainMemory.containsKey(addressesString[1])) {
					list = mainMemory.get(addressesString[1]);
					for (LinkedHashMap<String, Block> tagList : list) {
						if (!tagList.containsKey(addressesString[0])) {
							list.add(tagList);
							mainMemory.put(addressesString[1], list);
							break;
						}
					}

				} else {
					tempMap = new LinkedHashMap<String, Block>();
					tempMap.put(addressesString[0], new Block(1, addressesString[1]));
					list = mainMemory.get(addressesString[1]);
					if (list != null) {
							list.add(tempMap);
							mainMemory.put(addressesString[1], list);						
					} else {
						list = new ArrayList<LinkedHashMap<String, Block>>();
						list.add(tempMap);
						mainMemory.put(addressesString[1], list);
					}

				}
				//app.printLinkedMap(counter);
				counter++;
			}
		}
		return mainMemory;
	}
}
