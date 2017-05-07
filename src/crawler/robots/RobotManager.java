package crawler.robots;

import java.util.LinkedList;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.storage.DBWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
public class RobotManager {
	
	public static String NO_CRAWL_SUFFIX_FILE = "./NoCrawlSuffix.txt";
	
	public static HashSet<String> noCrawlHosts 
		= new HashSet<String>();  
	
	public static LinkedList<String> noCrawlSuffixes 
		= new LinkedList<String>(); 
	
	public static HashSet<String> allowedHostsCache
		= new HashSet<String>();

	public DBWrapper db;
	
	public RobotManager(String DBPath) {
		db = new DBWrapper(DBPath);
		db.setup();
		System.out.println("[Robot Manager] Robot info cache: " + db.rIdx.map().size());
		confignoCrawlHosts();
		configNoCrawlSuffix();
	}
	
	private void configNoCrawlSuffix() {
		BufferedReader br = null;
		FileReader fr = null;
		
		try {
			fr = new FileReader(NO_CRAWL_SUFFIX_FILE);
			br = new BufferedReader(fr);
			
			String line = null;
			while((line = br.readLine()) != null) {
				System.out.println("[Robot Manager]: no crawl suffix--" + line);
				noCrawlSuffixes.offer(line);
			}
			
		} catch(IOException e) {
			
		}
		System.out.println("[Robot Manager]: finish config suffix");
	}
	
//	public static void addToNoCrawlHost(String host) {
//		synchronized(noCrawlHosts) {
//			noCrawlHosts.add(host);
//		}
//	}

	/**
	 * taboo host
	 */
	public void confignoCrawlHosts() {
		noCrawlHosts.add("mailto");
		noCrawlHosts.add("pinayot.com");
		noCrawlHosts.add("thefappening.so");
		noCrawlHosts.add("jizzbo.com");
		noCrawlHosts.add("bestpornpictures.com");
		noCrawlHosts.add("freepornpics.net");
		noCrawlHosts.add("sexhotpictures.com");
		noCrawlHosts.add("pornktube.com");
		noCrawlHosts.add("bleacherreport.com");
	}
	
	private String getHostName(String url) {
        return new URLInfo(url).getHostName();
    }
	
	public RobotTxt getRobotTxt(String url) {
        String host = getHostName(url);
        RobotTxt rt = db.getRobotTxt(host);
        if(rt != null) return rt;

        String robotUrl = host + "/robots.txt";
        if (!robotUrl.startsWith("http://") && !robotUrl.startsWith("https://")) {
            robotUrl = "http://" + robotUrl;
        }
        
//        long start = System.currentTimeMillis();
        rt = new RobotTxt(robotUrl);
        if(rt.isEmpty()) {
//        	addToNoCrawlHost(host);
        	noCrawlHosts.add(host);
        	return rt;
        }
//        System.out.println("[got robot cache]: " + (System.currentTimeMillis() - start));
        db.saveRobotTxt(rt);
//        db.sync();
        return rt;
    }
	
	/**
     * if a link is allowed by robot rules
     * 
     * @param url
     * @return
     */
    public synchronized boolean isAllowed(String url) {
    	String host = getHostName(url);
        if (host == null || host.startsWith("mailto"))
            return false;
        
        if(isNoCrawlHost(host)) return false;
        if(hasNoCrawlSuffix(host)) return false;
//        /* return false if exceed the crawl limits */
//        Integer num = crawledPageNum.get(host);
//        if(num != null && num >= crawledPageLimit) return false;
        
        RobotTxt robotTxt = getRobotTxt(url);
        if (robotTxt == null)
            return true;
        
        /* not crawl the host with delay */
        if(robotTxt.isEmpty() || robotTxt.getCrawlDelay() > 0) return false;
        	
        String filePath = new URLInfo(url).getFilePath();
        
        return robotTxt.match(filePath);
    }
    
    private boolean hasNoCrawlSuffix(String host) {
		for(String suffix: noCrawlSuffixes) {
			if(host.endsWith(suffix)) return true;
		}
		return false;
	}

	/**
     * returns true if the host is taboo
     * @param host
     * @return
     */
    private boolean isNoCrawlHost(String host) {
		return noCrawlHosts.contains(host);
	}

	public void writeSnapshot() {
    	db.sync();
    	synchronized(db.rIdx) {
    		System.out.println("Robot txt size: " + db.rIdx.map().size());
    	}
    }
	
	public static void main(String[] args) {
		RobotManager rm = new RobotManager("../tmp");
		System.out.println(rm.isAllowed("http://ameblo.jp/qoomin0928/"));
	}
}
