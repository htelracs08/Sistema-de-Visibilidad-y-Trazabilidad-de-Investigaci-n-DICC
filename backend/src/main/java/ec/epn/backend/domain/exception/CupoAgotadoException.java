package ec.epn.backend.domain.exception;

public class CupoAgotadoException extends DominioException {
    
    private final int activos;
    private final int maximo;

    public CupoAgotadoException(int activos, int maximo) {
        super(String.format(
            "Cupo de ayudantes agotado: %d/%d activos", 
            activos, 
            maximo
        ));
        this.activos = activos;
        this.maximo = maximo;
    }

    public int getActivos() {
        return activos;
    }

    public int getMaximo() {
        return maximo;
    }
}