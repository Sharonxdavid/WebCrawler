import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class ExecResListener implements Runnable {

	SynchronizedQueue<String> downloaderQueue;
	SynchronizedQueue<AnalyzerQueueObject> analyzerQueue;
	SynchronizedQueue<Integer> ports;
	Statistics stats;
	HashMap<String, Statistics> domainMap;
	class1 c1object;
	String host;

	public ExecResListener(SynchronizedQueue<String> downloaderQueue,
			SynchronizedQueue<AnalyzerQueueObject> analyzerQueue,
			HashMap<String, Statistics> domainMap, String host,
			class1 class1Object, SynchronizedQueue<Integer> ports) {
		this.downloaderQueue = downloaderQueue;
		this.analyzerQueue = analyzerQueue;
		this.domainMap = domainMap;
		this.stats = domainMap.get(host);
		this.c1object = class1Object;
		this.host = host;
		this.ports = ports;
	}

	@Override
	public void run() {
		System.out.println("---Started ExecResListener---");
		while (true) {
			if (c1object.isDone(downloaderQueue, analyzerQueue)) {
				break;
			} else {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			System.out.println("---Inside ExecResListener---");
			System.out.println("The Domain here is: "
					+ this.stats.map.get("Domain Name"));
			createPageStats(this.stats.map, host);
//			createOldResultsPage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String fileNameFormat(String domain) {
		String res;
		res = domain + "_" + c1object.startCrawlDateTime;
		return res;
	}
	
	private String calcAvgRtt(ArrayList<Long> times){
		long counter = 0;
		for (Long t : times) {
			counter += t;
		}
		long avg = counter/times.size();
		return String.valueOf(avg);
	}

	private void createPageStats(HashMap<String, String> params, String domain)
			throws IOException {
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("---STATS PARAMS--- " + params);
		
		String connectedDomain="";
		int connectedCounter = 0;
		for (String url : this.stats.externalLink) {
			if(domainMap.containsKey(url)){
				connectedDomain = connectedDomain + url + "\r\n";
				connectedCounter++;
			}
		}
		params.put("Connected Domain", connectedDomain);
		params.put("# of connected domain", Integer.toString(connectedCounter));
		
		if(!params.containsKey("# of videos")){
			params.put("# of videos", "0");
		}
		if(!params.containsKey("# of docs")){
			params.put("# of docs", "0");
		}
		if(!params.containsKey("Total video size")){
			params.put("Total video size", "0");
		}
		if(!params.containsKey("Total documents size")){
			params.put("Total documents size", "0");
		}
		if(!params.containsKey("Total img size")){
			params.put("Total img size", "0");
		}
		String avgRtt = calcAvgRtt(this.stats.rtt);
		params.put("AVG rtt", avgRtt);
		
		String robots = Boolean.toString(c1object.respectRobots);
		params.put("Respect robot.txt", robots);
		
		if(c1object.portScan){
			params.put("Opened Port", c1object.portScanRes.toString());
		}
		
		File file = new File(c1object.rootPath + fileNameFormat(domain)
				+ ".html");
//		File file = new File(fileNameFormat(domain) + ".html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		bw.write("<!DOCTYPE html>");

		bw.write("<head>");
		// bw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
		bw.write("</head>");
		bw.write("<body bgcolor='#A9D0F5'>");
		bw.write("<iframe src=\"http://free.timeanddate.com/clock/i4za5yzk/n676/szw110/szh110/hbw0/hfc111/cf100/hgr0/fav0/fiv0/mqcfff/mql15/mqw4/mqd94/mhcfff/mhl15/mhw4/mhd94/hhcbbb/hmcddd/hsceee\" frameborder=\"0\" width=\"110\" height=\"110\" align:\"right\"></iframe>");

		bw.write("<br>");

		bw.write("<h3>" + domain + "results" + "</h3>");
		bw.write("<br>");

		bw.write("<table border = \"1\"");
		bw.write("<tr> </tr>");

		for (Entry<String, String> entry : params.entrySet()) {
			bw.write("<tr> <td>");
			bw.write(entry.getKey());
			bw.write("</td> <td>");
			bw.write(entry.getValue());
			bw.write("</td> </tr>");
		}

		bw.write("</table>");
		bw.write("</div>");
		bw.write("<a href='/oldStats.html' class=\"link\">Back to index</a><br>");
		bw.write("</body>");
		bw.write("</html>");
		bw.close();

	}

	private void createOldResultsPage() throws IOException {
//		File file = new File(c1object.rootPath + "old results" + ".html");
		File file = new File("oldStats.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		bw.write("<!DOCTYPE html>");

		bw.write("<head>");
		// bw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
		bw.write("</head>");
		bw.write("<body bgcolor='#A9D0F5'>");
		bw.write("<iframe src=\"http://free.timeanddate.com/clock/i4za5yzk/n676/szw110/szh110/hbw0/hfc111/cf100/hgr0/fav0/fiv0/mqcfff/mql15/mqw4/mqd94/mhcfff/mhl15/mhw4/mhd94/hhcbbb/hmcddd/hsceee\" frameborder=\"0\" width=\"110\" height=\"110\" align:\"right\"></iframe>");

		bw.write("<br><br>");

		bw.write("<table border = \"1\"");
		bw.write("<tr>");
		bw.write("</tr>");

		for (Entry<String, Statistics> entry : domainMap.entrySet()) {
			bw.write("<tr> <td>");
			bw.write(entry.getKey());
			bw.write("</td> <td>");
//			bw.write("<a href = " + "\"" + c1object.rootPath + entry.getKey()
//					+ "_" + c1object.startCrawlDateTime
//					+ ".html\">Link to page");
			bw.write("<a href = " + "\"/"  + entry.getKey()
					+ "_" + c1object.startCrawlDateTime
					+ ".html\">Link to page");
			bw.write("</td> </tr>");
		}

		bw.write("</table>");
		bw.write("</div>");
		bw.write("<a href='/index.html' class=\"link\">Back to index</a><br>");
		bw.write("<a href='/form.html' class=\"link\">Back to form</a>");
		bw.write("</body>");
		bw.write("</html>");
		bw.close();

	}
}
