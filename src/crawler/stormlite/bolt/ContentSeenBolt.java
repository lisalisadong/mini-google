package crawler.stormlite.bolt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import crawler.Crawler;
import crawler.client.Client;
import crawler.robots.RobotInfoManager;
import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.stormlite.tuple.Values;
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
	
	DBWrapper db;
	
	
   /**
    * To make it easier to debug: we have a unique ID for each
    * instance of the WordCounter, aka each "executor"
    */
   String id = "[CS]" + UUID.randomUUID().toString();
   
   /**
    * This is where we send our output stream
    */
   private OutputCollector collector;
   
   private RobotInfoManager robotManager;
   
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
       db = new DBWrapper(Crawler.DBPath);
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
	   
	   /* download the page*/
	   Client client = Client.getClient(url);
	   client.setMethod("GET");
	   robotManager.waitUntilAvailable(url);	
	   /* set last access time */
	   robotManager.setHostLastAccessTime(url);
	   client.sendReq();
	   
	   byte[] content = null;
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
	   
	   
	   client.close();
	   
	   // TODO: 
	   /* deal with the content seen */
	   boolean contentSeen = false;
	   
	   if(!contentSeen) {
//		   System.out.println(id + " emit " + url);
		   System.out.println(id + ": " + url + " downloaded ");
		   
		   CrawledPage newPage = new CrawledPage(content, url, client.getResContentType());
		   newPage.setLastCrawled(System.currentTimeMillis());
		   this.collector.emit(new Values<Object>(newPage));
	   } else {
		   // TODO: get url via fp and increase the hit
	   }
	   
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