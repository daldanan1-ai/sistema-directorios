package servicio;

import arbol.ArbolGeneral;
import arbol.NodoArbol;
import grafo.Arista;
import grafo.GrafoDirigido;
import modelo.Archivo;
import modelo.Carpeta;
import modelo.Elemento;
import modelo.TipoVinculo;
import modelo.Unidad;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema de directorios: integra las DOS estructuras del problema.
 *
 *  - ÁRBOL GENERAL: la jerarquía Este equipo > unidades > carpetas > archivos.
 *    Cada elemento tiene un único contenedor (padre) y un contenedor puede
 *    tener cualquier cantidad de elementos (hijos).
 *
 *  - GRAFO DIRIGIDO: los vínculos ENTRE ARCHIVOS (hojas de estilo, scripts,
 *    imágenes, hipervínculos, imports, datos vinculados, accesos directos).
 *    Un archivo puede apuntar a muchos y ser apuntado por muchos, incluso
 *    desde otra unidad: eso no es una jerarquía.
 *
 * Los vértices del grafo son LOS MISMOS objetos Archivo que están en el árbol.
 */
public class SistemaDirectorios {

    /** Orden de los hijos: unidades, luego carpetas, luego archivos; cada grupo por nombre. */
    private static final Comparator<Elemento> ORDEN_EXPLORADOR = Comparator
            .comparingInt(SistemaDirectorios::rangoTipo)
            .thenComparing(Elemento::getNombre, String.CASE_INSENSITIVE_ORDER);

    private final ArbolGeneral<Elemento> arbol = new ArbolGeneral<>(new Carpeta("Este equipo"), ORDEN_EXPLORADOR);
    private final GrafoDirigido<Archivo> red = new GrafoDirigido<>();
    /** Índice elemento -> nodo del árbol: permite obtener la ruta de un archivo del grafo. */
    private final Map<Elemento, NodoArbol<Elemento>> nodoDe = new HashMap<>();

    public SistemaDirectorios() {
        nodoDe.put(arbol.getRaiz().getDato(), arbol.getRaiz());
    }

    private static int rangoTipo(Elemento e) {
        if (e instanceof Unidad) {
            return 0;
        }
        return e instanceof Carpeta ? 1 : 2;
    }

    // ================================================================ RUTAS

    /** "C:/Proyectos/sitio-web/index.html". La raíz se llama "Este equipo". */
    public String ruta(Elemento e) {
        NodoArbol<Elemento> nodo = nodoDe.get(e);
        if (nodo == null) {
            return e.getNombre() + " (eliminado)";
        }
        if (nodo.esRaiz()) {
            return nodo.getDato().getNombre();
        }
        StringBuilder sb = new StringBuilder();
        for (NodoArbol<Elemento> n : arbol.caminoDesdeRaiz(nodo)) {
            if (!n.esRaiz()) {
                sb.append(sb.length() == 0 ? "" : "/").append(n.getDato().getNombre());
            }
        }
        return sb.toString();
    }

    /**
     * Ubica un nodo a partir de su ruta, bajando por el árbol desde la raíz y
     * eligiendo en cada nivel el hijo con ese nombre (sin distinguir mayúsculas,
     * como en Windows). Acepta "/" o "\" como separador.
     */
    public NodoArbol<Elemento> localizar(String ruta) {
        if (ruta == null || ruta.isBlank() || ruta.trim().equalsIgnoreCase("Este equipo")) {
            return arbol.getRaiz();
        }
        String[] partes = ruta.trim().replace('\\', '/').replaceAll("/+$", "").split("/+");
        NodoArbol<Elemento> actual = arbol.getRaiz();
        for (int i = 0; i < partes.length; i++) {
            String buscado = i == 0 && partes[i].matches("(?i)[a-z]") ? partes[i] + ":" : partes[i];
            NodoArbol<Elemento> siguiente = null;
            for (NodoArbol<Elemento> hijo : actual.getHijos()) {
                if (hijo.getDato().getNombre().equalsIgnoreCase(buscado)) {
                    siguiente = hijo;
                }
            }
            if (siguiente == null) {
                throw new IllegalArgumentException("No existe \"" + buscado + "\" dentro de " + ruta(actual.getDato()));
            }
            actual = siguiente;
        }
        return actual;
    }

    private Archivo archivoEn(String ruta) {
        Elemento e = localizar(ruta).getDato();
        if (!(e instanceof Archivo)) {
            throw new IllegalArgumentException(ruta(e) + " es una " + e.getTipo().toLowerCase()
                    + "; solo los archivos participan en la red de vínculos");
        }
        return (Archivo) e;
    }

