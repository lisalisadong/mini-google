package crawler.worker;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * wrapper class for workers
 * @author xiaofandou
 *
 */
public class WorkerStatus implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 496826026898565263L;
	
//	private String[] states = {"idle", "mapping", "waiting", "reducing"}; 
	public String ip;
	public int port;
	long crawledFileNum;
	
	@JsonIgnore
	public void setParams(String ip, int port, long crawledFileNum) 
	{
		this.ip = ip;
		this.port = port;
		this.crawledFileNum = crawledFileNum;
	}
	
	@JsonIgnore
	public synchronized void incFileNum() {
		crawledFileNum++;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public long getCrawledFileNum() {
		return crawledFileNum;
	}
	
	public void setCrawledFileNum(long crawledFileNum) {
		this.crawledFileNum = crawledFileNum;
	}
}
