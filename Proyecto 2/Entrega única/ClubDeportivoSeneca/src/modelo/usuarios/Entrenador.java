package modelo.usuarios;

import modelo.comun.EstadoAsistencia;
import modelo.competencia.InscripcionCompetencia;
import modelo.deportesconjunto.EstadisticaJugador;
import modelo.deportesconjunto.Partido;
import modelo.deportesconjunto.Suspension;
import modelo.entrenamiento.AsistenciaEntrenamiento;
import modelo.entrenamiento.SesionEntrenamiento;

public class Entrenador extends Empleado {
    private static final long serialVersionUID = 1L;

    public Entrenador(String login, String password, String nombre) {
        super(login, password, nombre);
    }

    public void planearSesion(SesionEntrenamiento s) {
        if (s.getResponsable() != this) {
            throw new IllegalArgumentException("Solo puede planear sesiones de las que es responsable");
        }
        if (s.getConvocados().isEmpty()) {
            throw new IllegalStateException("La sesión debe tener al menos un jugador convocado");
        }
    }

    public AsistenciaEntrenamiento registrarAsistencia(SesionEntrenamiento s, Socio socio, EstadoAsistencia estado) {
        if (s.getResponsable() != this) {
            throw new IllegalArgumentException("Solo el entrenador responsable registra la asistencia");
        }
        return s.registrarAsistencia(socio, estado);
    }

    public void evaluar(AsistenciaEntrenamiento a, int nota, String comentario) {
        a.evaluar(nota, comentario);
    }

    public void registrarResultado(Partido p, int local, int visitante) {
        p.registrarMarcador(local, visitante);
    }

    public void registrarEstadistica(Partido p, EstadisticaJugador e) {
        p.agregarEstadistica(e);
    }

    public Suspension registrarEstadisticaValidada(Partido p, EstadisticaJugador e) {
        return p.registrarEstadisticaJugador(e);
    }

    public void gestionarNomina(InscripcionCompetencia i, Socio s) {
        if (!i.getEquipo().tieneJugador(s)) {
            throw new IllegalArgumentException(s.getNombre() + " no pertenece al equipo " + i.getEquipo().getNombre());
        }
        i.habilitar(s);
    }

    @Override
    public String tipo() {
        return "Entrenador";
    }
}
