package crawler;

import crawler.worker.WorkerServer;
import crawler.stormlite.distributed.WorkerJob;
import utils.Logger;

import static spark.Spark.setPort;

import java.io.IOException;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CrawlerWorker extends WorkerServer {
	
	static Logger logger = new Logger(WorkerServer.class.getName());
	
	Crawler crawler;
	
	public CrawlerWorker(String masterAddr, int myPort) {
		super(masterAddr, myPort);
		crawler = new Crawler();
	}

	@Override
	public void run() {
		logger.debug("Creating server listener at socket " + myPort);
		
		runBackgroundThread();
		
		setPort(myPort);
    	final ObjectMapper om = new ObjectMapper();
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        
        Spark.post(new Route("/setup") {
        	@Override
			public Object handle(Request arg0, Response arg1) {
        		
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
        
        Spark.post(new Route("/runjob") {
        	
			@Override
			public Object handle(Request arg0, Response arg1) {
				
				crawler.start();
				
				return "Started";
			}
        });
        
        Spark.post(new Route("/pushdata/:stream") {
        	
        	// TODO: data handler
			@Override
			public Object handle(Request arg0, Response arg1) {
				return "";
			}
        	
        });
        
        Spark.post(new Route("/shutdown") {
        	@Override
			public Object handle(Request arg0, Response arg1) {
				System.out.println("received shutdown!");
        		logger.debug("Shutting down all workers");
        		shutdown();
				return "Shutted down";
			}
        });

		
	}

	//TODO: background thread sending status to master
    private void runBackgroundThread() {
    	
	}

	@Override
	public void shutdown() {
		crawler.stop();
	}
	
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage: [master addr] [port]");
			return;
		}
		
		String masterAddr = args[0];
		int port = 8000;
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		
		CrawlerWorker worker = new CrawlerWorker(masterAddr, port);
		
		worker.run();
		
	}
	
}
