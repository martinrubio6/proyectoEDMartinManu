package ed;

import ed.tdas.Position;
import ed.tdas.excepciones.*;
import ed.tdas.tdaarbol.Arbol;
import ed.tdas.tdaarbol.Tree;
import ed.tdas.tdadiccionario.DiccionarioHashAbierto;
import ed.tdas.tdalista.DoubleLinkedList;
import ed.tdas.tdalista.PositionList;
import ed.tdas.tdamapeo.Map;
import ed.tdas.tdamapeo.MapeoHashAbierto;
import ed.tdas.tdapila.LinkedStack;
import ed.tdas.tdapila.Stack;

/**
 * Incluir los import necesarios para las clases utilizadas en el código.
 */

/**
 * Clase que representa el motor de deliveración.
 * 
 */
public class MotorDeliberacion {
    /**
     * Incluir los atributos necesarios para el desarrollo.
     */

    private static final int CREAR_DEBATE = 1;
    private static final int POSTEAR = 2;
    private static final String A_FAVOR = "AFAVOR";
    private static final String EN_CONTRA = "ENCONTRA";

    private Tree<Opinion> arbol;
    private Map<String, Position<Opinion>> mapaOpiniones;
    private Map<String, PositionList<Opinion>> mapaUsuarios;
    private Map<String, Position<Opinion>> posEnListaUsuario;
    private Stack<Operaciones> historialOperaciones;

    /**
     * Constructor de la clase MotorDeliveracion.
     * Inicializa el árbol de debate, y las estructuras correspondientes.  
     * 
     */
    public MotorDeliberacion() {
        arbol = new Arbol<Opinion>();
        mapaOpiniones = new MapeoHashAbierto<String, Position<Opinion>>();
        mapaUsuarios = new MapeoHashAbierto<String, PositionList<Opinion>>();
        posEnListaUsuario = new MapeoHashAbierto<String, Position<Opinion>>();
        historialOperaciones = new LinkedStack<Operaciones>();
    }

    /**
     * Registra una operación en la estructura que almacena las operaciones.
     * Las siguientes operaciones se pueden deshacer: "CREAR_DEBATE", "POSTEAR". El resto de las operaciones no se pueden deshacer.
     * 
     * La operación eliminar posteo no se puede deshacer, y si se intenta deshacer una operación cuando no hay operaciones registradas, se lanza una excepción.
     * 
     * 
     * @param operacion : La operación a registrar. 1: CREAR_DEBATE, 2: POSTEAR
     * @param detalle : Detalle de la operación, como el id del argumento o el id del usuario. El texto de la operación se almacena en mayúsculas para mantener consistencia. Por ejemplo: "CREAR_DEBATE:" + idRaiz.trim()
     * @param idArgumento : Identificador del argumento o de root en caso de ser el tema raíz.
     * @throws IllegalArgumentException si la operación es nula o vacía.
     */
    public void registrarOperacion(int operacion, String detalle, String idArgumento) {
        if (vacio(detalle)) {
            throw new IllegalArgumentException("El detalle de la operación no puede ser nulo o vacío.");
        }
        if (operacion != CREAR_DEBATE && operacion != POSTEAR) {
            throw new IllegalArgumentException("Código de operación inválido: debe ser 1 o 2.");
        }
        historialOperaciones.push(new Operaciones(operacion, detalle.toUpperCase(), idArgumento));
    }

/**
 * Deshace la última operación registrada en el motor de deliveración.
 * La operación eliminar posteo no se puede deshacer, y si se intenta deshacer una operación cuando no hay operaciones registradas, se lanza una excepción.
 * @return
 * @throws EmptyStackException
 */
    public String deshacerUltimaOperacion() throws EmptyStackException {
        if (historialOperaciones.isEmpty()) {
            throw new EmptyStackException("No hay operaciones registradas para deshacer.");
        }

        Operaciones ultima = historialOperaciones.pop();
        String idArg = ultima.getId();

        if (ultima.getOperacion() == CREAR_DEBATE) {
            clearDebate();
            return "Se deshizo CREAR_DEBATE: " + idArg;
        }

        Position<Opinion> pos = mapaOpiniones.get(idArg);
        if (pos == null || !arbol.isExternal(pos)) {
            throw new IllegalStateException("No se pudo deshacer la operación POSTEAR: " + idArg);
        }
        desvincularNodoHoja(pos);
        return "Se deshizo POSTEAR: " + idArg;
    }

