import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;

public class Downloader implements Runnable {

	private SynchronizedQueue<String> URLtoDownload;
	private SynchronizedQueue<AnalyzerQueueObject> HTMLtoAnalyze;
	private DataOutputStream outputStream;
	private Socket socket;
	private HttpGetRequest currentRequest;

	HashMap<String, Statistics> domainMap;
	class1 c1object;

	int numOfLinks = 0;
	int linksSizeCounter = 0;
	int numOfImgs = 0;
	int imgSizeCounter = 0;

	private static String[] supportedVideoFormat = { "mov", "flv", "swf",
			"mkv", "avi", "mpg", "mp4", "wmv" };
	private static String[] supportedImageFormat = { "jpg", "png", "bmp", "gif" , "ico"};
	private static String[] supportedDocFormat = { "pdf", "doc", "docx", "xls",
			"xlsx", "ppt", "pptx" };

	public Downloader(SynchronizedQueue<String> url,
			SynchronizedQueue<AnalyzerQueueObject> analyzerQueue,
			HashMap<String, Statistics> domainMap, class1 c1object) {

		this.domainMap = domainMap;
		this.URLtoDownload = url;
		this.HTMLtoAnalyze = analyzerQueue;
		this.HTMLtoAnalyze.registerProducer();
		this.c1object = c1object;
	}