    // ===================================================== ÁRBOL: INSERTAR

    public NodoArbol<Elemento> crearUnidad(String letra, String etiqueta) {
        return insertar(arbol.getRaiz(), new Unidad(letra, etiqueta));
    }

    public NodoArbol<Elemento> crearCarpeta(String rutaPadre, String nombre) {
        return insertar(localizar(rutaPadre), new Carpeta(nombre));
    }

    public NodoArbol<Elemento> crearArchivo(String rutaPadre, String nombre, long tamanoKB) {
        return insertar(localizar(rutaPadre), new Archivo(nombre, tamanoKB));
    }

    /** Reglas de la jerarquía antes de insertar en el árbol. */
    private NodoArbol<Elemento> insertar(NodoArbol<Elemento> padre, Elemento nuevo) {
        Elemento p = padre.getDato();
        if (!p.esContenedor()) {
            throw new IllegalArgumentException(ruta(p) + " es un archivo: no puede contener elementos");
        }
        if (padre.esRaiz() && !(nuevo instanceof Unidad)) {
            throw new IllegalArgumentException("En \"Este equipo\" solo se pueden crear unidades (C:, D:, ...)");
        }
        if (!padre.esRaiz() && nuevo instanceof Unidad) {
            throw new IllegalArgumentException("Las unidades solo se crean directamente en \"Este equipo\"");
        }
        validarNombreLibre(padre, nuevo.getNombre(), null);
        NodoArbol<Elemento> nodo = arbol.insertar(padre, nuevo);
        nodoDe.put(nuevo, nodo);
        return nodo;
    }

