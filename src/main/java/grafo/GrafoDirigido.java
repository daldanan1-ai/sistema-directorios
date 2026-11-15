package grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Grafo DIRIGIDO y NO PONDERADO implementado con LISTA DE ADYACENCIA:
 *
 *   adyacencias = { vértice -> lista de aristas que SALEN de él }
 *
 *   index.html -> [estilos.css, app.js, logo.png, contacto.html]
 *   app.js     -> [utilidades.js]
 *   logo.png   -> []
 *
 * Se usa LinkedHashMap para que los vértices conserven el orden en que se
 * agregaron; así BFS y DFS siempre producen el mismo resultado.
 */
public class GrafoDirigido<T> {

    private final Map<T, List<Arista<T>>> adyacencias = new LinkedHashMap<>();

    // ============================================================ VÉRTICES

    /** Agrega un vértice sin conexiones. Devuelve false si ya existía. */
    public boolean agregarVertice(T vertice) {
        if (vertice == null) {
            throw new IllegalArgumentException("El vértice no puede ser nulo");
        }
        if (adyacencias.containsKey(vertice)) {
            return false;
        }
        adyacencias.put(vertice, new ArrayList<>());
        return true;
    }

    public boolean contieneVertice(T vertice) {
        return adyacencias.containsKey(vertice);
    }

    /**
     * Elimina el vértice: su lista de aristas de salida y TODAS las aristas que
     * llegaban a él desde otros vértices (si no, quedarían aristas "colgando"
     * hacia un vértice inexistente). Devuelve los vértices que apuntaban a él.
     */
    public List<T> eliminarVertice(T vertice) {
        validarVertice(vertice);
        List<T> apuntabanAEl = predecesores(vertice);
        adyacencias.remove(vertice);
        for (List<Arista<T>> aristas : adyacencias.values()) {
            aristas.removeIf(a -> a.getDestino().equals(vertice));
        }
        return apuntabanAEl;
    }

    public Set<T> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    public int numeroVertices() {
        return adyacencias.size();
    }

    // ============================================================= ARISTAS

