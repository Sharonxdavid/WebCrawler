import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Multi-threaded WebServer implementation over TCP
 * @author Sharon David,Tal Bigel
 *
 */
public class WebServer {

	public static String directorySeperator = "\\"; // For Windows: "\\"  For Mac: "/"
	public static boolean runOnMac = false;
	public final static String CRLF = "\r\n";
	private int port;
	private String rootDir;
	private String defaultPage;
	private int maxThread;
	private int maxDownloaders;
	private int maxAnalyzers;
	
	SynchronizedQueue<String> downloaderQueue;
	SynchronizedQueue<AnalyzerQueueObject> analyzerQueue;
	class1 c1object;
	
	Downloader downloader;
	Analyzer analyzer;
	
	HashMap<String, Statistics> domainMap;

	
	public WebServer(){
		this.c1object = new class1();
//		this.downloaderQueue = new SynchronizedQueue<>(10000, c1object);
		this.downloaderQueue = new SynchronizedQueue<>(10000);
		this.analyzerQueue = new SynchronizedQueue<>(10000);
		domainMap = new HashMap<>();
		downloader = new Downloader(downloaderQueue, analyzerQueue, domainMap, c1object);
		analyzer = new Analyzer(downloaderQueue, analyzerQueue, domainMap, c1object);
		//for loop i < 10 t[i] = new thread
		Thread[] downloaderThreadArray = new Thread[10];
		Thread[] analyzerThreadArray = new Thread[2];
		
		for (int i = 0; i < downloaderThreadArray.length; i++) {
			downloaderThreadArray[i] = new Thread(downloader);
			downloaderThreadArray[i].start();
		}
		for (int i = 0; i < analyzerThreadArray.length; i++) {
			analyzerThreadArray[i] = new Thread(analyzer);
			analyzerThreadArray[i].start();
		}		
	}

	/***
	 * Reads config file,parse it, and init. class variables.
	 * 
	 * @throws FileNotFoundException
	 *             if config file is not found
	 * @throws IOException
	 *             else
	 */
	public void readAndParseConfigFile() throws FileNotFoundException,
			IOException {
		// read and parse config file.
		StringBuilder configFileContent = readConfigFile();
		parseConfigFile(configFileContent);
		isValidConfigFile();
	}

	/**
	 * Checks whether the config file contains valid values.
	 */
	private void isValidConfigFile() {
		if (this.port < 1) {
			throw new IllegalArgumentException(
					"Port parameter is illegal, value is negative.\n Please change it and try again. Thank you!");
		}

		// negative number of threads is not allowed
		if (this.maxThread < 0 || this.maxThread > 10) {
			throw new IllegalArgumentException(
					"ThreadMax parameter is illegal, value is negative.\n Please change it and try again. Thank you!");
		}

		if (this.defaultPage == null || this.defaultPage.isEmpty()) {
			throw new IllegalArgumentException(
					"Default page parameter is illegal.\n Please change it and try again. Thank you!");
		}

		if (this.rootDir == null || this.rootDir.isEmpty()) {
			throw new IllegalArgumentException(
					"Root directory parameter is illegal.\n Please change it and try again. Thank you!");
		}
	}

