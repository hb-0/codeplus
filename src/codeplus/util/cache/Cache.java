package codeplus.util.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class Cache<K, V> implements Computable<K, V> {
	private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<K, Future<V>>();
	private final Computable<K, V> c;

	public Cache(Computable<K, V> c) {
		this.c = c;
	}

	@Override
	public synchronized V compute(final K key) throws InterruptedException {
		Future<V> f = cache.get(key);
		if (f == null) {
			Callable<V> eval = new Callable<V>() {
				@Override
				public V call() throws Exception {
					return c.compute(key);
				}
			};
			FutureTask<V> ft = new FutureTask<V>(eval);
			f = cache.putIfAbsent(key, ft);
			if (f == null) {
				f = ft;
				ft.run();// 实际运算发生在这
			}
		}
		try {
			return f.get();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

}
