package modelo.admin;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Club;
import modelo.comun.EstadoAsistencia;
import modelo.comun.Periodo;
import modelo.competencia.Competencia;
import modelo.competencia.FilaPosicion;
import modelo.competencia.PartidoCompetencia;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesindividuales.InscripcionDisciplina;
import modelo.deportesindividuales.Instalacion;
import modelo.deportesindividuales.PartidoPractica;
import modelo.deportesindividuales.RankingELO;
import modelo.deportesindividuales.Reserva;
import modelo.entrenamiento.AsistenciaEntrenamiento;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.tienda.Articulo;
import modelo.tienda.Compra;
import modelo.tienda.UbicacionInventario;
import modelo.torneos.Torneo;
import modelo.usuarios.Socio;

public class ServicioReportes implements Serializable {
    private static final long serialVersionUID = 1L;

    private Club club;

    public ServicioReportes(Club club) {
        this.club = club;
    }

    private static boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    public Map<String, Double> ventasPorRubro(LocalDate desde, LocalDate hasta) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("TIENDA", 0.0);
        m.put("CAFETERIA", 0.0);
        m.put("MENSUALIDAD", 0.0);
        for (Compra c : club.getTodasLasCompras()) {
            if (enRango(c.getFecha().toLocalDate(), desde, hasta)) {
                m.merge(c.rubro(), c.getTotal(), Double::sum);
            }
        }
        return m;
    }

    public String reporteVentas(Periodo periodo, LocalDate referencia) {
        return reporteVentas(periodo.desde(referencia), periodo.hasta(referencia));
    }

    public String reporteVentas(LocalDate desde, LocalDate hasta) {
        Map<String, Double> m = ventasPorRubro(desde, hasta);
        double total = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("=== Reporte de ventas (").append(desde).append(" a ").append(hasta).append(") ===\n");
        sb.append(String.format("  Tienda:        $%,.2f%n", m.get("TIENDA")));
        sb.append(String.format("  Cafetería:     $%,.2f%n", m.get("CAFETERIA")));
        sb.append(String.format("  Mensualidades: $%,.2f%n", m.get("MENSUALIDAD")));
        for (double v : m.values()) {
            total += v;
        }
        sb.append(String.format("  TOTAL:         $%,.2f", total));
        return sb.toString();
    }

    public String reporteDeportivo(Periodo periodo, LocalDate referencia) {
        return reporteDeportivo(periodo.desde(referencia), periodo.hasta(referencia));
    }

    public String reporteDeportivo(LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Reporte deportivo (").append(desde).append(" a ").append(hasta).append(") ===\n");
        sb.append("- Asistencia a entrenamientos:\n");
        Map<Socio, int[]> asistencia = new LinkedHashMap<>();
        for (SesionEntrenamiento s : club.getSesiones()) {
            if (enRango(s.getFecha(), desde, hasta)) {
                sb.append("    ").append(s).append("\n");
                for (AsistenciaEntrenamiento a : s.getAsistencias()) {
                    int[] c = asistencia.computeIfAbsent(a.getSocio(), k -> new int[3]);
                    c[a.getEstado() == EstadoAsistencia.ASISTIO ? 0 : a.getEstado() == EstadoAsistencia.TARDE ? 1 : 2]++;
                }
            }
        }
        for (Map.Entry<Socio, int[]> e : asistencia.entrySet()) {
            sb.append(String.format("      %s: asistió %d, tarde %d, faltó %d%n", e.getKey().getNombre(),
                    e.getValue()[0], e.getValue()[1], e.getValue()[2]));
        }
        sb.append("- Resultados de partidos internos:\n");
        for (PartidoInterno p : club.getPartidosInternos()) {
            if (enRango(p.getFecha(), desde, hasta)) {
                sb.append("    ").append(p).append("\n");
            }
        }
        sb.append("- Competencias:\n");
        for (Competencia comp : club.getCompetencias()) {
            sb.append("    ").append(comp.getNombre()).append("\n");
            for (PartidoCompetencia p : comp.getPartidos()) {
                if (enRango(p.getFecha(), desde, hasta)) {
                    sb.append("      ").append(p).append("\n");
                }
            }
            for (FilaPosicion f : comp.getTabla().ordenar()) {
                sb.append("      ").append(f.getInscripcion().getEquipo().getNombre()).append(" Pts ")
                        .append(f.getPuntos()).append("\n");
            }
        }
        sb.append("- Evolución de rankings ELO:\n");
        for (Socio s : club.getSocios()) {
            for (InscripcionDisciplina ins : s.getInscripcionesDisciplina()) {
                StringBuilder ev = new StringBuilder();
                for (RankingELO r : ins.getHistorial()) {
                    if (enRango(r.getFecha().toLocalDate(), desde, hasta)) {
                        ev.append(" ").append(r.getPuntajeAntes()).append("->").append(r.getPuntajeDespues());
                    }
                }
                sb.append(String.format("    %s %s: ELO actual %d%s%n", s.getNombre(), ins.getDisciplina(),
                        ins.getPuntajeELO(), ev.length() > 0 ? " (" + ev.toString().trim() + ")" : ""));
            }
        }
        sb.append("- Torneos internos:\n");
        for (Torneo t : club.getGestorTorneos().getTorneos()) {
            sb.append("    ").append(t)
                    .append(t.getCampeon() != null ? " - campeón: " + t.getCampeon().getNombre() : "").append("\n");
        }
        return sb.toString();
    }

    public Object[] historialCompleto() {
        List<Object> historial = new ArrayList<>();
        historial.addAll(club.getTodasLasReservas());
        historial.addAll(club.getPartidosInternos());
        for (Competencia c : club.getCompetencias()) {
            historial.addAll(c.getPartidos());
        }
        historial.addAll(club.getPartidosPractica());
        historial.addAll(club.getSesiones());
        historial.addAll(club.getTodasLasCompras());
        historial.addAll(club.getGestorTorneos().getTorneos());
        return historial.toArray();
    }

    public String historialDetallado() {
        StringBuilder sb = new StringBuilder("=== Historial completo ===\n");
        sb.append("Reservas:\n");
        lista(sb, club.getTodasLasReservas());
        sb.append("Partidos internos:\n");
        lista(sb, club.getPartidosInternos());
        sb.append("Partidos de competencia:\n");
        List<PartidoCompetencia> pc = new ArrayList<>();
        for (Competencia c : club.getCompetencias()) {
            pc.addAll(c.getPartidos());
        }
        lista(sb, pc);
        sb.append("Partidos de práctica:\n");
        lista(sb, club.getPartidosPractica());
        sb.append("Sesiones de entrenamiento:\n");
        lista(sb, club.getSesiones());
        sb.append("Torneos internos:\n");
        lista(sb, club.getGestorTorneos().getTorneos());
        return sb.toString();
    }

    private static void lista(StringBuilder sb, List<?> elementos) {
        if (elementos.isEmpty()) {
            sb.append("   (ninguno)\n");
        }
        for (Object o : elementos) {
            sb.append("   - ").append(o).append("\n");
        }
    }

    public String estadoInventario() {
        StringBuilder sb = new StringBuilder("=== Estado del inventario ===\n");
        sb.append("Artículos:\n");
        for (Articulo a : club.getArticulos()) {
            sb.append("  ").append(a).append(" @ ").append(a.getUbicacion() != null ? a.getUbicacion() : "sin ubicación")
                    .append("\n");
        }
        sb.append("Ubicaciones:\n");
        for (UbicacionInventario u : club.getUbicaciones()) {
            sb.append("  ").append(u.getNombre()).append(": ").append(u.getArticulos().size()).append(" artículos\n");
        }
        sb.append("Instalaciones:\n");
        for (Instalacion i : club.getInstalaciones()) {
            int confirmadas = 0;
            for (Reserva r : i.getReservas()) {
                if (r.getEstado() == modelo.comun.EstadoReserva.CONFIRMADA) {
                    confirmadas++;
                }
            }
            sb.append("  ").append(i).append(i.isActiva() ? " activa" : " inactiva").append(", reservas confirmadas: ")
                    .append(confirmadas).append("\n");
        }
        return sb.toString();
    }

    public List<PartidoPractica> practicasEn(LocalDate desde, LocalDate hasta) {
        List<PartidoPractica> r = new ArrayList<>();
        for (PartidoPractica p : club.getPartidosPractica()) {
            if (enRango(p.getFecha(), desde, hasta)) {
                r.add(p);
            }
        }
        return r;
    }
}
