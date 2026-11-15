package servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resumen de una eliminación: cuántos elementos salieron del árbol, qué
 * archivos salieron de la red y qué referencias quedaron rotas.
 */
public class ResultadoEliminacion {

    private final String rutaEliminada;
    private final int elementosEliminados;
    private final List<String> retiradosDeRed = new ArrayList<>();
    private final List<String> referenciasRotas = new ArrayList<>();

    public ResultadoEliminacion(String rutaEliminada, int elementosEliminados) {
        this.rutaEliminada = rutaEliminada;
        this.elementosEliminados = elementosEliminados;
    }

    void agregarRetiradoDeRed(String archivo) {
        retiradosDeRed.add(archivo);
    }

    void agregarReferenciaRota(String descripcion) {
        referenciasRotas.add(descripcion);
    }

    public String getRutaEliminada() {
        return rutaEliminada;
    }

    public int getElementosEliminados() {
        return elementosEliminados;
    }

    public List<String> getRetiradosDeRed() {
        return Collections.unmodifiableList(retiradosDeRed);
    }

    public List<String> getReferenciasRotas() {
        return Collections.unmodifiableList(referenciasRotas);
    }
}
