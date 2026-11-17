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

public class MotorDeliberacion {

    private Tree<Opinion> arbol;
    private Map<String, Position<Opinion>> mapaOpiniones;
    private Map<String, PositionList<Opinion>> mapaUsuarios;
    private Map<String, Position<Opinion>> posEnListaUsuario;
    private Stack<Operaciones> historialOperaciones;

    public MotorDeliberacion() {
        arbol = new Arbol<Opinion>();
        mapaOpiniones = new MapeoHashAbierto<String, Position<Opinion>>();
        mapaUsuarios = new MapeoHashAbierto<String, PositionList<Opinion>>();
        posEnListaUsuario = new MapeoHashAbierto<String, Position<Opinion>>();
        historialOperaciones = new LinkedStack<Operaciones>();
    }

    public void registrarOperacion(int operacion, String detalle, String idArgumento) {
        if (detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("El detalle de la operación no puede ser nulo o vacío.");
        }
        if (operacion != 1 && operacion != 2) {
            throw new IllegalArgumentException("Código de operación inválido: debe ser 1 o 2.");
        }
        Operaciones op = new Operaciones(operacion, detalle.toUpperCase(), idArgumento);
        historialOperaciones.push(op);
    }

    public String deshacerUltimaOperacion() throws EmptyStackException {
        if (historialOperaciones.isEmpty()) {
            throw new EmptyStackException("No hay operaciones registradas para deshacer.");
        }

        Operaciones ultima = historialOperaciones.pop();
        int tipo = ultima.getOperacion();
        String idArg = ultima.getId();

        if (tipo == 1) { // CREAR_DEBATE
            if (arbol.isEmpty()) {
                throw new IllegalStateException("No se pudo deshacer la operación CREAR_DEBATE.");
            }
            clearDebate();
            return "Se deshizo CREAR_DEBATE: " + idArg;
        } else if (tipo == 2) { // POSTEAR
            Position<Opinion> pos = mapaOpiniones.get(idArg);
            if (pos == null) {
                throw new IllegalStateException("No se pudo deshacer la operación POSTEAR: " + idArg);
            }
            desvincularNodoHoja(pos);
            return "Se deshizo POSTEAR: " + idArg;
        }

        return ultima.getDetalle();
    }

    public void crearDebate(String idRaiz, String texto, String idUsuario, String nombreUsuario) {
        if (idRaiz == null || idRaiz.trim().isEmpty() ||
            texto == null || texto.trim().isEmpty() ||
            idUsuario == null || idUsuario.trim().isEmpty() ||
            nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Los parámetros del debate no pueden ser nulos o vacíos.");
        }

        if (!arbol.isEmpty()) {
            throw new IllegalStateException("Ya existe un debate en curso.");
        }

        Usuario usr = new Usuario(idUsuario.trim(), nombreUsuario.trim());
        Opinion raizOp = new Opinion(idRaiz.trim(), texto.trim(), "ROOT", usr);

        arbol.createRoot(raizOp);
        Position<Opinion> posRaiz = arbol.root();

        registrarEnIndices(posRaiz);
        registrarOperacion(1, "CREAR_DEBATE:" + idRaiz.trim(), idRaiz.trim());
    }

    public void Postear(Usuario usr, String idNuevo, String idPadre, String postura, String texto) {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }

        if (usr == null || usr.getId() == null || usr.getId().trim().isEmpty() ||
            idNuevo == null || idNuevo.trim().isEmpty() ||
            idPadre == null || idPadre.trim().isEmpty() ||
            postura == null || postura.trim().isEmpty() ||
            texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ningún parámetro del posteo puede ser nulo o vacío.");
        }

        String pNorm = postura.trim().toUpperCase();
        if (!pNorm.equals("AFAVOR") && !pNorm.equals("ENCONTRA") &&
            !pNorm.equals("A FAVOR") && !pNorm.equals("EN CONTRA")) {
            throw new IllegalArgumentException("Postura inválida: debe ser AFAVOR o ENCONTRA.");
        }

