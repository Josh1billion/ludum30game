package com.josh.ludumgame;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.josh.graphicsengine2d.Globals;
import com.josh.graphicsengine2d.Image;

public class Ghost
{
	public float x, y;
	public float velX, velY;
	public Image image;
	
	public void tick(float delta)
	{

		float ghostAccelerationRate = 30.0f; // 200 pixels per second^2
		float maxVel = 100.0f;

		if (this.x + 16 < Globals.game.heroX + 16)
			velX += ghostAccelerationRate * delta;
		if (this.x + 16 > Globals.game.heroX + 16)
			velX -= ghostAccelerationRate * delta;
		if (this.y + 16 < Globals.game.heroY + 16)
			velY += ghostAccelerationRate * delta;
		if (this.y + 16 > Globals.game.heroY + 16)
			velY -= ghostAccelerationRate * delta;

		if (velX > maxVel)
			velX = maxVel;
		if (velX < -maxVel)
			velX = -maxVel;
		if (velY > maxVel)
			velY = maxVel;
		if (velY < -maxVel)
			velY = -maxVel;
		
		float heroDestX = x + velX * delta;
		float heroDestY = y + velY * delta;
		
		// some buggy stuff right here...!
		boolean hCovered = false;
		boolean vCovered = false;
		for (int tileX = 0; tileX < Globals.game.mapWidth; tileX++)
			for (int tileY = 0; tileY < Globals.game.mapHeight; tileY++)
			{
				Tile tile = Globals.game.tiles[tileX][tileY];
				if (tile.isSolid())
				{
					Rectangle r1 = new Rectangle(heroDestX, heroDestY, 32, 32);
					Rectangle r2 = new Rectangle(tileX * 64, tileY * 64, 64, 64);
					int collisionType = Globals.game.getCollision(r1, r2);
					if (collisionType == Globals.game.COLLISION_LEFT || collisionType == Globals.game.COLLISION_RIGHT)
						if (!hCovered)
						{
							velX *= -1;
							hCovered = true;
						}
					if (collisionType == Globals.game.COLLISION_TOP || collisionType == Globals.game.COLLISION_BOTTOM)
						if (!vCovered)
						{
							velY *= -1;
							vCovered = true;
						}
				}
			}
		
		x += velX * delta;
		y += velY * delta;
		
	}
	
}
