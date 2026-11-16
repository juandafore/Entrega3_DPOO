package modelo.usuarios;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Autenticable;
import modelo.tienda.BonoDescuento;
import modelo.tienda.Compra;

public abstract class Usuario implements Autenticable, Serializable {
    private static final long serialVersionUID = 1L;

    protected String login;
    protected String password;
    protected String nombre;
    protected boolean activo = true;
    protected final List<Compra> compras = new ArrayList<>();

    protected final List<BonoDescuento> bonos = new ArrayList<>();

    protected Usuario(String login, String password, String nombre) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Todo usuario debe tener login y password");
        }
        this.login = login;
        this.password = password;
        this.nombre = nombre;
    }

    @Override
    public boolean autenticar(String login, String password) {
        return activo && this.login.equals(login) && this.password.equals(password);
    }

    @Override
    public void cambiarPassword(String nueva) {
        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede ser vacía");
        }
        this.password = nueva;
    }

    public void comprar(Compra c) {
        compras.add(c);
    }

    public void agregarBono(BonoDescuento b) {
        bonos.add(b);
    }

    public List<BonoDescuento> getBonos() {
        return bonos;
    }

    public List<BonoDescuento> bonosDisponibles() {
        List<BonoDescuento> r = new ArrayList<>();
        for (BonoDescuento b : bonos) {
            if (!b.isUsado()) {
                r.add(b);
            }
        }
        return r;
    }

    public boolean esEmpleado() {
        return false;
    }

    public String getLogin() {
        return login;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Compra> getCompras() {
        return compras;
    }

    public abstract String tipo();

    @Override
    public String toString() {
        return tipo() + " " + nombre + " (" + login + ")";
    }
}
