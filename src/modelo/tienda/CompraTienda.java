package modelo.tienda;

import java.util.ArrayList;
import java.util.List;

import modelo.usuarios.Usuario;

public class CompraTienda extends Compra {
    private static final long serialVersionUID = 1L;

    public static final double IVA = 0.19;

    private final List<LineaTienda> lineas = new ArrayList<>();
    private Usuario comprador;
    private CodigoDescuento codigoAplicado;

    public CompraTienda(Usuario comprador) {
        this.comprador = comprador;
    }

    @Override
    public double calcularImpuesto() {
        return calcularSubtotal() * IVA;
    }

    @Override
    protected double calcularSubtotal() {
        double s = 0;
        for (LineaTienda l : lineas) {
            s += l.subtotal();
        }
        return s;
    }

    @Override
    public String rubro() {
        return "TIENDA";
    }

    public LineaTienda agregarLinea(Articulo a, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        int yaPedido = 0;
        for (LineaTienda l : lineas) {
            if (l.getArticulo() == a) {
                yaPedido += l.getCantidad();
            }
        }
        if (!a.hayExistencias(n + yaPedido)) {
            throw new IllegalStateException("Sin existencias suficientes para " + a.getNombre()
                    + " (disponible: " + a.getCantidadDisponible() + ")");
        }
        LineaTienda linea = new LineaTienda(a, n);
        lineas.add(linea);
        return linea;
    }

    public void aplicarCodigo(CodigoDescuento c) {
        if (bonoAplicado != null) {
            throw new IllegalStateException("El código de descuento no es acumulable con el bono de torneo");
        }
        int pct = c.porcentajePara(comprador);
        if (pct == 0) {
            throw new IllegalStateException("El código " + c.getCodigo() + " no está habilitado para este usuario");
        }
        aplicarDescuento(calcularSubtotal() * pct / 100.0);
        this.codigoAplicado = c;
    }

    public void descontarInventario() {
        for (LineaTienda l : lineas) {
            if (!l.getArticulo().hayExistencias(l.getCantidad())) {
                throw new IllegalStateException("Sin existencias para " + l.getArticulo().getNombre());
            }
        }
        for (LineaTienda l : lineas) {
            l.getArticulo().descontar(l.getCantidad());
        }
    }

    @Override
    public void confirmar() {
        if (lineas.isEmpty()) {
            throw new IllegalStateException("La compra no tiene artículos");
        }
        if (codigoAplicado != null) {
            aplicarDescuento(calcularSubtotal() * codigoAplicado.porcentajePara(comprador) / 100.0);
        }
        descontarInventario();
        super.confirmar();
    }

    public List<LineaTienda> getLineas() {
        return lineas;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public CodigoDescuento getCodigoAplicado() {
        return codigoAplicado;
    }
}
