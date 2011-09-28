package org.toadking.games.underwaterroguelike;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PathVector extends AStar<MapVector> {
    private final MapVector start;
    private final MapVector target;
    private final LevelMap parentMap;
    private final boolean canOpendoors;
    private LinkedList<MapVector> moveList = null;
    private boolean isMoveListValid = true;

    static final MapVector[] MOVEOPTIONS = { new MapVector().north(),
	    new MapVector().south(), new MapVector().east(),
	    new MapVector().west(), new MapVector().northEast(),
	    new MapVector().northWest(), new MapVector().southEast(),
	    new MapVector().SouthWest() };

    public PathVector(final LevelMap newParentMap,
	    final boolean newCanOpendoors, final MapVector newStart,
	    final MapVector newTarget) {

	parentMap = newParentMap;
	canOpendoors = newCanOpendoors;
	start = newStart;
	target = newTarget;

	// can we handle the move in one step?
	for (int i = 0; i < MOVEOPTIONS.length; i++) {
	    if (target == start.add(MOVEOPTIONS[i])) {
		moveList.add(target);
		return;
	    }
	}

	// calculate the path from start to destination
	moveList = compute(start);
	if (moveList == null) {
	    // We can't get there from here, so do nothing
	    moveList = new LinkedList<MapVector>();
	}
    }

    @Override
    protected boolean isGoal(MapVector node) {
	return target.equals(node);
    }

    // cost to go from square X to square Y
    @Override
    protected Double g(MapVector from, MapVector to) {
	if (from.equals(to))
	    return 0D;

	if (from.bothAxesChange(to))
	    return 1.25 * parentMap.getBlockMap(to).getMoveCost();
	else
	    return parentMap.getBlockMap(to).getMoveCost();
    }

    // Estimated cost to reach a goal node
    @Override
    protected Double h(MapVector from, MapVector to) {
	return g(from, to);
    }

    @Override
    protected List<MapVector> generateSuccessors(final MapVector node) {
	List<MapVector> ret = new LinkedList<MapVector>();

	for (MapVector d : MOVEOPTIONS) {
	    if (!parentMap.collides(node.getX(), node.getY(), canOpendoors))
		ret.add(node.add(d));
	}

	// System.out.println("Checking from " + node + " to " + ret);

	return ret;
    }

    public int movesLeft() {
	// TODO Auto-generated method stub
	if (moveList == null)
	    return 0;
	else
	    return moveList.size();
    }

    public MapVector getNextMove() {
	if ((moveList != null) && (!moveList.isEmpty()))
	    return moveList.removeFirst();
	else
	    return null;
    }
}