import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
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
	
	private static String[] supportedVideoFormat = {"mov", "flv", "swf", "mkv", "avi", "mpg", "mp4", "wmv"};
	private static String[] supportedImageFormat = { "jpg", "png", "bmp", "gif" };
	private static String[] supportedDocFormat = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};

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
		System.out.println(new Date() + "Download starts");
		String currURL;

		while ((currURL = URLtoDownload.dequeue()) != null) {
			System.out.println("before increment Downloader");
			c1object.increment();
			// get page
			// TODO: insert result to HTML queue
			System.out.println(Msgs.printMsg(Msgs.DOWNLOADER_INFO, currURL));
			String result;

			try {
				result = java.net.URLDecoder.decode(currURL, "UTF-8");
				System.out.println("URL afetr decode is: " + result);
//				if(result.startsWith("https")){
//					System.out.println("HTTPS is unsupported.");
//					continue;
//				}
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
				System.out.println("Domain Host is: " + domainHost);
				System.out.println("Relative path is: " + relativePath);
				// if(isHtml(relativePath)){
				// generate GET request
				// }
				// else{
				// if(isImg(relativePath) || isVideo(relativePath))
				// TODO: create HEAD request
				// }
				this.currentRequest = new HttpGetRequest(domainHost);
				String reqAsString = HttpGetRequest.generateGetRequestAsString(
						currentRequest, relativePath);
				System.out.println("----This is Get Request----");
				System.out.println(reqAsString);

				socket = new Socket(levels[0], 80);

				this.outputStream = new DataOutputStream(
						socket.getOutputStream());
				System.out.println("Downloader Port is " + socket.getPort());
				outputStream.write(reqAsString.getBytes());

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line;
				String urlSrcToAnalyze = "";
				System.out.println("before read response");
				try {
					socket.setSoTimeout(5000);
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
						urlSrcToAnalyze += line;
					}
					socket.close();
				} catch (SocketTimeoutException e) {
					System.err.println("Socket time out! (Expected:)");
				}
				catch (Exception e){
					System.err.println("failed to download " + result);
				}
				System.out.println(new Date() + "Enqueue Analyzer");
				HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
						domainHost, urlSrcToAnalyze, new Date()));
				System.out.println("befor decrement Downloader");
				c1object.decrement();


			} catch (UnsupportedEncodingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Check if requested URI is an HTML file.
	 */
	public boolean isHtml(String path) {
		if (path == null) {
			return false;
		}

		return path.equalsIgnoreCase("html")
				|| path.equalsIgnoreCase("htm");
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
				isValidImgFormat = isValidImgFormat
						|| path
								.equalsIgnoreCase(supportedImageFormat[i]);
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
				isValidVideoFormat = isValidVideoFormat
						|| path
								.equalsIgnoreCase(supportedVideoFormat[i]);
			}
			return isValidVideoFormat;
		}
	}
	/***
	 * Checks if requested URI is a document
	 */
	public boolean isDoc(String path){
		boolean isValidDocFormat = false;
		if (path == null) {
			return false;
		} else {
			for (int i = 0; i < supportedDocFormat.length; i++) {
				isValidDocFormat = isValidDocFormat
						|| path
								.equalsIgnoreCase(supportedDocFormat[i]);
			}
			return isValidDocFormat;
		}
	}
	
	public static class HttpGetRequest {
		public String requestHeaders;
		public HashMap<String, String> headerParams;
		public static HttpMethods methodType = HttpMethods.GET;
		public static String httpVersion = "HTTP/1.1";
		public String UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
		public static String CRLF = "\r\n";
		public static String domainHost;

		public HttpGetRequest(String host) {
			this.domainHost = host;
		}

		public static String generateGetRequestAsString(HttpGetRequest req,
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