	/**
	 * 
	 * Welcomes the client with a message,creates a socket and listens
	 * to the port mention in the configurtion file. When a new connection
	 * is created, the HTTP request is wrapped in a runnable object and a thread handle it.
	 * 
	 */
	public void requestsListener() {
		ServerSocket welcomeSocket = null;
		try {
			// Welcome client msg, notify what port the server is listening.
			System.out.println("***********************************************");
			System.out
					.println("*** Welcome to our server, Nice to see you! ***\n****** Server is listening to port #"
							+ Integer.toString(this.port) + " ******");
			System.out.println("***********************************************");

			welcomeSocket = new ServerSocket(this.port);
			while (true) {
				try {
					boolean canCreateNewThread = false;
					Socket currentConnection = welcomeSocket.accept();
					// check if possible to create a new thread
					canCreateNewThread = checkLock(canCreateNewThread);
					// check if a new thread can be added, then start it.
					startThread(canCreateNewThread, currentConnection, downloaderQueue, analyzerQueue);
				} catch (Exception e) {
					System.out.println(e.getClass().getSimpleName()
							+ " Server listener loop --- 4 thrown, with message: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getClass().getSimpleName()
					+ " 5 thrown, with message: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				// When finished,close socket.
				if (welcomeSocket != null) {
					welcomeSocket.close();
				}
			} catch (IOException e) {
				System.exit(-1);
			}
		}
	}

	/**
	 * Takes the lock in order to increase the threads counter(only if possible
	 * by thread limitation!) , this is a critical section and therefore
	 * protected by synchronized.
	 * 
	 * @param canCreateNewThread
	 * @return
	 */
	private boolean checkLock(boolean canCreateNewThread) {
		synchronized (HttpRequestHandler.getLock()) {
			if (HttpRequestHandler.getThreadCounter() < this.maxThread) {
				HttpRequestHandler.increaseThreadCounter();
				canCreateNewThread = true;
			}
		}
		return canCreateNewThread;
	}

	/**
	 * if param canCreateNewThread is true, creates the thread and start it.
	 * @param canCreateNewThread
	 * @param currentConnection
	 * @param downloaderQueue 
	 * @param analyzerQueue 
	 */
	private void startThread(boolean canCreateNewThread,
			Socket currentConnection, SynchronizedQueue<String> downloaderQueue, SynchronizedQueue<AnalyzerQueueObject> analyzerQueue) {
		if (canCreateNewThread) { // if true, a new thread would start
			HttpRequestHandler handler = new HttpRequestHandler(
					currentConnection, this.rootDir, this.defaultPage, downloaderQueue, analyzerQueue, domainMap, c1object);
			Thread thread = new Thread(handler);
			thread.start();
		}
		// if false, thread wasn't created.
	}

	/**
	 * Matches the values from the config file to the class variables.
	 * 
	 * @param configFileContent
	 *            the configuration file in a string builder format
	 * @throws IllegalArgumentException
	 * @throws NumberFormatException
	 */
	private void parseConfigFile(StringBuilder configFileContent)
			throws IllegalArgumentException, NumberFormatException {
		if (configFileContent == null) {
			throw new NullPointerException("Config file is null");
		}
		// split config file by lines
		String[] lines = configFileContent.toString().split(CRLF);
		// split each line
		for (String line : lines) {
			String[] lineArgs = line.split("=");

			if (lineArgs.length != 2) {
				// case line is not by the format of tag=value
				throw new IllegalArgumentException(line
						+ ": Config file is illegal. The line:" + line
						+ " isn't in the format of tag = value");
			}
			//init class variable by the Config file values.
			ConfigFileValues tag = ConfigFileValues.matchTag(lineArgs[0]);
			ArrayList<String> tempArrayList = new ArrayList<>();
			ArrayList<String> tempArrayList2 = new ArrayList<>();
			ArrayList<String> tempArrayList3 = new ArrayList<>();
			switch (tag) {
			case PORT:
				this.port = Integer.parseInt(lineArgs[1]);
				break;
			case DEFAULTPAGE:
				this.defaultPage = lineArgs[1];
				break;
			case ROOT:
				// Add \ in the end of folder path if does not exist
				if (!lineArgs[1].endsWith(WebServer.directorySeperator)) {
					this.rootDir = lineArgs[1] + WebServer.directorySeperator;
				} else {
					this.rootDir = lineArgs[1];
				}
				c1object.rootPath = this.rootDir;
				break;
			case MAXTHREADS:
				this.maxThread = Integer.parseInt(lineArgs[1]);
				break;
			case MAXDOWNLOADER:
				this.maxDownloaders = Integer.parseInt(lineArgs[1]);
				break;
			case MAXANALYZERS:
				this.maxAnalyzers = Integer.parseInt(lineArgs[1]);
				break;
			case IMAGEEXTENSIONS:
				String[] temp = lineArgs[1].split(",");
				for (String img : temp) {
					if(img.startsWith("\"")){
						tempArrayList.add(img.substring(1));
					}
					else if(img.endsWith("\"")){
						tempArrayList.add(img.substring(0,img.length()-1));
					}
					else tempArrayList.add(img);
				}
				c1object.setImagesExtensions(tempArrayList);
//				tempArrayList.clear();
				break;
			case VIDEOEXTENSIONS:
				String[] tempV = lineArgs[1].split(",");
				for (String img : tempV) {
					if(img.startsWith("\"")){
						tempArrayList.add(img.substring(1));
					}
					else if(img.endsWith("\"")){
						tempArrayList.add(img.substring(0,img.length()-1));
					}
					else tempArrayList.add(img);
				}
				c1object.setVideoExtensions(tempArrayList2);
//				tempArrayList.clear();
				break;
			case DOCUMENTEXTENSIONS:
				String[] tempD = lineArgs[1].split(",");
				for (String img : tempD) {
					if(img.startsWith("\"")){
						tempArrayList.add(img.substring(1));
					}
					else if(img.endsWith("\"")){
						tempArrayList.add(img.substring(0,img.length()-1));
					}
					else tempArrayList.add(img);
				}
				c1object.setDocumentExtensions(tempArrayList3);
//				tempArrayList.clear();
				break;
			default:
				throw new IllegalArgumentException("Tag: " + tag.toString()
						+ " has no implementation");
			}
		}
	}


	public String getRootDir() {
		return rootDir;
	}

	/**
	 * Read from config file into a stringbuilder.
	 * 
	 * @return String builder represent the config file content.
	 * @throws FileNotFoundException
	 *             when config file does not exist
	 * @throws IOException
	 */
	private static StringBuilder readConfigFile() throws FileNotFoundException,
			IOException {
		// System.out.println("Inside readConfigFile");
		StringBuilder configFileContent = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader("config.ini"));
		while (br.ready()) {
			configFileContent.append(br.readLine() + "\r\n");
		}
		return configFileContent;
	}

	/**
	 * Main method - Initialize the webserver and starts the listening to
	 * requests
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WebServer webServer = new WebServer();
			webServer.readAndParseConfigFile();
			webServer.requestsListener();
		} catch (FileNotFoundException e) {
			System.out.println(e.getClass().getSimpleName()
					+" 1 thrown, with message: " + e.getMessage()
					+ "\nThe config file is missing!");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println(e.getClass().getSimpleName()
					+" 2 thrown, with message: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			System.out.println(e.getClass().getSimpleName()
					+ " 3 thrown, with message: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
