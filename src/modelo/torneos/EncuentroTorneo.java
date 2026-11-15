package modelo.torneos;

import java.io.Serializable;

public class EncuentroTorneo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ronda;
    private ParticipanteTorneo participanteA;
    private ParticipanteTorneo participanteB;
    private int marcadorA;
    private int marcadorB;
    private boolean jugado;

    public EncuentroTorneo(int ronda, ParticipanteTorneo a, ParticipanteTorneo b) {
        this.ronda = ronda;
        this.participanteA = a;
        this.participanteB = b;
        if (b == null) {
            jugado = true;
        }
    }

    public boolean esDescanso() {
        return participanteB == null;
    }

    public void registrar(int a, int b, boolean permiteEmpate) {
        if (esDescanso()) {
            throw new IllegalStateException("Un descanso no tiene resultado");
        }
        if (jugado) {
            throw new IllegalStateException("El encuentro ya tiene resultado");
        }
        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("El marcador no puede ser negativo");
        }
        if (!permiteEmpate && a == b) {
            throw new IllegalArgumentException("Este encuentro no admite empates");
        }
        marcadorA = a;
        marcadorB = b;
        jugado = true;
    }

    public ParticipanteTorneo ganador() {
        if (!jugado) {
            return null;
        }
        if (esDescanso()) {
            return participanteA;
        }
        if (marcadorA == marcadorB) {
            return null;
        }
        return marcadorA > marcadorB ? participanteA : participanteB;
    }

    public ParticipanteTorneo perdedor() {
        ParticipanteTorneo g = ganador();
        if (g == null || esDescanso()) {
            return null;
        }
        return g == participanteA ? participanteB : participanteA;
    }

    public boolean involucra(ParticipanteTorneo p) {
        return participanteA == p || participanteB == p;
    }

    public int getRonda() {
        return ronda;
    }

    public ParticipanteTorneo getParticipanteA() {
        return participanteA;
    }

    public ParticipanteTorneo getParticipanteB() {
        return participanteB;
    }

    public int getMarcadorA() {
        return marcadorA;
    }

    public int getMarcadorB() {
        return marcadorB;
    }

    public boolean isJugado() {
        return jugado;
    }

    @Override
    public String toString() {
        if (esDescanso()) {
            return "Ronda " + ronda + ": " + participanteA.getNombre() + " pasa directo (descanso)";
        }
        return "Ronda " + ronda + ": " + participanteA.getNombre() + " "
                + (jugado ? marcadorA + " - " + marcadorB : "vs") + " " + participanteB.getNombre()
                + (jugado ? "" : " (pendiente)");
    }
}
