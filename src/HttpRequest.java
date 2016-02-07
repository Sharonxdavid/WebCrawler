import java.util.HashMap;

/**
 * HttpRequest Class
 * 
 * @author user Sharon David,Tal Bigel
 * 
 */
public class HttpRequest {

	public String requestHeaders;
	public HashMap<String, String> headerParams;
	public HttpMethods methodType;
	public String httpVersion;
	public String requestedPageLocation;
	public String uriExtension;
	public String UserAgent;
	public int ContentLength = -1;
	public String Referer;
	public boolean isChunked = false;
	public final static String CRLF = "\r\n";
	/**
	 * Supported image types
	 */
	private static String[] supportedImageFormat = { "jpg", "png", "bmp", "gif" };

	/**
	 * Supported HTTP versions
	 */
	private static String[] supportedHTTPVersions = { "1.0", "1.1" };

	/**
	 * HTTP request constructor.
	 * 
	 * @param headers
	 *            - HTTP Request header as a string
	 * @throws HttpRequestException
	 *             if the header is empty or cannot be parsed
	 */
	public HttpRequest(String headers) throws HttpRequestException {
		if (headers == null || headers.isEmpty()) {
			throw new HttpRequestException(
					"Error: Given request has an illegal header");
		}
		this.requestHeaders = headers; // headers are ok
		parseRequestHeaders(this.requestHeaders);
	}

	/***
	 * Check if requested URI is an HTML file.
	 */
	public boolean isHtml() {
		if (this.uriExtension == null) {
			return false;
		}

		return this.uriExtension.equalsIgnoreCase("html")
				|| this.uriExtension.equalsIgnoreCase("htm");
	}

	/***
	 * Check if requested URI is a CSS file (bonus :))
	 */
	public boolean isCss() {
		if (this.uriExtension == null) {
			return false;
		}

		return this.uriExtension.equalsIgnoreCase("css");
	}

	/***
	 * Check if the requested URI has an icon extension
	 */
	public boolean isIcon() {
		if (this.uriExtension == null) {
			return false;
		}

		return this.uriExtension.equalsIgnoreCase("ico");
	}

	/***
	 * Checks if requested URI is an image
	 */
	public boolean isImage() {
		boolean isValidImgFormat = false;
		if (this.uriExtension == null) {
			return false;
		} else {
			for (int i = 0; i < supportedImageFormat.length; i++) {
				isValidImgFormat = isValidImgFormat
						|| this.uriExtension
								.equalsIgnoreCase(supportedImageFormat[i]);
			}
			return isValidImgFormat;
		}
	}

	/**
	 * Parse the request headers
	 * 
	 * @param headers
	 *            - the headers of the request
	 * @throws HttpRequestException
	 */
	private void parseRequestHeaders(String headers)
			throws HttpRequestException {
		String httpRequestHeaders[] = headers.split(HttpRequestHandler.CRLF);
		parseHeadersAsPairs(httpRequestHeaders);
		// Parse first line with the standart format:
		// HTTP_METHOD /FILE_PATH HTTP/HTTP_VERSION
		String[] firstLineArgs = httpRequestHeaders[0].split(" ");
		if (firstLineArgs.length != 3) {// not enough args. by the request
										// format.
			throw new HttpRequestException(400);
		}
		parseHTTPMethod(firstLineArgs[0]);
		parseURIPath(firstLineArgs[1]);
		parseHttpVersion(firstLineArgs[2]);
	}

	/**
	 * Parse HTTP request method.
	 * 
	 * @param method
	 * @throws HttpRequestException
	 *             if the given method is illegal
	 */
	private void parseHTTPMethod(String method) throws HttpRequestException {
		try {
			this.methodType = HttpMethods.parseHTTPMethod(method);
		} catch (InternalError ie) {
			throw new HttpRequestException(501); // case unsupported method.
		}
	}

