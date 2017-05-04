package crawler.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue<T> implements Queue<T>{
	
	private LinkedList<T> tasks;
	private int capacity;
	
	public TaskQueue(int capacity) {
		tasks = new LinkedList<>();
		this.capacity = capacity;
	}
	
	public TaskQueue() {
		this(5000);
	}
	
	public void addTask(T task) {
		tasks.addFirst(task);
//		System.out.println("[taskQueue]: New task added.");
		notify();	// notify consumer(thread pool)
	}
	
	public T getTask() {
		if(tasks.isEmpty()) return null;
//		System.out.println("[taskQueue]: ready to pop out task");
		T res = tasks.pollLast();
		notifyAll();	// how to just only invoke the producer?
		return res;
	}
	
	public boolean isEmpty() {
		return tasks.isEmpty();
	}
	
	public boolean isFull() {
		return tasks.size() == capacity;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean add(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offer(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T peek() {
		// TODO Auto-generated method stub
		return null;
	}
}
