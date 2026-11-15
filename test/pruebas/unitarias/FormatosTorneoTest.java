package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import modelo.torneos.EncuentroTorneo;
import modelo.torneos.FilaTorneo;
import modelo.torneos.FormatoEliminacion;
import modelo.torneos.FormatoLiga;
import modelo.torneos.ParticipanteIndividual;
import modelo.torneos.ParticipanteTorneo;
import modelo.usuarios.Socio;

class FormatosTorneoTest {
    private List<ParticipanteTorneo> participantes(int n) {
        List<ParticipanteTorneo> r = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            r.add(new ParticipanteIndividual(new Socio("p" + i, "x", "P" + i, 20)));
        }
        return r;
    }

    @Test
    void ligaGeneraTodosContraTodosSinRepetirEnUnaRonda() {
        List<ParticipanteTorneo> ps = participantes(5);
        List<EncuentroTorneo> es = new FormatoLiga().generarIniciales(ps);
        assertEquals(10, es.size());
        Set<String> parejas = new HashSet<>();
        for (EncuentroTorneo e : es) {
            String k1 = e.getParticipanteA().getNombre() + e.getParticipanteB().getNombre();
            String k2 = e.getParticipanteB().getNombre() + e.getParticipanteA().getNombre();
            assertFalse(parejas.contains(k1) || parejas.contains(k2));
            parejas.add(k1);
        }
        for (int ronda = 1; ronda <= 5; ronda++) {
            Set<ParticipanteTorneo> vistos = new HashSet<>();
            for (EncuentroTorneo e : es) {
                if (e.getRonda() == ronda) {
                    assertTrue(vistos.add(e.getParticipanteA()));
                    assertTrue(vistos.add(e.getParticipanteB()));
                }
            }
        }
    }

    @Test
    void ligaOrdenaPorPuntosYDiferencia() {
        List<ParticipanteTorneo> ps = participantes(3);
        FormatoLiga liga = new FormatoLiga();
        List<EncuentroTorneo> es = liga.generarIniciales(ps);
        for (EncuentroTorneo e : es) {
            int ia = ps.indexOf(e.getParticipanteA());
            int ib = ps.indexOf(e.getParticipanteB());
            e.registrar(ia < ib ? 3 : 0, ia < ib ? 0 : 3, true);
        }
        assertTrue(liga.terminado(es));
        List<FilaTorneo> pos = liga.posiciones(es, ps);
        assertEquals(ps.get(0), pos.get(0).getParticipante());
        assertEquals(6, pos.get(0).puntos());
        assertEquals(ps.get(2), pos.get(2).getParticipante());
    }

    @Test
    void eliminacionConCuatroGeneraSemifinalesYFinal() {
        List<ParticipanteTorneo> ps = participantes(4);
        FormatoEliminacion f = new FormatoEliminacion();
        List<EncuentroTorneo> es = new ArrayList<>(f.generarIniciales(ps));
        assertEquals(2, es.size());

        assertEquals(ps.get(0), es.get(0).getParticipanteA());
        assertEquals(ps.get(3), es.get(0).getParticipanteB());
        es.get(0).registrar(2, 0, false);
        assertTrue(f.siguientes(es).isEmpty());
        es.get(1).registrar(2, 1, false);
        List<EncuentroTorneo> fin = f.siguientes(es);
        assertEquals(1, fin.size());
        assertEquals(2, fin.get(0).getRonda());
        es.addAll(fin);
        assertFalse(f.terminado(es));
        fin.get(0).registrar(0, 2, false);
        assertTrue(f.terminado(es));
        assertEquals(ps.get(1), f.posiciones(es, ps).get(0).getParticipante());
    }

    @Test
    void eliminacionConImparDaDescansoAlPrimerSembrado() {
        List<ParticipanteTorneo> ps = participantes(3);
        List<EncuentroTorneo> es = new FormatoEliminacion().generarIniciales(ps);
        assertEquals(2, es.size());
        assertTrue(es.get(0).esDescanso());
        assertEquals(ps.get(0), es.get(0).ganador());
        assertTrue(es.get(0).isJugado());
    }

    @Test
    void encuentroValidaMarcadorYEmpates() {
        List<ParticipanteTorneo> ps = participantes(2);
        EncuentroTorneo e = new EncuentroTorneo(1, ps.get(0), ps.get(1));
        assertThrows(IllegalArgumentException.class, () -> e.registrar(1, 1, false));
        assertThrows(IllegalArgumentException.class, () -> e.registrar(-1, 0, true));
        e.registrar(1, 1, true);
        assertNull(e.ganador());
        assertThrows(IllegalStateException.class, () -> e.registrar(2, 1, true));
    }
}
