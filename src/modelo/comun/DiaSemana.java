package modelo.comun;

import java.time.LocalDate;

public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    public static DiaSemana de(LocalDate fecha) {
        return values()[fecha.getDayOfWeek().getValue() - 1];
    }
}
