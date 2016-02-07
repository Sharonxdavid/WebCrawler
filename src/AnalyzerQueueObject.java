import java.text.SimpleDateFormat;
import java.util.Date;


	public class AnalyzerQueueObject {
		String htmlBody;
		String url;
		String host;
		Date downloadDate;
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
	    public AnalyzerQueueObject(String url, String host, String body, Date date){
	    	this.htmlBody = body;
	    	this.url = url;
	    	this.host = host;
	    	this.downloadDate = date;
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