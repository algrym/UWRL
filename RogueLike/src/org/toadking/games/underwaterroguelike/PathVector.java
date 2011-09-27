package org.toadking.games.underwaterroguelike;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PathVector extends AStar<MapVector> {
    private final MapVector target;
    private final LevelMap parentMap;
    // private HashMap<MapVector, Boolean> vectorCache;
    private static final MapVector[] MOVEOPTIONS = { new MapVector().north(),
	    new MapVector().south(), new MapVector().east(),
	    new MapVector().west(), new MapVector().northEast(),
	    new MapVector().northWest(), new MapVector().southEast(),
	    new MapVector().SouthWest() };

    public PathVector(final MapVector newTarget, final LevelMap newParentMap) {
	target = newTarget;
	parentMap = newParentMap;
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
	    if (!parentMap.collides(node.getX(), node.getY(), true))
		ret.add(node.add(d));
	}

	// System.out.println("Checking from " + node + " to " + ret);

	return ret;
    }
}