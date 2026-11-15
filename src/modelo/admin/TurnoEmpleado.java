package modelo.admin;

import java.io.Serializable;
import java.time.LocalTime;

import modelo.comun.DiaSemana;
import modelo.usuarios.Empleado;

public class TurnoEmpleado implements Serializable {
    private DiaSemana dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Empleado empleado;

    public TurnoEmpleado(Empleado empleado, DiaSemana dia, LocalTime horaInicio, LocalTime horaFin) {
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio del turno debe ser anterior a la de fin");
        }
        this.empleado = empleado;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public boolean solapaCon(TurnoEmpleado t) {
        if (dia != t.dia) {
            return false;
        }
        return horaInicio.isBefore(t.horaFin) && t.horaInicio.isBefore(horaFin);
    }

    public int duracionMinutos() {
        int ini = horaInicio.getHour() * 60 + horaInicio.getMinute();
        int fin = horaFin.getHour() * 60 + horaFin.getMinute();
        return fin - ini;
    }

    public boolean cubre(DiaSemana d, LocalTime apertura, LocalTime cierre) {
        if (dia != d) {
            return false;
        }
        return horaInicio.isBefore(cierre) && apertura.isBefore(horaFin);
    }

    @Override
    public String toString() {
        return dia + " " + horaInicio + "-" + horaFin + " (" + empleado.getNombre() + ")";
    }

    public DiaSemana getDia() {
        return dia;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setDia(DiaSemana dia) {
        this.dia = dia;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}
