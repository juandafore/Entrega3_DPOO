package modelo.torneos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatoEliminacion implements EstrategiaFormato {
    private static final long serialVersionUID = 1L;

    @Override
    public List<EncuentroTorneo> generarIniciales(List<ParticipanteTorneo> participantes) {
        return emparejar(new ArrayList<>(participantes), 1);
    }

    private List<EncuentroTorneo> emparejar(List<ParticipanteTorneo> ps, int ronda) {
        List<EncuentroTorneo> r = new ArrayList<>();
        if (ps.size() % 2 == 1) {
            r.add(new EncuentroTorneo(ronda, ps.remove(0), null));
        }
        int n = ps.size();
        for (int i = 0; i < n / 2; i++) {
            r.add(new EncuentroTorneo(ronda, ps.get(i), ps.get(n - 1 - i)));
        }
        return r;
    }

    private int rondaActual(List<EncuentroTorneo> encuentros) {
        int max = 0;
        for (EncuentroTorneo e : encuentros) {
            max = Math.max(max, e.getRonda());
        }
        return max;
    }

    private List<ParticipanteTorneo> ganadoresDeRonda(List<EncuentroTorneo> encuentros, int ronda) {
        List<ParticipanteTorneo> g = new ArrayList<>();
        for (EncuentroTorneo e : encuentros) {
            if (e.getRonda() == ronda) {
                if (!e.isJugado()) {
                    return null;
                }
                g.add(e.ganador());
            }
        }
        return g;
    }

    @Override
    public List<EncuentroTorneo> siguientes(List<EncuentroTorneo> encuentros) {
        int ronda = rondaActual(encuentros);
        List<ParticipanteTorneo> ganadores = ganadoresDeRonda(encuentros, ronda);
        if (ganadores == null || ganadores.size() <= 1) {
            return Collections.emptyList();
        }
        return emparejar(ganadores, ronda + 1);
    }

    @Override
    public boolean terminado(List<EncuentroTorneo> encuentros) {
        if (encuentros.isEmpty()) {
            return false;
        }
        List<ParticipanteTorneo> g = ganadoresDeRonda(encuentros, rondaActual(encuentros));
        return g != null && g.size() == 1;
    }

    @Override
    public List<FilaTorneo> posiciones(List<EncuentroTorneo> encuentros, List<ParticipanteTorneo> participantes) {
        List<FilaTorneo> filas = new ArrayList<>();
        for (ParticipanteTorneo p : participantes) {
            filas.add(new FilaTorneo(p));
        }
        for (EncuentroTorneo e : encuentros) {
            FormatoLiga.fila(filas, e.getParticipanteA()).alcanzarRonda(e.getRonda());
            if (e.esDescanso()) {
                continue;
            }
            FormatoLiga.fila(filas, e.getParticipanteB()).alcanzarRonda(e.getRonda());
            if (e.isJugado()) {
                FormatoLiga.fila(filas, e.getParticipanteA()).registrar(e.getMarcadorA(), e.getMarcadorB(), e.getRonda());
                FormatoLiga.fila(filas, e.getParticipanteB()).registrar(e.getMarcadorB(), e.getMarcadorA(), e.getRonda());
            }
        }
        final ParticipanteTorneo campeon = terminado(encuentros)
                ? ganadoresDeRonda(encuentros, rondaActual(encuentros)).get(0) : null;
        filas.sort((x, y) -> {
            if (x.getParticipante() == campeon) {
                return -1;
            }
            if (y.getParticipante() == campeon) {
                return 1;
            }
            if (x.getRondaAlcanzada() != y.getRondaAlcanzada()) {
                return y.getRondaAlcanzada() - x.getRondaAlcanzada();
            }
            if (x.getGanados() != y.getGanados()) {
                return y.getGanados() - x.getGanados();
            }
            return y.diferencia() - x.diferencia();
        });
        return filas;
    }
}
