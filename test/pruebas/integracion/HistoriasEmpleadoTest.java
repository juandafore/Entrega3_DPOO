package pruebas.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.DiaSemana;
import modelo.comun.EstadoAsistencia;
import modelo.comun.EstadoSolicitud;
import modelo.comun.EstadoSugerencia;
import modelo.comun.TipoSesion;
import modelo.comun.TipoSugerencia;
import modelo.competencia.InscripcionCompetencia;
import modelo.competencia.TorneoCerros;
import modelo.deportesconjunto.EstadisticaFutbol;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.PartidoInterno;
import modelo.entrenamiento.AsistenciaEntrenamiento;
import modelo.entrenamiento.SesionEntrenamiento;
import modelo.tienda.CompraTienda;
import pruebas.Fixtures;

class HistoriasEmpleadoTest {
    private Fixtures f;

    @BeforeEach
    void setUp() {
        f = new Fixtures();
    }

    @Test
    @DisplayName("RF-EMP-01 Iniciar sesión (entrenador/fisioterapeuta)")
    void iniciarSesion() {
        assertSame(f.carlos, f.club.autenticar("carlos", "carlos"));
        assertSame(f.fabiola, f.club.autenticar("fabiola", "fabiola"));
    }

    @Test
    @DisplayName("RF-EMP-02 Planear una sesión de entrenamiento")
    void planearSesion() {
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, f.sabado, LocalTime.of(16, 0), 90,
                TipoSesion.GRUPAL, f.tenis1);
        s.convocarEquipo(f.leones);
        f.carlos.planearSesion(s);
        f.club.agregarSesion(s);
        assertEquals(7, s.getConvocados().size());
        assertEquals(1, f.club.getSesiones().size());
    }

    @Test
    @DisplayName("RF-EMP-03 Registrar asistencia y evaluación de una sesión")
    void registrarAsistencia() {
        SesionEntrenamiento s = new SesionEntrenamiento(f.carlos, f.sabado, LocalTime.of(16, 0), 60,
                TipoSesion.INDIVIDUAL, f.tenis1);
        s.convocar(f.juan);
        AsistenciaEntrenamiento a = f.carlos.registrarAsistencia(s, f.juan, EstadoAsistencia.ASISTIO);
        f.carlos.evaluar(a, 5, "Excelente");
        assertTrue(s.asistenciaCompleta());
        assertEquals(5, a.getNota());
    }

    @Test
    @DisplayName("RF-EMP-04 Registrar resultado y estadísticas de un partido")
    void registrarResultado() {
        PartidoInterno p = new PartidoInterno(f.leones, f.halcones, f.sabado, LocalTime.NOON);
        f.carlos.registrarResultado(p, 2, 1);
        var jugador = f.leones.getInscripciones().get(0).getSocio();
        assertNotNull(f.carlos.registrarEstadisticaValidada(p, new EstadisticaFutbol(jugador, 2, 0, 0, 1)));
        p.finalizar();
        f.club.agregarPartidoInterno(p);
        assertTrue(jugador.estaSuspendidoEn(null));
        assertFalse(p.esElegible(jugador));
    }

    @Test
    @DisplayName("RF-EMP-05 Gestionar la nómina de una competencia")
    void gestionarNomina() {
        TorneoCerros c = new TorneoCerros("Cerros", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        InscripcionCompetencia ins = c.inscribir(f.leones);
        for (InscripcionEquipo ie : f.leones.getInscripciones()) {
            f.carlos.gestionarNomina(ins, ie.getSocio());
        }
        assertTrue(ins.nominaCompleta());
    }

    @Test
    @DisplayName("RF-EMP-06 Sugerir nuevos artículos o snacks")
    void sugerir() {
        SugerenciaArticulo s = new SugerenciaArticulo(f.fabiola, "Barra de proteína", TipoSugerencia.SNACK);
        f.fabiola.sugerirArticulo(s);
        f.club.registrarSugerencia(s);
        assertEquals(EstadoSugerencia.PENDIENTE, f.club.getSugerencias().get(0).getEstado());
    }

    @Test
    @DisplayName("RF-EMP-07 Solicitar cambio de turno")
    void solicitarCambioTurno() {
        SolicitudCambioTurno s = new SolicitudCambioTurno(f.fabiola, f.fabiola.getTurnos().get(0), DiaSemana.MARTES,
                LocalTime.of(8, 0), LocalTime.of(12, 0));
        f.fabiola.solicitarTurno(s);
        f.club.registrarSolicitud(s);
        assertFalse(f.admin.resolverTurno(s, true));
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertThrows(IllegalArgumentException.class, () -> f.carlos.solicitarTurno(s));
    }

    @Test
    @DisplayName("RF-EMP-08 Comprar en la tienda con descuento de empleado")
    void comprarConDescuento() {
        CompraTienda c = new CompraTienda(f.carlos);
        c.agregarLinea(f.raqueta, 1);
        f.carlos.comprarTienda(c);
        assertEquals(100000 * 1.19 - 15000, c.getTotal(), 0.01);
    }

    @Test
    @DisplayName("RF-EMP-09 Compartir mi código de descuento")
    void compartirCodigo() {
        f.carlos.compartirCodigo(f.maria);
        CompraTienda c = new CompraTienda(f.maria);
        c.agregarLinea(f.raqueta, 1);
        c.aplicarCodigo(f.carlos.getCodigo());
        f.maria.comprarTienda(c);
        assertEquals(8000, c.getDescuento(), 0.01);
        assertThrows(IllegalStateException.class, () -> f.diego.compartirCodigo(f.maria));
    }
}
