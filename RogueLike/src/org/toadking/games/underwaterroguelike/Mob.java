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

    LinkedList<MapVector> moveList = new LinkedList<MapVector>();
    volatile boolean isMoveListValid = true;

    public Mob(LevelMap lm) {
	super(lm);
    }

    public String movesLeft() {
	if ((isMoveListValid) && (moveList != null))
	    return ("" + moveList.size());
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
	moveList.clear();

	// TODO: this is probably where we should load the sprite sheet
	// System.out.println("Mob '" + mobName + "' at (" + mapX + ", " + mapY
	// + ")");
    }

    public abstract void draw(Graphics2D g);

    public void moveStep(MapVector d) {
	mapTarget = mapLocation.add(d);

	if (parentMap.collides(mapTarget)) {
	    mapTarget = mapLocation;
	    return;
	}

	// System.out.println("moveStep: " + mapLocation + " + " + d + " = "
	// + mapTarget);
	moveList.clear();
    }

    public void moveTo(final int newTargetMapX, final int newTargetMapY) {
	mapTarget = new MapVector(newTargetMapX, newTargetMapY);
	// System.out.println("moveTo: " + mapLocation + " -> " + mapTarget);

	// Threading: don't move until the move list is finished
	isMoveListValid = false;
	findPath();
    }

    private void findPath() {
	PathVector path = new PathVector(mapTarget, parentMap);
	moveList = path.compute(mapLocation);

	// Threading: don't move until the move list is finished
	isMoveListValid = true;

	// System.out.println("From " + mapLocation + " to " + mapTarget +
	// " in "
	// + path.getExpandedCounter() + " tries via " + moveList);
    }

    private void updatePosition() {
	// Attempting A* as per
	// http://www.policyalmanac.org/games/aStarTutorial.htm

	// Do we need to move?
	if (mapTarget.equals(mapLocation))
	    return; // we're where we should be

	// Threading: don't move until the move list is finished
	if (isMoveListValid == false)
	    return;

	// if the list is null, we can't get there from here
	if (moveList == null) {
	    // System.out.println("updatePosition: NO PATH from " + mapLocation
	    // + " to " + mapTarget);
	    mapTarget = mapLocation;
	    moveList = new LinkedList<MapVector>();
	    return;
	}

	// Figure out the next move either by
	MapVector next;
	if (moveList.isEmpty())
	    // can we take a quick movement jump?
	    next = mapTarget;
	else
	    // process the next step in the queue
	    next = moveList.removeFirst();

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
	    moveList.clear();
	    mapTarget = mapLocation;
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
	    count = 30;
	}
    }
}

class LocalPlayerMob extends Mob {
    private BufferedImage icon;
    AffineTransform xform = new AffineTransform();

    public LocalPlayerMob(LevelMap lm) {
	super(lm);
	mobName = "Local Player";

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
	if ((moveList != null) && (!moveList.isEmpty())) {
	    g.setColor(Color.green);
	    g.drawOval(parentMap.mapXToUI(mapTarget.getX()),
		    parentMap.mapYToUI(mapTarget.getY()),
		    parentMap.blockSize, parentMap.blockSize);
	}
    }
}