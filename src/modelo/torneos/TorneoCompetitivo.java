package modelo.torneos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.usuarios.Usuario;

public class TorneoCompetitivo extends Torneo {
    private static final long serialVersionUID = 1L;

    private double tarifaInscripcion;
    private int porcentajePremio;
    private final List<PremioEfectivo> premios = new ArrayList<>();

    public TorneoCompetitivo(int id, String nombre, Deporte deporte, Categoria categoria, FormatoTorneo formato,
            int cupos, LocalDate fecha, double tarifaInscripcion, int porcentajePremio) {
        super(id, nombre, deporte, categoria, formato, cupos, fecha);
        configurar(tarifaInscripcion, porcentajePremio);
    }

    public TorneoCompetitivo(int id, String nombre, Disciplina disciplina, Nivel nivel, boolean dobles,
            FormatoTorneo formato, int cupos, LocalDate fecha, double tarifaInscripcion, int porcentajePremio) {
        super(id, nombre, disciplina, nivel, dobles, formato, cupos, fecha);
        configurar(tarifaInscripcion, porcentajePremio);
    }

    private void configurar(double tarifa, int porcentaje) {
        if (tarifa <= 0) {
            throw new IllegalArgumentException("Un torneo competitivo exige una tarifa de inscripción positiva");
        }
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje del premio debe estar entre 1 y 100");
        }
        this.tarifaInscripcion = tarifa;
        this.porcentajePremio = porcentaje;
    }

    @Override
    public double tarifaPara(ParticipanteTorneo p) {
        int total = p.integrantes().size();
        if (total == 0) {
            return tarifaInscripcion;
        }
        return tarifaInscripcion * p.cantidadNoEmpleados() / total;
    }

    public double totalRecaudado() {
        double t = 0;
        for (InscripcionTorneo i : inscripciones) {
            t += i.getTarifaPagada();
        }
        return t;
    }

    public double premioTotal() {
        return totalRecaudado() * porcentajePremio / 100.0;
    }

    @Override
    protected void otorgarPremio() {
        List<Usuario> integrantes = campeon.integrantes();
        double cuota = integrantes.isEmpty() ? 0 : premioTotal() / integrantes.size();
        for (Usuario u : integrantes) {
            if (u.esEmpleado()) {
                registrarEvento(u.getNombre() + " es empleado: no recibe premio en efectivo");
            } else {
                PremioEfectivo p = new PremioEfectivo(u, cuota);
                premios.add(p);
                registrarEvento("Premio en efectivo para " + p);
            }
        }
    }

    @Override
    public String tipoTorneo() {
        return "Competitivo";
    }

    @Override
    public String descripcionPremio() {
        return String.format("Inscripción $%,.0f (gratis para empleados). Premio en efectivo: %d%% de lo recaudado"
                + " (actualmente $%,.0f de $%,.0f)", tarifaInscripcion, porcentajePremio, premioTotal(),
                totalRecaudado());
    }

    public double getTarifaInscripcion() {
        return tarifaInscripcion;
    }

    public int getPorcentajePremio() {
        return porcentajePremio;
    }

    public List<PremioEfectivo> getPremios() {
        return premios;
    }
}
