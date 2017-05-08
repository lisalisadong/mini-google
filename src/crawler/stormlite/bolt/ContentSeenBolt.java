package crawler.stormlite.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import crawler.utils.LRUCache;
import crawler.client.Client;
import crawler.robots.RobotInfoManager;
import crawler.robots.RobotInfoManager_old;
import crawler.robots.RobotManager;
import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.stormlite.tuple.Values;
import crawler.worker.CrawlerWorker;
import crawler.storage.*;
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

public class ContentSeenBolt implements IRichBolt {

	static Logger logger = new Logger(ContentSeenBolt.class.getName());
	
	Fields schema = new Fields("page");
	
	long maxSize = Long.MAX_VALUE;
//	AmazonS3 awsClient;
//    String BUCKET = "crawler-indexer-g02";
//
//    String accessKey = "AKIAJBEVSUPUI2OHEX6Q";
//    String secretKey = "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";
	
   /**
    * To make it easier to debug: we have a unique ID for each
    * instance of the WordCounter, aka each "executor"
    */
   String id = "[CS]" + UUID.randomUUID().toString();
   
   /**
    * This is where we send our output stream
    */
   private OutputCollector collector;
   
   private RobotManager robotManager;
   
   public ContentSeenBolt() {
   }
   
   /**
    * Initialization, just saves the output stream destination
    */
   @SuppressWarnings("unchecked")
   @Override
   public void prepare(Map stormConf, 
   		TopologyContext context, OutputCollector collector) 
   {
       this.collector = collector;
       robotManager = Crawler.getRobotManager();
       
//       AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//
//       ClientConfiguration clientConfig = new ClientConfiguration();
//       clientConfig.setProtocol(Protocol.HTTP);
//
//       try {
//           awsClient = new AmazonS3Client(credentials, clientConfig);
//       } catch (Exception e) {
//
//       }
   }
   
//   private String hashUrl(String url) {
//       try {
//           MessageDigest digest = MessageDigest.getInstance("MD5");
//           digest.reset();
//           digest.update(url.getBytes("utf-8"));
//           return DatatypeConverter.printHexBinary(digest.digest());
//       } catch (NoSuchAlgorithmException e) {
//           e.printStackTrace();
//       } catch (UnsupportedEncodingException e) {
//           e.printStackTrace();
//       }
//       return null;
//   }

   /**
    * Process a tuple received from the stream, incrementing our
    * counter and outputting a result
    */
   @Override
   public void execute(Tuple input) 
   {
	   // This bolt is useless
   }
   
   /**
    * TODO: check content seen
    * @param content
    * @return
    */
   private boolean checkContentSeen(byte[] content) {
	   return false;
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