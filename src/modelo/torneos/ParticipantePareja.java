package modelo.torneos;

import java.util.Arrays;
import java.util.List;

import modelo.usuarios.Usuario;

public class ParticipantePareja extends ParticipanteTorneo {
    private static final long serialVersionUID = 1L;

    private Usuario jugador1;
    private Usuario jugador2;

    public ParticipantePareja(Usuario jugador1, Usuario jugador2) {
        if (jugador1 == null || jugador2 == null) {
            throw new IllegalArgumentException("Una pareja requiere dos jugadores");
        }
        if (jugador1 == jugador2) {
            throw new IllegalArgumentException("Una pareja debe estar conformada por dos jugadores distintos");
        }
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    public Usuario getJugador1() {
        return jugador1;
    }

    public Usuario getJugador2() {
        return jugador2;
    }

    @Override
    public String getNombre() {
        return jugador1.getNombre() + " / " + jugador2.getNombre();
    }

    @Override
    public List<Usuario> integrantes() {
        return Arrays.asList(jugador1, jugador2);
    }
}
