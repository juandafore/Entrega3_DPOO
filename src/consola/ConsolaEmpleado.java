package consola;

import modelo.usuarios.Empleado;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Usuario;

public class ConsolaEmpleado extends ConsolaBase {
    public static void main(String[] args) {
        new ConsolaEmpleado().ejecutar();
    }

    @Override
    protected String titulo() {
        return "Consola de empleados (entrenadores y fisioterapeutas)";
    }

    @Override
    protected boolean acepta(Usuario u) {
        return u instanceof Empleado;
    }

    @Override
    protected Menu menuPara(Usuario u) {
        Empleado e = (Empleado) u;
        AccionesEmpleado emp = new AccionesEmpleado(club, in);
        AccionesCompras compras = new AccionesCompras(club, in);
        AccionesTorneos torneos = new AccionesTorneos(club, in);
        Menu m = new Menu("MENÚ " + e.tipo().toUpperCase() + " (" + e.getNombre() + ")");
        if (e instanceof Entrenador) {
            Entrenador en = (Entrenador) e;
            AccionesEntrenador ent = new AccionesEntrenador(club, in);
            m.opcion("Planear una sesión de entrenamiento", () -> ent.planearSesion(en))
                    .opcion("Registrar asistencia y evaluación de una sesión", () -> ent.registrarAsistencia(en))
                    .opcion("Registrar resultado y estadísticas de un partido interno",
                            () -> ent.registrarPartidoInterno(en))
                    .opcion("Registrar resultado y estadísticas de un partido de competencia",
                            () -> ent.registrarPartidoCompetencia(en))
                    .opcion("Gestionar la nómina de una competencia", () -> ent.gestionarNomina(en));
        }
        m.opcion("Sugerir un nuevo artículo o snack", () -> emp.sugerir(e))
                .opcion("Ver mis turnos y solicitudes", () -> emp.verTurnos(e))
                .opcion("Solicitar un cambio de turno", () -> emp.solicitarCambioTurno(e))
                .opcion("Comprar en la tienda con mi descuento de empleado", () -> compras.comprarTienda(e))
                .opcion("Compartir mi código de descuento con un socio", () -> emp.compartirCodigo(e))
                .opcion("Torneos: consultar torneos y su historial", torneos::consultarTorneos)
                .opcion("Torneos: inscribirme en un torneo individual (si no estoy de turno)",
                        () -> torneos.inscribirse(e))
                .opcion("Torneos: retirarme de un torneo", () -> torneos.retirarse(e))
                .opcion("Torneos: mis torneos", () -> torneos.misTorneos(e))
                .opcion("Torneos: mis bonos", () -> torneos.misPremios(e));
        if (e instanceof Entrenador) {
            m.opcion("Torneos: inscribir un equipo", () -> torneos.inscribirEquipo(e))
                    .opcion("Torneos: retirar un equipo", () -> torneos.retirarEquipo(e));
        }
        return m;
    }
}
