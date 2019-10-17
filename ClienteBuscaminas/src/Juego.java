
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Juego extends JFrame implements ActionListener, MouseListener{
    public static JButton tabla_botones[][];
    int n;
    int m;
    int minas;
    PrintWriter out;
    int color_jugador;
    
    JPanel panel= new JPanel();
    JPanel jugadores= new JPanel();
    JLabel lblJ1= new JLabel();
    
    Color amarillo = Color.decode("#BCD807");
    Color azul = Color.decode("#5DBCD2");
    Color verde = Color.decode("#23B14D");
    Color morado = Color.decode("#A349A3");

    public Juego(int n, int m, int minas, PrintWriter printWriter, int color) {
        if(n!=m)
            return;
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.n = n;
        this.m = m;
        this.minas = minas;
        this.out= printWriter;
        color_jugador= color;
        tabla_botones = new JButton [n][m];
        //this.setSize(new Dimension(1024, 720));
        panel.setLayout(new GridLayout(n,m));
        //jugadores.setSize(new Dimension(100, 720));
        jugadores.setLayout(new GridLayout(1,4));
        Font arial = new Font("Arial", Font.BOLD, 20);
        lblJ1.setFont(arial);
        lblJ1.setHorizontalAlignment(SwingConstants.CENTER);
        switch(color_jugador){
            case 0:
                lblJ1.setForeground(amarillo);
                lblJ1.setText(Principal.jugador + " (Izquierda)");
                break;
            case 1:
                lblJ1.setForeground(azul);
                lblJ1.setText(Principal.jugador + " (Arriba)");
                break;
            case 2:
                lblJ1.setForeground(verde);
                lblJ1.setText(Principal.jugador + " (Derecha)");
                break;
            case 3:
                lblJ1.setForeground(morado);
                lblJ1.setText(Principal.jugador + " (Abajo)");
                break;
        }
        jugadores.add(lblJ1);
        
        //setLayout(new GridLayout(n,m));
        for (int x = 0;x<m;x++){
            for (int y = 0;y<n;y++){
                tabla_botones[x][y] = new JButton("");
                tabla_botones[x][y].addActionListener(this);
                tabla_botones[x][y].addMouseListener(this);
                tabla_botones[x][y].setName(x + "," + y);
                tabla_botones[x][y].setSize(new Dimension(30,30));
            
                try{
                ImageIcon usuario= new ImageIcon(getClass().getResource("/campo.png"));
               
                ImageIcon icono= new ImageIcon(usuario.getImage().getScaledInstance(Juego.tabla_botones[x][y].getWidth(), Juego.tabla_botones[x][y].getHeight(), Image.SCALE_DEFAULT));
                tabla_botones[x][y].setIcon(icono);
                tabla_botones[x][y].setMargin(new Insets(0, 0, 0, 0));
                tabla_botones[x][y].setBackground(Color.WHITE);
                tabla_botones[x][y].setContentAreaFilled(false);
                tabla_botones[x][y].setFocusPainted(false);
                tabla_botones[x][y].setBorder(new LineBorder(Color.WHITE));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                tabla_botones[x][y].setEnabled(true);
                panel.add(tabla_botones[x][y]);
            }//end inner for
        }//end for
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(jugadores, BorderLayout.NORTH);
        this.pack();
        this.setVisible(true);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        int result = JOptionPane.showConfirmDialog(null, "CONFIRMAR", "SALIR", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
          System.exit(0);
      }
    });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        String ubicacion= e.toString().substring(e.toString().indexOf("]")+5);
        int x= Integer.parseInt(ubicacion.substring(0,ubicacion.indexOf(",")));
        int y= Integer.parseInt(ubicacion.substring(ubicacion.indexOf(",")+1));
        
        
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            out.println("/Partida=" + x + "," + y + ";" + "IZQ]");
            
        }
        
        if (e.getButton() == MouseEvent.BUTTON3) {
            out.println("/Partida=" + x + "," + y + ";" + "DER]");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
}
