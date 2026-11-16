package modelo.torneos;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.EstadoTorneo;
import modelo.comun.FormatoTorneo;
import modelo.comun.Nivel;
import modelo.comun.TipoParticipacion;
import modelo.usuarios.Usuario;

public abstract class Torneo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double PORCENTAJE_PREFERENCIAL = 0.20;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected int id;
    protected String nombre;
    protected TipoParticipacion tipoParticipacion;
    protected Deporte deporte;
    protected Categoria categoria;
    protected Disciplina disciplina;
    protected Nivel nivel;
    protected FormatoTorneo formato;
    protected int cupos;
    protected LocalDate fecha;
    protected EstadoTorneo estado = EstadoTorneo.INSCRIPCIONES_ABIERTAS;
    protected boolean preferencialesLiberados;
    protected final List<InscripcionTorneo> inscripciones = new ArrayList<>();
    protected final List<EncuentroTorneo> encuentros = new ArrayList<>();
    protected final List<String> bitacora = new ArrayList<>();
    protected ParticipanteTorneo campeon;

    protected Torneo(int id, String nombre, Deporte deporte, Categoria categoria, FormatoTorneo formato,
            int cupos, LocalDate fecha) {
        this(id, nombre, TipoParticipacion.EQUIPO, formato, cupos, fecha);
        if (deporte == null || categoria == null) {
            throw new IllegalArgumentException("Un torneo de conjunto requiere deporte y categoría");
        }
        this.deporte = deporte;
        this.categoria = categoria;
    }

    protected Torneo(int id, String nombre, Disciplina disciplina, Nivel nivel, boolean dobles,
            FormatoTorneo formato, int cupos, LocalDate fecha) {
        this(id, nombre, dobles ? TipoParticipacion.DOBLES : TipoParticipacion.INDIVIDUAL, formato, cupos, fecha);
        if (disciplina == null || nivel == null) {
            throw new IllegalArgumentException("Un torneo individual requiere disciplina y nivel");
        }
        if (!dobles && disciplina == Disciplina.PADEL) {
            throw new IllegalArgumentException("El pádel siempre se juega en dobles");
        }
        this.disciplina = disciplina;
        this.nivel = nivel;
    }

    private Torneo(int id, String nombre, TipoParticipacion tipo, FormatoTorneo formato, int cupos, LocalDate fecha) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El torneo debe tener nombre");
        }
        if (cupos < 2) {
            throw new IllegalArgumentException("Un torneo debe tener al menos 2 cupos");
        }
        if (fecha == null || formato == null) {
            throw new IllegalArgumentException("El torneo requiere fecha y formato");
        }
        this.id = id;
        this.nombre = nombre;
        this.tipoParticipacion = tipo;
        this.formato = formato;
        this.cupos = cupos;
        this.fecha = fecha;
        registrarEvento("Torneo creado (" + tipoTorneo() + ", " + cupos + " cupos, "
                + cuposPreferenciales() + " preferenciales)");
    }

    public int cuposPreferenciales() {
        return (int) Math.ceil(cupos * PORCENTAJE_PREFERENCIAL);
    }

    public int inscritosPreferenciales() {
        int n = 0;
        for (InscripcionTorneo i : inscripciones) {
            if (i.isPreferencial()) {
                n++;
            }
        }
        return n;
    }

    public int inscritosRegulares() {
        return inscripciones.size() - inscritosPreferenciales();
    }

    public int cuposPreferencialesDisponibles() {
        if (preferencialesLiberados) {
            return 0;
        }
        return Math.max(0, cuposPreferenciales() - inscritosPreferenciales());
    }

    public int cuposRegularesDisponibles() {
        int capacidadRegular = preferencialesLiberados ? cupos - inscritosPreferenciales()
                : cupos - cuposPreferenciales();
        return Math.max(0, capacidadRegular - inscritosRegulares());
    }

    public int cuposDisponibles() {
        return cupos - inscripciones.size();
    }

    public void liberarCuposPreferenciales() {
        verificarAbierto();
        if (preferencialesLiberados) {
            throw new IllegalStateException("Los cupos preferenciales ya fueron liberados");
        }
        int libres = cuposPreferencialesDisponibles();
        preferencialesLiberados = true;
        registrarEvento("Se liberaron " + libres + " cupos preferenciales como regulares");
    }

    public InscripcionTorneo inscribir(ParticipanteTorneo p, boolean elegiblePreferencial) {
        verificarAbierto();
        validarTipo(p);
        for (InscripcionTorneo i : inscripciones) {
            if (i.getParticipante().comparteIntegrantesCon(p)) {
                throw new IllegalStateException("Algún integrante de " + p.getNombre()
                        + " ya está inscrito en el torneo");
            }
        }
        boolean preferencial;
        if (elegiblePreferencial && cuposPreferencialesDisponibles() > 0) {
            preferencial = true;
        } else if (cuposRegularesDisponibles() > 0) {
            preferencial = false;
        } else {
            throw new IllegalStateException("No hay cupos disponibles"
                    + (cuposPreferencialesDisponibles() > 0
                            ? " (los cupos restantes son preferenciales para los mejores del ranking)" : ""));
        }
        InscripcionTorneo ins = new InscripcionTorneo(p, preferencial, tarifaPara(p));
        inscripciones.add(ins);
        registrarEvento("Inscripción: " + ins);
        return ins;
    }

    private void validarTipo(ParticipanteTorneo p) {
        boolean ok = (tipoParticipacion == TipoParticipacion.EQUIPO && p instanceof ParticipanteEquipo)
                || (tipoParticipacion == TipoParticipacion.INDIVIDUAL && p instanceof ParticipanteIndividual)
                || (tipoParticipacion == TipoParticipacion.DOBLES && p instanceof ParticipantePareja);
        if (!ok) {
            throw new IllegalArgumentException("Este torneo es de participación " + tipoParticipacion);
        }
    }

    public InscripcionTorneo inscripcionDe(Usuario u) {
        for (InscripcionTorneo i : inscripciones) {
            if (i.getParticipante().contiene(u)) {
                return i;
            }
        }
        return null;
    }

    public InscripcionTorneo inscripcionDe(ParticipanteTorneo p) {
        for (InscripcionTorneo i : inscripciones) {
            if (i.getParticipante() == p) {
                return i;
            }
        }
        return null;
    }

    public void retirar(InscripcionTorneo ins) {
        verificarAbierto();
        if (!inscripciones.remove(ins)) {
            throw new IllegalArgumentException("La inscripción no pertenece a este torneo");
        }
        registrarEvento("Retiro: " + ins.getParticipante().getNombre() + " (se libera un cupo "
                + (ins.isPreferencial() ? "preferencial" : "regular") + ")");
    }

    public void iniciar() {
        verificarAbierto();
        if (inscripciones.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 participantes para iniciar el torneo");
        }
        encuentros.addAll(estrategia().generarIniciales(participantes()));
        estado = EstadoTorneo.EN_CURSO;
        registrarEvento("Torneo iniciado con " + inscripciones.size() + " participantes");
    }

    public void registrarResultado(EncuentroTorneo e, int marcadorA, int marcadorB) {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new IllegalStateException("Solo se registran resultados de torneos en curso");
        }
        if (!encuentros.contains(e)) {
            throw new IllegalArgumentException("El encuentro no pertenece a este torneo");
        }
        e.registrar(marcadorA, marcadorB, permiteEmpate());
        registrarEvento("Resultado: " + e);
        encuentros.addAll(estrategia().siguientes(encuentros));
        if (estrategia().terminado(encuentros)) {
            finalizar();
        }
    }

    private void finalizar() {
        campeon = estrategia().posiciones(encuentros, participantes()).get(0).getParticipante();
        estado = EstadoTorneo.FINALIZADO;
        registrarEvento("Torneo finalizado. Campeón: " + campeon.getNombre());
        otorgarPremio();
    }

    public boolean permiteEmpate() {
        return formato == FormatoTorneo.LIGA && tipoParticipacion == TipoParticipacion.EQUIPO
                && deporte.admiteEmpate();
    }

    public EstrategiaFormato estrategia() {
        return formato == FormatoTorneo.LIGA ? new FormatoLiga() : new FormatoEliminacion();
    }

    public List<FilaTorneo> posiciones() {
        return estrategia().posiciones(encuentros, participantes());
    }

    public List<ParticipanteTorneo> participantes() {
        List<ParticipanteTorneo> r = new ArrayList<>();

        for (InscripcionTorneo i : inscripciones) {
            if (i.isPreferencial()) {
                r.add(i.getParticipante());
            }
        }
        for (InscripcionTorneo i : inscripciones) {
            if (!i.isPreferencial()) {
                r.add(i.getParticipante());
            }
        }
        return r;
    }

    public List<EncuentroTorneo> encuentrosPendientes() {
        List<EncuentroTorneo> r = new ArrayList<>();
        for (EncuentroTorneo e : encuentros) {
            if (!e.isJugado()) {
                r.add(e);
            }
        }
        return r;
    }

    protected void verificarAbierto() {
        if (estado != EstadoTorneo.INSCRIPCIONES_ABIERTAS) {
            throw new IllegalStateException("El torneo " + nombre + " ya inició (estado: " + estado + ")");
        }
    }

    protected void registrarEvento(String texto) {
        bitacora.add(LocalDateTime.now().format(FMT) + " - " + texto);
    }

    public abstract double tarifaPara(ParticipanteTorneo p);

    protected abstract void otorgarPremio();

    public abstract String tipoTorneo();

    public abstract String descripcionPremio();

    public String deporteODisciplina() {
        return tipoParticipacion == TipoParticipacion.EQUIPO ? deporte + " " + categoria
                : disciplina + " " + nivel + (tipoParticipacion == TipoParticipacion.DOBLES ? " (dobles)" : "");
    }

    public String historialDetallado() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(this).append(" ===\n");
        sb.append("Formato: ").append(formato).append(" | Fecha: ").append(fecha)
                .append(" | Cupos: ").append(inscripciones.size()).append("/").append(cupos)
                .append(" (preferenciales: ").append(cuposPreferenciales())
                .append(preferencialesLiberados ? ", liberados" : "").append(")\n");
        sb.append("Premio: ").append(descripcionPremio()).append("\n");
        sb.append("Participantes:\n");
        if (inscripciones.isEmpty()) {
            sb.append("   (ninguno)\n");
        }
        for (InscripcionTorneo i : inscripciones) {
            sb.append("   - ").append(i).append("\n");
        }
        sb.append("Encuentros:\n");
        if (encuentros.isEmpty()) {
            sb.append("   (aún no se han generado)\n");
        }
        for (EncuentroTorneo e : encuentros) {
            sb.append("   ").append(e).append("\n");
        }
        if (!encuentros.isEmpty()) {
            sb.append("Posiciones:\n");
            int pos = 1;
            for (FilaTorneo f : posiciones()) {
                sb.append(String.format("   %2d. %s%n", pos++, f));
            }
        }
        if (campeon != null) {
            sb.append("Campeón: ").append(campeon.getNombre()).append("\n");
        }
        sb.append("Bitácora:\n");
        for (String b : bitacora) {
            sb.append("   ").append(b).append("\n");
        }
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoParticipacion getTipoParticipacion() {
        return tipoParticipacion;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public FormatoTorneo getFormato() {
        return formato;
    }

    public int getCupos() {
        return cupos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public boolean isPreferencialesLiberados() {
        return preferencialesLiberados;
    }

    public List<InscripcionTorneo> getInscripciones() {
        return inscripciones;
    }

    public List<EncuentroTorneo> getEncuentros() {
        return encuentros;
    }

    public List<String> getBitacora() {
        return bitacora;
    }

    public ParticipanteTorneo getCampeon() {
        return campeon;
    }

    @Override
    public String toString() {
        return "#" + id + " " + nombre + " [" + tipoTorneo() + " - " + deporteODisciplina() + "] " + estado;
    }
}
