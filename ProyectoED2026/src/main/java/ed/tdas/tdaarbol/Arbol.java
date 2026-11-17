package ed.tdas.tdaarbol;

import java.util.Iterator;

import ed.tdas.Position;
import ed.tdas.excepciones.BoundaryViolationException;
import ed.tdas.excepciones.EmptyListException;
import ed.tdas.excepciones.EmptyTreeException;
import ed.tdas.excepciones.InvalidOperationException;
import ed.tdas.excepciones.InvalidPositionException;
import ed.tdas.tdalista.DoubleLinkedList;
import ed.tdas.tdalista.PositionList;



public class Arbol<E> implements Tree<E> {

	protected TNodo<E> raiz;
	protected int size;

	public Arbol() {
		raiz = null;
		size = 0;
	}

	private TNodo<E> checkPosition(Position<E> p) throws InvalidPositionException {
		try {
			if (p == null || isEmpty())
				throw new InvalidPositionException("Posición nula");
			return (TNodo<E>) p;
		} catch (ClassCastException e) {
			throw new InvalidPositionException("Clase casteada no es de tipo Position");
		}
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return raiz == null;
	}

	public Iterator<E> iterator() {
		PositionList<E> list = new DoubleLinkedList<E>();
		if (!isEmpty())
			preordenIT(list, raiz);

		return list.iterator();
	}

	public Iterable<Position<E>> positions() {

		PositionList<Position<E>> list = new DoubleLinkedList<Position<E>>();
		if (!isEmpty())
			preorden(list, raiz);
		return list;
	}

	public E replace(Position<E> v, E e) throws InvalidPositionException {
		TNodo<E> pos = checkPosition(v);
		E removed = pos.element();
		pos.setElemento(e);

		return removed;
	}

	public Position<E> root() throws EmptyTreeException {
		if (raiz == null)
			throw new EmptyTreeException("Arbol vacio");
		return raiz;
	}

	public Position<E> parent(Position<E> v) throws InvalidPositionException, BoundaryViolationException {

		TNodo<E> pos = checkPosition(v);
		TNodo<E> padre = pos.getPadre();

		if (pos == raiz)
			throw new BoundaryViolationException("No tiene padre");
		return padre;
	}

	public Iterable<Position<E>> children(Position<E> v) throws InvalidPositionException {
		TNodo<E> pos = checkPosition(v);
		PositionList<Position<E>> list = new DoubleLinkedList<Position<E>>();
		for (TNodo<E> elem : pos.getHijos())
			list.addLast(elem);
		return list;
	}

	public boolean isInternal(Position<E> v) throws InvalidPositionException {

		TNodo<E> pos = checkPosition(v);

		return !pos.getHijos().isEmpty();
	}

	public boolean isExternal(Position<E> v) throws InvalidPositionException {

		TNodo<E> pos = checkPosition(v);

		return pos.getHijos().isEmpty();
	}

	public boolean isRoot(Position<E> v) throws InvalidPositionException {

		TNodo<E> pos = checkPosition(v);

		return pos.getPadre() == null;
	}

	public void createRoot(E e) throws InvalidOperationException {
		if (raiz != null)
			throw new InvalidOperationException("El arbol ya tiene raiz");
		TNodo<E> nodo = new TNodo<E>(e);
		raiz = nodo;
		size++;
	}

	public Position<E> addFirstChild(Position<E> p, E e) throws InvalidPositionException {

		TNodo<E> padre = checkPosition(p);
		TNodo<E> nuevo = new TNodo<E>(e, padre);
		padre.getHijos().addFirst(nuevo);
		size++;
		return nuevo;
	}

	public Position<E> addLastChild(Position<E> p, E e) throws InvalidPositionException {
		TNodo<E> padre = checkPosition(p);
		TNodo<E> nuevo = new TNodo<E>(e, padre);
		padre.getHijos().addLast(nuevo);
		size++;
		return nuevo;
	}

	public Position<E> addBefore(Position<E> p, Position<E> rb, E e) throws InvalidPositionException {

		TNodo<E> padre = checkPosition(p);
		TNodo<E> hermano = checkPosition(rb);
		if (hermano.getPadre() != padre)
			throw new InvalidPositionException("p no es padre de rb");
		TNodo<E> nuevo = new TNodo<E>(e, padre);
		PositionList<TNodo<E>> hijosPadre = padre.getHijos();
		boolean es = false;
		try {
			Position<TNodo<E>> pp = hijosPadre.first();
			while (pp != null && !es)
				if (hermano == pp.element())
					es = true;
				else
					pp = (pp != hijosPadre.last()) ? hijosPadre.next(pp) : null;

			hijosPadre.addBefore(pp, nuevo);
			size++;
		} catch (EmptyListException | InvalidPositionException | BoundaryViolationException ex) {
			ex.getMessage();
		}

		return nuevo;
	}

