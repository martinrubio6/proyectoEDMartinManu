package ed.tdas.excepciones;

public class EmptyQueueException extends RuntimeException {
	public EmptyQueueException(String msg) {
		super(msg);
	}
}
