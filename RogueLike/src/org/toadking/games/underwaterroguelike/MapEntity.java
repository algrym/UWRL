package org.toadking.games.underwaterroguelike;

public abstract class MapEntity {
    int mapX, mapY;
    CardinalDirection facing = CardinalDirection.NORTH;
    LevelMap parentMap = null;
    
    public MapEntity (LevelMap lm) {
	this.parentMap = lm;
    }
    
    public void setFacing (CardinalDirection d) {
	this.facing = d;
    }
    public abstract void gameUpdate();
}
