package com.josh.ludumgame;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.josh.graphicsengine2d.*;
import com.josh.graphicsengine2d.Light.LightType;

public class Game
{
	public static int SCREEN_WIDTH = 768;
	public static int SCREEN_HEIGHT = 576;
	
	// IMPORTANT NOTE: when exporting as a runnable jar, first do a find-and-replace to replace "assets/" (without quotes) to nothing.  this is because the assets get packaged outside of the
	// assets folder.

	// images
	private ArrayList<Image> tileImagesSolid; // images for tiles that the player can't walk across
	private ArrayList<Image> tileImagesWalkable; // images for tiles that the player can walk across
	private Image exitImage = new Image("assets/tiles/exit.png"); // walking on this tile leads to the next randomly-generated map
	private Image heroImage = new Image("assets/sprites/hero.png"); // just a single frame for now...
	private Image nextWorldImage = new Image("assets/next_world.png");
	private Image titleScreenImage = new Image("assets/titlescreen.png");
	private Image gameOverImage = new Image("assets/game_over.png");
	private Image ghostImages[];
	
	// sounds
	Sound winSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/win.wav"));
	Sound loseSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/lose.wav"));
	Sound startSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/start.wav"));
	Sound musicSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/music.mp3"));
	
	int exitX = 0;
	int exitY = 0;

	float heroX = 200; // pixel coordinates instead of tile coordinates, unlike everything else... should have thought this through a little better.
	float heroY = 200;
	
	public Tile[][] tiles; // the current map, made up of randomly generated tiles
	private Light[] lights;
	private Light heroLight; // a little torch held by the player
	private Ghost[] ghosts;
	private int ghostCount = 3;

	private int scrollX = 0; // not actually used, since there is no scrolling, but this is required by my still-poorly-written graphics engine
	private int scrollY = 0;
	public Graphics g;
	
	int mapWidth = 12;
	int mapHeight = 9;
	
	boolean transitioningToNextLevel = false;
	float transitionCountdown = 3.0f;
	boolean gameOver = false;
	boolean titleScreen = true;
	
	public int getScrollX() { return scrollX; } 
	public int getScrollY() { return scrollY; }
	
