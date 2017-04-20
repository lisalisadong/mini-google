package crawler;

import java.util.Map;
import java.util.UUID;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import crawler.stormlite.*;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.spout.IRichSpout;
import crawler.stormlite.spout.SpoutOutputCollector;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Values;
import crawler.urlfrontier.URLFrontier;

/**
 * URLFrontier
 * 
 */
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
public class URLSpout implements IRichSpout {
	static Logger log = Logger.getLogger(URLSpout.class);

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordSpout, aka each "executor"
     */
    String id = UUID.randomUUID().toString();
    
    Fields schema = new Fields("url");
    
    private URLFrontier URLFrontier = new URLFrontier();

    /**
	 * The collector is the destination for tuples; you "emit" tuples there
	 */
	SpoutOutputCollector collector;
	
	int cnt = 0;
	

    public URLSpout() {
    	log.debug("Starting spout");
    }


    /**
     * Initializes the instance of the spout (note that there can be multiple
     * objects instantiated)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    /**
     * Shut down the spout
     */
    @Override
    public void close() {
    	
    }

    /**
     * The real work happens here, in incremental fashion.  We process and output
     * the next item(s).  They get fed to the collector, which routes them
     * to targets
     */
    @Override
    public void nextTuple() {
//    	String url = URLFrontier.getNextURL();
//    	
//    	if(url != null) this.collector.emit(new Values<Object>(url));
//    	
    	String url = cnt++ + "hello world!";
//    	System.out.println(id + ": " + url);
    	if(cnt < 3) this.collector.emit(new Values<Object>(url));
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
        Thread.yield();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }


	@Override
	public String getExecutorId() {
		return id;
	}


	@Override
	public void setRouter(StreamRouter router) {
		this.collector.setRouter(router);
	}

}
