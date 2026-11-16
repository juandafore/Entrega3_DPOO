package modelo.torneos;

import java.io.Serializable;
import java.util.List;

public interface EstrategiaFormato extends Serializable {
    List<EncuentroTorneo> generarIniciales(List<ParticipanteTorneo> participantes);

    List<EncuentroTorneo> siguientes(List<EncuentroTorneo> encuentros);

    boolean terminado(List<EncuentroTorneo> encuentros);

    List<FilaTorneo> posiciones(List<EncuentroTorneo> encuentros, List<ParticipanteTorneo> participantes);
}
