/**
 * HttpRequestException Class.
 * stores HTTP error codes
 * @author Sharon David, Tal Bigel
 *
 */
@SuppressWarnings("serial")
public class HttpRequestException extends Exception {

	public final static String HTTP_CODE_404_FILE_NOT_FOUND = "404 Not Found";
	public final static String HTTP_CODE_400_BAD_REQUEST = "400 Bad Request";
	public final static String HTTP_CODE_500_INTERNAL_ERROR = "500 Internal Server Error";
	public final static String HTTP_CODE_501_NOT_IMPLEMENTED = "501 Not Implemented";
	public final static String HTTP_CODE_UNKOWN_ERROR = "Unknown error code";
	public final static String HTTP_CODE_403_FORBIDDEN = "403 Forbidden"; // Bonus :)
	public final static String HTTP_CODE_401_UNAUTHORIZED = "401 Unauthorized"; // Bonus :)
	public final static String HTTP_CODE_405_NOT_ALLOWED = "405 Method Not Allowed"; //Bonus :)

	private String httpVersion;
	private int errCode;

	public HttpRequestException(String msg) {
		super(msg);
	}

	public HttpRequestException(int errCode) {	
		// set 1.0 as default
		this.httpVersion = "1.0";
		this.errCode = errCode;
	}
	
	public HttpRequestException(String version, int errCode) {
		if (version == null) {
			this.httpVersion = "1.0";
		} else {
			this.httpVersion = version;
		}
		this.errCode = errCode;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public String toString() {
		String errString = "";
		switch (this.errCode) {
		case 400: 
			errString = HTTP_CODE_400_BAD_REQUEST;
			break;
		case 404: 
			errString = HTTP_CODE_404_FILE_NOT_FOUND;
			break;
		case 500:
			errString = HTTP_CODE_500_INTERNAL_ERROR;
			break;
		case 501:
			errString = HTTP_CODE_501_NOT_IMPLEMENTED;
			break;
		case 403:
			errString = HTTP_CODE_403_FORBIDDEN;
			break;
		case 401:
			errString = HTTP_CODE_401_UNAUTHORIZED;
			break;
		case 405:
			errString = HTTP_CODE_405_NOT_ALLOWED;
			break;
		default:
			errString = HTTP_CODE_UNKOWN_ERROR;
			break;
		}

		String msg = "HTTP/" + this.httpVersion + " " + errString;
		return msg + HttpRequest.CRLF + HttpRequest.CRLF;
	}
}