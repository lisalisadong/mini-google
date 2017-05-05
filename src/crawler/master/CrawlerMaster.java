package crawler.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crawler.Crawler;
import crawler.due.DUEBolt;
import crawler.storage.DBWrapper;
import crawler.stormlite.Config;
import crawler.stormlite.Topology;
import crawler.stormlite.TopologyBuilder;
import crawler.stormlite.bolt.ContentSeenBolt;
import crawler.stormlite.bolt.HTTPModuleBolt;
import crawler.stormlite.bolt.LinkExtractorBolt;
import crawler.stormlite.bolt.URLFilterBolt;
import crawler.stormlite.distributed.WorkerJob;
import crawler.stormlite.tuple.Fields;
import crawler.urlfrontier.URLSpout;
import crawler.stormlite.distributed.WorkerHelper;

public class CrawlerMaster {
	static Logger logger = Logger.getLogger(CrawlerMaster.class.getName());
	
//	static String workerList = "[127.0.0.1:8000,127.0.0.1:8001]";
	static String workerList = "[127.0.0.1:8001]";
	
//	static DBWrapper db = new DBWrapper(Crawler.DBPath);
	
	
	public static void main(String[] args) {
		
		try {
			
			System.out.println("Master is up, Press [Enter] to launch query, once nodes are alive...");
			
			(new BufferedReader(new InputStreamReader(System.in))).readLine();
			
			Config config = new Config();
	    	config.put("workerList", workerList);
			WorkerJob workerJob = getWorkerJob(config);
			
			String[] workers = WorkerHelper.getWorkers(config);
			
			ObjectMapper mapper = new ObjectMapper();
		    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
			
		    int i = 0;
			for(String dest: workers) {
//				System.out.println("dest: " + dest);
				config.put("workerIndex", String.valueOf(i++));
				if (sendJob(dest, "POST", config, "setup", 
						mapper.writerWithDefaultPrettyPrinter().writeValueAsString(workerJob)).getResponseCode() != 
						HttpURLConnection.HTTP_OK) 
				{
					throw new RuntimeException("Job definition request failed");
				}
			}
			
			for (String dest: workers) {
				if (sendJob(dest, "POST", config, "run", "").getResponseCode() != 
						HttpURLConnection.HTTP_OK) {
					throw new RuntimeException("Job execution request failed");
				}
			}
			
			System.out.println("[Enter] to stop crawling...");
			(new BufferedReader(new InputStreamReader(System.in))).readLine();

			for (String dest: workers) {
				sendJob(dest, "POST", config, "shutdown", "").getResponseCode();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static Topology configTopology(Config config) {
    	
    	URLSpout URLSpout = new URLSpout();
        HTTPModuleBolt httpModule = new HTTPModuleBolt();
        ContentSeenBolt contentSeen = new ContentSeenBolt();
        LinkExtractorBolt linkExtractor = new LinkExtractorBolt();
        URLFilterBolt urlFilter = new URLFilterBolt();
//        HostSplitterBolt hostSplitter = new HostSplitterBolt();
        DUEBolt due = new DUEBolt();
        
        // wordSpout ==> countBolt ==> MongoInsertBolt
        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the words
        builder.setSpout(Crawler.URL_SPOUT, URLSpout, Crawler.URL_SPOUT_NUM);
        
        // Four parallel word counters, each of which gets specific words
        builder.setBolt(Crawler.HTTP_MODULE_BOLT, httpModule, Crawler.HTTP_MODULE_BOLT_NUM)
        .shuffleGrouping(Crawler.URL_SPOUT);

        builder.setBolt(Crawler.CONTENT_SEEN_BOLT, contentSeen, Crawler.CONTENT_SEEN_BOLT_NUM)
        .shuffleGrouping(Crawler.HTTP_MODULE_BOLT);
        
        builder.setBolt(Crawler.LINK_EXTRACTOR_BOLT, linkExtractor, Crawler.LINK_EXTRACTOR_BOLT_NUM)
        .shuffleGrouping(Crawler.CONTENT_SEEN_BOLT);
        
        builder.setBolt(Crawler.URL_FILTER_BOLT, urlFilter, Crawler.URL_FILTER_BOLT_NUM)
        .shuffleGrouping(Crawler.LINK_EXTRACTOR_BOLT);
        
        builder.setBolt(Crawler.DUE_BOLT, due, Crawler.DUE_BOLT_NUM)
//        .fieldsGrouping(Crawler.URL_FILTER_BOLT, new Fields("url"));
        .fieldsGrouping(Crawler.URL_FILTER_BOLT, new Fields("host"));
       
        return builder.createTopology();
    }
	
	public static WorkerJob getWorkerJob(Config config) {
    	
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
	
	
	
	static HttpURLConnection sendJob(String dest, String reqType, Config config, String job, String parameters) throws IOException {

		System.out.println(dest + "/" + job);
		
		URL url = new URL(dest + "/" + job);
		
		logger.info("Sending request to " + url.toString());
		
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod(reqType);
		
		if (reqType.equals("POST")) {
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream os = conn.getOutputStream();
			byte[] toSend = parameters.getBytes();
			os.write(toSend);
			os.flush();
		} else {
			conn.getOutputStream();
		}
		
		
		return conn;
    }
}
