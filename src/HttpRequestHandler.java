import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * HttpRequestHandler class
 * 
 * @author Sharon David, Tal Bigel
 * 
 */
public class HttpRequestHandler implements Runnable {

	private Socket socket;
	public static String defaultPage;
	private HttpRequest currentRequest;
	private DataOutputStream outputStream;
	public static String rootDir;
	private static int threadCounter = 0;
	private static final Object threadCounterLock = new Object();
	public final static String serverSubmitResponseFileName = "params_info.html";
	public final static String CRLF = "\r\n";
	public final static String OK_MESSAGE = "HTTP/1.1 200 OK";
	private HashMap<String, String> parametersHashMap;

	SynchronizedQueue<String> downloaderQueue;
	SynchronizedQueue<AnalyzerQueueObject> analyzerQueue;
	SynchronizedQueue<Integer> ports;

	HashMap<String, Statistics> domainMap;

	class1 c1object;

	/**
	 * Class constructor
	 * 
	 * @param socket
	 * @param rootDir
	 * @param defaultPage
	 * @param downloaderQueue
	 * @param domainMap
	 * @param c1object
	 * @param ports
	 * @param analyzerQueue2
	 */
	public HttpRequestHandler(Socket socket, String rootDir,
			String defaultPage, SynchronizedQueue<String> downloaderQueue,
			SynchronizedQueue<AnalyzerQueueObject> analyzerQueue,
			HashMap<String, Statistics> domainMap, class1 c1object,
			SynchronizedQueue<Integer> ports) {
		parametersHashMap = new HashMap<String, String>();
		this.socket = socket;
		HttpRequestHandler.defaultPage = defaultPage;
		HttpRequestHandler.rootDir = rootDir;
		this.downloaderQueue = downloaderQueue;
		this.analyzerQueue = analyzerQueue;
		this.domainMap = domainMap;
		downloaderQueue.registerProducer();
		this.c1object = c1object;
		this.ports = ports;
	}

	// Getters
	public static int getThreadCounter() {
		return threadCounter;
	}

	public static Object getLock() {
		return threadCounterLock;
	}

	/***
	 * Increasing the thread counter
	 */
	public static void increaseThreadCounter() {
		synchronized (threadCounterLock) {
			threadCounter++;
		}
	}

	/***
	 * Decreasing the thread counter
	 */
	public static void decreaseThreadCounter() {
		synchronized (threadCounterLock) {
			threadCounter--;
		}
	}

	/**
	 * When sending in chunks
	 * 
	 * @param outputStream
	 * @param responseData
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void writeChunked(DataOutputStream outputStream,
			byte[][] responseData) throws NumberFormatException, IOException {
		int chunkSize = Integer.parseInt("400", 16);// setting chunk size to be
		// 1024- 400 in hex base.
		int bytesCounter = 0;
		byte[] bytesToWriteArray;
		int bytesLeftToWrite = responseData[1].length - bytesCounter;

		while (bytesLeftToWrite > 0) {
			if (responseData[1].length - bytesCounter <= chunkSize) {
				bytesToWriteArray = new byte[bytesLeftToWrite];
				System.arraycopy(responseData[1], bytesCounter,
						bytesToWriteArray, 0, bytesLeftToWrite);
				bytesCounter += bytesLeftToWrite;
			} else {
				bytesToWriteArray = new byte[chunkSize];
				System.arraycopy(responseData[1], bytesCounter,
						bytesToWriteArray, 0, chunkSize);
				bytesCounter += chunkSize; // update counter after setting
				// another chunk
			}
			// write chunk to stream
			outputStream
					.write((Integer.toHexString(bytesToWriteArray.length) + CRLF)
							.getBytes());
			outputStream.write(bytesToWriteArray);
			outputStream.write(CRLF.getBytes());

			bytesLeftToWrite = responseData[1].length - bytesCounter;
		}
		outputStream.write((Integer.toHexString(0)).getBytes());
		outputStream.write(CRLF.getBytes());
	}

	/***
	 * Reads HTTP request header without body, and prints it when reading the
	 * header.
	 * 
	 * @param buffer
	 * @return the request as a string
	 * @throws IOException
	 * @throws HttpRequestException
	 */
	private String readRequest(BufferedReader buffer) throws IOException,
			HttpRequestException {

		StringBuilder requestStringFormat = new StringBuilder();
		String currLine;

		// read headers until no more left to read
		while ((currLine = buffer.readLine()).length() != 0) {
			requestStringFormat.append(currLine + CRLF);
			System.out.println(currLine);
		}
		requestStringFormat.append(CRLF);// end of request.

		try {
			this.currentRequest = new HttpRequest(
					requestStringFormat.toString());
		} catch (HttpRequestException e) {
			throw new HttpRequestException(400);
		}

		// read params
		if (currentRequest.ContentLength > 0) {
			char[] readParameters = new char[currentRequest.ContentLength];
			buffer.read(readParameters);
			requestStringFormat.append(String.valueOf(readParameters)); // add
			// params
			// to
			// request
		}

		return requestStringFormat.toString();
	}

