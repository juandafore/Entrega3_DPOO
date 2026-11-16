package modelo.torneos;

import java.util.Collections;
import java.util.List;

import modelo.usuarios.Usuario;

public class ParticipanteIndividual extends ParticipanteTorneo {
    private static final long serialVersionUID = 1L;

    private Usuario jugador;

    public ParticipanteIndividual(Usuario jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador inválido");
        }
        this.jugador = jugador;
    }

    public Usuario getJugador() {
        return jugador;
    }

    @Override
    public String getNombre() {
        return jugador.getNombre();
    }

    @Override
    public List<Usuario> integrantes() {
        return Collections.singletonList(jugador);
    }
}