        if (mapaOpiniones.get(idNuevo.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un argumento con el ID especificado.");
        }

        Position<Opinion> posPadre = mapaOpiniones.get(idPadre.trim());
        if (posPadre == null) {
            throw new IllegalArgumentException("El argumento padre no existe en el debate.");
        }

        Opinion nuevaOp = new Opinion(idNuevo.trim(), texto.trim(), pNorm, usr);
        Position<Opinion> posNueva = arbol.addLastChild(posPadre, nuevaOp);

        registrarEnIndices(posNueva);
        registrarOperacion(2, "POSTEAR:" + idNuevo.trim(), idNuevo.trim());
    }

    public void eliminarPosteo(String idUsuario, String idPosteo) {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }

        if (idUsuario == null || idUsuario.trim().isEmpty() ||
            idPosteo == null || idPosteo.trim().isEmpty()) {
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

            for (Position<Opinion> posActual : posiciones) {
                desvincularNodoHoja(posActual);
            }
        }
    }

    private void obtenerIdsYPosicionesSubarbol(Position<Opinion> nodo, PositionList<String> ids, PositionList<Position<Opinion>> posiciones) {
        for (Position<Opinion> hijo : arbol.children(nodo)) {
            obtenerIdsYPosicionesSubarbol(hijo, ids, posiciones);
        }
        ids.addLast(nodo.element().getIdArgumento());
        posiciones.addLast(nodo);
    }

    public void clearDebate() {
        arbol = new Arbol<Opinion>();
        mapaOpiniones = new MapeoHashAbierto<String, Position<Opinion>>();
        mapaUsuarios = new MapeoHashAbierto<String, PositionList<Opinion>>();
        posEnListaUsuario = new MapeoHashAbierto<String, Position<Opinion>>();
        historialOperaciones = new LinkedStack<Operaciones>();
    }

    public PositionList<Opinion> consultarArgumentosPorUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo o vacío.");
        }
        PositionList<Opinion> lista = mapaUsuarios.get(idUsuario.trim());
        if (lista == null) {
            return new DoubleLinkedList<Opinion>();
        }
        return lista;
    }

    public DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> calcularPolarizacionDebate() {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> toRet =
            new DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>>();
        calcularPolarizacionRec(arbol.root(), toRet);
        return toRet;
    }

    public DiccionarioHashAbierto<String, Integer> calcularPolarizacionNodo(String idNodo) {
        if (arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        if (idNodo == null || idNodo.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del nodo no puede ser nulo o vacío.");
        }
        Position<Opinion> nodo = mapaOpiniones.get(idNodo.trim());
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo especificado no existe.");
        }

        DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> todos =
            new DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>>();
        calcularPolarizacionRec(nodo, todos);
        return todos.find(idNodo.trim()).getValue();
    }

    // --- Métodos auxiliares privados ---

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
        }

        arbol.removeExternalNode(pos);
    }

    private int[] calcularPolarizacionRec(
            Position<Opinion> nodo,
            DiccionarioHashAbierto<String, DiccionarioHashAbierto<String, Integer>> acumulador) {

        int aFavor = 0;
        int enContra = 0;

        for (Position<Opinion> hijo : arbol.children(nodo)) {
            int[] sub = calcularPolarizacionRec(hijo, acumulador);
            aFavor += sub[0];
            enContra += sub[1];
        }

        String postura = nodo.element().getPostura().trim().toUpperCase();
        if (postura.equals("AFAVOR") || postura.equals("A FAVOR")) {
            aFavor++;
        } else if (postura.equals("ENCONTRA") || postura.equals("EN CONTRA")) {
            enContra++;
        }

        DiccionarioHashAbierto<String, Integer> dicNodo = new DiccionarioHashAbierto<String, Integer>();
        dicNodo.insert("AFAVOR", aFavor);
        dicNodo.insert("ENCONTRA", enContra);

        acumulador.insert(nodo.element().getIdArgumento(), dicNodo);
        return new int[]{aFavor, enContra};
    }

    /*** Mostrar árbol por consola ***/

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

    private String formatearNodo(Opinion a) {
        if (a == null) {
            return "(argumento nulo)";
        }
        return "[" + a.getIdArgumento() + "] (" + a.getPostura() + ") "
                + "usuario=" + a.getUsuario().getId() + " -> " + a.getTexto();
    }
}