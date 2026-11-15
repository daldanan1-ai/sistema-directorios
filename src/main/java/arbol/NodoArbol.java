package arbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nodo de un árbol GENERAL: un dato, la referencia a su padre y una lista de
 * hijos de tamaño variable (no está limitado a dos hijos como un árbol binario).
 */
public class NodoArbol<T> {

    private T dato;
    private NodoArbol<T> padre;                       // null solo en la raíz
    private final List<NodoArbol<T>> hijos = new ArrayList<>();

    public NodoArbol(T dato) {
        this.dato = dato;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodoArbol<T> getPadre() {
        return padre;
    }

    /** Lista de hijos de solo lectura: se modifica únicamente a través del árbol. */
    public List<NodoArbol<T>> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    public boolean esHoja() {
        return hijos.isEmpty();
    }

    public boolean esRaiz() {
        return padre == null;
    }

    // Métodos de paquete: solo ArbolGeneral cambia las referencias padre-hijo.

    void setPadre(NodoArbol<T> padre) {
        this.padre = padre;
    }

    void agregarHijo(int posicion, NodoArbol<T> hijo) {
        hijos.add(posicion, hijo);
    }

    boolean quitarHijo(NodoArbol<T> hijo) {
        return hijos.remove(hijo);
    }

    List<NodoArbol<T>> hijosModificables() {
        return hijos;
    }

    @Override
    public String toString() {
        return String.valueOf(dato);
    }
}
