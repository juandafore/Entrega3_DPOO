package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.EstadoTorneo;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.comun.TipoParticipacion;
import modelo.competencia.Competencia;
import modelo.competencia.FilaPosicion;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesindividuales.InscripcionDisciplina;
import modelo.usuarios.Administrador;
import modelo.usuarios.Empleado;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;
import modelo.usuarios.Usuario;

public class GestorTorneos implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Club club;
    private final List<Torneo> torneos = new ArrayList<>();
    private int siguienteId = 1;

    public GestorTorneos(Club club) {
        this.club = club;
    }

    public Torneo crearTorneoConjunto(String nombre, Deporte deporte, Categoria categoria, FormatoTorneo formato,
            int cupos, LocalDate fecha, boolean competitivo, double tarifa, int porcentaje) {
        Torneo t = competitivo
                ? new TorneoCompetitivo(siguienteId, nombre, deporte, categoria, formato, cupos, fecha, tarifa,
                        porcentaje)
                : new TorneoRecreativo(siguienteId, nombre, deporte, categoria, formato, cupos, fecha, porcentaje);
        return registrar(t);
    }

    public Torneo crearTorneoIndividual(String nombre, Disciplina disciplina, Nivel nivel, boolean dobles,
            FormatoTorneo formato, int cupos, LocalDate fecha, boolean competitivo, double tarifa, int porcentaje) {
        Torneo t = competitivo
                ? new TorneoCompetitivo(siguienteId, nombre, disciplina, nivel, dobles, formato, cupos, fecha, tarifa,
                        porcentaje)
                : new TorneoRecreativo(siguienteId, nombre, disciplina, nivel, dobles, formato, cupos, fecha,
                        porcentaje);
        return registrar(t);
    }

    private Torneo registrar(Torneo t) {
        torneos.add(t);
        siguienteId++;
        return t;
    }

    public InscripcionTorneo inscribirIndividual(Usuario u, Torneo t) {
        if (t.getTipoParticipacion() != TipoParticipacion.INDIVIDUAL) {
            throw new IllegalArgumentException("El torneo " + t.getNombre() + " es de participación "
                    + t.getTipoParticipacion());
        }
        validarJugadorIndividual(u, t);
        return t.inscribir(new ParticipanteIndividual(u), esPreferencial(u, t));
    }

    public InscripcionTorneo inscribirPareja(Usuario u1, Usuario u2, Torneo t) {
        if (t.getTipoParticipacion() != TipoParticipacion.DOBLES) {
            throw new IllegalArgumentException("El torneo " + t.getNombre() + " no es de dobles");
        }
        validarJugadorIndividual(u1, t);
        validarJugadorIndividual(u2, t);
        boolean pref = esPreferencial(u1, t) || esPreferencial(u2, t);
        return t.inscribir(new ParticipantePareja(u1, u2), pref);
    }

    public InscripcionTorneo inscribirEquipo(Usuario solicitante, Equipo e, Torneo t) {
        if (t.getTipoParticipacion() != TipoParticipacion.EQUIPO) {
            throw new IllegalArgumentException("En el torneo " + t.getNombre() + " se inscriben jugadores, no equipos");
        }
        validarResponsableEquipo(solicitante, e);
        if (!e.isActivo()) {
            throw new IllegalStateException("El equipo " + e.getNombre() + " está dado de baja");
        }
        if (e.getDeporte() != t.getDeporte() || e.getCategoria() != t.getCategoria()) {
            throw new IllegalArgumentException("El equipo " + e.getNombre() + " no es de " + t.deporteODisciplina());
        }
        if (!e.estaCompleto()) {
            throw new IllegalStateException("En los torneos de conjunto se inscriben equipos completos: "
                    + e.getNombre() + " tiene " + e.jugadoresHabilitados(null) + " jugadores habilitados y necesita "
                    + e.getDeporte().getMinJugadores());
        }
        return t.inscribir(new ParticipanteEquipo(e), esPreferencial(e, t));
    }

    public void validarJugadorIndividual(Usuario u, Torneo t) {
        if (u == null || !u.isActivo()) {
            throw new IllegalArgumentException("Usuario inválido o inactivo");
        }
        if (u instanceof Administrador) {
            throw new IllegalStateException("El administrador no puede inscribirse en torneos");
        }
        if (u instanceof Empleado) {
            Empleado emp = (Empleado) u;
            if (emp.estaDeTurno(t.getFecha())) {
                throw new IllegalStateException(u.getNombre() + " está de turno el día del torneo ("
                        + modelo.comun.DiaSemana.de(t.getFecha()) + ")");
            }
        }
        if (u instanceof Socio) {
            InscripcionDisciplina ins = ((Socio) u).inscripcionEn(t.getDisciplina());
            if (ins != null && ins.getNivel() != t.getNivel()) {
                throw new IllegalStateException(u.getNombre() + " tiene nivel " + ins.getNivel() + " en "
                        + t.getDisciplina() + " y el torneo es de nivel " + t.getNivel());
            }
        }
    }

    private void validarResponsableEquipo(Usuario solicitante, Equipo e) {
        if (solicitante instanceof Administrador || solicitante instanceof Entrenador) {
            return;
        }
        if (solicitante instanceof Socio && e.tieneJugador((Socio) solicitante)) {
            return;
        }
        throw new IllegalStateException("Solo un jugador del equipo, un entrenador o el administrador puede "
                + "inscribir o retirar al equipo " + e.getNombre());
    }

    public boolean puedeRetirarseLibremente(Usuario u) {
        return u instanceof Socio || (u instanceof Invitado && ((Invitado) u).isEstudiante());
    }

    public void retirarJugador(Usuario u, Torneo t) {
        if (t.getTipoParticipacion() == TipoParticipacion.EQUIPO) {
            throw new IllegalArgumentException("En torneos de conjunto se retira al equipo completo");
        }
        InscripcionTorneo ins = t.inscripcionDe(u);
        if (ins == null) {
            throw new IllegalStateException(u.getNombre() + " no está inscrito en " + t.getNombre());
        }
        if (!puedeRetirarseLibremente(u)) {
            throw new IllegalStateException("Solo los socios del club y los estudiantes de la universidad pueden "
                    + "retirarse libremente de un torneo");
        }
        t.retirar(ins);
    }

    public void retirarEquipo(Usuario solicitante, Equipo e, Torneo t) {
        validarResponsableEquipo(solicitante, e);
        InscripcionTorneo encontrada = null;
        for (InscripcionTorneo i : t.getInscripciones()) {
            if (i.getParticipante() instanceof ParticipanteEquipo
                    && ((ParticipanteEquipo) i.getParticipante()).getEquipo() == e) {
                encontrada = i;
            }
        }
        if (encontrada == null) {
            throw new IllegalStateException("El equipo " + e.getNombre() + " no está inscrito en " + t.getNombre());
        }
        t.retirar(encontrada);
    }

    public List<InscripcionDisciplina> rankingIndividual(Disciplina d, Nivel n) {
        List<InscripcionDisciplina> r = new ArrayList<>();
        for (Socio s : club.getSocios()) {
            InscripcionDisciplina ins = s.inscripcionEn(d);
            if (s.isActivo() && ins != null && ins.getNivel() == n) {
                r.add(ins);
            }
        }
        r.sort((a, b) -> b.getPuntajeELO() - a.getPuntajeELO());
        return r;
    }

    public boolean esPreferencial(Usuario u, Torneo t) {
        if (!(u instanceof Socio) || t.getDisciplina() == null) {
            return false;
        }
        InscripcionDisciplina ins = ((Socio) u).inscripcionEn(t.getDisciplina());
        if (ins == null) {
            return false;
        }
        List<InscripcionDisciplina> ranking = rankingIndividual(t.getDisciplina(), t.getNivel());
        int k = Math.min(t.cuposPreferenciales(), ranking.size());
        return ranking.subList(0, k).contains(ins);
    }

    public int desempeno(Equipo e) {
        int pts = 0;
        for (PartidoInterno p : club.getPartidosInternos()) {
            if (p.getEstado() != modelo.comun.EstadoPartido.JUGADO) {
                continue;
            }
            int favor;
            int contra;
            if (p.getLocal() == e) {
                favor = p.getMarcadorLocal();
                contra = p.getMarcadorVisitante();
            } else if (p.getVisitante() == e) {
                favor = p.getMarcadorVisitante();
                contra = p.getMarcadorLocal();
            } else {
                continue;
            }
            pts += favor > contra ? 3 : (favor == contra ? 1 : 0);
        }
        for (Competencia c : club.getCompetencias()) {
            for (FilaPosicion f : c.getTabla().getFilas()) {
                if (f.getInscripcion().getEquipo() == e) {
                    pts += f.getPuntos();
                }
            }
        }
        return pts;
    }

    public List<Equipo> rankingEquipos(Deporte d, Categoria c) {
        List<Equipo> r = new ArrayList<>();
        for (Equipo e : club.getEquipos()) {
            if (e.isActivo() && e.getDeporte() == d && e.getCategoria() == c) {
                r.add(e);
            }
        }
        r.sort((a, b) -> desempeno(b) - desempeno(a));
        return r;
    }

    public boolean esPreferencial(Equipo e, Torneo t) {
        List<Equipo> ranking = rankingEquipos(t.getDeporte(), t.getCategoria());
        int k = Math.min(t.cuposPreferenciales(), ranking.size());
        return ranking.subList(0, k).contains(e);
    }

    public List<Torneo> porEstado(EstadoTorneo estado) {
        List<Torneo> r = new ArrayList<>();
        for (Torneo t : torneos) {
            if (t.getEstado() == estado) {
                r.add(t);
            }
        }
        return r;
    }

    public List<Torneo> torneosDe(Usuario u) {
        List<Torneo> r = new ArrayList<>();
        for (Torneo t : torneos) {
            if (t.inscripcionDe(u) != null) {
                r.add(t);
            }
        }
        return r;
    }

    public List<Torneo> getTorneos() {
        return torneos;
    }
}
