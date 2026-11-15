package pruebas.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.cafeteria.VentaCafeteria;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.EstadoTorneo;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.tienda.BonoDescuento;
import modelo.tienda.CompraTienda;
import modelo.torneos.GestorTorneos;
import modelo.torneos.InscripcionTorneo;
import modelo.torneos.Torneo;
import modelo.torneos.TorneoCompetitivo;
import modelo.usuarios.Invitado;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class HistoriasTorneosTest {
    private Fixtures f;
    private GestorTorneos g;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
        g = f.admin.torneos();
        f.club.getGestorTorneos();
        f.admin.setClub(f.club);
    }

    private Torneo tenis(int cupos, boolean competitivo) {
        return g.crearTorneoIndividual("Tenis", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.ELIMINACION_DIRECTA, cupos, f.sabado, competitivo, 40000, 75);
    }

    @Test
    @DisplayName("HU-TOR-01 Crear un torneo recreativo o competitivo")
    void crearTorneo() {
        Torneo r = tenis(8, false);
        Torneo c = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.SUB15, FormatoTorneo.LIGA, 6, f.sabado,
                true, 100000, 80);
        assertEquals("Recreativo", r.tipoTorneo());
        assertEquals("Competitivo", c.tipoTorneo());
        assertEquals(2, r.cuposPreferenciales());
        assertEquals(EstadoTorneo.INSCRIPCIONES_ABIERTAS, c.getEstado());
        assertEquals(2, g.getTorneos().size());
        assertThrows(IllegalArgumentException.class, () -> g.crearTorneoIndividual("X", Disciplina.TENIS,
                Nivel.INTERMEDIO, false, FormatoTorneo.LIGA, 4, f.sabado, true, 0, 80));
    }

    @Test
    @DisplayName("HU-TOR-02 Consultar estado e historial detallado de los torneos")
    void consultarEstadoEHistorial() {
        Torneo t = tenis(4, false);
        g.inscribirIndividual(f.juan, t);
        g.inscribirIndividual(f.maria, t);
        t.iniciar();
        assertEquals(1, g.porEstado(EstadoTorneo.EN_CURSO).size());
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 0);
        assertEquals(1, g.porEstado(EstadoTorneo.FINALIZADO).size());
        String h = t.historialDetallado();
        assertTrue(h.contains("Juan") && h.contains("María"));
        assertTrue(h.contains("Campeón"));
        assertTrue(h.contains("Posiciones"));
    }

    @Test
    @DisplayName("HU-TOR-03 Inscribirme en un torneo individual")
    void inscribirseIndividual() {
        Torneo t = tenis(4, true);
        InscripcionTorneo ins = g.inscribirIndividual(f.juan, t);
        assertEquals(40000, ins.getTarifaPagada());
        assertEquals(3, t.cuposDisponibles());
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(f.juan, t));
    }

    @Test
    @DisplayName("HU-TOR-04 Inscribir una pareja en un torneo de dobles")
    void inscribirPareja() {
        Torneo dobles = g.crearTorneoIndividual("Pádel", Disciplina.PADEL, Nivel.AVANZADO, true,
                FormatoTorneo.LIGA, 4, f.sabado, false, 0, 10);
        InscripcionTorneo ins = g.inscribirPareja(f.juan, f.estudiante, dobles);
        assertEquals(2, ins.getParticipante().integrantes().size());
        assertNotNull(dobles.inscripcionDe(f.estudiante));
    }

    @Test
    @DisplayName("HU-TOR-05 Inscribir un equipo completo en un torneo de conjunto")
    void inscribirEquipo() {
        Torneo t = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, false, 0, 10);
        Socio capitan = f.leones.getInscripciones().get(0).getSocio();
        g.inscribirEquipo(capitan, f.leones, t);
        g.inscribirEquipo(f.admin, f.halcones, t);
        t.iniciar();
        assertEquals(1, t.getEncuentros().size());
        t.registrarResultado(t.encuentrosPendientes().get(0), 2, 2);
        assertEquals(EstadoTorneo.FINALIZADO, t.getEstado());
    }

    @Test
    @DisplayName("HU-TOR-06 Retirarme de un torneo antes de que inicie")
    void retirarse() {
        Torneo t = tenis(4, false);
        g.inscribirIndividual(f.juan, t);
        g.inscribirIndividual(f.estudiante, t);
        g.retirarJugador(f.juan, t);
        assertEquals(3, t.cuposDisponibles());
        g.inscribirIndividual(f.maria, t);
        t.iniciar();
        assertThrows(IllegalStateException.class, () -> g.retirarJugador(f.estudiante, t));
    }

    @Test
    @DisplayName("HU-TOR-07 Cupos preferenciales para los mejores del ranking")
    void cuposPreferenciales() {
        Torneo t = tenis(5, false);
        f.juan.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscribirseDisciplina(Disciplina.TENIS, Nivel.INTERMEDIO);
        f.maria.inscripcionEn(Disciplina.TENIS).actualizarELO(100, null);
        g.inscribirIndividual(f.juan, t);
        g.inscribirIndividual(f.estudiante, t);
        g.inscribirIndividual(f.externo, t);
        g.inscribirIndividual(f.pedro, t);
        Socio otro = new Socio("otro", "x", "Otro", 20);
        f.club.registrarSocio(otro);
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(otro, t));
        assertTrue(g.inscribirIndividual(f.maria, t).isPreferencial());

        Torneo t2 = tenis(5, false);
        for (Socio s : new Socio[] {f.juan, f.pedro, otro}) {
            g.inscribirIndividual(s, t2);
        }
        g.inscribirIndividual(f.estudiante, t2);
        f.admin.torneos().getTorneos().get(1).liberarCuposPreferenciales();
        g.inscribirIndividual(f.externo, t2);
        assertEquals(0, t2.cuposDisponibles());
    }

    @Test
    @DisplayName("HU-TOR-08 Participación de empleados en torneos individuales")
    void participacionEmpleados() {
        Torneo sabado = tenis(4, true);
        InscripcionTorneo ins = g.inscribirIndividual(f.fabiola, sabado);
        assertEquals(0, ins.getTarifaPagada());
        Torneo lunes = g.crearTorneoIndividual("Lunes", Disciplina.TENIS, Nivel.INTERMEDIO, false,
                FormatoTorneo.LIGA, 4, f.lunes, true, 40000, 75);
        assertThrows(IllegalStateException.class, () -> g.inscribirIndividual(f.carlos, lunes));
        Torneo futbol = g.crearTorneoConjunto("Fútbol", Deporte.FUTBOL, Categoria.MAYORES, FormatoTorneo.LIGA, 4,
                f.sabado, false, 0, 10);
        assertThrows(IllegalArgumentException.class, () -> g.inscribirIndividual(f.fabiola, futbol));
    }

    @Test
    @DisplayName("HU-TOR-09 Iniciar el torneo y registrar resultados de los encuentros")
    void iniciarYRegistrar() {
        Torneo t = tenis(8, false);
        Socio[] js = {f.juan, f.maria, f.pedro};
        for (Socio s : js) {
            g.inscribirIndividual(s, t);
        }
        g.inscribirIndividual(f.estudiante, t);
        t.iniciar();
        assertEquals(2, t.encuentrosPendientes().size());
        while (t.getEstado() == EstadoTorneo.EN_CURSO) {
            t.registrarResultado(t.encuentrosPendientes().get(0), 2, 1);
        }
        assertEquals(3, t.getEncuentros().size());
        assertNotNull(t.getCampeon());
    }

    @Test
    @DisplayName("HU-TOR-10 Premiación: bono en recreativos y efectivo en competitivos")
    void premiacion() {
        Torneo rec = tenis(4, false);
        g.inscribirIndividual(f.juan, rec);
        g.inscribirIndividual(f.maria, rec);
        rec.iniciar();
        rec.registrarResultado(rec.encuentrosPendientes().get(0), 2, 0);
        Socio campeon = (Socio) rec.getCampeon().integrantes().get(0);
        assertEquals(1, campeon.bonosDisponibles().size());

        TorneoCompetitivo comp = (TorneoCompetitivo) tenis(4, true);
        g.inscribirIndividual(f.juan, comp);
        g.inscribirIndividual(f.externo, comp);
        comp.iniciar();
        comp.registrarResultado(comp.encuentrosPendientes().get(0), 2, 0);
        assertEquals(60000, comp.getPremios().get(0).getMonto(), 0.01);
    }

    @Test
    @DisplayName("HU-TOR-11 Usar el bono de descuento ganado en una compra futura")
    void usarBono() {
        BonoDescuento b = new BonoDescuento("BONO-1", 10, "Torneo");
        f.juan.agregarBono(b);
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.agregarLinea(f.agua, 2);
        v.aplicarBono(b);
        f.juan.comprarCafeteria(v);
        assertEquals(600, v.getDescuento(), 0.01);
        assertTrue(b.isUsado());
        CompraTienda c = new CompraTienda(f.juan);
        c.agregarLinea(f.balon, 1);
        assertThrows(IllegalStateException.class, () -> c.aplicarBono(b));
    }

    @Test
    @DisplayName("HU-TOR-12 Registrar estudiantes y personas externas para los torneos")
    void registrarInvitados() {
        Invitado nuevo = new Invitado("luis", "luis", "Luis", "333", true);
        f.admin.registrarInvitado(nuevo);
        assertSame(nuevo, f.club.autenticar("luis", "luis"));
        assertEquals("Estudiante", nuevo.tipo());
        assertThrows(IllegalArgumentException.class,
                () -> f.admin.registrarInvitado(new Invitado("luis", "x", "Otro", "4", false)));
        assertFalse(g.puedeRetirarseLibremente(f.externo));
    }
}
