package vista;

import arbol.ArbolGeneral;
import arbol.NodoArbol;
import grafo.Arista;
import modelo.Archivo;
import modelo.Elemento;
import modelo.TipoVinculo;
import servicio.ResultadoEliminacion;
import servicio.SistemaDirectorios;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interfaz de consola: pide datos y muestra resultados. La lógica está en
 * SistemaDirectorios, ArbolGeneral y GrafoDirigido.
 */
public class MenuConsola {

    private static final String LINEA = "-".repeat(90);

    private final SistemaDirectorios sistema;
    private final ArbolGeneral<Elemento> arbol;
    private final Scanner sc;

    public MenuConsola(SistemaDirectorios sistema, Scanner sc) {
        this.sistema = sistema;
        this.arbol = sistema.getArbol();
        this.sc = sc;
    }

    public void iniciar() {
        while (true) {
            mostrarOpciones();
            String opcion = leer("Seleccione una opción: ");
            if (opcion == null || opcion.trim().equals("0")) {
                System.out.println("Sesión finalizada.");
                return;
            }
            System.out.println(LINEA);
            try {
                ejecutar(opcion.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("✗ " + e.getMessage());
            }
        }
    }

    private void mostrarOpciones() {
        System.out.println();
        System.out.println("==================== SISTEMA DE DIRECTORIOS ====================");
        System.out.println(" ESTRUCTURA DE CARPETAS (árbol)          RED DE VÍNCULOS ENTRE ARCHIVOS (grafo)");
        System.out.println("  1. Crear carpeta o archivo              9. Incluir archivo en la red");
        System.out.println("  2. Ver árbol de directorios            10. Crear vínculo entre archivos");
        System.out.println("  3. Buscar por ruta o por nombre        11. Eliminar vínculo");
        System.out.println("  4. Recorridos del árbol                12. Ver vínculos de un archivo");
        System.out.println("  5. Contenido, carpeta padre, nivel     13. Verificar vínculo directo");
        System.out.println("     y altura                            14. Recorrido BFS de referencias");
        System.out.println("  6. Renombrar                           15. Recorrido DFS de referencias");
        System.out.println("  7. Mover a otra carpeta                16. Ruta de referencias entre archivos");
        System.out.println("  8. Eliminar carpeta o archivo          17. Detectar referencias circulares");
        System.out.println("                                         18. Análisis de impacto de un archivo");
        System.out.println(" 20. Información completa del sistema    19. Retirar archivo de la red");
        System.out.println("  0. Salir");
    }

    private void ejecutar(String opcion) {
        switch (opcion) {
            case "1" -> crear();
            case "2" -> {
                String ruta = leer("Ruta desde donde dibujar (Enter = todo el equipo): ");
                System.out.print(sistema.dibujarArbol(sistema.localizar(ruta)));
            }
            case "3" -> buscar();
            case "4" -> recorridosArbol();
            case "5" -> consultarNodo();
            case "6" -> {
                String ruta = leer("Ruta del elemento a renombrar: ");
                Elemento e = sistema.renombrar(ruta, leer("Nuevo nombre: "));
                System.out.println("✓ Renombrado. Nueva ruta: " + sistema.ruta(e));
                System.out.println("  Sus vínculos en la red se conservan (el grafo usa el objeto, no el nombre).");
            }
            case "7" -> {
                String ruta = leer("Ruta del elemento a mover: ");
                Elemento e = sistema.mover(ruta, leer("Carpeta o unidad de destino: "));
                System.out.println("✓ Movido con todo su contenido. Nueva ruta: " + sistema.ruta(e));
                System.out.println("  Nuevo nivel: " + arbol.nivel(sistema.localizar(sistema.ruta(e))));
            }
            case "8" -> eliminar();
            case "9" -> {
                Archivo a = sistema.incluirEnRed(leer("Ruta del archivo: "));
                System.out.println("✓ " + sistema.ruta(a) + " ahora es un vértice de la red (sin vínculos todavía).");
            }
            case "10" -> crearVinculo();
            case "11" -> {
                String o = leer("Archivo de origen: ");
                String d = leer("Archivo de destino: ");
                System.out.println(sistema.eliminarVinculo(o, d)
                        ? "✓ Vínculo eliminado." : "✗ No existía un vínculo de ese origen hacia ese destino.");
            }
            case "12" -> verVinculos();
            case "13" -> verificarVinculo();
            case "14" -> bfs();
            case "15" -> dfs();
            case "16" -> rutaReferencias();
            case "17" -> ciclos();
            case "18" -> impacto();
            case "19" -> {
                String ruta = leer("Archivo a retirar de la red: ");
                List<Archivo> apuntaban = sistema.retirarDeRed(ruta);
                System.out.println("✓ Retirado de la red (sigue existiendo en su carpeta).");
                System.out.println("  Se eliminaron también los vínculos que llegaban desde: " + nombres(apuntaban));
            }
            case "20" -> informacionCompleta();
            default -> System.out.println("Opción no válida.");
        }
    }

    // ============================================================== ÁRBOL

    private void crear() {
        String tipo = leer("¿Qué desea crear? (C = carpeta, A = archivo): ").trim().toUpperCase();
        String rutaPadre = leer("Ruta de la carpeta o unidad donde se creará (ej. C:/Proyectos): ");
        NodoArbol<Elemento> nuevo;
        if (tipo.equals("C")) {
            nuevo = sistema.crearCarpeta(rutaPadre, leer("Nombre de la carpeta: "));
        } else if (tipo.equals("A")) {
            String nombre = leer("Nombre del archivo con extensión: ");
            nuevo = sistema.crearArchivo(rutaPadre, nombre, leerEntero("Tamaño en KB: "));
        } else {
            throw new IllegalArgumentException("Escriba C (carpeta) o A (archivo)");
        }
        System.out.println("✓ Creado: " + sistema.ruta(nuevo.getDato()));
        System.out.println("  Padre: " + sistema.ruta(nuevo.getPadre().getDato()) + " | nivel " + arbol.nivel(nuevo)
                + " | posición " + (nuevo.getPadre().getHijos().indexOf(nuevo) + 1) + " de "
                + nuevo.getPadre().getHijos().size() + " entre sus hermanos");
        System.out.print(sistema.dibujarArbol(nuevo.getPadre()));
    }

    private void buscar() {
        String texto = leer("Ruta completa o parte del nombre: ");
        if (texto.contains("/") || texto.contains("\\") || texto.trim().matches("(?i)[a-z]:")) {
            NodoArbol<Elemento> nodo = sistema.localizar(texto);
            System.out.println("✓ Encontrado por ruta: " + sistema.ruta(nodo.getDato()) + " (" + nodo.getDato().getTipo()
                    + ", nivel " + arbol.nivel(nodo) + ")");
        } else {
            List<NodoArbol<Elemento>> encontrados = sistema.buscarPorNombre(texto);
            if (encontrados.isEmpty()) {
                System.out.println("✗ Ningún elemento contiene \"" + texto.trim() + "\" en su nombre.");
            }
            for (NodoArbol<Elemento> n : encontrados) {
                System.out.printf("  ✓ %-55s %-8s nivel %d%n", sistema.ruta(n.getDato()), n.getDato().getTipo(),
                        arbol.nivel(n));
            }
        }
    }

    private void recorridosArbol() {
        NodoArbol<Elemento> desde = sistema.localizar(leer("Recorrer desde (Enter = todo el equipo): "));
        System.out.println("PREORDEN (profundidad: cada carpeta ANTES de su contenido):");
        System.out.println("  " + nombresNodos(arbol.preorden(desde)));
        System.out.println("POSTORDEN (profundidad: cada carpeta DESPUÉS de su contenido):");
        System.out.println("  " + nombresNodos(arbol.postorden(desde)));
        System.out.println("POR NIVELES (amplitud, con cola):");
        List<List<NodoArbol<Elemento>>> niveles = arbol.porNiveles(desde);
        int base = arbol.nivel(desde);
        for (int i = 0; i < niveles.size(); i++) {
            System.out.println("  Nivel " + (base + i) + ": " + nombresNodos(niveles.get(i)));
        }
    }

    private void consultarNodo() {
        NodoArbol<Elemento> nodo = sistema.localizar(leer("Ruta del elemento (Enter = Este equipo): "));
        Elemento e = nodo.getDato();
        System.out.println("Elemento:  " + sistema.ruta(e) + " (" + e.getTipo() + ")");
        System.out.println("Padre:     " + (nodo.esRaiz() ? "(es la raíz)" : sistema.ruta(nodo.getPadre().getDato())));
        System.out.println("Nivel:     " + arbol.nivel(nodo) + "   (la raíz \"Este equipo\" está en el nivel 0)");
        System.out.println("Altura del subárbol: " + arbol.altura(nodo) + "   | Altura de todo el árbol: " + arbol.altura());
        System.out.println("Tamaño total: " + sistema.tamanoTotalKB(nodo) + " KB (suma en postorden)");
        System.out.println("Hijos directos (" + nodo.getHijos().size() + "):");
        if (nodo.esHoja()) {
            System.out.println("  (ninguno: es una hoja)");
        }
        for (NodoArbol<Elemento> h : nodo.getHijos()) {
            System.out.println("  - " + h.getDato() + "  [" + h.getDato().getTipo() + "]");
        }
    }

    private void eliminar() {
        String ruta = leer("Ruta de la carpeta o archivo a eliminar: ");
        NodoArbol<Elemento> nodo = sistema.localizar(ruta);
        int cantidad = arbol.preorden(nodo).size();
        if (cantidad > 1) {
            System.out.println("  " + sistema.ruta(nodo.getDato()) + " contiene " + (cantidad - 1)
                    + " elemento(s). Se eliminará TODO su contenido:");
            System.out.print(sistema.dibujarArbol(nodo).indent(4));
        }
        String ok = leer("  ¿Confirma la eliminación? (S/N): ");
        if (!ok.trim().equalsIgnoreCase("S")) {
            System.out.println("  Eliminación cancelada.");
            return;
        }
        ResultadoEliminacion r = sistema.eliminar(ruta);
        System.out.println("✓ Eliminado " + r.getRutaEliminada() + ": " + r.getElementosEliminados()
                + " elemento(s) salieron del árbol.");
        if (!r.getRetiradosDeRed().isEmpty()) {
            System.out.println("  Retirados de la red de vínculos: " + r.getRetiradosDeRed());
        }
        if (r.getReferenciasRotas().isEmpty()) {
            System.out.println("  Ningún archivo restante quedó con referencias rotas.");
        } else {
            System.out.println("  ⚠ Referencias rotas (vínculos que se eliminaron):");
            for (String s : r.getReferenciasRotas()) {
                System.out.println("    - " + s);
            }
        }
    }

    // ============================================================== GRAFO

    private void crearVinculo() {
        String origen = leer("Archivo de origen (el que usa o apunta): ");
        String destino = leer("Archivo de destino (el usado o apuntado): ");
        TipoVinculo[] tipos = TipoVinculo.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("   " + (i + 1) + ". " + tipos[i] + " (" + tipos[i].getDescripcion() + ")");
        }
        TipoVinculo tipo = TipoVinculo.desdeTexto(leer("Tipo de vínculo: "));
        sistema.crearVinculo(origen, destino, tipo);
        System.out.println("✓ Vínculo creado: " + sistema.ruta(sistema.archivoDeRed(origen)) + " "
                + tipo.getDescripcion() + " " + sistema.ruta(sistema.archivoDeRed(destino)));
        System.out.println("  Es DIRIGIDO: no se creó el vínculo en sentido contrario.");
    }

