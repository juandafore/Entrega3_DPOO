package consola;

import java.time.LocalDate;

import modelo.comun.Periodo;
import modelo.usuarios.Administrador;
import modelo.usuarios.Usuario;

public class ConsolaAdministrador extends ConsolaBase {
    public static void main(String[] args) {
        new ConsolaAdministrador().ejecutar();
    }

    @Override
    protected String titulo() {
        return "Consola del administrador";
    }

    @Override
    protected boolean acepta(Usuario u) {
        return u instanceof Administrador;
    }

    @Override
    protected Menu menuPara(Usuario u) {
        Administrador a = (Administrador) u;
        AccionesAdminGestion g = new AccionesAdminGestion(club, in);
        AccionesAdminTorneos t = new AccionesAdminTorneos(club, in);
        AccionesTorneos consulta = new AccionesTorneos(club, in);

        Menu equiposUsuarios = new Menu("EQUIPOS Y USUARIOS")
                .opcion("Crear equipo", () -> g.crearEquipo(a))
                .opcion("Dar de baja equipo", () -> g.darDeBajaEquipo(a))
                .opcion("Registrar socio (jugador)", () -> g.registrarSocio(a))
                .opcion("Dar de baja socio (jugador)", () -> g.darDeBajaSocio(a))
                .opcion("Registrar estudiante o persona externa", () -> g.registrarInvitado(a))
                .opcion("Registrar empleado", () -> g.registrarEmpleado(a))
                .opcion("Registrar pago de mensualidad", () -> g.registrarMensualidad(a));
        Menu inventario = new Menu("INVENTARIO Y CATÁLOGO")
                .opcion("Consultar inventario de artículos e instalaciones", () -> g.consultarInventario(a))
                .opcion("Mover artículo entre ubicaciones", () -> g.moverArticulo(a))
                .opcion("Reabastecer la tienda", () -> g.reabastecer(a))
                .opcion("Aprobar o rechazar sugerencias de artículos/snacks", () -> g.resolverSugerencias(a));
        Menu turnos = new Menu("TURNOS DE EMPLEADOS")
                .opcion("Ver horarios y turnos", g::verTurnos)
                .opcion("Asignar un turno semanal", () -> g.definirTurno(a))
                .opcion("Eliminar un turno", () -> g.eliminarTurno(a))
                .opcion("Resolver solicitudes de cambio de turno", () -> g.resolverSolicitudes(a));
        Menu torneos = new Menu("TORNEOS INTERNOS")
                .opcion("Crear torneo (recreativo o competitivo)", () -> t.crearTorneo(a))
                .opcion("Consultar torneos por estado e historial", consulta::consultarTorneos)
                .opcion("Ver inscritos y ranking de cupos preferenciales", t::verInscritosYRanking)
                .opcion("Liberar cupos preferenciales no usados", t::liberarPreferenciales)
                .opcion("Inscribir un equipo", () -> consulta.inscribirEquipo(a))
                .opcion("Retirar un equipo", () -> consulta.retirarEquipo(a))
                .opcion("Iniciar torneo (generar encuentros)", t::iniciarTorneo)
                .opcion("Registrar resultado de un encuentro", t::registrarResultado);
        Menu reportes = new Menu("REPORTES E HISTORIAL")
                .opcion("Historial completo (reservas, partidos, sesiones, torneos)",
                        () -> in.out().println(a.reportes().historialDetallado()))
                .opcion("Reporte de ventas por rubro y periodo", () -> {
                    Periodo p = in.enumeracion("Periodo", Periodo.class);
                    LocalDate ref = in.fecha("Fecha de referencia");
                    in.out().println(a.reportes().reporteVentas(p, ref));
                })
                .opcion("Reporte deportivo (asistencia, resultados, rankings)", () -> {
                    Periodo p = in.enumeracion("Periodo", Periodo.class);
                    LocalDate ref = in.fecha("Fecha de referencia");
                    in.out().println(a.reportes().reporteDeportivo(p, ref));
                });

        return new Menu("MENÚ ADMINISTRADOR (" + a.getNombre() + ")")
                .opcion("Equipos y usuarios", () -> equiposUsuarios.ejecutar(in))
                .opcion("Inventario y catálogo", () -> inventario.ejecutar(in))
                .opcion("Turnos de empleados", () -> turnos.ejecutar(in))
                .opcion("Inscribir equipo en competencia (ASCUN / Torneo de los Cerros)",
                        () -> g.inscribirEnCompetencia(a))
                .opcion("Torneos internos", () -> torneos.ejecutar(in))
                .opcion("Reportes e historial", () -> reportes.ejecutar(in));
    }
}
