package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.EstadoPartido;
import modelo.comun.FaseAscun;
import modelo.competencia.ASCUN;
import modelo.competencia.InscripcionCompetencia;
import modelo.competencia.PartidoCompetencia;
import modelo.competencia.TorneoCerros;
import modelo.deportesconjunto.Alineacion;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.EstadisticaFutbol;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class PartidosTest {
    private Fixtures f;
    private Socio leon1;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
        leon1 = f.leones.getInscripciones().get(0).getSocio();
    }

    private PartidoInterno partido() {
        return new PartidoInterno(f.leones, f.halcones, LocalDate.now(), LocalTime.NOON);
    }

    @Test
    void partidoInternoRequiereMismoDeporteYCategoria() {
        Equipo basket = f.equipoCompleto("Basket", "b", Deporte.BALONCESTO, Categoria.MAYORES, 5);
        assertThrows(IllegalArgumentException.class,
                () -> new PartidoInterno(f.leones, basket, LocalDate.now(), LocalTime.NOON));
        assertThrows(IllegalArgumentException.class,
                () -> new PartidoInterno(f.leones, f.leones, LocalDate.now(), LocalTime.NOON));
    }

    @Test
    void sinMinimoDeJugadoresNoSePuedeJugar() {
        Equipo corto = f.equipoCompleto("Corto", "c", Deporte.FUTBOL, Categoria.MAYORES, 6);
        PartidoInterno p = new PartidoInterno(f.leones, corto, LocalDate.now(), LocalTime.NOON);
        assertFalse(p.puedeJugarse());
        assertThrows(IllegalStateException.class, () -> p.registrarMarcador(1, 0));
        assertEquals(EstadoPartido.NO_DISPUTABLE, p.getEstado());
    }

    @Test
    void dosAmarillasSuspendenParaElSiguientePartidoYLuegoSeCumple() {
        Socio suplente = new Socio("suplente", "x", "Suplente", 25);
        f.club.registrarSocio(suplente);
        suplente.inscribirseEquipo(f.leones, "suplente", 30);
        PartidoInterno p1 = partido();
        p1.registrarMarcador(2, 1);
        assertNotNull(p1.registrarEstadisticaJugador(new EstadisticaFutbol(leon1, 1, 0, 2, 0)));
        p1.finalizar();
        assertTrue(leon1.estaSuspendidoEn(null));

        PartidoInterno p2 = partido();
        assertTrue(p2.puedeJugarse());
        p2.registrarMarcador(0, 0);
        assertThrows(IllegalStateException.class,
                () -> p2.registrarEstadisticaJugador(new EstadisticaFutbol(leon1, 0, 0, 0, 0)));
        p2.finalizar();
        assertFalse(leon1.estaSuspendidoEn(null));
    }

    @Test
    void estadisticaRequiereMarcadorYNoSeDuplica() {
        PartidoInterno p = partido();
        assertThrows(IllegalStateException.class,
                () -> p.registrarEstadisticaJugador(new EstadisticaFutbol(leon1, 1, 0, 0, 0)));
        p.registrarMarcador(1, 0);
        p.registrarEstadisticaJugador(new EstadisticaFutbol(leon1, 1, 0, 0, 0));
        assertThrows(IllegalStateException.class,
                () -> p.registrarEstadisticaJugador(new EstadisticaFutbol(leon1, 1, 0, 0, 0)));
        assertThrows(IllegalStateException.class,
                () -> p.registrarEstadisticaJugador(new EstadisticaFutbol(f.juan, 1, 0, 0, 0)));
        assertEquals(1, leon1.consultarEstadisticas().size());
    }

    @Test
    void competenciaValidaRequisitosNominaYTabla() {
        TorneoCerros cerros = new TorneoCerros("Cerros", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        Equipo corto = f.equipoCompleto("Corto", "c", Deporte.FUTBOL, Categoria.MAYORES, 6);
        assertThrows(IllegalStateException.class, () -> cerros.inscribir(corto));
        InscripcionCompetencia ins = cerros.inscribir(f.leones);
        assertThrows(IllegalStateException.class, () -> cerros.inscribir(f.leones));

        PartidoCompetencia p = new PartidoCompetencia(ins, "U. Rival", "Estadio", LocalDate.now(), LocalTime.NOON);
        InscripcionEquipo ie = f.leones.getInscripciones().get(0);
        assertThrows(IllegalStateException.class, () -> p.alinear(new Alineacion(ie, true, p)));
        for (InscripcionEquipo x : f.leones.getInscripciones()) {
            f.carlos.gestionarNomina(ins, x.getSocio());
            p.alinear(new Alineacion(x, true, p));
        }
        assertTrue(ins.nominaCompleta());
        cerros.registrarResultado(p, 2, 1);
        assertEquals(3, cerros.getTabla().ordenar().get(0).getPuntos());
        p.registrarEstadisticaJugador(new EstadisticaFutbol(ie.getSocio(), 2, 0, 0, 1));
        assertEquals(ie.getSocio(), cerros.maximoGoleador());
        assertTrue(ie.getSocio().estaSuspendidoEn(cerros));
        assertFalse(ie.getSocio().estaSuspendidoEn(null));
    }

    @Test
    void baloncestoNoAdmiteEmpatesYAscunAvanzaDeFase() {
        Equipo basket = f.equipoCompleto("Basket", "b", Deporte.BALONCESTO, Categoria.MAYORES, 5);
        ASCUN ascun = new ASCUN("ASCUN", "2026", Deporte.BALONCESTO, Categoria.MAYORES, 5);
        InscripcionCompetencia ins = ascun.inscribir(basket);
        PartidoCompetencia p = new PartidoCompetencia(ins, "Rival", "Coliseo", LocalDate.now(), LocalTime.NOON);
        assertThrows(IllegalArgumentException.class, () -> ascun.registrarResultado(p, 70, 70));
        assertEquals(FaseAscun.GRUPOS, ascun.getFaseActual());
        ascun.avanzarFase();
        assertEquals(FaseAscun.ELIMINACION, ascun.getFaseActual());
    }

    @Test
    void entrenadorSoloHabilitaJugadoresDelEquipo() {
        TorneoCerros cerros = new TorneoCerros("Cerros", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        InscripcionCompetencia ins = cerros.inscribir(f.leones);
        assertThrows(IllegalArgumentException.class, () -> f.carlos.gestionarNomina(ins, f.juan));
    }
}
