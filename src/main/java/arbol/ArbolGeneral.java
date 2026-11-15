package arbol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Árbol general (cada nodo puede tener cualquier cantidad de hijos).
 *
 * Los hijos de cada nodo se mantienen ordenados según un Comparator, que en
 * el sistema de directorios pone primero las carpetas y luego los archivos,
 * cada grupo por nombre, como lo muestra un explorador de archivos.
 */
public class ArbolGeneral<T> {

    private final NodoArbol<T> raiz;
    private final Comparator<T> ordenHijos;

    public ArbolGeneral(T datoRaiz, Comparator<T> ordenHijos) {
        this.raiz = new NodoArbol<>(datoRaiz);
        this.ordenHijos = ordenHijos;
    }

    public NodoArbol<T> getRaiz() {
        return raiz;
    }

    // ============================================================ INSERTAR

    /**
     * Inserta un dato como HIJO del nodo padre indicado. Se ubica en la posición
     * que le corresponde según el Comparator, para que los hijos queden ordenados.
     */
    public NodoArbol<T> insertar(NodoArbol<T> padre, T dato) {
        if (padre == null) {
            throw new IllegalArgumentException("Debe indicar el nodo padre");
        }
        NodoArbol<T> nuevo = new NodoArbol<>(dato);
        enlazar(padre, nuevo);
        return nuevo;
    }

    /** Conecta hijo con padre en ambas direcciones, en la posición ordenada. */
    private void enlazar(NodoArbol<T> padre, NodoArbol<T> hijo) {
        List<NodoArbol<T>> hijos = padre.hijosModificables();
        int posicion = 0;
        while (posicion < hijos.size() && ordenHijos.compare(hijos.get(posicion).getDato(), hijo.getDato()) <= 0) {
            posicion++;
        }
        padre.agregarHijo(posicion, hijo);
        hijo.setPadre(padre);
    }

    // ============================================================== BUSCAR

    /** Primer nodo (en recorrido en profundidad) cuyo dato cumple la condición, o null. */
    public NodoArbol<T> buscar(Predicate<T> condicion) {
        return buscar(raiz, condicion);
    }