	@Override
	public void run() {
		int crawlPort = 80;
		System.out.println(new Date() + "Download starts");
		String currURL;

		while ((currURL = URLtoDownload.dequeue()) != null) {
			System.out.println(Thread.currentThread().getName() + " " + "before increment Downloader");
			c1object.increment();
			// get page
			// TODO: insert result to HTML queue
			System.out.println(Thread.currentThread().getName() + " " + Msgs.printMsg(Msgs.DOWNLOADER_INFO, currURL));
			String result;

			try {
				result = java.net.URLDecoder.decode(currURL, "UTF-8");
				System.out.println(Thread.currentThread().getName() + " " + "URL afetr decode is: " + result);
				// if(result.startsWith("https")){
				// System.out.println("HTTPS is unsupported.");
				// continue;
				// }
				if (result.startsWith("http://")) {
					result = result.substring(7);
				}
				String relativePath = "";
				String[] levels = result.split("/");
				String domainHost = levels[0];
				if (levels.length == 1)
					relativePath = "";
				else {
					for (int i = 1; i < levels.length; i++) {
						relativePath = relativePath + "/" + levels[i];
					}
				}
				if(domainHost.contains(":")){
					String[] temp = domainHost.split(":");
					domainHost = temp[0];
					crawlPort = Integer.valueOf(temp[1]);
				}
				
				System.out.println("************" +"url host is " + domainHost + "port " + crawlPort + " path is" + relativePath);
				System.out.println(Thread.currentThread().getName() + " " + "Domain Host is: " + domainHost);
				System.out.println(Thread.currentThread().getName() + " " + "Relative path is: " + relativePath);
				if (isImage(relativePath) || isVideo(relativePath)
						|| isDoc(relativePath)) {
					System.out.println(Thread.currentThread().getName() + " " + "media file");
					HttpHeadRequest currentHeadRequest = new HttpHeadRequest(domainHost);
					String reqAsString = currentHeadRequest.generateHeadRequestAsString(relativePath);
					System.out.println(Thread.currentThread().getName() + " " + "----This is HEAD Request----");
					System.out.println(Thread.currentThread().getName() + " " + reqAsString);
					socket = new Socket();
					socket.connect(new InetSocketAddress(domainHost, crawlPort), 1000);
					while (socket.isConnected() == false) {
						//TODO: enter timeout to make sure it quits sometime...
					}

					this.outputStream = new DataOutputStream(socket.getOutputStream());
					outputStream.write(reqAsString.getBytes());

					BufferedReader rd = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
//					ByteStringInputStream rd = new ByteStringInputStream(socket.getInputStream());
					String line;
					String urlSrcToAnalyze = "";
					System.out.println(Thread.currentThread().getName() + " " + "before read response");
					try {
						socket.setSoTimeout(5000);
						while ((line = rd.readLine()) != null) {
							System.out.println(Thread.currentThread().getName() +  " " + line);
							urlSrcToAnalyze += line + "\r\n";
						}
						socket.close();
						System.out.println(Thread.currentThread().getName() + " " + new Date() + " Enqueue Analyzer1");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost,crawlPort, urlSrcToAnalyze, new Date()));
					} catch (SocketTimeoutException e) {
						System.out.println(Thread.currentThread().getName() + " " + "Socket time out! (Expected:)");
						System.out.println(Thread.currentThread().getName() + " " + new Date() + " Enqueue Analyzer2");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost,crawlPort, urlSrcToAnalyze, new Date()));
					} catch (SocketException e){
						System.out.println(Thread.currentThread().getName() + " " + "Socket expcetion in here");
						e.getStackTrace();
					} catch (Exception e) {
						System.out.println(Thread.currentThread().getName() + " " + "failed to download " + result);
						e.printStackTrace();
					}
				}
				else {
					this.currentRequest = new HttpGetRequest(domainHost);
					String reqAsString = currentRequest.generateGetRequestAsString(relativePath);
					System.out.println(Thread.currentThread().getName() + " " + "----This is Get Request----");
					System.out.println(Thread.currentThread().getName() + " " + reqAsString);

					socket = new Socket();
					socket.connect(new InetSocketAddress(domainHost, crawlPort), 1000);
					while (socket.isConnected() == false) {
						//TODO: enter timeout to make sure it quits sometime...
					}

					this.outputStream = new DataOutputStream(socket.getOutputStream());
					outputStream.write(reqAsString.getBytes());

					BufferedReader rd = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
//					ByteStringInputStream rd = new ByteStringInputStream(socket.getInputStream());
					String line;
					String urlSrcToAnalyze = "";
					System.out.println(Thread.currentThread().getName() + " " + "before read response");
					try {
						socket.setSoTimeout(5000);
						while ((line = rd.readLine()) != null) {
							System.out.println(Thread.currentThread().getName() +  " " + line);
							urlSrcToAnalyze += line + "\r\n";
						}
						socket.close();
						System.out.println(Thread.currentThread().getName() + " " + new Date() + " Enqueue Analyzer1");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost,crawlPort, urlSrcToAnalyze, new Date()));
					} catch (SocketTimeoutException e) {
						System.out.println(Thread.currentThread().getName() + " " + "Socket time out! (Expected:)");
						System.out.println(Thread.currentThread().getName() + " " + new Date() + " Enqueue Analyzer2");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost,crawlPort, urlSrcToAnalyze, new Date()));
					} catch(SocketException e){
						System.out.println(Thread.currentThread().getName() + " " + "Socket Exception");
						e.printStackTrace();
					} catch (Exception e) {
						System.out.println(Thread.currentThread().getName() + " " + "failed to download " + result);
						e.printStackTrace();
					}
					
				
				} 

			} catch(Exception e){
				System.out.println(Thread.currentThread().getName() + " Exception!!!");
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " " + "decrement downloader");
			c1object.decrement();
		}
	}

	/***
	 * Check if requested URI is an HTML file. TODO REMOVE
	 */
	public boolean isHtml(String path) {
		if (path == null) {
			return false;
		}
		
		return path.equalsIgnoreCase("html") || path.equalsIgnoreCase("htm") || path.equals("");
	}

	/***
	 * Checks if requested URI is an image
	 */
	public boolean isImage(String path) {
		boolean isValidImgFormat = false;
		if (path == null) {
			return false;
		} else {
			for (int i = 0; i < supportedImageFormat.length; i++) {
				if(path.endsWith(supportedImageFormat[i]))
				isValidImgFormat = true;
			}
			return isValidImgFormat;
		}
	}

	/***
	 * Checks if requested URI is a video
	 */
	public boolean isVideo(String path) {
		boolean isValidVideoFormat = false;
		if (path == null) {
			return false;
		} else {
			for (int i = 0; i < supportedVideoFormat.length; i++) {
				if(path.endsWith(supportedVideoFormat[i]))
					isValidVideoFormat = true;
			}
			return isValidVideoFormat;
		}
	}

	/***
	 * Checks if requested URI is a document
	 */
	public boolean isDoc(String path) {
		boolean isValidDocFormat = false;
		if (path == null) {
			return false;
		} else {
			for (int i = 0; i < supportedDocFormat.length; i++) {
				if(path.endsWith(supportedDocFormat[i]))
					isValidDocFormat = true;
			}
			return isValidDocFormat;
		}
	}

	public static class HttpGetResponse {
		// public String requestHeaders;
		// public HashMap<String, String> headerParams;
		// public HttpMethods methodType = HttpMethods.GET;
		// public String httpVersion = "1.1";
		// public String UserAgent =
		// "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
		public String CRLF = "\r\n";
		public String body;
		public String statusCode;

		// public int contentLength = -1;
		// public String contentType;

		public HttpGetResponse(String response) {
			this.statusCode = "OK 123";
			this.body = "no body";
		}

		static byte[][] createResponse(String requestString) {
			// TODO Auto-generated method stub
			return null;
		}
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

		public String generateHeadRequestAsString(String path) {
			String res;
			if (path.equals(""))
				res = methodType + " " + "/" + " " + httpVersion + CRLF;
			else
				res = methodType + " " + path + " " + httpVersion + CRLF;
			res = res + "Host: " + this.domainHost + " " + CRLF;
			res = res + CRLF;
			return res;
		}
	}

}
