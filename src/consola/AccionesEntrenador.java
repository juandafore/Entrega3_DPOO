package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.EstadoAsistencia;
import modelo.comun.TipoSesion;
import modelo.competencia.ASCUN;
import modelo.competencia.Competencia;
import modelo.competencia.InscripcionCompetencia;
import modelo.competencia.PartidoCompetencia;
import modelo.deportesconjunto.Alineacion;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.EstadisticaBaloncesto;
import modelo.deportesconjunto.EstadisticaFutbol;
import modelo.deportesconjunto.EstadisticaJugador;
import modelo.deportesconjunto.EstadisticaVoleibol;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.Partido;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesconjunto.Suspension;
import modelo.deportesindividuales.InscripcionDisciplina;
import modelo.deportesindividuales.Instalacion;
import modelo.entrenamiento.AsistenciaEntrenamiento;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Socio;

public class AccionesEntrenador {
    private final Club club;
    private final Entrada in;

    public AccionesEntrenador(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    public void planearSesion(Entrenador e) {
        LocalDate fecha = in.fecha("Fecha de la sesión");
        LocalTime hora = in.hora("Hora de inicio");
        int duracion = in.entero("Duración en minutos (15-300): ", 15, 300);
        TipoSesion tipo = in.enumeracion("Tipo de sesión", TipoSesion.class);
        Instalacion inst = in.seleccionar("Instalación (0 = cancha/espacio de equipo)", club.getInstalaciones());
        SesionEntrenamiento ses = new SesionEntrenamiento(e, fecha, hora, duracion, tipo, inst);
        in.out().println("Convocatoria: 1. Equipo completo  2. Subconjunto de socios de una disciplina");
        if (in.entero("Opción: ", 1, 2) == 1) {
            Equipo eq = in.seleccionar("Equipos", activos());
            if (eq == null) {
                return;
            }
            ses.convocarEquipo(eq);
        } else {
            Disciplina d = in.enumeracion("Disciplina", Disciplina.class);
            List<Socio> inscritos = new ArrayList<>();
            for (Socio s : club.getSocios()) {
                InscripcionDisciplina ins = s.inscripcionEn(d);
                if (s.isActivo() && ins != null) {
                    inscritos.add(s);
                }
            }
            for (Socio s : in.seleccionarVarios("Socios a convocar", inscritos,
                    s -> s.getNombre() + " (" + s.inscripcionEn(d).getNivel() + ")")) {
                ses.convocar(s);
            }
        }
        e.planearSesion(ses);
        club.agregarSesion(ses);
        in.out().println("Sesión creada: " + ses);
    }

    public void registrarAsistencia(Entrenador e) {
        List<SesionEntrenamiento> mias = new ArrayList<>();
        for (SesionEntrenamiento s : club.getSesiones()) {
            if (s.getResponsable() == e && !s.asistenciaCompleta()) {
                mias.add(s);
            }
        }
        SesionEntrenamiento ses = in.seleccionar("Sesiones con asistencia pendiente", mias);
        if (ses == null) {
            return;
        }
        for (Socio s : ses.getConvocados()) {
            boolean registrado = false;
            for (AsistenciaEntrenamiento a : ses.getAsistencias()) {
                registrado |= a.getSocio() == s;
            }
            if (registrado) {
                continue;
            }
            EstadoAsistencia est = in.enumeracion("Asistencia de " + s.getNombre(), EstadoAsistencia.class);
            AsistenciaEntrenamiento a = e.registrarAsistencia(ses, s, est);
            if (est != EstadoAsistencia.FALTO && in.siNo("¿Evaluar el desempeño de " + s.getNombre() + "?")) {
                int nota = in.entero("Nota (1-5): ", 1, 5);
                e.evaluar(a, nota, in.textoOpcional("Comentario: "));
            }
        }
        in.out().println(ses.resumenAsistencia());
    }

    public void registrarPartidoInterno(Entrenador e) {
        Equipo local = in.seleccionar("Equipo local", activos());
        if (local == null) {
            return;
        }
        List<Equipo> rivales = new ArrayList<>();
        for (Equipo eq : activos()) {
            if (eq != local && eq.getDeporte() == local.getDeporte() && eq.getCategoria() == local.getCategoria()) {
                rivales.add(eq);
            }
        }
        Equipo visita = in.seleccionar("Equipo visitante (mismo deporte y categoría)", rivales);
        if (visita == null) {
            return;
        }
        LocalDate fecha = in.fecha("Fecha del partido");
        LocalTime hora = in.hora("Hora del partido");
        PartidoInterno p = new PartidoInterno(local, visita, fecha, hora);
        if (!p.puedeJugarse()) {
            in.out().println("El partido NO puede jugarse: se requieren " + local.getDeporte().getMinJugadores()
                    + " jugadores habilitados (" + local.getNombre() + ": " + local.jugadoresHabilitados(null) + ", "
                    + visita.getNombre() + ": " + visita.jugadoresHabilitados(null) + ").");
            return;
        }
        registrarMarcador(e, p);
        club.agregarPartidoInterno(p);
        registrarEstadisticas(e, p);
        p.finalizar();
        in.out().println("Partido registrado: " + p);
    }

    public void registrarPartidoCompetencia(Entrenador e) {
        Competencia c = in.seleccionar("Competencias", club.getCompetencias());
        if (c == null) {
            return;
        }
        InscripcionCompetencia ins = in.seleccionar("Equipos inscritos", c.getInscripciones(),
                i -> i.getEquipo().getNombre() + " (nómina: " + i.getNomina().size() + ")");
        if (ins == null) {
            return;
        }
        String rival = in.texto("Institución rival: ");
        String escenario = in.texto("Escenario: ");
        LocalDate fecha = in.fecha("Fecha del partido");
        LocalTime hora = in.hora("Hora del partido");
        PartidoCompetencia p = new PartidoCompetencia(ins, rival, escenario, fecha, hora);
        if (c instanceof ASCUN) {
            p.setFase(((ASCUN) c).getFaseActual());
            in.out().println("Fase actual de ASCUN: " + p.getFase());
        }
        List<Socio> elegibles = new ArrayList<>();
        for (Socio s : ins.getNomina()) {
            if (p.esElegible(s)) {
                elegibles.add(s);
            } else {
                in.out().println("  " + s.getNombre() + " está suspendido y no puede ser alineado.");
            }
        }
        for (Socio s : in.seleccionarVarios("Alineación (jugadores de la nómina)", elegibles, Socio::getNombre)) {
            InscripcionEquipo ie = null;
            for (InscripcionEquipo x : ins.getEquipo().getInscripciones()) {
                if (x.getSocio() == s) {
                    ie = x;
                }
            }
            p.alinear(new Alineacion(ie, true, p));
        }
        int min = c.getDeporte().getMinJugadores();
        if (p.getAlineacion().size() < min) {
            in.out().println("El partido no puede jugarse: se requieren al menos " + min + " jugadores alineados.");
            return;
        }
        int[] marcador = leerMarcador(c.getDeporte(), ins.getEquipo().getNombre(), rival);
        c.registrarResultado(p, marcador[0], marcador[1]);
        registrarEstadisticas(e, p);
        p.finalizar();
        in.out().println("Partido registrado: " + p);
        if (c instanceof ASCUN && in.siNo("¿Avanzar ASCUN a la siguiente fase?")) {
            ((ASCUN) c).avanzarFase();
            in.out().println("Nueva fase: " + ((ASCUN) c).getFaseActual());
        }
    }

    private void registrarMarcador(Entrenador e, PartidoInterno p) {
        int[] m = leerMarcador(p.getLocal().getDeporte(), p.getLocal().getNombre(), p.getVisitante().getNombre());
        e.registrarResultado(p, m[0], m[1]);
    }

    private int[] leerMarcador(Deporte d, String local, String visita) {
        String unidad = d == Deporte.VOLEIBOL ? "Sets ganados por " : "Marcador de ";
        int max = d == Deporte.VOLEIBOL ? 3 : 300;
        while (true) {
            int a = in.entero(unidad + local + ": ", 0, max);
            int b = in.entero(unidad + visita + ": ", 0, max);
            if (d.admiteEmpate() || a != b) {
                return new int[] {a, b};
            }
            in.out().println("  En " + d + " no existen los empates.");
        }
    }

    private void registrarEstadisticas(Entrenador e, Partido p) {
        List<Socio> jugadores = new ArrayList<>();
        for (InscripcionEquipo ie : p.getLocal().getInscripciones()) {
            jugadores.add(ie.getSocio());
        }
        if (p.getVisitante() != null) {
            for (InscripcionEquipo ie : p.getVisitante().getInscripciones()) {
                jugadores.add(ie.getSocio());
            }
        }
        List<Socio> elegibles = new ArrayList<>();
        for (Socio s : jugadores) {
            if (p.esElegible(s)) {
                elegibles.add(s);
            }
        }
        Deporte d = p.getLocal().getDeporte();
        for (Socio s : in.seleccionarVarios("Jugadores que participaron (registrar estadísticas)", elegibles,
                Socio::getNombre)) {
            in.out().println("Estadísticas de " + s.getNombre() + ":");
            Suspension susp = e.registrarEstadisticaValidada(p, crearEstadistica(d, s));
            if (susp != null) {
                in.out().println("  >> " + s.getNombre() + " queda SUSPENDIDO para el siguiente partido.");
            }
        }
    }

    private EstadisticaJugador crearEstadistica(Deporte d, Socio s) {
        if (d == Deporte.FUTBOL) {
            return new EstadisticaFutbol(s, in.entero("  Goles: ", 0, 30), in.entero("  Asistencias: ", 0, 30),
                    in.entero("  Tarjetas amarillas (0-2): ", 0, 2), in.entero("  Tarjetas rojas (0-1): ", 0, 1));
        }
        if (d == Deporte.BALONCESTO) {
            return new EstadisticaBaloncesto(s, in.entero("  Puntos: ", 0, 200), in.entero("  Rebotes: ", 0, 100),
                    in.entero("  Asistencias: ", 0, 100));
        }
        return new EstadisticaVoleibol(s, in.entero("  Aces: ", 0, 100), in.entero("  Bloqueos: ", 0, 100),
                in.entero("  Puntos: ", 0, 200));
    }

    public void gestionarNomina(Entrenador e) {
        Competencia c = in.seleccionar("Competencias", club.getCompetencias());
        if (c == null) {
            return;
        }
        InscripcionCompetencia ins = in.seleccionar("Equipos inscritos", c.getInscripciones(),
                i -> i.getEquipo().getNombre());
        if (ins == null) {
            return;
        }
        List<Socio> fuera = new ArrayList<>();
        for (InscripcionEquipo ie : ins.getEquipo().getInscripciones()) {
            if (!ins.enNomina(ie.getSocio())) {
                fuera.add(ie.getSocio());
            }
        }
        for (Socio s : in.seleccionarVarios("Jugadores a habilitar en la nómina", fuera, Socio::getNombre)) {
            e.gestionarNomina(ins, s);
        }
        in.out().println("Nómina de " + ins.getEquipo().getNombre() + " en " + c.getNombre() + ":");
        for (Socio s : ins.getNomina()) {
            in.out().println("   - " + s.getNombre());
        }
        in.out().println("Mínimo requerido: " + c.getNominaMinima() + " | ¿Completa? "
                + (ins.nominaCompleta() ? "sí" : "no"));
    }

    private List<Equipo> activos() {
        List<Equipo> r = new ArrayList<>();
        for (Equipo eq : club.getEquipos()) {
            if (eq.isActivo()) {
                r.add(eq);
            }
        }
        return r;
    }
}
