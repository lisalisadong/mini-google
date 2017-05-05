package searchengine;

import java.util.HashMap;

/**
 * Created by QingxiaoDong on 5/4/17.
 */
public class LRUCache {

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

    public LRUCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<String, Node>();
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public ResultEntry[] getOriginal(String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Node current = map.get(key);
        remove(current);
        add(current);
        return map.get(key).original;
    }

    public ResultEntry[] getSorted(String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Node current = map.get(key);
        remove(current);
        add(current);
        return map.get(key).sorted;
    }

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
