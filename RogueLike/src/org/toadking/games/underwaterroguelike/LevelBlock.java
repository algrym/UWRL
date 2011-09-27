/**
 * 
 */
package org.toadking.games.underwaterroguelike;

import java.awt.Graphics;
import java.awt.Color;

public abstract class LevelBlock {
    protected static final int GRIDLINEWIDTH = 0;

    public abstract Color blockColor();

    public abstract char blockChar();

    public abstract boolean isWalkable();

    public abstract boolean isAutoWalkable();

    public abstract LevelBlock newBlock();

    public void draw(Graphics g, int x, int y, int blockSize) {
	g.setColor(blockColor());
	g.fillRect(x, y, blockSize - GRIDLINEWIDTH, blockSize - GRIDLINEWIDTH);
    }

    public boolean action() {
	System.out.println("Action on: " + this);
	return true; // was it successful?
    }

    public abstract String toString();

    public abstract String toJSONString();

    public abstract Double getMoveCost();
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
	return new String("Air");

    }

    @Override
    public String toJSONString() {
	return new String("\"lbAir\"");
    }

    @Override
    public Double getMoveCost() {
	return 1.0D;
    }

    @Override
    public boolean isAutoWalkable() {
	return isWalkable();
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
	return new String("Bedrock");
    }

    @Override
    public String toJSONString() {
	return null;
    }

    @Override
    public Double getMoveCost() {
	return Double.MAX_VALUE;
    }

    @Override
    public boolean isAutoWalkable() {
	return isWalkable();
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
    public boolean action() {
	super.action();
	isOpened = !isOpened;
	// TODO: what if its locked?
	return true;
    }

    public void draw(Graphics g, int x, int y, int blockSize) {
	g.setColor(blockColor());
	final int doorWidthMin = (int) Math.max(1, blockSize - GRIDLINEWIDTH);
	final int doorWidthMax = (int) Math.max(1,
		((0.2 * blockSize) - GRIDLINEWIDTH));

	// TODO: should really open against a wall

	switch (opensAwayFrom) {
	case NORTH:
	    if (isOpened)
		g.fillRect(x, y, doorWidthMax, doorWidthMin);
	    else
		g.fillRect(x, y, doorWidthMin, doorWidthMax);
	    return;
	case SOUTH:
	    if (isOpened)
		g.fillRect((int) (x + (0.9 * blockSize)), y, doorWidthMax,
			doorWidthMin);
	    else
		g.fillRect(x, (int) (y + (0.9 * blockSize)), doorWidthMin,
			doorWidthMax);
	    return;
	case EAST:
	    if (isOpened)
		g.fillRect(x, (int) (y + (0.9 * blockSize)), doorWidthMin,
			doorWidthMax);
	    else
		g.fillRect(x, y, doorWidthMax, doorWidthMin);
	    return;
	case WEST:
	    if (isOpened)
		g.fillRect(x, y, doorWidthMin, doorWidthMax);
	    else
		g.fillRect((int) (x + (0.9 * blockSize)), y, doorWidthMax,
			doorWidthMin);
	    return;

	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "No lbDoor.blockChar for heading" + opensAwayFrom.heading());
	}
    }

    @Override
    public String toString() {
	if (isOpened)
	    return new String("Open Door");
	else
	    return new String("Closed Door");
    }

    @Override
    public String toJSONString() {
	return toString();
    }

    @Override
    public Double getMoveCost() {
	if (isOpened)
	    return 1.0D;
	else
	    return 5.0D;
    }

    @Override
    public boolean isAutoWalkable() {
	return true;
    }
}