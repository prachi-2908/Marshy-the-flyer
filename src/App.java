import javax.swing.*;
public class App 
{
    public static void main(String args[]) throws Exception {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Marshy the flyer");
        frame.setVisible(true); 
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MarshyTheFlyer marshy = new MarshyTheFlyer();
        frame.add(marshy);
        frame.pack();
        marshy.requestFocus();
        frame.setVisible(true);

    }
}