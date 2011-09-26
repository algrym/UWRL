/**
 * 
 */
package org.toadking.games.underwaterroguelike;

import java.awt.Graphics;
import java.awt.Color;

public abstract class LevelBlock {
    protected static final int GRIDLINEWIDTH = 1;

    public abstract Color blockColor();

    public abstract char blockChar();

    public abstract boolean isWalkable();

    public abstract LevelBlock newBlock();

    public void draw(Graphics g, int x, int y, int blockSize) {
	g.setColor(blockColor());
	g.fillRect(x, y, blockSize - GRIDLINEWIDTH, blockSize - GRIDLINEWIDTH);
    }

    public void playerAction() {
	System.out.println("Action on: " + this);
	return;
    }

    public abstract String toString();

    public abstract String toJSONString();
}

class lbAir extends LevelBlock {
    @Override
    public Color blockColor() {
	return Color.gray;
    }

    @Override
    public char blockChar() {
	return '_';
    }

    @Override
    public boolean isWalkable() {
	return true;
    }

    @Override
    public LevelBlock newBlock() {
	return new lbAir();
    }

    @Override
    public void draw(Graphics g, int x, int y, int blockSize) {
	return;
    }

    @Override
    public String toString() {
	return new String("lbAir");

    }

    @Override
    public String toJSONString() {
	return new String("\"lbAir\"");
    }
}

class lbBedRock extends LevelBlock {
    @Override
    public Color blockColor() {
	return Color.black;
    }

    @Override
    public char blockChar() {
	return '#';
    }

    @Override
    public boolean isWalkable() {
	return false;
    }

    @Override
    public LevelBlock newBlock() {
	return new lbBedRock();
    }

    @Override
    public String toString() {
	return new String("lbBedRock");
    }

    @Override
    public String toJSONString() {
	return null;
    }
}

class lbDoor extends LevelBlock {
    CardinalDirection opensAwayFrom;
    boolean isOpened = false;

    public lbDoor(CardinalDirection d) {
	opensAwayFrom = d;
    }

    @Override
    public Color blockColor() {
	return Color.gray;
    }

    @Override
    public char blockChar() {
	switch (opensAwayFrom) {
	case NORTH:
	case SOUTH:
	    return '-';
	case EAST:
	case WEST:
	    return '|';
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "No lbDoor.blockChar for heading" + opensAwayFrom.heading());
	}
    }

    @Override
    public boolean isWalkable() {
	return isOpened;
    }

    @Override
    public LevelBlock newBlock() {
	return new lbAir();
    }

    @Override
    public void playerAction() {
	super.playerAction();
	isOpened = !isOpened;
    }

    public void draw(Graphics g, int x, int y, int blockSize) {
	g.setColor(blockColor());

	// TODO: should really open against a wall

	switch (opensAwayFrom) {
	case NORTH:
	    if (isOpened)
		g.fillRect((int) (x), y, (int) (0.2 * blockSize)
			- GRIDLINEWIDTH, blockSize - GRIDLINEWIDTH);
	    else
		g.fillRect(x, (int) (y), blockSize - GRIDLINEWIDTH,
			(int) (0.2 * blockSize) - GRIDLINEWIDTH);
	    return;
	case SOUTH:
	    if (isOpened)
		g.fillRect((int) (x + (0.9 * blockSize)), y,
			(int) (0.2 * blockSize) - GRIDLINEWIDTH, blockSize
				- GRIDLINEWIDTH);
	    else
		g.fillRect(x, (int) (y + (0.9 * blockSize)), blockSize
			- GRIDLINEWIDTH, (int) (0.2 * blockSize)
			- GRIDLINEWIDTH);
	    return;
	case EAST:
	    if (isOpened)
		g.fillRect(x, (int) (y + (0.9 * blockSize)), blockSize
			- GRIDLINEWIDTH, (int) (0.2 * blockSize)
			- GRIDLINEWIDTH);
	    else
		g.fillRect((int) (x), y, (int) (0.2 * blockSize)
			- GRIDLINEWIDTH, blockSize - GRIDLINEWIDTH);
	    return;
	case WEST:
	    if (isOpened)
		g.fillRect(x, (int) (y), blockSize - GRIDLINEWIDTH,
			(int) (0.2 * blockSize) - GRIDLINEWIDTH);
	    else
		g.fillRect((int) (x + (0.9 * blockSize)), y,
			(int) (0.2 * blockSize) - GRIDLINEWIDTH, blockSize
				- GRIDLINEWIDTH);
	    return;

	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "No lbDoor.blockChar for heading" + opensAwayFrom.heading());
	}
    }

    @Override
    public String toString() {
	return new String("\"lbDoor\": {\"isOpened\": \"" + isOpened + "\"}");
    }

    @Override
    public String toJSONString() {
	return toString();
    }
}