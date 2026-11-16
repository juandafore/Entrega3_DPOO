package consola;

import modelo.usuarios.Administrador;
import modelo.usuarios.Empleado;
import modelo.usuarios.Usuario;

public class ConsolaPrincipal extends ConsolaBase {
    public static void main(String[] args) {
        new ConsolaPrincipal().ejecutar();
    }

    @Override
    protected String titulo() {
        return "Consola principal (todos los usuarios)";
    }

    @Override
    protected boolean acepta(Usuario u) {
        return true;
    }

    @Override
    protected void agregarOpcionesInicio(Menu inicio) {
        consolaPara(null).agregarOpcionesInicio(inicio);
    }

    @Override
    protected Menu menuPara(Usuario u) {
        ConsolaBase consola = consolaPara(u);
        in.out().println("Abriendo: " + consola.titulo());
        return consola.menuPara(u);
    }

    private ConsolaBase consolaPara(Usuario u) {
        if (u instanceof Administrador) {
            return new ConsolaAdministrador(club, in);
        }
        if (u instanceof Empleado) {
            return new ConsolaEmpleado(club, in);
        }
        return new ConsolaJugador(club, in);
    }
}