    /**
     * Crea la arista origen -> destino. Como el grafo es DIRIGIDO, NO se crea
     * automáticamente destino -> origen.
     */
    public void agregarArista(T origen, T destino, String etiqueta) {
        validarVertice(origen);
        validarVertice(destino);
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Un vértice no puede conectarse consigo mismo");
        }
        if (existeArista(origen, destino)) {
            throw new IllegalArgumentException("Ya existe una conexión de " + origen + " hacia " + destino);
        }
        adyacencias.get(origen).add(new Arista<>(destino, etiqueta));
    }

    /** Elimina la arista origen -> destino. Devuelve false si no existía. */
    public boolean eliminarArista(T origen, T destino) {
        validarVertice(origen);
        validarVertice(destino);
        Iterator<Arista<T>> it = adyacencias.get(origen).iterator();
        while (it.hasNext()) {
            if (it.next().getDestino().equals(destino)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /** ¿Existe la conexión DIRECTA origen -> destino? (solo revisa la lista del origen). */
    public boolean existeArista(T origen, T destino) {
        List<Arista<T>> aristas = adyacencias.get(origen);
        if (aristas == null) {
            return false;
        }
        for (Arista<T> a : aristas) {
            if (a.getDestino().equals(destino)) {
                return true;
            }
        }
        return false;
    }

    /** Aristas que SALEN del vértice (sus vecinos o "sucesores"). */
    public List<Arista<T>> aristasDeSalida(T vertice) {
        validarVertice(vertice);
        return Collections.unmodifiableList(adyacencias.get(vertice));
    }

    /**
     * Vértices que tienen una arista HACIA este vértice. Como la lista de
     * adyacencia solo guarda las salidas, hay que revisar las listas de todos
     * los vértices: O(V + E).
     */
    public List<T> predecesores(T vertice) {
        validarVertice(vertice);
        List<T> resultado = new ArrayList<>();
        for (Map.Entry<T, List<Arista<T>>> e : adyacencias.entrySet()) {
            for (Arista<T> a : e.getValue()) {
                if (a.getDestino().equals(vertice)) {
                    resultado.add(e.getKey());
                }
            }
        }
        return resultado;
    }

    /** Etiqueta de la arista origen -> destino, o null si no existe. */
    public String etiqueta(T origen, T destino) {
        for (Arista<T> a : adyacencias.getOrDefault(origen, List.of())) {
            if (a.getDestino().equals(destino)) {
                return a.getEtiqueta();
            }
        }
        return null;
    }

    public int gradoSalida(T vertice) {
        validarVertice(vertice);
        return adyacencias.get(vertice).size();
    }

    public int gradoEntrada(T vertice) {
        return predecesores(vertice).size();
    }

    public int numeroAristas() {
        int total = 0;
        for (List<Arista<T>> aristas : adyacencias.values()) {
            total += aristas.size();
        }
        return total;
    }

    // ========================================================== RECORRIDOS

    /**
     * BFS (Breadth-First Search, búsqueda en AMPLITUD).
     * Usa una COLA: primero se visitan todos los vecinos directos del inicio
     * (nivel 1), luego los vecinos de esos vecinos (nivel 2), etc. Un conjunto
     * de "visitados" evita procesar dos veces un vértice (por ciclos o caminos
     * alternativos). Devuelve una lista por cada nivel de distancia.
     */
    public List<List<T>> bfs(T inicio) {
        validarVertice(inicio);
        List<List<T>> niveles = new ArrayList<>();
        Set<T> visitados = new java.util.HashSet<>();
        java.util.Queue<T> cola = new java.util.ArrayDeque<>();
        visitados.add(inicio);
        cola.offer(inicio);
        while (!cola.isEmpty()) {
            int enEsteNivel = cola.size();
            List<T> nivel = new ArrayList<>();
            for (int i = 0; i < enEsteNivel; i++) {
                T actual = cola.poll();
                nivel.add(actual);
                for (Arista<T> a : adyacencias.get(actual)) {
                    if (visitados.add(a.getDestino())) {   // add devuelve false si ya estaba
                        cola.offer(a.getDestino());
                    }
                }
            }
            niveles.add(nivel);
        }
        return niveles;
    }

    /**
     * DFS (Depth-First Search, búsqueda en PROFUNDIDAD), versión RECURSIVA.
     * Desde un vértice se toma el primer vecino no visitado y se sigue por él
     * hasta no poder avanzar más; solo entonces se RETROCEDE (vuelve la llamada
     * recursiva) y se prueba el siguiente vecino. La pila de llamadas hace el
     * papel de la pila explícita.
     */
    public List<T> dfs(T inicio) {
        return dfs(inicio, null);
    }

    /** Igual que dfs(inicio), y además anota en "rastro" cada entrada y cada retroceso. */
    public List<T> dfs(T inicio, List<String> rastro) {
        validarVertice(inicio);
        List<T> orden = new ArrayList<>();
        dfs(inicio, new java.util.HashSet<>(), orden, rastro, 0);
        return orden;
    }

    private void dfs(T actual, Set<T> visitados, List<T> orden, List<String> rastro, int profundidad) {
        visitados.add(actual);
        orden.add(actual);
        if (rastro != null) {
            rastro.add("  ".repeat(profundidad) + "entra a " + actual);
        }
        for (Arista<T> a : adyacencias.get(actual)) {
            if (!visitados.contains(a.getDestino())) {
                dfs(a.getDestino(), visitados, orden, rastro, profundidad + 1);
            }
        }
        if (rastro != null) {
            rastro.add("  ".repeat(profundidad) + "retrocede desde " + actual);
        }
    }

    private void validarVertice(T vertice) {
        if (!adyacencias.containsKey(vertice)) {
            throw new IllegalArgumentException("\"" + vertice + "\" no está en la red");
        }
    }
}
