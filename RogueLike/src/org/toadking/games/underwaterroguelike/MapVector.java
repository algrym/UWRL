package org.toadking.games.underwaterroguelike;

public class MapVector {
    private final int X;
    private final int Y;

    public MapVector() {
	X = 0;
	Y = 0;
    }

    public MapVector(final int newX, final int newY) {
	X = newX;
	Y = newY;
    }

    public final int getX() {
	return X;
    }

    public final int getY() {
	return Y;
    }

    public MapVector north() {
	return new MapVector(X, (Y - 1));
    }

    public MapVector south() {
	return new MapVector(X, (Y + 1));
    }

    public MapVector east() {
	return new MapVector((X + 1), Y);
    }

    public MapVector west() {
	return new MapVector((X - 1), Y);
    }

    public MapVector northWest() {
	return new MapVector((X - 1), (Y - 1));
    }

    public MapVector SouthWest() {
	return new MapVector((X - 1), (Y + 1));
    }

    public MapVector northEast() {
	return new MapVector((X + 1), (Y - 1));
    }

    public MapVector southEast() {
	return new MapVector((X + 1), (Y + 1));
    }

    public MapVector add(final MapVector other) {
	return new MapVector((this.X + other.X), (this.Y + other.Y));
    }

    @Override
    public String toString() {
	return new String("(" + X + ", " + Y + ")");
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }

    public CardinalDirection nearestDirection(final MapVector to) {
	// Get the difference between the target and the player's location
	float diffX = to.X - X;
	float diffY = to.Y - Y;

	// normalize the vector along the same angle
	float dirLen = (float) Math.sqrt((diffX * diffX) + (diffY * diffY));

	// create the normal vector for one step
	int normalX = Math.round(diffX / dirLen);
	int normalY = Math.round(diffY / dirLen);

	// determine how facing should be changed
	if (Math.abs(normalX) > Math.abs(normalY)) {
	    if (normalX > 0)
		return CardinalDirection.EAST;
	    else
		return CardinalDirection.WEST;
	} else {
	    if (normalY < 0)
		return CardinalDirection.NORTH;
	    else
		return CardinalDirection.SOUTH;
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (this.getClass() != obj.getClass())
	    return false;

	final MapVector other = (MapVector) obj;
	if ((this.X != other.X) || (this.Y != other.Y))
	    return false;

	return true;
    }

    public boolean bothAxesChange(MapVector to) {
	return ((X != to.X) && (Y != to.Y));
    }
}
