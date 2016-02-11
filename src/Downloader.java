import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.ObjectInputStream.GetField;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

public class Downloader implements Runnable {

	private SynchronizedQueue<String> URLtoDownload;
	private SynchronizedQueue<AnalyzerQueueObject> HTMLtoAnalyze;
	private HttpGetRequest currentRequest;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

	HashMap<String, Statistics> domainMap;
	class1 c1object;

	int numOfLinks = 0;
	int linksSizeCounter = 0;
	int numOfImgs = 0;
	int imgSizeCounter = 0;

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

			System.out.println(Thread.currentThread().getName() + " "
					+ "before increment Downloader");
			c1object.increment();
			System.out.println(Thread.currentThread().getName() + " "
					+ Msgs.printMsg(Msgs.DOWNLOADER_INFO, currURL));
			String result;

			try {
				result = java.net.URLDecoder.decode(currURL, "UTF-8");
				System.out.println(Thread.currentThread().getName() + " "
						+ "URL afetr decode is: " + result);
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
				// if (domainHost.contains(":")) {
				// String[] temp = domainHost.split(":");
				// domainHost = temp[0];
				// crawlPort = Integer.valueOf(temp[1]);
				// }

				System.out.println("************" + "url host is " + domainHost
						+ "port " + crawlPort + " path is" + relativePath);
				System.out.println(Thread.currentThread().getName() + " "
						+ "Domain Host is: " + domainHost);
				System.out.println(Thread.currentThread().getName() + " "
						+ "Relative path is: " + relativePath);
				if (isImage(relativePath) || isVideo(relativePath)
						|| isDoc(relativePath)) {
					System.out.println(Thread.currentThread().getName() + " "
							+ "media file");
					HttpHeadRequest currentHeadRequest = new HttpHeadRequest(
							domainHost);
					String reqAsString = currentHeadRequest
							.generateHeadRequestAsString(relativePath);
					System.out.println(Thread.currentThread().getName() + " "
							+ "----This is HEAD Request----");
					System.out.println(Thread.currentThread().getName() + " "
							+ reqAsString);
					Socket socket = new Socket();
					socket.connect(
							new InetSocketAddress(domainHost, crawlPort), 1000);
					while (socket.isConnected() == false) {
						// TODO: enter timeout to make sure it quits sometime...
					}

					BufferedReader rd = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));

					DataOutputStream outputStream = new DataOutputStream(
							socket.getOutputStream());
					long startTime = new Date().getTime();
					outputStream.write(reqAsString.getBytes());
					outputStream.flush();
					while (rd.ready() == false) {

					}
					long endTime = new Date().getTime();
					long diff = endTime - startTime;
					System.out.println("TIME DIFFERENCE "
							+ (endTime - startTime));
					domainMap.get(c1object.initialDomain).rtt.add(diff);

