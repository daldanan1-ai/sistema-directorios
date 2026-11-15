package datos;

import modelo.TipoVinculo;
import servicio.SistemaDirectorios;

/**
 * Datos de prueba.
 *
 * ÁRBOL (32 nodos, altura 5: la raíz está en el nivel 0 y los archivos más profundos en el 5):
 *   Este equipo
 *   ├── C: (Disco local)
 *   │   ├── Proyectos/ ── sitio-web/ (css/, img/, js/, contacto.html, index.html)
 *   │   │               └─ app-java/ (Gestor, Main, Menu, Modelo .java)
 *   │   └── Usuarios/ ── Domingo/ ── Documentos/ (datos.xlsx, informe.docx, presentacion.pptx)
 *   │                             └─ Escritorio/ (Acceso a informe.lnk)
 *   └── D: (Datos) ── Compartido/ (manual.pdf, plantilla.docx), Respaldos/ (respaldo_datos.xlsx)
 *
 * GRAFO (17 archivos, 20 vínculos dirigidos). Casos preparados:
 *  - Varias conexiones: index.html sale hacia 4 archivos; logo.png recibe 4.
 *  - Rutas alternativas: index -> logo (directo) y index -> estilos -> logo.
 *  - Camino de más de dos conexiones: manual.pdf -> index -> app.js -> utilidades.js.
 *  - Ciclo (dependencia circular): Menu.java -> Gestor.java -> Menu.java.
 *  - Vértice aislado: respaldo_datos.xlsx (está en la red pero sin vínculos).
 *  - Vínculos entre ramas distintas del árbol e incluso entre unidades (D: -> C:).
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static void cargar(SistemaDirectorios s) {
        // ---------------------------------------------------------- ÁRBOL
        s.crearUnidad("C", "Disco local");
        s.crearUnidad("D", "Datos");

        s.crearCarpeta("C:", "Proyectos");
        s.crearCarpeta("C:/Proyectos", "sitio-web");
        s.crearArchivo("C:/Proyectos/sitio-web", "index.html", 12);
        s.crearArchivo("C:/Proyectos/sitio-web", "contacto.html", 8);
        s.crearCarpeta("C:/Proyectos/sitio-web", "css");
        s.crearArchivo("C:/Proyectos/sitio-web/css", "estilos.css", 6);
        s.crearCarpeta("C:/Proyectos/sitio-web", "js");
        s.crearArchivo("C:/Proyectos/sitio-web/js", "app.js", 15);
        s.crearArchivo("C:/Proyectos/sitio-web/js", "utilidades.js", 4);
        s.crearCarpeta("C:/Proyectos/sitio-web", "img");
        s.crearArchivo("C:/Proyectos/sitio-web/img", "logo.png", 85);

        s.crearCarpeta("C:/Proyectos", "app-java");
        s.crearArchivo("C:/Proyectos/app-java", "Main.java", 2);
        s.crearArchivo("C:/Proyectos/app-java", "Menu.java", 9);
        s.crearArchivo("C:/Proyectos/app-java", "Gestor.java", 14);
        s.crearArchivo("C:/Proyectos/app-java", "Modelo.java", 5);

        s.crearCarpeta("C:", "Usuarios");
        s.crearCarpeta("C:/Usuarios", "Domingo");
        s.crearCarpeta("C:/Usuarios/Domingo", "Documentos");
        s.crearArchivo("C:/Usuarios/Domingo/Documentos", "informe.docx", 340);
        s.crearArchivo("C:/Usuarios/Domingo/Documentos", "datos.xlsx", 120);
        s.crearArchivo("C:/Usuarios/Domingo/Documentos", "presentacion.pptx", 2048);
        s.crearCarpeta("C:/Usuarios/Domingo", "Escritorio");
        s.crearArchivo("C:/Usuarios/Domingo/Escritorio", "Acceso a informe.lnk", 1);

        s.crearCarpeta("D:", "Respaldos");
        s.crearArchivo("D:/Respaldos", "respaldo_datos.xlsx", 118);
        s.crearCarpeta("D:", "Compartido");
        s.crearArchivo("D:/Compartido", "manual.pdf", 1500);
        s.crearArchivo("D:/Compartido", "plantilla.docx", 60);

        // ---------------------------------------------------------- GRAFO
        String web = "C:/Proyectos/sitio-web/";
        String app = "C:/Proyectos/app-java/";
        String docs = "C:/Usuarios/Domingo/Documentos/";

        s.crearVinculo(web + "index.html", web + "css/estilos.css", TipoVinculo.HOJA_DE_ESTILO);
        s.crearVinculo(web + "index.html", web + "js/app.js", TipoVinculo.SCRIPT);
        s.crearVinculo(web + "index.html", web + "img/logo.png", TipoVinculo.IMAGEN);
        s.crearVinculo(web + "index.html", web + "contacto.html", TipoVinculo.HIPERVINCULO);
        s.crearVinculo(web + "contacto.html", web + "css/estilos.css", TipoVinculo.HOJA_DE_ESTILO);
        s.crearVinculo(web + "contacto.html", web + "js/app.js", TipoVinculo.SCRIPT);
        s.crearVinculo(web + "js/app.js", web + "js/utilidades.js", TipoVinculo.IMPORTA);
        s.crearVinculo(web + "css/estilos.css", web + "img/logo.png", TipoVinculo.IMAGEN);

        s.crearVinculo(app + "Main.java", app + "Menu.java", TipoVinculo.IMPORTA);
        s.crearVinculo(app + "Main.java", app + "Gestor.java", TipoVinculo.IMPORTA);
        s.crearVinculo(app + "Menu.java", app + "Gestor.java", TipoVinculo.IMPORTA);
        s.crearVinculo(app + "Gestor.java", app + "Modelo.java", TipoVinculo.IMPORTA);
        s.crearVinculo(app + "Gestor.java", app + "Menu.java", TipoVinculo.IMPORTA);   // dependencia circular

        s.crearVinculo(docs + "informe.docx", docs + "datos.xlsx", TipoVinculo.DATOS_VINCULADOS);
        s.crearVinculo(docs + "informe.docx", web + "img/logo.png", TipoVinculo.IMAGEN);
        s.crearVinculo(docs + "presentacion.pptx", docs + "datos.xlsx", TipoVinculo.DATOS_VINCULADOS);
        s.crearVinculo(docs + "presentacion.pptx", docs + "informe.docx", TipoVinculo.HIPERVINCULO);
        s.crearVinculo("C:/Usuarios/Domingo/Escritorio/Acceso a informe.lnk", docs + "informe.docx",
                TipoVinculo.ACCESO_DIRECTO);
        s.crearVinculo("D:/Compartido/manual.pdf", web + "index.html", TipoVinculo.HIPERVINCULO);
        s.crearVinculo("D:/Compartido/plantilla.docx", web + "img/logo.png", TipoVinculo.IMAGEN);

        s.incluirEnRed("D:/Respaldos/respaldo_datos.xlsx");   // vértice aislado
    }
}
