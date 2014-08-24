package com.josh.ludumgame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.josh.graphicsengine2d.Globals;
import com.josh.ludumgame.Game;
import com.josh.ludumgame.LudumGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 768;
		config.height = 576;
		config.title = "Ghostly Worlds - Josh1billion's Ludum Dare 30 entry";
		
		new LwjglApplication(new LudumGame(), config);
	}
}
