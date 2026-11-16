package consola;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public class Entrada {
    public static class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner sc;
    private final PrintStream out;

    public Entrada(InputStream in, PrintStream out) {
        this.sc = new Scanner(in, "UTF-8");
        this.out = out;
    }

    public PrintStream out() {
        return out;
    }

    private String linea(String msg) {
        out.print(msg);
        try {
            return sc.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new FinDeEntrada();
        }
    }

    public String texto(String msg) {
        while (true) {
            String s = linea(msg);
            if (!s.isEmpty()) {
                return s;
            }
            out.println("  El valor no puede estar vacío.");
        }
    }

    public String textoOpcional(String msg) {
        return linea(msg);
    }

    public int entero(String msg, int min, int max) {
        while (true) {
            String s = linea(msg);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) {
                    return v;
                }
                out.println("  Ingrese un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                out.println("  Ingrese un número entero válido.");
            }
        }
    }

    public double decimal(String msg, double min, double max) {
        while (true) {
            String s = linea(msg).replace(",", ".");
            try {
                double v = Double.parseDouble(s);
                if (v >= min && v <= max) {
                    return v;
                }
                out.println("  Ingrese un valor entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                out.println("  Ingrese un número válido.");
            }
        }
    }

    public boolean siNo(String msg) {
        while (true) {
            String s = linea(msg + " (s/n): ").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sí")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            out.println("  Responda s o n.");
        }
    }

    public LocalDate fecha(String msg) {
        while (true) {
            String s = linea(msg + " (AAAA-MM-DD, vacío = hoy): ");
            if (s.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                out.println("  Fecha inválida. Use el formato AAAA-MM-DD.");
            }
        }
    }

    public LocalTime hora(String msg) {
        while (true) {
            String s = linea(msg + " (HH:MM): ");
            try {
                return LocalTime.parse(s.length() == 4 ? "0" + s : s);
            } catch (DateTimeParseException e) {
                out.println("  Hora inválida. Use el formato HH:MM (24 horas).");
            }
        }
    }

    public LocalTime[] rangoHorario(String inicio, String fin) {
        while (true) {
            LocalTime a = hora(inicio);
            LocalTime b = hora(fin);
            if (a.isBefore(b)) {
                return new LocalTime[] {a, b};
            }
            out.println("  La hora de fin debe ser posterior a la de inicio.");
        }
    }

    public <T> T seleccionar(String titulo, List<T> opciones, Function<T, String> texto) {
        if (opciones.isEmpty()) {
            out.println("  (No hay " + titulo.toLowerCase() + " disponibles)");
            return null;
        }
        out.println(titulo + ":");
        for (int i = 0; i < opciones.size(); i++) {
            out.println("  " + (i + 1) + ". " + texto.apply(opciones.get(i)));
        }
        int idx = entero("Seleccione (0 = cancelar): ", 0, opciones.size());
        return idx == 0 ? null : opciones.get(idx - 1);
    }

    public <T> List<T> seleccionarVarios(String titulo, List<T> opciones, Function<T, String> texto) {
        List<T> elegidos = new java.util.ArrayList<>();
        List<T> restantes = new java.util.ArrayList<>(opciones);
        while (!restantes.isEmpty()) {
            T t = seleccionar(titulo + " (elegidos: " + elegidos.size() + ", 0 = terminar)", restantes, texto);
            if (t == null) {
                break;
            }
            elegidos.add(t);
            restantes.remove(t);
        }
        return elegidos;
    }

    public <T> T seleccionar(String titulo, List<T> opciones) {
        return seleccionar(titulo, opciones, String::valueOf);
    }

    public <E extends Enum<E>> E enumeracion(String titulo, Class<E> tipo) {
        E[] vals = tipo.getEnumConstants();
        out.println(titulo + ":");
        for (int i = 0; i < vals.length; i++) {
            out.println("  " + (i + 1) + ". " + vals[i]);
        }
        return vals[entero("Seleccione: ", 1, vals.length) - 1];
    }
}
