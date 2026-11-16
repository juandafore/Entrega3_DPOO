package modelo.comun;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum Periodo {
    DIARIO,
    SEMANAL,
    MENSUAL;

    public LocalDate desde(LocalDate ref) {
        switch (this) {
            case SEMANAL:
                return ref.with(DayOfWeek.MONDAY);
            case MENSUAL:
                return ref.withDayOfMonth(1);
            default:
                return ref;
        }
    }

    public LocalDate hasta(LocalDate ref) {
        switch (this) {
            case SEMANAL:
                return ref.with(DayOfWeek.SUNDAY);
            case MENSUAL:
                return ref.withDayOfMonth(ref.lengthOfMonth());
            default:
                return ref;
        }
    }
}
