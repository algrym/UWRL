/**
 * 
 */
package org.toadking.games.underwaterroguelike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;

/**
 * @author ajw
 * 
 */

public abstract class Mob extends MapEntity {
    static final float MINMOVETOLERANCE = 0.5f;

    String mobName = "Urist";
    float diameter = 0.75f;
    protected MapVector mapTarget;
    int count = 0;
    boolean canOpendoors = true;

    protected PathVector path;

    public Mob(LevelMap lm) {
	super(lm);
    }

    public String movesLeft() {
	if (path != null)
	    return Integer.toString(path.movesLeft());
	else
	    return "?";
    }

    public void setLoc(int startX, int startY) {
	if (parentMap.collides(startX, startY)) {
	    System.err.println("Should not place Mob(" + mobName
		    + ") in a non-walkable square (" + startX + "," + startY
		    + ")");
	}

	mapLocation = new MapVector(startX, startY);
	mapTarget = mapLocation;
	path = null;

	// TODO: this is probably where we should load the sprite sheet
	// System.out.println("Mob '" + mobName + "' at (" + mapX + ", " + mapY
	// + ")");
    }

    public abstract void draw(Graphics2D g);

    public void moveStop() {
	path = null;
	mapTarget = mapLocation;
    }

    public void moveStep(MapVector d) {
	moveStop();
	mapTarget = mapLocation.add(d);

	if (parentMap.collides(mapTarget))
	    moveStop();
	else
	    path = new PathVector(parentMap, canOpendoors, mapLocation,
		    mapTarget);

	// System.out.println("moveStep: " + mapLocation + " + " + d + " = "
	// + mapTarget);
    }

    public void moveTo(final MapVector newTarget) {
	moveStop();
	mapTarget = newTarget;
	// System.out.println("moveTo: " + mapLocation + " -> " + mapTarget);

	path = new PathVector(parentMap, canOpendoors, mapLocation, mapTarget);
    }

    protected void findPath() {

	// Threading: don't move until the move list is finished

	// System.out.println("From " + mapLocation + " to " + mapTarget +
	// " in "
	// + path.getExpandedCounter() + " tries via " + moveList);
    }

    protected void updatePosition() {
	// Attempting A* as per
	// http://www.policyalmanac.org/games/aStarTutorial.htm

	// Do we need to move?
	if (mapTarget.equals(mapLocation))
	    return; // we're where we should be

	// if the path is null, we can't get there from here

	// Just return if there is nowhere to go
	MapVector next;
	if (path != null)
	    next = path.getNextMove();
	else
	    return;

	if (next == null)
	    return;

	// Determine which direction we should be facing
	facing = mapLocation.nearestDirection(next);

	// Do we need to open a door before moving?
	if (parentMap.getBlockMap(next).getClass() == LevelMap.lbsDoor
		.getClass()) {
	    lbDoor d = (lbDoor) parentMap.getBlockMap(next);
	    if (!d.isOpened) // open the door if its closed.
		d.action();
	    // TODO: what about locked doors
	}

	// Check for movement collision
	if (parentMap.collides(next)) {
<<<<<<< HEAD
	    moveList.clear();
	    mapTarget = mapLocation;
=======
	    path = null;
>>>>>>> - Added marking of destination.
	    return;
	}

	// Move and update the map zoom
	mapLocation = next;
	parentMap.fixWindowEdges();

	// System.out.println("updatePosition: " + mapLocation + " remaining: "
	// + moveList);
    }

    public void gameUpdate() {
	// TODO: this should take the event queue into account
	if (count-- <= 0) {
	    updatePosition();
	    count = 20;
	}
    }
}

class LocalPlayerMob extends Mob {
    private BufferedImage icon;
    AffineTransform xform = new AffineTransform();

    public LocalPlayerMob(LevelMap lm) {
	super(lm);
	mobName = "Local Player";
	canOpendoors = true;

	try {
	    icon = ImageIO
		    .read(new File(
			    "bin/org/toadking/games/underwaterroguelike/Images/Player.png"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void draw(Graphics2D g) {
	// In case the graphic didn't load, go with something ... simpler.
	if (icon == null) {
	    g.setColor(Color.pink);
	    g.fillOval(parentMap.mapXToUI(mapLocation.getX()),
		    parentMap.mapYToUI(mapLocation.getY()),
		    Math.round(diameter * parentMap.blockSize),
		    Math.round(diameter * parentMap.blockSize));
	} else {
	    xform.setToIdentity();

	    float iconScale = (float) parentMap.blockSize / icon.getWidth();

	    xform.translate(parentMap.mapXToUI(mapLocation.getX()),
		    parentMap.mapYToUI(mapLocation.getY()));
	    xform.rotate(Math.toRadians(facing.heading() + 90), iconScale
		    * (icon.getWidth() / 2), iconScale * (icon.getHeight() / 2));
	    xform.scale(iconScale, iconScale);

	    g.drawImage(icon, xform, null);
	}

	// Draw the target if we need to move
<<<<<<< HEAD
	if ((moveList != null) && (!moveList.isEmpty())) {
	    g.setColor(Color.green);
	    g.drawOval(parentMap.mapXToUI(mapTarget.getX()),
		    parentMap.mapYToUI(mapTarget.getY()),
		    parentMap.blockSize, parentMap.blockSize);
=======
	if ((path != null) && (path.movesLeft() > 2)) {
	    g.setColor(Color.green);
	    g.drawOval(parentMap.mapXToUI(mapTarget.getX()),
		    parentMap.mapYToUI(mapTarget.getY()), parentMap.blockSize,
		    parentMap.blockSize);
>>>>>>> - Added marking of destination.
	}
    }
}