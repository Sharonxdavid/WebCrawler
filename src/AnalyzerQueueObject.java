import java.text.SimpleDateFormat;
import java.util.Date;


	public class AnalyzerQueueObject {
		String httpVersion;
    	String statusCode;
    	String statusMsg;
    	
		String httpResponseAsString;
		String url;
		String host;
		Date downloadDate;
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		int ContentLength;
		String Referer;
		boolean isChunked;
		String UserAgent;
		String ContentType;
	    
	    private static String[] supportedHTTPVersions = { "1.0", "1.1" };
		
	    public AnalyzerQueueObject(String url, String host, String body, Date date) throws HttpRequestException{
	    	this.httpResponseAsString = body;
	    	this.url = url;
	    	this.host = host;
	    	this.downloadDate = date;
	    	parseStatus(body);
	    }
	    
	    private void parseStatus(String reqAsString) throws HttpRequestException{
	    	String temp = reqAsString;
	    	String[] reqAsLines = temp.split("\r\n");
	    	String firstLine = reqAsLines[0];
	    	String[] firstLineArgs = firstLine.split(" ");
	    	//parse HTTP Version (1.0 or 1.1)
	    	String[] versionArgs = firstLineArgs[0].split("/");
			if (versionArgs.length != 2) {
				throw new HttpRequestException(400);
			}
			if (!versionArgs[0].toUpperCase().equals("HTTP")) {
				throw new HttpRequestException(400);
			}
			boolean isSupportedHtmlVersion = false;
			for (int i = 0; i < supportedHTTPVersions.length; i++) {
				isSupportedHtmlVersion = isSupportedHtmlVersion
						|| versionArgs[1].equals(supportedHTTPVersions[i]);
			}
			if (!isSupportedHtmlVersion) {
				throw new HttpRequestException(400);
			}
			this.httpVersion = versionArgs[1];
			//parse status code
			this.statusCode = firstLineArgs[1];
			this.statusMsg = firstLineArgs[2];
	    	
			//parse headers
			for (int i = 1; i < reqAsLines.length; i++) {
				if(reqAsLines[i].equals(""))
					break;
				String[] headerArgs = reqAsLines[i].split(": ");
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
				} else if (key.equalsIgnoreCase("content-type")) {
						this.ContentType = value;
				}
			}
	    }
	    
	    public String dateTime(){
			return sdf.format(this.downloadDate);
		}
	    
	    public String toString(){
			return domainFromUrl() +"_"+ this.dateTime();
		}
	    
	    // TODO TODO TODO TODO 
	    public String domainFromUrl(){
	    	String fullURL = this.url;
	    	if(fullURL.startsWith("http://")){
	    		fullURL = fullURL.substring(7);
			}
			String[] levels = fullURL.split("/");
			String domainHost = levels[0];
	    	//EXTRACT DOMAIN from URL
	    	return domainHost;
	    }		
	}