    public void mostrarOperaciones() {
        if (historialOperaciones.isEmpty()) {
            System.out.println("(No hay operaciones registradas.)");
            return;
        }

        Stack<Operaciones> aux = new LinkedStack<Operaciones>();
        while (!historialOperaciones.isEmpty()) {
            Operaciones op = historialOperaciones.pop();
            System.out.println(op.getDetalle());
            aux.push(op);
        }
        while (!aux.isEmpty()) {
            historialOperaciones.push(aux.pop());
        }
    }

/**
 * Crea un nuevo debate con un argumento raíz.
 * @param idRaiz   : Identificador único del tema raíz.
 * @param texto:    Texto del tema raíz.
 * @param idUsuario: Identificador del usuario.
 * @param nombreUsuario: Nombre del usuario.
 * 
 * El método valida que los parámetros no sean nulos o vacíos, y que el identificador de la raíz sea numérico.
 * Si el árbol ya tiene un debate, lanza una excepción.
 * El método registra la operación en la pila de operaciones para permitir deshacerla posteriormente.
 * @throws IllegalArgumentException si algún parámetro es inválido.
 * @throws IllegalStateException si ya existe un debate en curso.
 * Crea un usuario y un tema raíz, y los agrega al árbol y a las estructuras correspondientes.
 */
    public void crearDebate(String idRaiz, String texto, String idUsuario, String nombreUsuario) {
        if (vacio(idRaiz) || vacio(texto) || vacio(idUsuario) || vacio(nombreUsuario)) {
            throw new IllegalArgumentException("Los parámetros del debate no pueden ser nulos o vacíos.");
        }
        if (!arbol.isEmpty()) {
            throw new IllegalStateException("Ya existe un debate en curso.");
        }

        String id = idRaiz.trim();
        Usuario usr = new Usuario(idUsuario.trim(), nombreUsuario.trim());
        Opinion raiz = new Opinion(id, texto.trim(), "ROOT", usr);

        arbol.createRoot(raiz);
        registrarEnIndices(arbol.root());
        registrarOperacion(CREAR_DEBATE, "CREAR_DEBATE:" + id, id);
    }

    public void Postear(Usuario usr, String idNuevo, String idPadre, String postura, String texto) {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        if (usr == null || vacio(usr.getId()) || vacio(idNuevo) || vacio(idPadre)
                || vacio(postura) || vacio(texto)) {
            throw new IllegalArgumentException("Ningún parámetro del posteo puede ser nulo o vacío.");
        }

        String pNorm = postura.trim().toUpperCase().replace(" ", "");
        if (!pNorm.equals(A_FAVOR) && !pNorm.equals(EN_CONTRA)) {
            throw new IllegalArgumentException("Postura inválida: debe ser AFAVOR o ENCONTRA.");
        }

        String id = idNuevo.trim();
        if (mapaOpiniones.get(id) != null) {
            throw new IllegalArgumentException("Ya existe un argumento con el ID especificado.");
        }
        Position<Opinion> posPadre = mapaOpiniones.get(idPadre.trim());
        if (posPadre == null) {
            throw new IllegalArgumentException("El argumento padre no existe en el debate.");
        }

        Position<Opinion> posNueva = arbol.addLastChild(posPadre, new Opinion(id, texto.trim(), pNorm, usr));

        registrarEnIndices(posNueva);
        registrarOperacion(POSTEAR, "POSTEAR:" + id, id);
    }
    

