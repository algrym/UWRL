/**
 * LevelMap.java
 * @author ajw
 */
package org.toadking.games.underwaterroguelike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * LevelMap
 */
public class LevelMap {
    private static final short LEVELMAXSIDE = 1000;
    private static final short MINROOMS = 5;
    private static final short MAXROOMS = 50;

    LevelBlock[][] blockMap;
    private int mapWindowMinX = -1, mapWindowMinY = -1,
	    mapWindowMaxX = Integer.MAX_VALUE,
	    mapWindowMaxY = Integer.MAX_VALUE;

    private long randSeed;
    static Random rnd = new Random();

    // Static LevelBlock references to save memory
    static final lbAir lbsAir = new lbAir();
    static final lbBedRock lbsBedRock = new lbBedRock();

    // Pixel size of blocks when drawn in the UI
    private static final int DEFAULTBLOCKSIZE = 20;
    int blockSize = DEFAULTBLOCKSIZE;

    ArrayList<Room> roomList;

    // ArrayList<Mob> mobList = new ArrayList<Mob>();
    Mob LocalPlayer = new LocalPlayerMob(this);

    public void save() {
	// JSON Validator: http://jsonformatter.curiousconcept.com/
    }

    private int getRand(final int min, final int max) {
	int r = -1;
	if (max < min) { // Be forgiving if we get these backwards
	    r = rnd.nextInt(min - max) + max;

	    System.out.println("getRand(min=" + max + ", max=" + min + ") = "
		    + r);
	} else {
	    r = rnd.nextInt(max - min) + min;

	    System.out.println("getRand(min=" + min + ", max=" + max + ") = "
		    + r);
	}
	return r;
    }

