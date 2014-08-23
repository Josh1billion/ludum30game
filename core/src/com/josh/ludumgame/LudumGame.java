package com.josh.ludumgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.josh.graphicsengine2d.Globals;

public class LudumGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		Globals.game = new Game();
		Input.init();
	}

	@Override
	public void render () {
		Input.poll();
		tick(Gdx.graphics.getDeltaTime());
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Globals.game.draw();
	}
	
	public void tick(float delta)
	{
		Globals.game.tick(delta);
	}
		
}
