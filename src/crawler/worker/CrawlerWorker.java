package crawler.worker;

import crawler.stormlite.tuple.Tuple;
import crawler.Crawler;
import crawler.storage.DBWrapper;
import crawler.stormlite.distributed.WorkerJob;
import utils.Logger;

import static spark.Spark.setPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrawlerWorker extends WorkerServer {
	
	static Logger logger = new Logger(WorkerServer.class.getName());
	
	public static String WORKER_ID;
	Crawler crawler;
	
	public static DBWrapper db;
	
	public static String STATUS_ID = "ID";
	public static WorkerStatus workerStatus;
	
	ObjectMapper om;
	
	public boolean isRunning = false;
	
	public static int port;
	
	public CrawlerWorker(String masterAddr, int myPort) {
		super(masterAddr, myPort);
		
		crawler = new Crawler();
		port = myPort;
		om = new ObjectMapper();
	    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Override
	public void run() {
		logger.debug("Creating server listener at socket " + myPort);
		isRunning = true;
		
		startPingThread();
		setPort(myPort);
        
        Spark.post(new Route("/setup") {
        	@Override
			public Object handle(Request arg0, Response arg1) {
        		
        		System.out.println("received setup");
        		
				try {
					WorkerJob workerJob = om.readValue(arg0.body(), WorkerJob.class);
	        		crawler.setUp(workerJob);

	        		return "set up finished";
				} catch (IOException e) {
					e.printStackTrace();
					// Internal server error
					arg1.status(500);
					return e.getMessage();
				}
			}
        });
        
        Spark.post(new Route("/run") {
        	
			@Override
			public Object handle(Request arg0, Response arg1) {
				
				System.out.println("received run");
				
				crawler.start();
				
				return "Started";
			}
        });
        
        Spark.post(new Route("/pushdata/:stream") {
        	
        	// TODO: data handler
			@Override
			public Object handle(Request arg0, Response arg1) {
				
				
				try {
					String stream = arg0.params(":stream");
					Tuple tuple = om.readValue(arg0.body(), Tuple.class);
					logger.debug("Worker received: " + tuple + " for " + stream);
//					System.out.println("Worker received: " + tuple + " for " + stream);
					
					crawler.pushData(stream, tuple);
					
					return "";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					arg1.status(500);
					return e.getMessage();
				}
				
				
			}
        	
        });
        
        Spark.post(new Route("/shutdown") {
        	@Override
			public Object handle(Request arg0, Response arg1) {
				System.out.println("received shutdown!");
        		logger.debug("Shutting down all workers");
        		shutdown();
//        		System.exit(0);
				return "Shutted down";
			}
        });

	}

	@Override
	public void shutdown() {
		db.saveWorkerStatus(workerStatus);
		db.sync();
		crawler.stop();
	}
	
	public static void config() {
		STATUS_ID += WORKER_ID;
		db = new DBWrapper(Crawler.DBPath + WORKER_ID);
		db.setup();
		
		workerStatus = db.getWorkerStatus(STATUS_ID);
		if(workerStatus == null) {
			workerStatus = new WorkerStatus(STATUS_ID);
			System.out.println("[status] new status");
		} else {
			System.out.println("[status] Pages crawled previously: " + workerStatus.getCrawledFileNum());
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: [master addr] [port] [id]");
			return;
		}
		
		String masterAddr = args[0];
		int port = 8000;
		System.out.println("[Crawler worker]: master in " + masterAddr);
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		
		CrawlerWorker.WORKER_ID = args[2];
		CrawlerWorker.config();
		Crawler.config();
		Logger.configure(false, false);
		CrawlerWorker worker = new CrawlerWorker(masterAddr, port);
		
	
		worker.run();
		try {
			(new BufferedReader(new InputStreamReader(System.in))).readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

    private void startPingThread() {
		Thread backgroundThread = new Thread(){
			public void run() {
				while(isRunning) {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						ObjectMapper mapper = new ObjectMapper();
				        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
				        
				        // TODO: should be /workerstatus
						URL url = new URL("http://" + masterAddr + 
								"/workerstatus" + getParamString());
						HttpURLConnection conn = (HttpURLConnection)url.openConnection();
						conn.setDoOutput(true);
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Content-Type", "application/json");
						conn.getResponseCode();
						conn.disconnect();
					} catch (IOException e) {
//						e.printStackTrace();
					} 
				}
			}
		};
		backgroundThread.start();
	}
    
    private static String getParamString() {
		StringBuilder sb = new StringBuilder("?");
		synchronized(workerStatus){
			sb.append("port=" + port + "&");
			sb.append("crawledFileNum=" + workerStatus.getCrawledFileNum());
		}
		return sb.toString();
	}
	
	
	
}