    private void generateRogueLikeLevelMap() {
	// From
	// http://roguebasin.roguelikedevelopment.org/index.php?title=Dungeon-Building_Algorithm
	Room currentRoom;

	// Erase and allocate the room map
	blankLevelMap();

	// Generate random rooms until you can get out a single room in the
	// centre of the map
	while (roomList.isEmpty()) {
	    // Generate a random room
	    int minX = (int) (blockMap.length / 2.0 - getRand(5, 10));
	    int minY = (int) (blockMap[0].length / 2.0 - getRand(5, 10));
	    int width = (int) (blockMap.length / 2.0 - minX + getRand(5, 10));
	    int height = (int) (blockMap.length / 2.0 - minX + getRand(5, 10));
	    currentRoom = new RectangleRoom(this, minX, minY, width, height);

	    // Remove the room if its not in the right place
	    if (currentRoom.containsPoint(blockMap.length / 2,
		    blockMap[0].length / 2))
		currentRoom.addToMap();
	}
	System.out.println("Rooms:" + roomList);

	// Create the new player and just sort of ... stick them in the middle
	// of the map.
	LocalPlayer.setLoc((int) (blockMap.length / 2.0),
		(int) (blockMap[0].length / 2.0));

	// loop until we have "enough rooms"
	short maxRooms = (short) getRand(MINROOMS, MAXROOMS);
	System.out.println("Generating up to " + maxRooms + " rooms.");

	do {
	    // Pick a room at random
	    currentRoom = roomList.get(getRand(0, roomList.size()));

	    // Pick a square of that room's walls at random
	    // Decide upon a new feature to build

	    int wallX = 0, wallY = 0, doorX = 0, doorY = 0;
	    final int doorSize = 3;
	    final int width = getRand(2 * doorSize, 20);
	    final int height = getRand(2 * doorSize, 20);

	    System.out.println("x=" + currentRoom.getX() + " w="
		    + currentRoom.getWidth() + " h=" + currentRoom.getHeight()
		    + " w'=" + width + " h'=" + height + " d=" + doorSize);

	    CardinalDirection doorDirection = CardinalDirection.NORTH;

	    // Randomly select a wall
	    switch (getRand(0, 4)) {
	    case 0: // North wall!
		wallX = ((currentRoom.getX() - width + doorSize) + LevelMap.rnd
			.nextInt((int) (currentRoom.getWidth() + width - (2 * doorSize))));
		doorX = getRand(
			Math.max(wallX, currentRoom.getX()),
			Math.min(wallX + width, currentRoom.getX()
				+ currentRoom.getWidth()));
		doorY = currentRoom.getY() - 1;
		wallY = doorY - height;
		doorDirection = CardinalDirection.NORTH;
		break;

	    case 1: // East wall!
		doorX = (int) (currentRoom.getX() + currentRoom.getWidth());
		wallX = doorX + 1;
		wallY = (int) ((currentRoom.getY() - height + doorSize))
			+ LevelMap.rnd.nextInt((int) (currentRoom.getHeight()
				+ height - (2 * doorSize)));
		doorY = getRand(
			Math.max(wallY, currentRoom.getY()),
			Math.min(wallY + height, currentRoom.getY()
				+ currentRoom.getHeight()));
		doorDirection = CardinalDirection.EAST;
		break;

	    case 2: // South wall!
		wallX = (int) ((currentRoom.getX() - width + doorSize) + LevelMap.rnd
			.nextInt((int) (currentRoom.getWidth() + width - (2 * doorSize))));
		doorY = (int) (currentRoom.getY() + currentRoom.getHeight());
		doorX = getRand(
			Math.max(wallX, currentRoom.getX()),
			Math.min(wallX + width, currentRoom.getX()
				+ currentRoom.getWidth()));
		wallY = doorY + 1;
		doorDirection = CardinalDirection.SOUTH;
		break;

	    case 3: // West wall!
		doorX = (int) currentRoom.getX() - 1;
		wallX = doorX - width;
		wallY = (int) ((currentRoom.getY() - height + doorSize))
			+ LevelMap.rnd.nextInt((int) (currentRoom.getHeight()
				+ height - (2 * doorSize)));
		doorY = getRand(
			Math.max(wallY, currentRoom.getY()),
			Math.min(wallY + height, currentRoom.getY()
				+ currentRoom.getHeight()));
		doorDirection = CardinalDirection.WEST;
		break;
	    default:
		System.err.println("WTF?");
	    }

	    // See if there is room to add the new feature through the
	    // chosen wall. If yes, continue. If no, go back to
	    // "pick a room at random"

	    Rectangle r = new Rectangle(wallX, wallY, width, height);
	    for (int i = 0; i < roomList.size(); i++) {
		if (roomList.get(i).intersects(r)) {
		    r = null;
		    break;
		}
	    }

	    // We succeeded in adding a non-colliding room to the map
	    // Add it to the room list and add a door
	    if (r != null) {
		new RectangleRoom(this, r).addToMap();

		// randomly pick how to join rooms
		switch (getRand(0, 2)) {
		case 0:
		    blockMap[doorX][doorY] = lbsAir;
		    break;
		case 1:
		    blockMap[doorX][doorY] = new lbDoor(doorDirection);
		    break;
		}

	    }

	    // Go back to "pick a room at random", until the dungeon is complete
	} while (roomList.size() < maxRooms);

	// Add the up and down staircases at random points in map
	// Finally, sprinkle some monsters and items liberally over dungeon
    }

    private void blankLevelMap() {
	// Reset the roomList
	roomList = new ArrayList<Room>();

	// Fill the whole map with solid earth
	blockMap = new LevelBlock[LEVELMAXSIDE][LEVELMAXSIDE];
	for (int i = 0; i < blockMap.length; i++) {
	    for (int j = 0; j < blockMap[i].length; j++) {
		blockMap[i][j] = lbsBedRock;
	    }
	}

    }

