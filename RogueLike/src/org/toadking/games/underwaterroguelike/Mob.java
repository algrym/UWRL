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
	    d.action();
	    // TODO: what about locked doors
	}

	// Move and update the map zoom
	mapLocation = next;
	parentMap.fixWindowEdges();

	// System.out.println("updatePosition: " + mapLocation + " remaining: "
	// + moveList);
    }

    private void oldUpdatePosition() {
	// Do we need to move at all?
	if (!mapTarget.equals(mapLocation)) {
	    // Get the difference between the target and the player's location
	    float diffX = (mapTarget.getX() - mapLocation.getX());
	    float diffY = -(mapTarget.getY() - mapLocation.getY());

	    // normalize the vector along the same angle
	    float dirLen = (float) Math.sqrt((diffX * diffX) + (diffY * diffY));

	    // create the normal vector for one step
	    int normalX = Math.round(diffX / dirLen);
	    int normalY = Math.round(diffY / dirLen);

	    // determine how facing should be changed
	    if (Math.abs(normalX) > Math.abs(normalY)) {
		if (normalX > 0)
		    facing = CardinalDirection.EAST;
		else
		    facing = CardinalDirection.WEST;
	    } else {
		if (normalY > 0)
		    facing = CardinalDirection.NORTH;
		else
		    facing = CardinalDirection.SOUTH;
	    }

	    // System.out.println("updatePosition: (" + mapX + ", " + mapY +
	    // ") -> ("
	    // + targetMapX + ", " + targetMapY + ") diff: (" + diffX + ", "
	    // + diffY + ") normal: (" + normalX + ", " + normalY + ")");

	    int mapX = mapLocation.getX();
	    int mapY = mapLocation.getY();

	    // are we legal to move in the direction listed?
	    if (parentMap.collides(mapX + normalX, mapY - normalY)) {
		// not legal at all, so reset the final location to present
		mapTarget = mapLocation;
	    } else if (parentMap.collides(mapX + normalX, mapY)) {
		// only legal to move along normalX
		mapY -= normalY;
	    } else if (parentMap.collides(mapX, mapY - normalY)) {
		// only legal to move along normalY
		mapX += normalX;
	    } else {
		// legal to move as directed
		mapX += normalX;
		mapY -= normalY;
	    }

	    mapLocation = new MapVector(mapX, mapY);

	    // Update the map to account for the move.
	    parentMap.fixWindowEdges();
	}
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
    }
}