	/**
	 * Create HTTP response following the HTTP request
	 * 
	 * @param requestString
	 * @return HTTP byte[][] response
	 * @throws Exception
	 */
	private byte[][] createResponse(String requestString) throws Exception {
		byte[][] httpResponse = new byte[2][];
		try {
			if (requestString.indexOf(CRLF + CRLF) == -1) {
				throw new HttpRequestException(400);
			}
			int bodyStartIndex = requestString.indexOf(CRLF + CRLF) + 2 * 2;
			byte[] pageContent;
			// create response according to request method.
			// System.out.println("CURR REQUEST");
			// System.out.println(currentRequest.requestedPageLocation);
			// System.out.println(currentRequest.Referer);
			// System.out.println(currentRequest.methodType);
			// System.out.println("ISDONE" + c1object.isDone(downloaderQueue,
			// analyzerQueue));
			if (currentRequest.methodType == HttpMethods.POST) {
				System.out.println("** handle post ! **");
				pageContent = handlePostRequest(requestString, bodyStartIndex);
			} else if (this.currentRequest.methodType == HttpMethods.GET) {
				if (currentRequest.requestedPageLocation
						.equals("crawlform.html")) {
					boolean isDone = c1object.isDone(downloaderQueue,
							analyzerQueue);
					pageContent = handleGetCrawlFormRequest(requestString,
							isDone);

				} else if (this.currentRequest.requestedPageLocation
						.equals("oldStats.html")) {
					pageContent = getOldStats();
				}
				// if (currentRequest.requestedPageLocation.equals("\\"
				// + "stats.html")) {
				// pageContent = handleGetRequest(requestString);
				// }
				else if (currentRequest.requestedPageLocation.equals("\\"
						+ serverSubmitResponseFileName)) {
					pageContent = handleGetRequest(requestString);
				} else {
					pageContent = isValidFilePath(currentRequest);
				}
			} else if (this.currentRequest.methodType == HttpMethods.TRACE) {
				pageContent = (this.currentRequest.requestHeaders.toString())
						.getBytes();
			} else {
				pageContent = new byte[0];
			}

			// building response header.
			StringBuilder responseHeader = new StringBuilder();
			responseHeader.append(String.format(OK_MESSAGE,
					currentRequest.httpVersion) + CRLF);

			if (currentRequest.isImage()) {
				responseHeader.append("content-type: image" + CRLF);
			} else if (currentRequest.isIcon()) {
				responseHeader.append("content-type: image/x-icon" + CRLF);
			} else if (currentRequest.isHtml()) {
				responseHeader.append("content-type: text/html" + CRLF);
			} else if (currentRequest.isCss()) {
				responseHeader.append("content-type: text/css" + CRLF);
			} else {
				responseHeader.append("content-type: application/octet-stream"
						+ CRLF);
			}

			if (currentRequest.isChunked) {
				responseHeader.append("transfer-encoding: chunked" + CRLF);
			} else {
				responseHeader.append("content-length: " + pageContent.length
						+ CRLF);
			}

			if (currentRequest.methodType == HttpMethods.OPTIONS) {
				responseHeader.append("Allow: "
						+ HttpMethods.getMethodsString());
			}

			// CRLF to indicate requestHeaders section is over.
			responseHeader.append(CRLF);
			httpResponse[0] = responseHeader.toString().getBytes();
			httpResponse[1] = pageContent;

			return httpResponse;
		} catch (HttpRequestException exception) {
			httpResponse[0] = exception.toString().getBytes();
			return httpResponse;
		} catch (Exception exception) {
			exception.printStackTrace();
			System.out.println("272!!!");
			HttpRequestException httpRequestException = new HttpRequestException(
					500);
			httpResponse[0] = httpRequestException.toString().getBytes();
			return httpResponse;
		}
	}

