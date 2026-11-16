package consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.Modalidad;
import modelo.comun.Nivel;
import modelo.competencia.Competencia;
import modelo.competencia.FilaPosicion;
import modelo.competencia.PartidoCompetencia;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.EstadisticaBaloncesto;
import modelo.deportesconjunto.EstadisticaFutbol;
import modelo.deportesconjunto.EstadisticaJugador;
import modelo.deportesconjunto.EstadisticaVoleibol;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesconjunto.Suspension;
import modelo.deportesindividuales.InscripcionDisciplina;
import modelo.deportesindividuales.Instalacion;
import modelo.deportesindividuales.ParticipantePractica;
import modelo.deportesindividuales.PartidoPractica;
import modelo.deportesindividuales.RankingELO;
import modelo.deportesindividuales.Reserva;
import modelo.usuarios.Socio;

public class AccionesSocio {
    private final Club club;
    private final Entrada in;

    public AccionesSocio(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    public void inscribirseEquipo(Socio s) {
        List<Equipo> equipos = new ArrayList<>();
        for (Equipo e : club.getEquipos()) {
            if (e.isActivo()) {
                equipos.add(e);
            }
        }
        in.out().println("Su categoría por edad (" + s.getEdad() + " años): " + s.categoriaPorEdad());
        Equipo e = in.seleccionar("Equipos", equipos);
        if (e == null) {
            return;
        }
        String pos = in.texto("Posición (p.ej. portero, defensa, base, armador): ");
        int num = in.entero("Número de camiseta (1-99): ", 1, 99);
        InscripcionEquipo ins = s.inscribirseEquipo(e, pos, num);
        in.out().println("Inscripción exitosa: " + ins + " (" + e + ")");
    }

    public void inscribirseDisciplina(Socio s) {
        Disciplina d = in.enumeracion("Disciplina", Disciplina.class);
        Nivel n = in.enumeracion("Nivel", Nivel.class);
        InscripcionDisciplina ins = s.inscribirseDisciplina(d, n);
        in.out().println("Inscrito: " + ins);
    }

    public void reservar(Socio s) {
        Instalacion inst = in.seleccionar("Instalaciones", club.getInstalaciones());
        if (inst == null) {
            return;
        }
        LocalDate fecha = in.fecha("Fecha de la reserva");
        if (fecha.isBefore(LocalDate.now())) {
            in.out().println("No se puede reservar en una fecha pasada.");
            return;
        }
        java.time.LocalTime[] rango = in.rangoHorario("Hora de inicio", "Hora de fin");
        Modalidad m;
        if (inst.getDisciplina() == Disciplina.PADEL) {
            m = Modalidad.DOBLES;
            in.out().println("El pádel siempre se juega en dobles (4 jugadores).");
        } else {
            m = in.enumeracion("Modalidad", Modalidad.class);
        }
        Reserva r = new Reserva(s, inst, fecha, rango[0], rango[1], m.getNumeroJugadores(), m);
        if (club.reservar(s, inst, r)) {
            in.out().println("Reserva CONFIRMADA: " + r);
        } else {
            in.out().println("Reserva RECHAZADA: " + r.getMotivoRechazo());
        }
    }

    public void programarPractica(Socio s) {
        List<Disciplina> mias = new ArrayList<>();
        for (InscripcionDisciplina i : s.getInscripcionesDisciplina()) {
            mias.add(i.getDisciplina());
        }
        Disciplina d = in.seleccionar("Sus disciplinas", mias);
        if (d == null) {
            in.out().println("Debe inscribirse primero en una disciplina.");
            return;
        }
        List<Socio> rivales = new ArrayList<>();
        for (Socio otro : club.getSocios()) {
            if (otro != s && otro.isActivo() && otro.inscripcionEn(d) != null) {
                rivales.add(otro);
            }
        }
        Socio rival = in.seleccionar("Rivales inscritos en " + d, rivales,
                o -> o.getNombre() + " (" + o.inscripcionEn(d).getNivel() + (o.esEntrenador() ? ", entrenador" : "")
                        + ")");
        if (rival == null) {
            return;
        }
        LocalDate fecha = in.fecha("Fecha del partido");
        PartidoPractica p = s.programarPractica(rival, d, fecha, null);
        in.out().println("Partido programado: " + p);
        if (in.siNo("¿Registrar ya el resultado?")) {
            registrarResultadoPractica(p);
        }
    }

    public void registrarPracticaPendiente(Socio s) {
        List<PartidoPractica> pendientes = new ArrayList<>();
        for (PartidoPractica p : s.getPartidosPractica()) {
            if (!p.isJugado()) {
                pendientes.add(p);
            }
        }
        PartidoPractica p = in.seleccionar("Partidos de práctica pendientes", pendientes);
        if (p != null) {
            registrarResultadoPractica(p);
        }
    }

    private void registrarResultadoPractica(PartidoPractica p) {
        List<ParticipantePractica> ps = p.getParticipantes();
        String a = ps.get(0).getInscripcion().getSocio().getNombre();
        String b = ps.get(1).getInscripcion().getSocio().getNombre();
        int sa = in.entero("Sets ganados por " + a + ": ", 0, 7);
        int sb = in.entero("Sets ganados por " + b + ": ", 0, 7);
        p.registrarMarcador(sa, sb);
        for (ParticipantePractica pp : ps) {
            in.out().println("Estadísticas de " + pp.getInscripcion().getSocio().getNombre() + ":");
            if (p.getDisciplina() == Disciplina.TENIS_MESA) {
                int puntos = in.entero("  Puntos totales: ", 0, 500);
                pp.registrarEstadisticas(0, 0, 0, puntos);
            } else {
                int games = in.entero("  Games ganados: ", 0, 100);
                int aces = in.entero("  Aces: ", 0, 100);
                int errores = in.entero("  Errores no forzados: ", 0, 200);
                pp.registrarEstadisticas(aces, games, errores, 0);
            }
        }
        p.actualizarELO();
        in.out().println("Resultado registrado. ELO actualizado:");
        for (ParticipantePractica pp : ps) {
            in.out().println("   " + pp.getInscripcion().getSocio().getNombre() + ": "
                    + pp.getInscripcion().getPuntajeELO());
        }
    }

    public void consultarEstadisticas(Socio s) {
        in.out().println("Ranking ELO por disciplina:");
        if (s.getInscripcionesDisciplina().isEmpty()) {
            in.out().println("   (sin disciplinas)");
        }
        for (InscripcionDisciplina i : s.getInscripcionesDisciplina()) {
            StringBuilder hist = new StringBuilder();
            for (RankingELO r : i.getHistorial()) {
                hist.append(" ").append(r.getPuntajeAntes()).append("->").append(r.getPuntajeDespues());
            }
            in.out().println("   " + i.getDisciplina() + ": nivel " + i.getNivel() + ", ELO " + i.getPuntajeELO()
                    + (hist.length() > 0 ? " | evolución:" + hist : ""));
        }
        int goles = 0, asistF = 0, amarillas = 0, rojas = 0, puntosB = 0, rebotes = 0, asistB = 0, aces = 0,
                bloqueos = 0, puntosV = 0, partidos = 0;
        for (EstadisticaJugador e : s.consultarEstadisticas()) {
            partidos++;
            if (e instanceof EstadisticaFutbol) {
                EstadisticaFutbol f = (EstadisticaFutbol) e;
                goles += f.getGoles();
                asistF += f.getAsistencias();
                amarillas += f.getTarjetasAmarillas();
                rojas += f.getTarjetasRojas();
            } else if (e instanceof EstadisticaBaloncesto) {
                EstadisticaBaloncesto b = (EstadisticaBaloncesto) e;
                puntosB += b.getPuntos();
                rebotes += b.getRebotes();
                asistB += b.getAsistencias();
            } else if (e instanceof EstadisticaVoleibol) {
                EstadisticaVoleibol v = (EstadisticaVoleibol) e;
                aces += v.getAces();
                bloqueos += v.getBloqueos();
                puntosV += v.getPuntos();
            }
        }
        in.out().println("Partidos de conjunto con estadísticas: " + partidos);
        in.out().println("   Fútbol: goles " + goles + ", asistencias " + asistF + ", amarillas " + amarillas
                + ", rojas " + rojas);
        in.out().println("   Baloncesto: puntos " + puntosB + ", rebotes " + rebotes + ", asistencias " + asistB);
        in.out().println("   Voleibol: aces " + aces + ", bloqueos " + bloqueos + ", puntos " + puntosV);
        in.out().println("Partidos de práctica:");
        for (PartidoPractica p : s.getPartidosPractica()) {
            in.out().println("   " + p);
        }
        for (Suspension susp : s.getSuspensiones()) {
            if (susp.estaVigente()) {
                in.out().println("SUSPENSIÓN VIGENTE: " + susp.getMotivo()
                        + (susp.getCompetencia() != null ? " en " + susp.getCompetencia().getNombre() : " (partidos internos)"));
            }
        }
    }

    public void consultarResultados(Socio s) {
        in.out().println("Partidos internos:");
        if (club.getPartidosInternos().isEmpty()) {
            in.out().println("   (sin partidos)");
        }
        for (PartidoInterno p : club.getPartidosInternos()) {
            in.out().println("   " + p);
        }
        boolean alguna = false;
        for (Competencia c : club.getCompetencias()) {
            boolean participa = false;
            for (Deporte d : Deporte.values()) {
                Equipo e = s.equipoDe(d);
                if (e != null && c.inscripcionDe(e) != null) {
                    participa = true;
                }
            }
            if (!participa) {
                continue;
            }
            alguna = true;
            in.out().println("Competencia " + c + ":");
            for (PartidoCompetencia p : c.getPartidos()) {
                in.out().println("   " + p);
            }
            in.out().println("   Tabla de posiciones:");
            int pos = 1;
            for (FilaPosicion f : c.getTabla().ordenar()) {
                in.out().println(String.format("     %d. %s  Pts %d  (G %d E %d P %d)", pos++,
                        f.getInscripcion().getEquipo().getNombre(), f.getPuntos(), f.getVictorias(),
                        f.getEmpates(), f.getDerrotas()));
            }
            if (c.maximoGoleador() != null) {
                in.out().println("   Goleador: " + c.maximoGoleador().getNombre());
            }
        }
        if (!alguna) {
            in.out().println("(Sus equipos no participan en competencias externas)");
        }
    }

    public void historial(Socio s) {
        List<Object> h = s.consultarHistorial();
        in.out().println("Mi historial (" + h.size() + " elementos):");
        for (Object o : h) {
            in.out().println("   - " + o);
        }
        in.out().println("Saldo de puntos de fidelidad: " + s.getPuntosFidelidad());
    }

    public void terminarBebida(Socio s) {
        if (!s.tieneBebidaCaliente()) {
            in.out().println("No tiene bebidas calientes registradas.");
            return;
        }
        s.terminarConsumo();
        in.out().println("Listo: ya puede ingresar a cualquier instalación.");
    }
}