					// ByteStringInputStream rd = new
					// ByteStringInputStream(socket.getInputStream());
					String line;
					String urlSrcToAnalyze = "";
					System.out.println(Thread.currentThread().getName() + " "
							+ "before read response");
					try {
						socket.setSoTimeout(5000);
						while ((line = rd.readLine()) != null) {
							// while ((line = rd.readOneLine()) != null) {
							System.out.println(Thread.currentThread().getName()
									+ " " + line);
							if (line.isEmpty())
								break;
							urlSrcToAnalyze += line + "\r\n";
							// urlSrcToAnalyze += line;
						}
						socket.close();
						System.out.println(Thread.currentThread().getName()
								+ " " + new Date() + " Enqueue Analyzer1");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost, crawlPort, urlSrcToAnalyze,
								new Date(), 0));
					} catch (SocketTimeoutException e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "Socket time out! (Expected:)");
						System.out.println(Thread.currentThread().getName()
								+ " " + new Date() + " Enqueue Analyzer2");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost, crawlPort, urlSrcToAnalyze,
								new Date(), 0));
					} catch (SocketException e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "Socket expcetion in here");
						e.getStackTrace();
					} catch (Exception e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "failed to download " + result);
						e.printStackTrace();
					}
				} else {
					this.currentRequest = new HttpGetRequest(domainHost);
					String reqAsString = currentRequest
							.generateGetRequestAsString(relativePath);
					System.out.println(Thread.currentThread().getName() + " "
							+ "----This is Get Request----");
					System.out.println(Thread.currentThread().getName() + " "
							+ reqAsString);

					Socket socket = new Socket();
					socket.connect(
							new InetSocketAddress(domainHost, crawlPort), 1000);
					while (socket.isConnected() == false) {
						// TODO: enter timeout to make sure it quits sometime...
					}

					BufferedReader rd = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));

					DataOutputStream outputStream = new DataOutputStream(
							socket.getOutputStream());
					outputStream.write(reqAsString.getBytes());
					outputStream.flush();

					long startTime = new Date().getTime();
					outputStream.write(reqAsString.getBytes());
					outputStream.flush();
					while (rd.ready() == false) {

					}
					long endTime = new Date().getTime();
					System.out.println("TIME DIFFERENCE "
							+ (endTime - startTime));
					long diff = endTime - startTime;
					domainMap.get(c1object.initialDomain).rtt.add(diff);
					// while (rd.ready() == false) {}

					String line = null;
					int readResult = -1;
					String urlSrcToAnalyze = "";
					String headersToAnalyze = "";
					StringBuilder sb = new StringBuilder();
					System.out.println(Thread.currentThread().getName() + " "
							+ "before read response");
					int lengthToRead = -1;
					int readingCounter = 0;
					boolean isChunked = false;
					int totalChunk = 0;
					try {
						socket.setSoTimeout(5000);
						while ((line = rd.readLine()) != null) {
							System.out.println("READING RESPONSE");
							// while ((line = rd.readOneLine()) != null) {
							System.out.println(Thread.currentThread().getName()
									+ " " + line);
							if (line.isEmpty())
								break;
							String[] args = line.split(": ");
							if (args.length > 1) {
								if (args[0].equalsIgnoreCase("content-length")) {
									lengthToRead = Integer.parseInt(args[1]);
								} else if (args[0]
										.equalsIgnoreCase("Transfer-Encoding")) {
									System.out.println(args[1].length() + " "
											+ args[1]);
									if (args[1].equalsIgnoreCase("chunked")) {
										isChunked = true;
									}
								}
							}
							headersToAnalyze += line + "\r\n";
							// urlSrcToAnalyze += line;

						}
						if (isChunked) {
							line = rd.readLine();
							System.out.println("if chunked this is line: "
									+ line);
							lengthToRead = Integer.parseInt(line, 16);
							totalChunk+=lengthToRead;
							System.out
									.println("after changing from hex to int "
											+ lengthToRead);
							sb.append(line + "\r\n");
						}

						while (readingCounter < lengthToRead) {
							readResult = rd.read();
							if (readResult == -1) {
								break;
							} else {
								sb.append(Character.toString((char) readResult));
								readingCounter++;
							}
							if (isChunked && readingCounter == lengthToRead) {
								readingCounter = 0;
								rd.read(); // '\r'
								rd.read(); // '\n'
								line = rd.readLine();
								System.out
										.println("LOOP CHUNKED COUNTER UPDATE "
												+ line);
								lengthToRead = Integer.parseInt(line, 16);
								totalChunk+=lengthToRead;
								sb.append("\r\n" + line + "\r\n");
							}
						}
						System.out.println("this is sb " + sb.toString()
								+ " sb length is " + sb.toString().length());
						System.out.println("after sb");
						urlSrcToAnalyze = headersToAnalyze + "\r\n"
								+ sb.toString();
						socket.close();
						System.out.println(Thread.currentThread().getName()
								+ " " + new Date() + " Enqueue Analyzer1");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost, crawlPort, urlSrcToAnalyze,
								new Date(), totalChunk));
					} catch (SocketTimeoutException e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "Socket time out! (Expected:)");
						System.out.println(Thread.currentThread().getName()
								+ " " + new Date() + " Enqueue Analyzer2");
						HTMLtoAnalyze.enqueue(new AnalyzerQueueObject(result,
								domainHost, crawlPort, urlSrcToAnalyze,
								new Date(),  totalChunk));
					} catch (SocketException e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "Socket Exception");
						e.printStackTrace();
					} catch (Exception e) {
						System.out.println(Thread.currentThread().getName()
								+ " " + "failed to download " + result);
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				System.out.println(Thread.currentThread().getName()
						+ " Exception!!!");
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " "
					+ "decrement downloader");
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

		return path.equalsIgnoreCase("html") || path.equalsIgnoreCase("htm")
				|| path.equals("");
	}

	/***
	 * Checks if requested URI is an image
	 */
	public boolean isImage(String path) {
		boolean isValidImgFormat = false;
		if (path == null) {
			return false;
		} else {
			ArrayList<String> temp = c1object.getImagesExtensions();
			for (String type : temp) {
				if (path.endsWith(type))
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
			ArrayList<String> temp = c1object.getImagesExtensions();
			for (String type : temp) {
				if (path.endsWith(type))
					return true;
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
			ArrayList<String> temp = c1object.getImagesExtensions();
			for (String type : temp) {
				if (path.endsWith(type))
					return true;
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
