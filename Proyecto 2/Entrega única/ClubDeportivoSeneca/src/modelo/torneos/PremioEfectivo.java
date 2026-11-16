package modelo.torneos;

import java.io.Serializable;

import modelo.usuarios.Usuario;

public class PremioEfectivo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Usuario beneficiario;
    private double monto;

    public PremioEfectivo(Usuario beneficiario, double monto) {
        this.beneficiario = beneficiario;
        this.monto = monto;
    }

    public Usuario getBeneficiario() {
        return beneficiario;
    }

    public double getMonto() {
        return monto;
    }

    @Override
    public String toString() {
        return String.format("%s: $%,.0f", beneficiario.getNombre(), monto);
    }
}
