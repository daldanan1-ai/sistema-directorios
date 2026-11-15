package modelo;

import java.util.Objects;

/**
 * Elemento del sistema de archivos: una unidad, una carpeta o un archivo.
 *
 * Cada elemento recibe un IDENTIFICADOR único e inmutable (como el "inodo" de
 * un sistema de archivos real). El nombre y la ubicación pueden cambiar
 * (renombrar, mover), pero el identificador no; por eso equals() y hashCode()
 * usan el id, y el grafo de vínculos no se rompe al renombrar o mover.
 */
public abstract class Elemento {

    private static int siguienteId = 1;

    private final int id;
    private String nombre;

    protected Elemento(String nombre) {
        this.id = siguienteId++;
        setNombre(nombre);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarNombre(nombre);
    }

    /** Las subclases validan el formato propio de su nombre. */
    protected String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.matches(".*[\\\\/:*?\"<>|].*")) {
            throw new IllegalArgumentException("El nombre no puede contener \\ / : * ? \" < > |");
        }
        if (limpio.length() > 60) {
            throw new IllegalArgumentException("El nombre no puede superar 60 caracteres");
        }
        return limpio;
    }

    /** true si el elemento puede contener otros elementos (unidad o carpeta). */
    public abstract boolean esContenedor();

    /** Etiqueta corta para mostrar el tipo: "Unidad", "Carpeta", "Archivo". */
    public abstract String getTipo();

    /** Tamaño en KB. Las carpetas y unidades calculan el suyo sumando su contenido (árbol). */
    public long getTamanoPropioKB() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Elemento)) {
            return false;
        }
        return id == ((Elemento) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
