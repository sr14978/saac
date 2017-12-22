package saac.dataObjects;

public class DelayQueueItem<T>{
	T result;
	int delay;
	public DelayQueueItem(T r, int d) {
		this.result = r;
		this.delay = d;
	}
	public T getResult() {
		return result;
	}
	public int getDelay() {
		return delay;
	}
	public void decrementResultToZero() {
		if(delay > 0) {
			delay--;
		}
	}
	
	public String toString() {
		return result.toString() + " t=(" + Integer.toString(delay) + ")";
	}
	
}