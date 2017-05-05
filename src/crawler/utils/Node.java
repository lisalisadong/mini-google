package crawler.utils;

public class Node<T> {
	
	public String key;
	public T val;
	
	public Node<T> prev;
	public Node<T> next;
	
	public Node(String key, T val) {
		this.key = key;
		this.val = val;
	}
}
