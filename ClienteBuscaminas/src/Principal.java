
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Image;
import java.io.File;

public class Principal {
    
    String serverAdress;
    Scanner in;
    PrintWriter out;
    JFrame frame= new JFrame("-BUSCAMINAS-");
    JTextArea messageUsuarios= new JTextArea(15, 20);
    JButton btnIniciar= new JButton("-ENTRAR-");
    String puntaje= "";
    static Juego juego;
    static String jugador;
    static int n=0;
    static int m=0;
    
    
    public static void main(String[] args) throws Exception {
        Principal cliente= new Principal();
        cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cliente.frame.setVisible(true);
        cliente.run();
    }
    
    public Principal(){
        btnIniciar.setEnabled(false);
        messageUsuarios.setEditable(false);
        Font arial = new Font("Arial", Font.BOLD, 20);
        messageUsuarios.setFont(arial);
        messageUsuarios.setForeground(Color.MAGENTA);
        frame.getContentPane().add(messageUsuarios, BorderLayout.CENTER);
        frame.getContentPane().add(btnIniciar, BorderLayout.SOUTH);
        frame.setResizable(true);
        frame.pack();
        
        btnIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println("/JUGAR");
            }
        });
        
        while (true) {            
        try{
            serverAdress= getIP();
            if(checarIP(serverAdress)){
               Socket socket= new Socket(serverAdress, 59001);
            break;
            }else{
                System.exit(0);
            }
        }catch(Exception e){
            
             System.exit(0);
            
        }
      }
    }
    
    private String getNombre(){
        return JOptionPane.showInputDialog(frame, "JUGADOR: ", "-INGRESA NOMBRE JUGADOR-", JOptionPane.PLAIN_MESSAGE);
    }
    private String getIP(){
        return  
                JOptionPane.showInputDialog(null, "DIRECCION IP: ", "-IP SERVIDOR-", JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        try{
            Socket socket= new Socket(serverAdress, 59001);
            in= new Scanner(socket.getInputStream());
            out= new PrintWriter(socket.getOutputStream(), true);
            while (in.hasNextLine()) {
                String mensaje_recibido= in.nextLine();
                if(mensaje_recibido.startsWith("SUBMITNAME")){
                    jugador= getNombre();
                    out.println(jugador);
                } else if(mensaje_recibido.startsWith("NAMEACCEPTED")){
                    this.frame.setTitle("BIENVENIDO - " + mensaje_recibido.substring(13));
                } else if(mensaje_recibido.equals("ADMIN")){
                        btnIniciar.setEnabled(true);
                } else if(mensaje_recibido.equals("NOADMIN")){
                        btnIniciar.setEnabled(false);
                } else if(mensaje_recibido.equals("LIMPIAR_LISTA")){
                        messageUsuarios.setText("");
                        messageUsuarios.append("MINIMO 2 JUGADORES PARA INICIAR EL JUEGO\n\n ");
                }
                else if(mensaje_recibido.startsWith("LIST")){
                    messageUsuarios.append(mensaje_recibido.substring(5) + "\n");
                } else if(mensaje_recibido.startsWith("INICIAR")){
                    JUEGO(mensaje_recibido, out);
                }else if(mensaje_recibido.startsWith("ACTUALIZAR")){
                    if(Juego.tabla_botones!=null){
                        int x= Integer.parseInt(mensaje_recibido.substring(mensaje_recibido.indexOf("=")+1, mensaje_recibido.indexOf(",")));
                        int y= Integer.parseInt(mensaje_recibido.substring(mensaje_recibido.indexOf(",")+1, mensaje_recibido.indexOf(";")));
                        String estado= mensaje_recibido.substring(mensaje_recibido.indexOf(";")+1, mensaje_recibido.indexOf(":"));
                        String resultado= "";
                        int color= -1;
                        if(mensaje_recibido.contains("/")){
                            resultado= (mensaje_recibido.substring(mensaje_recibido.indexOf(":")+1, mensaje_recibido.indexOf("/")));
                            color= Integer.parseInt(mensaje_recibido.substring(mensaje_recibido.indexOf("/")+1));
                        }else{
                            resultado= (mensaje_recibido.substring(mensaje_recibido.indexOf(":")+1));
                        }
                        if(estado.equals("Oculta")){
                                ImageIcon usuario= new ImageIcon(getClass().getResource("/campo.png"));
                               
                                ImageIcon icono= new ImageIcon(usuario.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                                Juego.tabla_botones[x][y].setIcon(icono);
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setBackground(Color.WHITE);
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.WHITE));
                                Juego.tabla_botones[x][y].setText("");
                        }else if(estado.equals("Visible")){
                            Juego.tabla_botones[x][y].setIcon(null);
                            if(esNumerico(resultado)){
                                if(resultado.equals("0")){
                              
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.BLACK));
                                Juego.tabla_botones[x][y].setText("");
                                }else{
                                
                                switch(resultado){
                                    case "1":
                                        Juego.tabla_botones[x][y].setForeground(Color.RED);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                        break;
                                    case "2":
                                        Juego.tabla_botones[x][y].setForeground(Color.BLUE);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.BLUE));
                                        break;
                                    case "3":
                                        Color verde = Color.decode("#1B7344");
                                        Juego.tabla_botones[x][y].setForeground(verde);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(verde));
                                        break;
                                    case "4":
                                        Color morado = Color.decode("#681B85");
                                        Juego.tabla_botones[x][y].setForeground(morado);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(morado));
                                        break;
                                    case "5":
                                        Color dorado = Color.decode("#857A1B");
                                        Juego.tabla_botones[x][y].setForeground(dorado);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(dorado));
                                        break;
                                    case "6":
                                        Color cafe = Color.decode("#8E672D");
                                        Juego.tabla_botones[x][y].setForeground(cafe);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(cafe));
                                        break;
                                    case "7":
                                        Color rosa = Color.decode("#C1055B");
                                        Juego.tabla_botones[x][y].setForeground(rosa);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(rosa));
                                        break;
                                    case "8":
                                        Juego.tabla_botones[x][y].setForeground(Color.BLACK);
                                        Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.BLACK));
                                        break;
                                }
                                Juego.tabla_botones[x][y].setText(resultado);
                                }
                            }else if(resultado.equals("mina")){
                                ImageIcon usuario= new ImageIcon(getClass().getResource("/mina.png")) ;
                                ImageIcon icono= new ImageIcon(usuario.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_AREA_AVERAGING));
                                Juego.tabla_botones[x][y].setIcon(icono);
                                
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                Juego.tabla_botones[x][y].setText("");
                            }else if(resultado.startsWith("bandera")){
                                int jugador= Integer.parseInt(resultado.substring(7, resultado.indexOf("|")));
                                switch(jugador){
                                    case 0:
                                        ImageIcon bandera1= new ImageIcon(getClass().getResource("/banderaJ1.png"));
                                ImageIcon iconobandera1= new ImageIcon(bandera1.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                                Juego.tabla_botones[x][y].setIcon(iconobandera1);
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setBackground(Color.YELLOW);
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                Juego.tabla_botones[x][y].setText("");
                                        break;
                                        case 1:
                                            ImageIcon bandera2= new ImageIcon(getClass().getResource("/banderaJ2.png"));
                                ImageIcon iconobandera2= new ImageIcon(bandera2.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                                Juego.tabla_botones[x][y].setIcon(iconobandera2);
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setBackground(Color.YELLOW);
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                Juego.tabla_botones[x][y].setText("");
                                        break;
                                        case 2:
                                            ImageIcon bandera3= new ImageIcon(getClass().getResource("/banderaJ3.png"));
                                ImageIcon iconobandera3= new ImageIcon(bandera3.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                                Juego.tabla_botones[x][y].setIcon(iconobandera3);
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setBackground(Color.YELLOW);
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                Juego.tabla_botones[x][y].setText("");
                                        break;
                                        case 3:
                                            ImageIcon bandera4= new ImageIcon(getClass().getResource("/banderaJ4.png"));
                                ImageIcon iconobandera4= new ImageIcon(bandera4.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                                Juego.tabla_botones[x][y].setIcon(iconobandera4);
                                Juego.tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                                Juego.tabla_botones[x][y].setBackground(Color.YELLOW);
                                Juego.tabla_botones[x][y].setContentAreaFilled(false);
                                Juego.tabla_botones[x][y].setFocusPainted(false);
                                Juego.tabla_botones[x][y].setBorder(new LineBorder(Color.RED));
                                Juego.tabla_botones[x][y].setText("");
                                        break;
                                }   
                            }
                        }
            }
                }else if(mensaje_recibido.startsWith("MUERTO")){
                    JOptionPane.showMessageDialog(null, "-MUERTO :0-");
            } else if(mensaje_recibido.equals("LIMPIAR_PUNTOS")){
                        puntaje= "";
                } else if(mensaje_recibido.startsWith("PUNTOS")){
                        puntaje= puntaje + mensaje_recibido.substring(6) + "\n";
                } else if(mensaje_recibido.equals("FIN")){
                    if(juego!=null){
                    JOptionPane.showMessageDialog(null, puntaje);
                    juego.dispose();
                    juego= null;
                    }
            }
                
            }
        } finally { 
            
            
            frame.setVisible(false);
            frame.dispose();
        }
    }
    
    public static void JUEGO(String mensaje, PrintWriter out){
        int x= Integer.parseInt(mensaje.substring(mensaje.indexOf("=")+1, mensaje.indexOf(",")));
        int y= Integer.parseInt(mensaje.substring(mensaje.indexOf(",")+1, mensaje.indexOf(";")));
        n= x;
        m= y;
        int minas= Integer.parseInt(mensaje.substring(mensaje.indexOf(";")+1, mensaje.indexOf("]")));
        int color= Integer.parseInt(mensaje.substring(mensaje.indexOf("]")+1));
        
        if(juego==null){
        juego= new Juego(x, y, minas, out, color);
        juego.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //juego.setVisible(true);
        juego.setTitle("JUGADOR:  " + jugador);
        }else{
            JOptionPane.showMessageDialog(null, "NUEVO TABLERO");
            juego= new Juego(x, y, minas, out, color);
            juego.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            juego.setVisible(true);
            juego.setTitle("JUGADOR: " + jugador);
        }
    }
    
    public static void TABLERO(String texto, int x, int y){
        String linea= texto.substring(texto.indexOf("]")+1);
        int[][] cuadricula= new int[x][y];
        StringTokenizer tokens=new StringTokenizer(linea, " ");
        int fila=0;
        int columna=0;
        while(tokens.hasMoreTokens()){
            String str=tokens.nextToken();
            cuadricula[fila][columna]= Integer.parseInt(str);
            columna++;
            if(columna==x){
                fila++;
                columna=0;
            }
        }
    }
    
    public static boolean esNumerico(String mensaje){
        boolean numerico= false;
        try {
            int num= Integer.parseInt(mensaje);
            numerico= true;
        } catch (Exception e) {
        }
        return numerico;
    }
    public static boolean checarIP(final String ip) {
        String patron = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(patron);
    }
}
