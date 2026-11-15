package modelo;

/**
 * Significado de una arista del grafo: el archivo ORIGEN usa o apunta al DESTINO.
 */
public enum TipoVinculo {
    HOJA_DE_ESTILO("aplica la hoja de estilos"),
    SCRIPT("incluye el script"),
    IMAGEN("muestra la imagen"),
    HIPERVINCULO("tiene un hipervínculo a"),
    IMPORTA("importa la clase"),
    DATOS_VINCULADOS("toma datos vinculados de"),
    ACCESO_DIRECTO("es un acceso directo a");

    private final String descripcion;

    TipoVinculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoVinculo desdeTexto(String texto) {
        if (texto != null) {
            String t = texto.trim().toUpperCase().replace(' ', '_');
            for (TipoVinculo tipo : values()) {
                if (tipo.name().equals(t) || String.valueOf(tipo.ordinal() + 1).equals(t)) {
                    return tipo;
                }
            }
        }
        throw new IllegalArgumentException("Tipo de vínculo inválido: " + texto);
    }
}
