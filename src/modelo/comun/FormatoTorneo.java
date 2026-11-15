package modelo.comun;

public enum FormatoTorneo {
    LIGA("Liga (todos contra todos)"),
    ELIMINACION_DIRECTA("Cuadro de eliminación directa");

    private final String texto;

    FormatoTorneo(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
