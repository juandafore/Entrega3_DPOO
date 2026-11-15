package modelo.torneos;

import java.io.Serializable;
import java.util.List;

import modelo.usuarios.Usuario;

public abstract class ParticipanteTorneo implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getNombre();

    public abstract List<Usuario> integrantes();

    public boolean contiene(Usuario u) {
        return integrantes().contains(u);
    }

    public boolean comparteIntegrantesCon(ParticipanteTorneo otro) {
        for (Usuario u : integrantes()) {
            if (otro.contiene(u)) {
                return true;
            }
        }
        return false;
    }

    public int cantidadNoEmpleados() {
        int n = 0;
        for (Usuario u : integrantes()) {
            if (!u.esEmpleado()) {
                n++;
            }
        }
        return n;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
