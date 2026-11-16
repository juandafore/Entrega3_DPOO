package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.cafeteria.VentaCafeteria;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.Nivel;
import modelo.deportesconjunto.Equipo;
import modelo.deportesindividuales.PartidoPractica;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class SocioTest {
    private Fixtures f;
    private Equipo tigres;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
        tigres = new Equipo("Tigres", Deporte.BALONCESTO, Categoria.MAYORES, 5);
        f.club.agregarEquipo(tigres);
    }

    @Test
    void inscripcionEnEquipoAsignaPosicionYCamiseta() {
        f.juan.inscribirseEquipo(tigres, "base", 7);
        assertTrue(tigres.tieneJugador(f.juan));
        assertEquals(7, f.juan.getInscripcionesEquipo().get(0).getNumeroCamiseta());
    }

    @Test
    void camisetaRepetidaCupoLlenoYEdadSonRechazados() {
        f.juan.inscribirseEquipo(tigres, "base", 7);
        assertThrows(IllegalStateException.class, () -> f.maria.inscribirseEquipo(tigres, "escolta", 7));
        Socio nino = new Socio("nino", "x", "Niño", 12);
        assertThrows(IllegalStateException.class, () -> nino.inscribirseEquipo(tigres, "base", 9));
        for (int i = 0; i < 4; i++) {
            new Socio("t" + i, "x", "T" + i, 20).inscribirseEquipo(tigres, "x", 20 + i);
        }
        assertThrows(IllegalStateException.class, () -> f.maria.inscribirseEquipo(tigres, "pivot", 30));
    }

    @Test
    void unSoloEquipoPorDeportePeroVariosDeportes() {
        Equipo otroBasket = new Equipo("Otro", Deporte.BALONCESTO, Categoria.MAYORES, 10);
        f.juan.inscribirseEquipo(tigres, "base", 7);
        assertThrows(IllegalStateException.class, () -> f.juan.inscribirseEquipo(otroBasket, "base", 1));
        Equipo voley = new Equipo("Voley", Deporte.VOLEIBOL, Categoria.MAYORES, 12);
        f.juan.inscribirseEquipo(voley, "líbero", 3);
        assertEquals(2, f.juan.getInscripcionesEquipo().size());
    }

    @Test
    void disciplinaNoSePuedeRepetir() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        assertThrows(IllegalStateException.class, () -> f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.AVANZADO));
    }

    @Test
    void practicaEntreNivelesAdyacentesSiYExtremosNo() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.AVANZADO);
        f.pedro.inscribirseDisciplina(Disciplina.TENIS, Nivel.PRINCIPIANTE);
        assertNotNull(f.juan.programarPractica(f.maria, Disciplina.TENIS));
        assertThrows(IllegalStateException.class, () -> f.pedro.programarPractica(f.maria, Disciplina.TENIS));
    }

    @Test
    void entrenadorPuedeJugarContraCualquierNivel() {
        f.pedro.inscribirseDisciplina(Disciplina.TENIS, Nivel.PRINCIPIANTE);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.AVANZADO);
        f.maria.setEntrenadorAsociado(f.carlos);
        assertNotNull(f.pedro.programarPractica(f.maria, Disciplina.TENIS));
    }

    @Test
    void eloSeActualizaTrasLaPractica() {
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        PartidoPractica p = f.juan.programarPractica(f.maria, Disciplina.TENIS);
        p.registrarMarcador(2, 1);
        p.actualizarELO();
        assertEquals(1024, f.juan.inscripcionEn(Disciplina.TENIS).getPuntajeELO());
        assertEquals(976, f.maria.inscripcionEn(Disciplina.TENIS).getPuntajeELO());
        assertThrows(IllegalStateException.class, p::actualizarELO);
        assertEquals(1, f.juan.getPartidosPractica().size());
    }

    @Test
    void conBebidaCalienteNoPuedeIngresarAInstalacionTechadaRestringida() {
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.agregarLinea(f.cafe, 1);
        f.juan.comprarCafeteria(v);
        assertFalse(f.juan.puedeIngresarA(f.padel));
        assertFalse(f.juan.puedeIngresarA(f.mesa));
        assertTrue(f.juan.puedeIngresarA(f.tenis1));
        f.juan.terminarConsumo();
        assertTrue(f.juan.puedeIngresarA(f.padel));
    }

    @Test
    void redimirPuntosValidaSaldo() {
        f.juan.agregarPuntos(100);
        assertThrows(IllegalStateException.class, () -> f.juan.redimirPuntos(200));
        assertThrows(IllegalArgumentException.class, () -> f.juan.redimirPuntos(0));
        assertEquals(50, f.juan.redimirPuntos(50), 0.01);
        assertEquals(50, f.juan.getPuntosFidelidad());
    }
}
