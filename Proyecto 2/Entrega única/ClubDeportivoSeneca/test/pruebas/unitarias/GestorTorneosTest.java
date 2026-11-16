package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.EstadoTorneo;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.torneos.GestorTorneos;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.Torneo;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class GestorTorneosTest {
    private Fixtures f;
    private GestorTorneos g;
    private Torneo tenisSabado;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
        g = f.club.getGestorTorneos();
        tenisSabado = g.crearTorneoIndividual("Tenis", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.LIGA, 5, f.sabado, false, 0, 10);
    }

    @Test
    void administradorNoPuedeInscribirse() {
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(f.admin, tenisSabado));
    }

    @Test
    void empleadoDeTurnoElDiaDelTorneoEsRechazado() {
        Torneo lunes = g.crearTorneoIndividual("Lunes", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.LIGA, 4, f.lunes, false, 0, 10);
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(f.carlos, lunes));
        assertEquals(0, lunes.getInscripciones().size());
    }

    @Test
    void empleadoSinTurnoPuedeInscribirseEnIndividual() {
        InscripcionTorneo ins = g.inscribirIndividual(f.fabiola, tenisSabado);
        assertEquals(f.fabiola, ins.getParticipante().integrantes().get(0));
    }

    @Test
    void socioConNivelDistintoEsRechazado() {
        f.pedro.inscribirseDisciplina(Disciplina.TENIS, Nivel.PRINCIPIANTE);
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(f.pedro, tenisSabado));
    }

    @Test
    void estudiantesYExternosPuedenParticipar() {
        g.inscribirIndividual(f.estudiante, tenisSabado);
        g.inscribirIndividual(f.externo, tenisSabado);
        assertEquals(2, tenisSabado.getInscripciones().size());
    }

    @Test
    void mejorDelRankingObtieneCupoPreferencial() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscripcionEn(Disciplina.TENIS).actualizarELO(50, null);
        assertTrue(g.esPreferencial(f.maria, tenisSabado));
        assertFalse(g.esPreferencial(f.juan, tenisSabado));
        assertFalse(g.esPreferencial(f.estudiante, tenisSabado));
        assertTrue(g.inscribirIndividual(f.maria, tenisSabado).isPreferencial());
        assertFalse(g.inscribirIndividual(f.juan, tenisSabado).isPreferencial());
    }

    @Test
    void parejaSoloEnTorneoDeDobles() {
        assertThrows(IllegalArgumentException.class, () -> g.inscribirPareja(f.juan, f.maria, tenisSabado));
        Torneo dobles = g.crearTorneoIndividual("Dobles", Disciplina.TENIS, Nivel.INTERMEDIO, true,
                FormatoTorneo.LIGA, 4, f.sabado, false, 0, 10);
        g.inscribirPareja(f.juan, f.estudiante, dobles);
        assertThrows(IllegalStateException.class, () -> g.inscribirPareja(f.juan, f.maria, dobles));
    }

    @Test
    void equipoCompletoSeInscribeEnTorneoDeConjunto() {
        Torneo futbol = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, true, 100000, 80);
        Socio capitan = (Socio) f.leones.getInscripciones().get(0).getSocio();
        InscripcionTorneo ins = g.inscribirEquipo(capitan, f.leones, futbol);
        assertEquals(100000, ins.getTarifaPagada());

        assertThrows(IllegalStateException.class, () -> g.inscribirEquipo(f.juan, f.halcones, futbol));

        g.inscribirEquipo(f.carlos, f.halcones, futbol);
        assertEquals(2, futbol.getInscripciones().size());
    }

    @Test
    void equipoIncompletoODeOtraCategoriaEsRechazado() {
        Torneo futbol = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, false, 0, 10);
        Equipo incompleto = f.equipoCompleto("Incompleto", "inc", Deporte.FUTBOL, Categoria.MAYORES, 5);
        Equipo sub15 = f.equipoCompleto("Sub15", "sub", Deporte.FUTBOL, Categoria.SUB15, 7);
        assertThrows(IllegalStateException.class, () -> g.inscribirEquipo(f.admin, incompleto, futbol));
        assertThrows(IllegalArgumentException.class, () -> g.inscribirEquipo(f.admin, sub15, futbol));
        assertThrows(IllegalArgumentException.class, () -> g.inscribirIndividual(f.juan, futbol));
    }

    @Test
    void equipoConMejorDesempenoObtieneCupoPreferencial() {
        Torneo futbol = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, false, 0, 10);
        PartidoInterno p = new PartidoInterno(f.halcones, f.leones, f.sabado.minusDays(10), null);
        p.registrarMarcador(3, 0);
        f.club.agregarPartidoInterno(p);
        assertEquals(3, g.desempeno(f.halcones));
        assertTrue(g.esPreferencial(f.halcones, futbol));
        assertFalse(g.esPreferencial(f.leones, futbol));
    }

    @Test
    void socioYEstudiantePuedenRetirarseExternoYEmpleadoNo() {
        g.inscribirIndividual(f.juan, tenisSabado);
        g.inscribirIndividual(f.estudiante, tenisSabado);
        g.inscribirIndividual(f.externo, tenisSabado);
        g.inscribirIndividual(f.fabiola, tenisSabado);
        g.retirarJugador(f.juan, tenisSabado);
        g.retirarJugador(f.estudiante, tenisSabado);
        assertThrows(IllegalStateException.class, () -> g.retirarJugador(f.externo, tenisSabado));
        assertThrows(IllegalStateException.class, () -> g.retirarJugador(f.fabiola, tenisSabado));
        assertEquals(2, tenisSabado.getInscripciones().size());
    }

    @Test
    void retirarEquipoLiberaCupo() {
        Torneo futbol = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, false, 0, 10);
        g.inscribirEquipo(f.admin, f.leones, futbol);
        g.retirarEquipo(f.admin, f.leones, futbol);
        assertEquals(4, futbol.cuposDisponibles());
    }

    @Test
    void consultaPorEstado() {
        g.inscribirIndividual(f.juan, tenisSabado);
        g.inscribirIndividual(f.maria, tenisSabado);
        tenisSabado.iniciar();
        assertEquals(1, g.porEstado(EstadoTorneo.EN_CURSO).size());
        assertEquals(0, g.porEstado(EstadoTorneo.FINALIZADO).size());
        assertEquals(1, g.torneosDe(f.juan).size());
    }
}
