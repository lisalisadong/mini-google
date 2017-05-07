package crawler.analysis;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import crawler.storage.DBWrapper;
import crawler.storage.CrawledPage;

public class BDBToS3 {

	private static final String ROOTDIR = "../indexer";
	
	private static int LOG_INTERVAL = 1000;
	
	static AmazonS3 awsClient;
	
	static String INDEXER_BUCKET = "crawler-g02";

	static String accessKey = "AKIAJBEVSUPUI2OHEX6Q";
	static String secretKey = "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";
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		if(args.length != 2 && args.length != 1) {
			System.out.println("Usage: [DB Path] [(optional)if this param exists, print msg in console]");
			return;
		}
		
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);

        try {
           awsClient = new AmazonS3Client(credentials, clientConfig);
        } catch (Exception e) { }
        
        long fileNum = 0;
		
		DBWrapper db = new DBWrapper(args[0]);
		db.setup();

		long start = System.currentTimeMillis();
		logEvent("start uploading from " + args[0] + " to S3");
		for(String url: db.pIdx.map().keySet()) {

			try {
				
				++fileNum;
				ObjectMetadata meta = new ObjectMetadata();
				
				String key = hashUrl(url);

				CrawledPage p = db.getPage(url);
				StringBuilder sb = new StringBuilder();
				sb.append(url + "\t");
				sb.append(p.getContentType() + "\t");
				sb.append(new String(p.getContent()));
				meta.setContentType("text/plain");
				
				byte[] byteToSend = sb.toString().getBytes();
			    meta.setContentLength(byteToSend.length);
			    ByteArrayInputStream contentToWrite = new ByteArrayInputStream(byteToSend);
			    awsClient.putObject(new PutObjectRequest(INDEXER_BUCKET, key, contentToWrite, meta)
						   .withCannedAcl(CannedAccessControlList.PublicRead));
				
			    if(fileNum % LOG_INTERVAL == 0) {
			    	logEvent("Finished uploading " + fileNum + " files", start);
			    }
			    
				if(fileNum == 100) break;
			} catch (Exception e) {

			}
		}
		logEvent("Finished uploading", start);
	}

	private static void logEvent(String event, long start) {
		System.out.println("[" + event + "]: " 
				+ (System.currentTimeMillis() - start) + "ms");
	}
	
	private static void logEvent(String event) {
		System.out.println("[" + event + "]" );
	}
	
	private static String hashUrl(String url) {
      try {
          MessageDigest digest = MessageDigest.getInstance("MD5");
          digest.reset();
          digest.update(url.getBytes("utf-8"));
          return DatatypeConverter.printHexBinary(digest.digest());
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
      return null;
  }
}
