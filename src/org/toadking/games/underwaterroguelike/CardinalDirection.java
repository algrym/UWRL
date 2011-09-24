package org.toadking.games.underwaterroguelike;

public enum CardinalDirection {
    NORTH(90), EAST(0), SOUTH(270), WEST(180);

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
	case 0:
	    return new MapVector(+1, 0);
	case 90:
	    return new MapVector(0, -1);
	case 180:
	    return new MapVector(-1, 0);
	case 270:
	    return new MapVector(0, +1);
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get unit vector for heading of " + heading);
	}
    }

    public int getXProjection() {
	switch (heading) {
	case 0:
	    return +1;
	case 90:
	    return 0;
	case 180:
	    return -1;
	case 270:
	    return 0;
	default:
	    // TODO: figure out a good way to address non-cardinal moves
	    throw new UnsupportedOperationException(
		    "Can't get X projection for heading of " + heading);
	}
    }

    public int getYProjection() {
	switch (heading) {
	case 0:
	    return 0;
	case 90:
	    return -1;
	case 180:
	    return 0;
	case 270:
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
