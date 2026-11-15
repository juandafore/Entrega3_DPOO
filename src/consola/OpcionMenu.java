package consola;

public class OpcionMenu {
    private final String texto;
    private final Runnable accion;

    public OpcionMenu(String texto, Runnable accion) {
        this.texto = texto;
        this.accion = accion;
    }

    public String getTexto() {
        return texto;
    }

    public void ejecutar() {
        accion.run();
    }
}