	private byte[] getOldStats() throws IOException {
		File file = new File(rootDir + "oldStats.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		getOldStatsResponseHTMLBody(bw);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] responseBytes = new byte[(int) file.length()];

		// read until the end of the stream.
		while (fileInputStream.available() != 0) {
			fileInputStream.read(responseBytes, 0, responseBytes.length);
		}
		fileInputStream.close();
		return responseBytes;
	}

	private void getOldStatsResponseHTMLBody(BufferedWriter bw) throws IOException {
		bw.write("<!DOCTYPE html>");

		bw.write("<head>");
		// bw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
		bw.write("</head>");
		bw.write("<body bgcolor='#A9D0F5'>");

		bw.write("<br><br>");

		oldStatsLinks(bw);
		
		
		bw.write("</div>");
		bw.write("</body>");
		bw.write("</html>");
		bw.close();
	}
	private void oldStatsLinks(BufferedWriter bw) throws IOException {
		bw.write("Previoulsy scanned:");
		bw.write("<table border = \"1\"");
		bw.write("<tr>");
		bw.write("</tr>");
		for (Entry<String, Statistics> entry : domainMap.entrySet()) {
			bw.write("<tr> <td>");
			bw.write(entry.getKey());
			bw.write("</td> <td>");
			// bw.write("<a href = " + "\"" + c1object.rootPath + entry.getKey()
			// + "_" + c1object.startCrawlDateTime
			// + ".html\">Link to page");
			bw.write("<a href = " + "\"/" + entry.getKey() + "_"
					+ domainMap.get(entry.getKey()).map.get("Start time") + ".html\">Link to page");
			bw.write("</td> </tr>");
		}
		bw.write("</table>");

	}

	private byte[] handleGetCrawlFormRequest(String requestString,
			boolean isDone) throws IOException {
		File file = new File(rootDir + "/crawlform.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		getCrawlFormResponseHTMLBody(bw, isDone);

		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] responseBytes = new byte[(int) file.length()];

		// read until the end of the stream.
		while (fileInputStream.available() != 0) {
			fileInputStream.read(responseBytes, 0, responseBytes.length);
		}
		fileInputStream.close();
		return responseBytes;
	}

	private void getCrawlFormResponseHTMLBody(BufferedWriter bw, boolean isDone)
			throws IOException {

		bw.write("<!DOCTYPE html>");
		bw.write("<body>");
		
		if (isDone) {
			bw.write("<form id=\"form\" method = \"POST\" action = \"execResult.html\">");
			bw.write("Domain:<br>");
			bw.write("<input type=\"text\" name=\"Domain\">");
			bw.write("<br>");

			bw.write("<input type=\"checkbox\" name=\"portscan\" >Perform full TCP port scan<br>");
			bw.write("<input type=\"checkbox\" name=\"robotscan\" checked=\"on\">Disrespect robots.txt<br>");
			bw.write("<input type=\"submit\" value=\"Start crawler\">");
			bw.write("</form>");
		} else {
			bw.write("Crawler already running...<br>");
			bw.write("currently scanning " + c1object.initialDomain);
		}

		bw.write("</body>");
		bw.write("</html>");

		bw.close();
	}

	/**
	 * Handle POST request
	 * 
	 * @param requestString
	 * @param bodyStartIndex
	 * @return
	 * @throws IOException
	 * @throws HttpRequestException
	 */
	private byte[] handlePostRequest(String requestString, int bodyStartIndex)
			throws IOException, HttpRequestException {
		byte[] pageContent = null;
		String requestBody = null;
		if (currentRequest.ContentLength > 0) {
			requestBody = requestString.substring(bodyStartIndex,
					bodyStartIndex + currentRequest.ContentLength);
		}
		if (this.currentRequest.requestedPageLocation
				.equalsIgnoreCase("execResult.html")) {
			try {
				parseParamsFromString(requestBody);

				String crawlUrl = parametersHashMap.get("Domain");

				// both strings are null or "on"
				String robotScan = parametersHashMap.get("robotscan");
				String portScan = parametersHashMap.get("portscan");

				String tempCrawlUrl = "";
				int crawlPort = 80;
				tempCrawlUrl = java.net.URLDecoder.decode(crawlUrl, "UTF-8");
				try {
					InetAddress.getByName(tempCrawlUrl);
				} catch (UnknownHostException e) {
					System.out.println("Unknwon host!");
					throw new UnknownHostException();
				}
				if (!domainMap.containsKey(tempCrawlUrl)) {

					c1object.setInitialDomain(tempCrawlUrl);
					
					c1object.initDateTime();
					// if(tempCrawlUrl.contains(":")){
					// String[] temp = tempCrawlUrl.split(":");
					// crawlUrl = temp[0];
					// crawlPort = Integer.valueOf(temp[1]);
					// }
					domainMap.put(tempCrawlUrl, new Statistics());
					domainMap.get(tempCrawlUrl).map.put("Domain Name",
							tempCrawlUrl);
					domainMap.get(tempCrawlUrl).map.put("Start time",
							c1object.startCrawlDateTime);
					domainMap.get(tempCrawlUrl).visitedURL.add(tempCrawlUrl);

					if (robotScan == null || robotScan.equals("off")) {
						RobotRules rr = new RobotRules();
						rr.loadRobotsTxt(tempCrawlUrl, 80);
						System.out.println("PRINTING RR ALLOW LIST "
								+ rr.allowed);
						System.out.println("PRINTING RR DISALLOW LIST "
								+ rr.disallowed);
						c1object.respectRobots = false;
					}

					if (portScan != null && portScan.equals("on")) {
						System.out.println("Starting Port Scan Stub");
						c1object.portScan = true;
						for (int i = 1; i < 1025; i++) {
							this.ports.enqueue(i);
						}
						PortScanThread scan = new PortScanThread(
								downloaderQueue, analyzerQueue, domainMap,
								domainMap.get(tempCrawlUrl).map
										.get("Domain Name"), this.ports,
								c1object);
						scan.start();
					} else {
						c1object.portScan = false;
						downloaderQueue.enqueue(c1object.initialDomain);
						ExecResListener execRes = new ExecResListener(
								this.downloaderQueue, this.analyzerQueue,
								domainMap,
								domainMap.get(c1object.initialDomain).map
										.get("Domain Name"), c1object, ports);

						Thread execResThread = new Thread(execRes);
						execResThread.start();
					}
					System.out.println("IS DONE PORT SCAN"
							+ c1object.isDonePortScan(ports));
					System.out.println("IS DONE PORT SCAN"
							+ c1object.isDonePortScan(ports));
					System.out.println("IS DONE PORT SCAN"
							+ c1object.isDonePortScan(ports));
					System.out.println("IS DONE PORT SCAN"
							+ c1object.isDonePortScan(ports));

					pageContent = createStatsPostResponseHTML(tempCrawlUrl,
							true);
				} else {
					System.out.println("Already crawled domain");
					pageContent = createStatsPostResponseHTML(tempCrawlUrl,
							true);
				}
			} catch (Exception e) {
				pageContent = createStatsPostResponseHTML(e.getMessage(), false);
				e.printStackTrace();
			}
		} else {
			if (this.currentRequest.requestedPageLocation
					.equalsIgnoreCase(serverSubmitResponseFileName)) {
				parseParamsFromString(requestBody);
				System.out.println("@@@@" + parametersHashMap.toString());
				pageContent = createPostResponseHTML();
			} else {
				pageContent = isValidFilePath(currentRequest);
			}
		}
		return pageContent;
	}

	private byte[] createStatsPostResponseHTML(String domain, boolean isDone)
			throws IOException {
		File file = new File(rootDir + "/execResult.tmp.html");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		postResponseStatsHTMLBody(bw, domain, isDone);

		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] responseBytes = new byte[(int) file.length()];

		// read until the end of the stream.
		while (fileInputStream.available() != 0) {
			fileInputStream.read(responseBytes, 0, responseBytes.length);
		}
		fileInputStream.close();
		return responseBytes;
	}

