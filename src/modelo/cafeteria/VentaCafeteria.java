package modelo.cafeteria;

import java.util.ArrayList;
import java.util.List;

import modelo.deportesindividuales.Instalacion;
import modelo.tienda.Compra;
import modelo.usuarios.Socio;

public class VentaCafeteria extends Compra {
    public static final double IMPUESTO_CONSUMO = 0.08;
    public static final double PROPINA_SUGERIDA = 0.10;

    private final List<LineaCafeteria> lineas = new ArrayList<>();
    private Socio comprador;
    private double propina;
    private boolean propinaModificada;

    private Instalacion instalacionDelSocio;

    public VentaCafeteria(Socio comprador) {
        this.comprador = comprador;
    }

    @Override
    public double calcularImpuesto() {
        return calcularSubtotal() * IMPUESTO_CONSUMO;
    }

    @Override
    protected double calcularSubtotal() {
        double s = 0;
        for (LineaCafeteria l : lineas) {
            s += l.subtotal();
        }
        return s;
    }

    @Override
    public String rubro() {
        return "CAFETERIA";
    }

    @Override
    protected double extras() {
        if (!propinaModificada) {
            propina = calcularSubtotal() * PROPINA_SUGERIDA;
        }
        return propina;
    }

    public LineaCafeteria agregarLinea(ProductoCafeteria p, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        LineaCafeteria linea = new LineaCafeteria(p, n);
        lineas.add(linea);
        return linea;
    }

    public void modificarPropina(double p) {
        if (p < 0) {
            throw new IllegalArgumentException("La propina no puede ser negativa");
        }
        this.propina = p;
        this.propinaModificada = true;
    }

    public boolean validarBebidaCaliente() {
        if (instalacionDelSocio == null || !instalacionDelSocio.restringeBebidaCaliente()) {
            return true;
        }
        for (LineaCafeteria l : lineas) {
            if (l.getProducto().esCaliente()) {
                return false;
            }
        }
        return true;
    }

    public boolean tieneBebidaCaliente() {
        for (LineaCafeteria l : lineas) {
            if (l.getProducto().esCaliente()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void confirmar() {
        if (lineas.isEmpty()) {
            throw new IllegalStateException("La venta no tiene productos");
        }
        if (!validarBebidaCaliente()) {
            throw new IllegalStateException("No se puede despachar bebida caliente en esta instalación");
        }
        super.confirmar();
    }

    public void setInstalacionDelSocio(Instalacion instalacionDelSocio) {
        this.instalacionDelSocio = instalacionDelSocio;
    }

    public List<LineaCafeteria> getLineas() {
        return lineas;
    }

    public Socio getComprador() {
        return comprador;
    }

    public double getPropina() {
        return propina;
    }

    public Instalacion getInstalacionDelSocio() {
        return instalacionDelSocio;
    }
}
