package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import modelo.comun.Disciplina;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.torneos.ParticipanteIndividual;
import modelo.torneos.ParticipantePareja;
import modelo.torneos.TorneoCompetitivo;
import modelo.torneos.TorneoRecreativo;
import modelo.usuarios.Entrenador;
import modelo.usuarios.Socio;

class PremiosTorneoTest {
    private final Socio a = new Socio("a", "x", "A", 20);
    private final Socio b = new Socio("b", "x", "B", 20);
    private final Socio c = new Socio("c", "x", "C", 20);
    private final Entrenador coach = new Entrenador("coach", "x", "Coach");

    @Test
    void recreativoEsGratisYEntregaBonoAlCampeon() {
        TorneoRecreativo t = new TorneoRecreativo(1, "R", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 4, LocalDate.now(), 15);
        assertEquals(0, t.inscribir(new ParticipanteIndividual(a), false).getTarifaPagada());
        t.inscribir(new ParticipanteIndividual(b), false);
        t.iniciar();
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 0);
        assertEquals(1, a.bonosDisponibles().size());
        assertEquals(15, a.bonosDisponibles().get(0).getPorcentaje());
        assertTrue(b.getBonos().isEmpty());
    }

    @Test
    void recreativoDeDoblesEntregaBonoACadaIntegrante() {
        TorneoRecreativo t = new TorneoRecreativo(1, "R", Disciplina.PADEL, Nivel.AVANZADO, true,
                FormatoTorneo.LIGA, 4, LocalDate.now(), 10);
        Socio d = new Socio("d", "x", "D", 20);
        t.inscribir(new ParticipantePareja(a, b), false);
        t.inscribir(new ParticipantePareja(c, d), false);
        t.iniciar();
        t.registrarResultado(t.encuentrosPendientes().get(0), 6, 3);
        assertEquals(1, a.getBonos().size());
        assertEquals(1, b.getBonos().size());
        assertEquals(2, t.getBonosEntregados().size());
    }

    @Test
    void competitivoCobraTarifaYEmpleadosNoPagan() {
        TorneoCompetitivo t = new TorneoCompetitivo(1, "C", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 4, LocalDate.now(), 50000, 80);
        assertEquals(50000, t.inscribir(new ParticipanteIndividual(a), false).getTarifaPagada());
        assertEquals(0, t.inscribir(new ParticipanteIndividual(coach), false).getTarifaPagada());
        assertEquals(50000, t.totalRecaudado());
        assertEquals(40000, t.premioTotal());
    }

    @Test
    void parejaConEmpleadoPagaSoloLaParteDelNoEmpleado() {
        TorneoCompetitivo t = new TorneoCompetitivo(1, "C", Disciplina.TENIS, Nivel.INTERMEDIO, true,
                FormatoTorneo.LIGA, 4, LocalDate.now(), 60000, 50);
        assertEquals(30000, t.tarifaPara(new ParticipantePareja(a, coach)));
        assertEquals(60000, t.tarifaPara(new ParticipantePareja(a, b)));
    }

    @Test
    void empleadoCampeonNoRecibePremioEnEfectivo() {
        TorneoCompetitivo t = new TorneoCompetitivo(1, "C", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 4, LocalDate.now(), 50000, 100);
        t.inscribir(new ParticipanteIndividual(coach), false);
        t.inscribir(new ParticipanteIndividual(a), false);
        t.iniciar();
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 0);
        assertEquals(coach, t.getCampeon().integrantes().get(0));
        assertTrue(t.getPremios().isEmpty());
    }

    @Test
    void socioCampeonRecibePremioAcordeConLoRecaudado() {
        TorneoCompetitivo t = new TorneoCompetitivo(1, "C", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, 4, LocalDate.now(), 50000, 80);
        t.inscribir(new ParticipanteIndividual(a), false);
        t.inscribir(new ParticipanteIndividual(b), false);
        t.inscribir(new ParticipanteIndividual(c), false);
        t.iniciar();
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 0);
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 1);
        assertEquals(1, t.getPremios().size());
        assertEquals(a, t.getPremios().get(0).getBeneficiario());
        assertEquals(120000, t.getPremios().get(0).getMonto(), 0.001);
    }

    @Test
    void competitivoRequiereTarifaPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new TorneoCompetitivo(1, "C", Disciplina.TENIS,
                Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 4, LocalDate.now(), 0, 80));
    }
}
