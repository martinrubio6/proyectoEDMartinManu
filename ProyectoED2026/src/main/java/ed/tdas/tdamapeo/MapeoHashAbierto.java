package ed.tdas.tdamapeo;

import java.util.Iterator;

import ed.tdas.Entrada;
import ed.tdas.Entry;
import ed.tdas.Position;
import ed.tdas.excepciones.InvalidKeyException;
import ed.tdas.excepciones.InvalidPositionException;
import ed.tdas.tdalista.DoubleLinkedList;
import ed.tdas.tdalista.PositionList;

public class MapeoHashAbierto<K, V> implements Map<K, V> {

	protected PositionList<Entrada<K, V>>[] A;
	protected int N;
	protected int n;
	private static final double fc = 0.9;

	private int hash(K key) {
		return Math.abs(key.hashCode() % N);
	}

	@SuppressWarnings("unchecked")
	public MapeoHashAbierto() {
		N = 13;
		n = 0;
		A = (PositionList<Entrada<K, V>>[]) new PositionList[N];
		for (int i = 0; i < N; i++) {
			A[i] = new DoubleLinkedList<Entrada<K, V>>();
		}
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public boolean isEmpty() {
		return n == 0;
	}

	@Override
	public V get(K key) throws InvalidKeyException {
		checkKey(key);
		V ret = null;
		int clave = hash(key);
		Iterator<Entrada<K, V>> it = A[clave].iterator();
		boolean esta = false;

		while (!esta && it.hasNext()) {
			Entrada<K, V> act = it.next();
			if (act.getKey().equals(key)) {
				ret = act.getValue();
				esta = true;
			}
		}
		return ret;
	}

	@Override
	public V put(K key, V value) throws InvalidKeyException {
		checkKey(key);
		V ret = null;
		int clave = hash(key);
		Iterator<Entrada<K, V>> it = A[clave].iterator();
		boolean esta = false;

		while (!esta && it.hasNext()) {
			Entrada<K, V> act = it.next();
			if (key.equals(act.getKey())) {
				esta = true;
				ret = act.getValue();
				act.setValue(value);
			}
		}

		if (!esta) {
			Entrada<K, V> e = new Entrada<K, V>(key, value);
			A[clave].addLast(e);
			n++;
		}

		if ((double) n / N > fc) {
			agrandarTabla();
		}

		return ret;
	}

	@Override
	public V remove(K key) throws InvalidKeyException {
		checkKey(key);
		V valor = null;
		int clave = hash(key);
		Iterator<Position<Entrada<K, V>>> it = A[clave].positions().iterator();
		boolean esta = false;

		try {
			while (!esta && it.hasNext()) {
				Position<Entrada<K, V>> pos = it.next();
				if (pos.element().getKey().equals(key)) {
					valor = pos.element().getValue();
					esta = true;
					A[clave].remove(pos);
					n--;
				}
			}
		} catch (InvalidPositionException e) {
			e.printStackTrace();
		}

		return valor;
	}

	@Override
	public Iterable<K> keys() {
		PositionList<K> lista = new DoubleLinkedList<K>();
		for (int i = 0; i < N; i++) {
			for (Entrada<K, V> en : A[i]) {
				lista.addLast(en.getKey());
			}
		}
		return lista;
	}

	@Override
	public Iterable<V> values() {
		PositionList<V> lista = new DoubleLinkedList<V>();
		for (int i = 0; i < N; i++) {
			for (Entrada<K, V> en : A[i]) {
				lista.addLast(en.getValue());
			}
		}
		return lista;
	}

	@Override
	public Iterable<Entry<K, V>> entries() {
		PositionList<Entry<K, V>> lista = new DoubleLinkedList<Entry<K, V>>();
		for (int i = 0; i < N; i++) {
			for (Entrada<K, V> en : A[i]) {
				lista.addLast(en);
			}
		}
		return lista;
	}

	@SuppressWarnings("unchecked")
	private void agrandarTabla() {
		N = proximoPrimo(N * 2);
		PositionList<Entrada<K, V>>[] T = (PositionList<Entrada<K, V>>[]) new PositionList[N];
		for (int i = 0; i < N; i++) {
			T[i] = new DoubleLinkedList<Entrada<K, V>>();
		}

		for (int i = 0; i < A.length; i++) {
			for (Entrada<K, V> e : A[i]) {
				int p = hash(e.getKey());
				T[p].addLast(e);
			}
		}
		A = T;
	}

	private void checkKey(K key) throws InvalidKeyException {
		if (key == null) {
			throw new InvalidKeyException("Clave invalida");
		}
	}

	private int proximoPrimo(int num) {
		num++;
		while (!esPrimo(num)) {
			num++;
		}
		return num;
	}

	private boolean esPrimo(int num) {
		if (num <= 1) {
			return false;
		}
		for (int divisor = 2; divisor * divisor <= num; divisor++) {
			if (num % divisor == 0) {
				return false;
			}
		}
		return true;
	}
}