package crawler.stormlite.distributed;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;


import com.fasterxml.jackson.databind.ObjectMapper;

import crawler.Crawler;
import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.bolt.IRichBolt;
import crawler.stormlite.bolt.OutputCollector;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.worker.CrawlerWorker;
import utils.Logger;

/**
 * This is a virtual bolt that is used to route data to the WorkerServer on a
 * different worker.
 * 
 * @author zives
 *
 */
public class SenderBolt implements IRichBolt {
	
	static Logger log = new Logger(SenderBolt.class.getName());

    /**
     * To make it easier to debug: we have a unique ID for each instance of the
     * WordCounter, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("key", "value");

    String stream;
    String address;
    ObjectMapper mapper = new ObjectMapper();
    URL url;

    TopologyContext context;

    boolean isEndOfStream = false;

    public SenderBolt(String address, String stream) {
        this.stream = stream;
        this.address = address;
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.context = context;
        try {
            url = new URL(address + "/pushdata/" + stream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create remote URL");
        }
    }

    /**
     * Process a tuple received from the stream, incrementing our counter and
     * outputting a result
     */
    @Override
    public void execute(Tuple input) {
        try {
            send(input);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    /**
     * Sends the data along a socket
     * 
     * @param stream
     * @param tuple
     * @throws IOException
     */
    private synchronized void send(Tuple tuple) throws IOException {

    	isEndOfStream = tuple.isEndOfStream();
		long start = System.currentTimeMillis();
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		String jsonForTuple = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tuple);
		byte[] toSend = jsonForTuple.getBytes();
		os.write(toSend);
		os.flush();
		conn.getResponseCode();
		conn.disconnect();
		Crawler.logEvent("Sent to remote host", start);
    }

    /**
     * Shutdown, just frees memory
     */
    @Override
    public void cleanup() {
    }

    /**
     * Lets the downstream operators know our schema
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

    /**
     * Used for debug purposes, shows our executor/operator's unique ID
     */
    @Override
    public String getExecutorId() {
        return executorId;
    }

    /**
     * Called during topology setup, sets the router to the next bolt
     */
    @Override
    public void setRouter(StreamRouter router) {
        // NOP for this, since its destination is a socket
    }

    /**
     * The fields (schema) of our output stream
     */
    @Override
    public Fields getSchema() {
        return schema;
    }
}
