package ed.tdas.tdadiccionario;

import java.util.Iterator;

import ed.tdas.Entrada;
import ed.tdas.Entry;
import ed.tdas.Position;
import ed.tdas.excepciones.InvalidEntryException;
import ed.tdas.excepciones.InvalidKeyException;
import ed.tdas.excepciones.InvalidPositionException;
import ed.tdas.tdalista.DoubleLinkedList;
import ed.tdas.tdalista.PositionList;

public class DiccionarioHashAbierto<K, V> implements Dictionary<K, V> {

	protected PositionList<Entrada<K, V>>[] A;
	protected int n;
	protected int N;
	protected static final double fc = 0.9;

	@SuppressWarnings("unchecked")
	public DiccionarioHashAbierto() {
		N = 13;
		n = 0;
		A = (PositionList<Entrada<K, V>>[]) new PositionList[N];
		for (int i = 0; i < N; i++) {
			A[i] = new DoubleLinkedList<Entrada<K, V>>();
		}
	}

	private int H(K key) {
		return Math.abs(key.hashCode() % N);
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
	public Entry<K, V> find(K key) throws InvalidKeyException {
		checkKey(key);
		Entry<K, V> ret = null;
		int clave = H(key);
		Iterator<Entrada<K, V>> it = A[clave].iterator();
		boolean esta = false;

		while (!esta && it.hasNext()) {
			Entrada<K, V> act = it.next();
			if (key.equals(act.getKey())) {
				esta = true;
				ret = act;
			}
		}
		return ret;
	}

	@Override
	public Iterable<Entry<K, V>> findAll(K key) throws InvalidKeyException {
		checkKey(key);
		PositionList<Entry<K, V>> lista = new DoubleLinkedList<Entry<K, V>>();
		int clave = H(key);

		for (Entry<K, V> elem : A[clave]) {
			if (key.equals(elem.getKey())) {
				lista.addLast(elem);
			}
		}
		return lista;
	}

	@Override
	public Entry<K, V> insert(K key, V value) throws InvalidKeyException {
		checkKey(key);

		if ((double) n / N >= fc) {
			rehash();
		}
		int clave = H(key);
		Entrada<K, V> nueva = new Entrada<K, V>(key, value);
		A[clave].addLast(nueva);
		n++;
		return nueva;
	}

	@Override
	public Entry<K, V> remove(Entry<K, V> e) throws InvalidEntryException {
		if (e == null || e.getKey() == null) {
			throw new InvalidEntryException("Entrada Invalida");
		}
		int clave = H(e.getKey());
		PositionList<Entrada<K, V>> l = A[clave];
		Iterator<Position<Entrada<K, V>>> it = l.positions().iterator();
		Entry<K, V> salida = null;

		while (it.hasNext() && salida == null) {
			Position<Entrada<K, V>> cursor = it.next();
			if (cursor.element() == e) {
				salida = cursor.element();
				try {
					l.remove(cursor);
				} catch (InvalidPositionException ex) {
					ex.printStackTrace();
				}
				n--;
			}
		}

		if (salida == null) {
			throw new InvalidEntryException("La entrada no se encuentra en el diccionario");
		}

		return salida;
	}

	@Override
	public Iterable<Entry<K, V>> entries() {
		PositionList<Entry<K, V>> lista = new DoubleLinkedList<Entry<K, V>>();
		for (int i = 0; i < N; i++) {
			for (Entry<K, V> en : A[i]) {
				lista.addLast(en);
			}
		}
		return lista;
	}

	private void checkKey(K key) throws InvalidKeyException {
		if (key == null) {
			throw new InvalidKeyException("Clave invalida");
		}
	}

	@SuppressWarnings("unchecked")
	private void rehash() {
		Iterable<Entry<K, V>> entradas = entries();
		N = proximoPrimo(N * 2);
		A = (PositionList<Entrada<K, V>>[]) new PositionList[N];
		n = 0;
		for (int i = 0; i < N; i++) {
			A[i] = new DoubleLinkedList<Entrada<K, V>>();
		}
		for (Entry<K, V> e : entradas) {
			try {
				insert(e.getKey(), e.getValue());
			} catch (InvalidKeyException ex) {
				ex.printStackTrace();
			}
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