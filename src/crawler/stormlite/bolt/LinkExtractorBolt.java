package crawler.stormlite.bolt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import crawler.Crawler;
import crawler.client.URLInfo;
import crawler.storage.CrawledPage;
import crawler.storage.DBWrapper;
import crawler.stormlite.OutputFieldsDeclarer;
import crawler.stormlite.TopologyContext;
import crawler.stormlite.routers.StreamRouter;
import crawler.stormlite.tuple.Fields;
import crawler.stormlite.tuple.Tuple;
import crawler.stormlite.tuple.Values;
import crawler.utils.LRUCache;
import crawler.utils.PageCache;
import crawler.worker.CrawlerWorker;
import utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

public class LinkExtractorBolt implements IRichBolt {
    static Logger logger = new Logger(LinkExtractorBolt.class.getName());

    Fields schema = new Fields("host", "links");
    DBWrapper db;
    LRUCache<CrawledPage> pageCache;

    /**
     * To make it easier to debug: we have a unique ID for each instance of the
     * WordCounter, aka each "executor"
     */
    String id = "[LE]" + UUID.randomUUID().toString();

    /**
     * This is where we send our output stream
     */
    private OutputCollector collector;

    public LinkExtractorBolt() {
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @SuppressWarnings("unchecked")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        
        db = new DBWrapper(Crawler.DBPath);
        db.setup();
    }

    /**
     * Process a tuple received from the stream, incrementing our counter and
     * outputting a result
     */
    @Override
    public void execute(Tuple input) {
        CrawledPage page = (CrawledPage) input.getObjectByField("page");
//         System.out.println(id + " got " + page.getUrl());
        String url = page.getUrl();
        if(url == null) return;
        String host = new URLInfo(url).getHostName();
        
        if ("text/html".equals(page.getContentType())) {
            byte[] content = page.getContent();
            Document doc = Jsoup.parse(new String(content), url);
            Elements links = doc.select("a[href]");
            if(links.size() == 0) return;
            for (Element link : links) {
                String l = link.attr("abs:href");
                
                if(url != null && !url.equals(l)) {
                	if (l == null || l.length() == 0)
                        continue;
                    // System.out.println(id + " emit " + l);
                	page.addLink(l);
                }
            }
            System.out.println(id + " emit links");
            this.collector.emit(new Values<Object>(host, page.getLinks()));
        }
        
//        pageCache.put(page.getUrl(), page);
        CrawlerWorker.workerStatus.incFileNum();
        db.savePage(page);
        db.sync();
        Crawler.logEvent("finished parsing: " + url);
//		System.out.println(id + ": " + url + " downloaded ");
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
     * Called during topology setup, sets the router to the next bolt
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