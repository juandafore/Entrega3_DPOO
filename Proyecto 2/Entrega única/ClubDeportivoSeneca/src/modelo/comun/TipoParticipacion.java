package modelo.comun;

public enum TipoParticipacion {
    EQUIPO,
    INDIVIDUAL,
    DOBLES;

    public boolean esIndividual() {
        return this != EQUIPO;
    }
}
