import java.util.HashMap;

public class HttpGetRequest {
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

	public String generateGetRequestAsString(String path) {
		String res;
		if (path.equals("")){
			res = methodType + " " + "/" + " " + httpVersion + CRLF;
		}else
			res = methodType + " " + path + " " + httpVersion + CRLF;
		res = res + "Host: " + this.domainHost + " " + CRLF;
		res = res + CRLF;
		return res;
	}

}