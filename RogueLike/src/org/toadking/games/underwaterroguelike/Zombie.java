package org.toadking.games.underwaterroguelike;

import java.awt.Color;
import java.awt.Graphics2D;

public class Zombie extends Mob {
    public Zombie(LevelMap lm) {
	super(lm);
	mobName = "Zombie!";
	canOpendoors = false;  // zombies are dumb
    }

    public Zombie(LevelMap lm, MapVector node) {
	this(lm);
	this.setLoc(node.getX(), node.getY());
    }

    @Override
    public void gameUpdate() {
	// TODO: this should take the event queue into account
	if (count-- <= 0) {
	    // Wander around!
	    int r = parentMap.getRand(0, 10);

	    if (r < 4) { // change direction!
		// Where should we go next?
		moveTo(mapLocation.add(PathVector.MOVEOPTIONS[parentMap.getRand(0,
			PathVector.MOVEOPTIONS.length)]
			.scale((double) parentMap.getRand(1, 10))));
	    } else if (r < 6) { // stop!
				moveStop();
	    }
	    // keep on keeping on

	    updatePosition();
	    count = 100; // Zombies move slow
	}
    }

    @Override
    public void draw(Graphics2D g) {
	g.setColor(Color.green);
	g.fillOval(parentMap.mapXToUI(mapLocation.getX()),
		parentMap.mapYToUI(mapLocation.getY()),
		Math.round(diameter * parentMap.blockSize),
		Math.round(diameter * parentMap.blockSize));
    }
}
