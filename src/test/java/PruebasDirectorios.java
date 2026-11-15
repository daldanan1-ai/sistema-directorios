import arbol.ArbolGeneral;
import arbol.NodoArbol;
import datos.DatosPrueba;
import grafo.GrafoDirigido;
import modelo.Archivo;
import modelo.Elemento;
import modelo.TipoVinculo;
import servicio.ResultadoEliminacion;
import servicio.SistemaDirectorios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pruebas automáticas del árbol, del grafo y de su integración.
 * Ejecutar con: java -cp out PruebasDirectorios
 */
public class PruebasDirectorios {

    private static int aprobadas = 0;
    private static int fallidas = 0;

    private static final String WEB = "C:/Proyectos/sitio-web/";
    private static final String APP = "C:/Proyectos/app-java/";

    public static void main(String[] args) {
        probarArbolGenerico();
        probarGrafoGenerico();
        probarJerarquia();
        probarRecorridosDelCaso();
        probarRed();
        probarAlgoritmosDelCaso();
        probarIntegracion();

        System.out.println();
        System.out.println("Pruebas aprobadas: " + aprobadas + " | fallidas: " + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static SistemaDirectorios cargado() {
        SistemaDirectorios s = new SistemaDirectorios();
        DatosPrueba.cargar(s);
        return s;
    }

    // ------------------------------------------------- ÁRBOL GENÉRICO

    private static void probarArbolGenerico() {
        //        A
        //      / | \
        //     B  C  D
        //    / \     \
        //   E   F     G
        //             |
        //             H
        ArbolGeneral<String> t = new ArbolGeneral<>("A", Comparator.naturalOrder());
        NodoArbol<String> d = t.insertar(t.getRaiz(), "D");
        NodoArbol<String> b = t.insertar(t.getRaiz(), "B");
        t.insertar(t.getRaiz(), "C");
        t.insertar(b, "F");
        t.insertar(b, "E");
        NodoArbol<String> g = t.insertar(d, "G");
        NodoArbol<String> h = t.insertar(g, "H");

        verificar("Árbol: los hijos quedan ordenados aunque se inserten en otro orden",
                nombres(t.getRaiz().getHijos()).equals("B C D") && nombres(b.getHijos()).equals("E F"));
        verificar("Árbol: preorden = A B E F C D G H", nombres(t.preorden(t.getRaiz())).equals("A B E F C D G H"));
        verificar("Árbol: postorden = E F B C H G D A", nombres(t.postorden(t.getRaiz())).equals("E F B C H G D A"));
        List<List<NodoArbol<String>>> niveles = t.porNiveles(t.getRaiz());
        verificar("Árbol: por niveles = [A] [B C D] [E F G] [H]", niveles.size() == 4
                && nombres(niveles.get(1)).equals("B C D") && nombres(niveles.get(2)).equals("E F G"));
        verificar("Árbol: nivel de la raíz 0 y de H 3", t.nivel(t.getRaiz()) == 0 && t.nivel(h) == 3);
        verificar("Árbol: altura total 3, de B 1, de una hoja 0",
                t.altura() == 3 && t.altura(b) == 1 && t.altura(h) == 0);
        verificar("Árbol: padre de H es G", h.getPadre() == g);
        verificar("Árbol: no se puede mover D dentro de su descendiente G", lanza(() -> t.mover(d, g)));
        List<NodoArbol<String>> eliminados = t.eliminar(d);
        verificar("Árbol: eliminar D quita su subárbol (devuelto en postorden H G D)",
                nombres(eliminados).equals("H G D") && t.contarNodos() == 5 && t.altura() == 2);
        verificar("Árbol: no se puede eliminar la raíz", lanza(() -> t.eliminar(t.getRaiz())));
        verificar("Árbol: buscar inexistente devuelve null", t.buscar(x -> x.equals("Z")) == null);
    }

    // ------------------------------------------------- GRAFO GENÉRICO

    private static void probarGrafoGenerico() {
        GrafoDirigido<String> g = new GrafoDirigido<>();
        for (String v : List.of("A", "B", "C", "D", "E")) {
            g.agregarVertice(v);
        }
        g.agregarArista("A", "B", "x");
        g.agregarArista("A", "C", "x");
        g.agregarArista("B", "D", "x");
        g.agregarArista("C", "D", "x");
        g.agregarArista("D", "E", "x");
        verificar("Grafo: dirigido (A→B existe, B→A no)", g.existeArista("A", "B") && !g.existeArista("B", "A"));
        verificar("Grafo: grados de D (entrada 2, salida 1)", g.gradoEntrada("D") == 2 && g.gradoSalida("D") == 1);
        verificar("Grafo: vértice repetido no se agrega", !g.agregarVertice("A"));
        verificar("Grafo: arista duplicada es rechazada", lanza(() -> g.agregarArista("A", "B", "x")));
        verificar("Grafo: arista a sí mismo es rechazada", lanza(() -> g.agregarArista("A", "A", "x")));
        verificar("Grafo: arista con vértice inexistente es rechazada", lanza(() -> g.agregarArista("A", "Z", "x")));
        verificar("Grafo: BFS por niveles [A] [B C] [D] [E]", g.bfs("A").toString().equals("[[A], [B, C], [D], [E]]"));
        verificar("Grafo: DFS en profundidad A B D E C", g.dfs("A").toString().equals("[A, B, D, E, C]"));
        verificar("Grafo: camino más corto A→E tiene 3 aristas", g.caminoMasCorto("A", "E").size() == 4);
        verificar("Grafo: sin camino en contra de la dirección (E→A)", g.caminoMasCorto("E", "A").isEmpty());
        verificar("Grafo: sin ciclos", g.detectarCiclos().isEmpty());
        g.agregarArista("E", "B", "x");
        verificar("Grafo: detecta el ciclo B→D→E→B", g.detectarCiclos().toString().equals("[[B, D, E, B]]"));
        verificar("Grafo: dependientes de D por niveles [[B, C], [A, E]]",
                g.dependientes("D").toString().equals("[[B, C], [A, E]]"));
        List<String> apuntaban = g.eliminarVertice("D");
        verificar("Grafo: eliminar D borra las aristas que llegaban (desde B y C)",
                apuntaban.equals(List.of("B", "C")) && g.gradoSalida("B") == 0 && g.numeroAristas() == 3);
        verificar("Grafo: eliminar arista inexistente devuelve false", !g.eliminarArista("C", "A"));
    }

    // ---------------------------------------------- JERARQUÍA DEL CASO

    private static void probarJerarquia() {
        SistemaDirectorios s = cargado();
        ArbolGeneral<Elemento> t = s.getArbol();
        verificar("Datos: el árbol tiene al menos 15 nodos (32)", t.contarNodos() == 32);
        verificar("Datos: altura 5 (hay profundidad, no todo cuelga de la raíz)", t.altura() == 5);
        verificar("Buscar por ruta existente (sin distinguir mayúsculas ni separador)",
                s.localizar("c:\\proyectos\\SITIO-WEB\\index.html").getDato().getNombre().equals("index.html"));
        verificar("Buscar por ruta inexistente lanza error", lanza(() -> s.localizar("C:/Nada/x.txt")));
        verificar("Buscar por nombre encuentra .xlsx en C: y en D:", s.buscarPorNombre(".xlsx").size() == 2);
        verificar("Hijos de sitio-web: carpetas primero y luego archivos",
                nombresElementos(s.localizar(WEB).getHijos()).equals("css img js contacto.html index.html"));
        verificar("Padre de estilos.css es css", s.localizar(WEB + "css/estilos.css").getPadre().getDato().getNombre()
                .equals("css"));
        verificar("Nivel: C: = 1, index.html = 4, logo.png = 5",
                t.nivel(s.localizar("C:")) == 1 && t.nivel(s.localizar(WEB + "index.html")) == 4
                        && t.nivel(s.localizar(WEB + "img/logo.png")) == 5);
        verificar("Tamaño de la carpeta js = 19 KB (postorden)", s.tamanoTotalKB(s.localizar(WEB + "js")) == 19);

        NodoArbol<Elemento> nuevo = s.crearArchivo(APP, "Vista.java", 3);
        verificar("Insertar archivo en el nivel 4 bajo app-java", t.nivel(nuevo) == 4
                && nuevo.getPadre() == s.localizar(APP));
        NodoArbol<Elemento> carpeta = s.crearCarpeta("D:/Compartido", "Fotos");
        verificar("Insertar carpeta en otro nivel (3) y en otra unidad", t.nivel(carpeta) == 3);
        verificar("Nombre repetido en la misma carpeta es rechazado", lanza(() -> s.crearArchivo(APP, "main.JAVA", 1)));
        verificar("No se crea nada dentro de un archivo", lanza(() -> s.crearCarpeta(WEB + "index.html", "x")));
        verificar("En la raíz solo se crean unidades", lanza(() -> s.crearCarpeta("", "Temp")));
        verificar("Archivo sin extensión es rechazado", lanza(() -> s.crearArchivo(APP, "LEEME", 1)));
        verificar("Nombre con caracteres inválidos es rechazado", lanza(() -> s.crearCarpeta("C:", "a/b")));
        verificar("No se puede eliminar una unidad", lanza(() -> s.eliminar("D:")));
    }

    private static void probarRecorridosDelCaso() {
        SistemaDirectorios s = cargado();
        ArbolGeneral<Elemento> t = s.getArbol();
        NodoArbol<Elemento> web = s.localizar(WEB);
        verificar("Preorden de sitio-web: cada carpeta antes de su contenido",
                nombresElementos(t.preorden(web)).startsWith("sitio-web css estilos.css img logo.png js"));
        verificar("Postorden de sitio-web: cada carpeta después de su contenido y sitio-web al final",
                nombresElementos(t.postorden(web)).startsWith("estilos.css css logo.png img")
                        && nombresElementos(t.postorden(web)).endsWith("index.html sitio-web"));
        verificar("Por niveles de sitio-web: 3 niveles", t.porNiveles(web).size() == 3
                && t.porNiveles(web).get(1).size() == 5);
    }

    // ---------------------------------------------------- RED DEL CASO

    private static void probarRed() {
        SistemaDirectorios s = cargado();
        verificar("Datos: la red tiene al menos 12 vértices (17) y 18 vínculos (20)",
                s.getRed().numeroVertices() == 17 && s.getRed().numeroAristas() == 20);
        verificar("index.html tiene 4 vínculos de salida", s.vinculosSalientes(WEB + "index.html").size() == 4);
        verificar("logo.png es usado por 4 archivos", s.vinculosEntrantes(WEB + "img/logo.png").size() == 4);
        verificar("Vínculo existente index.html → contacto.html", s.existeVinculo(WEB + "index.html", WEB + "contacto.html"));
        verificar("Vínculo inexistente contacto.html → index.html (es dirigido)",
                !s.existeVinculo(WEB + "contacto.html", WEB + "index.html"));
        s.crearVinculo(WEB + "contacto.html", WEB + "img/logo.png", TipoVinculo.IMAGEN);
        verificar("Crear vínculo nuevo", s.existeVinculo(WEB + "contacto.html", WEB + "img/logo.png"));
        verificar("Eliminar vínculo", s.eliminarVinculo(WEB + "contacto.html", WEB + "img/logo.png")
                && !s.existeVinculo(WEB + "contacto.html", WEB + "img/logo.png"));
        verificar("Una carpeta no puede participar en la red", lanza(() -> s.incluirEnRed(WEB + "css")));
        verificar("Incluir un archivo que ya está en la red es rechazado",
                lanza(() -> s.incluirEnRed(WEB + "index.html")));
        s.crearArchivo(WEB, "nuevo.html", 1);
        s.crearVinculo(WEB + "nuevo.html", WEB + "index.html", TipoVinculo.HIPERVINCULO);
        verificar("Crear un vínculo incluye automáticamente al archivo en la red",
                s.getRed().numeroVertices() == 18);
    }

    private static void probarAlgoritmosDelCaso() {
        SistemaDirectorios s = cargado();
        List<List<Archivo>> bfs = s.bfs(WEB + "index.html");
        List<Archivo> dfs = s.dfs(WEB + "index.html", null);
        verificar("BFS desde index.html: nivel 1 = sus 4 referencias directas", bfs.get(1).size() == 4);
        verificar("BFS y DFS visitan los mismos 6 archivos en distinto orden",
                dfs.size() == 6 && !nombresArchivos(dfs).equals(nombresArchivos(aplanar(bfs))));
        verificar("DFS baja por estilos.css hasta logo.png antes de ir a app.js",
                nombresArchivos(dfs).equals("index.html estilos.css logo.png app.js utilidades.js contacto.html"));
        List<Archivo> ruta = s.rutaDeReferencias("D:/Compartido/manual.pdf", WEB + "js/utilidades.js");
        verificar("Ruta manual.pdf → utilidades.js con 3 vínculos",
                nombresArchivos(ruta).equals("manual.pdf index.html app.js utilidades.js"));
        verificar("Ruta más corta index.html → logo.png es directa (1 vínculo), no por estilos.css",
                s.rutaDeReferencias(WEB + "index.html", WEB + "img/logo.png").size() == 2);
        verificar("Sin ruta desde un archivo aislado",
                s.rutaDeReferencias("D:/Respaldos/respaldo_datos.xlsx", WEB + "index.html").isEmpty());
        verificar("Detecta la referencia circular Menu.java → Gestor.java → Menu.java",
                s.referenciasCirculares().size() == 1
                        && nombresArchivos(s.referenciasCirculares().get(0)).equals("Menu.java Gestor.java Menu.java"));
        s.eliminarVinculo(APP + "Gestor.java", APP + "Menu.java");
        verificar("Al quitar Gestor → Menu desaparece el ciclo", s.referenciasCirculares().isEmpty());
        verificar("Impacto de logo.png: 4 directos y 8 en total",
                s.impacto(WEB + "img/logo.png").get(0).size() == 4 && aplanar(s.impacto(WEB + "img/logo.png")).size() == 8);
    }

    // ------------------------------------------------------ INTEGRACIÓN

    private static void probarIntegracion() {
        SistemaDirectorios s = cargado();
        s.renombrar(WEB + "index.html", "inicio.html");
        verificar("Renombrar conserva los vínculos del archivo", s.vinculosSalientes(WEB + "inicio.html").size() == 4);
        s.mover(WEB + "js", "D:/Compartido");
        verificar("Mover una carpeta cambia la ruta y el nivel pero conserva los vínculos",
                s.existeVinculo(WEB + "inicio.html", "D:/Compartido/js/app.js")
                        && s.getArbol().nivel(s.localizar("D:/Compartido/js/app.js")) == 4);
        ResultadoEliminacion r = s.eliminar(WEB + "img");
        verificar("Eliminar carpeta img: 2 elementos del árbol y logo.png de la red",
                r.getElementosEliminados() == 2 && r.getRetiradosDeRed().equals(List.of("logo.png"))
                        && s.getRed().numeroVertices() == 16);
        verificar("Eliminar informa 4 referencias rotas", r.getReferenciasRotas().size() == 4);
        verificar("Tras eliminar, no queda ningún vínculo hacia el archivo borrado",
                s.getRed().numeroAristas() == 16 && s.vinculosSalientes(WEB + "css/estilos.css").isEmpty());
        verificar("Tras eliminar, la ruta ya no existe", lanza(() -> s.localizar(WEB + "img/logo.png")));
        List<Archivo> apuntaban = s.retirarDeRed("C:/Usuarios/Domingo/Documentos/informe.docx");
        verificar("Retirar de la red: el archivo sigue en el árbol y se eliminan los vínculos que le llegaban",
                apuntaban.size() == 2 && s.localizar("C:/Usuarios/Domingo/Documentos/informe.docx") != null);
    }

    // ------------------------------------------------------- AUXILIARES

    private static <T> List<T> aplanar(List<List<T>> niveles) {
        List<T> r = new ArrayList<>();
        niveles.forEach(r::addAll);
        return r;
    }

    private static String nombres(List<NodoArbol<String>> nodos) {
        StringBuilder sb = new StringBuilder();
        for (NodoArbol<String> n : nodos) {
            sb.append(sb.length() == 0 ? "" : " ").append(n.getDato());
        }
        return sb.toString();
    }

    private static String nombresElementos(List<NodoArbol<Elemento>> nodos) {
        StringBuilder sb = new StringBuilder();
        for (NodoArbol<Elemento> n : nodos) {
            sb.append(sb.length() == 0 ? "" : " ").append(n.getDato().getNombre());
        }
        return sb.toString();
    }

    private static String nombresArchivos(List<Archivo> archivos) {
        StringBuilder sb = new StringBuilder();
        for (Archivo a : archivos) {
            sb.append(sb.length() == 0 ? "" : " ").append(a.getNombre());
        }
        return sb.toString();
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aprobadas++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallidas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }
}