	public Position<E> addAfter(Position<E> p, Position<E> lb, E e) throws InvalidPositionException {

		TNodo<E> padre = checkPosition(p);
		TNodo<E> hermano = checkPosition(lb);

		if (hermano.getPadre() != padre)
			throw new InvalidPositionException("p no es padre de lb");

		TNodo<E> nuevo = new TNodo<E>(e, padre);
		PositionList<TNodo<E>> hijosPadre = padre.getHijos();
		boolean es = false;
		try {
			Position<TNodo<E>> pp = hijosPadre.first();
			while (pp != null && !es)
				if (hermano == pp.element())
					es = true;
				else
					pp = (pp != hijosPadre.last()) ? hijosPadre.next(pp) : null;

			hijosPadre.addAfter(pp, nuevo);
			size++;
		} catch (EmptyListException | InvalidPositionException | BoundaryViolationException ex) {
			ex.getMessage();
		}

		return nuevo;
	}

	public void removeExternalNode(Position<E> p) throws InvalidPositionException {
		if (isInternal(p))
			throw new InvalidPositionException("No es un nodo externo.");
		TNodo<E> nodo = checkPosition(p);
		try {
			if (nodo == raiz) {
				raiz = null;
				size = 0;
			} else {
				TNodo<E> padre = nodo.getPadre();
				PositionList<TNodo<E>> hijos = padre.getHijos();
				Position<TNodo<E>> pos = null;
				boolean encontre = false;
				Iterator<Position<TNodo<E>>> it = hijos.positions().iterator();
				while (it.hasNext() && !encontre) {
					pos = it.next();
					if (pos.element() == nodo) {
						encontre = true;
						hijos.remove(pos);
						nodo.setPadre(null);
						nodo.setElemento(null);
						size--;
					}
				}
				if (!encontre) {
					throw new EmptyListException("P no pertenece al arbol.");
				}
			}
		} catch (EmptyListException e) {
			System.out.println(e.getMessage());
		}

	}

	public void removeInternalNode(Position<E> p) throws InvalidPositionException {

		TNodo<E> nodo = checkPosition(p);
		if (isExternal(p))
			throw new InvalidPositionException("p no es un nodo interno");
		if (nodo == raiz) {
			if (nodo.getHijos().size() > 1)
				throw new InvalidPositionException("el nodo raiz a eliminar no puede tener mas de 1 hijo");
			else {
				try {
					raiz = nodo.getHijos().first().element();
					nodo.setElemento(null);
					nodo.getHijos().remove(nodo.getHijos().first());
					raiz.setPadre(null);
				} catch (EmptyListException e) {
					System.out.println(e.getMessage());
				}
			}
		} else {
			TNodo<E> pa = nodo.getPadre();
			PositionList<TNodo<E>> hijos = pa.getHijos();
			Iterator<Position<TNodo<E>>> it = hijos.positions().iterator();
			Position<TNodo<E>> me = null;
			boolean encontre = false;
			while (it.hasNext() && !encontre) {
				me = it.next();
				encontre = me.element() == nodo;
			}
			if (!encontre)
				throw new InvalidPositionException("N no figura como hijo de su padre");
			PositionList<TNodo<E>> hijosN = nodo.getHijos();
			it = hijosN.positions().iterator();
			while (it.hasNext()) {
				TNodo<E> nh = it.next().element();
				nh.setPadre(pa);
				hijos.addBefore(me, nh);
			}
			hijos.remove(me);
			nodo.setPadre(null);
			nodo.setElemento(null);
			it = hijosN.positions().iterator();
			while (it.hasNext())
				hijosN.remove(it.next());
		}
		size--;
	}

	public void removeNode(Position<E> p) throws InvalidPositionException {
		TNodo<E> nEliminar = checkPosition(p);
		TNodo<E> padre = nEliminar.getPadre();
		PositionList<TNodo<E>> hijos = nEliminar.getHijos();
		try {
			if (nEliminar == raiz) {
				if (hijos.size() == 0) {
					raiz = null;
				} else {
					if (hijos.size() == 1) {
						TNodo<E> hijo = hijos.remove(hijos.first());
						hijo.setPadre(null);
						raiz = hijo;
					} else
						throw new InvalidPositionException("No se puede eliminar ra�z con hijos > 1");
				}
			} else {
				PositionList<TNodo<E>> hermanos = padre.getHijos();
				
				Position<TNodo<E>> posListaHermanos = hermanos.isEmpty() ? null : hermanos.first();
				while (posListaHermanos != null && posListaHermanos.element() != nEliminar) {
					posListaHermanos = (hermanos.last() == posListaHermanos) ? null : hermanos.next(posListaHermanos);
				}
				
				if (posListaHermanos == null)
					throw new InvalidPositionException("La posici�n p no se encuentra en la lista del padre");
				
				while (!hijos.isEmpty()) {
					TNodo<E> hijo = hijos.remove(hijos.first());
					hijo.setPadre(padre);
					hermanos.addBefore(posListaHermanos, hijo);
				}
				hermanos.remove(posListaHermanos);
			} 
			nEliminar.setPadre(null);
			nEliminar.setElemento(null);
			size--;
		} catch (EmptyListException | BoundaryViolationException e) {
		}
	}

	private void preorden(PositionList<Position<E>> l, TNodo<E> r) {
		l.addLast(r);
		for (TNodo<E> h : r.getHijos())
			preorden(l, h);
	}

	private void preordenIT(PositionList<E> list, TNodo<E> r) {
		list.addLast(r.element());
		for (TNodo<E> h : r.getHijos())
			preordenIT(list, h);
	}

}
