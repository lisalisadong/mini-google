package crawler.stormlite.bolt;

import java.util.Map;
import java.util.UUID;

import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.stormlite.tuple.Values;
import utils.Logger;

/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

public class HTTPModuleBolt implements IRichBolt {
	static Logger logger = new Logger(HTTPModuleBolt.class.getName());
	
	Fields schema = new Fields("url");
//	private RobotInfoManager robotInfoManager;
	
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
   @SuppressWarnings("unchecked")
   @Override
   public void prepare(Map stormConf, 
   		TopologyContext context, OutputCollector collector) 
   {
       this.collector = collector;
   }

   /**
    * Process a tuple received from the stream, incrementing our
    * counter and outputting a result
    */
   @Override
   public void execute(Tuple input) 
   {
	   String url = input.getStringByField("url");
	   System.out.println(id + " Got " + url);
	   this.collector.emit(new Values<Object>(url));
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