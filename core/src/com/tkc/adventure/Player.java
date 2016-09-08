package com.tkc.adventure;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Player extends Sprite{

    Vector2 previousPosition;

    public Player(Texture texture){
        super(texture);
        previousPosition = new Vector2(this.getX(), this.getY());
    }

    public boolean hasMoved(){
        if(previousPosition.x != this.getX() || previousPosition.y != this.getY()){
            previousPosition.x = this.getX();
            previousPosition.y = this.getY();
            return true;
        }
        return false;
    }

}
