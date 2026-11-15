package modelo;

/**
 * Archivo: es una HOJA del árbol (no contiene elementos) y puede participar
 * como VÉRTICE en la red de vínculos (grafo).
 */
public class Archivo extends Elemento {

    public static final long TAMANO_MAXIMO_KB = 10_000_000; // ~10 GB

    private long tamanoKB;

    public Archivo(String nombre, long tamanoKB) {
        super(nombre);
        setTamanoKB(tamanoKB);
    }

    @Override
    protected String validarNombre(String nombre) {
        String limpio = super.validarNombre(nombre);
        int punto = limpio.lastIndexOf('.');
        if (punto <= 0 || punto == limpio.length() - 1) {
            throw new IllegalArgumentException("El archivo debe tener nombre y extensión, por ejemplo informe.docx");
        }
        return limpio;
    }

    public String getExtension() {
        String nombre = getNombre();
        return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
    }

    public long getTamanoKB() {
        return tamanoKB;
    }

    public void setTamanoKB(long tamanoKB) {
        if (tamanoKB < 0 || tamanoKB > TAMANO_MAXIMO_KB) {
            throw new IllegalArgumentException("El tamaño debe estar entre 0 y " + TAMANO_MAXIMO_KB + " KB");
        }
        this.tamanoKB = tamanoKB;
    }

    @Override
    public long getTamanoPropioKB() {
        return tamanoKB;
    }

    @Override
    public boolean esContenedor() {
        return false;
    }

    @Override
    public String getTipo() {
        return "Archivo";
    }

    @Override
    public String toString() {
        return getNombre() + " (" + tamanoKB + " KB)";
    }
}
