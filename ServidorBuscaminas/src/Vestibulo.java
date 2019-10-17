
import java.util.List;

public class Vestibulo {

    private List<Usuarios> listaJugadores;
    private String[][] tablero;
    private String ID_Unico;

    public Vestibulo() {
    }

    public Vestibulo(List<Usuarios> lista_jugadores, String[][] tablero, String uid) {
        this.listaJugadores = lista_jugadores;
        this.tablero = tablero;
        this.ID_Unico = uid;
    }

    public List<Usuarios> getLista_usuarios() {
        return listaJugadores;
    }

    public void setLista_usuarios(List<Usuarios> lista_jugadores) {
        this.listaJugadores = lista_jugadores;
    }

    public String[][] getTablero() {
        return tablero;
    }

    public void setTablero(String[][] tablero) {
        this.tablero = tablero;
    }

    public String getID_Unico() {
        return ID_Unico;
    }

    public void setID_Unico(String ID_Unico) {
        this.ID_Unico = ID_Unico;
    }
}
