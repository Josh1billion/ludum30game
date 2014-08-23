package com.josh.ludumgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.josh.graphicsengine2d.*;

public class Game
{
	public static int SCREEN_WIDTH = 640;
	public static int SCREEN_HEIGHT = 480;
	

	private int scrollX = 0;
	private int scrollY = 0;
	public Graphics g;
	
	
	public int getScrollX() { return scrollX; } 
	public int getScrollY() { return scrollY; }
	
	Game()
	{
		g = new Graphics();
	}
	
	public void tick(float delta)
	{
		if (Input.keys[Keys.ESCAPE] == 1)
			Gdx.app.exit();
	}
	
	public void draw()
	{
	}
}
