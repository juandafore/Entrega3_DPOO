package modelo.deportesindividuales;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Disciplina;
import modelo.comun.Disputable;

public class PartidoPractica implements Disputable, Serializable {
    public static final int K_ELO = 24;

    private LocalDate fecha;
    private Disciplina disciplina;
    private int sets;
    private Reserva reserva;
    private final List<ParticipantePractica> participantes = new ArrayList<>();
    private boolean jugado;

    public PartidoPractica(Disciplina disciplina, LocalDate fecha, Reserva reserva) {
        this.disciplina = disciplina;
        this.fecha = fecha;
        this.reserva = reserva;
    }

    public ParticipantePractica agregarParticipante(InscripcionDisciplina inscripcion) {
        ParticipantePractica p = new ParticipantePractica(inscripcion);
        participantes.add(p);
        return p;
    }

    @Override
    public boolean puedeJugarse() {
        if (participantes.size() < 2 || participantes.size() > 4) {
            return false;
        }

        for (int i = 0; i < participantes.size(); i++) {
            for (int j = i + 1; j < participantes.size(); j++) {
                InscripcionDisciplina a = participantes.get(i).getInscripcion();
                InscripcionDisciplina b = participantes.get(j).getInscripcion();
                if (!a.puedeJugarCon(b)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void registrarMarcador(int local, int visitante) {
        if (local < 0 || visitante < 0) {
            throw new IllegalArgumentException("El marcador no puede ser negativo");
        }
        if (local == visitante) {
            throw new IllegalArgumentException("Un partido de práctica debe tener un ganador");
        }
        if (jugado) {
            throw new IllegalStateException("El partido ya fue registrado");
        }
        this.sets = local + visitante;
        if (participantes.size() >= 2) {
            participantes.get(0).setGanador(local >= visitante);
            participantes.get(1).setGanador(visitante > local);
        }
        this.jugado = true;
    }

    private boolean eloActualizado;

    public void actualizarELO() {
        if (!jugado) {
            throw new IllegalStateException("El partido aún no se ha jugado");
        }
        if (eloActualizado) {
            throw new IllegalStateException("El ELO de este partido ya fue actualizado");
        }
        eloActualizado = true;
        for (ParticipantePractica p : participantes) {
            int delta = p.isGanador() ? K_ELO : -K_ELO;
            p.getInscripcion().actualizarELO(delta, this);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Práctica " + disciplina + " " + fecha + ": ");
        for (int i = 0; i < participantes.size(); i++) {
            ParticipantePractica p = participantes.get(i);
            sb.append(i > 0 ? " vs " : "").append(p.getInscripcion().getSocio().getNombre());
            if (jugado && p.isGanador()) {
                sb.append(" (G)");
            }
        }
        return sb.append(jugado ? " - sets: " + sets : " - pendiente").toString();
    }

    public List<ParticipantePractica> getParticipantes() {
        return participantes;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public int getSets() {
        return sets;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public boolean isJugado() {
        return jugado;
    }
}
