package org.toadking.games.underwaterroguelike;

public abstract class MapEntity {
    protected MapVector mapLocation;
    protected CardinalDirection facing = CardinalDirection.NORTH;
    final LevelMap parentMap;
    
    public MapEntity (final LevelMap lm) {
	this.parentMap = lm;
    }
    
    public void setFacing (CardinalDirection d) {
	this.facing = d;
    }
    public abstract void gameUpdate();
}
