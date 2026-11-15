package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.comun.Disciplina;
import modelo.comun.EstadoTorneo;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.ParticipanteIndividual;
import modelo.torneos.ParticipantePareja;
import modelo.torneos.Torneo;
import modelo.torneos.TorneoRecreativo;
import modelo.usuarios.Socio;

class TorneoTest {
    private Torneo torneo;
    private Socio[] jugadores;

    @BeforeEach
    void setUp() {
        torneo = new TorneoRecreativo(1, "Copa", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 5, LocalDate.now().plusDays(3), 10);
        jugadores = new Socio[8];
        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new Socio("j" + i, "x", "Jugador " + i, 20);
        }
    }

    private ParticipanteIndividual p(int i) {
        return new ParticipanteIndividual(jugadores[i]);
    }

    @Test
    void cuposPreferencialesSonVeintePorCientoRedondeadoHaciaArriba() {
        assertEquals(1, torneo.cuposPreferenciales());
        Torneo t10 = new TorneoRecreativo(2, "T", Disciplina.TENIS, Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 10,
                LocalDate.now(), 10);
        assertEquals(2, t10.cuposPreferenciales());
        Torneo t11 = new TorneoRecreativo(3, "T", Disciplina.TENIS, Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 11,
                LocalDate.now(), 10);
        assertEquals(3, t11.cuposPreferenciales());
        Torneo t2 = new TorneoRecreativo(4, "T", Disciplina.TENIS, Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 2,
                LocalDate.now(), 10);
        assertEquals(1, t2.cuposPreferenciales());
    }

    @Test
    void elegibleOcupaCupoPreferencialYNoElegibleOcupaRegular() {
        InscripcionTorneo a = torneo.inscribir(p(0), true);
        InscripcionTorneo b = torneo.inscribir(p(1), false);
        assertTrue(a.isPreferencial());
        assertFalse(b.isPreferencial());
        assertEquals(0, torneo.cuposPreferencialesDisponibles());
        assertEquals(3, torneo.cuposRegularesDisponibles());
    }

    @Test
    void elegibleSinCupoPreferencialTomaCupoRegular() {
        torneo.inscribir(p(0), true);
        InscripcionTorneo b = torneo.inscribir(p(1), true);
        assertFalse(b.isPreferencial());
    }

    @Test
    void noElegibleNoPuedeTomarCupoPreferencial() {
        for (int i = 0; i < 4; i++) {
            torneo.inscribir(p(i), false);
        }
        assertEquals(1, torneo.cuposDisponibles());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> torneo.inscribir(p(4), false));
        assertTrue(ex.getMessage().contains("preferenciales"));

        assertTrue(torneo.inscribir(p(5), true).isPreferencial());
    }

    @Test
    void liberarCuposPreferencialesLosConvierteEnRegulares() {
        for (int i = 0; i < 4; i++) {
            torneo.inscribir(p(i), false);
        }
        torneo.liberarCuposPreferenciales();
        assertEquals(1, torneo.cuposRegularesDisponibles());
        assertFalse(torneo.inscribir(p(4), false).isPreferencial());
        assertThrows(IllegalStateException.class, () -> torneo.inscribir(p(5), true));
    }

    @Test
    void noSePuedeLiberarDosVeces() {
        torneo.liberarCuposPreferenciales();
        assertThrows(IllegalStateException.class, torneo::liberarCuposPreferenciales);
    }

    @Test
    void unJugadorNoPuedeInscribirseDosVeces() {
        torneo.inscribir(p(0), false);
        assertThrows(IllegalStateException.class, () -> torneo.inscribir(new ParticipanteIndividual(jugadores[0]), false));
    }

    @Test
    void tipoDeParticipanteDebeCoincidir() {
        assertThrows(IllegalArgumentException.class,
                () -> torneo.inscribir(new ParticipantePareja(jugadores[0], jugadores[1]), false));
    }

    @Test
    void retirarLiberaElCupo() {
        InscripcionTorneo a = torneo.inscribir(p(0), true);
        assertEquals(4, torneo.cuposDisponibles());
        torneo.retirar(a);
        assertEquals(5, torneo.cuposDisponibles());
        assertEquals(1, torneo.cuposPreferencialesDisponibles());
        assertTrue(torneo.getBitacora().get(torneo.getBitacora().size() - 1).contains("Retiro"));
    }

    @Test
    void noSePuedeRetirarNiInscribirDespuesDeIniciar() {
        InscripcionTorneo a = torneo.inscribir(p(0), false);
        torneo.inscribir(p(1), false);
        torneo.iniciar();
        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        assertThrows(IllegalStateException.class, () -> torneo.retirar(a));
        assertThrows(IllegalStateException.class, () -> torneo.inscribir(p(2), false));
    }

    @Test
    void iniciarRequiereAlMenosDosParticipantes() {
        torneo.inscribir(p(0), false);
        assertThrows(IllegalStateException.class, torneo::iniciar);
    }

    @Test
    void registrarResultadosHastaFinalizarDefineCampeon() {
        for (int i = 0; i < 4; i++) {
            torneo.inscribir(p(i), false);
        }
        torneo.iniciar();
        assertEquals(2, torneo.encuentrosPendientes().size());
        torneo.registrarResultado(torneo.encuentrosPendientes().get(0), 2, 0);
        torneo.registrarResultado(torneo.encuentrosPendientes().get(0), 0, 2);
        assertEquals(1, torneo.encuentrosPendientes().size());
        torneo.registrarResultado(torneo.encuentrosPendientes().get(0), 2, 1);
        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
        assertNotNull(torneo.getCampeon());
        assertEquals(torneo.getCampeon(), torneo.posiciones().get(0).getParticipante());
    }

    @Test
    void eliminacionIndividualNoAdmiteEmpates() {
        torneo.inscribir(p(0), false);
        torneo.inscribir(p(1), false);
        torneo.iniciar();
        assertThrows(IllegalArgumentException.class,
                () -> torneo.registrarResultado(torneo.encuentrosPendientes().get(0), 1, 1));
    }

    @Test
    void datosInvalidosAlCrear() {
        assertThrows(IllegalArgumentException.class, () -> new TorneoRecreativo(9, "X", Disciplina.TENIS,
                Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 1, LocalDate.now(), 10));
        assertThrows(IllegalArgumentException.class, () -> new TorneoRecreativo(9, "X", Disciplina.PADEL,
                Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 4, LocalDate.now(), 10));
        assertThrows(IllegalArgumentException.class, () -> new TorneoRecreativo(9, " ", Disciplina.TENIS,
                Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 4, LocalDate.now(), 10));
    }

    @Test
    void historialDetalladoIncluyeParticipantesEncuentrosYPosiciones() {
        torneo.inscribir(p(0), false);
        torneo.inscribir(p(1), false);
        torneo.iniciar();
        String h = torneo.historialDetallado();
        assertTrue(h.contains("Participantes"));
        assertTrue(h.contains("Encuentros"));
        assertTrue(h.contains("Posiciones"));
        assertTrue(h.contains("Jugador 0"));
    }
}
