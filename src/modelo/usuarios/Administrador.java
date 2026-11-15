package modelo.usuarios;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.admin.ServicioReportes;
import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.competencia.Competencia;
import modelo.competencia.InscripcionCompetencia;
import modelo.deportesconjunto.Equipo;
import modelo.tienda.Articulo;
import modelo.tienda.UbicacionInventario;

public class Administrador extends Usuario {
    private static final long serialVersionUID = 1L;

    private Club club;

    public Administrador(String login, String password, String nombre) {
        super(login, password, nombre);
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public void crearEquipo(Equipo e) {
        club.agregarEquipo(e);
    }

    public void darDeBajaEquipo(Equipo e) {
        club.eliminarEquipo(e);
    }

    public void registrarSocio(Socio s) {
        club.registrarSocio(s);
    }

    public void darDeBajaSocio(Socio s) {
        club.eliminarSocio(s);
    }

    public void reabastecer(Articulo a, int n) {
        a.reabastecer(n);
    }

    public void trasladar(Articulo a, UbicacionInventario u) {
        u.trasladar(a);
    }

    public void resolverSugerencia(SugerenciaArticulo s, double precio, String categoria, String[] alergenos) {
        if (s.getEstado() != modelo.comun.EstadoSugerencia.PENDIENTE) {
            throw new IllegalStateException("La sugerencia ya fue resuelta");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser positivo");
        }
        s.aprobar();
        club.incorporarSugerencia(s, precio, categoria, alergenos);
    }

    public void registrarInvitado(Invitado i) {
        club.registrarInvitado(i);
    }

    public void registrarEmpleado(Empleado e) {
        club.agregarEmpleado(e);
    }

    public modelo.tienda.Mensualidad registrarMensualidad(Socio s, String periodo, double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor de la mensualidad debe ser positivo");
        }
        modelo.tienda.Mensualidad m = new modelo.tienda.Mensualidad(s, periodo, valor);
        m.confirmar();
        s.comprar(m);
        return m;
    }

    public modelo.torneos.GestorTorneos torneos() {
        return club.getGestorTorneos();
    }

    public void resolverSugerencia(SugerenciaArticulo s, boolean aprobar) {
        if (aprobar) {
            s.aprobar();
            club.incorporarSugerencia(s);
        } else {
            s.rechazar();
        }
    }

    public void definirTurno(Empleado e, TurnoEmpleado t) {
        if (t.getEmpleado() != e) {
            throw new IllegalArgumentException("El turno no corresponde al empleado");
        }
        for (TurnoEmpleado otro : e.getTurnos()) {
            if (otro.solapaCon(t)) {
                throw new IllegalStateException("El turno se cruza con otro turno de " + e.getNombre());
            }
        }
        club.registrarTurno(t);
    }

    public boolean eliminarTurno(TurnoEmpleado t) {
        List<TurnoEmpleado> sin = new ArrayList<>(club.getTurnos());
        sin.remove(t);
        for (modelo.admin.HorarioAtencion h : club.getHorarios()) {
            if (!h.cubreMinimos(sin)) {
                return false;
            }
        }
        club.getTurnos().remove(t);
        t.getEmpleado().getTurnos().remove(t);
        return true;
    }

    public boolean resolverTurno(SolicitudCambioTurno s, boolean aprobar) {
        return club.resolverCambioTurno(s, aprobar);
    }

    public InscripcionCompetencia inscribirEquipo(Equipo e, Competencia c) {
        return club.inscribirEnCompetencia(e, c);
    }

    public ServicioReportes reportes() {
        return club.getServicioReportes();
    }

    @Override
    public String tipo() {
        return "Administrador";
    }
}
