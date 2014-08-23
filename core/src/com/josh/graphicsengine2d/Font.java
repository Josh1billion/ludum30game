package com.josh.graphicsengine2d;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;


public class Font
{
	BitmapFont bitmapFont;
	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";

	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int BOLD_ITALIC = 3;
	
	private float fontSize;
	
	public Font(String name, float size)
	{
		fontSize = size;
		
	}
	
	public Font(String name, int style, float size)
	{
		this(name + (style == BOLD ? "-bold" : style == ITALIC ? "-italic" : style == BOLD_ITALIC ? "-bold-italic" : ""), size);
	}
	
	public Font(String name, float size, int r0to255, int g0to255, int b0to255)
	{
		this(name, size);
		bitmapFont.setColor((float)r0to255 / 255.0f, (float)g0to255 / 255.0f, (float)b0to255 / 255.0f, 1.0f);
	}
	
	public void render(SpriteBatch batch, String textToDraw, int x, int y, int r0to255, int g0to255, int b0to255)
	{
		bitmapFont.setScale(1.0f);
		bitmapFont.setColor((float)r0to255 / 255.0f, (float)g0to255 / 255.0f, (float)b0to255 / 255.0f, 1.0f);
		this.render(batch, textToDraw, x, y);
	}
	
	public void render(SpriteBatch batch, String textToDraw, int x, int y)
	{
		batch.begin();
		bitmapFont.draw(batch, textToDraw, (float)x, (float)y - fontSize);
		batch.end();
	}
	
	public TextBounds getStringBounds(String textToDraw)
	{
		return bitmapFont.getBounds(textToDraw);
	}
}
