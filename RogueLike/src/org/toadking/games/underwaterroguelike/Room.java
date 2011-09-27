package org.toadking.games.underwaterroguelike;

import java.awt.Rectangle;

public abstract class Room {
    LevelMap parentMap;

    public Room(LevelMap l) {
	parentMap = l;
    }

    public abstract boolean containsPoint(final int x, final int y);

    public abstract void mapFill();

    public abstract boolean intersects(Rectangle r);

    public abstract void addToMap();

    public abstract int getX();

    public abstract int getY();

    public abstract int getWidth();

    public abstract int getHeight();
}

class RectangleRoom extends Room {
    final Rectangle wallRect;

    public RectangleRoom(final LevelMap l, final Rectangle r) {
	this(l, r.x, r.y, r.width, r.height);
    }

    public RectangleRoom(LevelMap l, final int x, final int y) {
	this(l, x, y, (int) LevelMap.rnd.nextInt(20), (int) LevelMap.rnd
		.nextInt(20));
    }

    public RectangleRoom(final LevelMap l, final int x, final int y,
	    final int width, final int height) {
	super(l);

	int newX = Math.max(x, 0);
	int newY = Math.max(y, 0);
	int newW = Math.min(x + width, LevelMap.getBlockMapWidth() - 1) - x;
	int newH = Math.min(y + height, LevelMap.getBlockMapWidth() - 1) - y;

	wallRect = new Rectangle(newX, newY, newW, newH);
    }

    @Override
    public void mapFill() {
	for (int i = wallRect.x; i < (wallRect.x + wallRect.width); i++) {
	    for (int j = wallRect.y; j < (wallRect.y + wallRect.height); j++) {
		parentMap.setBlockMap(i, j, LevelMap.lbsAir);
	    }
	}
    }

    @Override
    public String toString() {
	return wallRect.toString();
    }

    @Override
    public boolean containsPoint(final int x, final int y) {
	return wallRect.contains(x, y);
    }

    public boolean intersects(final Rectangle r) {
	return wallRect.intersects(r);
    }

    @Override
    public void addToMap() {
	parentMap.roomList.add(this);

	mapFill();
    }

    public String getRoomType() {
	return new String("RectangleRoom");
    }

    @Override
    public int getX() {
	return (int) wallRect.getX();
    }

    @Override
    public int getY() {
	return (int) wallRect.getY();
    }

    @Override
    public int getWidth() {
	return (int) wallRect.getWidth();
    }

    @Override
    public int getHeight() {
	return (int) wallRect.getHeight();
    }
}