package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utils.Logger;

import crawler.stormlite.Config;
import crawler.stormlite.DistributedCluster;
import crawler.stormlite.Topology;
import crawler.stormlite.TopologyBuilder;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.bolt.ContentSeenBolt;
import crawler.stormlite.bolt.HTTPModuleBolt;
import crawler.stormlite.bolt.LinkExtractorBolt;
import crawler.stormlite.bolt.URLFilterBolt;
import crawler.urlfrontier.URLFrontier;
import crawler.urlfrontier.URLSpout;
import crawler.utils.LRUCache;
import crawler.utils.PageCache;
import crawler.worker.CrawlerWorker;
import crawler.worker.WorkerStatus;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.distributed.WorkerJob;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.due.DUEBolt;
import crawler.due.URLSet;
import crawler.robots.RobotInfoManager;
import crawler.storage.CrawledPage;
import crawler.storage.DBWrapper;

/**
 * The Crawler
 * 
 * @author xiaofandou
 *
 */
public class Crawler {

	static Logger logger = new Logger(Crawler.class.getName());
	
	public static final String URL_SPOUT = "URL_SPOUT";
	public static final int URL_SPOUT_NUM = 1;
	
	public static final String HTTP_MODULE_BOLT = "HTTP_MODULE_BOLT";
	public static final int HTTP_MODULE_BOLT_NUM = 3;
	
	public static final String CONTENT_SEEN_BOLT = "CONTENT_SEEN_BOLT";
	public static final int CONTENT_SEEN_BOLT_NUM = 3;
	
	public static final String LINK_EXTRACTOR_BOLT = "LINK_EXTRACTOR_BOLT";
	public static final int LINK_EXTRACTOR_BOLT_NUM = 3;
	
	public static final String URL_FILTER_BOLT = "URL_FILTER_BOLT";
	public static final int URL_FILTER_BOLT_NUM = 3;
	
	public static final String HOST_SPLITTER_BOLT = "HOST_SPLITTER_BOLT";
	public static final int HOST_SPLITTER_BOLT_NUM = 3;
	
	public static final String DUE_BOLT = "DUE_BOLT";
	public static final int DUE_BOLT_NUM = 3;
	
    public static final String ROBOT_MANAGER = "ROBOT_MANAGER";
    public static final String FRONTIER_QUEUE = "FRONTIER_QUEUE";
   
    public static int PAGE_CACHE_SIZE = 65536;
    public static int NUM_TO_WRITE_SNAPSHOT_FOR_PAGE = 1000;
    
    public static String PAGEDB_Path = "./page_db";
    public static DBWrapper pageDB;
    
    public static String ROBOT_CACHE_PATH = "./robot_db";
    public static RobotInfoManager robotManager;
    
    public static String FRONTIER_DB_PATH = "./frontier_db";
    public static DBWrapper frontierDB;
    public static URLFrontier urlFrontier;
    
    public static String URL_SET_CACHE_PATH = "./url_cache";
    public static int NUM_TO_WRITE_SNAPSHOT_FOR_URL = 1000;
    public static int URL_SET_SIZE = 1000;
    public static URLSet urlSet;
     
    public static int fileNum = -1;
    public static int crawledPageLimit = 5000;
    
    public boolean isRunning;
    
    private DistributedCluster cluster;
    private List<String> topologies;
    private List<TopologyContext> contexts;
     
    public Crawler() {
    	
    	cluster = new DistributedCluster();
    	topologies = new ArrayList<>();
    	contexts = new ArrayList<>();
    	
//    	stateDB.setup();
//    	urlFrontier = stateDB.getURLFrontier();
//		if(urlFrontier == null) {
//			
//			System.out.println("Empty frontier");
//			urlFrontier = new URLFrontier();
//	    	urlFrontier.addURL("http://crawltest.cis.upenn.edu/");
//		} else {
//			System.out.println("Continue from previous crawl: ");
//			System.out.println(urlFrontier.urls);
//		}
    }
    
    public static void config() {
    	/* init page cache */
    	PAGEDB_Path += CrawlerWorker.WORKER_ID;
		pageDB = new DBWrapper(PAGEDB_Path);
		pageDB.setup();
		
		/* init robot cache */
		ROBOT_CACHE_PATH += CrawlerWorker.WORKER_ID;
		robotManager = new RobotInfoManager();
		
		/* init frontier */
		FRONTIER_DB_PATH += CrawlerWorker.WORKER_ID;
		urlFrontier = new URLFrontier(1000, FRONTIER_DB_PATH);
		
		/* init url set */
		URL_SET_CACHE_PATH += CrawlerWorker.WORKER_ID;
		urlSet = new URLSet(1000, URL_SET_CACHE_PATH, NUM_TO_WRITE_SNAPSHOT_FOR_URL);
    	
    }
    
    public void setUp(WorkerJob workerJob) {
    	cluster = new DistributedCluster();
    	
    	try {
    		Config config = workerJob.getConfig();
    		Topology topo = workerJob.getTopology();
    		
    		String suffix = (config.get("workerIndex") == null? "": config.get("workerIndex"));
    		
    		TopologyContext context = 
        			cluster.submitTopology("CrawlerJob", config, topo);
			contexts.add(context);
			synchronized (topologies) {
				topologies.add("CrawlerJob");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	
    }
    
    public void start() {
    	isRunning = true;
    	logger.debug("crwaler starts");
    	cluster.startTopology();
    }

	public void stop() {
		isRunning = false;
		
		writeSnapShot();
    	synchronized(topologies) {
			for (String topo: topologies)
				cluster.killTopology(topo);
		}
		cluster.shutdown();
		
		System.out.println("Crawler stopped");
		logger.debug("Crawler stopped");
    }
	
	public void pushData(String stream, Tuple tuple) {
		// Find the destination stream and route to it
		StreamRouter router = cluster.getStreamRouter(stream);
		
		contexts.get(contexts.size() - 1).incSendOutputs(router.getKey(tuple.getValues()));
		
		router.executeLocally(tuple, contexts.get(contexts.size() - 1));
	}

    public static URLFrontier getURLFrontier() {
        return urlFrontier;
    }

    public static RobotInfoManager getRobotManager() {
        return robotManager;
    }
    
    public static URLSet getURLSet() {
    	return urlSet;
    }
    
    public static DBWrapper getPageDB() {
    	return pageDB;
    }
    
    /*******************methods for snapshot***************/
    public void writeSnapShot() {
    	
//    	urlFrontier.writeSnapshot(Crawler.frontierDB);
    	Crawler.urlSet.writeSnapshot();
    	
    	Crawler.robotManager.writeSnapshot();
    	
    	urlFrontier.writeSnapshot();
//    	
//    	stateDB.sync();
    }
    
}
