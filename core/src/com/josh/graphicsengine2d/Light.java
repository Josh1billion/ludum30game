package com.josh.graphicsengine2d;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class Light
{
	public enum LightType { LIGHT_DIFFUSE, LIGHT_SPECULAR };
	
	private Graphics g;
	private LightType type = LightType.LIGHT_DIFFUSE;
	public int index;
	protected float x, y;
	protected float red, green, blue;
	protected float innerRadius, outerRadius;
	
	
	public void setPosition(float x, float y)
	{
		// allows you to move a light around.
		this.x = x;
		this.y = y;
	}
	
	public void setColor(float red0to255, float green0to255, float blue0to255)
	{
		this.red = red0to255 / 255.0f;
		this.green = green0to255 / 255.0f;
		this.blue = blue0to255 / 255.0f;
	}
	
	public void increaseColors(float red0to255, float green0to255, float blue0to255)
	{ 
		//Adds (r,g,b) to the light's current (r,g,b) values.
		this.red += red0to255 / 255.0f;
		this.green += green0to255 / 255.0f;
		this.blue += blue0to255 / 255.0f;
	}
	
	public void setRadius(float inner, float outer)
	{
		this.innerRadius = inner;
		this.outerRadius = outer;
	}
	
	public void increaseRadius(float inner, float outer)
	{
		this.innerRadius += inner;
		this.outerRadius += outer;
	}
	
	public Light(Graphics g, int index, LightType type, float x, float y, float r0to255, float g0to255, float b0to255, float innerRadius, float outerRadius)
	{ // should not be called directly!  use Graphics.createDiffuseLight() or Graphics.createSpecularLight() instead.
		this.g = g;
		this.type = type;
		this.x = x;
		this.y = y;
		this.red = r0to255 / 255.0f;
		this.green = g0to255 / 255.0f;
		this.blue = b0to255 / 255.0f;
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.index = index;
	}
	
	public void activateOnShader(ShaderProgram shader, float screenX, float screenY)
	{ // called internally by Graphics.  shouldn't be called elsewhere in the game code, so just ignore this method...
		shader.setUniformf("lightPositions[" + index + "]", new Vector3(x - screenX, y - screenY, 0.0f));
		shader.setUniformf("lightColors[" + index + "]", new Vector3(red, green, blue));
		shader.setUniformf("lightInnerRadiuses[" + index + "]", innerRadius);
		shader.setUniformf("lightOuterRadiuses[" + index + "]", outerRadius);		
	}
	
	public void remove() throws Exception
	{
		g.removeLight(this);
	}
	
	public void tick(float delta)
	{ // does nothing here, but lights that extend it can make good use of it
	}
}
