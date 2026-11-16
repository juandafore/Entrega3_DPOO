package consola;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final String titulo;
    private final List<OpcionMenu> opciones = new ArrayList<>();

    public Menu(String titulo) {
        this.titulo = titulo;
    }

    public Menu opcion(String texto, Runnable accion) {
        opciones.add(new OpcionMenu(texto, accion));
        return this;
    }

    public List<OpcionMenu> getOpciones() {
        return opciones;
    }

    public void ejecutar(Entrada in) {
        while (true) {
            in.out().println();
            in.out().println("--- " + titulo + " ---");
            for (int i = 0; i < opciones.size(); i++) {
                in.out().println(String.format("%2d. %s", i + 1, opciones.get(i).getTexto()));
            }
            in.out().println(" 0. Volver / cerrar sesión");
            int op = in.entero("Opción: ", 0, opciones.size());
            if (op == 0) {
                return;
            }
            try {
                opciones.get(op - 1).ejecutar();
            } catch (Entrada.FinDeEntrada e) {
                throw e;
            } catch (RuntimeException e) {
                in.out().println("  >> No se pudo completar la operación: " + e.getMessage());
            }
        }
    }
}