    private void verVinculos() {
        String ruta = leer("Archivo: ");
        Archivo a = sistema.archivoDeRed(ruta);
        List<Arista<Archivo>> salen = sistema.vinculosSalientes(ruta);
        List<Archivo> llegan = sistema.vinculosEntrantes(ruta);
        System.out.println(sistema.ruta(a));
        System.out.println("  Usa o apunta a (salida, grado " + salen.size() + "):");
        for (Arista<Archivo> ar : salen) {
            System.out.println("    → " + sistema.ruta(ar.getDestino()) + "  [" + ar.getEtiqueta() + "]");
        }
        System.out.println("  Es usado por (entrada, grado " + llegan.size() + "):");
        for (Archivo p : llegan) {
            System.out.println("    ← " + sistema.ruta(p) + "  [" + sistema.getRed().etiqueta(p, a) + "]");
        }
        System.out.println("  Grado total: " + (salen.size() + llegan.size()));
    }

    private void verificarVinculo() {
        String o = leer("Archivo de origen: ");
        String d = leer("Archivo de destino: ");
        boolean ida = sistema.existeVinculo(o, d);
        boolean vuelta = sistema.existeVinculo(d, o);
        System.out.println((ida ? "✓ SÍ" : "✗ NO") + " existe vínculo directo " + nombre(o) + " → " + nombre(d)
                + (ida ? " [" + sistema.getRed().etiqueta(sistema.archivoDeRed(o), sistema.archivoDeRed(d)) + "]" : ""));
        System.out.println("  En sentido contrario (" + nombre(d) + " → " + nombre(o) + "): " + (vuelta ? "SÍ" : "NO"));
    }

