import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class class1 {

	int runningThreadsCounter = 0;

	String initialDomain;
	
	private ArrayList<String> imagesExtensions = new ArrayList<>();
	private ArrayList<String> videoExtensions = new ArrayList<>();
	private ArrayList<String> documentExtensions = new ArrayList<>();
	
	
	String rootPath;
	String startCrawlDateTime;
	
	ArrayList<RobotRule> allow = new ArrayList<>();
	ArrayList<RobotRule> disallow = new ArrayList<>();
	
	boolean respectRobots;
	boolean portScan;
	
	ArrayList<Integer> portScanRes = new ArrayList<>();

	public synchronized void increment() {
		runningThreadsCounter++;
	}

	public synchronized void decrement() {
		runningThreadsCounter--;
	}

	public synchronized boolean isDone(
			SynchronizedQueue<String> downloaderQueue,
			SynchronizedQueue<AnalyzerQueueObject> analyzerQueue) {
		String s = "downloaderQ.size =" + downloaderQueue.getSize()
				+ ", analyzerQ.size =" + analyzerQueue.getSize() + ", j is "
				+ runningThreadsCounter;
		System.out.println(s);
		return (downloaderQueue.getSize() == 0 && analyzerQueue.getSize() == 0 && runningThreadsCounter == 0);
	}
	
	public synchronized boolean isDonePortScan(SynchronizedQueue<Integer> ports){
		return(ports.getSize() == 0);
	}
	
	public String dateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sdf.format(new Date());
	}
	
	public void initDateTime(){
		startCrawlDateTime = dateTime();
	}

	public String getInitialDomain() {
		return initialDomain;
	}

	public void setInitialDomain(String initialDomain) {
		this.initialDomain = initialDomain;
	}
	public ArrayList<String> getImagesExtensions() {
		return imagesExtensions;
	}

	public ArrayList<String> getVideoExtensions() {
		return videoExtensions;
	}

	public ArrayList<String> getDocumentExtensions() {
		return documentExtensions;
	}

	public void setImagesExtensions(ArrayList<String> imagesExtensions) {
		this.imagesExtensions = imagesExtensions;
	}

	public void setVideoExtensions(ArrayList<String> videoExtensions) {
		this.videoExtensions = videoExtensions;
	}

	public void setDocumentExtensions(ArrayList<String> documentExtensions) {
		this.documentExtensions = documentExtensions;
	}
}
