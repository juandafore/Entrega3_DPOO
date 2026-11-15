package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDate;

public class InscripcionTorneo implements Serializable {
    private static final long serialVersionUID = 1L;

    private ParticipanteTorneo participante;
    private boolean preferencial;
    private double tarifaPagada;
    private LocalDate fecha;

    public InscripcionTorneo(ParticipanteTorneo participante, boolean preferencial, double tarifaPagada) {
        this.participante = participante;
        this.preferencial = preferencial;
        this.tarifaPagada = tarifaPagada;
        this.fecha = LocalDate.now();
    }

    public ParticipanteTorneo getParticipante() {
        return participante;
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public double getTarifaPagada() {
        return tarifaPagada;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return participante.getNombre() + (preferencial ? " [cupo preferencial]" : " [cupo regular]")
                + (tarifaPagada > 0 ? String.format(" tarifa $%,.0f", tarifaPagada) : "");
    }
}
