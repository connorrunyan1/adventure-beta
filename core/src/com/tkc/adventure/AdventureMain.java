package com.tkc.adventure;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.tkc.adventure.screens.MainGameScreen;

public class AdventureMain extends Game {

    public static final MainGameScreen mainGameScreen = new MainGameScreen();

    @Override
    public void create(){
        setScreen(mainGameScreen);
    }

    @Override
    public void dispose(){
        mainGameScreen.dispose();
    }
}