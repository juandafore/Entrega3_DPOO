package modelo.deportesconjunto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PartidoInterno extends Partido {
    public PartidoInterno(Equipo local, Equipo visitante, LocalDate fecha, LocalTime hora) {
        super(local, visitante, fecha, hora);
        validarRivales(local, visitante);
    }

    private static void validarRivales(Equipo local, Equipo visitante) {
        if (local == visitante) {
            throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo");
        }
        if (local.getDeporte() != visitante.getDeporte() || local.getCategoria() != visitante.getCategoria()) {
            throw new IllegalArgumentException(
                    "Un partido interno requiere equipos del mismo deporte y categoría");
        }
    }

    @Override
    public modelo.competencia.Competencia ambito() {
        return null;
    }

    public void programar(Equipo l, Equipo v) {
        validarRivales(l, v);
        this.local = l;
        this.visitante = v;
    }
}
