package modelo.tienda;

import java.io.Serializable;
import java.time.LocalDateTime;

import modelo.comun.Gravable;

public abstract class Compra implements Gravable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final double TASA_PUNTOS = 0.02;

    protected LocalDateTime fecha = LocalDateTime.now();
    protected double subtotal;
    protected double descuento;
    protected int puntosRedimidos;
    protected int puntosGenerados;
    protected double total;
    protected BonoDescuento bonoAplicado;
    protected boolean confirmada;

    @Override
    public abstract double calcularImpuesto();

    protected abstract double calcularSubtotal();

    public abstract String rubro();

    protected double extras() {
        return 0.0;
    }

    public double calcularTotal() {
        subtotal = calcularSubtotal();
        if (bonoAplicado != null) {
            descuento = bonoAplicado.descuentoSobre(subtotal);
        }
        total = subtotal + calcularImpuesto() + extras() - descuento - puntosRedimidos;
        if (total < 0) {
            total = 0;
        }
        return total;
    }

    public void aplicarDescuento(double d) {
        if (d < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }
        if (bonoAplicado != null && d > 0) {
            throw new IllegalStateException("El bono de torneo no es acumulable con otros descuentos");
        }
        this.descuento = d;
    }

    public void aplicarBono(BonoDescuento b) {
        if (b == null) {
            throw new IllegalArgumentException("Bono inválido");
        }
        if (b.isUsado()) {
            throw new IllegalStateException("El bono " + b.getCodigo() + " ya fue utilizado");
        }
        if (descuento > 0 || puntosRedimidos > 0 || bonoAplicado != null) {
            throw new IllegalStateException("El bono de torneo no es acumulable con otros descuentos");
        }
        bonoAplicado = b;
        calcularTotal();
    }

    public int generarPuntos() {
        puntosGenerados = (int) Math.floor(calcularTotal() * TASA_PUNTOS);
        return puntosGenerados;
    }

    public void redimirPuntos(int p) {
        if (p < 0) {
            throw new IllegalArgumentException("Los puntos a redimir no pueden ser negativos");
        }
        if (bonoAplicado != null && p > 0) {
            throw new IllegalStateException("El bono de torneo no es acumulable con otros descuentos");
        }
        this.puntosRedimidos = p;
    }

    public double totalAntesDePuntos() {
        int antes = puntosRedimidos;
        puntosRedimidos = 0;
        double t = calcularTotal();
        puntosRedimidos = antes;
        calcularTotal();
        return t;
    }

    public void confirmar() {
        if (confirmada) {
            throw new IllegalStateException("La compra ya fue confirmada");
        }
        calcularTotal();
        if (bonoAplicado != null) {
            bonoAplicado.marcarUsado();
        }
        confirmada = true;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public int getPuntosRedimidos() {
        return puntosRedimidos;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public double getTotal() {
        return total;
    }

    public BonoDescuento getBonoAplicado() {
        return bonoAplicado;
    }

    public boolean isConfirmada() {
        return confirmada;
    }

    @Override
    public String toString() {
        return String.format("%s %s - total $%,.0f", rubro(), fecha.toLocalDate(), total);
    }
}
