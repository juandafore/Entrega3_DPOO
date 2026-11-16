package modelo.usuarios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.admin.SolicitudCambioTurno;
import modelo.admin.SugerenciaArticulo;
import modelo.admin.TurnoEmpleado;
import modelo.comun.DiaSemana;
import modelo.tienda.CodigoDescuento;
import modelo.tienda.CompraTienda;

public abstract class Empleado extends Usuario {
    private static final long serialVersionUID = 1L;

    private CodigoDescuento codigo;
    private final List<TurnoEmpleado> turnos = new ArrayList<>();
    private final List<SolicitudCambioTurno> solicitudes = new ArrayList<>();
    private final List<SugerenciaArticulo> sugerencias = new ArrayList<>();

    protected Empleado(String login, String password, String nombre) {
        super(login, password, nombre);
    }

    public void solicitarTurno(SolicitudCambioTurno s) {
        if (s.getSolicitante() != this) {
            throw new IllegalArgumentException("Solo puede solicitar cambios de sus propios turnos");
        }
        if (!turnos.contains(s.getTurnoOriginal())) {
            throw new IllegalArgumentException("El turno indicado no pertenece al empleado");
        }
        solicitudes.add(s);
    }

    public void sugerirArticulo(SugerenciaArticulo s) {
        sugerencias.add(s);
    }

    public void compartirCodigo(Socio s) {
        if (codigo == null) {
            throw new IllegalStateException("El empleado no tiene código de descuento");
        }
        codigo.compartirCon(s);
    }

    public void comprarTienda(CompraTienda c) {
        if (codigo != null && c.getBonoAplicado() == null && c.getCodigoAplicado() == null) {
            c.aplicarCodigo(codigo);
        }
        c.confirmar();
        comprar(c);
    }

    public void agregarTurno(TurnoEmpleado t) {
        if (!turnos.contains(t)) {
            turnos.add(t);
        }
    }

    public boolean estaDeTurno(LocalDate fecha) {
        DiaSemana d = DiaSemana.de(fecha);
        for (TurnoEmpleado t : turnos) {
            if (t.getDia() == d) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean esEmpleado() {
        return true;
    }

    public CodigoDescuento getCodigo() {
        return codigo;
    }

    public void setCodigo(CodigoDescuento codigo) {
        this.codigo = codigo;
    }

    public List<TurnoEmpleado> getTurnos() {
        return turnos;
    }

    public List<SolicitudCambioTurno> getSolicitudes() {
        return solicitudes;
    }

    public List<SugerenciaArticulo> getSugerencias() {
        return sugerencias;
    }
}
