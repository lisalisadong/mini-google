package crawler.master;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crawler.Crawler;
import crawler.due.DUEBolt;
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
import crawler.worker.WorkerStatus;
import utils.Logger;

public class CrawlerMasterServlet extends HttpServlet {
	static final long serialVersionUID = 455555001;
	
	static Logger logger = new Logger(CrawlerMasterServlet.class.getName());
	
  public final static String SPOUT_NUM = "1";
  
  public static String begin = "<html><meta charset=\"UTF-8\">"
			+ "<head><title>Master Admin Portal</title>"
			+ "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" "
			+ "integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">"
			+ "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css\" integrity=\"sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp\" "
			+ "crossorigin=\"anonymous\"><script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" "
			+ "crossorigin=\"anonymous\"></script></head><style></style>"
			+ "<body><div class=\"row\"><br><br><br></div><div class=\"col-md-2\"></div><div class=\"jumbotron col-md-8\" align=\"center\">"
			+ "<h1>Crawler Admin Portal</h1><p></p><br>";
  
  public static String end = "</body></html>";
  
  public static String tableBegin = "<h2 align=\"left\">Worker Status:</h2><table class=\"table table-bordered table-striped\"><thead><tr>"
								  + "<th>IP:PORT</th><th>#Crawled File</th></tr></thead><tbody>";
  
  public static String tableEnd = "</tbody></table>";
  
  public static boolean isCrawling = false;
  
  private HashMap<String, WorkerStatus> workerStatus;
  private HashMap<String, Long> lastPing;
  
  public void init() throws ServletException {
	super.init();
	workerStatus = new HashMap<>();
	lastPing = new HashMap<>();
  }
  
  @Override
  public void destroy() {
	super.destroy();
  }
  
  public void doPost(HttpServletRequest req, HttpServletResponse res) 
		  throws JsonProcessingException, IOException 
  {		
	  Set<String> activeWorkers = getActiveWorkers();

	  if(!isCrawling) { 	// start cralwer
		  Config config = new Config();
		  
		  Topology topo = buildTopo(config);
		  
		  WorkerJob job = new WorkerJob(topo, config);
		  
		  ObjectMapper mapper = new ObjectMapper();
	      mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	     
		  String workerList = getActiveWorkerList(activeWorkers);
		  System.out.println("worker list: " + workerList);
		  config.put("workerList", workerList);
		  
		  sendDefineJob(config, mapper, job, activeWorkers);
		  
	      sendRunJob(config, mapper, job, activeWorkers);
	      
	  } else {				// stop crawler
		  
		  sendStopJob(null, null, null, activeWorkers);
		  
	  }
	  
      isCrawling = !isCrawling;
      res.sendRedirect("status");
  }
  
  private void sendStopJob(Config config, ObjectMapper mapper,
		  WorkerJob job, Set<String> activeWorkers) 
		  throws IOException 
  {
	for (String dest: activeWorkers) {
		sendJob(dest, "POST", config, "shutdown", "").getResponseCode();
		System.out.println("sent stop job to " + dest);
	}
  }
  
  private void sendRunJob(Config config, ObjectMapper mapper,
		  WorkerJob job, Set<String> activeWorkers) 
		  throws IOException 
  {
	for (String dest: activeWorkers) {
		if (sendJob(dest, "POST", config, "run", "").getResponseCode() != 
			HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Job execution request failed");
		}
		System.out.println("sent run job to " + dest);
	}
  }
  
  private Set<String> getActiveWorkers() {
	  HashSet<String> res = new HashSet<>();
	  for(String worker: workerStatus.keySet()) {
		  if(isActive(worker)) {
			  res.add(worker);
		  }
	  }
	  return res;
  }
  
  private String getActiveWorkerList(Set<String> activeWorkers) {
	// construct worker list
	  StringBuilder sb = new StringBuilder("[");
	  for(String dest: activeWorkers) {
		  WorkerStatus ws = workerStatus.get(dest);
		  sb.append(ws.ip + ":" + ws.port + ",");
	  }
	  if(sb.length() > 1) sb.setLength(sb.length() - 1);
	  sb.append("]");
	  return sb.toString();
  }

