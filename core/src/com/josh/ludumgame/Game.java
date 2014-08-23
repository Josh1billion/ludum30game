package com.josh.ludumgame;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.josh.graphicsengine2d.*;
import com.josh.graphicsengine2d.Light.LightType;

public class Game
{
	public static int SCREEN_WIDTH = 800;
	public static int SCREEN_HEIGHT = 576;

	// images
	private ArrayList<Image> tileImagesSolid; // images for tiles that the player can't walk across
	private ArrayList<Image> tileImagesWalkable; // images for tiles that the player can walk across
	private Image exitImage = new Image("assets/tiles/exit.png"); // walking on this tile leads to the next randomly-generated map
	private Image heroImage = new Image("assets/sprites/hero.png"); // just a single frame for now...
	
	int exitX = 0;
	int exitY = 0;

	int heroX = 0; // pixel coordinates instead of tile coordinates, unlike everything else... should have thought this through a little better.
	int heroY = 0;
	
	private Tile[][] tiles; // the current map, made up of randomly generated tiles
	private Light[] lights;
	private Light heroLight; // a little torch held by the player

	private int scrollX = 0; // not actually used, since there is no scrolling, but this is required by my still-poorly-written graphics engine
	private int scrollY = 0;
	public Graphics g;
	
	int mapWidth = 25;
	int mapHeight = 18;
	
	
	public int getScrollX() { return scrollX; } 
	public int getScrollY() { return scrollY; }
	
	public Game()
	{
		g = new Graphics();
		
		// load all of the tile images
		tileImagesSolid = new ArrayList<Image>();
		tileImagesWalkable = new ArrayList<Image>();
		tileImagesSolid.add(new Image("assets/tiles/solid/1.png"));
		tileImagesSolid.add(new Image("assets/tiles/solid/2.png"));
		tileImagesWalkable.add(new Image("assets/tiles/walkable/1.png"));
		tileImagesWalkable.add(new Image("assets/tiles/walkable/2.png"));
		
		generateMap();
	}
	