    // Elimina un posteo y todo el subárbol.
    public void eliminarPosteo(String idUsuario, String idPosteo) {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        if (vacio(idUsuario) || vacio(idPosteo)) {
            throw new IllegalArgumentException("Usuario o posteo inválido.");
        }

        Position<Opinion> posObjetivo = mapaOpiniones.get(idPosteo.trim());
        if (posObjetivo == null) {
            throw new IllegalArgumentException("El posteo no existe en el debate.");
        }
        if (!posObjetivo.element().getUsuario().getId().equals(idUsuario.trim())) {
            throw new IllegalStateException("El usuario no es el propietario del posteo.");
        }

        if (posObjetivo == arbol.root()) {
            clearDebate();
        } else {
            PositionList<String> ids = new DoubleLinkedList<String>();
            PositionList<Position<Opinion>> posiciones = new DoubleLinkedList<Position<Opinion>>();
            obtenerIdsYPosicionesSubarbol(posObjetivo, ids, posiciones);

            for (Position<Opinion> pos : posiciones) {
                desvincularNodoHoja(pos);
            }
        }
    }

    //obtiene las posiciones en postorden.
    private void obtenerIdsYPosicionesSubarbol(Position<Opinion> nodo, PositionList<String> ids, PositionList<Position<Opinion>> posiciones) {
        for (Position<Opinion> hijo : arbol.children(nodo)) {
            obtenerIdsYPosicionesSubarbol(hijo, ids, posiciones);
        }
        ids.addLast(nodo.element().getIdArgumento());
        posiciones.addLast(nodo);
    }

    //limpia todo el debate, dejando la estructura vacía.   
    public void clearDebate() {
        arbol = new Arbol<Opinion>();
        mapaOpiniones = new MapeoHashAbierto<String, Position<Opinion>>();
        mapaUsuarios = new MapeoHashAbierto<String, PositionList<Opinion>>();
        posEnListaUsuario = new MapeoHashAbierto<String, Position<Opinion>>();
        historialOperaciones = new LinkedStack<Operaciones>();
    }

