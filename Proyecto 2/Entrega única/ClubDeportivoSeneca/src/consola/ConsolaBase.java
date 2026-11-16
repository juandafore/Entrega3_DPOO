package consola;

import java.io.IOException;

import main.DatosIniciales;
import modelo.Club;
import modelo.usuarios.Usuario;
import persistencia.PersistenciaClub;

public abstract class ConsolaBase {
    public static final String CARPETA_DATOS = "datos";
    private static final int MAX_INTENTOS = 3;

    protected final Club club;
    protected final Entrada in;

    protected ConsolaBase() {
        this(cargarClub(), new Entrada(System.in, System.out));
    }

    protected ConsolaBase(Club club, Entrada in) {
        this.club = club;
        this.in = in;
    }

    protected static Club cargarClub() {
        try {
            Club c = PersistenciaClub.cargar(CARPETA_DATOS);
            if (c != null) {
                return c;
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("(No se pudieron leer los datos guardados: " + e.getMessage() + ")");
        }
        System.out.println("(No había datos guardados; se cargaron los datos de prueba)");
        return DatosIniciales.crearClubDemo();
    }

    protected abstract String titulo();

    protected abstract boolean acepta(Usuario u);

    protected abstract Menu menuPara(Usuario u);

    protected void agregarOpcionesInicio(Menu inicio) {
    }

    public void ejecutar() {
        in.out().println("=== " + club.getNombre() + " - " + titulo() + " ===");
        Menu inicio = new Menu("INICIO");
        inicio.opcion("Iniciar sesión", this::iniciarSesion);
        agregarOpcionesInicio(inicio);
        try {
            inicio.ejecutar(in);
        } catch (Entrada.FinDeEntrada e) {
            in.out().println();
        } finally {
            guardar();
        }
    }

    private void iniciarSesion() {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            String login = in.texto("Login: ");
            String pass = in.texto("Password: ");
            Usuario u = club.autenticar(login, pass);
            if (u == null) {
                in.out().println("  Credenciales inválidas (intento " + intento + " de " + MAX_INTENTOS + ").");
            } else if (!acepta(u)) {
                in.out().println("  El usuario " + u.getLogin() + " (" + u.tipo()
                        + ") no puede usar esta consola. Use la consola correspondiente a su rol.");
                return;
            } else {
                in.out().println("Bienvenido, " + u.getNombre() + " (" + u.tipo() + ")");
                menuPara(u).ejecutar(in);
                in.out().println("Sesión cerrada.");
                return;
            }
        }
    }

    protected void guardar() {
        try {
            PersistenciaClub.guardar(club, CARPETA_DATOS);
            in.out().println("Datos guardados en la carpeta '" + CARPETA_DATOS + "'. ¡Hasta luego!");
        } catch (IOException e) {
            in.out().println("ERROR: no se pudieron guardar los datos: " + e.getMessage());
        }
    }
}
