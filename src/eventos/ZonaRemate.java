package eventos;

public enum ZonaRemate {
    AREA_CHICA(0.40),
    AREA_GRANDE(0.15),
    FUERA_DEL_AREA(0.04),
    PENAL(0.76);

    private final double valorXG;

    ZonaRemate(double valorXG) {
        this.valorXG = valorXG;
    }

    public double getValorXG() {
        return valorXG;
    }
}

