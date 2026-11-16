package consola;

import modelo.Club;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;
import modelo.usuarios.Usuario;

public class ConsolaJugador extends ConsolaBase {
    public ConsolaJugador() {
        super();
    }

    public ConsolaJugador(Club club, Entrada in) {
        super(club, in);
    }

    public static void main(String[] args) {
        new ConsolaJugador().ejecutar();
    }

    @Override
    protected String titulo() {
        return "Consola de jugadores (socios, estudiantes y externos)";
    }

    @Override
    protected boolean acepta(Usuario u) {
        return u instanceof Socio || u instanceof Invitado;
    }

    @Override
    protected void agregarOpcionesInicio(Menu inicio) {
        inicio.opcion("Crear cuenta para torneos (estudiante de la universidad o persona externa)",
                () -> new AccionesAdminGestion(club, in).registrarInvitado(club.getAdministrador()));
    }

    @Override
    protected Menu menuPara(Usuario u) {
        AccionesTorneos torneos = new AccionesTorneos(club, in);
        if (u instanceof Invitado) {
            return new Menu("MENÚ " + u.tipo().toUpperCase() + " (" + u.getNombre() + ")")
                    .opcion("Consultar torneos y su historial", torneos::consultarTorneos)
                    .opcion("Inscribirme en un torneo individual o de dobles", () -> torneos.inscribirse(u))
                    .opcion("Retirarme de un torneo", () -> torneos.retirarse(u))
                    .opcion("Mis torneos", () -> torneos.misTorneos(u))
                    .opcion("Mis premios y bonos", () -> torneos.misPremios(u));
        }
        Socio s = (Socio) u;
        AccionesSocio dep = new AccionesSocio(club, in);
        AccionesCompras compras = new AccionesCompras(club, in);
        return new Menu("MENÚ SOCIO (" + s.getNombre() + ")")
                .opcion("Inscribirme a un equipo", () -> dep.inscribirseEquipo(s))
                .opcion("Inscribirme a una disciplina individual", () -> dep.inscribirseDisciplina(s))
                .opcion("Reservar una instalación", () -> dep.reservar(s))
                .opcion("Programar un partido de práctica", () -> dep.programarPractica(s))
                .opcion("Registrar resultado de una práctica pendiente", () -> dep.registrarPracticaPendiente(s))
                .opcion("Consultar mis estadísticas y ranking", () -> dep.consultarEstadisticas(s))
                .opcion("Consultar resultados y tablas de posiciones", () -> dep.consultarResultados(s))
                .opcion("Comprar en la tienda", () -> compras.comprarTienda(s))
                .opcion("Comprar en la cafetería", () -> compras.comprarCafeteria(s))
                .opcion("Terminar mi bebida caliente", () -> dep.terminarBebida(s))
                .opcion("Consultar mi historial y puntos", () -> dep.historial(s))
                .opcion("Torneos: consultar torneos y su historial", torneos::consultarTorneos)
                .opcion("Torneos: inscribirme (individual o dobles)", () -> torneos.inscribirse(s))
                .opcion("Torneos: inscribir a mi equipo", () -> torneos.inscribirEquipo(s))
                .opcion("Torneos: retirarme / retirar a mi equipo", () -> torneos.retirarse(s))
                .opcion("Torneos: mis torneos", () -> torneos.misTorneos(s))
                .opcion("Torneos: mis bonos y premios", () -> torneos.misPremios(s));
    }
}
