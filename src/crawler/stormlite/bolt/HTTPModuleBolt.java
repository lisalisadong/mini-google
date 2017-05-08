package crawler.stormlite.bolt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

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

import crawler.Crawler;
import crawler.client.Client;
import crawler.client.URLInfo;
import crawler.robots.RobotInfoManager;
import crawler.robots.RobotManager;
import crawler.storage.CrawledPage;
import crawler.storage.DBWrapper;
import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.stormlite.tuple.Values;
import crawler.utils.LRUCache;
import crawler.worker.CrawlerWorker;
import utils.Logger;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

public class HTTPModuleBolt implements IRichBolt {
	
	static Logger logger = new Logger(HTTPModuleBolt.class.getName());
	
	static String TIME_CONSUMING_URL_LOG = "timeConsumingUrls.txt";
	
	static BufferedWriter bw;
	
	Fields schema = new Fields("page");
	
	/* s3 client */
//	static AmazonS3 awsClient;
//	
//	static String INDEXER_BUCKET = "crawler-g02";
//
//	static String accessKey = "AKIAJBEVSUPUI2OHEX6Q";
//	static String secretKey = "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";
	
	DBWrapper db;
	
   /**
    * To make it easier to debug: we have a unique ID for each
    * instance of the WordCounter, aka each "executor"
    */
   String id = "[HM]" + UUID.randomUUID().toString();
   
   /**
    * This is where we send our output stream
    */
   private OutputCollector collector;
   
   public HTTPModuleBolt() {
   }
   
   /**
    * Initialization, just saves the output stream destination
    */
   @SuppressWarnings({ "unchecked", "deprecation" })
   @Override
   public void prepare(Map stormConf, 
   		TopologyContext context, OutputCollector collector) 
   {
       this.collector = collector;
       
       db = new DBWrapper(Crawler.DBPath);
       db.setup();
       
       try {
       		bw = new BufferedWriter(new FileWriter(TIME_CONSUMING_URL_LOG));
       } catch (IOException e) {
    	   e.printStackTrace();
       }
   }

   /**
    * Process a tuple received from the stream, incrementing our
    * counter and outputting a result
    */
   @Override
   public void execute(Tuple input) 
   {
	   String url = input.getStringByField("url");
//	   System.out.println(id + " got " + url);
	   
	   long start = System.currentTimeMillis();
	   Client client = Client.getClient(url);
	   if(client == null) {
		   return;
	   }
	   client.setMethod("HEAD");
	   if(!client.sendReq()) {
		   System.out.println("HEAD req error: " + url);
		   return;
	   }
	   
	   Crawler.logEvent("finished HEAD: " + url, start);
	   
	   long contentLength = client.getResContentLength();
//	   Crawler.logEvent(url + " length: " + contentLength);
	   if(contentLength > 700000) {
//		   Crawler.logEvent("drop " + url + ": " + contentLength);
		   return;
	   }
//	   
//	   //TODO: HANDLE 3XX
	   String contentType = client.getResContentType();
//	   Crawler.logEvent(url + " content type" + contentType);
//	   Crawler.logEvent(url + " stats code: " + client.getStatusCode());
////	   System.out.println(id + " content type: " + contentType);
////	   System.out.println(id + " status code: " + client.getStatusCode());
	   boolean validContentType = (contentType == null) || "text/html".equalsIgnoreCase(contentType)
				|| "text/xml".equalsIgnoreCase(contentType)
				|| "application/xml".equalsIgnoreCase(contentType) 
				|| contentType.matches(".*+xml")
				|| "application/pdf".equals(contentType);
//	   // TODO: only support xml & html now
//	   /* the page should be retrieved */
//	   if(validContentType) {
//		   Crawler.logEvent("emit: " + url);
//		   CrawledPage p = downloadPage(url);
//		   collector.emit(new Values<Object>(p));
//	   }
	   
	   if(validContentType) {
		   CrawledPage p = downloadPage(url);
		   if(p == null) return;
		   if("text/html".equals(p.getContentType())) {
			   collector.emit(new Values<Object>(p));
		   } 
		   
		   else {
			   db.savePage(p);
			   db.sync();
		   }
		   
		   /* upload to s3 */
//		   start = System.currentTimeMillis();
//		   ObjectMetadata meta = new ObjectMetadata();
//		   String key = hashUrl(url);
//		   StringBuilder sb = new StringBuilder();
//		   sb.append(url + "\t");
//		   sb.append(p.getContentType() + "\t");
//		   sb.append(new String(p.getContent()));
//		   meta.setContentType("text/plain");
//		   
//		   byte[] byteToSend = sb.toString().getBytes();
//		   meta.setContentLength(byteToSend.length);
//		   ByteArrayInputStream contentToWrite = new ByteArrayInputStream(byteToSend);
//		   awsClient.putObject(new PutObjectRequest(INDEXER_BUCKET, key, contentToWrite, meta)
//					   .withCannedAcl(CannedAccessControlList.PublicRead));
//		   Crawler.logEvent("finished uploading " + url, start);
		   
	   }
	   
//	   CrawledPage p = downloadPage(url);
//	   if(p != null) {
//		   collector.emit(new Values<Object>(p));
//		   Crawler.logEvent("emit " + url);
//	   }
   }
   
   public static String hashUrl(String url) {
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

   public CrawledPage downloadPage(String url) {
	   long start = System.currentTimeMillis();
	   Client client = Client.getClient(url);
	   if(client == null) return null;
	   client.setMethod("GET");
	   if(!client.sendReq() || client.getStatusCode() != 200) {
		   Crawler.logEvent("finished GET: " + url, start);
		   System.out.println("GET req error: " + url);
		   return null;
	   }
	   Crawler.logEvent("finished GET: " + url, start);
	   String contentType = client.getResContentType();
//	   System.out.println(id + " content type: " + contentType);
//	   System.out.println(id + " status code: " + client.getStatusCode());
	   boolean validContentType = (contentType == null) || "text/html".equalsIgnoreCase(contentType)
				|| "text/xml".equalsIgnoreCase(contentType)
				|| "application/xml".equalsIgnoreCase(contentType) 
				|| contentType.matches(".*+xml");
	   
	   if(!validContentType) return null;
	   
	   start = System.currentTimeMillis();
	   byte[] content = null;
	   InputStream in = client.getInputStream();
	   if(in == null) return null;
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
//			e.printStackTrace();
		    client.close();
			return null;
	   }
	   client.close();
	   
	   long time = System.currentTimeMillis() - start;
	   if(time > 8000) {
		   
		   try {
			   bw.write("[********time consuming url: " + url + "**************]: " + time + "ms\n");
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
		   
		   Crawler.logEvent("********time consuming url: " + url + "**************", start);
	   }
	   Crawler.logEvent("finished download: " + url, start);
	   
	   CrawlerWorker.workerStatus.incFileNum();
	   
	   CrawledPage newPage = new CrawledPage(content, url, client.getResContentType());
	   return newPage;
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
    * Used for debug purposes, shows our exeuctor/operator's unique ID
    */
	@Override
	public String getExecutorId() {
		return id;
	}

	/**
	 * Called during topology setup, sets the router to the next
	 * bolt
	 */
	@Override
	public void setRouter(StreamRouter router) {
		this.collector.setRouter(router);
	}

	/**
	 * The fields (schema) of our output stream
	 */
	@Override
	public Fields getSchema() {
		return schema;
	}
	
}