package org.toadking.games.underwaterroguelike;

import java.awt.geom.Dimension2D;

public class MapVector extends Dimension2D {
    private int width, height;

    public MapVector() {
	super();
	this.setSize(0, 0);
    }

    public MapVector(int newWidth, int newHeight) {
	super();
	this.setSize(newWidth, newHeight);
    }

    public MapVector(double newWidth, double newHeight) {
	super();
	this.setSize((int) newWidth, (int) newHeight);
    }

    @Override
    public double getHeight() {
	return height;
    }

    @Override
    public double getWidth() {
	return width;
    }

    public int getX() {
	return height;
    }

    public int getY() {
	return width;
    }

    @Override
    public void setSize(double newWidth, double newHeight) {
	width = (int) newWidth;
	height = (int) newHeight;
    }

    public void setSize(int newWidth, int newHeight) {
	width = newWidth;
	height = newHeight;
    }
}
