package ed.tdas.tdapila;

public class Nodo<E> {

	private E elemento;
	private Nodo<E> siguiente;

	public Nodo() {
		elemento = null;
		siguiente = null;
	}

	public E getElem() {
		return elemento;
	}

	public void setElem(E elem) {
		elemento = elem;
	}

	public Nodo<E> getSig() {
		return siguiente;
	}

	public void setSig(Nodo<E> e) {
		siguiente=e;
	}
}

