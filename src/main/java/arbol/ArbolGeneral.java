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
