package modelo.torneos;

import java.util.ArrayList;
import java.util.List;

import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.usuarios.Usuario;

public class ParticipanteEquipo extends ParticipanteTorneo {
    private static final long serialVersionUID = 1L;

    private Equipo equipo;

    public ParticipanteEquipo(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("Equipo inválido");
        }
        this.equipo = equipo;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    @Override
    public String getNombre() {
        return equipo.getNombre();
    }

    @Override
    public List<Usuario> integrantes() {
        List<Usuario> r = new ArrayList<>();
        for (InscripcionEquipo i : equipo.getInscripciones()) {
            r.add(i.getSocio());
        }
        return r;
    }
}