	public Game()
	{
		g = new Graphics();
		
		// load all of the tile images
		tileImagesSolid = new ArrayList<Image>();
		tileImagesWalkable = new ArrayList<Image>();
		
		int tileStyleCount = 5;
		
		for (int i = 1; i <= tileStyleCount; i++)
		{
			tileImagesSolid.add(new Image("assets/tiles/solid/" + i + ".png"));
			tileImagesWalkable.add(new Image("assets/tiles/walkable/" + i + ".png"));
		}
		
		// ghost images
		ghostImages = new Image[5];
		ghostImages[0] = new Image("assets/sprites/ghost1.png");
		ghostImages[1] = new Image("assets/sprites/ghost2.png");
		ghostImages[2] = new Image("assets/sprites/ghost3.png");
		ghostImages[3] = new Image("assets/sprites/ghost4.png");
		ghostImages[4] = new Image("assets/sprites/ghost5.png");

		// start playing the music
		musicSound.loop();
		
		// initialize a random map to display on the title screen
		generateMap(3);
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
	
	private void generateMap(int ghostCount)
	{
		
		this.ghostCount = ghostCount;

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
		
		// generate ghosts
		ghosts = new Ghost[ghostCount];
		for (int i = 0; i < ghostCount; i++)
		{
			ghosts[i] = new Ghost();
			ghosts[i].x = (float)Math.random() * 500;
			ghosts[i].y = (float)Math.random() * 500;
			ghosts[i].image = ghostImages[(int)(Math.random() * ghostImages.length)];
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
		int style = (int)Math.floor(Math.random() * tileImagesSolid.size());
		
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

		heroX = (int)(Math.random()* 500);
		heroY = (int)(Math.random()* 500);
		while (collidingWithExit() || playerIsTouchingASolidTile() || playerIsTouchingAGhost())
		{
			heroX = (int)(Math.random()* 500);
			heroY = (int)(Math.random()* 500);
		}
			
			
	}

	float heroVelX = 0.0f;
	float heroVelY = 0.0f;
	
	public void tick(float delta)
	{
		if (Input.keys[Keys.ESCAPE] == 1)
			Gdx.app.exit();
		
		if (transitioningToNextLevel)
		{
			transitionCountdown -= delta;
			if (transitionCountdown <= 0.0f)
			{
				transitioningToNextLevel = false;
				generateMap(this.ghostCount + 1);
				Gdx.graphics.setTitle("Ghostly Worlds - World #" + (this.ghostCount - 2));
				g.setZoom(1.0f);
			}
			else
				g.setZoom(1.0f + 0.3f*(3.0f - transitionCountdown));
		}
		else if (gameOver || titleScreen)
		{
			if (Input.keys[Keys.SPACE] == 1)
			{
				gameOver = false;
				titleScreen = false;
				Gdx.graphics.setTitle("Ghostly Worlds - World #1");
				generateMap(3);
				startSound.play();
			}
			else if (titleScreen)
			{ // randomly move the invisible player, so that the ghosts have something to chase 
				heroVelX += -100 + (Math.random() * 200);
				heroVelY += -100 + (Math.random() * 200);
				heroX += heroVelX;
				heroY += heroVelY;
			}
				
		}
		else
		{ // normal gameplay
			movePlayer(delta);
			if (playerIsTouchingAGhost())
			{
				gameOver = true;
				loseSound.play();
			}
		}
		
		for (int i = 0; i < this.ghostCount; i++)
			ghosts[i].tick(delta);
		
		moveLightsAround(delta);
	}
	
	public void draw()
	{
		g.setAmbientLight(64, 64, 128);
		
		// draw the tiles of the map
		for (int x = 0; x < mapWidth; x += 1)
			for (int y = 0; y < mapHeight; y += 1)
			{
				g.drawImage(tiles[x][y].getImage(), x*64, y*64, 1.0f);
				if (tiles[x][y].isExit())
					g.drawImage(exitImage, x*64, y*64, 1.0f);
					
			}
		
		// draw the hero
		if (!titleScreen)
			g.drawImage(heroImage, heroX, heroY, 1.0f);
		
		// draw the ghosts
		for (int i = 0; i < this.ghostCount; i++)
			g.drawImage(ghosts[i].image, ghosts[i].x, ghosts[i].y, 1.0f);
		
		// draw Next World splash
		if (transitioningToNextLevel)
			g.drawImage(nextWorldImage, 234, 0, 1.0f, 1.0f - (transitionCountdown/10.0f), 1.0f + 2.0f*(3.0f - transitionCountdown));
		
		// draw Game Over splash
		if (gameOver)
			g.drawImage(gameOverImage, 70, 120, 1.0f, 2.0f, 2.0f);
		
		// draw title screen overlay
		if (titleScreen)
			g.drawImage(titleScreenImage, 0, 0, 1.0f);
		
		// draw level indicator
		if (!titleScreen)
			g.drawString("Hello world", 100, 100);
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
		if (collidingWithExit())
		{
			transitioningToNextLevel = true;
			transitionCountdown = 3.0f;
			winSound.play();
			return;
		}

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
		
		float heroDestX = heroX + heroVelX * delta;
		float heroDestY = heroY + heroVelY * delta;
		
		// some buggy stuff right here...!  trying to handle collisions with solid tiles and the edges of the map
		boolean hCovered = false;
		boolean vCovered = false;
		
		if (heroDestX < 0)
		{
			hCovered = true;
			heroVelX *= -1;
		}
		else if (heroDestX + 32 > mapWidth * 64)
		{
			hCovered = true;
			heroVelX *= -1;
		}
		if (heroDestY < 0)
		{
			vCovered = true;
			heroVelY *= -1;
		}
		else if (heroDestY + 32 > mapHeight * 64)
		{
			vCovered = true;
			heroVelY *= -1;
		}
		
		for (int tileX = 0; tileX < mapWidth; tileX++)
			for (int tileY = 0; tileY < mapHeight; tileY++)
			{
				Tile tile = tiles[tileX][tileY];
				if (tile.isSolid())
				{
					Rectangle r1 = new Rectangle(heroDestX, heroDestY, 32, 32);
					Rectangle r2 = new Rectangle(tileX * 64, tileY * 64, 64, 64);
					int collisionType = getCollision(r1, r2);
					if (collisionType == COLLISION_LEFT || collisionType == COLLISION_RIGHT)
						if (!hCovered)
						{
							heroVelX *= -1;
							hCovered = true;
						}
					if (collisionType == COLLISION_TOP || collisionType == COLLISION_BOTTOM)
						if (!vCovered)
						{
							heroVelY *= -1;
							vCovered = true;
						}
				}
			}
		
		heroX += heroVelX * delta;
		heroY += heroVelY * delta;
		
		heroLight.setPosition(heroX + 16, heroY + 16);
	}
	
	public int COLLISION_NONE = 0;
	public int COLLISION_RIGHT = 1;
	public int COLLISION_LEFT = 2;
	public int COLLISION_TOP = 3;
	public int COLLISION_BOTTOM = 4;
	
	public int getCollision(Rectangle r1, Rectangle r2)
	{
		Rectangle intersection = new Rectangle();
		Intersector.intersectRectangles(r1,  r2,  intersection);
		if (intersection.width == 0 || intersection.height == 0)
			return COLLISION_NONE;
		if (intersection.x > r1.x)
			return COLLISION_RIGHT;
		if (intersection.y > r1.y)
			return COLLISION_BOTTOM;
		if (intersection.x + intersection.width < r1.x + r1.width)
			return COLLISION_LEFT;
		if (intersection.y + intersection.height < r1.y + r1.height)
			return COLLISION_TOP;
		
		return COLLISION_NONE;
	}
	
	private boolean collidingWithExit()
	{
		Rectangle r1 = new Rectangle(heroX, heroY, 32, 32);
		Rectangle r2 = new Rectangle(exitX * 64 + 36, exitY * 64 + 36, 12, 12);
		return (getCollision(r1, r2) != COLLISION_NONE);
	}
	
	private boolean playerIsTouchingASolidTile()
	{
		for (int x = 0; x < mapWidth; x++)
			for (int y = 0; y < mapHeight; y++)
				if (tiles[x][y].isSolid())
				{
					Rectangle r1 = new Rectangle(heroX, heroY, 32, 32);
					Rectangle r2 = new Rectangle(x * 64, y * 64, 64, 64);
						if (getCollision(r1, r2) != COLLISION_NONE)
							return true;
				}
		return false;
	}
	
	private boolean playerIsTouchingAGhost()
	{
		for (int i = 0; i < this.ghostCount; i++)
		{
			Rectangle r1 = new Rectangle(heroX, heroY, 32, 32);
			Rectangle r2 = new Rectangle(ghosts[i].x, ghosts[i].y, 32, 32);
			if (getCollision(r1, r2) != COLLISION_NONE)
				return true;
		}
		return false;
	}
	
	
}
