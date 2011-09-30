package org.toadking.games.underwaterroguelike;

import java.awt.Color;
import java.awt.Graphics2D;

public class Zombie extends Mob {
    int stepCount = 0;
    int chatCount = 0;
    boolean isChatty = false;
    boolean isVegan = false;
    String sayText = "Brains!";

    private void updateSayText() {
	String s = "!";

	// Zombies feel self-doubt too.
	if (parentMap.getRand(0, 10) < 2)
	    s = "?";

	if (isVegan)
	    sayText = "Grains" + s;
	else
	    sayText = "Brains" + s;
    }

    public Zombie(LevelMap lm) {
	super(lm);
	setMobName("Zombie!");
	canOpendoors = false; // Zombies are dumb
	facing = facing.getDirection(parentMap.getRand(0, 7));

	// Create 1 in 100 zombies as vegan!
	if (parentMap.getRand(0, 100) < 2)
	    isVegan = true;
    }

    public Zombie(LevelMap lm, MapVector node) {
	this(lm);
	this.setLoc(node.getX(), node.getY());
    }

    @Override
    public boolean gameQueueUpdate() {
	stepCount++;
	return super.gameQueueUpdate();
    }

    @Override
    public void gameUpdate() {
	// Wander around!

	// Figure out if we should say stuff ... like "Brains!"
	int r = parentMap.getRand(0, 1000000);
	if (isChatty) {
	    if (chatCount <= 0) {
		isChatty = false;
		chatCount = 0;
	    }
	} else if (r < chatCount) {
	    isChatty = true;
	    updateSayText();
	}

	// Before we consider where the zombie should go, are we close to the
	// player?
	int d = (int) Math.round(mapLocation
		.distanceTo(parentMap.LocalPlayer.mapLocation));

	// Chase player ... if we're not vegan
	if ((d < 10) && (!isVegan)) {
	    if (!mapTarget.equals(parentMap.LocalPlayer.mapLocation)) {
		moveTo(parentMap.LocalPlayer.mapLocation);
//		System.out.println(getMobName() + " is " + d + " from player.");
	    }
	} else {
	    r = parentMap.getRand(0, 10000);
	    if (r < stepCount) { // change direction with increasing chance
				 // after
				 // each step
		facing = facing.getDirection(parentMap.getRand(0, 7));
		stepCount = 0;
	    }
	    // Move in the direction we're facing
	    moveTo(mapLocation.add(facing.getUnitVector()));
	}
    }

    @Override
    public void draw(Graphics2D g) {
	g.setColor(Color.green);
	g.fillOval(parentMap.mapXToUI(mapLocation.getX()),
		parentMap.mapYToUI(mapLocation.getY()),
		Math.round(diameter * parentMap.blockSize),
		Math.round(diameter * parentMap.blockSize));

	if (isChatty) {
	    chatCount -= 5;
	    g.drawString(sayText, parentMap.mapXToUI(mapLocation.getX())
		    - parentMap.blockSize,
		    parentMap.mapYToUI(mapLocation.getY())
			    - (int) (0.2 * parentMap.blockSize));
	} else
	    chatCount++;
    }

    @Override
    public long nextUpdateTime() {
	return 400L + parentMap.getRand(0, 400); // move every few ticks, plus a
						 // random amount
    }
}