    private NodoArbol<T> buscar(NodoArbol<T> nodo, Predicate<T> condicion) {
        if (condicion.test(nodo.getDato())) {
            return nodo;
        }
        for (NodoArbol<T> hijo : nodo.getHijos()) {
            NodoArbol<T> encontrado = buscar(hijo, condicion);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }

    /** Todos los nodos cuyo dato cumple la condición (puede haber varios con el mismo nombre). */
    public List<NodoArbol<T>> buscarTodos(Predicate<T> condicion) {
        List<NodoArbol<T>> resultado = new ArrayList<>();
        buscarTodos(raiz, condicion, resultado);
        return resultado;
    }

    private void buscarTodos(NodoArbol<T> nodo, Predicate<T> condicion, List<NodoArbol<T>> resultado) {
        if (condicion.test(nodo.getDato())) {
            resultado.add(nodo);
        }
        for (NodoArbol<T> hijo : nodo.getHijos()) {
            buscarTodos(hijo, condicion, resultado);
        }
    }

    // ========================================================= PROPIEDADES

    /** NIVEL = número de aristas desde la raíz. La raíz está en el nivel 0. */
    public int nivel(NodoArbol<T> nodo) {
        int nivel = 0;
        NodoArbol<T> actual = nodo;
        while (actual.getPadre() != null) {      // sube por las referencias al padre
            nivel++;
            actual = actual.getPadre();
        }
        return nivel;
    }

    /**
     * ALTURA de un nodo = número de aristas del camino más largo hasta una hoja.
     * Una hoja tiene altura 0; un nodo tiene altura 1 + la mayor altura de sus hijos.
     */
    public int altura(NodoArbol<T> nodo) {
        int mayor = -1;
        for (NodoArbol<T> hijo : nodo.getHijos()) {
            mayor = Math.max(mayor, altura(hijo));
        }
        return mayor + 1;
    }

    public int altura() {
        return altura(raiz);
    }

    /** Camino desde la raíz hasta el nodo (raíz incluida), subiendo por los padres. */
    public List<NodoArbol<T>> caminoDesdeRaiz(NodoArbol<T> nodo) {
        List<NodoArbol<T>> camino = new ArrayList<>();
        for (NodoArbol<T> actual = nodo; actual != null; actual = actual.getPadre()) {
            camino.add(0, actual);
        }
        return camino;
    }

    /** true si "posibleAncestro" está en el camino desde "nodo" hasta la raíz (o es el mismo nodo). */
    public boolean esAncestroOIgual(NodoArbol<T> posibleAncestro, NodoArbol<T> nodo) {
        for (NodoArbol<T> actual = nodo; actual != null; actual = actual.getPadre()) {
            if (actual == posibleAncestro) {
                return true;
            }
        }
        return false;
    }

    public int contarNodos() {
        return preorden(raiz).size();
    }

    // ======================================================== MODIFICACIÓN

    /**
     * Elimina el nodo y TODO su subárbol. Devuelve los nodos eliminados en
     * POSTORDEN (primero el contenido y al final el nodo), para que quien llama
     * pueda limpiar otras estructuras (por ejemplo, el grafo).
     */
    public List<NodoArbol<T>> eliminar(NodoArbol<T> nodo) {
        if (nodo == raiz) {
            throw new IllegalArgumentException("No se puede eliminar la raíz del árbol");
        }
        List<NodoArbol<T>> eliminados = postorden(nodo);
        nodo.getPadre().quitarHijo(nodo);        // el padre deja de apuntar al nodo
        nodo.setPadre(null);                     // y el nodo deja de apuntar al padre
        return eliminados;
    }

    /**
     * Mueve un nodo (con todo su subárbol) bajo un nuevo padre. No se permite
     * mover un nodo dentro de sí mismo o de uno de sus descendientes: eso dejaría
     * el subárbol desconectado de la raíz y formaría un ciclo.
     */
    public void mover(NodoArbol<T> nodo, NodoArbol<T> nuevoPadre) {
        if (nodo == raiz) {
            throw new IllegalArgumentException("No se puede mover la raíz");
        }
        if (esAncestroOIgual(nodo, nuevoPadre)) {
            throw new IllegalArgumentException("No se puede mover un elemento dentro de sí mismo o de su propio contenido");
        }
        nodo.getPadre().quitarHijo(nodo);
        enlazar(nuevoPadre, nodo);
    }

    /** Después de cambiar un dato que afecta el orden (renombrar), lo reubica entre sus hermanos. */
    public void reordenar(NodoArbol<T> nodo) {
        NodoArbol<T> padre = nodo.getPadre();
        if (padre != null) {
            padre.quitarHijo(nodo);
            enlazar(padre, nodo);
        }
    }

    // =========================================================== RECORRIDOS

    /**
     * PREORDEN (recorrido en profundidad): primero el nodo y después, uno por uno,
     * todo el subárbol de cada hijo. Una carpeta aparece ANTES que su contenido,
     * igual que al listar un directorio con el comando "tree".
     */
    public List<NodoArbol<T>> preorden(NodoArbol<T> desde) {
        List<NodoArbol<T>> orden = new ArrayList<>();
        preorden(desde, orden);
        return orden;
    }

    private void preorden(NodoArbol<T> nodo, List<NodoArbol<T>> orden) {
        orden.add(nodo);                         // 1. visitar el nodo
        for (NodoArbol<T> hijo : nodo.getHijos()) {
            preorden(hijo, orden);               // 2. recorrer cada subárbol completo
        }
    }

    /**
     * POSTORDEN (también en profundidad): primero todos los hijos y al final el
     * nodo. Una carpeta aparece DESPUÉS de su contenido. Es el orden necesario
     * para calcular el tamaño de una carpeta (se necesita el de sus hijos) o para
     * borrarla (primero se borra el contenido).
     */
    public List<NodoArbol<T>> postorden(NodoArbol<T> desde) {
        List<NodoArbol<T>> orden = new ArrayList<>();
        postorden(desde, orden);
        return orden;
    }

    private void postorden(NodoArbol<T> nodo, List<NodoArbol<T>> orden) {
        for (NodoArbol<T> hijo : nodo.getHijos()) {
            postorden(hijo, orden);              // 1. recorrer cada subárbol
        }
        orden.add(nodo);                         // 2. visitar el nodo al final
    }

    /**
     * POR NIVELES (en amplitud): usa una COLA. Se visita la raíz, luego todos sus
     * hijos (nivel 1), luego todos los nietos (nivel 2), etc. Devuelve una lista
     * por cada nivel.
     */
    public List<List<NodoArbol<T>>> porNiveles(NodoArbol<T> desde) {
        List<List<NodoArbol<T>>> niveles = new ArrayList<>();
        java.util.Queue<NodoArbol<T>> cola = new java.util.ArrayDeque<>();
        cola.offer(desde);
        while (!cola.isEmpty()) {
            int enEsteNivel = cola.size();       // los que están en la cola son todos del mismo nivel
            List<NodoArbol<T>> nivel = new ArrayList<>();
            for (int i = 0; i < enEsteNivel; i++) {
                NodoArbol<T> actual = cola.poll();
                nivel.add(actual);
                for (NodoArbol<T> hijo : actual.getHijos()) {
                    cola.offer(hijo);            // los hijos se atenderán en el siguiente nivel
                }
            }
            niveles.add(nivel);
        }
        return niveles;
    }

    // ===================================================== REPRESENTACIÓN

    /**
     * Dibuja el árbol con caracteres, como el comando "tree":
     *
     *   Este equipo
     *   ├── C:
     *   │   └── Proyectos/
     *   └── D:
     */
    public String representar(NodoArbol<T> desde, Function<T, String> texto) {
        StringBuilder sb = new StringBuilder(texto.apply(desde.getDato())).append('\n');
        List<NodoArbol<T>> hijos = desde.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            representar(hijos.get(i), "", i == hijos.size() - 1, texto, sb);
        }
        return sb.toString();
    }

    private void representar(NodoArbol<T> nodo, String prefijo, boolean esUltimo, Function<T, String> texto,
                             StringBuilder sb) {
        sb.append(prefijo).append(esUltimo ? "└── " : "├── ").append(texto.apply(nodo.getDato())).append('\n');
        String prefijoHijos = prefijo + (esUltimo ? "    " : "│   ");
        List<NodoArbol<T>> hijos = nodo.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            representar(hijos.get(i), prefijoHijos, i == hijos.size() - 1, texto, sb);
        }
    }
}
