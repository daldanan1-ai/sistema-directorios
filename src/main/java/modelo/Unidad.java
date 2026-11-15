package modelo;

/**
 * Unidad de almacenamiento (C:, D:, ...). Cuelga directamente de la raíz "Este equipo".
 */
public class Unidad extends Elemento {

    private final String etiqueta; // ej. "Disco local", "Datos"

    public Unidad(String letra, String etiqueta) {
        super(letra);
        this.etiqueta = etiqueta == null ? "" : etiqueta.trim();
    }

    @Override
    protected String validarNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().toUpperCase();
        if (!limpio.endsWith(":")) {
            limpio = limpio + ":";
        }
        if (!limpio.matches("[A-Z]:")) {
            throw new IllegalArgumentException("Una unidad se nombra con una letra, por ejemplo C:");
        }
        return limpio;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public boolean esContenedor() {
        return true;
    }

    @Override
    public String getTipo() {
        return "Unidad";
    }

    @Override
    public String toString() {
        return getNombre() + (etiqueta.isEmpty() ? "" : " (" + etiqueta + ")");
    }
}