    //consultar por usuario, devuelve una lista de argumentos que pertenecen a ese usuario.
    public PositionList<Opinion> consultarArgumentosPorUsuario(String idUsuario) {
        if (vacio(idUsuario)) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo o vacío.");
        }
        PositionList<Opinion> lista = mapaUsuarios.get(idUsuario.trim());
        if (lista == null) {
            return new DoubleLinkedList<Opinion>();
        }
        return lista;
    }

    /**
     * Calcula la polarización de todos los nodos del debate.
     * La polarización se define como la cantidad de argumentos a favor y en contra que existen en el subárbol que tiene como raíz cada nodo.
     * @throws IllegalStateException si no hay un debate activo.
     * @return Un diccionario cuya clave es el ID del nodo y el valor es un diccionario con la polarización: 
     * "AFAVOR" y "ENCONTRA" y sus respectivos conteos.
     */
    public DiccionarioHashAbierto<String,DiccionarioHashAbierto<String,Integer>> calcularPolarizacionDebate(){
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> resultado =
                new DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>>();
        polarizar(arbol.root(), resultado);
        return resultado;
    }

     /**
    * Calcula la polarización de un nodo específico en el árbol de debate.
    * La polarización se define como la cantidad de argumentos a favor y en contra que existen en el subárbol que tiene como raíz el nodo especificado por idNodo.
    * @throws IllegalStateException si no hay un debate activo. 
    * @param idNodo : Identificador del nodo para el cual se desea calcular la polarización.
    * @return Un DiccionarioHashAbierto<String,Integer> que contiene la polarización del nodo, con las claves "AFAVOR" y "ENCONTRA" y sus respectivos conteos.
    */
    public DiccionarioHashAbierto<String,Integer> calcularPolarizacionNodo(String idNodo){
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        if (vacio(idNodo)) {
            throw new IllegalArgumentException("El ID del nodo no puede ser nulo o vacío.");
        }
        Position<Opinion> nodo = mapaOpiniones.get(idNodo.trim());
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo especificado no existe.");
        }

        int[] cuenta = polarizar(nodo, null);
        return crearDiccionarioPolarizacion(cuenta[0], cuenta[1]);
    }

    private int[] polarizar(Position<Opinion> nodo,
            DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> acumulador) {
        int aFavor = 0;
        int enContra = 0;

        for (Position<Opinion> hijo : arbol.children(nodo)) {
            int[] sub = polarizar(hijo, acumulador);
            aFavor += sub[0];
            enContra += sub[1];
        }

        String postura = nodo.element().getPostura();
        if (postura.equals(A_FAVOR)) {
            aFavor++;
        } else if (postura.equals(EN_CONTRA)) {
            enContra++;
        }

        if (acumulador != null) {
            acumulador.insert(nodo.element().getIdArgumento(), crearDiccionarioPolarizacion(aFavor, enContra));
        }
        return new int[]{aFavor, enContra};
    }

    private DiccionarioHashAbierto<String, Integer> crearDiccionarioPolarizacion(int aFavor, int enContra) {
        DiccionarioHashAbierto<String, Integer> dic = new DiccionarioHashAbierto<String, Integer>();
        dic.insert(A_FAVOR, aFavor);
        dic.insert(EN_CONTRA, enContra);
        return dic;
    }

    private void registrarEnIndices(Position<Opinion> pos) {
        Opinion op = pos.element();
        String idOp = op.getIdArgumento();
        String idUsr = op.getUsuario().getId();

        mapaOpiniones.put(idOp, pos);

        PositionList<Opinion> listaUsr = mapaUsuarios.get(idUsr);
        if (listaUsr == null) {
            listaUsr = new DoubleLinkedList<Opinion>();
            mapaUsuarios.put(idUsr, listaUsr);
        }
        listaUsr.addLast(op);
        posEnListaUsuario.put(idOp, listaUsr.last());
    }

    private void desvincularNodoHoja(Position<Opinion> pos) {
        Opinion op = pos.element();
        String idOp = op.getIdArgumento();
        String idUsr = op.getUsuario().getId();

        mapaOpiniones.remove(idOp);

        Position<Opinion> posEnLista = posEnListaUsuario.remove(idOp);
        PositionList<Opinion> listaUsr = mapaUsuarios.get(idUsr);
        if (listaUsr != null && posEnLista != null) {
            listaUsr.remove(posEnLista);
            if (listaUsr.isEmpty()) {
                mapaUsuarios.remove(idUsr);
            }
        }

        arbol.removeExternalNode(pos);
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

 /*** Mostrar árbol por consola ***/
 
    /**
     * Imprime por consola, en modo texto, el contenido del árbol de debate,
     * con indentación jerárquica que muestra la estructura padre-hijo.
     * Cada nodo se muestra con su id, postura, usuario y texto del argumento.
     */
    public void imprimirDebate() {
        if (arbol == null || arbol.isEmpty()) {
            System.out.println("(El árbol de debate está vacío.)");
            return;
        }

        try {
            Position<Opinion> raiz = arbol.root();
            System.out.println(formatearNodo(raiz.element()));
            imprimirHijos(raiz, "");
        } catch (Exception e) {
            System.out.println("No se pudo imprimir el árbol: " + e.getMessage());
        }
    }

    /**
     * Imprime recursivamente los hijos de 'nodo', usando 'prefijo' para
     * mantener la indentación acumulada de los niveles superiores y
     * conectores tipo "├──" / "└──" para el nivel actual.
     */
    private void imprimirHijos(Position<Opinion> nodo, String prefijo) throws InvalidPositionException {
        PositionList<Position<Opinion>> hijos = new DoubleLinkedList<Position<Opinion>>();
        for (Position<Opinion> hijo : arbol.children(nodo)) {
            hijos.addLast(hijo);
        }

        for (Position<Opinion> hijo : arbol.children(nodo)) {
            boolean esUltimo = (hijo == hijos.last().element());
            
            String conector = esUltimo ? "└── " : "├── ";
            System.out.println(prefijo + conector + formatearNodo(hijo.element()));

            String prefijoHijo = prefijo + (esUltimo ? "    " : "│   ");
            imprimirHijos(hijo, prefijoHijo);
            
        }
    }

/** Arma la línea de texto que representa a un Argumento en la impresión. */
    private String formatearNodo(Opinion a) {
        if (a == null) {
            return "(argumento nulo)";
        }
        return "[" + a.getIdArgumento() + "] (" + a.getPostura() + ") "
                + "usuario=" + a.getUsuario().getId() + " -> " + a.getTexto();
    }
}