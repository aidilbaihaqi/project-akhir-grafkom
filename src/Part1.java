import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

// Class representasi titik 3D
class Vertex {
    double x, y, z;
    Vertex(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
}

// Class matriks 3x3 untuk transformasi
class Matrix3 {
    double[][] m;
    Matrix3(double[][] m) { this.m = m; }

    // Transformasi matriks ke vertex
    Vertex transform(Vertex v) {
        double nx = m[0][0]*v.x + m[0][1]*v.y + m[0][2]*v.z;
        double ny = m[1][0]*v.x + m[1][1]*v.y + m[1][2]*v.z;
        double nz = m[2][0]*v.x + m[2][1]*v.y + m[2][2]*v.z;
        return new Vertex(nx, ny, nz);
    }

    // Gabungkan dua matriks: this * other
    Matrix3 multiply(Matrix3 other) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    r[i][j] += this.m[i][k]*other.m[k][j];
                }
            }
        }
        return new Matrix3(r);
    }
}

public class Part1 extends JPanel implements KeyListener {
    private final Vertex[] points;         // Titik 3D
    private final int[][] faces;            // Definisi faces
    private double angleX = 0, angleY = 0;

    public Part1() {
        points = new Vertex[] {
            new Vertex(-2, -1, -0.75), new Vertex( 2, -1, -0.75),
            new Vertex( 2,  1, -0.75), new Vertex(-2,  1, -0.75),
            new Vertex(-2, -1,  0.75), new Vertex( 2, -1,  0.75),
            new Vertex( 2,  1,  0.75), new Vertex(-2,  1,  0.75)
        };
        faces = new int[][] {
            {0,1,2,3}, {4,5,6,7},
            {0,1,5,4}, {3,2,6,7},
            {1,2,6,5}, {0,3,7,4}
        };
        setPreferredSize(new Dimension(600,600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        new javax.swing.Timer(16, e -> repaint()).start();
    }

    @Override public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);

        int cx = getWidth()/2, cy = getHeight()/2, scale = 100;

        // Buat matriks rotasi X dan Y
        Matrix3 rotX = new Matrix3(new double[][] {
            {1, 0, 0},
            {0, Math.cos(angleX), -Math.sin(angleX)},
            {0, Math.sin(angleX),  Math.cos(angleX)}
        });
        Matrix3 rotY = new Matrix3(new double[][] {
            { Math.cos(angleY), 0, Math.sin(angleY)},
            { 0,                1, 0               },
            {-Math.sin(angleY), 0, Math.cos(angleY)}
        });
        Matrix3 transform = rotY.multiply(rotX);  // Transformasi gabungan

        // Terapkan transformasi ke semua titik
        Vertex[] r = new Vertex[points.length];
        for (int i = 0; i < points.length; i++) {
            r[i] = transform.transform(points[i]);
        }

        // Kumpulkan edges dari faces
        Set<String> edges = new HashSet<>();
        for (int[] f : faces) {
            for (int j = 0; j < f.length; j++) {
                int a = f[j], b = f[(j+1)%f.length];
                String key = a < b ? a+"-"+b : b+"-"+a;
                edges.add(key);
            }
        }

        // Proyeksi orthografis ke 2D
        Point[] proj = new Point[r.length];
        for (int i = 0; i < r.length; i++) {
            int x = (int)Math.round(r[i].x * scale) + cx;
            int y = (int)Math.round(-r[i].y * scale) + cy;
            proj[i] = new Point(x, y);
        }

        // Gambar wireframe
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.MAGENTA.darker());
        for (String key : edges) {
            String[] p = key.split("-");
            Point p1 = proj[Integer.parseInt(p[0])], p2 = proj[Integer.parseInt(p[1])];
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    @Override public void keyPressed(KeyEvent e) {
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
            JFrame frame = new JFrame("Balok Wireframe Smooth with Matrix");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new Part1());
            frame.pack(); frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
