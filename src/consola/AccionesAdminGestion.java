package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.DiaSemana;
import modelo.comun.EstadoSolicitud;
import modelo.comun.EstadoSugerencia;
import modelo.comun.TipoSugerencia;
import modelo.competencia.Competencia;
import modelo.deportesconjunto.Equipo;
import modelo.tienda.Articulo;
import modelo.tienda.CodigoDescuento;
import modelo.tienda.UbicacionInventario;
import modelo.usuarios.Administrador;
import modelo.usuarios.Empleado;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Fisioterapeuta;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;

public class AccionesAdminGestion {
    private final Club club;
    private final Entrada in;

    public AccionesAdminGestion(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    public void crearEquipo(Administrador a) {
        String nombre = in.texto("Nombre del equipo: ");
        Deporte d = in.enumeracion("Deporte", Deporte.class);
        Categoria c = in.enumeracion("Categoría", Categoria.class);
        int cupo = in.entero("Cupo máximo (" + d.getMinJugadores() + "-40): ", d.getMinJugadores(), 40);
        Equipo e = new Equipo(nombre, d, c, cupo);
        a.crearEquipo(e);
        in.out().println("Equipo creado: " + e);
    }

    public void darDeBajaEquipo(Administrador a) {
        Equipo e = in.seleccionar("Equipos", new ArrayList<>(club.getEquipos()));
        if (e != null && in.siNo("¿Dar de baja a " + e.getNombre() + "?")) {
            a.darDeBajaEquipo(e);
            in.out().println("Equipo dado de baja.");
        }
    }

    private String loginNuevo() {
        while (true) {
            String login = in.texto("Login: ");
            if (login.contains(" ")) {
                in.out().println("  El login no puede tener espacios.");
            } else if (club.existeLogin(login)) {
                in.out().println("  Ya existe un usuario con ese login.");
            } else {
                return login;
            }
        }
    }

    public void registrarSocio(Administrador a) {
        String login = loginNuevo();
        String pass = in.texto("Password: ");
        String nombre = in.texto("Nombre completo: ");
        int edad = in.entero("Edad (5-100): ", 5, 100);
        Socio s = new Socio(login, pass, nombre, edad);
        a.registrarSocio(s);
        in.out().println("Socio registrado: " + s + " - categoría " + s.categoriaPorEdad());
    }

    public void darDeBajaSocio(Administrador a) {
        Socio s = in.seleccionar("Socios", new ArrayList<>(club.getSocios()), x -> x.getNombre() + " (" + x.getLogin() + ")");
        if (s != null && in.siNo("¿Dar de baja a " + s.getNombre() + "?")) {
            a.darDeBajaSocio(s);
            in.out().println("Socio dado de baja (ya no puede iniciar sesión y salió de sus equipos).");
        }
    }

    public void registrarInvitado(Administrador a) {
        String login = loginNuevo();
        String pass = in.texto("Password: ");
        String nombre = in.texto("Nombre completo: ");
        String doc = in.texto("Documento de identidad: ");
        boolean est = in.siNo("¿Es estudiante matriculado en la universidad?");
        Invitado i = new Invitado(login, pass, nombre, doc, est);
        a.registrarInvitado(i);
        in.out().println("Participante registrado: " + i);
    }

    public void registrarEmpleado(Administrador a) {
        in.out().println("1. Entrenador  2. Fisioterapeuta");
        int tipo = in.entero("Tipo: ", 1, 2);
        String login = loginNuevo();
        String pass = in.texto("Password: ");
        String nombre = in.texto("Nombre completo: ");
        Empleado e = tipo == 1 ? new Entrenador(login, pass, nombre) : new Fisioterapeuta(login, pass, nombre);
        e.setCodigo(new CodigoDescuento(login.toUpperCase() + "15", e));
        a.registrarEmpleado(e);
        in.out().println("Empleado registrado: " + e + " con código " + e.getCodigo().getCodigo());
    }

    public void consultarInventario(Administrador a) {
        in.out().println(a.reportes().estadoInventario());
    }

    public void moverArticulo(Administrador a) {
        Articulo art = in.seleccionar("Artículos", club.getArticulos(),
                x -> x + " @ " + (x.getUbicacion() != null ? x.getUbicacion() : "sin ubicación"));
        if (art == null) {
            return;
        }
        List<UbicacionInventario> ubis = new ArrayList<>(club.getUbicaciones());
        in.out().println("(Puede crear una nueva ubicación escogiendo 0 y respondiendo 's')");
        UbicacionInventario u = in.seleccionar("Ubicación destino", ubis);
        if (u == null) {
            if (!in.siNo("¿Crear una nueva ubicación?")) {
                return;
            }
            u = new UbicacionInventario(in.texto("Nombre de la ubicación: "));
            club.agregarUbicacion(u);
        }
        a.trasladar(art, u);
        in.out().println(art.getNombre() + " ahora está en " + u.getNombre());
    }

    public void reabastecer(Administrador a) {
        Articulo art = in.seleccionar("Artículos", club.getArticulos());
        if (art == null) {
            return;
        }
        a.reabastecer(art, in.entero("Cantidad a agregar (1-10000): ", 1, 10000));
        in.out().println("Nuevo stock: " + art);
    }

    public void resolverSugerencias(Administrador a) {
        List<SugerenciaArticulo> pend = new ArrayList<>();
        for (SugerenciaArticulo s : club.getSugerencias()) {
            if (s.getEstado() == EstadoSugerencia.PENDIENTE) {
                pend.add(s);
            }
        }
        SugerenciaArticulo s = in.seleccionar("Sugerencias pendientes", pend);
        if (s == null) {
            return;
        }
        if (!in.siNo("¿Aprobar la sugerencia?")) {
            a.resolverSugerencia(s, false);
            in.out().println("Sugerencia rechazada.");
            return;
        }
        double precio = in.decimal("Precio de venta: $", 1, 100_000_000);
        if (s.getTipo() == TipoSugerencia.ARTICULO) {
            String cat = in.texto("Categoría del artículo: ");
            a.resolverSugerencia(s, precio, cat, new String[0]);
            in.out().println("Artículo incorporado al catálogo (stock 0: use 'Reabastecer').");
        } else {
            String al = in.textoOpcional("Alérgenos separados por coma (vacío si no tiene): ");
            String[] alergenos = al.isEmpty() ? new String[0] : al.split("\\s*,\\s*");
            a.resolverSugerencia(s, precio, "cafeteria", alergenos);
            in.out().println("Snack incorporado a la cafetería.");
        }
    }

    public void definirTurno(Administrador a) {
        Empleado e = in.seleccionar("Empleados", club.getEmpleados());
        if (e == null) {
            return;
        }
        DiaSemana d = in.enumeracion("Día", DiaSemana.class);
        java.time.LocalTime[] r = in.rangoHorario("Hora de inicio", "Hora de fin");
        a.definirTurno(e, new TurnoEmpleado(e, d, r[0], r[1]));
        in.out().println("Turno asignado.");
    }

    public void eliminarTurno(Administrador a) {
        TurnoEmpleado t = in.seleccionar("Turnos", new ArrayList<>(club.getTurnos()));
        if (t == null) {
            return;
        }
        in.out().println(a.eliminarTurno(t) ? "Turno eliminado."
                : "No se puede eliminar: se incumpliría el mínimo de 2 entrenadores y 1 fisioterapeuta.");
    }

    public void verTurnos() {
        in.out().println("Horarios de atención y turnos:");
        for (modelo.admin.HorarioAtencion h : club.getHorarios()) {
            in.out().println("  Horario " + h.getDia() + " " + h.getApertura() + "-" + h.getCierre()
                    + (h.cubreMinimos(club.getTurnos()) ? " (cubre mínimos)" : " (NO cubre mínimos)"));
        }
        for (TurnoEmpleado t : club.getTurnos()) {
            in.out().println("  " + t);
        }
    }

    public void resolverSolicitudes(Administrador a) {
        List<SolicitudCambioTurno> pend = new ArrayList<>();
        for (SolicitudCambioTurno s : club.getSolicitudes()) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                pend.add(s);
            }
        }
        SolicitudCambioTurno s = in.seleccionar("Solicitudes pendientes", pend);
        if (s == null) {
            return;
        }
        boolean aprobar = in.siNo("¿Aprobar la solicitud?");
        boolean ok = a.resolverTurno(s, aprobar);
        in.out().println(ok ? "Solicitud APROBADA."
                : aprobar ? "Solicitud RECHAZADA: el cambio incumple el mínimo de 2 entrenadores y 1 fisioterapeuta."
                        : "Solicitud RECHAZADA.");
    }

    public void inscribirEnCompetencia(Administrador a) {
        Competencia c = in.seleccionar("Competencias", club.getCompetencias());
        if (c == null) {
            return;
        }
        List<Equipo> candidatos = new ArrayList<>();
        for (Equipo e : club.getEquipos()) {
            if (e.isActivo() && c.inscripcionDe(e) == null) {
                candidatos.add(e);
            }
        }
        Equipo e = in.seleccionar("Equipos", candidatos);
        if (e != null) {
            a.inscribirEquipo(e, c);
            in.out().println(e.getNombre() + " inscrito en " + c.getNombre()
                    + ". Recuerde que un entrenador debe registrar la nómina.");
        }
    }

    public void registrarMensualidad(Administrador a) {
        Socio s = in.seleccionar("Socios", club.getSocios(), Socio::getNombre);
        if (s == null) {
            return;
        }
        String periodo = in.texto("Periodo (p.ej. 2026-11): ");
        double valor = in.decimal("Valor: $", 1, 100_000_000);
        a.registrarMensualidad(s, periodo, valor);
        in.out().println("Mensualidad registrada.");
    }
}