    private void fixWindowEdges(final int screenUIWidth,
	    final int screenUIHeight) {

	// Reset these values so ui?toMap conversions work
	mapWindowMinX = 0;
	mapWindowMinY = 0;

	int screenMapWidth = uiXToMap(screenUIWidth);
	int screenMapHeight = uiYToMap(screenUIHeight);

	// System.out.println("screenUI: " + screenUIWidth + ", " +
	// screenUIHeight
	// + " -> " + screenMapWidth + ", " + screenMapHeight);

	// Center on the player
	mapWindowMinX = LocalPlayer.mapX - (screenMapWidth / 2);
	mapWindowMinY = LocalPlayer.mapY - (screenMapHeight / 2);

	// Normalize the window edges before we use them
	mapWindowMaxX = Math.min(mapWindowMinX + screenMapWidth + 1,
		blockMap.length);
	mapWindowMaxY = Math.min(mapWindowMinY + screenMapHeight + 1,
		blockMap[0].length);

	// Normalize the window edges before we use them
	mapWindowMinX = Math.max(mapWindowMinX, 0);
	mapWindowMinY = Math.max(mapWindowMinY, 0);
	mapWindowMaxX = Math.min(mapWindowMaxX, blockMap.length);
	mapWindowMaxY = Math.min(mapWindowMaxY, blockMap[0].length);

	// System.out
	// .println("First FixEdges: " + mapWindowMinX + " to "
	// + mapWindowMaxX + "  " + mapWindowMinY + " to "
	// + mapWindowMaxY);
    }

    void fixWindowEdges() {
	// System.out.println("Edges: " + mapWindowMinX + " - " + mapWindowMaxX
	// + "  " + mapWindowMinY + " - " + mapWindowMaxY);

	// ensure the player is not within 10 map squares of the UI edge
	while ((LocalPlayer.mapX < (mapWindowMinX + 10)) && (mapWindowMinX > 0)) {
	    mapWindowMinX--;
	    mapWindowMaxX--;
	}

	while ((LocalPlayer.mapX > (mapWindowMaxX - 10))
		&& (mapWindowMaxX < blockMap.length)) {
	    mapWindowMinX++;
	    mapWindowMaxX++;
	}

	while ((LocalPlayer.mapY < (mapWindowMinY + 10)) && (mapWindowMinY > 0)) {
	    mapWindowMinY--;
	    mapWindowMaxY--;
	}

	while ((LocalPlayer.mapY > (mapWindowMaxY - 10))
		&& (mapWindowMaxY < blockMap.length)) {
	    mapWindowMinY++;
	    mapWindowMaxY++;
	}
    }

    int mapXToUI(int mapValue) {
	return (int) Math.round((mapValue - mapWindowMinX) * blockSize);
    }

    int uiXToMap(int uiValue) {
	return (int) Math.round((uiValue / blockSize) + mapWindowMinX);
    }

    int mapYToUI(int mapValue) {
	return (int) Math.round((mapValue - mapWindowMinY) * blockSize);
    }

    int uiYToMap(int uiValue) {
	return (int) Math.round((uiValue / blockSize) + mapWindowMinY);
    }

    public LevelMap(long seed) {
	rnd.setSeed(randSeed = seed);
	generateRogueLikeLevelMap();
    }

    public LevelMap() {
	rnd.setSeed(randSeed = System.currentTimeMillis());
	generateRogueLikeLevelMap();
    }

    public void drawMap(Graphics2D g, int width, int height) {
	// clear the background
	g.setColor(Color.darkGray);
	g.fillRect(0, 0, width, height);

	// TODO: Background needs to repeat over the size of the map
	// TODO: Background needs to move with the map, not the screen

	if (mapWindowMinX < 0)
	    fixWindowEdges(width, height);

	for (int i = mapWindowMinX; i < mapWindowMaxX; i++) {
	    for (int j = mapWindowMinY; j < mapWindowMaxY; j++) {
		if (blockMap[i][j] != null) {
		    blockMap[i][j].draw(g, mapXToUI(i), mapYToUI(j), blockSize);
		}
	    }
	}
	LocalPlayer.draw(g);

	// inform on the position of the character
	g.setColor(Color.white);
	if ((LocalPlayer.mapX == LocalPlayer.targetMapX)
		&& (LocalPlayer.mapY == LocalPlayer.targetMapY))

	    g.drawString("(" + LocalPlayer.mapX + "," + LocalPlayer.mapY + ")",
		    5, 20);
	else
	    g.drawString("(" + LocalPlayer.mapX + "," + LocalPlayer.mapY
		    + ") -> (" + LocalPlayer.targetMapX + ","
		    + LocalPlayer.targetMapY + ")", 5, 20);
    }

