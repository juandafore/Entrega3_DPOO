package main;

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
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.competencia.ASCUN;
import modelo.competencia.TorneoCerros;
import modelo.deportesconjunto.Equipo;
import modelo.deportesindividuales.CanchaPadel;
import modelo.deportesindividuales.CanchaTenis;
import modelo.deportesindividuales.ConfiguracionDisciplina;
import modelo.deportesindividuales.MesaTenis;
import modelo.tienda.Articulo;
import modelo.tienda.CodigoDescuento;
import modelo.tienda.UbicacionInventario;
import modelo.torneos.GestorTorneos;
import modelo.torneos.Torneo;
import modelo.usuarios.Administrador;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Fisioterapeuta;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;

public class DatosIniciales {
    public static Club crearClubDemo() {
        Club club = new Club();

        Administrador admin = new Administrador("admin", "admin", "Ana Admin");
        club.setAdministrador(admin);

        Entrenador carlos = new Entrenador("carlos", "carlos", "Carlos Coach");
        Entrenador diego = new Entrenador("diego", "diego", "Diego Coach");
        Fisioterapeuta fabiola = new Fisioterapeuta("fabiola", "fabiola", "Fabiola Fisio");
        club.agregarEmpleado(carlos);
        club.agregarEmpleado(diego);
        club.agregarEmpleado(fabiola);
        carlos.setCodigo(new CodigoDescuento("CARLOS15", carlos));
        diego.setCodigo(new CodigoDescuento("DIEGO15", diego));
        fabiola.setCodigo(new CodigoDescuento("FABIOLA15", fabiola));

        Socio juan = new Socio("juan", "juan", "Juan Pérez", 20);
        Socio maria = new Socio("maria", "maria", "María Gómez", 19);
        Socio pedro = new Socio("pedro", "pedro", "Pedro Ruiz", 22);
        Socio laura = new Socio("laura", "laura", "Laura Díaz", 21);
        Socio tomas = new Socio("tomas", "tomas", "Tomás Rey", 15);
        club.registrarSocio(juan);
        club.registrarSocio(maria);
        club.registrarSocio(pedro);
        club.registrarSocio(laura);
        club.registrarSocio(tomas);

        Socio carlosJugador = new Socio("carlosj", "carlosj", "Carlos Coach (jugador)", 35);
        carlosJugador.setEntrenadorAsociado(carlos);
        club.registrarSocio(carlosJugador);

        club.registrarInvitado(new Invitado("sofia", "sofia", "Sofía Estudiante", "1001", true));
        club.registrarInvitado(new Invitado("andres", "andres", "Andrés Externo", "2002", false));

        Equipo leones = new Equipo("Leones FC", Deporte.FUTBOL, Categoria.MAYORES, 18);
        Equipo halcones = new Equipo("Halcones FC", Deporte.FUTBOL, Categoria.MAYORES, 18);
        Equipo tigres = new Equipo("Tigres BC", Deporte.BALONCESTO, Categoria.MAYORES, 12);
        Equipo pumas = new Equipo("Pumas Sub-15", Deporte.FUTBOL, Categoria.SUB15, 16);
        club.agregarEquipo(leones);
        club.agregarEquipo(halcones);
        club.agregarEquipo(tigres);
        club.agregarEquipo(pumas);
        String[] posiciones = {"portero", "defensa", "defensa", "mediocampista", "mediocampista", "delantero",
            "delantero", "defensa"};
        juan.inscribirseEquipo(leones, "delantero", 9);
        pedro.inscribirseEquipo(leones, "portero", 1);
        for (int i = 1; i <= 6; i++) {
            Socio s = new Socio("leon" + i, "leon" + i, "Jugador León " + i, 20 + i);
            club.registrarSocio(s);
            s.inscribirseEquipo(leones, posiciones[i], 10 + i);
        }
        for (int i = 1; i <= 8; i++) {
            Socio s = new Socio("halcon" + i, "halcon" + i, "Jugador Halcón " + i, 19 + i);
            club.registrarSocio(s);
            s.inscribirseEquipo(halcones, posiciones[i - 1], i);
        }
        maria.inscribirseEquipo(tigres, "base", 7);
        tomas.inscribirseEquipo(pumas, "delantero", 10);

        juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        laura.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        pedro.inscribirseDisciplina(Disciplina.TENIS, Nivel.PRINCIPIANTE);
        laura.inscribirseDisciplina(Disciplina.PADEL, Nivel.AVANZADO);
        carlosJugador.inscribirseDisciplina(Disciplina.TENIS, Nivel.AVANZADO);
        maria.inscribirseDisciplina(Disciplina.TENIS_MESA, Nivel.PRINCIPIANTE);

        maria.inscripcionEn(Disciplina.TENIS).actualizarELO(48, null);
        laura.inscripcionEn(Disciplina.TENIS).actualizarELO(24, null);

        club.agregarCompetencia(new TorneoCerros("Torneo de los Cerros 2026", "2026",
                Deporte.FUTBOL, Categoria.MAYORES, 7));
        club.agregarCompetencia(new ASCUN("ASCUN Baloncesto 2026", "2026", Deporte.BALONCESTO, Categoria.MAYORES, 5));

        club.agregarInstalacion(new CanchaTenis("Tenis-1", false));
        club.agregarInstalacion(new CanchaTenis("Tenis-2", false));
        club.agregarInstalacion(new CanchaPadel("Padel-1", true));
        club.agregarInstalacion(new MesaTenis("Mesa-1", true));
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.TENIS, 2));
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.PADEL, 1));
        club.agregarConfiguracion(new ConfiguracionDisciplina(Disciplina.TENIS_MESA, 1));

        UbicacionInventario bodega = new UbicacionInventario("Bodega central");
        UbicacionInventario vitrina = new UbicacionInventario("Vitrina tienda");
        club.agregarUbicacion(bodega);
        club.agregarUbicacion(vitrina);
        Articulo raqueta = new Articulo("Raqueta Pro", "Raquetas", 200000, 10);
        Articulo balon = new Articulo("Balón fútbol", "Balones", 80000, 20);
        Articulo camiseta = new Articulo("Camiseta oficial", "Uniformes", 50000, 30);
        Articulo pelotas = new Articulo("Tarro de pelotas", "Pelotas", 25000, 40);
        club.agregarArticulo(raqueta);
        club.agregarArticulo(balon);
        club.agregarArticulo(camiseta);
        club.agregarArticulo(pelotas);
        bodega.trasladar(raqueta);
        bodega.trasladar(balon);
        vitrina.trasladar(camiseta);
        vitrina.trasladar(pelotas);

        club.agregarProductoCafeteria(new Bebida("Café americano", 4000, "Café", true));
        club.agregarProductoCafeteria(new Bebida("Agua", 3000, "Agua", false));
        club.agregarProductoCafeteria(new Bebida("Jugo natural", 6000, "Jugo", false));
        club.agregarProductoCafeteria(new Snack("Mix de maní", 6000, new String[] {"maní", "gluten"}));
        club.agregarProductoCafeteria(new Snack("Sándwich de queso", 9000, new String[] {"gluten", "lácteos"}));

        DiaSemana[] habiles = {DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES,
            DiaSemana.VIERNES};
        for (DiaSemana d : habiles) {
            club.agregarHorario(new HorarioAtencion(d, LocalTime.of(8, 0), LocalTime.of(18, 0), 2, 1));
            club.registrarTurno(new TurnoEmpleado(carlos, d, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            club.registrarTurno(new TurnoEmpleado(diego, d, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            club.registrarTurno(new TurnoEmpleado(fabiola, d, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        }

        GestorTorneos g = club.getGestorTorneos();
        LocalDate sabado = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDate miercoles = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        g.crearTorneoIndividual("Copa Recreativa de Tenis", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 8, sabado, false, 0, 15);
        g.crearTorneoConjunto("Mini-campeonato Fútbol Mayores", Deporte.FUTBOL, Categoria.MAYORES,
                FormatoTorneo.LIGA, 4, sabado, true, 120000, 80);
        g.crearTorneoIndividual("Abierto de Tenis de Mesa (entre semana)", Disciplina.TENIS_MESA,
                Nivel.PRINCIPIANTE, false, FormatoTorneo.LIGA, 6, miercoles, true, 20000, 70);
        g.crearTorneoIndividual("Dobles de Pádel Avanzado", Disciplina.PADEL, Nivel.AVANZADO, true,
                FormatoTorneo.ELIMINACION_DIRECTA, 4, sabado, false, 0, 10);

        Torneo fin = g.crearTorneoIndividual("Relámpago de Tenis (finalizado)", Disciplina.TENIS,
                Nivel.INTERMEDIO, false, FormatoTorneo.ELIMINACION_DIRECTA, 4, sabado, false, 0, 20);
        g.inscribirIndividual(maria, fin);
        g.inscribirIndividual(laura, fin);
        g.inscribirIndividual(club.buscarUsuario("sofia"), fin);
        fin.iniciar();
        fin.registrarResultado(fin.encuentrosPendientes().get(0), 2, 0);
        fin.registrarResultado(fin.encuentrosPendientes().get(0), 2, 1);
        return club;
    }
}
