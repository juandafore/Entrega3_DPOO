package modelo.torneos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.tienda.BonoDescuento;
import modelo.usuarios.Usuario;

public class TorneoRecreativo extends Torneo {
    private static final long serialVersionUID = 1L;

    private int porcentajeBono;
    private final List<BonoDescuento> bonosEntregados = new ArrayList<>();

    public TorneoRecreativo(int id, String nombre, Deporte deporte, Categoria categoria, FormatoTorneo formato,
            int cupos, LocalDate fecha, int porcentajeBono) {
        super(id, nombre, deporte, categoria, formato, cupos, fecha);
        setPorcentaje(porcentajeBono);
    }

    public TorneoRecreativo(int id, String nombre, Disciplina disciplina, Nivel nivel, boolean dobles,
            FormatoTorneo formato, int cupos, LocalDate fecha, int porcentajeBono) {
        super(id, nombre, disciplina, nivel, dobles, formato, cupos, fecha);
        setPorcentaje(porcentajeBono);
    }

    private void setPorcentaje(int p) {
        if (p <= 0 || p > 100) {
            throw new IllegalArgumentException("El porcentaje del bono debe estar entre 1 y 100");
        }
        this.porcentajeBono = p;
    }

    @Override
    public double tarifaPara(ParticipanteTorneo p) {
        return 0;
    }

    @Override
    protected void otorgarPremio() {
        for (Usuario u : campeon.integrantes()) {
            BonoDescuento b = new BonoDescuento("BONO-T" + id + "-" + u.getLogin().toUpperCase(), porcentajeBono,
                    "Torneo recreativo " + nombre);
            u.agregarBono(b);
            bonosEntregados.add(b);
            registrarEvento("Bono " + b.getCodigo() + " (" + porcentajeBono + "%) entregado a " + u.getNombre());
        }
    }

    @Override
    public String tipoTorneo() {
        return "Recreativo";
    }

    @Override
    public String descripcionPremio() {
        return "Bono de " + porcentajeBono + "% redimible en tienda o cafetería (inscripción gratuita)";
    }

    public int getPorcentajeBono() {
        return porcentajeBono;
    }

    public List<BonoDescuento> getBonosEntregados() {
        return bonosEntregados;
    }
}
