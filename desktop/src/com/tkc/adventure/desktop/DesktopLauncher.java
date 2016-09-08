package com.tkc.adventure.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tkc.adventure.AdventureMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Adventure";
		config.height = 1080;
		config.width = 1920;
		config.fullscreen = false;
		new LwjglApplication(new AdventureMain(), config);
	}
}
