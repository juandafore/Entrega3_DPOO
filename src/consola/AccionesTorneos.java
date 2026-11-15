package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.comun.EstadoTorneo;
import modelo.comun.TipoParticipacion;
import modelo.deportesconjunto.Equipo;
import modelo.tienda.BonoDescuento;
import modelo.torneos.GestorTorneos;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.ParticipanteEquipo;
import modelo.torneos.PremioEfectivo;
import modelo.torneos.Torneo;
import modelo.torneos.TorneoCompetitivo;
import modelo.usuarios.Socio;
import modelo.usuarios.Usuario;

public class AccionesTorneos {
    private final Club club;
    private final Entrada in;

    public AccionesTorneos(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    private GestorTorneos gestor() {
        return club.getGestorTorneos();
    }

    public void consultarTorneos() {
        in.out().println("Filtrar por estado: 1. Todos  2. Inscripciones abiertas  3. En curso  4. Finalizados");
        int f = in.entero("Opción: ", 1, 4);
        List<Torneo> lista = f == 1 ? gestor().getTorneos()
                : gestor().porEstado(EstadoTorneo.values()[f - 2]);
        Torneo t = in.seleccionar("Torneos (seleccione uno para ver su historial)", lista, this::resumen);
        if (t != null) {
            in.out().println(t.historialDetallado());
        }
    }

    String resumen(Torneo t) {
        return t + String.format(" | fecha %s | cupos %d/%d (pref. libres %d, regulares libres %d)", t.getFecha(),
                t.getInscripciones().size(), t.getCupos(), t.cuposPreferencialesDisponibles(),
                t.cuposRegularesDisponibles());
    }

    private List<Torneo> abiertos(boolean equipos) {
        List<Torneo> r = new ArrayList<>();
        for (Torneo t : gestor().porEstado(EstadoTorneo.INSCRIPCIONES_ABIERTAS)) {
            if ((t.getTipoParticipacion() == TipoParticipacion.EQUIPO) == equipos) {
                r.add(t);
            }
        }
        return r;
    }

    public void inscribirse(Usuario u) {
        Torneo t = in.seleccionar("Torneos individuales con inscripciones abiertas", abiertos(false), this::resumen);
        if (t == null) {
            return;
        }
        in.out().println("Premio: " + t.descripcionPremio());
        Usuario pareja = null;
        if (t.getTipoParticipacion() == TipoParticipacion.DOBLES) {
            String login = in.texto("Login de su pareja de dobles: ");
            pareja = club.buscarUsuario(login);
            if (pareja == null || !pareja.isActivo()) {
                in.out().println("No existe un usuario activo con ese login.");
                return;
            }
        }
        if (!in.siNo("¿Confirmar la inscripción en " + t.getNombre() + "?")) {
            return;
        }
        InscripcionTorneo ins = pareja == null ? gestor().inscribirIndividual(u, t)
                : gestor().inscribirPareja(u, pareja, t);
        informarInscripcion(t, ins);
    }

    public void inscribirEquipo(Usuario solicitante) {
        Torneo t = in.seleccionar("Torneos de conjunto con inscripciones abiertas", abiertos(true), this::resumen);
        if (t == null) {
            return;
        }
        List<Equipo> candidatos = new ArrayList<>();
        for (Equipo e : club.getEquipos()) {
            boolean propio = !(solicitante instanceof Socio) || e.tieneJugador((Socio) solicitante);
            if (propio && e.getDeporte() == t.getDeporte() && e.getCategoria() == t.getCategoria()) {
                candidatos.add(e);
            }
        }
        Equipo e = in.seleccionar("Equipos que puede inscribir", candidatos);
        if (e == null) {
            return;
        }
        in.out().println("Premio: " + t.descripcionPremio());
        if (!in.siNo("¿Confirmar la inscripción de " + e.getNombre() + "?")) {
            return;
        }
        informarInscripcion(t, gestor().inscribirEquipo(solicitante, e, t));
    }

    private void informarInscripcion(Torneo t, InscripcionTorneo ins) {
        in.out().println("Inscripción exitosa: " + ins);
        if (t instanceof TorneoCompetitivo) {
            in.out().println(ins.getTarifaPagada() > 0
                    ? String.format("Valor a pagar: $%,.0f (el pago se gestiona con el proveedor externo).",
                            ins.getTarifaPagada())
                    : "Participación gratuita (empleado del club).");
        }
        in.out().println("Cupos restantes: " + t.cuposDisponibles());
    }

    public void retirarse(Usuario u) {
        List<Torneo> mios = new ArrayList<>();
        for (Torneo t : gestor().torneosDe(u)) {
            if (t.getEstado() == EstadoTorneo.INSCRIPCIONES_ABIERTAS) {
                mios.add(t);
            }
        }
        Torneo t = in.seleccionar("Torneos (aún sin iniciar) en los que está inscrito", mios, this::resumen);
        if (t == null) {
            return;
        }
        if (!in.siNo("¿Confirmar el retiro de " + t.getNombre() + "?")) {
            return;
        }
        InscripcionTorneo ins = t.inscripcionDe(u);
        if (ins.getParticipante() instanceof ParticipanteEquipo) {
            gestor().retirarEquipo(u, ((ParticipanteEquipo) ins.getParticipante()).getEquipo(), t);
        } else {
            gestor().retirarJugador(u, t);
        }
        in.out().println("Retiro registrado. Cupos disponibles ahora: " + t.cuposDisponibles());
    }

    public void retirarEquipo(Usuario solicitante) {
        Torneo t = in.seleccionar("Torneos de conjunto abiertos", abiertos(true), this::resumen);
        if (t == null) {
            return;
        }
        List<Equipo> inscritos = new ArrayList<>();
        for (InscripcionTorneo i : t.getInscripciones()) {
            inscritos.add(((ParticipanteEquipo) i.getParticipante()).getEquipo());
        }
        Equipo e = in.seleccionar("Equipos inscritos", inscritos);
        if (e != null) {
            gestor().retirarEquipo(solicitante, e, t);
            in.out().println("Equipo retirado. Cupos disponibles: " + t.cuposDisponibles());
        }
    }

    public void misTorneos(Usuario u) {
        List<Torneo> mios = gestor().torneosDe(u);
        Torneo t = in.seleccionar("Mis torneos (seleccione para ver el historial)", mios, this::resumen);
        if (t != null) {
            in.out().println(t.historialDetallado());
        }
    }

    public void misPremios(Usuario u) {
        in.out().println("Bonos de descuento:");
        if (u.getBonos().isEmpty()) {
            in.out().println("   (ninguno)");
        }
        for (BonoDescuento b : u.getBonos()) {
            in.out().println("   - " + b);
        }
        in.out().println("Premios en efectivo:");
        boolean alguno = false;
        for (Torneo t : gestor().getTorneos()) {
            if (t instanceof TorneoCompetitivo) {
                for (PremioEfectivo p : ((TorneoCompetitivo) t).getPremios()) {
                    if (p.getBeneficiario() == u) {
                        in.out().println("   - " + t.getNombre() + ": " + p);
                        alguno = true;
                    }
                }
            }
        }
        if (!alguno) {
            in.out().println("   (ninguno)");
        }
    }
}
