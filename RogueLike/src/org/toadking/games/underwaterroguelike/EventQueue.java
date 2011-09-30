package org.toadking.games.underwaterroguelike;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

interface GameQueueObject {
    public abstract boolean gameQueueUpdate();

    public abstract long nextUpdateTime();
}

class GameEvent {
	public GameEvent(long newTriggerTime, GameQueueObject newObj) {
	    triggerTime = newTriggerTime;
	    obj = newObj;
	}

	final long triggerTime;
	final GameQueueObject obj;

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (this.getClass() != obj.getClass())
		return false;

	    final GameEvent other = (GameEvent) obj;
	    if ((this.triggerTime != other.triggerTime)
		    || (!this.obj.equals(other.obj)))
		return false;

	    return true;
	}

	@Override
	public String toString() {
	    return "[" + triggerTime + ": " + obj.toString()
		    + "]";
	}
}

public class EventQueue implements Comparator<GameEvent> {
    
    private PriorityQueue<GameEvent> queue = new PriorityQueue<GameEvent>(50, this);
    long currentTime = 0;

    public EventQueue() {
	currentTime = 0;
    }

    public int compare(final GameEvent o1, final GameEvent o2) {
	if (o1 == o2)
	    return 0;
	if ((o1 == null) || (o2 == null))
	    return -1;
	if (o1.getClass() != o2.getClass())
	    return -1;

	int i = 100;
	if (o1.triggerTime > o2.triggerTime)
	    i = +1;
	else if (o1.triggerTime < o2.triggerTime)
	    i = -1;
	else if (o1.triggerTime == o2.triggerTime)
	    i = 0;
	// int i = (int) (o1.triggerTime - o2.triggerTime);
	
//	System.out.println("Compare " + o1 + " and " + o2 + " = " + i);

	return i;
    }

    public void enqueue(GameQueueObject newObj) {
	//System.out.println("EventQueue: " + this);
	queue.add(new GameEvent(newObj.nextUpdateTime() + currentTime, newObj));
    }

    public void updateTime() {
	GameEvent g = null;

	// loop while there's a queue to examine
	while (!queue.isEmpty()) {
	    // get the head of the queue
	    g = queue.peek();

	    // ensure we have a valid object that is up for work
	    if ((g == null) || (g.triggerTime >= ++currentTime))
		return;

	    // remove the object from the queue
	    queue.remove(g);

	    // call update on that object
	    if (g.obj.gameQueueUpdate())
		enqueue(g.obj);
	}
    }

    @Override
    public String toString() {
	return currentTime + queue.toString();
    }
}