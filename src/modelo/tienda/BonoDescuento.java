package modelo.tienda;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BonoDescuento implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private int porcentaje;
    private String origen;
    private boolean usado;
    private LocalDateTime fechaUso;

    public BonoDescuento(String codigo, int porcentaje, String origen) {
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje del bono debe estar entre 1 y 100");
        }
        this.codigo = codigo;
        this.porcentaje = porcentaje;
        this.origen = origen;
    }

    public double descuentoSobre(double subtotal) {
        return subtotal * porcentaje / 100.0;
    }

    public void marcarUsado() {
        if (usado) {
            throw new IllegalStateException("El bono " + codigo + " ya fue utilizado");
        }
        usado = true;
        fechaUso = LocalDateTime.now();
    }

    public String getCodigo() {
        return codigo;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getOrigen() {
        return origen;
    }

    public boolean isUsado() {
        return usado;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    @Override
    public String toString() {
        return codigo + " (" + porcentaje + "% - " + origen + (usado ? ", USADO" : ", disponible") + ")";
    }
}
