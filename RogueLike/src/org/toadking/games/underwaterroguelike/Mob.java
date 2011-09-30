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

public abstract class Mob extends MapEntity implements GameQueueObject {
    static final float MINMOVETOLERANCE = 0.5f;

    private String mobName = "Urist McRandomMob";
    float diameter = 0.75f;
    protected MapVector mapTarget;
    int count = 0;
    boolean canOpendoors = true;

    protected PathVector path;

    public Mob(LevelMap lm) {
	super(lm);
    }

    public Mob(LevelMap lm, MapVector node) {
	super(lm);
	this.setLoc(node.getX(), node.getY());
    }

    public boolean gameQueueUpdate() {
	// System.out.println(getMobName() + " updating!");
	updatePosition();
	return true; // yes, this item should be requeued
    }

    public long nextUpdateTime() {
	return 100L; // requeue for movement every so many ticks
    }

    public String movesLeft() {
	if (path != null)
	    return Integer.toString(path.movesLeft());
	else
	    return "?";
    }

    public void setLoc(int startX, int startY) {
	if (parentMap.collides(startX, startY)) {
	    System.err.println("Should not place Mob(" + getMobName()
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

	path = new PathVector(parentMap, canOpendoors, mapLocation, mapTarget);

	// System.out.println(mobName + " moveTo: " + mapLocation + " -> "
	// + mapTarget + " along " + path);
    }

    protected void updatePosition() {
	// Do we need to move?
	if (mapTarget.equals(mapLocation))
	    return; // we're where we should be

	// Just return if there is nowhere to go
	MapVector next = null;
	if (path != null)
	    next = path.getNextMove();
	else
	    return;

	// System.out.println("Updating " + mobName + " who needs to move to "
	// + next);

	// if the path is null, we can't get there from here
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
	    path = null;
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
    }

    @Override
    public String toString() {
	return getMobName();
    }

    public String getMobName() {
	return mobName;
    }

    public void setMobName(String mobName) {
	this.mobName = mobName;
    }
}

class LocalPlayerMob extends Mob {
    private BufferedImage icon;
    AffineTransform xform = new AffineTransform();

    public LocalPlayerMob(LevelMap lm) {
	super(lm);
	setMobName("Local Player");
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
    public long nextUpdateTime() {
	return 50L; // move every 50 ticks
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
	if ((path != null) && (path.movesLeft() > 2)) {
	    g.setColor(Color.green);
	    g.drawOval(parentMap.mapXToUI(mapTarget.getX()),
		    parentMap.mapYToUI(mapTarget.getY()), parentMap.blockSize,
		    parentMap.blockSize);
	}
    }
}