package sample.concurrent;

import com.google.common.collect.Lists;

import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BoundedList<E> {
  private final int limit;
  private final Deque<E> deque;
  private final ReadWriteLock readWriteLock;
  private final Condition notEmpty;
  private final Condition notFull;


  public BoundedList() {
    this(20);
  }

  public BoundedList(int limit) {
    this.limit = limit;
    this.deque = Lists.newLinkedList();
    this.readWriteLock = new ReentrantReadWriteLock();
    this.notEmpty = this.readWriteLock.writeLock().newCondition();
    this.notFull = this.readWriteLock.writeLock().newCondition();
  }

  public void addFirst(E element) throws InterruptedException {
    this.readWriteLock.writeLock().lock();
    try {
      while (this.deque.size() == limit)
        this.notFull.await();
      this.deque.addFirst(element);
      this.notEmpty.signal();
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public void addLast(E element) throws InterruptedException {
    this.readWriteLock.writeLock().lock();
    try {
      while (this.deque.size() == limit)
        this.notFull.await();
      this.deque.addLast(element);
      this.notEmpty.signal();
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public E removeFirst() throws InterruptedException {
    this.readWriteLock.writeLock().lock();
    try {
      while (this.deque.size() == 0)
        this.notEmpty.await();
      E element = this.deque.removeFirst();
      this.notFull.signal();
      return element;
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public E removeLast() throws InterruptedException {
    this.readWriteLock.writeLock().lock();
    try {
      while (this.deque.size() == 0)
        this.notEmpty.await();
      E element = this.deque.removeLast();
      this.notFull.signal();
      return element;
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public boolean remove(E element) throws InterruptedException {
    this.readWriteLock.writeLock().lock();
    try {
      boolean isRemoved = this.deque.remove(element);
      if (isRemoved)
        this.notFull.signal();
      return isRemoved;
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public E peekFirst() throws InterruptedException {
    this.readWriteLock.readLock().lock();
    try {
      return this.deque.peekFirst();
    } finally {
      this.readWriteLock.readLock().unlock();
    }
  }

  public E peekLast() throws InterruptedException {
    this.readWriteLock.readLock().lock();
    try {
      return this.deque.peekLast();
    } finally {
      this.readWriteLock.readLock().unlock();
    }
  }

}
