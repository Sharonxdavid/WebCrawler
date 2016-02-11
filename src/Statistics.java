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
