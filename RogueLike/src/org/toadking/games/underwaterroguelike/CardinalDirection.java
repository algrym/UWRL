package org.toadking.games.underwaterroguelike;

import java.util.Random;

public enum CardinalDirection {
    EAST(0), NORTHEAST(45), NORTH(90), NORTHWEST(135), WEST(180), SOUTHWEST(225), SOUTH(
	    270), SOUTHEAST(315);

    // In the goofy coordinate plane that UI's work in,
    // 0--> +x (0 degrees)
    // |
    // v
    // +y (270 degrees)

    private final int heading;

    CardinalDirection(int newHeading) {
	newHeading %= 360; // ensure heading is between 0 and 360

	// make heading a positive number
	if (newHeading < 0)
	    newHeading += 360;

	assert (newHeading >= 0);
	assert (newHeading < 360);

	heading = newHeading;
    }

    public int heading() {
	return heading;
    }

    public MapVector getUnitVector() {
	switch (heading) {
	case 0: // EAST
	    return new MapVector(+1, 0);
	case 45: // NORTHEAST
	    return new MapVector(+1, -1);
	case 90: // NORTH
	    return new MapVector(0, -1);
	case 135: // NORTHWEST
	    return new MapVector(-1, -1);
	case 180: // WEST
	    return new MapVector(-1, 0);
	case 225: // SOUTHWEST
	    return new MapVector(-1, +1);
	case 270: // SOUTH
	    return new MapVector(0, +1);
	case 315: // SOUTHEAST
	    return new MapVector(+1, +1);
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get unit vector for heading of " + heading);
	}
    }

    public CardinalDirection getDirection(int r) {
	switch ((r * 45) % 360) {
	case 0: // EAST
	    return CardinalDirection.EAST;
	case 45: // NORTHEAST
	    return CardinalDirection.NORTHEAST;
	case 90: // NORTH
	    return CardinalDirection.NORTH;
	case 135: // NORTHWEST
	    return CardinalDirection.NORTHWEST;
	case 180: // WEST
	    return CardinalDirection.WEST;
	case 225: // SOUTHWEST
	    return CardinalDirection.SOUTHWEST;
	case 270: // SOUTH
	    return CardinalDirection.SOUTH;
	case 315: // SOUTHEAST
	    return CardinalDirection.SOUTHEAST;
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get unit vector for heading of " + heading);
	}
    }

    public int getXProjection() {
	switch (heading) {
	case 0: // EAST
	case 45: // NORTHEAST
	case 315: // SOUTHEAST
	    return +1;
	case 90: // NORTH
	case 270: // SOUTH
	    return 0;
	case 180: // WEST
	case 135: // NORTHWEST
	case 225: // SOUTHWEST
	    return -1;

	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get X projection for heading of " + heading);
	}
    }

    public int getYProjection() {
	switch (heading) {
	case 0: // EAST
	case 180: // WEST
	    return 0;
	case 90: // NORTH
	case 45: // NORTHEAST
	case 135: // NORTHWEST
	    return -1;
	case 270: // SOUTH
	case 225: // SOUTHWEST
	case 315: // SOUTHEAST
	    return +1;
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get Y projection for heading of " + heading);
	}
    }

    public String toString() {
	super.toString();
	return (heading + "¡");
    }
}
