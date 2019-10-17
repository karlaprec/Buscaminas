
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Principal {

    private static List<Vestibulo> listavestibulos = new ArrayList<>();

    private static final int Jugadores_Maximos = 4;
    private static final String bomba = "Oculta:mina";
    private static int filas_juego = 16;
    private static int columas_juego = 16;
    private static int minas_division = 6;

    static JFrame frame = new JFrame("-SERVIDOR-");
    static JTextArea messageArea = new JTextArea(15, 30);

    public static void main(String[] args) throws Exception {
        messageArea.setEditable(false);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        Font font = new Font("Arial", Font.BOLD, 20);
        messageArea.setFont(font);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.setResizable(true);
        frame.pack();
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            messageArea.append("IP: " + obtenerIP() + "\n");
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (Exception e) {
        }
    }

    private static class Handler implements Runnable {

        private String nombre;
        private Socket socket;
        private String lobby;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    nombre = in.nextLine();
                    if (nombre == null || nombre.isEmpty() || nombre.equals("null")) {
                        continue;
                    }
                    synchronized (listavestibulos) {
                        if (listavestibulos.isEmpty()) {
                            listavestibulos.add(new Vestibulo(new ArrayList<Usuarios>(), null, UUID.randomUUID().toString()));
                        }

                        int indice = LOBBYD();

                        if (indice == -1) {
                            listavestibulos.add(new Vestibulo(new ArrayList<Usuarios>(), null, UUID.randomUUID().toString()));
                            indice = listavestibulos.size() - 1;
                        }
                        if (!EXISTE(nombre, indice)) {
                            listavestibulos.get(indice).getLista_usuarios().add(new Usuarios(nombre, out, 0, false, 0, false, -1));
                            lobby = listavestibulos.get(indice).getID_Unico();
                            out.println("NAMEACCEPTED " + nombre);
                            if (listavestibulos.get(indice).getLista_usuarios().size() == 1) {
                                out.println("ADMIN");
                            }
                            out.println("LIST " + nombre);
                            messageArea.append(nombre + " ENTRO\n");
                            ENVIARL(indice);
                            if (listavestibulos.get(indice).getLista_usuarios().size() == Jugadores_Maximos) {
                                listavestibulos.add(new Vestibulo(new ArrayList<Usuarios>(), null, UUID.randomUUID().toString()));
                            }
                            break;
                        }
                    }
                }

                while (true) {
                    String input = in.nextLine();
                    if (input.startsWith("/JUGAR")) {
                        if (listavestibulos.get(BUSCAR(lobby)).getLista_usuarios().size() > 1) {
                            LIMPIARJ(BUSCAR(lobby));
                            LLENAR(BUSCAR(lobby));
                        }
                    } else if (input.startsWith("/Partida")) {
                        if (listavestibulos.get(BUSCAR(lobby)).getLista_usuarios().get(BUSCARU(nombre, BUSCAR(lobby))).getVivo()) {
                            int x = Integer.parseInt(input.substring(input.indexOf("=") + 1, input.indexOf(",")));
                            int y = Integer.parseInt(input.substring(input.indexOf(",") + 1, input.indexOf(";")));
                            String accion = input.substring(input.indexOf(";") + 1, input.indexOf("]"));

                            synchronized (listavestibulos.get(BUSCAR(lobby)).getTablero()) {
                                CLICKT(x, y, BUSCAR(lobby), BUSCARU(nombre, BUSCAR(lobby)), accion, out);
                            }

                            for (Usuarios usuario : listavestibulos.get(BUSCAR(lobby)).getLista_usuarios()) {
                                usuario.getEscritor().println("ACTUALIZAR=" + x + "," + y + ";" + listavestibulos.get(BUSCAR(lobby)).getTablero()[x][y]);
                            }

                        }
                    }
                }

            } catch (Exception e) {
                //System.err.println(e);
            } finally {
                if (out != null && nombre != null) {
                    messageArea.append(nombre + " SALIO\n");
                    if (listavestibulos.get(BUSCAR(lobby)).getTablero() != null) {
                        LIMPIAR(BUSCAR(lobby), BUSCARU(nombre, BUSCAR(lobby)));
                    }
                    listavestibulos.get(BUSCAR(lobby)).getLista_usuarios().remove(BUSCARU(nombre, BUSCAR(lobby)));
                    if (listavestibulos.get(BUSCAR(lobby)).getLista_usuarios().size() == 0) {
                        listavestibulos.remove(BUSCAR(lobby));
                    }
                    ENVIARL(BUSCAR(lobby));
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String obtenerIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) {
                        continue;
                    }
                    ip = addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    public static String CONVARREGLO(String mat[][]) {
        String arreglo = "";
        for (String[] row : mat) {
            for (String x : row) {
                arreglo = arreglo + x + " ";
            }
        }
        return arreglo;
    }

    public static void IMPRIMIRA(String mat[][]) {
        messageArea.append("-----------------------------------------\n");
        for (String[] row : mat) {
            for (String x : row) {
                messageArea.append("[" + x + "] ");
            }
            messageArea.append("\n");
        }
    }

    public static void LLENAR(int lobby) {
        int m = filas_juego;
        int n = columas_juego;
        int minas = (int) m * n / minas_division;
        String[][] cuadricula = new String[n][n];
        for (int x = 0; x < m; x++) {
            for (int y = 0; y < n; y++) {
                cuadricula[x][y] = "Oculta:0";
            }
        }
        int contador = 0;
        for (int x = 0; x < m; x++) {
            for (int y = 0; y < n; y++) {
                int ranx = (int) (Math.random() * (n));
                int rany = (int) (Math.random() * (m));
                if (!cuadricula[ranx][rany].equals(bomba) && contador < minas) {
                    cuadricula[ranx][rany] = bomba + "";
                    contador++;
                }
            }
        }

        for (int rx = 0; rx < m; rx++) {
            for (int ry = 0; ry < n; ry++) {
                if (cuadricula[rx][ry] != bomba + "") {
                    int lados = 0;
                    //Primera fila
                    try {
                        if (cuadricula[rx - 1][ry - 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }

                    try {
                        if (cuadricula[rx][ry - 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }

                    try {
                        if (cuadricula[rx + 1][ry - 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }

                    //Segunda fila
                    try {
                        if (cuadricula[rx - 1][ry] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }

                    try {
                        if (cuadricula[rx + 1][ry] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }

                    //Tercera fila
                    try {
                        if (cuadricula[rx - 1][ry + 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (cuadricula[rx][ry + 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (cuadricula[rx + 1][ry + 1] == bomba + "") {
                            lados++;
                        }
                    } catch (Exception e) {
                    }
                    cuadricula[rx][ry] = "Oculta:" + lados;
                }
            }
        }
        listavestibulos.get(lobby).setTablero(cuadricula);
        INICARJ(lobby, m, n, minas);
        for (int i = 0; i < listavestibulos.get(lobby).getLista_usuarios().size(); i++) {
            listavestibulos.get(lobby).getLista_usuarios().get(i).setBanderas(minas);
        }
    }

    public static void INICARJ(int lobby, int m, int n, int minas) {
        for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
            usuario.getEscritor().println("INICIAR=" + m + "," + n + ";" + minas + "]" + usuario.getColor());
        }
    }

    public static boolean EXISTE(String nombre, int indice) {
        boolean existe = false;
        for (Usuarios usuario : listavestibulos.get(indice).getLista_usuarios()) {
            if (usuario.getNombre().equals(nombre)) {
                existe = true;
            }
        }
        return existe;
    }

    public static int LOBBYD() {
        int lobby = -1;
        for (int x = 0; x < listavestibulos.size(); x++) {
            if (listavestibulos.get(x).getLista_usuarios().size() <= 4) {
                lobby = x;
                break;
            }
        }
        return lobby;
    }

    public static void ENVIARL(int indice) {
        int admin = 0;
        for (Usuarios usuario_mgs : listavestibulos.get(indice).getLista_usuarios()) {
            usuario_mgs.getEscritor().println("LIMPIAR_LISTA");
            if (admin == 0) {
                usuario_mgs.getEscritor().println("ADMIN");
                admin++;
            } else {
                usuario_mgs.getEscritor().println("NOADMIN");
            }
            for (Usuarios nombres : listavestibulos.get(indice).getLista_usuarios()) {
                usuario_mgs.getEscritor().println("LIST " + nombres.getNombre());
            }
        }
    }

    public static void LIMPIARJ(int indice) {
        for (int usr = 0; usr < listavestibulos.get(indice).getLista_usuarios().size(); usr++) {
            listavestibulos.get(indice).getLista_usuarios().get(usr).setPuntos(0);
            listavestibulos.get(indice).getLista_usuarios().get(usr).setVivo(true);
            listavestibulos.get(indice).getLista_usuarios().get(usr).setIniciado(false);
            listavestibulos.get(indice).getLista_usuarios().get(usr).setColor(usr);
        }
    }

    public static void CLICKT(int x, int y, int lobby, int posicion, String accion, PrintWriter out) {
        if (!listavestibulos.get(lobby).getLista_usuarios().get(posicion).getIniciado()) {
            switch (posicion) {
                case 0:
                    if (y == 0) {
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setIniciado(true);
                        CLICKT(x, y, lobby, posicion, accion, out);
                    }
                    break;
                case 1:
                    if (x == 0) {
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setIniciado(true);
                        CLICKT(x, y, lobby, posicion, accion, out);
                    }
                    break;
                case 2:
                    if (y == columas_juego - 1) {
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setIniciado(true);
                        CLICKT(x, y, lobby, posicion, accion, out);
                    }
                    break;
                case 3:
                    if (x == filas_juego - 1) {
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setIniciado(true);
                        CLICKT(x, y, lobby, posicion, accion, out);
                    }
                    break;
            }
        } else {

            if (accion.equals("IZQ")) {
                if (listavestibulos.get(lobby).getTablero()[x][y].startsWith("Oculta:")) {
                    if (listavestibulos.get(lobby).getTablero()[x][y].equals(bomba)) {
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setVivo(false);
                        listavestibulos.get(lobby).getTablero()[x][y] = "Visible:mina/" + listavestibulos.get(lobby).getLista_usuarios().get(posicion).getColor();
                        for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
                            usuario.getEscritor().println("ACTUALIZAR=" + x + "," + y + ";" + listavestibulos.get(lobby).getTablero()[x][y]);
                        }
                        out.println("MUERTO");
                    } else {
                        String num = (listavestibulos.get(lobby).getTablero()[x][y].substring(7));
                        if (esNumerico(num)) {
                            if (Integer.parseInt(num) == 0) {
                                DES0DIAG(x, y, lobby, posicion, out);
                            } else {
                                listavestibulos.get(lobby).getTablero()[x][y] = "Visible:" + num + "/" + listavestibulos.get(lobby).getLista_usuarios().get(posicion).getColor();
                            }
                        }
                    }
                }
            } else {
                //Click DER
                if (listavestibulos.get(lobby).getTablero()[x][y].startsWith("Oculta:")) {
                    if (listavestibulos.get(lobby).getLista_usuarios().get(posicion).getBanderas() > 0) {
                        //Si es Oculto
                        String valor = listavestibulos.get(lobby).getTablero()[x][y].substring(7);
                        if (valor.equals("mina")) {
                            listavestibulos.get(lobby).getTablero()[x][y] = "Visible:bandera" + listavestibulos.get(lobby).getLista_usuarios().get(posicion).getColor() + "|" + valor;
                            listavestibulos.get(lobby).getLista_usuarios().get(posicion).setPuntos(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getPuntos() + 1);
                        } else if (esNumerico(valor)) {
                            listavestibulos.get(lobby).getTablero()[x][y] = "Visible:bandera" + listavestibulos.get(lobby).getLista_usuarios().get(posicion).getColor() + "|" + valor;
                            listavestibulos.get(lobby).getLista_usuarios().get(posicion).setPuntos(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getPuntos() - 1);
                        }
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setBanderas(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getBanderas() - 1);
                    }
                } else {
                    //Si está Visible
                    String bandera = listavestibulos.get(lobby).getTablero()[x][y].substring(8);
                    if (bandera.startsWith("bandera")) {
                        int jugador = Integer.parseInt(bandera.substring(7, bandera.indexOf("|")));
                        String valor = bandera.substring(bandera.indexOf("|") + 1);
                        if (jugador == posicion) {
                            if (valor.equals("mina")) {
                                listavestibulos.get(lobby).getTablero()[x][y] = bomba;
                                listavestibulos.get(lobby).getLista_usuarios().get(posicion).setPuntos(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getPuntos() - 1);
                            } else if (esNumerico(valor)) {
                                listavestibulos.get(lobby).getTablero()[x][y] = "Oculta:" + valor;
                                listavestibulos.get(lobby).getLista_usuarios().get(posicion).setPuntos(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getPuntos() + 1);
                            }
                        }
                        listavestibulos.get(lobby).getLista_usuarios().get(posicion).setBanderas(listavestibulos.get(lobby).getLista_usuarios().get(posicion).getBanderas() + 1);
                    }
                }
            }
        }
        VEFJUGADOR(out, lobby);
    }

    public static void VEFJUGADOR(PrintWriter out, int lobby) {
        int vivos = 0;
        for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
            if (usuario.getVivo()) {
                vivos++;
            }
        }
        if (vivos == 0) {
            limpiarTablero(lobby, out);
        }
        int ocultas = 0;
        for (String[] row : listavestibulos.get(lobby).getTablero()) {
            for (String x : row) {
                if (x.startsWith("Oculta")) {
                    ocultas++;
                }
            }
        }
        if (ocultas == 0) {
            limpiarTablero(lobby, out);
        }
    }

    public static void FINALP(PrintWriter out, int lobby) {
        for (Usuarios usuario_mgs : listavestibulos.get(lobby).getLista_usuarios()) {
            if (usuario_mgs.getIniciado()) {
                usuario_mgs.getEscritor().println("LIMPIAR_PUNTOS");
                for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
                    usuario_mgs.getEscritor().println("PUNTOS" + usuario.getNombre() + "= " + usuario.getPuntos() + " Puntos");
                }
            }
        }
        for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
            usuario.getEscritor().println("FIN");
        }
    }

    public static boolean esNumerico(String mensaje) {
        boolean numerico = false;
        try {
            int num = Integer.parseInt(mensaje);
            numerico = true;
        } catch (Exception e) {
        }
        return numerico;
    }

    public static void DES0DIAG(int filas, int columnas, int lobby, int posicion, PrintWriter out) {
        if (listavestibulos.get(lobby).getTablero()[filas][columnas].equals("Oculta:0")) {

            //Primera fila
            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas - 1, columnas - 1, lobby, posicion, out);
            } catch (Exception e) {
            }

            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas, columnas - 1, lobby, posicion, out);
            } catch (Exception e) {
            }

            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas + 1, columnas - 1, lobby, posicion, out);
            } catch (Exception e) {
            }

            //Segunda fila
            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas - 1, columnas, lobby, posicion, out);
            } catch (Exception e) {
            }

            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas + 1, columnas, lobby, posicion, out);
            } catch (Exception e) {
            }

            //Tercera fila
            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas - 1, columnas + 1, lobby, posicion, out);
            } catch (Exception e) {
            }

            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas, columnas + 1, lobby, posicion, out);
            } catch (Exception e) {
            }

            try {
                actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
                DES0DIAG(filas + 1, columnas + 1, lobby, posicion, out);
            } catch (Exception e) {
            }

        } else {
            actualizarCeldaTablero(filas, columnas, lobby, posicion, out);
        }
    }

    public static void actualizarCeldaTablero(int f, int c, int lobby, int posicion, PrintWriter out) {
        if (listavestibulos.get(lobby).getTablero()[f][c].startsWith("Oculta")) {
            if (esNumerico(listavestibulos.get(lobby).getTablero()[f][c].substring(7))) {
                listavestibulos.get(lobby).getTablero()[f][c] = "Visible:" + listavestibulos.get(lobby).getTablero()[f][c].substring(7) + "/" + listavestibulos.get(lobby).getLista_usuarios().get(posicion).getColor();
                for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
                    usuario.getEscritor().println("ACTUALIZAR=" + f + "," + c + ";" + listavestibulos.get(lobby).getTablero()[f][c]);
                }
            }
        }
    }

    public static void limpiarTablero(int lobby, PrintWriter out) {
        for (int x = 0; x < filas_juego; x++) {
            for (int y = 0; y < columas_juego; y++) {
                if (listavestibulos.get(lobby).getTablero()[x][y].startsWith("Oculta")) {
                    String contenido = listavestibulos.get(lobby).getTablero()[x][y].substring(7);
                    listavestibulos.get(lobby).getTablero()[x][y] = "Visible:" + contenido;
                    for (Usuarios usuario : listavestibulos.get(lobby).getLista_usuarios()) {
                        usuario.getEscritor().println("ACTUALIZAR=" + x + "," + y + ";" + listavestibulos.get(lobby).getTablero()[x][y]);
                    }
                }
            }
        }
        FINALP(out, lobby);
    }

    /*public static void destaparCerosCruz(int filas, int columnas, int lobby, PrintWriter out) {
        imprimirarreglo(lista_lobbys.get(lobby).getTablero());
        if (!(filas < 0) && !(columnas + 1 > columas_juego) && !(filas + 1 > filas_juego) && !(columnas < 0)) {
            if (lista_lobbys.get(lobby).getTablero()[filas][columnas].equals("Oculta:0")) {
                
                if (!(filas < 0)) {
                    actualizarCelda(filas, columnas, lobby, out);
                    destaparCeros(filas - 1, columnas, lobby, out);
                }
                if (!(columnas + 1 > columas_juego)) {
                    actualizarCelda(filas, columnas, lobby, out);
                    destaparCeros(filas, columnas + 1, lobby, out);
                }
                if (!(filas + 1 > filas_juego)) {
                    actualizarCelda(filas, columnas, lobby, out);
                    destaparCeros(filas + 1, columnas, lobby, out);
                }
                if (!(columnas < 0)) {
                    actualizarCelda(filas, columnas, lobby, out);
                    destaparCeros(filas, columnas - 1, lobby, out);
                }
            }else{
                actualizarCelda(filas, columnas, lobby, out);
            }
        }
    }*/
    public static int BUSCARU(String Nombre, int lobby) {
        int indice = -1;
        for (int x = 0; x < listavestibulos.get(lobby).getLista_usuarios().size(); x++) {
            if (listavestibulos.get(lobby).getLista_usuarios().get(x).getNombre().equals(Nombre)) {
                indice = x;
                break;
            }
        }
        return indice;
    }

    public static int BUSCAR(String id) {
        int indice = -1;
        for (int x = 0; x < listavestibulos.size(); x++) {
            if (listavestibulos.get(x).getID_Unico().equals(id)) {
                indice = x;
                break;
            }
        }
        return indice;
    }

    public static void LIMPIAR(int lobby, int usuario) {
        int color = listavestibulos.get(lobby).getLista_usuarios().get(usuario).getColor();
        for (int x = 0; x < filas_juego; x++) {
            for (int y = 0; y < columas_juego; y++) {
                if (listavestibulos.get(lobby).getTablero()[x][y].startsWith("Visible:bandera" + color)) {
                    listavestibulos.get(lobby).getTablero()[x][y] = "Oculta:" + listavestibulos.get(lobby).getTablero()[x][y].substring(listavestibulos.get(lobby).getTablero()[x][y].indexOf("|"));
                    int admin = 0;
                    for (Usuarios usuarios : listavestibulos.get(lobby).getLista_usuarios()) {
                        usuarios.getEscritor().println("ACTUALIZAR=" + x + "," + y + ";" + listavestibulos.get(lobby).getTablero()[x][y]);
                        if (admin == 0) {
                            usuarios.getEscritor().println("ADMIN");
                            admin++;
                        } else {
                            usuarios.getEscritor().println("NOADMIN");
                        }
                    }
                }
            }
        }
    }

}