	private void postResponseStatsHTMLBody(BufferedWriter bw, String domain,
			boolean isDone) throws IOException {
		bw.write("<!DOCTYPE html>");
		bw.write("<body bgcolor='#A9D0F5'>");

		// param3 (started) true:
		if (isDone) {
			bw.write("Crawler Started successfully!<br>");
			bw.write("Domain: " + domain + "<br>");
		} else {// param3 started false
			bw.write("Crawler Failed to start because:<br>");
			bw.write("e.getMessage() is: " + domain);
		}
		bw.write("<br><br>");

		oldStatsLinks(bw);
		
		bw.write("</body>");
		bw.write("</html>");
		bw.close();

	}

	/**
	 * Handle GET request
	 * 
	 * @param requestString
	 * @return
	 * @throws IOException
	 */
	private byte[] handleGetRequest(String requestString) throws IOException {
		byte[] pageContent;
		int firstLineStartIndex = requestString.indexOf("?");
		int firstLineEndIndex = requestString.substring(firstLineStartIndex)
				.indexOf(" ");
		String params = requestString.substring(1 + firstLineStartIndex,
				firstLineStartIndex + firstLineEndIndex);
		parseParamsFromString(params);
		pageContent = createPostResponseHTML();
		return pageContent;
	}

	/**
	 * Parse params given from HTTP request with POST/GET method and return as
	 * string.
	 * 
	 * @param requestBody
	 *            - string of the body request.
	 */
	private void parseParamsFromString(String requestBody) {
		// params. separeted by '&'
		String[] params = requestBody.split("&");
		for (String param : params) {
			String[] paramKeyValue = param.split("=");
			if (paramKeyValue.length == 1 && param.indexOf("=") != -1)// param
				// has
				// no
				// value
				parametersHashMap.put(paramKeyValue[0], "NO VALUE SENT");
			else if (paramKeyValue.length == 2) {
				parametersHashMap.put(paramKeyValue[0], paramKeyValue[1]); // store
				// all
				// params
				// in
				// params
				// map.
			} else {
				// params isn't in format - ignore.
			}
		}
	}

