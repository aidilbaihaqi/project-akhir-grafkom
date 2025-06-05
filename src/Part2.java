import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Part2 extends JPanel implements KeyListener {
    private double[][] points;
    private int[][] edges;
    private double angleX = 0;
    private double angleY = 0;

    public Part2() {
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

        // Gambar sisi depan terlebih dahulu (sisi yang lebih dekat dengan viewer)
        drawFace(g2, projected, new int[]{0, 1, 2, 3}, Color.RED); // Sisi depan berwarna merah

        // Gambar sisi belakang (hanya digambar jika tidak tertutupi)
        drawFace(g2, projected, new int[]{4, 5, 6, 7}, Color.BLUE); // Sisi belakang berwarna biru

        // Gambar sisi samping kiri (hanya digambar jika tidak tertutupi)
        drawFace(g2, projected, new int[]{0, 4, 7, 3}, Color.GREEN); // Sisi kiri berwarna hijau

        // Gambar sisi samping kanan (hanya digambar jika tidak tertutupi)
        drawFace(g2, projected, new int[]{1, 5, 6, 2}, Color.YELLOW); // Sisi kanan berwarna kuning

        // Gambar sisi atas
        drawFace(g2, projected, new int[]{3, 2, 6, 7}, Color.CYAN); // Sisi atas berwarna cyan

        // Gambar sisi bawah
        drawFace(g2, projected, new int[]{0, 1, 5, 4}, Color.ORANGE); // Sisi bawah berwarna oranye

        // Gambar semua garis tepi (edge) dengan warna hitam
        g2.setColor(Color.BLACK); // Mengatur warna garis menjadi hitam
        for (int[] edge : edges) {
            Point p1 = projected[edge[0]];
            Point p2 = projected[edge[1]];
            g2.drawLine(p1.x, p1.y, p2.x, p2.y); // Menggambar garis
        }
    }

    // Fungsi untuk menggambar sisi dengan warna tertentu
    private void drawFace(Graphics2D g2, Point[] projected, int[] indices, Color faceColor) {
        // Menghitung vektor normal dari sisi untuk deteksi orientasi
        double[] v1 = new double[]{projected[indices[1]].x - projected[indices[0]].x, projected[indices[1]].y - projected[indices[0]].y};
        double[] v2 = new double[]{projected[indices[2]].x - projected[indices[0]].x, projected[indices[2]].y - projected[indices[0]].y};

        // Produk silang untuk menghitung normal
        double normalZ = v1[0] * v2[1] - v1[1] * v2[0];

        // Jika normal mengarah ke viewer (misalnya normalZ positif), gambar sisi
        if (normalZ > 0) {
            g2.setColor(faceColor); // Mengatur warna sisi
            Polygon poly = new Polygon();
            for (int i : indices) {
                poly.addPoint(projected[i].x, projected[i].y);
            }
            g2.fillPolygon(poly); // Mengisi sisi dengan warna
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
        frame.setContentPane(new Part2());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
