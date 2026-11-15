package modelo.competencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.FaseAscun;
import modelo.deportesconjunto.Alineacion;
import modelo.deportesconjunto.Partido;
import modelo.usuarios.Socio;

public class PartidoCompetencia extends Partido {
    private String institucionRival;
    private String escenario;
    private FaseAscun fase;
    private InscripcionCompetencia inscripcion;
    private final List<Alineacion> alineacion = new ArrayList<>();

    public PartidoCompetencia(InscripcionCompetencia inscripcion, String institucionRival,
            String escenario, LocalDate fecha, LocalTime hora) {
        super(inscripcion.getEquipo(), null, fecha, hora);
        this.inscripcion = inscripcion;
        this.institucionRival = institucionRival;
        this.escenario = escenario;
    }

    @Override
    public Competencia ambito() {
        return inscripcion.getCompetencia();
    }

    @Override
    public boolean puedeJugarse() {
        int min = local.getDeporte().getMinJugadores();
        return local.cumpleMinimo(min, ambito());
    }

    @Override
    public boolean esElegible(Socio s) {
        if (!super.esElegible(s) || !validarNomina(s)) {
            return false;
        }
        if (alineacion.isEmpty()) {
            return true;
        }
        for (Alineacion a : alineacion) {
            if (a.getInscripcion().getSocio() == s) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getFecha() + " " + local.getNombre() + " " + marcadorLocal + "-" + marcadorVisitante + " "
                + institucionRival + " @ " + escenario + " (" + estado + ")";
    }

    public void alinear(Alineacion a) {
        Socio socio = a.getInscripcion().getSocio();
        if (!a.esElegible()) {
            throw new IllegalStateException("No se puede alinear a " + socio.getNombre() + ": está suspendido");
        }
        if (!validarNomina(socio)) {
            throw new IllegalStateException("No se puede alinear a " + socio.getNombre()
                    + ": no está en la nómina de la competencia");
        }
        for (Alineacion ya : alineacion) {
            if (ya.getInscripcion().getSocio() == socio) {
                throw new IllegalStateException(socio.getNombre() + " ya está alineado");
            }
        }
        alineacion.add(a);
    }

    public boolean validarNomina(Socio s) {
        return inscripcion.enNomina(s);
    }

    public String getInstitucionRival() {
        return institucionRival;
    }

    public String getEscenario() {
        return escenario;
    }

    public FaseAscun getFase() {
        return fase;
    }

    public void setFase(FaseAscun fase) {
        this.fase = fase;
    }

    public InscripcionCompetencia getInscripcion() {
        return inscripcion;
    }

    public List<Alineacion> getAlineacion() {
        return alineacion;
    }
}
