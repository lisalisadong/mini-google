package crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utils.Logger;

import crawler.stormlite.Config;
import crawler.stormlite.DistributedCluster;
import crawler.stormlite.Topology;
import crawler.stormlite.TopologyBuilder;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.bolt.ContentSeenBolt;
import crawler.stormlite.bolt.DUEBolt;
import crawler.stormlite.bolt.HTTPModuleBolt;
import crawler.stormlite.bolt.LinkExtractorBolt;
import crawler.stormlite.bolt.URLFilterBolt;
import crawler.urlfrontier.URLFrontier;
import crawler.stormlite.distributed.WorkerJob;
import crawler.stormlite.tuple.Fields;
import crawler.robots.RobotInfoManager;

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
    
    public static String DBPath = "./db";
    
    public static int fileNum = -1;
    
    public static URLFrontier urlFrontier = new URLFrontier();
    public static RobotInfoManager  robotManager = new RobotInfoManager();
    
    private DistributedCluster cluster;
    private List<String> topologies;
    private List<TopologyContext> contexts;
    
    public Crawler() {
    	cluster = new DistributedCluster();
    	topologies = new ArrayList<>();
    	contexts = new ArrayList<>();
    }
    
    public void setUp(WorkerJob workerJob) {
    	
    	Logger.configure(false, false);
    	cluster = new DistributedCluster();
    	urlFrontier.addURL("http://crawltest.cis.upenn.edu/");
//    	urlFrontier.addURL("https://www.reddit.com/");
    	
    	try {
    		Config config = workerJob.getConfig();
    		Topology topo = workerJob.getTopology();
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
    	cluster.startTopology();
    }

	public void stop() {
    	synchronized(topologies) {
			for (String topo: topologies)
				cluster.killTopology(topo);
		}
		cluster.shutdown();
    }

    private static Topology configTopology(Config config) {

        URLSpout URLSpout = new URLSpout();
        HTTPModuleBolt httpModule = new HTTPModuleBolt();
        ContentSeenBolt contentSeen = new ContentSeenBolt();
        LinkExtractorBolt linkExtractor = new LinkExtractorBolt();
        URLFilterBolt urlFilter = new URLFilterBolt();
        // HostSplitterBolt hostSplitter = new HostSplitterBolt();
        DUEBolt due = new DUEBolt();

        // wordSpout ==> countBolt ==> MongoInsertBolt
        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the words
        builder.setSpout(URL_SPOUT, URLSpout, URL_SPOUT_NUM);

        // Four parallel word counters, each of which gets specific words
        builder.setBolt(HTTP_MODULE_BOLT, httpModule, HTTP_MODULE_BOLT_NUM)
        .shuffleGrouping(URL_SPOUT);

        builder.setBolt(CONTENT_SEEN_BOLT, contentSeen, CONTENT_SEEN_BOLT_NUM)
        .shuffleGrouping(HTTP_MODULE_BOLT);
        
        builder.setBolt(LINK_EXTRACTOR_BOLT, linkExtractor, LINK_EXTRACTOR_BOLT_NUM)
        .shuffleGrouping(CONTENT_SEEN_BOLT);
        
        builder.setBolt(URL_FILTER_BOLT, urlFilter, URL_FILTER_BOLT_NUM)
        .shuffleGrouping(LINK_EXTRACTOR_BOLT);
        
        builder.setBolt(DUE_BOLT, due, DUE_BOLT_NUM)
        .fieldsGrouping(URL_FILTER_BOLT, new Fields("host"));
        
        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
    	
    	Crawler crawler = new Crawler();
    	
    	WorkerJob workerJob = getWorkerJob();
    	
    	crawler.setUp(workerJob);
    	
    	crawler.start();
    	
    	(new BufferedReader(new InputStreamReader(System.in))).readLine();
    	
    	crawler.stop();
        
        System.exit(0);
    }
    

	public static WorkerJob getWorkerJob() {

    	Config config = new Config();
    	config.put("workerList", "[0:0:0:0:8000]");
    	config.put("workerIndex", "0");
    	
    	Topology topo = configTopology(config);
    	
    	ObjectMapper mapper = new ObjectMapper();
		try {
			String str = mapper.writeValueAsString(topo);
			System.out.println("The StormLite topology is:\n" + str);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return new WorkerJob(topo, config);
	}

    public static URLFrontier getURLFrontier() {
        return urlFrontier;
    }

    public static RobotInfoManager getRobotManager() {
        return robotManager;
    }
}
