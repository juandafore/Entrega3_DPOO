package modelo.torneos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatoLiga implements EstrategiaFormato {
    private static final long serialVersionUID = 1L;

    @Override
    public List<EncuentroTorneo> generarIniciales(List<ParticipanteTorneo> participantes) {
        List<ParticipanteTorneo> lista = new ArrayList<>(participantes);
        if (lista.size() % 2 == 1) {
            lista.add(null);
        }
        int n = lista.size();
        List<EncuentroTorneo> r = new ArrayList<>();
        for (int ronda = 1; ronda < n; ronda++) {
            for (int i = 0; i < n / 2; i++) {
                ParticipanteTorneo a = lista.get(i);
                ParticipanteTorneo b = lista.get(n - 1 - i);
                if (a != null && b != null) {
                    r.add(new EncuentroTorneo(ronda, a, b));
                }
            }

            ParticipanteTorneo ultimo = lista.remove(n - 1);
            lista.add(1, ultimo);
        }
        return r;
    }

    @Override
    public List<EncuentroTorneo> siguientes(List<EncuentroTorneo> encuentros) {
        return Collections.emptyList();
    }

    @Override
    public boolean terminado(List<EncuentroTorneo> encuentros) {
        for (EncuentroTorneo e : encuentros) {
            if (!e.isJugado()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<FilaTorneo> posiciones(List<EncuentroTorneo> encuentros, List<ParticipanteTorneo> participantes) {
        List<FilaTorneo> filas = new ArrayList<>();
        for (ParticipanteTorneo p : participantes) {
            filas.add(new FilaTorneo(p));
        }
        for (EncuentroTorneo e : encuentros) {
            if (e.isJugado() && !e.esDescanso()) {
                fila(filas, e.getParticipanteA()).registrar(e.getMarcadorA(), e.getMarcadorB(), e.getRonda());
                fila(filas, e.getParticipanteB()).registrar(e.getMarcadorB(), e.getMarcadorA(), e.getRonda());
            }
        }
        filas.sort((x, y) -> {
            if (x.puntos() != y.puntos()) {
                return y.puntos() - x.puntos();
            }
            if (x.diferencia() != y.diferencia()) {
                return y.diferencia() - x.diferencia();
            }
            return y.getAFavor() - x.getAFavor();
        });
        return filas;
    }

    static FilaTorneo fila(List<FilaTorneo> filas, ParticipanteTorneo p) {
        for (FilaTorneo f : filas) {
            if (f.getParticipante() == p) {
                return f;
            }
        }
        FilaTorneo f = new FilaTorneo(p);
        filas.add(f);
        return f;
    }
}
