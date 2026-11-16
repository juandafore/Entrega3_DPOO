package pruebas.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.admin.SolicitudCambioTurno;
import modelo.admin.TurnoEmpleado;
import modelo.cafeteria.VentaCafeteria;
import modelo.comun.DiaSemana;
import modelo.comun.EstadoReserva;
import modelo.comun.EstadoSolicitud;
import modelo.comun.Modalidad;
import modelo.deportesindividuales.Reserva;
import modelo.usuarios.Socio;
import pruebas.Fixtures;

class ClubTest {
    private Fixtures f;
    private final LocalTime diez = LocalTime.of(10, 0);
    private final LocalTime once = LocalTime.of(11, 0);

    @BeforeEach
    void setUp() {
        f = new Fixtures();
    }

    private Reserva reserva(Socio s, modelo.deportesindividuales.Instalacion i, int jugadores, Modalidad m) {
        return new Reserva(s, i, f.sabado, diez, once, jugadores, m);
    }

    @Test
    void autenticacionConLoginYPassword() {
        assertEquals(f.juan, f.club.autenticar("juan", "juan"));
        assertNull(f.club.autenticar("juan", "mala"));
        assertEquals(f.estudiante, f.club.autenticar("sofia", "sofia"));
        f.club.eliminarSocio(f.juan);
        assertNull(f.club.autenticar("juan", "juan"));
    }

    @Test
    void loginDuplicadoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> f.club.registrarSocio(new Socio("JUAN", "x", "Otro", 20)));
        assertThrows(IllegalArgumentException.class, () -> new Socio("", "x", "Sin login", 20));
    }

    @Test
    void reservaValidaSeConfirma() {
        Reserva r = reserva(f.juan, f.tenis1, 2, Modalidad.SENCILLOS);
        assertTrue(f.club.reservar(f.juan, f.tenis1, r));
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals(1, f.juan.getReservas().size());
    }

    @Test
    void capacidadYModalidadSeValidan() {
        Reserva padel2 = reserva(f.juan, f.padel, 2, Modalidad.SENCILLOS);
        assertFalse(f.club.reservar(f.juan, f.padel, padel2));
        Reserva incoherente = reserva(f.juan, f.tenis1, 4, Modalidad.SENCILLOS);
        assertFalse(f.club.reservar(f.juan, f.tenis1, incoherente));
        assertTrue(incoherente.getMotivoRechazo().contains("modalidad"));
    }

    @Test
    void maximoDeInstalacionesSimultaneasPorBloque() {
        assertTrue(f.club.reservar(f.juan, f.tenis1, reserva(f.juan, f.tenis1, 2, Modalidad.SENCILLOS)));
        Reserva otra = reserva(f.maria, f.tenis2, 2, Modalidad.SENCILLOS);
        assertFalse(f.club.reservar(f.maria, f.tenis2, otra));
        assertTrue(otra.getMotivoRechazo().contains("simultáneas"));
        Reserva otraHora = new Reserva(f.maria, f.tenis2, f.sabado, LocalTime.of(12, 0), LocalTime.of(13, 0), 2,
                Modalidad.SENCILLOS);
        assertTrue(f.club.reservar(f.maria, f.tenis2, otraHora));
    }

    @Test
    void instalacionOcupadaEnElMismoBloque() {
        f.club.getConfiguraciones().clear();
        assertTrue(f.club.reservar(f.juan, f.tenis1, reserva(f.juan, f.tenis1, 2, Modalidad.SENCILLOS)));
        Reserva choque = new Reserva(f.maria, f.tenis1, f.sabado, LocalTime.of(10, 30), LocalTime.of(11, 30), 2,
                Modalidad.SENCILLOS);
        assertFalse(f.club.reservar(f.maria, f.tenis1, choque));
    }

    @Test
    void socioConBebidaCalienteNoPuedeReservarPadelTechado() {
        VentaCafeteria v = new VentaCafeteria(f.juan);
        v.agregarLinea(f.cafe, 1);
        f.juan.comprarCafeteria(v);
        Reserva r = reserva(f.juan, f.padel, 4, Modalidad.DOBLES);
        assertFalse(f.club.reservar(f.juan, f.padel, r));
        assertTrue(r.getMotivoRechazo().contains("bebida caliente"));
    }

    @Test
    void cambioDeTurnoSeApruebaSoloSiSeMantienenLosMinimos() {
        modelo.usuarios.Entrenador elena = new modelo.usuarios.Entrenador("elena", "x", "Elena");
        f.club.agregarEmpleado(elena);
        f.club.registrarTurno(new TurnoEmpleado(elena, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        TurnoEmpleado tCarlos = f.carlos.getTurnos().get(0);
        SolicitudCambioTurno ok = new SolicitudCambioTurno(f.carlos, tCarlos, DiaSemana.MARTES, diez, once);
        assertTrue(f.club.resolverCambioTurno(ok, true));
        assertEquals(DiaSemana.MARTES, tCarlos.getDia());

        TurnoEmpleado tFabiola = f.fabiola.getTurnos().get(0);
        SolicitudCambioTurno mal = new SolicitudCambioTurno(f.fabiola, tFabiola, DiaSemana.MARTES, diez, once);
        assertFalse(f.club.resolverCambioTurno(mal, true));
        assertEquals(EstadoSolicitud.RECHAZADA, mal.getEstado());
        assertEquals(DiaSemana.LUNES, tFabiola.getDia());
        assertThrows(IllegalStateException.class, () -> f.club.resolverCambioTurno(mal, true));
    }

    @Test
    void bajaDeSocioLoRetiraDeSusEquipos() {
        Socio s = f.leones.getInscripciones().get(0).getSocio();
        f.club.eliminarSocio(s);
        assertFalse(f.leones.tieneJugador(s));
        assertFalse(s.isActivo());
    }

    @Test
    void empleadoDeTurnoSegunElDia() {
        assertTrue(f.carlos.estaDeTurno(f.lunes));
        assertFalse(f.carlos.estaDeTurno(f.sabado));
    }
}
