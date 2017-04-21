package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import crawler.client.Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

/**
 * class that represents a crawled page
 * @author xiaofandou
 *
 */
@Entity
public class CrawledPage {
	
	@PrimaryKey
	private String url;
//	private URLInfo urlInfo;
	private long lastCrawledTime;
	private byte[] content;
	private LinkedList<String> links;
	
	private String contentType;
	
	public CrawledPage(Client client, String url) {
		this.url = url;
		links = new LinkedList<>();
//		urlInfo = new URLInfo(url);
		lastCrawledTime = -1L;
		InputStream in = client.getInputStream();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int next;
		try {
			while ((next = in.read()) != -1) {
				bos.write(next);
			}
			bos.flush();
			content = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			 e.printStackTrace();
		}
	}
	
	public CrawledPage(byte[] content, String url, String contentType) {
		this.content = content;
		this.url = url;
		this.contentType = contentType;
		this.links = new LinkedList<>();
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public CrawledPage() {
		url = "";
	}
	
	public String getUrl() {
		return url;
	}
	
	public long getLastCrawled() {
		return lastCrawledTime;
	}

	public void setLastCrawled(long now) {
		lastCrawledTime = now;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public InputStream getContentStream() {
		return new ByteArrayInputStream(content);
	}
	
//	private void extractUrl() {
//		if(maxSize > 0 && content.length > maxSize) return;
//		Document doc = Jsoup.parse(new String(content), url);
//		Elements links = doc.select("a[href]");
//		for (Element link : links) {
//			String l = link.attr("abs:href");
//			this.links.add(l);
////			System.out.println(l);
//		}
//	}
	
	public void addLinks(List<String> links) {
		this.links.addAll(links);
	}
	
	public void addLink(String link) {
		this.links.add(link);
	}
	
	public List<String> getLinks() {
		return links;
	}
	
	public static void main(String[] args) {
//		String url = "https://piazza.com/";
//		Client client = Client.getClient(url);
//		client.setMethod("GET");
//		client.sendReq();
//		CrawledPage p = new CrawledPage(client, url, Long.MAX_VALUE);
//		
//		System.out.println(new String(p.content));
		
		
	}
	
}
