package crawler.worker;

/**
 * Simple listener for worker creation 
 * 
 * @author zives
 *
 */
public abstract class WorkerServer {
	
	protected String masterAddr;
	protected int myPort;
	
	public WorkerServer(String masterAddr, int myPort) 
	{
		this.masterAddr = masterAddr;
		this.myPort = myPort;
	}
	
	public abstract void run();

	public abstract void shutdown();
}
