import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;


public class PortScanThread extends Thread{
	
	SynchronizedQueue<Integer> ports;
	class1 c1object;
	SynchronizedQueue<String> downloaderQueue;
	SynchronizedQueue<AnalyzerQueueObject> analyzerQueue;
	HashMap<String, Statistics> domainMap;
	String host;
	
	public PortScanThread(SynchronizedQueue<String> downloaderQ,SynchronizedQueue<AnalyzerQueueObject> analyzerQueue, HashMap<String, Statistics> domainMap, String host, SynchronizedQueue<Integer> ports, class1 c1object){
		this.ports = ports;
		this.c1object = c1object;
		this.downloaderQueue = downloaderQ;
		this.analyzerQueue = analyzerQueue;
		this.domainMap = domainMap;
		this.host = host;
	}
	
	public void run(){
		System.out.println("PORT SCAN THREAD HAS STARTED");
		System.out.println("PORT SCAN THREAD HAS STARTED");
		System.out.println("PORT SCAN THREAD HAS STARTED");
		System.out.println("PORT SCAN THREAD HAS STARTED");
		try{
			while(ports.getSize() > 0) {
				int port = ports.dequeue();
				Socket socket = new Socket();
				try{
					socket.connect(new InetSocketAddress(c1object.initialDomain, port), 500);
					
					c1object.portScanRes.add(port);
					
					socket.close();
				} catch (Exception e){
					System.out.println("Failed to scan port num. " + port);
				}
			}
		}
		catch (Exception e) {
			System.out.println("Port scan failed, starting web crawler istead");
		}
		finally{
			downloaderQueue.enqueue(c1object.initialDomain);
			ExecResListener execRes = new ExecResListener(this.downloaderQueue,this.analyzerQueue,domainMap, domainMap.get(host).map.get("Domain Name"), c1object, ports);

			Thread execResThread = new Thread(execRes);
			execResThread.start();
		}

		}
	}
	
