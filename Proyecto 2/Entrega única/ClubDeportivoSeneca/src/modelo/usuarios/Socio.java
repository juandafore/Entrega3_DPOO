package modelo.usuarios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.cafeteria.Bebida;
import modelo.cafeteria.ConsumoCafeteria;
import modelo.cafeteria.LineaCafeteria;
import modelo.cafeteria.VentaCafeteria;
import modelo.comun.Categoria;
import modelo.comun.Deporte;
import modelo.comun.Disciplina;
import modelo.comun.Nivel;
import modelo.competencia.Competencia;
import modelo.deportesconjunto.Equipo;
import modelo.deportesconjunto.EstadisticaJugador;
import modelo.deportesconjunto.InscripcionEquipo;
import modelo.deportesconjunto.Suspension;
import modelo.deportesindividuales.InscripcionDisciplina;
import modelo.deportesindividuales.Instalacion;
import modelo.deportesindividuales.PartidoPractica;
import modelo.deportesindividuales.Reserva;
import modelo.tienda.Compra;
import modelo.tienda.CompraTienda;

public class Socio extends Usuario {
    private static final long serialVersionUID = 1L;

    private int edad;
    private int puntosFidelidad;
    private Entrenador entrenadorAsociado;
    private final List<InscripcionEquipo> inscripcionesEquipo = new ArrayList<>();
    private final List<InscripcionDisciplina> inscripcionesDisciplina = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<EstadisticaJugador> estadisticas = new ArrayList<>();
    private final List<Suspension> suspensiones = new ArrayList<>();
    private final List<PartidoPractica> partidosPractica = new ArrayList<>();
    private ConsumoCafeteria consumoActivo;

    public Socio(String login, String password, String nombre, int edad) {
        super(login, password, nombre);
        if (edad <= 0 || edad > 120) {
            throw new IllegalArgumentException("Edad inválida: " + edad);
        }
        this.edad = edad;
    }

    public InscripcionEquipo inscribirseEquipo(Equipo e, String posicion, int numeroCamiseta) {
        if (perteneceAEquipo(e.getDeporte())) {
            throw new IllegalStateException(nombre + " ya pertenece a un equipo de " + e.getDeporte());
        }
        InscripcionEquipo ins = e.inscribir(this, posicion, numeroCamiseta);
        inscripcionesEquipo.add(ins);
        return ins;
    }

    public boolean perteneceAEquipo(Deporte d) {
        return equipoDe(d) != null;
    }

    public Equipo equipoDe(Deporte d) {
        for (InscripcionEquipo i : inscripcionesEquipo) {
            if (i.getEquipo().getDeporte() == d) {
                return i.getEquipo();
            }
        }
        return null;
    }

    public void retirarDeEquipo(Equipo e) {
        inscripcionesEquipo.removeIf(i -> i.getEquipo() == e);
    }

    public InscripcionDisciplina inscribirseDisciplina(Disciplina d, Nivel nivel) {
        if (inscripcionEn(d) != null) {
            throw new IllegalStateException(nombre + " ya está inscrito en " + d);
        }
        InscripcionDisciplina ins = new InscripcionDisciplina(this, d, nivel);
        inscripcionesDisciplina.add(ins);
        return ins;
    }

    public InscripcionDisciplina inscripcionEn(Disciplina d) {
        for (InscripcionDisciplina i : inscripcionesDisciplina) {
            if (i.getDisciplina() == d) {
                return i;
            }
        }
        return null;
    }

    public void reservar(Instalacion i, Reserva r) {
        i.agregarReserva(r);
        reservas.add(r);
    }

    public PartidoPractica programarPractica(Socio rival, Disciplina d) {
        return programarPractica(rival, d, LocalDate.now(), null);
    }

    public PartidoPractica programarPractica(Socio rival, Disciplina d, LocalDate fecha, Reserva reserva) {
        if (rival == this) {
            throw new IllegalArgumentException("No puede programar un partido contra sí mismo");
        }
        InscripcionDisciplina mia = inscripcionEn(d);
        InscripcionDisciplina suya = rival.inscripcionEn(d);
        if (mia == null || suya == null) {
            throw new IllegalStateException("Ambos socios deben estar inscritos en " + d);
        }
        PartidoPractica p = new PartidoPractica(d, fecha, reserva);
        p.agregarParticipante(mia);
        p.agregarParticipante(suya);
        if (!p.puedeJugarse()) {
            throw new IllegalStateException("Niveles incompatibles para el partido de práctica ("
                    + mia.getNivel() + " vs " + suya.getNivel() + ")");
        }
        partidosPractica.add(p);
        rival.partidosPractica.add(p);
        return p;
    }

