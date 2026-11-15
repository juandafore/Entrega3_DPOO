package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import modelo.cafeteria.VentaCafeteria;
import modelo.comun.EstadoAsistencia;
import modelo.comun.Periodo;
import modelo.comun.TipoSesion;
import modelo.entrenamiento.AsistenciaEntrenamiento;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.tienda.CompraTienda;
import pruebas.Fixtures;

class SesionYReportesTest {
    private final Fixtures f = new Fixtures();

    @Test
    void sesionRegistraAsistenciaSoloDeConvocadosYUnaVez() {
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, LocalDate.now(), LocalTime.of(16, 0), 90,
                TipoSesion.GRUPAL, f.tenis1);
        s.convocarEquipo(f.leones);
        f.carlos.planearSesion(s);
        assertThrows(IllegalArgumentException.class, () -> s.registrarAsistencia(f.juan, EstadoAsistencia.ASISTIO));
        var jugador = f.leones.getInscripciones().get(0).getSocio();
        AsistenciaEntrenamiento a = f.carlos.registrarAsistencia(s, jugador, EstadoAsistencia.TARDE);
        assertThrows(IllegalStateException.class, () -> s.registrarAsistencia(jugador, EstadoAsistencia.ASISTIO));
        assertThrows(IllegalArgumentException.class, () -> a.evaluar(6, "x"));
        a.evaluar(4, "Bien");
        assertEquals(4, a.getNota());
        assertTrue(s.resumenAsistencia().contains("Tarde: 1"));
        assertThrows(IllegalArgumentException.class, () -> f.diego.registrarAsistencia(s, jugador,
                EstadoAsistencia.ASISTIO));
    }

    @Test
    void sesionSinConvocadosNoSePuedePlanear() {
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, LocalDate.now(), LocalTime.of(16, 0), 60,
                TipoSesion.INDIVIDUAL, null);
        assertThrows(IllegalStateException.class, () -> f.carlos.planearSesion(s));
        assertThrows(IllegalArgumentException.class, () -> new SesionEntrenamiento(f.carlos, LocalDate.now(),
                LocalTime.NOON, 0, TipoSesion.GRUPAL, null));
    }

    @Test
    void periodosCalculanRangos() {
        LocalDate ref = LocalDate.of(2026, 11, 18);
        assertEquals(LocalDate.of(2026, 11, 16), Periodo.SEMANAL.desde(ref));
        assertEquals(LocalDate.of(2026, 11, 22), Periodo.SEMANAL.hasta(ref));
        assertEquals(LocalDate.of(2026, 11, 1), Periodo.MENSUAL.desde(ref));
        assertEquals(LocalDate.of(2026, 11, 30), Periodo.MENSUAL.hasta(ref));
        assertEquals(ref, Periodo.DIARIO.desde(ref));
    }

    @Test
    void reporteDeVentasDiscriminaPorRubro() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        f.juan.comprarTienda(c);
        VentaCafeteria v = new VentaCafeteria(f.maria);
        v.agregarLinea(f.agua, 1);
        f.maria.comprarCafeteria(v);
        f.admin.registrarMensualidad(f.pedro, "2026-11", 80000);
        Map<String, Double> m = f.admin.reportes().ventasPorRubro(LocalDate.now(), LocalDate.now());
        assertEquals(59500, m.get("TIENDA"), 0.01);
        assertEquals(3000 + 240 + 300, m.get("CAFETERIA"), 0.01);
        assertEquals(80000, m.get("MENSUALIDAD"), 0.01);
        assertEquals(0.0, f.admin.reportes().ventasPorRubro(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
                .get("TIENDA"));
        assertTrue(f.admin.reportes().reporteVentas(Periodo.MENSUAL, LocalDate.now()).contains("Mensualidades"));
    }
}
