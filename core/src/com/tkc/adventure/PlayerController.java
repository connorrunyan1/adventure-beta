package com.tkc.adventure;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class PlayerController implements InputProcessor {

    private final static String TAG = PlayerController.class.getSimpleName();

    enum Keys {
        LEFT, RIGHT, UP, DOWN, QUIT
    }

    enum Mouse {
        SELECT, DOACTION
    }

    private static Map<Keys, Boolean> keys = new HashMap<PlayerController.Keys, Boolean>();
    private static Map<Mouse, Boolean> mouseButtons = new HashMap<PlayerController.Mouse, Boolean>();
    private Vector3 lastMouseCoordinates;
    private Vector2 mouseWalkingTarget;
    private OrthographicCamera camera = null;

    //initialize hashmap for inputs
    static {
        keys.put(Keys.LEFT, false);
        keys.put(Keys.RIGHT, false);
        keys.put(Keys.UP, false);
        keys.put(Keys.DOWN, false);
        keys.put(Keys.QUIT, false);
    }

    ;

    static {
        mouseButtons.put(Mouse.SELECT, false);
        mouseButtons.put(Mouse.DOACTION, false);
    }

    ;

    private Entity player;

    public PlayerController(Entity player, OrthographicCamera camera) {
        this.lastMouseCoordinates = new Vector3();
        this.player = player;
        this.camera = camera;
        mouseWalkingTarget = new Vector2(0,0);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            this.leftPressed();
            player.pathing = false;
        }

        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            this.rightPressed();
            player.pathing = false;
        }

        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            this.upPressed();
            player.pathing = false;
        }

        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            this.downPressed();
            player.pathing = false;
        }

        if (keycode == Input.Keys.Q) {
            this.quitPressed();
            player.pathing = false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            this.leftReleased();
        }

        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            this.rightReleased();
        }

        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            this.upReleased();
        }

        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            this.downReleased();
        }

        if (keycode == Input.Keys.Q) {
            this.quitReleased();
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            this.setClickedMouseCoordinates(screenX, screenY);
        }

        //left is selection, right is context menu
        if (button == Input.Buttons.LEFT) {
            this.selectMouseButtonPressed(screenX, screenY);
        }
        if (button == Input.Buttons.RIGHT) {
            this.doActionMouseButtonPressed(screenX, screenY);
        }

        Vector3 toTranslate = new Vector3(new Vector2(screenX, screenY), 0f);
        Vector3 translated = camera.unproject(toTranslate);
        mouseWalkingTarget.x = translated.x - 0.5f;
        mouseWalkingTarget.y = translated.y;
        System.out.println("clicked at " + mouseWalkingTarget.x + "," + mouseWalkingTarget.y);
        player.pathing = true;

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //left is selection, right is context menu
        if (button == Input.Buttons.LEFT) {
            this.selectMouseButtonReleased(screenX, screenY);
        }
        if (button == Input.Buttons.RIGHT) {
            this.doActionMouseButtonReleased(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void dispose() {

    }

    //Key presses
    public void leftPressed() {
        keys.put(Keys.LEFT, true);
    }

    public void rightPressed() {
        keys.put(Keys.RIGHT, true);
    }

    public void upPressed() {
        keys.put(Keys.UP, true);
    }

    public void downPressed() {
        keys.put(Keys.DOWN, true);
    }

    public void quitPressed() {
        keys.put(Keys.QUIT, true);
    }

    public void setClickedMouseCoordinates(int x, int y) {
        lastMouseCoordinates.set(x, y, 0);
    }

    public void selectMouseButtonPressed(int x, int y) {
        mouseButtons.put(Mouse.SELECT, true);
    }

    public void doActionMouseButtonPressed(int x, int y) {
        mouseButtons.put(Mouse.DOACTION, true);
    }

    //Releases

    public void leftReleased() {
        keys.put(Keys.LEFT, false);
    }

    public void rightReleased() {
        keys.put(Keys.RIGHT, false);
    }

    public void upReleased() {
        keys.put(Keys.UP, false);
    }

    public void downReleased() {
        keys.put(Keys.DOWN, false);
    }

    public void quitReleased() {
        keys.put(Keys.QUIT, false);
    }

    public void selectMouseButtonReleased(int x, int y) {
        mouseButtons.put(Mouse.SELECT, false);
    }

    public void doActionMouseButtonReleased(int x, int y) {
        mouseButtons.put(Mouse.DOACTION, false);
    }


    public void update(float delta) {
        processInput(delta);
    }

    public static void hide() {
        keys.put(Keys.LEFT, false);
        keys.put(Keys.RIGHT, false);
        keys.put(Keys.UP, false);
        keys.put(Keys.DOWN, false);
        keys.put(Keys.QUIT, false);
    }

    private void processInput(float delta) {
        //Keyboard Input
        if (keys.get(Keys.LEFT) && keys.get(Keys.UP)) {
            player.calculateNextPosition(Entity.Direction.UPLEFT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.UPLEFT, delta);
        } else if (keys.get(Keys.RIGHT) && keys.get(Keys.UP)) {
            player.calculateNextPosition(Entity.Direction.UPRIGHT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.UPRIGHT, delta);
        } else if (keys.get(Keys.LEFT) && keys.get(Keys.DOWN)) {
            player.calculateNextPosition(Entity.Direction.DOWNLEFT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.DOWNLEFT, delta);
        } else if (keys.get(Keys.RIGHT) && keys.get(Keys.DOWN)) {
            player.calculateNextPosition(Entity.Direction.DOWNRIGHT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.DOWNRIGHT, delta);
        } else if (keys.get(Keys.UP)) {
            player.calculateNextPosition(Entity.Direction.UP, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.UP, delta);
        } else if (keys.get(Keys.LEFT)) {
            player.calculateNextPosition(Entity.Direction.LEFT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.LEFT, delta);
        } else if (keys.get(Keys.RIGHT)) {
            player.calculateNextPosition(Entity.Direction.RIGHT, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.RIGHT, delta);
        } else if (keys.get(Keys.DOWN)) {
            player.calculateNextPosition(Entity.Direction.DOWN, delta);
            player.setState(Entity.State.WALKING);
            player.setDirection(Entity.Direction.DOWN, delta);
        } else if (keys.get(Keys.QUIT)) {
            Gdx.app.exit();
        } else {
            player.setState(Entity.State.IDLE);
        }

        //Mouse input
        if (mouseButtons.get(Mouse.SELECT))
        {

        }

        if(player.pathing){
            player.pathToward(mouseWalkingTarget, delta);
        } else {
            mouseWalkingTarget = player.currentPlayerPosition.cpy();
            player.setState(Entity.State.IDLE);
        }



    }
}



