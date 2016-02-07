import java.util.HashMap;

public class Statistics {

	HashMap<String, String> map;

	public Statistics() {
		map = new HashMap<>();
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
