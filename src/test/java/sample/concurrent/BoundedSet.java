package sample.concurrent;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;

public class BoundedSet<E> extends ForwardingSet<E> {
	private final Set<E> set;
	private final int permits;
	private final Semaphore semaphore;

	public BoundedSet(int permits) {
		this.set = Sets.newCopyOnWriteArraySet();
		this.permits = permits;
		this.semaphore = new Semaphore(permits);
	}

	@Override
	protected Set<E> delegate() {
		return this.set;
	}

	@Override
	public boolean add(E element) {
		try {
			this.semaphore.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		boolean isAdded = false;
		try {
			isAdded = this.set.add(element);
			return isAdded;
		} finally {
			if (!isAdded) {
				this.semaphore.release();
			}
		}
	}

	@Override
	public boolean remove(Object object) {
		boolean isRemoved = false;
		try {
			isRemoved = this.set.remove(object);
			return isRemoved;
		} finally {
			if (isRemoved) {
				this.semaphore.release();
			}
		}
	}

	@Override
	public void clear() {
		try {
			super.clear();
		} finally {
			this.semaphore.release(permits);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}
}
