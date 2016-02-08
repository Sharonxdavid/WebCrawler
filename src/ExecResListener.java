import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class ExecResListener implements Runnable {

	SynchronizedQueue<String> downloaderQueue;
	SynchronizedQueue<AnalyzerQueueObject> analyzerQueue;
	Statistics stats;
	HashMap<String, Statistics> domainMap;
	class1 c1object;
	String host;

	public ExecResListener(SynchronizedQueue<String> downloaderQueue,
			SynchronizedQueue<AnalyzerQueueObject> analyzerQueue,
			HashMap<String, Statistics> domainMap, String host,
			class1 class1Object) {
		this.downloaderQueue = downloaderQueue;
		this.analyzerQueue = analyzerQueue;
		this.domainMap = domainMap;
		this.stats = domainMap.get(host);
		this.c1object = class1Object;
		this.host = host;
	}

	@Override
	public void run() {
		System.out.println("---Started ExecResListener---");
		System.out.println("---Started ExecResListener---");
		while (true) {
			if (c1object.isDone(downloaderQueue, analyzerQueue)) {
				break;
			} else {
				try {
//					Thread.sleep(9000);
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {

			System.out.println("---Inside ExecResListener---");
			System.out.println("---Inside ExecResListener---");
			System.out.println("---Inside ExecResListener---");
			System.out.println("The Domain here is: " + this.stats.map.get("Domain Name"));
			createPageStats(this.stats.map, host);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String dateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sdf.format(new Date());
	}

	private String fileNameFormat(String domain) {
		String res;
		res = domain + "_" + this.dateTime();
		return res;
	}

	private void createPageStats(HashMap<String, String> params, String domain)
			throws IOException {
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("DOMAIN IN STATS IS: " + domain);
		System.out.println("---STATS PARAMS--- " + params);
//		File file = new File(domain + ".html");
		File file = new File(fileNameFormat(domain) + ".html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		bw.write("<!DOCTYPE html>");

		bw.write("<head>");
		// bw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
		bw.write("</head>");
		bw.write("<body bgcolor='#A9D0F5'>");
		bw.write("<iframe src=\"http://free.timeanddate.com/clock/i4za5yzk/n676/szw110/szh110/hbw0/hfc111/cf100/hgr0/fav0/fiv0/mqcfff/mql15/mqw4/mqd94/mhcfff/mhl15/mhw4/mhd94/hhcbbb/hmcddd/hsceee\" frameborder=\"0\" width=\"110\" height=\"110\" align:\"right\"></iframe>");

		bw.write("<h2>Computer networks 2015/2016</h2>");
		bw.write("<h3> Tal Bigel, Sharon David </h3>");

		bw.write("<br>");

		bw.write("<h3>" + domain + "</h3>");
		bw.write("<br>");

		bw.write("<table border = \"1\"");
		bw.write("<tr>");
		bw.write("</tr>");

		for (Entry<String, String> entry : params.entrySet()) {
			bw.write("<tr>");
			bw.write("<td>");
			bw.write(entry.getKey());
			bw.write("</td>");
			bw.write("<td>");
			bw.write(entry.getValue());
			bw.write("</td>");
			bw.write("</tr>");
		}

		bw.write("</table>");
		bw.write("</div>");
		bw.write("<a href='index.html' class=\"link\">Back to index</a><br>");
		bw.write("<a href='form.html' class=\"link\">Back to form</a>");
		bw.write("</body>");
		bw.write("</html>");
		bw.close();

	}
}
