import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Main {
    private static Particle[] particles;
    private enum Mode {Balls, SmallParticles} //Available modes.

    //Parameters for configurations:
    public final static int width = 800; //Width of the panel.
    public final static int height = 800; //Height of the panel.

    private final static Mode mode = Mode.Balls; //Selected mode.

    private final static int numberOfBalls = 10; //Used in Balls mode.
    private final static int numberOfSmallParticles = 500; //Used in Small Particles mode.

    private final static int dt = 16; //Time difference between each frame (in milliseconds).
    //End of configuration parameters.

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Particle Collision Simulation");
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); //To terminate the process when the window is closed.

        if (mode == Mode.Balls)
            createBalls();
        else
            createSmallParticles();


        JPanel jPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.white);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //To enable the anti aliasing.
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); //To enable the anti aliasing for texts.

                //When called, every particle is drawn at its position.
                for (int i = 0; i < particles.length; i++) {
                    int radius = particles[i].getRadius();
                    g2d.setColor(particles[i].getColor());
                    g2d.fillOval((int) particles[i].getxLocation() - radius, (int) particles[i].getyLocation() - radius, radius * 2, radius * 2);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }
        };

        jFrame.getContentPane().add(jPanel);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null); //To make the window centered.

        while (true) {
            long time = System.currentTimeMillis();
            Physics.update(particles, dt); //Call the update method to perform collision checks and necessary calculations.
            jFrame.repaint(); //Then, repaint the frame. By this way, paintComponent method is called.
            int timeToSleep = dt - (int) (System.currentTimeMillis() - time); //Needed time to sleep is target - elapsed time.

            //Calculations might have lasted longer than dt, in such a case this value will be negative. We need to avoid negative values.
            //Nevertheless, the time gap might still be longer than dt.
            if (timeToSleep < 0)
                timeToSleep = 0;

            try {
                Thread.sleep(timeToSleep); //Wait to create the time gap between frames.
            } catch (InterruptedException e) {}
        }
    }

    //Creates balls and fills the particles array with them.
    private static void createBalls() {
        particles = new Particle[numberOfBalls];
        Physics.initializeCollisionMatrix(numberOfBalls);
        Random random = new Random();
        for (int i = 0; i < particles.length; i++) {
            int radius = random.nextInt(10) + 15;
            int x = random.nextInt(width - 2 * radius) + radius;
            int y = random.nextInt(height - 2 * radius) + radius;
            //The unit for velocity is pixels per second.
            double xVel = random.nextInt(1000) - 500;
            double yVel = random.nextInt(1000) - 500;
            int red = random.nextInt(220);
            int green = random.nextInt(220);
            int blue = random.nextInt(220);
            particles[i] = new Particle(x, y, xVel, yVel, radius, new Color(red, green, blue));
        }
    }

    //Creates small particles and fills the particles array with them.
    private static void createSmallParticles() {
        particles = new Particle[numberOfSmallParticles];
        Physics.initializeCollisionMatrix(numberOfSmallParticles);
        Random random = new Random();
        Color color = new Color(50, 60, 120);
        for (int i = 0; i < particles.length; i++) {
            int radius = 5;
            int x = random.nextInt(width - 2 * radius) + radius;
            int y = random.nextInt(height - 2 * radius) + radius;
            //The unit for velocity is pixels per second.
            double xVel = random.nextInt(500) - 250;
            double yVel = random.nextInt(500) - 250;
            particles[i] = new Particle(x, y, xVel, yVel, radius, color);
        }
    }
}
