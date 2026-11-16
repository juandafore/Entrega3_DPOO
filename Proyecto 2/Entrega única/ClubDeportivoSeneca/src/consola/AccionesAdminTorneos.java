package consola;

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
import modelo.torneos.EncuentroTorneo;
import modelo.torneos.GestorTorneos;
import modelo.torneos.Torneo;
import modelo.usuarios.Administrador;

public class AccionesAdminTorneos {
    private final Club club;
    private final Entrada in;
    private final AccionesTorneos consulta;

    public AccionesAdminTorneos(Club club, Entrada in) {
        this.club = club;
        this.in = in;
        this.consulta = new AccionesTorneos(club, in);
    }

    public void crearTorneo(Administrador a) {
        GestorTorneos g = a.torneos();
        String nombre = in.texto("Nombre del torneo: ");
        in.out().println("1. Deporte de conjunto (se inscriben equipos)  2. Disciplina individual");
        boolean conjunto = in.entero("Tipo: ", 1, 2) == 1;
        FormatoTorneo formato = in.enumeracion("Formato", FormatoTorneo.class);
        int cupos = in.entero("Número de cupos (2-64): ", 2, 64);
        LocalDate fecha;
        do {
            fecha = in.fecha("Fecha del torneo");
            if (fecha.isBefore(LocalDate.now())) {
                in.out().println("  La fecha no puede ser pasada.");
            }
        } while (fecha.isBefore(LocalDate.now()));
        boolean competitivo = in.siNo("¿Es competitivo? (n = recreativo)");
        double tarifa = 0;
        int porcentaje;
        if (competitivo) {
            tarifa = in.decimal("Tarifa de inscripción por jugador/equipo: $", 1, 100_000_000);
            porcentaje = in.entero("Porcentaje de lo recaudado que se entrega como premio (1-100): ", 1, 100);
        } else {
            porcentaje = in.entero("Porcentaje del bono de descuento para los ganadores (1-100): ", 1, 100);
        }
        Torneo t;
        if (conjunto) {
            Deporte d = in.enumeracion("Deporte", Deporte.class);
            Categoria c = in.enumeracion("Categoría", Categoria.class);
            t = g.crearTorneoConjunto(nombre, d, c, formato, cupos, fecha, competitivo, tarifa, porcentaje);
        } else {
            Disciplina d = in.enumeracion("Disciplina", Disciplina.class);
            Nivel n = in.enumeracion("Nivel", Nivel.class);
            boolean dobles = d == Disciplina.PADEL || in.siNo("¿Es un torneo de dobles (parejas)?");
            t = g.crearTorneoIndividual(nombre, d, n, dobles, formato, cupos, fecha, competitivo, tarifa, porcentaje);
        }
        in.out().println("Torneo creado: " + t);
        in.out().println("Cupos preferenciales reservados (20% redondeado hacia arriba): " + t.cuposPreferenciales());
    }

    private Torneo seleccionar(EstadoTorneo estado) {
        return in.seleccionar("Torneos (" + estado + ")", club.getGestorTorneos().porEstado(estado), consulta::resumen);
    }

    public void liberarPreferenciales() {
        Torneo t = seleccionar(EstadoTorneo.INSCRIPCIONES_ABIERTAS);
        if (t != null) {
            t.liberarCuposPreferenciales();
            in.out().println("Cupos preferenciales liberados. Regulares disponibles: " + t.cuposRegularesDisponibles());
        }
    }

    public void iniciarTorneo() {
        Torneo t = seleccionar(EstadoTorneo.INSCRIPCIONES_ABIERTAS);
        if (t != null && in.siNo("Al iniciar se cierran las inscripciones y retiros. ¿Continuar?")) {
            t.iniciar();
            in.out().println("Torneo en curso. Encuentros generados:");
            for (EncuentroTorneo e : t.getEncuentros()) {
                in.out().println("   " + e);
            }
        }
    }

    public void registrarResultado() {
        Torneo t = seleccionar(EstadoTorneo.EN_CURSO);
        if (t == null) {
            return;
        }
        EncuentroTorneo e = in.seleccionar("Encuentros pendientes", t.encuentrosPendientes());
        if (e == null) {
            return;
        }
        while (true) {
            int a = in.entero("Marcador de " + e.getParticipanteA().getNombre() + ": ", 0, 300);
            int b = in.entero("Marcador de " + e.getParticipanteB().getNombre() + ": ", 0, 300);
            if (a == b && !t.permiteEmpate()) {
                in.out().println("  Este encuentro no admite empates.");
                continue;
            }
            t.registrarResultado(e, a, b);
            break;
        }
        in.out().println("Resultado registrado: " + e);
        List<EncuentroTorneo> nuevos = new ArrayList<>(t.encuentrosPendientes());
        if (t.getEstado() == EstadoTorneo.FINALIZADO) {
            in.out().println("¡Torneo finalizado! Campeón: " + t.getCampeon().getNombre());
            in.out().println("Premio: " + t.descripcionPremio());
        } else {
            in.out().println("Encuentros pendientes: " + nuevos.size());
        }
    }

    public void verInscritosYRanking() {
        Torneo t = in.seleccionar("Torneos", club.getGestorTorneos().getTorneos(), consulta::resumen);
        if (t == null) {
            return;
        }
        in.out().println(t.historialDetallado());
        GestorTorneos g = club.getGestorTorneos();
        in.out().println("Ranking usado para cupos preferenciales (top " + t.cuposPreferenciales() + "):");
        if (t.getDisciplina() != null) {
            g.rankingIndividual(t.getDisciplina(), t.getNivel()).forEach(
                    i -> in.out().println("   " + i.getSocio().getNombre() + " - ELO " + i.getPuntajeELO()));
        } else {
            g.rankingEquipos(t.getDeporte(), t.getCategoria()).forEach(
                    e -> in.out().println("   " + e.getNombre() + " - desempeño " + g.desempeno(e) + " pts"));
        }
    }
}
