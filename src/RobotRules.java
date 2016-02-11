import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class RobotRules {
	ArrayList<RobotRule> disallowed; 
	ArrayList<RobotRule> allowed;
	
	public RobotRules(){
		this.disallowed = new ArrayList<>();
		this.allowed =  new ArrayList<>();
	}
	
	public void addToDisallowed(RobotRule r) {
		this.disallowed.add(r);
	}
	public void addToAllowed(RobotRule r) {
		this.allowed.add(r);
	}
	
	public boolean isAllowedToDownload(String urlToDownload) {
		boolean res = true;
		
		// scan disallowed first 
		//while(i < disallowed)
		//   if urlToDownload.match(disallowed[i]) (substring or equals!!!)
		//       res = false;

		
		//if false -> go over allowed and test if it matches
		//while (allowed)
		//   if urlToDownload.match(allowed[i])
		//     res = true;
		
		return res;
	}
	

	public void loadRobotsTxt(String crawlUrl, int crawlPort) {
		System.out.println("Starting loadRobotsTxt for the Domain");
		System.out.println("Starting loadRobotsTxt for the Domain");
		String relativePath= "/robots.txt";
		String line;
		String urlSrcToAnalyze = "";
		RobotRules robotRules = null;
		
		//get domain relPath
		//save response
		//parse response in RobotRules.class with UA and response.
		// UA: -> ["allow/disallow", "RelPath"]
		HttpGetRequest getRobots = new HttpGetRequest(crawlUrl);
		
		String getRobotsRequestString = getRobots.generateGetRequestAsString(relativePath);
		
		System.out.println(Thread.currentThread().getName() + " " + "----This is Get Request----");
		System.out.println(Thread.currentThread().getName() + " " + getRobotsRequestString);
		
		Socket socket;
		DataOutputStream outputStream;
		
		socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(crawlUrl, crawlPort), 1000);
			while (socket.isConnected() == false) {
				//TODO: enter timeout to make sure it quits sometime...
			}
			outputStream = new DataOutputStream(socket.getOutputStream());
			outputStream.write(getRobotsRequestString.getBytes());
			BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println(Thread.currentThread().getName() + " " + "before read response");
			socket.setSoTimeout(5000);
			while ((line = rd.readLine()) != null) {
				System.out.println(Thread.currentThread().getName() +  " " + line);
				urlSrcToAnalyze += line + "\r\n";
			}
			socket.close();
			
		}catch (SocketTimeoutException e) {
			System.out.println(Thread.currentThread().getName() + " " + "Socket time out! (Expected:)");
		
		}
		
		catch (IOException e1) {
			System.out.println("Exception in the get request :-( ");
			e1.printStackTrace();
		} catch(Exception e) {
			System.out.println("Exception in the get response :-( ");
			e.printStackTrace();
		}
		parseRobotsTxt(urlSrcToAnalyze);
//		return robotRules;
	}

	private  void parseRobotsTxt(String ruleList) {
		// TODO Auto-generated method stub
		String myUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
	
		String temp = ruleList;
		String[] reqAsLines = temp.split("\r\n");
		String firstLine = reqAsLines[0];
		String[] firstLineArgs = firstLine.split(" ");
		int i = 0;
		
		// parse status code, return if not 200
		if (!firstLineArgs[2].equals("OK")) {
			System.out.println("Http response code: "+ firstLineArgs[1] +" "+ firstLineArgs[2] + ". Return with out parsing robots.txt // Robot tesxt not found");
//			return robotRules; //No robots text
		}


		for (i = 1 ; i < reqAsLines.length; i++) {
//			if (reqAsLines[i].equals(""))
//				break;
			String[] headerArgs = reqAsLines[i].split(": ");
			if(headerArgs.length == 1){
				continue;
			}
			String key = headerArgs[0];
			String value = headerArgs[1];
			
			System.out.println(key+": "+value );
			try{
				if (key.equalsIgnoreCase("User-agent") ) {
					if (value.equals("*")){
						System.out.println("Our user agent matches this useragent rule!");
//						robotRules = aggregateRules(i,reqAsLines);
						aggregateRules(i+1,reqAsLines);
					}
				}
			}catch(Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
//		return robotRules;
	}

	private  void aggregateRules(int j, String[] reqAsLines) {
//		RobotRules rr = new RobotRules();

		for(int i = j; i < reqAsLines.length; i++){
			String[] headerArgs = reqAsLines[i].split(": ");
			
			//not sure if this is needed or not
			if(headerArgs.length == 1 ){
				break;
			}
			String key = headerArgs[0];
			String value = headerArgs[1];
			
			value = value.replace("/", "\\/");
			value = value.replace("-", "\\-");
			value = value.replace(".", "\\.");
			value = value.replace("*", ".*");
			RobotRule robotRule = new RobotRule(Pattern.compile(value), value);
			if(key.equals("Disallow")){
				addToDisallowed(robotRule);
				System.out.println("disallow " + value);
			} else if (key.equals("Allow")) {
				addToAllowed(robotRule);
				// Disallow: /*?
				// /asdasda?
				System.out.println("allow " + value);
			} else {
				// No more rules.
				break;
			}
			
		}
//		return rr;
		
	}

	
	
		
}
