import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Analyzer implements Runnable {

	private SynchronizedQueue<String> URLtoDownload;
	private SynchronizedQueue<AnalyzerQueueObject> HTMLtoAnalyze;
	int imgCounter = 0;
	int urlCounter = 0;
//	HashMap<String, String> params = new HashMap<String, String>();
	HashMap<String, Statistics> domainMap;
	class1 c1object;
	

	public Analyzer(SynchronizedQueue<String> url,
			SynchronizedQueue<AnalyzerQueueObject> data, HashMap<String, Statistics> domainMap, class1 c1object) {

		this.HTMLtoAnalyze = data;
		this.URLtoDownload = url;
		this.domainMap = domainMap;
		this.c1object = c1object;
		this.URLtoDownload.registerProducer();
	}

	@Override
	public void run() {
		AnalyzerQueueObject currData;
		String currPageBody;

		while ((currData = HTMLtoAnalyze.dequeue()) != null) {
			System.out.println("Before increment Analyzer");
			c1object.increment();
			// analyze currPage
			// insert new url to url queue
			System.out.println("Analyzer Object Params:");
			System.out.println("1" + currData.url);
			System.out.println("2" + currData.host);
			System.out.println("3" + currData.htmlBody);
			System.out.println("4" + currData.downloadDate);
			System.out.println("5 Domain and Date " + currData.toString());
			
//			if(!domainMap.containsKey(currData.host)){
//				domainMap.put(currData.host, new Statistics());
//				domainMap.get(currData.host).addToKey("Domain Name", currData.host);
//			}
			
			currPageBody = currData.htmlBody;
			crawlHref(currPageBody, currData.host);
			System.out.println("# of urls " + urlCounter);
			domainMap.get(currData.host).addToKey("# of urls", String.valueOf(urlCounter));
//			params.put("# of imgs", Integer.toString(imgCounter));
//			try {
//				crawlImgs(currPageBody, currData.host);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			domainMap.get(currData.host).addToKey("# of imgs", String.valueOf(imgCounter));
//			params.put("# of imgs", Integer.toString(imgCounter));
			System.out.println("# of imgs " + imgCounter);
//			if(!(currData.url.equals(currData.host)))
//				try {
//					createPageStats(params, currData.toString());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			System.out.println("before decrement analyzer");
			c1object.decrement();
		}
	}

	private void createPageStats(HashMap<String, String> params, String domain)
			throws IOException {
		System.out.println("DOMAIN IN STATS IS: " + domain);
		File file = new File(domain + ".html");
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

	private void crawlHref(String currPage, String host) {
		Pattern p = Pattern.compile("<a href=\"(.*?)\">");
		Matcher m = p.matcher(currPage);
		System.out.println("**Matcher loop**");
		String nextUrl;
		while (m.find()) {
			System.out.println("Group 0 is " + m.group(0));
			System.out.println(m.group(1));
			urlCounter++;
			if (m.group(1).startsWith("http://")) {
				String temp = m.group(1).substring(7);
				String relativePath = "";
				String[] levels = temp.split("/");
				String domainHost = levels[0];
				if (levels.length == 1)
					relativePath = "";
				else {
					for (int i = 1; i < levels.length; i++) {
						relativePath = relativePath + "/" + levels[i];
					}
				}
				System.out.println("Domain Host is: " + domainHost);
				System.out.println("Relative path is: " + relativePath);
				if (domainHost.equalsIgnoreCase(host)) {
					nextUrl = host + relativePath;
				} else {
					nextUrl = domainHost + relativePath;
				}
			} else {
				if (m.group(1).startsWith("/")) {
					nextUrl = host + m.group(1);
				} else {
					String relativePath = "";
					String[] levels = m.group(1).split("/");
					String domainHost = levels[0];
					if (levels.length == 1)
						relativePath = "";
					else {
						for (int i = 1; i < levels.length; i++) {
							relativePath = relativePath + "/" + levels[i];
						}
					}
					nextUrl = domainHost + relativePath;
				}
			}
			System.out.println("NEXT URL IS " + nextUrl);
			URLtoDownload.enqueue(nextUrl);
		}
	}

	private void crawlImgs(String currPage, String host)
			throws UnknownHostException, IOException {
		Pattern p = Pattern.compile("<img.+?src=\"(.+?)\".*?>");
		Matcher m = p.matcher(currPage);
		System.out.println("**Matcher loop**");
		int imgSizeCounter = 0;
		while (m.find()) {
			System.out.println(m.group(0));
			HttpHeadRequest getImgSize = new HttpHeadRequest(host);
			String reqAsString = HttpHeadRequest.generateHeadRequestAsString(
					getImgSize,
					m.group(0).substring(10, m.group(0).indexOf('"', 11)));
			System.out.println("----This is HEAD Request----");
			System.out.println(reqAsString);

			Socket socket = new Socket(host, 80);

			DataOutputStream outputStream = new DataOutputStream(
					socket.getOutputStream());
			System.out.println("Analyzer Port is " + socket.getPort());
			outputStream.write(reqAsString.getBytes());

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				if (line.startsWith("Content-Length:"))
					imgSizeCounter = imgSizeCounter
							+ Integer.valueOf(line.substring(16));
			}
			imgCounter++;
		}
		System.out.println("@@IMGS SIZE IS: " + imgSizeCounter);
		domainMap.get(host).addToKey("Total size of imgs", String.valueOf(imgSizeCounter));
//		params.put("total size of imgs", Integer.toString(imgSizeCounter));
	}

	public static class HttpHeadRequest {
		public String requestHeaders;
		public HashMap<String, String> headerParams;
		public static HttpMethods methodType = HttpMethods.HEAD;
		public static String httpVersion = "HTTP/1.1";
		public String UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
		public static String CRLF = "\r\n";
		public static String domainHost;

		public HttpHeadRequest(String host) {
			this.domainHost = host;
		}

		public static String generateHeadRequestAsString(HttpHeadRequest req,
				String path) {
			String res;
			if (path.equals(""))
				res = methodType + " " + "/" + " " + httpVersion + CRLF;
			else
				res = methodType + " " + path + " " + httpVersion + CRLF;
			res = res + "Host: " + req.domainHost + " " + CRLF;
			res = res + CRLF;
			return res;
		}
	}

}
