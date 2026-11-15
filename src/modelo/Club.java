package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.admin.HorarioAtencion;
import modelo.admin.ServicioReportes;
import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.cafeteria.ProductoCafeteria;
import modelo.cafeteria.Snack;
import modelo.comun.DiaSemana;
import modelo.comun.Disciplina;
import modelo.comun.TipoSugerencia;
import modelo.competencia.Competencia;
import modelo.competencia.InscripcionCompetencia;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.deportesindividuales.ConfiguracionDisciplina;
import modelo.deportesindividuales.Instalacion;
import modelo.deportesindividuales.PartidoPractica;
import modelo.deportesindividuales.Reserva;
import modelo.tienda.Articulo;
import modelo.tienda.Compra;
import modelo.tienda.UbicacionInventario;
import modelo.torneos.GestorTorneos;
import modelo.usuarios.Administrador;
import modelo.usuarios.Empleado;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;
import modelo.usuarios.Usuario;

public class Club implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre = "Club Deportivo Séneca";

    private final List<Socio> socios = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Invitado> invitados = new ArrayList<>();
    private Administrador administrador;

    private final List<Equipo> equipos = new ArrayList<>();
    private final List<Competencia> competencias = new ArrayList<>();
    private final List<PartidoInterno> partidosInternos = new ArrayList<>();

    private final List<Instalacion> instalaciones = new ArrayList<>();
    private final List<ConfiguracionDisciplina> configuraciones = new ArrayList<>();

    private final List<Articulo> articulos = new ArrayList<>();
    private final List<UbicacionInventario> ubicaciones = new ArrayList<>();
    private final List<ProductoCafeteria> productosCafeteria = new ArrayList<>();

    private final List<HorarioAtencion> horarios = new ArrayList<>();
    private final List<TurnoEmpleado> turnos = new ArrayList<>();
    private final List<SugerenciaArticulo> sugerencias = new ArrayList<>();
    private final List<SolicitudCambioTurno> solicitudes = new ArrayList<>();
    private final List<SesionEntrenamiento> sesiones = new ArrayList<>();

    private ServicioReportes servicioReportes;
    private GestorTorneos gestorTorneos;

    public GestorTorneos getGestorTorneos() {
        if (gestorTorneos == null) {
            gestorTorneos = new GestorTorneos(this);
        }
        return gestorTorneos;
    }

    public boolean existeLogin(String login) {
        for (Usuario u : todosLosUsuarios()) {
            if (u.getLogin().equalsIgnoreCase(login)) {
                return true;
            }
        }
        return false;
    }

    private void validarLoginNuevo(Usuario u) {
        if (existeLogin(u.getLogin())) {
            throw new IllegalArgumentException("Ya existe un usuario con el login " + u.getLogin());
        }
    }

    public void registrarInvitado(Invitado i) {
        validarLoginNuevo(i);
        invitados.add(i);
    }

    public List<Invitado> getInvitados() {
        return invitados;
    }

    public Usuario buscarUsuario(String login) {
        for (Usuario u : todosLosUsuarios()) {
            if (u.getLogin().equalsIgnoreCase(login)) {
                return u;
            }
        }
        return null;
    }

    public List<PartidoPractica> getPartidosPractica() {
        List<PartidoPractica> r = new ArrayList<>();
        for (Socio s : socios) {
            for (PartidoPractica p : s.getPartidosPractica()) {
                if (!r.contains(p)) {
                    r.add(p);
                }
            }
        }
        return r;
    }

    public List<Reserva> getTodasLasReservas() {
        List<Reserva> r = new ArrayList<>();
        for (Instalacion i : instalaciones) {
            r.addAll(i.getReservas());
        }
        return r;
    }

    public Usuario autenticar(String login, String password) {
        for (Usuario u : todosLosUsuarios()) {
            if (u.autenticar(login, password)) {
                return u;
            }
        }
        return null;
    }

    public List<Usuario> todosLosUsuarios() {
        List<Usuario> todos = new ArrayList<>();
        todos.addAll(socios);
        todos.addAll(empleados);
        todos.addAll(invitados);
        if (administrador != null) {
            todos.add(administrador);
        }
        return todos;
    }

    public void agregarEquipo(Equipo e) {
        for (Equipo otro : equipos) {
            if (otro.getNombre().equalsIgnoreCase(e.getNombre())) {
                throw new IllegalArgumentException("Ya existe un equipo llamado " + e.getNombre());
            }
        }
        equipos.add(e);
    }

    public void eliminarEquipo(Equipo e) {
        e.setActivo(false);
        equipos.remove(e);
    }

    public void registrarSocio(Socio s) {
        validarLoginNuevo(s);
        socios.add(s);
    }

    public void eliminarSocio(Socio s) {
        s.setActivo(false);
        for (Equipo e : new ArrayList<>(equipos)) {
            if (e.tieneJugador(s)) {
                e.retirar(s);
            }
        }
        socios.remove(s);
    }

    public void registrarSugerencia(SugerenciaArticulo s) {
        if (!sugerencias.contains(s)) {
            sugerencias.add(s);
        }
    }

    public void incorporarSugerencia(SugerenciaArticulo s) {
        incorporarSugerencia(s, 0.0, "nuevo", new String[0]);
    }

    public void incorporarSugerencia(SugerenciaArticulo s, double precio, String categoria, String[] alergenos) {
        if (s.getTipo() == TipoSugerencia.ARTICULO) {
            Articulo a = new Articulo(s.getNombre(), categoria, precio, 0);
            articulos.add(a);
            if (!ubicaciones.isEmpty()) {
                ubicaciones.get(0).trasladar(a);
            }
        } else if (s.getTipo() == TipoSugerencia.SNACK) {
            productosCafeteria.add(new Snack(s.getNombre(), precio, alergenos));
        }
        if (!sugerencias.contains(s)) {
            sugerencias.add(s);
        }
    }

    public void registrarTurno(TurnoEmpleado t) {
        if (!turnos.contains(t)) {
            turnos.add(t);
        }
        t.getEmpleado().agregarTurno(t);
    }

    public void registrarSolicitud(SolicitudCambioTurno s) {
        if (!solicitudes.contains(s)) {
            solicitudes.add(s);
        }
    }

    public boolean resolverCambioTurno(SolicitudCambioTurno s, boolean aprobar) {
        if (s.getEstado() != modelo.comun.EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue resuelta");
        }
        if (!solicitudes.contains(s)) {
            solicitudes.add(s);
        }
        if (!aprobar) {
            s.rechazar();
            return false;
        }
        TurnoEmpleado t = s.getTurnoOriginal();
        DiaSemana oldDia = t.getDia();
        LocalTime oldInicio = t.getHoraInicio();
        LocalTime oldFin = t.getHoraFin();
        s.aprobar();
        boolean ok = true;
        for (HorarioAtencion h : horarios) {
            if (!h.cubreMinimos(turnos)) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            t.setDia(oldDia);
            t.setHoraInicio(oldInicio);
            t.setHoraFin(oldFin);
            s.rechazar();
        }
        return ok;
    }

    public InscripcionCompetencia inscribirEnCompetencia(Equipo e, Competencia c) {
        return c.inscribir(e);
    }

    public ServicioReportes getServicioReportes() {
        if (servicioReportes == null) {
            servicioReportes = new ServicioReportes(this);
        }
        return servicioReportes;
    }

    private ConfiguracionDisciplina configDe(Disciplina d) {
        for (ConfiguracionDisciplina c : configuraciones) {
            if (c.getDisciplina() == d) {
                return c;
            }
        }
        return null;
    }

    public int contarInstalacionesEnUso(Disciplina d, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        int enUso = 0;
        for (Instalacion i : instalaciones) {
            if (i.getDisciplina() == d && !i.estaDisponible(fecha, inicio, fin) && i.isActiva()) {
                enUso++;
            }
        }
        return enUso;
    }

    public boolean reservar(Socio socio, Instalacion instalacion, Reserva reserva) {
        String motivo = null;
        if (socio == null || !socio.isActivo()) {
            motivo = "Solo los socios activos pueden reservar";
        } else if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            motivo = "La hora de inicio debe ser anterior a la hora de fin";
        } else if (!instalacion.validarJugadores(reserva.getNumeroJugadores())) {
            motivo = "La instalación no admite " + reserva.getNumeroJugadores() + " jugadores";
        } else if (!reserva.modalidadCoherente()) {
            motivo = "La modalidad " + reserva.getModalidad() + " no corresponde a "
                    + reserva.getNumeroJugadores() + " jugadores";
        } else if (!socio.puedeIngresarA(instalacion)) {
            motivo = "No puede usar " + instalacion.getNombre() + " con una bebida caliente";
        } else if (!instalacion.estaDisponible(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin())) {
            motivo = "La instalación ya está reservada en ese bloque";
        } else {
            ConfiguracionDisciplina cfg = configDe(instalacion.getDisciplina());
            int enUso = contarInstalacionesEnUso(instalacion.getDisciplina(), reserva.getFecha(),
                    reserva.getHoraInicio(), reserva.getHoraFin());
            if (cfg != null && !cfg.hayCupo(enUso)) {
                motivo = "Se alcanzó el máximo de " + cfg.getMaxInstalacionesSimultaneas()
                        + " instalaciones simultáneas de " + instalacion.getDisciplina();
            }
        }
        if (motivo != null) {
            reserva.rechazar(motivo);
            return false;
        }
        reserva.confirmar();
        socio.reservar(instalacion, reserva);
        return true;
    }

    public List<Compra> getTodasLasCompras() {
        List<Compra> todas = new ArrayList<>();
        for (Socio s : socios) {
            todas.addAll(s.getCompras());
        }
        for (Empleado e : empleados) {
            todas.addAll(e.getCompras());
        }
        if (administrador != null) {
            todas.addAll(administrador.getCompras());
        }
        return todas;
    }

    public void agregarEmpleado(Empleado e) {
        validarLoginNuevo(e);
        empleados.add(e);
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
        administrador.setClub(this);
    }

    public void agregarCompetencia(Competencia c) {
        competencias.add(c);
    }

    public void agregarPartidoInterno(PartidoInterno p) {
        partidosInternos.add(p);
    }

    public void agregarInstalacion(Instalacion i) {
        instalaciones.add(i);
    }

    public void agregarConfiguracion(ConfiguracionDisciplina c) {
        configuraciones.add(c);
    }

    public void agregarArticulo(Articulo a) {
        articulos.add(a);
    }

    public void agregarUbicacion(UbicacionInventario u) {
        ubicaciones.add(u);
    }

    public void agregarProductoCafeteria(ProductoCafeteria p) {
        productosCafeteria.add(p);
    }

    public void agregarHorario(HorarioAtencion h) {
        horarios.add(h);
    }

    public void agregarSesion(SesionEntrenamiento s) {
        sesiones.add(s);
    }

    public String getNombre() {
        return nombre;
    }

    public List<Socio> getSocios() {
        return socios;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public List<Competencia> getCompetencias() {
        return competencias;
    }

    public List<PartidoInterno> getPartidosInternos() {
        return partidosInternos;
    }

    public List<Instalacion> getInstalaciones() {
        return instalaciones;
    }

    public List<ConfiguracionDisciplina> getConfiguraciones() {
        return configuraciones;
    }

    public List<Articulo> getArticulos() {
        return articulos;
    }

    public List<UbicacionInventario> getUbicaciones() {
        return ubicaciones;
    }

    public List<ProductoCafeteria> getProductosCafeteria() {
        return productosCafeteria;
    }

    public List<HorarioAtencion> getHorarios() {
        return horarios;
    }

    public List<TurnoEmpleado> getTurnos() {
        return turnos;
    }

    public List<SugerenciaArticulo> getSugerencias() {
        return sugerencias;
    }

    public List<SolicitudCambioTurno> getSolicitudes() {
        return solicitudes;
    }

    public List<SesionEntrenamiento> getSesiones() {
        return sesiones;
    }
}