	/**
	 * Parse headers into pairs. supported
	 * 
	 * @param requestHeadersArgs
	 */
	private void parseHeadersAsPairs(String[] requestHeadersArgs) {
		for (int i = 1; i < requestHeadersArgs.length; i++) {
			String[] headerArgs = requestHeadersArgs[i].split(": ");
			String key = headerArgs[0];
			String value = headerArgs[1];

			if (key.equalsIgnoreCase("content-length")) {
				this.ContentLength = Integer.parseInt(value);
			} else if (key.equalsIgnoreCase("referer")) {
				if (headerArgs.length == 2)
					this.Referer = value;
			} else if (key.equalsIgnoreCase("chunked")) {
				if (headerArgs.length == 2 && value != null)
					this.isChunked = (value.toLowerCase().contains("yes"));
			} else if (key.equalsIgnoreCase("user-agent")) {
				if (headerArgs.length == 2)
					this.UserAgent = value;
			}
		}
	}

	/**
	 * Check if the URI path is valid and parse.
	 * 
	 * @param path
	 * @throws HttpRequestException
	 */
	private void parseURIPath(String path) throws HttpRequestException {
		if (!path.startsWith("/")) {
			if ("*".equals(path) && this.methodType == HttpMethods.OPTIONS) {
				this.requestedPageLocation = "";
			}

			throw new HttpRequestException(400);
		}

		int parametersStartIndex = path.indexOf('?');
		String requestPath = path.substring(1, path.length());

		if (parametersStartIndex != -1) {
			// case request has params:
			requestPath = path.substring(0, parametersStartIndex);
			String parametersString = path.substring(parametersStartIndex + 1,
					path.length());

			// Stores params in hashMap
			HashMap<String, String> parametersHashMap = new HashMap<String, String>();

			// params separated by &
			String[] params = parametersString.split("&");

			// add parameters to hashmap.
			for (int i = 0; i < params.length; i++) {
				String[] paramAsPair = params[i].split("=");
				if (paramAsPair.length == 1 && params[i].indexOf("=") != -1)
					// the param. has no value,will be set to null.
					parametersHashMap.put(paramAsPair[0], null);
				else if (paramAsPair.length == 2) {
					if (!parametersHashMap.containsKey(paramAsPair[0])) {
						// add the param. only if hasn't been added yet. values
						// won't be override.
						parametersHashMap.put(paramAsPair[0], paramAsPair[1]);
					}
				} else {

					// param. is not in the format - ignore.
				}
			}

			this.headerParams = parametersHashMap;
		}
		this.requestedPageLocation = requestPath.replace('/', '\\');

		// Blocking users when trying to get out the root directory.
		if (this.requestedPageLocation.contains("..")) {
			throw new HttpRequestException(403);
		}

		// Check if there is a file extension , when "." the is not last char
		int fileExtentionLocation = requestPath.lastIndexOf(".");
		if ((fileExtentionLocation > 0) && (!requestPath.endsWith("."))) {
			this.uriExtension = requestPath.substring(
					fileExtentionLocation + 1, requestPath.length());
		}
	}

	/**
	 * Parse HTTP version from the first headers line
	 * 
	 * @param httpVersion
	 *            with standart format of- HTTP/version
	 * @throws HttpRequestException
	 */
	private void parseHttpVersion(String httpVersion)
			throws HttpRequestException {
		String args[] = httpVersion.split("/");

		if (args.length != 2) {
			throw new HttpRequestException(400);
		}

		if (!args[0].toUpperCase().equals("HTTP")) {
			throw new HttpRequestException(400);
		}

		boolean isSupportedHtmlVersion = false;
		for (int i = 0; i < supportedHTTPVersions.length; i++) {
			isSupportedHtmlVersion = isSupportedHtmlVersion
					|| args[1].equals(supportedHTTPVersions[i]);
		}
		if (!isSupportedHtmlVersion) {
			throw new HttpRequestException(400);
		}
		this.httpVersion = args[1];
	}
}
