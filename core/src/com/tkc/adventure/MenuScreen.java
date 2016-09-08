package com.tkc.adventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;

public class MenuScreen extends ScreenAdapter {

    private Game game;
    private Texture backgroundTexture;
    private Stage stage;
    private Skin uiSkin;
    private Music music;
    private static final int WORLD_WIDTH = 1920;
    private static final int WORLD_HEIGHT = 1080;

    public MenuScreen(Game game1) {
        super();
        game = game1;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void show() {
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        music = Gdx.audio.newMusic(Gdx.files.internal("sisters.wav"));
        music.play();

        backgroundTexture = new Texture(Gdx.files.internal("LoginBackground.png"));
        Image background = new Image(backgroundTexture);
        stage.addActor(background);

        TextButton joinLobbyButton = new TextButton("Join Lobby", uiSkin);
        joinLobbyButton.setOrigin(Align.center);
        joinLobbyButton.setScale(3.0f, 3.0f);
        joinLobbyButton.setPosition(WORLD_WIDTH/2, WORLD_HEIGHT/2);
        stage.addActor(joinLobbyButton);

        joinLobbyButton.addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);

            }
        });
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render(float delta) {
        clearScreen();
        stage.act(delta);
        stage.draw();
    }
}
