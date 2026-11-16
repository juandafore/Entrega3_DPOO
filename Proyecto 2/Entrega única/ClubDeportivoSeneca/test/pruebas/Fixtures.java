package pruebas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import modelo.Club;
import modelo.admin.HorarioAtencion;
import modelo.admin.TurnoEmpleado;
import modelo.cafeteria.Bebida;
import modelo.cafeteria.Snack;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.DiaSemana;
import modelo.comun.Disciplina;
import modelo.deportesconjunto.Equipo;
import modelo.deportesindividuales.CanchaPadel;
import modelo.deportesindividuales.CanchaTenis;
import modelo.deportesindividuales.ConfiguracionDisciplina;
import modelo.deportesindividuales.MesaTenis;
import modelo.tienda.Articulo;
import modelo.tienda.CodigoDescuento;
import modelo.tienda.UbicacionInventario;
import modelo.usuarios.Administrador;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Fisioterapeuta;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;

public class Fixtures {
    public Club club = new Club();
    public Administrador admin = new Administrador("admin", "admin", "Admin");
    public Entrenador carlos = new Entrenador("carlos", "carlos", "Carlos");
    public Entrenador diego = new Entrenador("diego", "diego", "Diego");
    public Fisioterapeuta fabiola = new Fisioterapeuta("fabiola", "fabiola", "Fabiola");
    public Socio juan = new Socio("juan", "juan", "Juan", 20);
    public Socio maria = new Socio("maria", "maria", "María", 19);
    public Socio pedro = new Socio("pedro", "pedro", "Pedro", 22);
    public Invitado estudiante = new Invitado("sofia", "sofia", "Sofía", "1", true);
    public Invitado externo = new Invitado("andres", "andres", "Andrés", "2", false);
    public Equipo leones;
    public Equipo halcones;
    public CanchaTenis tenis1 = new CanchaTenis("Tenis-1", false);
    public CanchaTenis tenis2 = new CanchaTenis("Tenis-2", false);
    public CanchaPadel padel = new CanchaPadel("Padel-1", true);
    public MesaTenis mesa = new MesaTenis("Mesa-1", true);
    public Articulo raqueta = new Articulo("Raqueta", "Raquetas", 100000, 5);
    public Articulo balon = new Articulo("Balón", "Balones", 50000, 10);
    public Bebida cafe = new Bebida("Café", 4000, "Café", true);
    public Bebida agua = new Bebida("Agua", 3000, "Agua", false);
    public Snack mani = new Snack("Maní", 5000, new String[] {"maní"});

    public LocalDate sabado = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
    public LocalDate lunes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    public Fixtures() {
        club.setAdministrador(admin);
        club.agregarEmpleado(carlos);
        club.agregarEmpleado(diego);
        club.agregarEmpleado(fabiola);
        carlos.setCodigo(new CodigoDescuento("CARLOS15", carlos));
        club.registrarSocio(juan);
        club.registrarSocio(maria);
        club.registrarSocio(pedro);
        club.registrarInvitado(estudiante);
        club.registrarInvitado(externo);
        leones = equipoCompleto("Leones", "leon", Deporte.FUTBOL, Categoria.MAYORES, 7);
        halcones = equipoCompleto("Halcones", "halcon", Deporte.FUTBOL, Categoria.MAYORES, 7);
        club.agregarInstalacion(tenis1);
        club.agregarInstalacion(tenis2);
        club.agregarInstalacion(padel);
        club.agregarInstalacion(mesa);
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.TENIS, 1));
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.PADEL, 1));
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.TENIS_MESA, 1));
        UbicacionInventario bodega = new UbicacionInventario("Bodega");
        club.agregarUbicacion(bodega);
        club.agregarArticulo(raqueta);
        club.agregarArticulo(balon);
        bodega.trasladar(raqueta);
        bodega.trasladar(balon);
        club.agregarProductoCafeteria(cafe);
        club.agregarProductoCafeteria(agua);
        club.agregarProductoCafeteria(mani);
        club.agregarHorario(new HorarioAtencion(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0), 2, 1));
        club.registrarTurno(new TurnoEmpleado(carlos, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        club.registrarTurno(new TurnoEmpleado(diego, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        club.registrarTurno(new TurnoEmpleado(fabiola, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0)));
    }

    public Equipo equipoCompleto(String nombre, String prefijo, Deporte d, Categoria c, int n) {
        Equipo e = new Equipo(nombre, d, c, 18);
        club.agregarEquipo(e);
        int edad = c == Categoria.MAYORES ? 20 : c == Categoria.SUB15 ? 15 : c == Categoria.SUB17 ? 17 : 12;
        for (int i = 1; i <= n; i++) {
            Socio s = new Socio(prefijo + i, "x", prefijo + " " + i, edad);
            club.registrarSocio(s);
            s.inscribirseEquipo(e, "jugador", i);
        }
        return e;
    }
}