    public void print() {
	for (int i = 0; i < blockMap.length; i++) {
	    for (int j = 0; j < blockMap[i].length; j++) {
		if (blockMap[i][j] == null)
		    System.out.print('?');
		else
		    System.out.print(blockMap[i][j].blockChar());
	    }
	    System.out.println();
	}
    }

    boolean collides(int mapX, int mapY) {
	// forbid movement off the edge of the map
	if ((mapX < 0) || (mapX > blockMap.length) || (mapY < 0)
		|| (mapY > blockMap.length))
	    return true;

	// forbid collisions with the map
	if (!blockMap[mapX][mapY].isWalkable())
	    return true;

	// TODO check for collisions with other mobs
	// TODO handle collisions if stepSize > 1 (jump over obstacles)

	return false; // Looks clear.
    }

    public void movePlayerClick(final int mouseX, final int mouseY) {
	// convert UI dimensions to map dimensions and make this
	// the player's new destination
	LocalPlayer.moveTo(uiXToMap(mouseX), uiYToMap(mouseY));
	fixWindowEdges();
    }

    public void movePlayer(CardinalDirection degrees) {
	LocalPlayer.moveStep(degrees);
	fixWindowEdges();
    }

    public CardinalDirection movePlayerNorth() {
	LocalPlayer.moveStep(CardinalDirection.NORTH);
	fixWindowEdges();
	return CardinalDirection.NORTH;
    }

    public CardinalDirection movePlayerSouth() {
	LocalPlayer.moveStep(CardinalDirection.SOUTH);
	fixWindowEdges();
	return CardinalDirection.SOUTH;
    }

    public CardinalDirection movePlayerWest() {
	LocalPlayer.moveStep(CardinalDirection.WEST);
	fixWindowEdges();
	return CardinalDirection.WEST;
    }

    public CardinalDirection movePlayerEast() {
	LocalPlayer.moveStep(CardinalDirection.EAST);
	fixWindowEdges();
	return CardinalDirection.EAST;
    }

    public void gameUpdate() {
	// update player
	LocalPlayer.gameUpdate();
    }

    public void playerAction() {
	// If the player is facing a map square with stuff in it
	// call the action method on that area
	int nextMapX = LocalPlayer.mapX + LocalPlayer.facing.getXProjection();
	int nextMapY = LocalPlayer.mapY + LocalPlayer.facing.getYProjection();

	System.out.println("Action: " + LocalPlayer.facing + " " + nextMapX
		+ ", " + nextMapY);

	// Ensure we're not attempting something off the map edge
	if ((nextMapX < 0) || (nextMapX >= blockMap.length) || (nextMapY < 0)
		|| (nextMapY >= blockMap[0].length))
	    return;

	// TODO: check to see if the target block contains a mob

	// Otherwise call action on the map area being faced
	blockMap[nextMapX][nextMapY].playerAction();
    }

    public void zoomIn() {
	mapWindowMinX = -1;
	blockSize = Math.min(50, blockSize + 1);
    }

    public void zoomDefault() {
	mapWindowMinX = -1;
	blockSize = DEFAULTBLOCKSIZE;
    }

    public void zoomOut() {
	mapWindowMinX = -1;
	blockSize = Math.max(5, blockSize - 1);
    }
}