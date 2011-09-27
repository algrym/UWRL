package org.toadking.games.underwaterroguelike;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 1L;
    private static final int PWIDTH = 1024;
    private static final int PHEIGHT = 768;
    private static final int NO_DELAYS_PER_YIELD = 16;
    private static int MAX_FRAME_SKIPS = 2;
    private static int period = 10; // period between drawing in ms

    private LevelMap currentLevel;

    private Thread animator;
    private volatile boolean running = false;
    private volatile boolean gameOver = false;
    private volatile boolean isPaused = false;

    private volatile boolean keepMoving = false;
    private volatile int lastKeyCode = 0;
    private volatile MapVector lastMoveDirection;

    // global variables for off-screen rendering
    private Graphics2D dbg2D;
    private BufferedImage dbImage2D = null;
    private GraphicsConfiguration gc;

    // record stats every 1 second (roughly) in millisecs
    private static long MAX_STATS_INTERVAL = 10000L;

    // period between drawing in ms
    private static int NUM_FPS = 10;

    // used for gathering statistics
    private long statsInterval = 0L;
    private long prevStatsTime;
    private long totalElapsedTime = 0L;
    private long frameCount = 0;
    private double fpsStore[];
    private long statsCount = 0;
    private double averageFPS = 0.0; // in ms

    private DecimalFormat df = new DecimalFormat("0.##"); // 2 dp
    private DecimalFormat timedf = new DecimalFormat("0.####"); // 4 dp

    private void reportStats() {
	frameCount++;
	statsInterval += period;

	// Only display stats once per MAX_STATS_INTERVAL
	if (statsInterval >= MAX_STATS_INTERVAL) {
	    long timeNow = System.nanoTime();
	    long realElapsedTime = timeNow - prevStatsTime;

	    // time since last stats collection
	    totalElapsedTime += realElapsedTime;
	    long sInterval = (long) statsInterval * 1000000L; // ms --> ns
	    double timingError = ((double) (realElapsedTime - sInterval))
		    / sInterval * 100.0;
	    double actualFPS = 0; // calculate the latest FPS

	    if (totalElapsedTime > 0)
		actualFPS = (((double) frameCount / totalElapsedTime) * 1000000000L);

	    // store the latest FPS
	    fpsStore[(int) statsCount % NUM_FPS] = actualFPS;
	    statsCount = statsCount + 1;

	    double totalFPS = 0.0; // total the stored FPSs
	    for (int i = 0; i < NUM_FPS; i++)
		totalFPS += fpsStore[i];

	    if (statsCount < NUM_FPS) // obtain the average FPS
		averageFPS = totalFPS / statsCount;
	    else
		averageFPS = totalFPS / NUM_FPS;

	    System.out
		    .println("Display stats: Interval="
			    + timedf.format((double) statsInterval / 1000)
			    + "s realElapsedTime="
			    + timedf.format((double) realElapsedTime / 1000000000L)
			    + "s timingError=" + df.format(timingError)
			    + "% frameCount=" + frameCount + "c actualFPS="
			    + df.format(actualFPS) + " avgFPS="
			    + df.format(averageFPS));

	    prevStatsTime = timeNow;
	    statsInterval = 0L; // reset
	}
    } // end of reportStats()

    public void run() {
	/*
	 * Repeatedly update, render, sleep so loop takes close to period
	 * milliseconds. Sleep inaccuracies are handled. The timing calculations
	 * use the Java 3D time.
	 * 
	 * Overruns in update/renders will cause extra updates to be carried out
	 * so UPS is as close as possible to requested FPS.
	 */

	// From:
	// http://www.rednovalabs.com/simple-easy-accurate-collision-detection-part-1/
	// - Process player input
	// - Update all game objects, process AI, move camera, etc.
	// - Detection any / all collisions, then respond
	// - Draw / Render scene

	long beforeTime, timeDiff, sleepTime, afterTime;
	long overSleepTime = 0L;
	int noDelays = 0;
	long excess = 0L;

	beforeTime = System.nanoTime();

	running = true;
	while (running) {
	    gameUpdate();
	    gameRender();
	    paintScreen();

	    afterTime = System.nanoTime();
	    timeDiff = afterTime - beforeTime;
	    sleepTime = (period - timeDiff) - overSleepTime;

	    if (sleepTime > 0) // Some time left in this cycle
	    {
		try {
		    Thread.sleep(sleepTime / 1000000L); // nano -> ms
		} catch (InterruptedException ex) {
		}
		overSleepTime = (System.nanoTime() - afterTime) - sleepTime;

	    } else { // sleepTime <= 0; frame took longer than the period
		excess -= sleepTime; // store excess time value
		overSleepTime = 0L;

		if (++noDelays >= NO_DELAYS_PER_YIELD) {
		    Thread.yield(); // give another thread a chance to run
		    noDelays = 0;
		}
	    }

	    beforeTime = System.nanoTime();

	    /*
	     * if frame animation is taking too long, updated the game state
	     * without rendering it, to get the updates/sec nearer to the
	     * required FPS.
	     */
	    int skips = 0;
	    while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
		excess -= period;
		gameUpdate(); // update state but don't render
		skips++;
	    }

	    reportStats(); // record/report statistics
	}
	System.exit(0);
    }

    private void paintScreen() {
	Graphics2D g;

	try {
	    g = (Graphics2D) this.getGraphics();
	    if ((g != null) && (dbImage2D != null))
		g.drawImage(dbImage2D, 0, 0, null);

	    Toolkit.getDefaultToolkit().sync();

	    g.dispose();
	} catch (Exception e) {
	    System.out.println("Graphics context error: " + e);
	}
    }

    public GamePanel() {
	setBackground(Color.white);
	setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

	setFocusable(true);
	requestFocus(); // JPanel now receives key events
	readyForTermination();

	// get this device's graphics configuration
	GraphicsEnvironment ge = GraphicsEnvironment
		.getLocalGraphicsEnvironment();
	gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	// initialize timer stats
	fpsStore = new double[NUM_FPS];
	for (int i = 0; i < NUM_FPS; i++)
	    fpsStore[i] = 0.0;

	prevStatsTime = System.nanoTime();

	// Create game components
	currentLevel = new LevelMap();

	// listen for mouse presses
	addMouseListener(new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {
		currentLevel.movePlayerClick(e.getX(), e.getY());
	    }
	});
    }

    private void readyForTermination() {
	addKeyListener(new KeyAdapter() {
	    // listen for keypresses and move as expected
	    public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		lastKeyCode = keyCode;

		switch (keyCode) {
		case KeyEvent.VK_ESCAPE:
		    running = false;
		    break;

		case KeyEvent.VK_W:
		    lastMoveDirection = currentLevel.movePlayerNorth();
		    if (e.isShiftDown())
			keepMoving = false;
		    else
			keepMoving = true;
		    break;

		case KeyEvent.VK_S:
		    lastMoveDirection = currentLevel.movePlayerSouth();
		    if (e.isShiftDown())
			keepMoving = false;
		    else
			keepMoving = true;
		    break;

		case KeyEvent.VK_A:
		    lastMoveDirection = currentLevel.movePlayerWest();
		    if (e.isShiftDown())
			keepMoving = false;
		    else
			keepMoving = true;
		    break;

		case KeyEvent.VK_D:
		    lastMoveDirection = currentLevel.movePlayerEast();
		    if (e.isShiftDown())
			keepMoving = false;
		    else
			keepMoving = true;
		    break;

		case KeyEvent.VK_SPACE:
		    currentLevel.playerAction();
		    break;

		case KeyEvent.VK_R:
		    currentLevel.zoomIn();
		    break;

		case KeyEvent.VK_F:
		    currentLevel.zoomDefault();
		    break;

		case KeyEvent.VK_V:
		    currentLevel.zoomOut();
		    break;

		default:
		    System.out.println("Unknown key pressed.  keyCode: "
			    + keyCode);
		}
	    }

	    public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode != lastKeyCode)
		    return;
		else
		    keepMoving = false;
	    }
	});
    }

    // Start the game when the panel is added to a container
    public void addNotify() {
	super.addNotify();
	startGame();
    }

    private void startGame() {
	// Initialize and start the game thread
	if (animator == null || !running) {
	    animator = new Thread(this, "Game Panel Animator");
	    animator.start();
	}
	running = true;
    }

    // private void pauseGame() {
    // // called by the user to pause execution
    // isPaused = true;
    // }

    // private void resumeGame() {
    // // called by the user to resume execution
    // isPaused = false;
    // }

    public void stopGame() {
	// called by the user to stop execution
	running = false;
    }

    private void gameUpdate() {
	if (!isPaused && !gameOver) {
	    // update game state
	    if (keepMoving)
		currentLevel.movePlayer(lastMoveDirection);
	    currentLevel.gameUpdate();
	}
    }

    private void gameRender() {
	// draw the current frame to an image buffer
	if (dbImage2D == null) {
	    dbImage2D = gc.createCompatibleImage(PWIDTH, PHEIGHT,
		    BufferedImage.TYPE_INT_ARGB);
	    if (dbImage2D == null) {
		System.out.println("dbImage2D is null!");
		return;
	    } else
		dbg2D = (Graphics2D) dbImage2D.getGraphics();

	    RenderingHints renderHints = new RenderingHints(
		    RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    renderHints.put(RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY);
	    dbg2D.setRenderingHints(renderHints);

	    dbg2D.setFont(new Font("Courier", Font.PLAIN, 16));
	}

	// draw game elements here
	currentLevel.drawMap(dbg2D, PWIDTH, PHEIGHT);
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);

	if (dbImage2D != null)
	    g.drawImage(dbImage2D, 0, 0, null);
    }
}
