package pruebas.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.DiaSemana;
import modelo.comun.EstadoAsistencia;
import modelo.comun.Modalidad;
import modelo.comun.Periodo;
import modelo.comun.TipoSesion;
import modelo.comun.TipoSugerencia;
import modelo.competencia.ASCUN;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesindividuales.Reserva;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.tienda.CompraTienda;
import modelo.tienda.UbicacionInventario;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class HistoriasAdministradorTest {
    private Fixtures f;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
    }

    @Test
    @DisplayName("RF-ADM-01 Iniciar sesión")
    void iniciarSesion() {
        assertSame(f.admin, f.club.autenticar("admin", "admin"));
        assertNull(f.club.autenticar("admin", "x"));
    }

    @Test
    @DisplayName("RF-ADM-02 Crear y dar de baja equipos")
    void crearYBajaEquipos() {
        Equipo e = new Equipo("Nuevos", Deporte.BALONCESTO, Categoria.SUB17, 10);
        f.admin.crearEquipo(e);
        assertTrue(f.club.getEquipos().contains(e));
        assertThrows(IllegalArgumentException.class,
                () -> f.admin.crearEquipo(new Equipo("nuevos", Deporte.FUTBOL, Categoria.MAYORES, 10)));
        f.admin.darDeBajaEquipo(e);
        assertFalse(f.club.getEquipos().contains(e));
        assertFalse(e.isActivo());
    }

    @Test
    @DisplayName("RF-ADM-03 Crear y dar de baja jugadores")
    void crearYBajaJugadores() {
        Socio s = new Socio("nuevo", "nuevo", "Nuevo", 18);
        f.admin.registrarSocio(s);
        assertSame(s, f.club.autenticar("nuevo", "nuevo"));
        f.admin.darDeBajaSocio(s);
        assertNull(f.club.autenticar("nuevo", "nuevo"));
    }

    @Test
    @DisplayName("RF-ADM-04 Consultar el estado del inventario")
    void consultarInventario() {
        String inv = f.admin.reportes().estadoInventario();
        assertTrue(inv.contains("Raqueta"));
        assertTrue(inv.contains("Tenis-1"));
        assertTrue(inv.contains("Bodega"));
    }

    @Test
    @DisplayName("RF-ADM-05 Mover artículos entre ubicaciones")
    void moverArticulos() {
        UbicacionInventario vitrina = new UbicacionInventario("Vitrina");
        f.club.agregarUbicacion(vitrina);
        f.admin.trasladar(f.raqueta, vitrina);
        assertSame(vitrina, f.raqueta.getUbicacion());
        assertFalse(f.club.getUbicaciones().get(0).getArticulos().contains(f.raqueta));
    }

    @Test
    @DisplayName("RF-ADM-06 Reabastecer la tienda")
    void reabastecer() {
        f.admin.reabastecer(f.raqueta, 10);
        assertEquals(15, f.raqueta.getCantidadDisponible());
        assertThrows(IllegalArgumentException.class, () -> f.admin.reabastecer(f.raqueta, -1));
    }

    @Test
    @DisplayName("RF-ADM-07 Aprobar sugerencias de artículos o snacks")
    void aprobarSugerencias() {
        SugerenciaArticulo s = new SugerenciaArticulo(f.carlos, "Grip", TipoSugerencia.ARTICULO);
        f.club.registrarSugerencia(s);
        f.admin.resolverSugerencia(s, 15000, "Accesorios", new String[0]);
        assertTrue(f.club.getArticulos().stream().anyMatch(a -> a.getNombre().equals("Grip") && a.getPrecio() == 15000));
        SugerenciaArticulo r = new SugerenciaArticulo(f.carlos, "Chicle", TipoSugerencia.SNACK);
        f.admin.resolverSugerencia(r, false);
        assertThrows(IllegalStateException.class, () -> f.admin.resolverSugerencia(r, 1000, "x", new String[0]));
    }

    @Test
    @DisplayName("RF-ADM-08 Administrar turnos de empleados")
    void administrarTurnos() {
        Entrenador elena = new Entrenador("elena", "x", "Elena");
        f.admin.registrarEmpleado(elena);
        TurnoEmpleado t = new TurnoEmpleado(elena, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0));
        f.admin.definirTurno(elena, t);
        assertThrows(IllegalStateException.class, () -> f.admin.definirTurno(elena,
                new TurnoEmpleado(elena, DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(10, 0))));
        SolicitudCambioTurno s = new SolicitudCambioTurno(f.carlos, f.carlos.getTurnos().get(0), DiaSemana.MARTES,
                LocalTime.of(8, 0), LocalTime.of(12, 0));
        f.club.registrarSolicitud(s);
        assertTrue(f.admin.resolverTurno(s, true));
        assertFalse(f.admin.eliminarTurno(f.fabiola.getTurnos().get(0)));
    }

    @Test
    @DisplayName("RF-ADM-09 Inscribir un equipo en una competencia")
    void inscribirEnCompetencia() {
        ASCUN ascun = new ASCUN("ASCUN", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        f.club.agregarCompetencia(ascun);
        f.admin.inscribirEquipo(f.leones, ascun);
        Equipo sub15 = f.equipoCompleto("Sub15", "s", Deporte.FUTBOL, Categoria.SUB15, 7);
        assertThrows(IllegalStateException.class, () -> f.admin.inscribirEquipo(sub15, ascun));
        assertEquals(1, ascun.getInscripciones().size());
    }

    @Test
    @DisplayName("RF-ADM-10 Consultar historial completo")
    void consultarHistorial() {
        f.club.reservar(f.juan, f.tenis1, new Reserva(f.juan, f.tenis1, f.sabado, LocalTime.of(8, 0),
                LocalTime.of(9, 0), 2, Modalidad.SENCILLOS));
        PartidoInterno p = new PartidoInterno(f.leones, f.halcones, f.sabado, LocalTime.NOON);
        p.registrarMarcador(1, 0);
        f.club.agregarPartidoInterno(p);
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, f.sabado, LocalTime.of(7, 0), 60,
                TipoSesion.GRUPAL, null);
        s.convocarEquipo(f.leones);
        f.club.agregarSesion(s);
        String h = f.admin.reportes().historialDetallado();
        assertTrue(h.contains("Reserva Tenis-1"));
        assertTrue(h.contains("Leones 1-0 Halcones"));
        assertTrue(h.contains("Sesión GRUPAL"));
    }

    @Test
    @DisplayName("RF-ADM-11 Generar reportes de ventas")
    void reportesVentas() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        f.juan.comprarTienda(c);
        String r = f.admin.reportes().reporteVentas(Periodo.SEMANAL, LocalDate.now());
        assertTrue(r.contains("59.500") || r.contains("59,500"));
    }

    @Test
    @DisplayName("RF-ADM-12 Generar reportes deportivos")
    void reportesDeportivos() {
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, LocalDate.now(), LocalTime.of(7, 0), 60,
                TipoSesion.INDIVIDUAL, null);
        s.convocar(f.juan);
        s.registrarAsistencia(f.juan, EstadoAsistencia.ASISTIO);
        f.club.agregarSesion(s);
        String r = f.admin.reportes().reporteDeportivo(Periodo.DIARIO, LocalDate.now());
        assertTrue(r.contains("Juan: asistió 1"));
        assertTrue(r.contains("Evolución de rankings"));
    }
}
