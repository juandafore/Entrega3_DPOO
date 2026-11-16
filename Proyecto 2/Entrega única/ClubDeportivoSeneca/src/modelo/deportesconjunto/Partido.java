package modelo.deportesconjunto;

import modelo.competencia.Competencia;
import modelo.usuarios.Socio;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Disputable;
import modelo.comun.EstadoPartido;

public abstract class Partido implements Disputable, Serializable {
    protected LocalDate fecha;
    protected LocalTime hora;
    protected EstadoPartido estado = EstadoPartido.PROGRAMADO;
    protected int marcadorLocal;
    protected int marcadorVisitante;
    protected Equipo local;
    protected Equipo visitante;
    protected final List<SetPartido> sets = new ArrayList<>();
    protected final List<EstadisticaJugador> estadisticas = new ArrayList<>();

    protected Partido(Equipo local, Equipo visitante, LocalDate fecha, LocalTime hora) {
        this.local = local;
        this.visitante = visitante;
        this.fecha = fecha;
        this.hora = hora;
    }

    public abstract Competencia ambito();

    @Override
    public boolean puedeJugarse() {
        int min = local.getDeporte().getMinJugadores();
        return local.cumpleMinimo(min, ambito()) && visitante.cumpleMinimo(min, ambito());
    }

    public boolean esElegible(Socio s) {
        boolean deEquipo = local.tieneJugador(s) || (visitante != null && visitante.tieneJugador(s));
        return deEquipo && !s.estaSuspendidoEn(ambito());
    }

    public Suspension registrarEstadisticaJugador(EstadisticaJugador e) {
        Socio s = e.getSocio();
        if (estado != EstadoPartido.JUGADO) {
            throw new IllegalStateException("Primero debe registrarse el marcador del partido");
        }
        if (!esElegible(s)) {
            throw new IllegalStateException("No se puede alinear a " + s.getNombre()
                    + ": está suspendido o no pertenece a los equipos del partido");
        }
        for (EstadisticaJugador ya : estadisticas) {
            if (ya.getSocio() == s) {
                throw new IllegalStateException("Ya se registró la estadística de " + s.getNombre());
            }
        }
        agregarEstadistica(e);
        s.agregarEstadistica(e);
        if (e.generaSuspension()) {
            Suspension susp = new Suspension(s, "Acumulación de tarjetas", this, ambito());
            s.agregarSuspension(susp);
            return susp;
        }
        return null;
    }

    public void finalizar() {
        cumplirSuspensiones(local);
        if (visitante != null) {
            cumplirSuspensiones(visitante);
        }
    }

    private void cumplirSuspensiones(Equipo e) {
        for (InscripcionEquipo i : e.getInscripciones()) {
            for (Suspension s : i.getSocio().getSuspensiones()) {
                if (s.estaVigente() && s.getCompetencia() == ambito() && s.getPartidoOrigen() != this) {
                    s.cumplir();
                }
            }
        }
    }

    @Override
    public void registrarMarcador(int local, int visitante) {
        if (local < 0 || visitante < 0) {
            throw new IllegalArgumentException("El marcador no puede ser negativo");
        }
        if (estado == EstadoPartido.JUGADO) {
            throw new IllegalStateException("El partido ya tiene marcador registrado");
        }
        if (!puedeJugarse()) {
            this.estado = EstadoPartido.NO_DISPUTABLE;
            throw new IllegalStateException("El partido no puede jugarse: un equipo no alcanza el mínimo");
        }
        this.marcadorLocal = local;
        this.marcadorVisitante = visitante;
        this.estado = EstadoPartido.JUGADO;
    }

    public SetPartido registrarSet(int puntosLocal, int puntosVisitante) {
        SetPartido s = new SetPartido(sets.size() + 1, puntosLocal, puntosVisitante);
        sets.add(s);
        if (s.ganadorSet() == 1) {
            marcadorLocal++;
        } else {
            marcadorVisitante++;
        }
        this.estado = EstadoPartido.JUGADO;
        return s;
    }

    public void agregarEstadistica(EstadisticaJugador e) {
        e.registrar(this);
        estadisticas.add(e);
    }

    @Override
    public String toString() {
        String rival = visitante != null ? visitante.getNombre() : "?";
        return fecha + " " + local.getNombre() + " " + marcadorLocal + "-" + marcadorVisitante + " " + rival
                + " (" + estado + ")";
    }

    public String resultado() {
        if (marcadorLocal > marcadorVisitante) {
            return "VICTORIA_LOCAL";
        }
        if (marcadorVisitante > marcadorLocal) {
            return "VICTORIA_VISITANTE";
        }
        return "EMPATE";
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public int getMarcadorLocal() {
        return marcadorLocal;
    }

    public int getMarcadorVisitante() {
        return marcadorVisitante;
    }

    public Equipo getLocal() {
        return local;
    }

    public Equipo getVisitante() {
        return visitante;
    }

    public List<SetPartido> getSets() {
        return sets;
    }

    public List<EstadisticaJugador> getEstadisticas() {
        return estadisticas;
    }
}
