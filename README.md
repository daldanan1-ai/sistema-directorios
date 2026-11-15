# Sistema de directorios: árbol y grafo

**Actividad:** Estructuras de Datos – Unidad IV: *Árboles y Grafos aplicados a la solución de problemas*.
**Caso asignado:** 1. Sistema de Directorios.
**Estudiante:** Domingo Antonio Aldana Noel – Ingeniería de Software, Universidad de Cartagena.

## Descripción

Aplicación de consola en Java que administra **unidades, carpetas, subcarpetas y archivos**, y además los **vínculos entre archivos**: hojas de estilo, scripts, imágenes, hipervínculos, imports, datos vinculados y accesos directos.

| Parte del problema | Estructura | Justificación |
|---|---|---|
| Jerarquía *Este equipo → unidades → carpetas → archivos* | **Árbol general** (`ArbolGeneral<T>`, `NodoArbol<T>`) | Cada elemento está en **una sola** carpeta (un padre) y una carpeta puede tener **cualquier cantidad** de hijos. Un árbol binario limitaría a dos el contenido de cada carpeta. |
| Vínculos *archivo → archivo* | **Grafo dirigido, no ponderado**, con **lista de adyacencia** (`GrafoDirigido<T>`) | Un archivo puede usar a muchos y ser usado por muchos, incluso desde otra rama u otra unidad, y pueden existir ciclos: eso no es una jerarquía. Es **dirigido** porque "index.html usa estilos.css" no implica lo contrario. **Sin pesos** porque todos los vínculos valen lo mismo; lo relevante es cuántos saltos hay. |

**Integración:** los vértices del grafo son **los mismos objetos `Archivo`** que están en el árbol. Renombrar o mover un archivo conserva sus vínculos, porque el grafo usa el objeto (identificado por un id inmutable) y no su nombre. Eliminar una carpeta del árbol retira sus archivos del grafo e informa qué archivos quedaron con **referencias rotas**.

## Organización del proyecto

```
src/main/java/
  Main.java                          Carga los datos e inicia el menú
  modelo/      Elemento (abstracta), Unidad, Carpeta, Archivo, TipoVinculo
  arbol/       NodoArbol<T> (dato, padre, lista de hijos), ArbolGeneral<T>
  grafo/       Arista<T> (destino, etiqueta), GrafoDirigido<T> (Map<T, List<Arista<T>>>)
  servicio/    SistemaDirectorios (reglas e integración), ResultadoEliminacion
  datos/       DatosPrueba (32 nodos en el árbol; 17 archivos y 20 vínculos en el grafo)
  vista/       MenuConsola
src/test/java/
  PruebasDirectorios.java            72 pruebas automáticas
```

## Árbol

- **Raíz:** "Este equipo" (nivel 0). Unidades en el nivel 1. Las carpetas y los archivos van debajo.
- **Inserción:** bajo un padre indicado por su **ruta** (`C:/Proyectos/sitio-web`). Los hijos se mantienen ordenados: carpetas primero y luego archivos, cada grupo por nombre.
- **Búsqueda:** por ruta (bajando nivel por nivel) o por parte del nombre (recorrido completo).
- **Modificación:** renombrar (valida que el nombre no se repita en la carpeta) y mover con todo el contenido (no permite mover una carpeta dentro de sí misma).
- **Eliminación:** como en un sistema de archivos real, eliminar una carpeta elimina **todo su subárbol**, previa confirmación. La raíz y las unidades no se eliminan.
- **Consultas:** hijos, padre, nivel, altura del subárbol y del árbol, y tamaño total (suma en postorden).
- **Recorridos:** **preorden** (carpeta antes que su contenido), **postorden** (contenido antes que la carpeta) y **por niveles** (con cola).

## Grafo

- **Operaciones:** agregar y retirar archivos de la red (al retirar se eliminan las aristas que llegaban), crear y eliminar vínculos, ver vínculos de salida y de entrada, verificar un vínculo directo, y calcular el grado de entrada, de salida y total.
- **BFS:** con `ArrayDeque` como cola. Muestra los archivos por **nivel de distancia**.
- **DFS:** recursivo. Muestra el orden y el **rastro** de cuándo entra y cuándo retrocede.
- **Problemas resueltos:**
  1. **Ruta de referencias con menos vínculos** entre dos archivos (BFS con registro del anterior).
  2. **Detección de referencias circulares** (DFS con estados blanco, gris y negro), por ejemplo `Menu.java → Gestor.java → Menu.java`.
  3. **Análisis de impacto:** qué archivos dependen directa o indirectamente de uno (BFS sobre las aristas invertidas).

## Requisitos y ejecución

Java **17 o superior** (probado con Microsoft OpenJDK 21).

```bat
ejecutar.bat            :: compila y abre el menú
ejecutar.bat pruebas    :: compila y ejecuta las 72 pruebas
```

```bash
# Linux/macOS:  find src -name "*.java" > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
java -cp out Main
java -cp out PruebasDirectorios
```

En **VS Code**, abre **esta carpeta** (la que contiene `src` y `.vscode`), ve a *Ejecutar y depurar* (Ctrl+Shift+D), elige **Sistema de directorios (menú)** y pulsa **F5**.

## Datos de prueba

```
Este equipo
├── C: (Disco local)
│   ├── Proyectos/
│   │   ├── app-java/      Gestor.java, Main.java, Menu.java, Modelo.java
│   │   └── sitio-web/     css/estilos.css, img/logo.png, js/app.js, js/utilidades.js, contacto.html, index.html
│   └── Usuarios/Domingo/  Documentos/ (datos.xlsx, informe.docx, presentacion.pptx), Escritorio/ (Acceso a informe.lnk)
└── D: (Datos)             Compartido/ (manual.pdf, plantilla.docx), Respaldos/ (respaldo_datos.xlsx)
```

Vínculos (20): `index.html` → estilos.css, app.js, logo.png y contacto.html · `contacto.html` → estilos.css y app.js · `app.js` → utilidades.js · `estilos.css` → logo.png · `Main.java` → Menu.java y Gestor.java · `Menu.java` → Gestor.java · `Gestor.java` → Modelo.java y **Menu.java (ciclo)** · `informe.docx` → datos.xlsx y logo.png · `presentacion.pptx` → datos.xlsx e informe.docx · `Acceso a informe.lnk` → informe.docx · `manual.pdf` → index.html · `plantilla.docx` → logo.png · `respaldo_datos.xlsx` está **aislado**.