	/**
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	private byte[] createPostResponseHTML() throws IOException {
		File file = new File(rootDir + serverSubmitResponseFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		postResponseHTMLBody(bw);

		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] responseBytes = new byte[(int) file.length()];

		// read until the end of the stream.
		while (fileInputStream.available() != 0) {
			fileInputStream.read(responseBytes, 0, responseBytes.length);
		}
		fileInputStream.close();
		return responseBytes;
	}

	private void postResponseHTMLBody(BufferedWriter bw) throws IOException {
		bw.write("<!DOCTYPE html>");

		bw.write("<head>");
		bw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/style.css\">");
		bw.write("</head>");
		bw.write("<body bgcolor='#A9D0F5'>");
		bw.write("<iframe src=\"http://free.timeanddate.com/clock/i4za5yzk/n676/szw110/szh110/hbw0/hfc111/cf100/hgr0/fav0/fiv0/mqcfff/mql15/mqw4/mqd94/mhcfff/mhl15/mhw4/mhd94/hhcbbb/hmcddd/hsceee\" frameborder=\"0\" width=\"110\" height=\"110\" align:\"right\"></iframe>");
		bw.write("<div id=\"titleDiv\">");
		bw.write("<h2>Computer networks 2015/2016</h2>");
		bw.write("<h3> Tal Bigel, Sharon David </h3>");
		bw.write("</div>");
		bw.write("<br>");
		bw.write("<div id=\"valuesDiv\">");
		bw.write("<h3>Values sent to the server</h3>");
		bw.write("<br>");

		bw.write("<table border = \"1\" class=\"_table\">");
		bw.write("<tr>");

		bw.write("<th>");
		bw.write("Parameter Key");
		bw.write("</th>");

		bw.write("<th>");
		bw.write("Parameter Value");
		bw.write("</th>");

		bw.write("</tr>");

		for (Entry<String, String> entry : parametersHashMap.entrySet()) {
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
		bw.write("<a href='/index.html' class=\"link\">Back to index</a><br>");
		bw.write("<a href='/form.html' class=\"link\">Back to form</a>");
		bw.write("</body>");
		bw.write("</html>");
		bw.close();
	}

	/***
	 * Process HTTP request.
	 */
	public void run() {
		try {
			System.out.println("Handler Port is " + socket.getPort());
			this.outputStream = new DataOutputStream(socket.getOutputStream());
			InputStream inputStream = socket.getInputStream();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					inputStream));
			String requestString = readRequest(buffer);

