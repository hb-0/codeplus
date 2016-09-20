package codeplus.util.cache;

public interface Computable<K, V> {
	V compute(K key) throws InterruptedException;
}
