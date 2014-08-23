package com.josh.ludumgame;

/*
 * Input.java:
 * 	Provides an interface for easily checking whether keys are pressed, the mouse is clicked, etc.
 *  No need to add code here, unless you are adding support for more keys not already defined here.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class Input
{
	public static int mouseClick = 0;
	public static int mouseX = 0;
	public static int mouseY = 0;
	
	public static int keys[];
	
	public static void init()
	{
		keys = new int[256];
	}
	
	public static void poll()
	{
		// poll the mouse/touchscreen
		if (!Gdx.input.isTouched())
			mouseClick = 0;
		else if (Input.mouseClick > 0)
			Input.mouseClick = 2;
		else
			Input.mouseClick = 1;
		
		for (int i = 0; i < 256; i++)
			if (Gdx.input.isKeyPressed(i))
			{
				if (keys[i] > 0)
					keys[i] = 2;
				else
				 	keys[i] = 1;
			}
			else
				keys[i] = 0;
		Input.mouseX = Gdx.input.getX();
		Input.mouseY = Gdx.input.getY();
	}
}
