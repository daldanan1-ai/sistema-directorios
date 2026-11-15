package modelo;

/**
 * Carpeta: puede contener subcarpetas y archivos.
 */
public class Carpeta extends Elemento {

    public Carpeta(String nombre) {
        super(nombre);
    }

    @Override
    public boolean esContenedor() {
        return true;
    }

    @Override
    public String getTipo() {
        return "Carpeta";
    }

    @Override
    public String toString() {
        return getNombre() + "/";
    }
}
