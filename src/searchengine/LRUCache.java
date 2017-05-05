package searchengine;

import java.util.HashMap;

/**
 * Created by QingxiaoDong on 5/4/17.
 * Least Recently Used Cache.
 * get(key) and put(key, value) are both O(1) operations.
 */
public class LRUCache {

    /**
     * Linked list node.
     * Stores the original result entries and the sorted result entries.
     */
    private class Node {
        private String key;
        private ResultEntry[] original;
        private ResultEntry[] sorted;
        Node prev;
        Node next;
        public Node (String key, ResultEntry[] original) {
            this.key = key;
            this.original = original;
            this.sorted = new ResultEntry[original.length];
            prev = null;
            next = null;
        }
    }

    private int capacity;
    private HashMap<String, Node> map;
    private Node head;
    private Node tail;

    /**
     * When the cache reached its capacity, it should invalidate the least recently
     * used item before inserting a new item.
     * @param capacity capacity of the LRU cache
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<String, Node>();
        head = new Node(null, new ResultEntry[0]);
        tail = new Node(null, new ResultEntry[0]);
        head.next = tail;
        tail.prev = head;
    }

    /**
     * Get the original result entries of the key(query) if the key exists in the cache.
     * @param key key of the result entries
     * @return original result entries or null
     */
    public ResultEntry[] getOriginal(String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Node current = map.get(key);
        remove(current);
        add(current);
        return map.get(key).original;
    }

    /**
     * Get the sorted result entries of the key(query) if the key exists in the cache.
     * @param key key of the result entries
     * @return sorted result entries or null
     */
    public ResultEntry[] getSorted(String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Node current = map.get(key);
        remove(current);
        add(current);
        return map.get(key).sorted;
    }

    /**
     * Set or insert the result entries if the key is not already present. When the cache reached
     * its capacity, it should invalidate the least recently used item before inserting a new item.
     * @param key key of the result entries
     * @param original original result entries
     */
    public void put(String key, ResultEntry[] original) {
        if (getOriginal(key) != null) {
            Node current = map.get(key);
            current.original = original;
            current.sorted = new ResultEntry[original.length];
        } else {
            if (map.size() == capacity) {
                Node lfu = head.next;
                map.remove(lfu.key);
                remove(lfu);
            }
            Node newNode = new Node(key, original);
            add(newNode);
            map.put(key, newNode);
        }
    }


    /******************************************
     * Helper functions                       *
     ******************************************/

    private void remove(Node current) {
        current.prev.next = current.next;
        current.next.prev = current.prev;
    }

    private void add(Node current) {
        tail.prev.next = current;
        current.prev = tail.prev;
        current.next = tail;
        tail.prev = current;
    }
}
