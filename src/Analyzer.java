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
import java.util.ArrayList;
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
			if (domainMap.get(c1object.initialDomain).visited
					.contains(currData.url)) {
				c1object.decrement();
				continue;
			} else {
				// analyze currPage
				System.out.println(Thread.currentThread().getName()
						+ "Request content-length: " + currData.ContentLength
						+ " URL " + currData.url);

				// if(!domainMap.containsKey(currData.host)){
				// domainMap.put(currData.host, new Statistics());
				// domainMap.get(currData.host).addToKey("Domain Name",
				// currData.host);
				// }
				if (currData.statusCode.equalsIgnoreCase("200")) {
					if (!currData.host.equalsIgnoreCase(c1object.initialDomain)) {
						c1object.decrement();
						// this is external link
						System.out.println("EXTERNAL LINK" + currData.url);
						domainMap.get(c1object.initialDomain).externalLink
								.add(currData.url);
						domainMap.get(c1object.initialDomain).addToKey(
								"# of external links ", "1");
						continue;
					} else {
						domainMap.get(c1object.initialDomain).addToKey(
								"# of internal links ", "1");
					}
					// if img
					if (isImg(currData.url)) {
						System.out.println("IMG CONTENT LENGTH IS "
								+ currData.ContentLength);
						domainMap.get(currData.host).addToKey("Total img size",
								String.valueOf(currData.ContentLength));

					}
					// if video
					else if (isVideo(currData.url)) {
						System.out.println("VIDEO CONTENT LENGTH IS "
								+ currData.ContentLength);
						domainMap.get(currData.host).addToKey(
								"Total video size",
								String.valueOf(currData.ContentLength));
					}
					// if doc
					else if (isDoc(currData.url)) {
						System.out.println("DOC CONTENT LENGTH IS "
								+ currData.ContentLength);
						domainMap.get(currData.host).addToKey(
								"Total documents size",
								String.valueOf(currData.ContentLength));
					} else { // html page

						System.out.println("PAGE CONTENT LENGTH IS "
								+ currData.ContentLength);
						domainMap.get(currData.host).addToKey("Total url size",
								String.valueOf(currData.ContentLength));
						currPageBody = currData.httpResponseAsString;

						crawlHref(currPageBody, currData.host,
								currData.crawlPort);
						System.out.println(Thread.currentThread().getName()
								+ " " + "# of urls " + urlCounter);
						crawlImgs(currPageBody, currData.host, currData.url,
								currData.crawlPort);
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
	}

	public boolean isImg(String url) {
		ArrayList<String> temp = c1object.getImagesExtensions();
		for (String type : temp) {
			if (url.endsWith(type))
				return true;
		}
		return false;
	}

	public boolean isVideo(String url) {
		ArrayList<String> temp = c1object.getImagesExtensions();
		for (String type : temp) {
			if (url.endsWith(type))
				return true;
		}
		return false;
	}

	public boolean isDoc(String url) {
		ArrayList<String> temp = c1object.getImagesExtensions();
		for (String type : temp) {
			if (url.endsWith(type))
				return true;
		}
		return false;
	}

	private void crawlHref(String currPage, String host, int crawlPort) {
		Pattern p = Pattern.compile("<a href=\"(.*?)\">");
		// Pattern p = Pattern.compile("<a.+?href=\"(.*?)\">");
		Matcher m = p.matcher(currPage);
		boolean respectRobots = c1object.respectRobots;
		System.out.println("**Matcher loop**");
		String nextUrl;
		urlCounter = 0;
		while (m.find()) {
			System.out.println("VISITED " + domainMap.get(host).visited);
			if (m.group(0).contains("mailto")) {
				continue;
			}
			System.out.println("Group 0 is " + m.group(0));
			System.out.println(m.group(1));
			urlCounter++;
			if (m.group(1).startsWith("https")) {
				System.out.println("IS HTTPS");
				System.out.println("IS HTTPS");
				System.out.println("IS HTTPS");
				System.out.println("IS HTTPS");
				System.out.println("IS HTTPS");

				continue;
			}
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
				if (domainHost.equalsIgnoreCase(c1object.initialDomain)) {
					nextUrl = host + relativePath;
				} else {
					nextUrl = domainHost + relativePath;
				}
			} else {
				if (m.group(1).startsWith("/")) {
					// nextUrl = host + ":" + crawlPort + m.group(1);
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
			if (!domainMap.get(host).visited.contains(nextUrl)) {
				if (respectRobots) {
					int disallowLength = isDisallowed(nextUrl);
					if (disallowLength > 0) {
						
						int allowLength = isAllowed(nextUrl);
						if (allowLength > disallowLength) {
							// The link is allowed.
							URLtoDownload.enqueue(nextUrl);
						}
					} else {
						// disallowLength == 0
						URLtoDownload.enqueue(nextUrl);

					}
				} else {
					URLtoDownload.enqueue(nextUrl);
				}
			}
		}
		domainMap.get(host).addToKey("# of urls", String.valueOf(urlCounter));
	}

	private int isDisallowed(String nextUrl) {
		int maxLength = 0;
		for (RobotRule r : c1object.disallow) {
			Matcher m = r.pattern.matcher(nextUrl);
			if (m.find() && r.originalValue.length() > maxLength) {
				maxLength = r.originalValue.length();
			}
		}
		return maxLength;
	}
	
	private int isAllowed(String nextUrl) {
		int maxLength = 0;
		for (RobotRule r : c1object.allow) {
			Matcher m = r.pattern.matcher(nextUrl);
			if (m.find() && r.originalValue.length() > maxLength) {
				maxLength = r.originalValue.length();
			}
		}
		return maxLength;
	}
	
	private void crawlImgs(String currPage, String host, String url,
			int crawlPort) {
		imgCounter = 0;
		Pattern p = Pattern.compile("<img.+?src=\"(.+?)\".*?>");
		Matcher m = p.matcher(currPage);
		System.out.println("**Matcher loop**");
		String nextImg;
		boolean respectRobots = c1object.respectRobots;
		while (m.find()) {
			System.out.println("GROUP 0" + m.group(0));
			System.out.println("IMG GROUP 1" + m.group(1) + " URL " + url + " "
					+ domainMap.get(host).map.get("# of imgs"));
			imgCounter++;
			if (m.group(1).startsWith("https")) {
				continue;
			}
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
				if (domainHost.equalsIgnoreCase(c1object.initialDomain)) {
					nextImg = host + relativePath;
				} else {
					nextImg = domainHost + relativePath;
				}
			} else {
				if (m.group(1).startsWith("/")) {
					// nextImg = host + ":" + crawlPort + m.group(1);
					nextImg = host + m.group(1);
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
			if (!domainMap.get(host).visited.contains(nextImg)) {
				if (respectRobots) {
					int disallowLength = isDisallowed(nextImg);
					if (disallowLength > 0) {
						
						int allowLength = isAllowed(nextImg);
						if (allowLength > disallowLength) {
							// The link is allowed.
							URLtoDownload.enqueue(nextImg);
						}
					} else {
						// disallowLength == 0
						URLtoDownload.enqueue(nextImg);

					}
				} else {
					URLtoDownload.enqueue(nextImg);
				}
			}

		}
		domainMap.get(host).addToKey("# of imgs", String.valueOf(imgCounter));
	}

}
