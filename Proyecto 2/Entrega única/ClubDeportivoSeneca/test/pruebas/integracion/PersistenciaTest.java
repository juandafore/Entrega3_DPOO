package pruebas.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import main.DatosIniciales;
import modelo.Club;
import modelo.comun.EstadoTorneo;
import modelo.torneos.Torneo;
import modelo.usuarios.Socio;
import persistencia.PersistenciaClub;

class PersistenciaTest {
    @Test
    @DisplayName("Persistencia: guardar y cargar el club completo")
    void guardarYCargar(@TempDir Path dir) throws Exception {
        Club original = DatosIniciales.crearClubDemo();
        PersistenciaClub.guardar(original, dir.toString());
        Club cargado = PersistenciaClub.cargar(dir.toString());
        assertNotNull(cargado);
        assertEquals(original.todosLosUsuarios().size(), cargado.todosLosUsuarios().size());
        assertEquals(original.getGestorTorneos().getTorneos().size(), cargado.getGestorTorneos().getTorneos().size());
        Torneo fin = cargado.getGestorTorneos().porEstado(EstadoTorneo.FINALIZADO).get(0);
        Socio maria = (Socio) cargado.autenticar("maria", "maria");
        assertEquals(maria, fin.getCampeon().integrantes().get(0));
        assertTrue(maria.bonosDisponibles().size() > 0);
        assertNotNull(cargado.autenticar("sofia", "sofia"));
    }

    @Test
    @DisplayName("Persistencia: carpeta sin datos devuelve null")
    void carpetaVacia(@TempDir Path dir) throws Exception {
        assertNull(PersistenciaClub.cargar(dir.toString()));
    }
}