  private void sendDefineJob(Config config, ObjectMapper mapper, 
		  WorkerJob job, Set<String> activeWorkers) 
		  throws JsonProcessingException, IOException 
  {
	  /* send "setup" to all active workers */
	  int i = 0;
	  for (String dest: activeWorkers) {
		  config.put("workerIndex", String.valueOf(i++));
		  HttpURLConnection conn = sendJob(dest, "POST", config, "setup", 
					mapper.writerWithDefaultPrettyPrinter().writeValueAsString(job));
		  if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) 
		  {	
			  throw new RuntimeException("Job definition request failed");
		  }
		  System.out.println("Define job sent to " + dest);
	  }
  }
  
  static HttpURLConnection sendJob (String dest, String reqType, Config config, String job, String parameters) throws IOException {
	  	URL url = new URL("http://" + dest + "/" + job);
		
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
  
  private Topology buildTopo(Config config) {
	  URLSpout URLSpout = new URLSpout();
      HTTPModuleBolt httpModule = new HTTPModuleBolt();
      ContentSeenBolt contentSeen = new ContentSeenBolt();
      LinkExtractorBolt linkExtractor = new LinkExtractorBolt();
      URLFilterBolt urlFilter = new URLFilterBolt();
//      HostSplitterBolt hostSplitter = new HostSplitterBolt();
      DUEBolt due = new DUEBolt();
      
      // wordSpout ==> countBolt ==> MongoInsertBolt
      TopologyBuilder builder = new TopologyBuilder();

      // Only one source ("spout") for the words
      builder.setSpout(Crawler.URL_SPOUT, URLSpout, Crawler.URL_SPOUT_NUM);
      
      // Four parallel word counters, each of which gets specific words
      builder.setBolt(Crawler.HTTP_MODULE_BOLT, httpModule, Crawler.HTTP_MODULE_BOLT_NUM)
      .shuffleGrouping(Crawler.URL_SPOUT);

//      builder.setBolt(Crawler.CONTENT_SEEN_BOLT, contentSeen, Crawler.CONTENT_SEEN_BOLT_NUM)
//      .shuffleGrouping(Crawler.HTTP_MODULE_BOLT);
      
//      builder.setBolt(Crawler.LINK_EXTRACTOR_BOLT, linkExtractor, Crawler.LINK_EXTRACTOR_BOLT_NUM)
//      .shuffleGrouping(Crawler.HTTP_MODULE_BOLT);
//      .shuffleGrouping(Crawler.CONTENT_SEEN_BOLT);
      
      builder.setBolt(Crawler.URL_FILTER_BOLT, urlFilter, Crawler.URL_FILTER_BOLT_NUM)
//      .shuffleGrouping(Crawler.LINK_EXTRACTOR_BOLT);
      .shuffleGrouping(Crawler.HTTP_MODULE_BOLT);
      
      builder.setBolt(Crawler.DUE_BOLT, due, Crawler.DUE_BOLT_NUM)
//      .fieldsGrouping(Crawler.URL_FILTER_BOLT, new Fields("url"));
      .fieldsGrouping(Crawler.URL_FILTER_BOLT, new Fields("host"));
     
      return builder.createTopology();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws java.io.IOException
  {		
	  try {
		  String path = req.getPathInfo();
		  switch(path) {
		  case "/workerstatus": 
			  getWorkerstatusHandler(req, res);
			  break;
		  case "/status":
		  default:
			  getStatusHandler(req, res);
			  break;
		  }
	  } catch(NullPointerException e) {
		  e.printStackTrace();
		  res.sendError(400);
	  }
  }

  private void getStatusHandler(HttpServletRequest req, HttpServletResponse res) throws IOException {
	  res.setContentType("text/html");
	  PrintWriter out = res.getWriter();
	  out.print(begin);
	  
	  out.print(tableBegin);
	  //show status for each active worker
	  for(String worker: workerStatus.keySet()) {
		  if(isActive(worker)) {
			  String row = getRow(workerStatus.get(worker));
			  out.print(row);
		  }
	  }
//	  WorkerStatus stat = new WorkerStatus();
//	  stat.setParams("0.0.0.0", 8000, "idle", "job", 100, 100, "nothing");
//	  out.print(getRow(stat));
	  out.print(tableEnd);
	  
	  out.print("<form action=\"/\" method=\"post\">");
//	  out.println("<strong>Job class path:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</strong>"
//	  		+ "<input type=\"text\" name=\"jobclasspath\"><p></p>");
//	  out.println("<strong>Input directory:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</strong>"
//	  		+ "<input type=\"text\" name=\"inputdir\"><p></p>");
//	  out.println("<strong>Output directory:&nbsp;&nbsp;&nbsp;&nbsp;</strong>"
//	  		+ "<input type=\"text\" name=\"outputdir\"><p></p>");
//	  out.println("<strong>Map thread number:&nbsp;&nbsp;</strong>"
//	  		+ "<input type=\"text\" name=\"mapthreadnum\"><p></p>");
//	  out.println("<strong>Reduce thread number:&nbsp;</strong>"
//	  		+ "<input type=\"text\" name=\"reducethreadnum\"><p></p>");
	  if(!isCrawling) {
		  out.println("<button action=\"/start\" formmethod=\"post\" type=\"submit\" "
			  		+ "class=\"btn btn-warning btn-md\">Start Crawling</button><p></p>");
	  } else {
		  out.println("<button action=\"/stop\" formmethod=\"post\" type=\"submit\" "
			  		+ "class=\"btn btn-danger btn-md\">Stop Crawling</button><p></p>");
	  }
	  out.print("</form>");
	  
	  out.print(end);
  }
  
  private String getRow(WorkerStatus stat) {
	  StringBuilder sb = new StringBuilder();
	  sb.append("<tr>");
	  sb.append("<td>" + stat.ip + ":" + stat.port + "</td>");
	  sb.append("<td>" + stat.getCrawledFileNum() + "</td>");
	  sb.append("</tr>");
	  return sb.toString();
  }
  
  private boolean isActive(String worker) {
	  if(!lastPing.containsKey(worker)) return false;
	  return System.currentTimeMillis() - 30000 < lastPing.get(worker);
  }

  private void getWorkerstatusHandler(HttpServletRequest req, HttpServletResponse res) {
	
//	  System.out.println("Get worker status!");
	String ip = req.getRemoteAddr();
	int port = Integer.parseInt(req.getParameter("port"));
	
	long crawledFileNum = Integer.parseInt(req.getParameter("crawledFileNum"));
	
	String workerId = ip + ":" + port;
	
	if(workerStatus.get(workerId) == null) {
		workerStatus.put(workerId, new WorkerStatus());
	}
	WorkerStatus stat = workerStatus.get(workerId);
	stat.setParams(ip, port, crawledFileNum);

	workerStatus.put(workerId, stat);
	lastPing.put(workerId, System.currentTimeMillis());
  }
}