	private boolean tileIsSolid(int x, int y)
	{
		if (x < 0 || y < 0)
			return false;
		if (x >= mapWidth || y >= mapHeight)
			return false;
		if (tiles[x][y] == null)
			return false;
		return tiles[x][y].isSolid();
	}
	
	
	private ArrayList<Tile> getTilesNextTo(int x, int y)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int yOffset = -1; yOffset <= 1; yOffset++)
			{
				int xDest = x + xOffset;
				int yDest = y + yOffset;
				if (xDest >= 0 && yDest >= 0 && xDest < mapWidth && yDest < mapHeight && tiles[xDest][yDest] != null)
					result.add(tiles[xDest][yDest]);
			}
		return result;
	}
	
	private void generateMap()
	{

		// first, remove any existing lights from a previous map, and give the player a torch light...
		try
		{
			if (heroLight != null)
				g.removeLight(heroLight);
			
			// give the player a light (torch)
			heroLight = g.createDiffuseLight(heroX, heroY, 96, 96, 96, 64, 32);
			
			if (lights != null)
				for (int i = 0; i < 5; i++)
					if (lights[i] != null)
							g.removeLight(lights[i]);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// generate some lights
		lights = new Light[5];
		for (int i = 0; i < 5; i++)
		{
			int red = (int)(Math.random() * 25) * 10;
			int green = (int)(Math.random() * 25) * 10;
			int blue = (int)(Math.random() * 25) * 10;
			float innerRadius = (float) (10.0f + Math.ceil(Math.random() * 100.0f)); 
			float outerRadius = (float) (10.0f + Math.ceil(Math.random() * 100.0f)); 
			lights[i] = new Light(g, 0, LightType.LIGHT_DIFFUSE, (int)Math.floor(Math.random() * 640), (int)Math.floor(Math.random() * 480), red, green, blue, innerRadius, outerRadius);
		}
		
		// add the lights to the graphics engine
		try {
			for (int i = 0; i < lights.length; i++)
				g.addLight(lights[i], LightType.LIGHT_DIFFUSE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// randomly generate some tiles... this might be less random later on..
		int style = (int)Math.floor(Math.random()*2);
		
		tiles = new Tile[mapWidth][mapHeight];
		for (int i = 0; i < 2; i++) // do two iterations, so tiles can more accurately gauge whether a tile next to them is solid
			for (int x = 0; x < mapWidth; x++)
				for (int y = 0; y < mapHeight; y++)
				{
					float randomChance = 0.05f;
					if (i > 0) // remove the random chance on the second iteration
						randomChance = 0.0f;
					// increase the chance of this being a solid tile by 10% for every nearby existing solid tile.  this should cluster solid tiles (water) closer together
					for (Tile nearbyTile : getTilesNextTo(x, y))
						if (nearbyTile.isSolid())
							randomChance += 0.15f;
					
					boolean solid = (Math.random() < randomChance);
					Image tileImage = (solid ? tileImagesSolid.get(style) : tileImagesWalkable.get(style));
					
					tiles[x][y] = new Tile(tileImage, false, solid, x, y);
				}
		
		// remove "lonely" solid tiles -- tiles that are solid but not surrounded by any other solid tiles
		for (int x = 0; x < mapWidth; x++)
			for (int y = 0; y < mapHeight; y++)
				if (tiles[x][y].isSolid())
				{
					boolean hasSolidNeighbors = false;
					for (Tile nearbyTile : getTilesNextTo(x, y))
						if (nearbyTile.isSolid())
						{
							hasSolidNeighbors = true;
							break;
						}
					
					// make this solid tile non-solid if it has no solid neighbors
					if (!hasSolidNeighbors)
						tiles[x][y] = new Tile(tileImagesWalkable.get(style), false, false, x, y);
				}

		// randomly choose a coordinate for the exit, out of all walkable tiles
		exitX = (int)(Math.floor(Math.random() * mapWidth));
		exitY = (int)(Math.floor(Math.random() * mapHeight));
		while (tiles[exitX][exitY].isSolid())
		{
			exitX = (int)(Math.floor(Math.random() * mapWidth));
			exitY = (int)(Math.floor(Math.random() * mapHeight));
		}
		tiles[exitX][exitY] = new Tile(tiles[exitX][exitY].getImage(), true, false, exitX, exitY);
			
	}

	float heroVelX = 0.0f;
	float heroVelY = 0.0f;
	
	public void tick(float delta)
	{
		if (Input.keys[Keys.ESCAPE] == 1)
			Gdx.app.exit();
		
		movePlayer(delta);
		moveLightsAround(delta);
	}
	
	public void draw()
	{
		g.setAmbientLight(128, 128, 128);
		
		// draw the tiles of the map
		for (int x = 0; x < mapWidth; x += 1)
			for (int y = 0; y < mapHeight; y += 1)
			{
				g.drawImage(tiles[x][y].getImage(), x*32, y*32, 1.0f);
				if (tiles[x][y].isExit())
					g.drawImage(exitImage, x*32, y*32, 1.0f);
					
			}
		
		// draw the hero
		g.drawImage(heroImage, heroX, heroY, 1.0f);
	}
	
	float lightVelX[] = new float[5];
	float lightVelY[] = new float[5];
	private void moveLightsAround(float delta)
	{
		// randomly change up the velocity of each light
		for (int i = 0; i < 5; i++)
		{
			float changeAmount = 15.0f;
			
			lightVelX[i] += -changeAmount/2.0f + (Math.random() * changeAmount);
			lightVelY[i] += -changeAmount/2.0f + (Math.random() * changeAmount);
			
			// if the light is too far on the left, make sure the X velocity gets very positive.
			// and so on for the other directions as well..
			// as a result, the lights should generally stay on the screen.
			if (lights[i].getX() < 0)
				lightVelX[i] += changeAmount;
			if (lights[i].getX() > 640)
				lightVelX[i] -= changeAmount;
			if (lights[i].getY() < 0)
				lightVelY[i] += changeAmount;
			if (lights[i].getY() > 640)
				lightVelY[i] -= changeAmount;
		}
		
		
		// move each light according to its velocity
		for (int i = 0; i < 5; i++)
		{
			lights[i].setPosition(lights[i].getX() + lightVelX[i]*delta, lights[i].getY() + lightVelY[i]*delta);
		}
	}
	
	private void movePlayer(float delta)
	{

		float heroAccelerationRate = 800.0f; // 800 pixels per second^2
		float maxVel = 200.0f;

		if (Input.keys[Keys.A] > 0)
			heroVelX -= delta * heroAccelerationRate;
		else if (Input.keys[Keys.D] > 0)
			heroVelX += delta * heroAccelerationRate;
		else if (heroVelX != 0.0f)
		{ // slow down to a halt (horizontally) if not pressing A or D
			if (heroVelX > 0.0f)
			{
				heroVelX -= heroAccelerationRate * 2 * delta;
				if (heroVelX < 0.0f)
					heroVelX = 0.0f;
			}
			else
			{
				heroVelX += heroAccelerationRate * 2 * delta;
				if (heroVelX > 0.0f)
					heroVelX = 0.0f;
			}
		}
		
		if (Input.keys[Keys.W] > 0)
			heroVelY -= delta * heroAccelerationRate;
		else if (Input.keys[Keys.S] > 0)
			heroVelY += delta * heroAccelerationRate;
		else if (heroVelY != 0.0f)
		{ // slow down to a halt (vertically) if not pressing W or S
			if (heroVelY > 0.0f)
			{
				heroVelY -= heroAccelerationRate * 2 * delta;
				if (heroVelY < 0.0f)
					heroVelY = 0.0f;
			}
			else
			{
				heroVelY += heroAccelerationRate * 2 * delta;
				if (heroVelY > 0.0f)
					heroVelY = 0.0f;
			}
		}

		if (heroVelX > maxVel)
			heroVelX = maxVel;
		if (heroVelX < -maxVel)
			heroVelX = -maxVel;
		if (heroVelY > maxVel)
			heroVelY = maxVel;
		if (heroVelY < -maxVel)
			heroVelY = -maxVel;
		
		heroX += heroVelX * delta;
		heroY += heroVelY * delta;
		
		heroLight.setPosition(heroX + 16, heroY + 16);
	}
}