			if (requestString == null || requestString.isEmpty()) {
				return; // no request to process.
			}
			System.out.println("REQUEST STRING " + requestString);
			byte[][] response = createResponse(requestString);
			System.out.print(new String(response[0])); // print request as
			// required.

			// headers
			outputStream.write(response[0]);

			if (response[1] != null && response[1].length > 0
					&& currentRequest.methodType != HttpMethods.HEAD
					&& currentRequest.methodType != HttpMethods.OPTIONS) {
				if (currentRequest.isChunked) {
					writeChunked(outputStream, response);

				} else {
					outputStream.write(response[1]); // not chunked, write all
					// at once.
				}
			}
		} catch (HttpRequestException e) {
			try {
				outputStream.writeBytes(e.toString());
			} catch (IOException e2) {
				e2.printStackTrace();
				System.out
						.println(" 6 thrown, with message: " + e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" 7 thrown, with message: " + e.getMessage());
		} finally {
			try {
				decreaseThreadCounter(); // thread has finished
				socket.close();
				downloaderQueue.unregisterProducer();
			} catch (IOException e) {
				e.printStackTrace();
				System.out
						.println(HttpRequestException.HTTP_CODE_500_INTERNAL_ERROR);
			}
		}
	}

	/**
	 * Validate that the file exist, if it's a folder read default page.
	 * 
	 * @param request
	 * @return the page in bytes
	 * @throws HttpRequestException
	 */
	@SuppressWarnings("resource")
	private byte[] isValidFilePath(HttpRequest request)
			throws HttpRequestException {
		try {
			String fileName = rootDir + request.requestedPageLocation;
			File file = new File(fileName);

			if (file.isDirectory()) {
				if (fileName.endsWith(WebServer.directorySeperator)) {
					fileName += defaultPage;
				} else {
					fileName += WebServer.directorySeperator + defaultPage;
				}
				request.uriExtension = "html";
				file = new File(fileName);
			}
			// case file does'nt exist, a 404 exception is thrown
			if (!file.exists()) {
				throw new HttpRequestException(404);
			}

			FileInputStream inputFile;
			inputFile = new FileInputStream(file);
			byte[] fileData = new byte[(int) file.length()];

			while (inputFile.available() != 0) {
				inputFile.read(fileData, 0, fileData.length);
			}

			return fileData;
		} catch (FileNotFoundException e) {
			throw new HttpRequestException(404);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("693!!!");
			throw new HttpRequestException(500);
		} catch (Exception e) {
			throw new HttpRequestException(404);
		}
	}

}
