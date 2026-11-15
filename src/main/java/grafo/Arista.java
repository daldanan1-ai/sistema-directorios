package grafo;

/**
 * Arista DIRIGIDA: solo guarda el destino (el origen es el vértice dueño de la
 * lista de adyacencia donde está la arista) y una etiqueta con su significado.
 * No tiene peso: en esta red todos los vínculos "cuestan" lo mismo.
 */
public class Arista<T> {

    private final T destino;
    private final String etiqueta;

    public Arista(T destino, String etiqueta) {
        this.destino = destino;
        this.etiqueta = etiqueta;
    }

    public T getDestino() {
        return destino;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return "→ " + destino + " [" + etiqueta + "]";
    }
}
