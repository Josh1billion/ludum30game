package com.josh.ludumgame;

import com.josh.graphicsengine2d.Image;

public class Tile
{
	Image image;
	boolean isExit;
	boolean solid;
	int x, y;
	
	public Tile(Image image, boolean isExit, boolean solid, int x, int y)
	{
		this.image = image;
		this.isExit = isExit;
		this.solid = solid;
		this.x = x;
		this.y = y;
	}
	
	public Image getImage() { return image; }
	public boolean isSolid() { return solid; }
	public boolean isExit() { return isExit; }
}
