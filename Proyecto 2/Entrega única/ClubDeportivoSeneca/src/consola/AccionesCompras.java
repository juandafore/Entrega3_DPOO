package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.cafeteria.LineaCafeteria;
import modelo.cafeteria.ProductoCafeteria;
import modelo.cafeteria.VentaCafeteria;
import modelo.deportesindividuales.Instalacion;
import modelo.tienda.Articulo;
import modelo.tienda.BonoDescuento;
import modelo.tienda.CodigoDescuento;
import modelo.tienda.Compra;
import modelo.tienda.CompraTienda;
import modelo.tienda.LineaTienda;
import modelo.usuarios.Empleado;
import modelo.usuarios.Socio;
import modelo.usuarios.Usuario;

public class AccionesCompras {
    private final Club club;
    private final Entrada in;

    public AccionesCompras(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    public void comprarTienda(Usuario comprador) {
        CompraTienda c = new CompraTienda(comprador);
        llenarCompraTienda(c);
        if (c.getLineas().isEmpty()) {
            in.out().println("Compra cancelada (sin artículos).");
            return;
        }
        boolean usoBono = ofrecerBono(comprador, c);
        if (!usoBono) {
            if (comprador instanceof Empleado) {
                Empleado e = (Empleado) comprador;
                if (e.getCodigo() != null) {
                    c.aplicarCodigo(e.getCodigo());
                    in.out().println("Se aplica su código de empleado " + e.getCodigo().getCodigo() + " (15%).");
                }
            } else {
                ofrecerCodigoCompartido(comprador, c);
            }
        }
        int puntos = preguntarPuntos(comprador, c);
        imprimirResumenTienda(c, puntos);
        if (!in.siNo("¿Confirmar la compra?")) {
            in.out().println("Compra cancelada.");
            return;
        }
        if (comprador instanceof Socio) {
            Socio s = (Socio) comprador;
            if (puntos > 0) {
                s.redimirPuntosEn(c, puntos);
            }
            s.comprarTienda(c);
        } else if (comprador instanceof Empleado) {
            ((Empleado) comprador).comprarTienda(c);
        } else {
            throw new IllegalStateException("Este tipo de usuario no puede comprar en la tienda");
        }
        reciboTienda(c);
    }

    private void llenarCompraTienda(CompraTienda c) {
        while (true) {
            List<Articulo> disponibles = new ArrayList<>();
            for (Articulo a : club.getArticulos()) {
                if (a.getCantidadDisponible() > 0 && a.getPrecio() > 0) {
                    disponibles.add(a);
                }
            }
            Articulo a = in.seleccionar("Artículos disponibles (0 para terminar)", disponibles);
            if (a == null) {
                return;
            }
            int cant = in.entero("Cantidad: ", 1, a.getCantidadDisponible());
            try {
                c.agregarLinea(a, cant);
                in.out().println("  Agregado: " + cant + " x " + a.getNombre());
            } catch (RuntimeException ex) {
                in.out().println("  No se pudo agregar: " + ex.getMessage());
            }
        }
    }

    private void ofrecerCodigoCompartido(Usuario comprador, CompraTienda c) {
        List<CodigoDescuento> codigos = new ArrayList<>();
        for (Empleado e : club.getEmpleados()) {
            if (e.getCodigo() != null && e.getCodigo().porcentajePara(comprador) > 0) {
                codigos.add(e.getCodigo());
            }
        }
        if (codigos.isEmpty() || !in.siNo("Tiene códigos de descuento compartidos (8%). ¿Usar uno?")) {
            return;
        }
        CodigoDescuento cod = in.seleccionar("Códigos", codigos,
                k -> k.getCodigo() + " (de " + k.getDueno().getNombre() + ")");
        if (cod != null) {
            c.aplicarCodigo(cod);
        }
    }

    private void imprimirResumenTienda(CompraTienda c, int puntos) {
        c.calcularTotal();
        in.out().println(String.format("Resumen: subtotal $%,.0f | IVA 19%% $%,.0f | descuento $%,.0f | puntos -%d | "
                + "TOTAL $%,.0f", c.getSubtotal(), c.calcularImpuesto(), c.getDescuento(), puntos,
                Math.max(0, c.getTotal() - puntos)));
    }

    private void reciboTienda(CompraTienda c) {
        in.out().println("\n--- RECIBO TIENDA (" + c.getFecha().toLocalDate() + ") ---");
        for (LineaTienda l : c.getLineas()) {
            in.out().println("  " + l);
        }
        in.out().println(String.format("  Subtotal $%,.0f%n  IVA (19%%) $%,.0f%n  Descuento $%,.0f%s%n"
                + "  Puntos redimidos -%d%n  TOTAL $%,.0f%n  Puntos generados +%d", c.getSubtotal(),
                c.calcularImpuesto(), c.getDescuento(),
                c.getBonoAplicado() != null ? " (bono " + c.getBonoAplicado().getCodigo() + ")"
                        : c.getCodigoAplicado() != null ? " (código " + c.getCodigoAplicado().getCodigo() + ")" : "",
                c.getPuntosRedimidos(), c.getTotal(), c.getPuntosGenerados()));
    }

    public void comprarCafeteria(Socio s) {
        VentaCafeteria v = new VentaCafeteria(s);
        if (in.siNo("¿Se encuentra en este momento dentro de una instalación deportiva?")) {
            Instalacion i = in.seleccionar("Instalaciones", club.getInstalaciones());
            v.setInstalacionDelSocio(i);
        }
        while (true) {
            ProductoCafeteria p = in.seleccionar("Productos (0 para terminar)", club.getProductosCafeteria(),
                    x -> x.getNombre() + String.format(" $%,.0f", x.getPrecio()) + (x.esCaliente() ? " [caliente]" : ""));
            if (p == null) {
                break;
            }
            String[] al = p.informarAlergenos();
            if (al.length > 0) {
                in.out().println("  ATENCIÓN - alérgenos de " + p.getNombre() + ": " + String.join(", ", al));
                if (!in.siNo("  ¿Desea agregarlo de todas formas?")) {
                    continue;
                }
            }
            if (p.esCaliente() && v.getInstalacionDelSocio() != null
                    && v.getInstalacionDelSocio().restringeBebidaCaliente()) {
                in.out().println("  No se puede despachar una bebida caliente dentro de "
                        + v.getInstalacionDelSocio().getNombre() + ".");
                continue;
            }
            int cant = in.entero("Cantidad: ", 1, 50);
            v.agregarLinea(p, cant);
        }
        if (v.getLineas().isEmpty()) {
            in.out().println("Compra cancelada (sin productos).");
            return;
        }
        v.calcularTotal();
        in.out().println(String.format("Propina sugerida (10%%): $%,.0f", v.getPropina()));
        if (in.siNo("¿Desea modificar la propina?")) {
            v.modificarPropina(in.decimal("Nueva propina: $", 0, 1_000_000));
        }
        ofrecerBono(s, v);
        int puntos = preguntarPuntos(s, v);
        v.calcularTotal();
        in.out().println(String.format("Resumen: subtotal $%,.0f | consumo 8%% $%,.0f | propina $%,.0f | descuento $%,.0f"
                + " | puntos -%d | TOTAL $%,.0f", v.getSubtotal(), v.calcularImpuesto(), v.getPropina(),
                v.getDescuento(), puntos, Math.max(0, v.getTotal() - puntos)));
        if (!in.siNo("¿Confirmar la compra?")) {
            in.out().println("Compra cancelada.");
            return;
        }
        if (puntos > 0) {
            s.redimirPuntosEn(v, puntos);
        }
        s.comprarCafeteria(v);
        in.out().println("\n--- RECIBO CAFETERÍA (" + v.getFecha().toLocalDate() + ") ---");
        for (LineaCafeteria l : v.getLineas()) {
            in.out().println("  " + l);
        }
        in.out().println(String.format("  Subtotal $%,.0f%n  Impuesto al consumo (8%%) $%,.0f%n  Propina $%,.0f%n"
                + "  Descuento $%,.0f%n  Puntos redimidos -%d%n  TOTAL $%,.0f%n  Puntos generados +%d",
                v.getSubtotal(), v.calcularImpuesto(), v.getPropina(), v.getDescuento(), v.getPuntosRedimidos(),
                v.getTotal(), v.getPuntosGenerados()));
        if (s.tieneBebidaCaliente()) {
            in.out().println("  Recuerde: con una bebida caliente no puede ingresar a canchas de pádel ni mesas techadas.");
        }
    }

    private boolean ofrecerBono(Usuario u, Compra c) {
        List<BonoDescuento> bonos = u.bonosDisponibles();
        if (bonos.isEmpty() || !in.siNo("Tiene bonos de torneo disponibles (no acumulables con otros descuentos). "
                + "¿Usar uno?")) {
            return false;
        }
        BonoDescuento b = in.seleccionar("Bonos", bonos);
        if (b == null) {
            return false;
        }
        c.aplicarBono(b);
        return true;
    }

    private int preguntarPuntos(Usuario u, Compra c) {
        if (!(u instanceof Socio) || c.getBonoAplicado() != null) {
            return 0;
        }
        Socio s = (Socio) u;
        if (s.getPuntosFidelidad() <= 0) {
            return 0;
        }
        int max = (int) Math.min(s.getPuntosFidelidad(), Math.floor(c.totalAntesDePuntos()));
        if (max <= 0 || !in.siNo("Tiene " + s.getPuntosFidelidad() + " puntos (1 punto = $1). ¿Desea redimir?")) {
            return 0;
        }
        return in.entero("Puntos a redimir (máx " + max + "): ", 0, max);
    }
}
