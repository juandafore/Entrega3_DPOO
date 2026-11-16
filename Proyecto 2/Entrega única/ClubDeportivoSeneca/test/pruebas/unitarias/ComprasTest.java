package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.cafeteria.VentaCafeteria;
import modelo.tienda.BonoDescuento;
import modelo.tienda.CompraTienda;
import pruebas.Fixtures;

class ComprasTest {
    private Fixtures f;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
    }

    @Test
    void compraTiendaAplicaIvaYGeneraPuntos() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.raqueta, 1);
        f.juan.comprarTienda(c);
        assertEquals(100000, c.getSubtotal(), 0.01);
        assertEquals(119000, c.getTotal(), 0.01);
        assertEquals(2380, f.juan.getPuntosFidelidad());
        assertEquals(4, f.raqueta.getCantidadDisponible());
    }

    @Test
    void sinExistenciasSeRechazaLaLinea() {
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.raqueta, 4);
        assertThrows(IllegalStateException.class, () -> c.agregarLinea(f.raqueta, 2));
        assertThrows(IllegalArgumentException.class, () -> c.agregarLinea(f.balon, 0));
    }

    @Test
    void codigoEmpleadoDa15AlDuenoY8AlSocioCompartido() {
        CompraTienda propia = new CompraTienda(f.carlos);
        propia.agregarLinea(f.raqueta, 1);
        f.carlos.comprarTienda(propia);
        assertEquals(15000, propia.getDescuento(), 0.01);

        CompraTienda sinCompartir = new CompraTienda(f.juan);
        sinCompartir.agregarLinea(f.balon, 1);
        assertThrows(IllegalStateException.class, () -> sinCompartir.aplicarCodigo(f.carlos.getCodigo()));

        f.carlos.compartirCodigo(f.juan);
        CompraTienda compartida = new CompraTienda(f.juan);
        compartida.agregarLinea(f.balon, 2);
        compartida.aplicarCodigo(f.carlos.getCodigo());
        f.juan.comprarTienda(compartida);
        assertEquals(8000, compartida.getDescuento(), 0.01);
    }

    @Test
    void bonoDeTorneoDescuentaYQuedaUsado() {
        BonoDescuento b = new BonoDescuento("B1", 20, "Torneo");
        f.juan.agregarBono(b);
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        c.aplicarBono(b);
        f.juan.comprarTienda(c);
        assertEquals(10000, c.getDescuento(), 0.01);
        assertEquals(50000 * 1.19 - 10000, c.getTotal(), 0.01);
        assertTrue(b.isUsado());
        assertTrue(f.juan.bonosDisponibles().isEmpty());
    }

    @Test
    void bonoNoEsAcumulableConOtrosDescuentos() {
        f.carlos.compartirCodigo(f.juan);
        BonoDescuento b = new BonoDescuento("B1", 20, "Torneo");
        CompraTienda conCodigo = new CompraTienda(f.juan);
        conCodigo.agregarLinea(f.balon, 1);
        conCodigo.aplicarCodigo(f.carlos.getCodigo());
        assertThrows(IllegalStateException.class, () -> conCodigo.aplicarBono(b));

        CompraTienda conBono = new CompraTienda(f.juan);
        conBono.agregarLinea(f.balon, 1);
        conBono.aplicarBono(b);
        assertThrows(IllegalStateException.class, () -> conBono.aplicarCodigo(f.carlos.getCodigo()));
        assertThrows(IllegalStateException.class, () -> conBono.redimirPuntos(100));

        CompraTienda conPuntos = new CompraTienda(f.juan);
        conPuntos.agregarLinea(f.balon, 1);
        conPuntos.redimirPuntos(100);
        assertThrows(IllegalStateException.class, () -> conPuntos.aplicarBono(b));
    }

    @Test
    void bonoUsadoNoSePuedeReutilizar() {
        BonoDescuento b = new BonoDescuento("B1", 20, "Torneo");
        b.marcarUsado();
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        assertThrows(IllegalStateException.class, () -> c.aplicarBono(b));
    }

    @Test
    void compraCanceladaNoConsumeElBono() {
        BonoDescuento b = new BonoDescuento("B1", 20, "Torneo");
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        c.aplicarBono(b);
        assertFalse(b.isUsado());
    }

    @Test
    void redimirPuntosEnCompraDescuentaSaldoYTotal() {
        f.juan.agregarPuntos(5000);
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        f.juan.redimirPuntosEn(c, 3000);
        f.juan.comprarTienda(c);
        assertEquals(50000 * 1.19 - 3000, c.getTotal(), 0.01);
        assertEquals(2000 + c.getPuntosGenerados(), f.juan.getPuntosFidelidad());
        assertThrows(IllegalStateException.class, () -> f.juan.redimirPuntosEn(new CompraTienda(f.juan), 999999));
    }

    @Test
    void cafeteriaAplicaImpuestoAlConsumoYPropinaModificable() {
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.agregarLinea(f.agua, 2);
        v.calcularTotal();
        assertEquals(600, v.getPropina(), 0.01);
        v.modificarPropina(1000);
        f.juan.comprarCafeteria(v);
        assertEquals(6000 + 480 + 1000, v.getTotal(), 0.01);
        assertEquals(149, f.juan.getPuntosFidelidad());
        assertThrows(IllegalArgumentException.class, () -> v.modificarPropina(-1));
    }

    @Test
    void bebidaCalienteRechazadaDentroDePadelOMesaTechada() {
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.setInstalacionDelSocio(f.padel);
        v.agregarLinea(f.cafe, 1);
        assertThrows(IllegalStateException.class, () -> f.juan.comprarCafeteria(v));
        VentaCafeteria v2 = new VentaCafeteria(f.juan);
        v2.setInstalacionDelSocio(f.tenis1);
        v2.agregarLinea(f.cafe, 1);
        f.juan.comprarCafeteria(v2);
        assertTrue(f.juan.tieneBebidaCaliente());
    }

    @Test
    void snackInformaAlergenos() {
        assertEquals("maní", f.mani.informarAlergenos()[0]);
        assertEquals(0, f.agua.informarAlergenos().length);
    }
}
