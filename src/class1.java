
public class class1 {
	
	int j = 0;
	
	public synchronized void increment(){
		j++;
	}
	
	public synchronized void decrement(){
		j--;
	}
	
	
	public synchronized boolean isDone(SynchronizedQueue<String> downloaderQueue, SynchronizedQueue<AnalyzerQueueObject> analyzerQueue) {
		String s = "downloaderQ.size =" + downloaderQueue.getSize() + ", analyzerQ.size =" + analyzerQueue.getSize() + ", j is " + j;
		System.out.println(s);
		return (downloaderQueue.getSize() == 0 && analyzerQueue.getSize() == 0 && j == 0);
	}

	
}
