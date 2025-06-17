import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

// ini class utama
public class Part1 extends JPanel implements KeyListener {
    private final double[][] points;
    private final int[][] faces;
    private double angleX = 0, angleY = 0;

    public Part1() {
        // 8 titik sudut balok
        points = new double[][] {
            {-2, -1, -0.75}, { 2, -1, -0.75}, { 2,  1, -0.75}, {-2,  1, -0.75},
            {-2, -1,  0.75}, { 2, -1,  0.75}, { 2,  1,  0.75}, {-2,  1,  0.75}
        };
        // Faces untuk kumpulkan edges
        faces = new int[][] {
            {0,1,2,3}, {4,5,6,7},
            {0,1,5,4}, {3,2,6,7},
            {1,2,6,5}, {0,3,7,4}
        };

        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        // javax.swing.Timer untuk repaint ~60FPS
        new javax.swing.Timer(16, e -> repaint()).start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Nyalakan anti-aliasing untuk garis mulus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        int cx = getWidth()/2, cy = getHeight()/2;
        int scale = 100;  // Kembali ke skala awal supaya ukuran "kamera" sama

        // 1) Rotasi titik
        double[][] r = new double[points.length][3];
        for (int i = 0; i < points.length; i++) {
            r[i] = rotateY(rotateX(points[i], angleX), angleY);
        }

        // 2) Kumpulkan semua edge unik
        Set<String> edges = new HashSet<>();
        for (int[] f : faces) {
            for (int j = 0; j < f.length; j++) {
                int a = f[j], b = f[(j+1)%f.length];
                String key = a < b ? a + "-" + b : b + "-" + a;
                edges.add(key);
            }
        }

        // 3) Proyeksi orthografis + rounding
        Point[] proj = new Point[r.length];
        for (int i = 0; i < r.length; i++) {
            int x = (int) Math.round(r[i][0] * scale) + cx;
            int y = (int) Math.round(-r[i][1] * scale) + cy;
            proj[i] = new Point(x, y);
        }

        // 4) Gambar wireframe
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.MAGENTA.darker());
        for (String key : edges) {
            String[] parts = key.split("-");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            Point p1 = proj[u], p2 = proj[v];
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private double[] rotateX(double[] p, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[]{ p[0], p[1]*c - p[2]*s, p[1]*s + p[2]*c };
    }

    private double[] rotateY(double[] p, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[]{ p[0]*c + p[2]*s, p[1], -p[0]*s + p[2]*c };
    }

    @Override
    public void keyPressed(KeyEvent e) {
        double sp = 0.1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> angleX += sp;
            case KeyEvent.VK_S -> angleX -= sp;
            case KeyEvent.VK_A -> angleY += sp;
            case KeyEvent.VK_D -> angleY -= sp;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Balok Wireframe Smooth");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new Part1());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
