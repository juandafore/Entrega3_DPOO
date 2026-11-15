package modelo.comun;

public enum EstadoTorneo {
    INSCRIPCIONES_ABIERTAS("Inscripciones abiertas"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado");

    private final String texto;

    EstadoTorneo(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