    /** En una misma carpeta no puede haber dos elementos con el mismo nombre. */
    private void validarNombreLibre(NodoArbol<Elemento> padre, String nombre, NodoArbol<Elemento> excepto) {
        for (NodoArbol<Elemento> hijo : padre.getHijos()) {
            if (hijo != excepto && hijo.getDato().getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe \"" + hijo.getDato().getNombre() + "\" en "
                        + ruta(padre.getDato()));
            }
        }
    }

    // ================================================ ÁRBOL: CONSULTAR

    public ArbolGeneral<Elemento> getArbol() {
        return arbol;
    }

    public List<NodoArbol<Elemento>> buscarPorNombre(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Escriba el nombre o parte del nombre a buscar");
        }
        return arbol.buscarTodos(e -> e.getNombre().toLowerCase().contains(t));
    }

    /** Tamaño total = tamaño propio + tamaño de todo el contenido (recorrido en POSTORDEN). */
    public long tamanoTotalKB(NodoArbol<Elemento> nodo) {
        long total = 0;
        for (NodoArbol<Elemento> n : arbol.postorden(nodo)) {
            total += n.getDato().getTamanoPropioKB();
        }
        return total;
    }

    public String dibujarArbol(NodoArbol<Elemento> desde) {
        return arbol.representar(desde, e -> {
            if (nodoDe.get(e) == arbol.getRaiz()) {
                return e.getNombre();
            }
            return e instanceof Archivo && red.contieneVertice((Archivo) e) ? e + "  ⇄ en red" : e.toString();
        });
    }

    // =============================================== ÁRBOL: MODIFICAR

    /** Renombra sin tocar las referencias padre-hijo ni los vínculos del grafo (usan el id). */
    public Elemento renombrar(String ruta, String nuevoNombre) {
        NodoArbol<Elemento> nodo = localizar(ruta);
        if (nodo.esRaiz() || nodo.getDato() instanceof Unidad) {
            throw new IllegalArgumentException("No se puede renombrar \"Este equipo\" ni una unidad");
        }
        validarNombreLibre(nodo.getPadre(), nuevoNombre, nodo);
        nodo.getDato().setNombre(nuevoNombre);
        arbol.reordenar(nodo);
        return nodo.getDato();
    }

    /** Mueve un elemento (y todo su contenido) a otra carpeta o unidad. */
    public Elemento mover(String ruta, String rutaDestino) {
        NodoArbol<Elemento> nodo = localizar(ruta);
        NodoArbol<Elemento> destino = localizar(rutaDestino);
        if (nodo.esRaiz() || nodo.getDato() instanceof Unidad) {
            throw new IllegalArgumentException("No se puede mover \"Este equipo\" ni una unidad");
        }
        if (!destino.getDato().esContenedor() || destino.esRaiz()) {
            throw new IllegalArgumentException("El destino debe ser una unidad o una carpeta");
        }
        validarNombreLibre(destino, nodo.getDato().getNombre(), nodo);
        arbol.mover(nodo, destino);
        return nodo.getDato();
    }

    /**
     * POLÍTICA DE ELIMINACIÓN: como en un sistema de archivos real, eliminar una
     * carpeta elimina TODO su contenido (subárbol). Los archivos eliminados se
     * retiran también de la red de vínculos, y se informa qué archivos que
     * siguen existiendo quedaron con referencias rotas. Las unidades y la raíz
     * no se pueden eliminar.
     */
    public ResultadoEliminacion eliminar(String ruta) {
        NodoArbol<Elemento> nodo = localizar(ruta);
        if (nodo.esRaiz() || nodo.getDato() instanceof Unidad) {
            throw new IllegalArgumentException("No se puede eliminar \"Este equipo\" ni una unidad");
        }
        String rutaEliminada = ruta(nodo.getDato());
        List<NodoArbol<Elemento>> eliminados = arbol.eliminar(nodo);
        ResultadoEliminacion resultado = new ResultadoEliminacion(rutaEliminada, eliminados.size());
        List<Archivo> archivosEliminados = new ArrayList<>();
        for (NodoArbol<Elemento> n : eliminados) {
            if (n.getDato() instanceof Archivo && red.contieneVertice((Archivo) n.getDato())) {
                archivosEliminados.add((Archivo) n.getDato());
            }
        }
        for (Archivo a : archivosEliminados) {
            for (Archivo quienApuntaba : red.eliminarVertice(a)) {
                if (!archivosEliminados.contains(quienApuntaba)) {
                    resultado.agregarReferenciaRota(ruta(quienApuntaba) + " apuntaba a " + a.getNombre());
                }
            }
            resultado.agregarRetiradoDeRed(a.getNombre());
        }
        for (NodoArbol<Elemento> n : eliminados) {
            nodoDe.remove(n.getDato());
        }
        return resultado;
    }

    // ============================================== GRAFO: ADMINISTRAR

    public GrafoDirigido<Archivo> getRed() {
        return red;
    }

    /** Agrega un archivo existente del árbol como vértice de la red. */
    public Archivo incluirEnRed(String rutaArchivo) {
        Archivo a = archivoEn(rutaArchivo);
        if (!red.agregarVertice(a)) {
            throw new IllegalArgumentException(ruta(a) + " ya está en la red de vínculos");
        }
        return a;
    }

    /** Crea el vínculo origen -> destino. Si alguno no estaba en la red, se incluye. */
    public void crearVinculo(String rutaOrigen, String rutaDestino, TipoVinculo tipo) {
        Archivo origen = archivoEn(rutaOrigen);
        Archivo destino = archivoEn(rutaDestino);
        red.agregarVertice(origen);
        red.agregarVertice(destino);
        red.agregarArista(origen, destino, tipo.name());
    }

    public boolean eliminarVinculo(String rutaOrigen, String rutaDestino) {
        return red.eliminarArista(archivoEn(rutaOrigen), archivoEn(rutaDestino));
    }

    public boolean existeVinculo(String rutaOrigen, String rutaDestino) {
        return red.existeArista(archivoEn(rutaOrigen), archivoEn(rutaDestino));
    }

    /** Retira un archivo de la red (sigue existiendo en el árbol). Devuelve quiénes apuntaban a él. */
    public List<Archivo> retirarDeRed(String rutaArchivo) {
        return red.eliminarVertice(archivoEn(rutaArchivo));
    }

    public Archivo archivoDeRed(String rutaArchivo) {
        Archivo a = archivoEn(rutaArchivo);
        if (!red.contieneVertice(a)) {
            throw new IllegalArgumentException(ruta(a) + " no está en la red de vínculos");
        }
        return a;
    }

    public List<Arista<Archivo>> vinculosSalientes(String ruta) {
        return red.aristasDeSalida(archivoDeRed(ruta));
    }

    public List<Archivo> vinculosEntrantes(String ruta) {
        return red.predecesores(archivoDeRed(ruta));
    }

    // =============================================== GRAFO: ALGORITMOS

    public List<List<Archivo>> bfs(String rutaInicio) {
        return red.bfs(archivoDeRed(rutaInicio));
    }

    public List<Archivo> dfs(String rutaInicio, List<String> rastro) {
        return red.dfs(archivoDeRed(rutaInicio), rastro);
    }

    public List<Archivo> rutaDeReferencias(String rutaOrigen, String rutaDestino) {
        return red.caminoMasCorto(archivoDeRed(rutaOrigen), archivoDeRed(rutaDestino));
    }

    public List<List<Archivo>> referenciasCirculares() {
        return red.detectarCiclos();
    }

    public List<List<Archivo>> impacto(String rutaArchivo) {
        return red.dependientes(archivoDeRed(rutaArchivo));
    }
}