    public void comprarTienda(CompraTienda c) {
        c.confirmar();
        int pts = c.generarPuntos();
        comprar(c);
        agregarPuntos(pts);
    }

    public void comprarCafeteria(VentaCafeteria v) {
        if (!v.validarBebidaCaliente()) {
            throw new IllegalStateException("No se puede despachar bebida caliente en esta instalación");
        }
        v.confirmar();
        int pts = v.generarPuntos();
        comprar(v);
        agregarPuntos(pts);
        if (v.tieneBebidaCaliente()) {
            if (consumoActivo == null || !consumoActivo.estaActivo()) {
                consumoActivo = new ConsumoCafeteria(this);
            }
            for (LineaCafeteria l : v.getLineas()) {
                if (l.getProducto() instanceof Bebida) {
                    consumoActivo.agregarBebida((Bebida) l.getProducto());
                }
            }
        }
    }

    public void terminarConsumo() {
        if (consumoActivo != null) {
            consumoActivo.cerrar();
            consumoActivo = null;
        }
    }

    public boolean tieneBebidaCaliente() {
        return consumoActivo != null && consumoActivo.estaActivo() && consumoActivo.tieneBebidaCaliente();
    }

    public boolean puedeIngresarA(Instalacion i) {
        return consumoActivo == null || !consumoActivo.estaActivo() || consumoActivo.permiteIngreso(i);
    }

    public void agregarPuntos(int p) {
        puntosFidelidad += p;
    }

    public double redimirPuntos(int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("Debe redimir una cantidad positiva de puntos");
        }
        if (p > puntosFidelidad) {
            throw new IllegalStateException("Puntos insuficientes (saldo: " + puntosFidelidad + ")");
        }
        puntosFidelidad -= p;
        return p;
    }

    public void redimirPuntosEn(Compra c, int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("Debe redimir una cantidad positiva de puntos");
        }
        if (p > puntosFidelidad) {
            throw new IllegalStateException("Puntos insuficientes (saldo: " + puntosFidelidad + ")");
        }
        if (p > c.totalAntesDePuntos()) {
            throw new IllegalArgumentException("No puede redimir más puntos que el valor de la compra");
        }
        c.redimirPuntos(p);
        puntosFidelidad -= p;
    }

    public List<EstadisticaJugador> consultarEstadisticas() {
        return new ArrayList<>(estadisticas);
    }

    public List<Object> consultarHistorial() {
        List<Object> h = new ArrayList<>();
        h.addAll(compras);
        h.addAll(reservas);
        h.addAll(partidosPractica);
        h.addAll(estadisticas);
        h.addAll(inscripcionesEquipo);
        h.addAll(inscripcionesDisciplina);
        return h;
    }

    public Categoria categoriaPorEdad() {
        return Categoria.porEdad(edad);
    }

    public boolean esEntrenador() {
        return entrenadorAsociado != null;
    }

    public void agregarEstadistica(EstadisticaJugador e) {
        estadisticas.add(e);
    }

    public void agregarSuspension(Suspension s) {
        suspensiones.add(s);
    }

    public boolean estaSuspendido() {
        for (Suspension s : suspensiones) {
            if (s.estaVigente()) {
                return true;
            }
        }
        return false;
    }

    public boolean estaSuspendidoEn(Competencia ambito) {
        for (Suspension s : suspensiones) {
            if (s.estaVigente() && s.getCompetencia() == ambito) {
                return true;
            }
        }
        return false;
    }

    public int getEdad() {
        return edad;
    }

    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public Entrenador getEntrenadorAsociado() {
        return entrenadorAsociado;
    }

    public void setEntrenadorAsociado(Entrenador entrenadorAsociado) {
        this.entrenadorAsociado = entrenadorAsociado;
    }

    public List<InscripcionEquipo> getInscripcionesEquipo() {
        return inscripcionesEquipo;
    }

    public List<InscripcionDisciplina> getInscripcionesDisciplina() {
        return inscripcionesDisciplina;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public List<Suspension> getSuspensiones() {
        return suspensiones;
    }

    public List<PartidoPractica> getPartidosPractica() {
        return partidosPractica;
    }

    public ConsumoCafeteria getConsumoActivo() {
        return consumoActivo;
    }

    @Override
    public String tipo() {
        return "Socio";
    }
}
