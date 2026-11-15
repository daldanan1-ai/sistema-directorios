import datos.DatosPrueba;
import servicio.SistemaDirectorios;
import vista.MenuConsola;

import java.util.Scanner;

/**
 * Punto de entrada: crea el sistema, carga los datos de prueba e inicia el menú.
 */
public class Main {

    public static void main(String[] args) {
        SistemaDirectorios sistema = new SistemaDirectorios();
        DatosPrueba.cargar(sistema);
        System.out.println("Datos cargados: " + sistema.getArbol().contarNodos() + " elementos en el árbol, "
                + sistema.getRed().numeroVertices() + " archivos y " + sistema.getRed().numeroAristas()
                + " vínculos en la red.");
        new MenuConsola(sistema, new Scanner(System.in)).iniciar();
    }
}
