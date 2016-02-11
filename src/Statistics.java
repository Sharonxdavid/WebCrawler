import java.util.ArrayList;
import java.util.HashMap;

public class Statistics {

	HashMap<String, String> map;
	ArrayList<String> visitedURL;
	ArrayList<String> visitedImg;
	ArrayList<String> visited;
	ArrayList<String> externalLink;
	ArrayList<Long> rtt;
	
	public Statistics() {
		visitedImg = new ArrayList<>();
		visitedURL = new ArrayList<>();
		visited = new ArrayList<>();
		externalLink = new ArrayList<>();
		rtt = new ArrayList<>();
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
