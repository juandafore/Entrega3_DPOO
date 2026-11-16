package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.comun.DiaSemana;
import modelo.comun.TipoSugerencia;
import modelo.usuarios.Empleado;
import modelo.usuarios.Socio;

public class AccionesEmpleado {
    private final Club club;
    private final Entrada in;

    public AccionesEmpleado(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    public void sugerir(Empleado e) {
        String nombre = in.texto("Nombre del artículo o snack: ");
        TipoSugerencia tipo = in.enumeracion("Tipo", TipoSugerencia.class);
        SugerenciaArticulo s = new SugerenciaArticulo(e, nombre, tipo);
        e.sugerirArticulo(s);
        club.registrarSugerencia(s);
        in.out().println("Sugerencia enviada: " + s);
    }

    public void verTurnos(Empleado e) {
        in.out().println("Mis turnos:");
        if (e.getTurnos().isEmpty()) {
            in.out().println("   (sin turnos asignados)");
        }
        for (TurnoEmpleado t : e.getTurnos()) {
            in.out().println("   " + t);
        }
        in.out().println("Mis solicitudes de cambio:");
        for (SolicitudCambioTurno s : e.getSolicitudes()) {
            in.out().println("   " + s);
        }
    }

    public void solicitarCambioTurno(Empleado e) {
        TurnoEmpleado t = in.seleccionar("Turno a cambiar", new ArrayList<>(e.getTurnos()));
        if (t == null) {
            return;
        }
        DiaSemana dia = in.enumeracion("Nuevo día", DiaSemana.class);
        java.time.LocalTime[] r = in.rangoHorario("Nueva hora de inicio", "Nueva hora de fin");
        SolicitudCambioTurno s = new SolicitudCambioTurno(e, t, dia, r[0], r[1]);
        e.solicitarTurno(s);
        club.registrarSolicitud(s);
        in.out().println("Solicitud enviada (pendiente de aprobación): " + s);
    }

    public void compartirCodigo(Empleado e) {
        if (e.getCodigo() == null) {
            in.out().println("No tiene código de descuento.");
            return;
        }
        List<Socio> socios = new ArrayList<>();
        for (Socio s : club.getSocios()) {
            if (s.isActivo() && !e.getCodigo().getCompartidoCon().contains(s)) {
                socios.add(s);
            }
        }
        Socio s = in.seleccionar("Socios", socios, Socio::getNombre);
        if (s != null) {
            e.compartirCodigo(s);
            in.out().println("Código " + e.getCodigo().getCodigo() + " compartido con " + s.getNombre()
                    + " (8% en sus compras de tienda).");
        }
    }
}
