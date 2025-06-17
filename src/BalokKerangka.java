import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

// === Definisi Vertex ===
// Class untuk merepresentasikan vertex (titik) 3D
class Vertex {
    double x, y, z;
    Vertex(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
}

// === Definisi Matriks 3×3 ===
class Matrix3 {
    double[][] m;
    Matrix3(double[][] m) { this.m = m; }

    // transform(Vertex v): transformasi matriks ke vertex
    Vertex transform(Vertex v) {
        double nx = m[0][0]*v.x + m[0][1]*v.y + m[0][2]*v.z;
        double ny = m[1][0]*v.x + m[1][1]*v.y + m[1][2]*v.z;
        double nz = m[2][0]*v.x + m[2][1]*v.y + m[2][2]*v.z;
        return new Vertex(nx, ny, nz);
    }

    // multiply: menggabungkan 2 matriks menjadi satu (this * other)
    Matrix3 multiply(Matrix3 other) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    r[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return new Matrix3(r);
    }
}

public class BalokKerangka extends JPanel implements KeyListener {
    // === Array Vertex (titik 3D) ===
    private final Vertex[] points;  // <-- Inilah 'vertex' utama
    private final int[][] faces;
    private double angleX = 0, angleY = 0;

    public BalokKerangka() {
        // Inisialisasi 8 titik sudut balok sebagai Vertex
        points = new Vertex[] {
            new Vertex(-2, -1, -0.75),  // sudut 0
            new Vertex( 2, -1, -0.75),  // sudut 1
            new Vertex( 2,  1, -0.75),  // sudut 2
            new Vertex(-2,  1, -0.75),  // sudut 3
            new Vertex(-2, -1,  0.75),  // sudut 4
            new Vertex( 2, -1,  0.75),  // sudut 5
            new Vertex( 2,  1,  0.75),  // sudut 6
            new Vertex(-2,  1,  0.75)   // sudut 7
        };
        faces = new int[][] { {0,1,2,3}, {5,4,7,6}, {4,5,1,0}, {3,2,6,7}, {1,5,6,2}, {4,0,3,7} };
        setPreferredSize(new Dimension(600,600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        new javax.swing.Timer(16, e -> repaint()).start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int cx = getWidth()/2, cy = getHeight()/2, scale = 100;

        // Mengatur kualitas rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // === Transformasi Matriks ===
        // 1) Matriks rotasi sumbu X
        Matrix3 rotX = new Matrix3(new double[][] {
            {1, 0, 0},
            {0, Math.cos(angleX), -Math.sin(angleX)},
            {0, Math.sin(angleX),  Math.cos(angleX)}
        });
        // 2) Matriks rotasi sumbu Y
        Matrix3 rotY = new Matrix3(new double[][] {
            { Math.cos(angleY), 0, Math.sin(angleY)},
            { 0,                1, 0               },
            {-Math.sin(angleY), 0, Math.cos(angleY)}
        });
        // 3) Gabungkan: transform = rotY * rotX
        Matrix3 transform = rotY.multiply(rotX);  // <-- Inilah 'transformasi matrix'

        // 4) Terapkan transformasi ke tiap vertex
        Vertex[] r = new Vertex[points.length];
        for (int i = 0; i < points.length; i++) {
            r[i] = transform.transform(points[i]);  // <-- Transformasi via Matrix3.transform()
        }

        // ... (lanjut perhitungan back-face culling dan proyeksi)
        boolean[] faceVisible = new boolean[faces.length];
        for (int i = 0; i < faces.length; i++) {
            Vertex p0 = r[faces[i][0]], p1 = r[faces[i][1]], p2 = r[faces[i][2]];
            double[] v1 = {p1.x-p0.x, p1.y-p0.y, p1.z-p0.z};
            double[] v2 = {p2.x-p0.x, p2.y-p0.y, p2.z-p0.z};
            double[] n  = { v1[1]*v2[2] - v1[2]*v2[1], v1[2]*v2[0] - v1[0]*v2[2], v1[0]*v2[1] - v1[1]*v2[0] };
            faceVisible[i] = (n[2] * -1) > 0;
        }
        Set<String> edgesToDraw = new HashSet<>();
        for (int i = 0; i < faces.length; i++) {
            if (!faceVisible[i]) continue;
            int[] f = faces[i];
            for (int j = 0; j < f.length; j++) {
                int a = f[j], b = f[(j+1)%f.length];
                String key = a < b ? a+"-"+b : b+"-"+a;
                edgesToDraw.add(key);
            }
        }
        Point[] proj = new Point[r.length];
        for (int i = 0; i < r.length; i++) {
            int x = (int)Math.round(r[i].x * scale) + cx;
            int y = (int)Math.round(-r[i].y * scale) + cy;
            proj[i] = new Point(x,y);
        }
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.MAGENTA.darker());
        for (String key : edgesToDraw) {
            String[] p = key.split("-");
            Point p1 = proj[Integer.parseInt(p[0])], p2 = proj[Integer.parseInt(p[1])];
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    @Override public void keyPressed(KeyEvent e) { double speed = 0.1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> angleX += speed;
            case KeyEvent.VK_S -> angleX -= speed;
            case KeyEvent.VK_A -> angleY += speed;
            case KeyEvent.VK_D -> angleY -= speed;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kerangka Balok with Matrix");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new BalokKerangka());
            frame.pack(); frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
