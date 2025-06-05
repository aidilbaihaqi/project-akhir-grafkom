import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class KerangkaBalok extends JPanel implements KeyListener {
    private double[][] points;
    private int[][] edges;
    private double angleX = 0;
    private double angleY = 0;

    public KerangkaBalok() {
        // Titik-titik sudut untuk balok (dimensi X, Y, Z)
        points = new double[][]{
            {-2, -1, -0.75}, {2, -1, -0.75}, {2, 1, -0.75}, {-2, 1, -0.75}, // sisi depan
            {-2, -1, 0.75}, {2, -1, 0.75}, {2, 1, 0.75}, {-2, 1, 0.75} // sisi belakang
        };

        // Garis (edges) antar titik (menghubungkan sisi depan, belakang, dan samping)
        edges = new int[][]{
            {0, 1},{1, 2},{2, 3},{3, 0}, // sisi depan
            {4, 5},{5, 6},{6, 7},{7, 4}, // sisi belakang
            {0, 4},{1, 5},{2, 6},{3, 7}  // sisi samping
        };

        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        Timer timer = new Timer(16, e -> repaint()); // refresh 60 FPS
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int scale = 100;

        // Hitung rotasi semua titik
        double[][] rotated = new double[8][3];
        for (int i = 0; i < points.length; i++) {
            rotated[i] = rotateY(rotateX(points[i], angleX), angleY);
        }

        // Proyeksikan ke layar
        Point[] projected = new Point[8];
        for (int i = 0; i < rotated.length; i++) {
            int x = (int)(rotated[i][0] * scale) + centerX;
            int y = (int)(-rotated[i][1] * scale) + centerY;
            projected[i] = new Point(x, y);
        }

        // Gambar semua garis
        g2.setColor(Color.MAGENTA.darker());
        for (int[] edge : edges) {
            Point p1 = projected[edge[0]];
            Point p2 = projected[edge[1]];
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    // Fungsi rotasi terhadap sumbu X
    private double[] rotateX(double[] p, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double y = p[1] * cos - p[2] * sin;
        double z = p[1] * sin + p[2] * cos;
        return new double[]{p[0], y, z};
    }

    // Fungsi rotasi terhadap sumbu Y
    private double[] rotateY(double[] p, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = p[0] * cos + p[2] * sin;
        double z = -p[0] * sin + p[2] * cos;
        return new double[]{x, p[1], z};
    }

    // Keyboard handler untuk kontrol rotasi
    @Override
    public void keyPressed(KeyEvent e) {
        double speed = 0.1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> angleX += speed;
            case KeyEvent.VK_S -> angleX -= speed;
            case KeyEvent.VK_A -> angleY += speed;
            case KeyEvent.VK_D -> angleY -= speed;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Main method untuk menjalankan aplikasi
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kerangka Balok");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new KerangkaBalok());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
