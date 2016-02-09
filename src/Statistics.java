import java.util.HashMap;

public class Statistics {

	HashMap<String, String> map;

	public Statistics() {
		map = new HashMap<>();
		map.put("Domain Name", "");
		map.put("# of imgs", "0");
		map.put("Total img size", "0");
		map.put("# of videos", "0");
		map.put("Total videos size", "0");
		map.put("# of documents", "0");
		map.put("Total documents size", "0");
		map.put("# of urls", "0");
		map.put("Total url size", "0");
		map.put("# of external links", "0");
		map.put("# of internal links","0");
		map.put("Respect robot.txt", "");
		map.put("open ports", "0");
		map.put("AVG RTT", "0");
	}

	public synchronized void addToKey(String key, String value) {
		if (map.containsKey(key)) {
			int temp = Integer.valueOf(map.get(key));
			map.put(key, String.valueOf(temp + Integer.valueOf(value)));
		} else {
			map.put(key, value);
		}

	}

	public synchronized void concateToKey(String key, String value) {
		if (map.containsKey(key)) {
			String temp = map.get(key);
			map.put(key, temp + "," + value);
		} else {
			map.put(key, value);
		}
	}
}
