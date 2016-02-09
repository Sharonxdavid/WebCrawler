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
	HashMap<String, Statistics> domainMap;
	class1 c1object;

	public Analyzer(SynchronizedQueue<String> url,
			SynchronizedQueue<AnalyzerQueueObject> data,
			HashMap<String, Statistics> domainMap, class1 c1object) {

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
			System.out.println(Thread.currentThread().getName() + " "
					+ "Before increment Analyzer");
			c1object.increment();
			// analyze currPage
			// insert new url to url queue
			System.out.println(Thread.currentThread().getName()
					+ "Request content-length: " + currData.ContentLength
					+ " URL " + currData.url);

//			 if(!domainMap.containsKey(currData.host)){
//			 domainMap.put(currData.host, new Statistics());
//			 domainMap.get(currData.host).addToKey("Domain Name",
//			 currData.host);
//			 }
			if (currData.statusCode.equalsIgnoreCase("200")) {
				// if img
				if (currData.ContentType.endsWith(".png")
						|| currData.ContentType.endsWith(".bmp")
						|| currData.ContentType.endsWith(".jpg")
						|| currData.ContentType.endsWith(".gif")
						|| currData.ContentType.endsWith(".ico")) {
					System.out.println("IMG CONTENT LENGTH IS "
							+ currData.ContentLength);
				}
				// if video
				else if (currData.ContentType.endsWith(".mp4")
						|| currData.ContentType.endsWith(".avi")
						|| currData.ContentType.endsWith(".mpg")
						|| currData.ContentType.endsWith(".wmv")
						|| currData.ContentType.endsWith(".mov")
						|| currData.ContentType.endsWith(".flv")
						|| currData.ContentType.endsWith(".swf")
						|| currData.ContentType.endsWith(".mkv")) {
					System.out.println("VIDEO CONTENT LENGTH IS "
							+ currData.ContentLength);
				}
				// if doc
				else if (currData.ContentType
						.endsWith(".pdf")
						|| currData.url.endsWith(".doc")
						|| currData.url.endsWith(".docx")
						|| currData.url.endsWith(".ppt")
						|| currData.url.endsWith(".pptx")
						|| currData.url.endsWith(".xlsx")
						|| currData.url.endsWith(".xls")) {
					System.out.println("DOC CONTENT LENGTH IS "
							+ currData.ContentLength);
				} else { // html page
					currPageBody = currData.httpResponseAsString;

					crawlHref(currPageBody, currData.host, currData.crawlPort);
					System.out.println(Thread.currentThread().getName() + " "
							+ "# of urls " + urlCounter);

					crawlImgs(currPageBody, currData.host, currData.url, currData.crawlPort);
				}

				System.out.println(Thread.currentThread().getName() + " "
						+ "before decrement analyzer");
				c1object.decrement();
			} else {
				System.out.println(Thread.currentThread().getName()
						+ "STATUS CODE IS NOT 200 OK");
				c1object.decrement();
			}

		}
	}

	private void crawlHref(String currPage, String host, int crawlPort) {
		Pattern p = Pattern.compile("<a href=\"(.*?)\">");
		Matcher m = p.matcher(currPage);
		System.out.println("**Matcher loop**");
		String nextUrl;
		urlCounter = 0;
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
					nextUrl = host  + ":" +crawlPort + m.group(1);
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
		System.out.println("$$$$$$$$$$$$$$$$$$" + Thread.currentThread().getName() + domainMap.containsKey(host)  + " host is " + host);
		domainMap.get(host).addToKey("# of urls", String.valueOf(urlCounter));
	}

	private void crawlImgs(String currPage, String host, String url, int crawlPort) {
		imgCounter = 0;
		Pattern p = Pattern.compile("<img.+?src=\"(.+?)\".*?>");
		Matcher m = p.matcher(currPage);
		System.out.println("**Matcher loop**");
		int imgSizeCounter = 0;
		String nextImg;
		while (m.find()) {
			System.out.println("GROUP 0" + m.group(0));
			System.out.println("IMG GROUP 1" + m.group(1) + " URL " + url + " "
					+ domainMap.get(host).map.get("# of imgs"));
			imgCounter++;
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
					nextImg = host + relativePath;
				} else {
					nextImg = domainHost + relativePath;
				}
			} else {
				if (m.group(1).startsWith("/")) {
					nextImg = host + ":" + crawlPort + m.group(1);
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
					nextImg = domainHost + relativePath;
				}
			}
			System.out.println("NEXT URL IS " + nextImg);
			URLtoDownload.enqueue(nextImg);
		}
		domainMap.get(host).addToKey("# of imgs", String.valueOf(imgCounter));
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
