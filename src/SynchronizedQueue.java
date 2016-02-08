/**
 * A synchronized bounded-size queue for multithreaded producer-consumer
 * applications.
 * 
 * @param <T>
 *            Type of data items
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int producers;
	private int first; //pointer to the first element in the queue 
	private int last; //pointer to the last element in the queue
	private int currSize; //store the number of elements in the queue
	class1 c1object;

	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * 
	 * @param capacity
	 *            Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[]) (new Object[capacity]);
		this.producers = 0;
		this.first = 0;
		this.last = 0;
		this.currSize = 0;
//		this.c1object = obj;
	}

	/**
	 * Dequeues the first item from the queue and returns it. If the queue is
	 * empty but producers are still registered to this queue, this method
	 * blocks until some item is available. If the queue is empty and no more
	 * items are planned to be added to this queue (because no producers are
	 * registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public synchronized T dequeue() {
		
		while(0 == this.currSize){
			if (0 == this.producers){
				System.out.println("deque 0 producers");
				return null;
			}
			else {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//Before the pop that way - J++, while the list is not empty.
//		c1object.increment();
		//no need to wait and there is at least one element in the queue
		T curr = this.buffer[first];
		currSize--; //update size
		
		first = (first + 1) % buffer.length; //update first element
		this.notifyAll();
		return curr;
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this
	 * method blocks until some space becomes available.
	 * 
	 * @param item
	 *            Item to enqueue
	 */
	public synchronized void enqueue(T item) {
		while(currSize == buffer.length)
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		//no need to wait there is available place to insert to
		buffer[last] = item;
		currSize++; //update number of elements in the queue
		last = (last + 1) % buffer.length;
		

		//After the push - J--, after the item is added to the list.
//		c1object.decrement();
		this.notifyAll();
	}

	/**
	 * Returns the capacity of this queue
	 * 
	 * @return queue capacity
	 */
	public int getCapacity() {
		return buffer.length;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * 
	 * @return queue size
	 */
	public int getSize() {
		return currSize;
	}

	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items. Every producer of this
	 * queue must call this method before starting to enqueue items, and must
	 * also call <see>{@link #unregisterProducer()}</see> when finishes to
	 * enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public synchronized void registerProducer() {
		// TODO: This should be in a critical section
		this.producers++;
		notifyAll();
	}

	/**
	 * Unregisters a producer from this queue. See <see>
	 * {@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public synchronized void unregisterProducer() {
		// TODO: This should be in a critical section
		this.producers--;
		notifyAll();
	}
	
	public synchronized int getProducer(){
		return this.producers;
	}
}