    private void bfs() {
        String inicio = leer("Archivo de inicio: ");
        List<List<Archivo>> niveles = sistema.bfs(inicio);
        System.out.println("BFS (amplitud, con cola) desde " + nombre(inicio) + ":");
        for (int i = 0; i < niveles.size(); i++) {
            System.out.println("  Nivel " + i + (i == 0 ? " (inicio)" : i == 1 ? " (referencias directas)"
                    : " (a " + i + " vínculos)") + ": " + nombres(niveles.get(i)));
        }
    }

    private void dfs() {
        String inicio = leer("Archivo de inicio: ");
        List<String> rastro = new ArrayList<>();
        List<Archivo> orden = sistema.dfs(inicio, rastro);
        System.out.println("DFS (profundidad, recursivo) desde " + nombre(inicio) + ":");
        System.out.println("  Orden de visita: " + nombres(orden));
        System.out.println("  Rastro (entra hasta el fondo de cada rama antes de retroceder):");
        for (String r : rastro) {
            System.out.println("    " + r.replace(" (", " [").replace(" KB)", " KB]"));
        }
    }

    private void rutaReferencias() {
        String o = leer("Archivo de origen: ");
        String d = leer("Archivo de destino: ");
        List<Archivo> camino = sistema.rutaDeReferencias(o, d);
        if (camino.isEmpty()) {
            System.out.println("✗ No hay forma de llegar de " + nombre(o) + " a " + nombre(d)
                    + " siguiendo los vínculos (en su dirección).");
            return;
        }
        System.out.println("✓ Ruta con menos vínculos (" + (camino.size() - 1) + "):");
        for (int i = 0; i < camino.size(); i++) {
            Archivo a = camino.get(i);
            String flecha = i == 0 ? "   " : "   └→ [" + sistema.getRed().etiqueta(camino.get(i - 1), a) + "] ";
            System.out.println(flecha + sistema.ruta(a));
        }
    }

