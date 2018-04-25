package com.pocket.network;

import android.os.Handler;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpPool
{
	private final WeakHashMap<Object, ConcurrentHashMap<Integer, Future<?>>> futures = new WeakHashMap<Object, ConcurrentHashMap<Integer, Future<?>>>();
	private final ConcurrentLinkedQueue<QueuedTask> queueOfTasks = new ConcurrentLinkedQueue<QueuedTask>();
	private ThreadPoolExecutor pool;
	private static final int THREADS_SPARE = 5;
	public static final Handler handler = new Handler();

	public HttpPool()
	{
		this.pool = new ThreadPoolExecutor(0, 50, 60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>())
		{
			protected void afterExecute(Runnable r, Throwable t)
			{
				for (int i = 0; i < 1; i++)
				{
					QueuedTask qt = (QueuedTask) HttpPool.this.queueOfTasks.poll();
					if (qt != null)
					{
						handler.post(qt);
					}
				}
			}
		};
		this.pool.setThreadFactory(new MyThreadFactory());
	}

	public void submit(Runnable task, Object container, int taskId)
	{
		if (this.pool.getActiveCount() > this.pool.getMaximumPoolSize() - THREADS_SPARE)
		{
			this.queueOfTasks.add(new QueuedTask(task, container, taskId));
		} 
		else
		{
			submitToPool(task, container, taskId);
		}
	}

	private void submitToPool(Runnable task, Object container, int taskId)
	{
		Future<?> f=null;
		try
		{
			f = this.pool.submit(task);
		} 
		catch (RejectedExecutionException ree)
		{
			try
			{
				Thread.sleep(100L);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			submitToPool(task, container, taskId);
		}
		
		ConcurrentHashMap<Integer, Future<?>> map=null;
		synchronized (this.futures)
		{
			map = (ConcurrentHashMap<Integer, Future<?>>) this.futures.get(container);
			if (map == null)
			{
				map = new ConcurrentHashMap<Integer, Future<?>>();
				this.futures.put(container, map);
			}
		}
		
		for (Iterator<Future<?>> it = map.values().iterator(); it.hasNext();)
		{
			Future<?> fit = (Future<?>) it.next();
			if (fit.isDone())
			{
				it.remove();
			}
		}
		map.put(Integer.valueOf(taskId), f);
	}

	public boolean isRunning(Object container, int taskId)
	{
		ConcurrentHashMap<Integer, Future<?>> map = (ConcurrentHashMap<Integer, Future<?>>) this.futures.get(container);
		if (map == null)
		{
			return false;
		}
		Future<?> f = (Future<?>) map.get(Integer.valueOf(taskId));
		if (f == null)
		{
			return false;
		}
		return !f.isDone();
	}

	public void markTaskAsFinished(Object container, int taskId)
	{
		ConcurrentHashMap<Integer, Future<?>> map = (ConcurrentHashMap<Integer, Future<?>>) this.futures.get(container);
		if (map == null)
		{
			return;
		}
		map.remove(Integer.valueOf(taskId));
	}

	private static class MyThreadFactory implements ThreadFactory
	{
		private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

		public Thread newThread(Runnable r)
		{
			Thread t = this.defaultFactory.newThread(r);
			t.setDaemon(true);
			return t;
		}
	}

	class QueuedTask implements Runnable
	{
		final Runnable task;
		final Object container;
		final int taskId;

		public QueuedTask(Runnable task, Object container, int taskId)
		{
			this.task = task;
			this.container = container;
			this.taskId = taskId;
		}

		public void run()
		{
			if (HttpPool.this.pool.getActiveCount() > HttpPool.this.pool.getMaximumPoolSize() - 5)
			{
				handler.postDelayed(this, 50L);
			} 
			else
			{
				HttpPool.this.submitToPool(this.task, this.container,this.taskId);
			}
		}
	}
}
