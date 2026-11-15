package modelo.usuarios;

public class Invitado extends Usuario {
    private static final long serialVersionUID = 1L;

    private boolean estudiante;
    private String documento;

    public Invitado(String login, String password, String nombre, String documento, boolean estudiante) {
        super(login, password, nombre);
        this.documento = documento;
        this.estudiante = estudiante;
    }

    public boolean isEstudiante() {
        return estudiante;
    }

    public String getDocumento() {
        return documento;
    }

    @Override
    public String tipo() {
        return estudiante ? "Estudiante" : "Externo";
    }
}