    private void ciclos() {
        List<List<Archivo>> ciclos = sistema.referenciasCirculares();
        if (ciclos.isEmpty()) {
            System.out.println("✓ No hay referencias circulares en la red.");
            return;
        }
        System.out.println("⚠ Se encontraron " + ciclos.size() + " referencia(s) circular(es):");
        for (List<Archivo> c : ciclos) {
            System.out.println("  " + String.join(" → ", c.stream().map(Archivo::getNombre).toList()));
        }
        System.out.println("  (Por ejemplo, dos clases que se importan mutuamente son una dependencia circular.)");
    }

    private void impacto() {
        String ruta = leer("Archivo a analizar: ");
        List<List<Archivo>> niveles = sistema.impacto(ruta);
        System.out.println("Archivos que dependen de " + nombre(ruta) + " (se verían afectados si se borra o daña):");
        if (niveles.isEmpty()) {
            System.out.println("  Ninguno: ningún archivo lo usa.");
        }
        int total = 0;
        for (int i = 0; i < niveles.size(); i++) {
            total += niveles.get(i).size();
            System.out.println("  " + (i == 0 ? "Directamente" : "A " + (i + 1) + " vínculos") + ": "
                    + nombres(niveles.get(i)));
        }
        System.out.println("  Total afectados: " + total);
    }

    private void informacionCompleta() {
        System.out.println("ÁRBOL DE DIRECTORIOS");
        System.out.print(sistema.dibujarArbol(arbol.getRaiz()));
        System.out.println("  Nodos: " + arbol.contarNodos() + " | altura: " + arbol.altura()
                + " | tamaño total: " + sistema.tamanoTotalKB(arbol.getRaiz()) + " KB");
        System.out.println();
        System.out.println("RED DE VÍNCULOS (lista de adyacencia: archivo → archivos que usa)");
        for (Archivo a : sistema.getRed().vertices()) {
            List<String> destinos = new ArrayList<>();
            for (Arista<Archivo> ar : sistema.getRed().aristasDeSalida(a)) {
                destinos.add(ar.getDestino().getNombre() + " [" + ar.getEtiqueta() + "]");
            }
            System.out.printf("  %-22s → %s%n", a.getNombre(), destinos.isEmpty() ? "(sin vínculos)" : destinos);
        }
        System.out.println("  Archivos en la red: " + sistema.getRed().numeroVertices() + " | vínculos: "
                + sistema.getRed().numeroAristas());
    }

    // ========================================================= AUXILIARES

    private String nombre(String ruta) {
        return sistema.localizar(ruta).getDato().getNombre();
    }

    private static String nombres(List<Archivo> archivos) {
        List<String> n = new ArrayList<>();
        for (Archivo a : archivos) {
            n.add(a.getNombre());
        }
        return n.isEmpty() ? "(ninguno)" : String.join(", ", n);
    }

    private static String nombresNodos(List<NodoArbol<Elemento>> nodos) {
        List<String> n = new ArrayList<>();
        for (NodoArbol<Elemento> nodo : nodos) {
            n.add(nodo.getDato().getNombre());
        }
        return String.join(", ", n);
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return sc.hasNextLine() ? sc.nextLine() : "";
    }

    private long leerEntero(String mensaje) {
        try {
            return Long.parseLong(leer(mensaje).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un número entero");
        }
    }
}
