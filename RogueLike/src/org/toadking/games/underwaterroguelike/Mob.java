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

import javax.imageio.ImageIO;

/**
 * @author ajw
 * 
 */

public abstract class Mob extends MapEntity {
    static final float MINMOVETOLERANCE = 0.5f;
    String mobName = "Urist";
    float diameter = 0.75f;
    int targetMapX, targetMapY;
    int count = 0;

    public Mob(LevelMap lm) {
	super(lm);
    }

    public void setLoc(int startX, int startY) {
	if (parentMap.collides(startX, startY)) {
	    System.err.println("Should not place Mob(" + mobName
		    + ") in a non-walkable square (" + startX + "," + startY + ")");
	}

	targetMapX = mapX = startX;
	targetMapY = mapY = startY;

	// TODO: this is probably where we should load the sprite sheet
	// System.out.println("Mob '" + mobName + "' at (" + mapX + ", " + mapY
	// 	+ ")");
    }

    public abstract void draw(Graphics2D g);

    public void moveStep(CardinalDirection d) {
	// System.out.println("moveStep(" + d + "): " + d.getXProjection() +
	// ", "
	// + d.getYProjection() + " -> " + targetMapX + ", " + targetMapY);

	if (parentMap.collides(mapX + d.getXProjection(),
		mapY + d.getYProjection()))
	    return;

	facing = d;

	targetMapX = mapX + d.getXProjection();
	targetMapY = mapY + d.getYProjection();
    }

    public void moveTo(int targetMapX, int targetMapY) {
	// System.out.println("moveTo: " + mapX + ", " + mapY + " -> "
	// + targetMapX + ", " + targetMapY);
	this.targetMapX = targetMapX;
	this.targetMapY = targetMapY;
    }

    private void updatePosition() {
	// Do we need to move at all?
	if ((targetMapX != mapX) || (targetMapY != mapY)) {
	    // Get the difference between the target and the player's location
	    float diffX = (targetMapX - mapX);
	    float diffY = -(targetMapY - mapY);

	    // normalize the vector along the same angle
	    float dirLen = (float) Math.sqrt((diffX * diffX) + (diffY * diffY));

	    // create the normal vector for one step
	    int normalX = Math.round(diffX / dirLen);
	    int normalY = Math.round(diffY / dirLen);

	    // System.out.println("updatePosition: (" + mapX + ", " + mapY +
	    // ") -> ("
	    // + targetMapX + ", " + targetMapY + ") diff: (" + diffX + ", "
	    // + diffY + ") normal: (" + normalX + ", " + normalY + ")");

	    // are we legal to move in the direction listed?
	    if (parentMap.collides(mapX + normalX, mapY - normalY)) {
		// not legal at all, so reset the final location to present
		targetMapX = mapX;
		targetMapY = mapY;
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
	}
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

	try {
	    icon = ImageIO.read(new File("/Users/ajw/Downloads/Player.png"));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public void draw(Graphics2D g) {
	g.setColor(Color.pink);
	// g.fillOval(parentMap.mapXToUI(mapX), parentMap.mapYToUI(mapY),
	// Math.round(diameter * parentMap.blockSize),
	// Math.round(diameter * parentMap.blockSize));

	xform.setToIdentity();

	float iconScale = (float) parentMap.blockSize / icon.getWidth();

	xform.translate(parentMap.mapXToUI(mapX), parentMap.mapYToUI(mapY));
	xform.rotate(Math.toRadians(facing.heading() + 90),
		iconScale * (icon.getWidth() / 2),
		iconScale * (icon.getHeight() / 2));
	xform.scale(iconScale, iconScale);

	g.drawImage(icon, xform, null);
    }
}