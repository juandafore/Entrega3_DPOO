package modelo.torneos;

import java.io.Serializable;

public class FilaTorneo implements Serializable {
    private static final long serialVersionUID = 1L;

    private ParticipanteTorneo participante;
    private int jugados;
    private int ganados;
    private int empatados;
    private int perdidos;
    private int aFavor;
    private int enContra;
    private int rondaAlcanzada;

    public FilaTorneo(ParticipanteTorneo participante) {
        this.participante = participante;
    }

    public void registrar(int favor, int contra, int ronda) {
        jugados++;
        aFavor += favor;
        enContra += contra;
        if (favor > contra) {
            ganados++;
        } else if (favor == contra) {
            empatados++;
        } else {
            perdidos++;
        }
        alcanzarRonda(ronda);
    }

    public void alcanzarRonda(int ronda) {
        rondaAlcanzada = Math.max(rondaAlcanzada, ronda);
    }

    public int puntos() {
        return ganados * 3 + empatados;
    }

    public int diferencia() {
        return aFavor - enContra;
    }

    public ParticipanteTorneo getParticipante() {
        return participante;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getAFavor() {
        return aFavor;
    }

    public int getEnContra() {
        return enContra;
    }

    public int getRondaAlcanzada() {
        return rondaAlcanzada;
    }

    @Override
    public String toString() {
        return String.format("%-28s PJ %d  G %d  E %d  P %d  +%d/-%d  Pts %d", participante.getNombre(),
                jugados, ganados, empatados, perdidos, aFavor, enContra, puntos());
    }
}
