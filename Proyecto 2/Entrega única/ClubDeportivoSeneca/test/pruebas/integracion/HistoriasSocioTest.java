package pruebas.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.cafeteria.VentaCafeteria;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.Modalidad;
import modelo.comun.Nivel;
import modelo.competencia.InscripcionCompetencia;
import modelo.competencia.PartidoCompetencia;
import modelo.competencia.TorneoCerros;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.EstadisticaFutbol;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.deportesindividuales.PartidoPractica;
import modelo.deportesindividuales.Reserva;
import modelo.tienda.CompraTienda;
import pruebas.Fixtures;

class HistoriasSocioTest {
    private Fixtures f;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
    }

    @Test
    @DisplayName("RF-SOC-01 Iniciar sesión")
    void iniciarSesion() {
        assertSame(f.juan, f.club.autenticar("juan", "juan"));
        assertNull(f.club.autenticar("juan", "otra"));
    }

    @Test
    @DisplayName("RF-SOC-02 Inscribirme a un equipo de deporte de conjunto")
    void inscribirseEquipo() {
        Equipo voley = new Equipo("Voley", Deporte.VOLEIBOL, Categoria.MAYORES, 12);
        f.admin.crearEquipo(voley);
        InscripcionEquipo ins = f.juan.inscribirseEquipo(voley, "líbero", 5);
        assertEquals("líbero", ins.getPosicion());
        assertTrue(voley.tieneJugador(f.juan));
        assertThrows(IllegalStateException.class, () -> f.maria.inscribirseEquipo(voley, "armadora", 5));
    }

    @Test
    @DisplayName("RF-SOC-03 Inscribirme a una disciplina individual")
    void inscribirseDisciplina() {
        f.juan.inscribirseDisciplina(Disciplina.PADEL, Nivel.AVANZADO);
        assertEquals(Nivel.AVANZADO, f.juan.inscripcionEn(Disciplina.PADEL).getNivel());
        assertEquals(1000, f.juan.inscripcionEn(Disciplina.PADEL).getPuntajeELO());
    }

    @Test
    @DisplayName("RF-SOC-04 Reservar una instalación deportiva")
    void reservarInstalacion() {
        Reserva ok = new Reserva(f.juan, f.padel, f.sabado, LocalTime.of(9, 0), LocalTime.of(10, 0), 4,
                Modalidad.DOBLES);
        assertTrue(f.club.reservar(f.juan, f.padel, ok));
        Reserva cupoLleno = new Reserva(f.maria, f.padel, f.sabado, LocalTime.of(9, 30), LocalTime.of(10, 30), 4,
                Modalidad.DOBLES);
        assertFalse(f.club.reservar(f.maria, f.padel, cupoLleno));
        VentaCafeteria cafe = new VentaCafeteria(f.pedro);
        cafe.agregarLinea(f.cafe, 1);
        f.pedro.comprarCafeteria(cafe);
        Reserva conCafe = new Reserva(f.pedro, f.mesa, f.sabado, LocalTime.of(15, 0), LocalTime.of(16, 0), 2,
                Modalidad.SENCILLOS);
        assertFalse(f.club.reservar(f.pedro, f.mesa, conCafe));
    }

    @Test
    @DisplayName("RF-SOC-05 Programar un partido de práctica")
    void programarPractica() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.PRINCIPIANTE);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.AVANZADO);
        assertThrows(IllegalStateException.class, () -> f.juan.programarPractica(f.maria, Disciplina.TENIS));
        f.maria.setEntrenadorAsociado(f.carlos);
        PartidoPractica p = f.juan.programarPractica(f.maria, Disciplina.TENIS);
        p.registrarMarcador(0, 2);
        p.getParticipantes().get(0).registrarEstadisticas(1, 3, 10, 0);
        p.actualizarELO();
        assertTrue(p.isJugado());
    }

    @Test
    @DisplayName("RF-SOC-06 Consultar mis estadísticas y ranking")
    void consultarEstadisticasYRanking() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        PartidoPractica p = f.juan.programarPractica(f.maria, Disciplina.TENIS);
        p.registrarMarcador(2, 0);
        p.actualizarELO();
        assertEquals(1024, f.juan.inscripcionEn(Disciplina.TENIS).getPuntajeELO());
        assertEquals(1, f.juan.inscripcionEn(Disciplina.TENIS).getHistorial().size());
        var leon = f.leones.getInscripciones().get(0).getSocio();
        PartidoInterno pi = new PartidoInterno(f.leones, f.halcones, f.sabado, LocalTime.NOON);
        pi.registrarMarcador(1, 0);
        pi.registrarEstadisticaJugador(new EstadisticaFutbol(leon, 1, 0, 0, 0));
        assertEquals(1, leon.consultarEstadisticas().size());
    }

    @Test
    @DisplayName("RF-SOC-07 Consultar resultados y tabla de posiciones")
    void consultarResultadosYTabla() {
        TorneoCerros cerros = new TorneoCerros("Cerros", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        f.club.agregarCompetencia(cerros);
        InscripcionCompetencia ins = f.admin.inscribirEquipo(f.leones, cerros);
        PartidoCompetencia p = new PartidoCompetencia(ins, "Rival", "Estadio", f.sabado, LocalTime.NOON);
        cerros.registrarResultado(p, 1, 1);
        assertEquals(1, cerros.getTabla().ordenar().get(0).getPuntos());
        assertEquals(1, cerros.getPartidos().size());
    }

    @Test
    @DisplayName("RF-SOC-08 Comprar artículos en la tienda")
    void comprarTienda() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.raqueta, 1);
        c.agregarLinea(f.balon, 2);
        f.juan.comprarTienda(c);
        assertEquals(200000 * 1.19, c.getTotal(), 0.01);
        assertEquals(4760, f.juan.getPuntosFidelidad());
        assertEquals(8, f.balon.getCantidadDisponible());
    }

    @Test
    @DisplayName("RF-SOC-09 Comprar en la cafetería")
    void comprarCafeteria() {
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.agregarLinea(f.mani, 1);
        v.agregarLinea(f.cafe, 1);
        f.juan.comprarCafeteria(v);
        assertEquals(9000 * 1.18, v.getTotal(), 0.01);
        assertTrue(f.juan.getPuntosFidelidad() > 0);
        assertEquals(1, f.juan.getCompras().size());
    }

    @Test
    @DisplayName("RF-SOC-10 Redimir puntos de fidelidad")
    void redimirPuntos() {
        f.juan.agregarPuntos(10000);
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        f.juan.redimirPuntosEn(c, 10000);
        f.juan.comprarTienda(c);
        assertEquals(59500 - 10000, c.getTotal(), 0.01);
        assertEquals(c.getPuntosGenerados(), f.juan.getPuntosFidelidad());
    }

    @Test
    @DisplayName("RF-SOC-11 Consultar mi historial")
    void consultarHistorial() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        f.juan.comprarTienda(c);
        f.club.reservar(f.juan, f.tenis1, new Reserva(f.juan, f.tenis1, f.sabado, LocalTime.of(8, 0),
                LocalTime.of(9, 0), 2, Modalidad.SENCILLOS));
        assertEquals(2, f.juan.consultarHistorial().size());
    }
}
