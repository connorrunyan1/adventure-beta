package com.tkc.adventure;


import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tkc.adventure.screens.MainGameScreen;

public class Entity {
    private static final String TAG = Entity.class.getSimpleName();
    private  String defaultSpritePath = "Warrior.png";

    private Vector2 velocity;
    private Vector2 sprintVelocity;
    private String entityID;
    public boolean pathing = false;

    private Direction currentDirection = Direction.LEFT;
    private Direction previousDirection = Direction.UP;
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation walkUpAnimation;
    private Animation walkDownAnimation;
    private Animation walkDownLeftAnimation;
    private Animation walkDownRightAnimation;
    private Animation walkUpRightAnimation;
    private Animation walkUpLeftAnimation;

    private Array<TextureRegion> walkLeftFrames;
    private Array<TextureRegion> walkRightFrames;
    private Array<TextureRegion> walkUpFrames;
    private Array<TextureRegion> walkDownFrames;
    private Array<TextureRegion> walkUpRightFrames;
    private Array<TextureRegion> walkUpLeftFrames;
    private Array<TextureRegion> walkDownRightFrames;
    private Array<TextureRegion> walkDownLeftFrames;

    protected Vector2 nextPlayerPosition;
    protected Vector2 currentPlayerPosition;
    protected State state = State.IDLE;
    protected float frameTime = 0f;
    protected Sprite frameSprite = null;
    protected TextureRegion currentFrame = null;

    public final int FRAME_WIDTH = 32; // todo these might have to change up to 32
    public final int FRAME_HEIGHT = 32;
    public static Rectangle boundingBox;

    public enum State {
        IDLE, WALKING
    }

    public enum Direction {
        UP, RIGHT, DOWN, LEFT, UPRIGHT, UPLEFT, DOWNRIGHT, DOWNLEFT
    }

    public Entity(String name) {
        defaultSpritePath = name;
        initEntity();
    }

    public void initEntity() {
        this.entityID = UUID.randomUUID().toString();
        this.nextPlayerPosition = new Vector2();
        this.currentPlayerPosition = new Vector2();
        this.boundingBox = new Rectangle();
        this.velocity = new Vector2(5f, 5f);
        this.sprintVelocity = new Vector2(3f, 3f);

        System.out.println("about to load texture: "+ defaultSpritePath);
        Utility.loadTextureAsset(defaultSpritePath);
        loadDefaultSprite();
        loadAllAnimations();
    }

    public void update(float delta) {
        frameTime = (frameTime + delta) % 5; // modulo to avoid overflow lol

        // want hitbox to be at feet for better feel
        setBoundingBoxSize(0f, 0.5f);
    }

    public void init(float startX, float startY) {
        this.currentPlayerPosition.x = startX;
        this.currentPlayerPosition.y = startY;

        this.nextPlayerPosition.x = startX;
        this.nextPlayerPosition.y = startY;
    }

    public void setBoundingBoxSize(float percentageWidthReduced, float percentageHeightReduced) {
        //update current bounding box
        float width;
        float height;

        float widthReductionAmmount = 1.0f - percentageWidthReduced; //.8f for 20% (1 - .20)
        float heightReductionAmmount = 1.0f - percentageHeightReduced; //.8f for 20% (1 - .20)

        if(widthReductionAmmount > 0 && widthReductionAmmount < 1){
            width = FRAME_WIDTH * widthReductionAmmount;
        } else {
            width = FRAME_WIDTH;
        }

        if(heightReductionAmmount > 0 && heightReductionAmmount < 1){
            height = FRAME_HEIGHT * heightReductionAmmount;
        } else {
            height = FRAME_HEIGHT;
        }

        if(width == 0 || height == 0){
            Gdx.app.debug(TAG, "Width and Height are 0" + width + ":" +height);
        }

        // need to account for unit scale, godbless
        float minX;
        float minY;
        if(MapManager.UNIT_SCALE > 0){
            minX = nextPlayerPosition.x / MapManager.UNIT_SCALE;
            minY = nextPlayerPosition.y / MapManager.UNIT_SCALE;
        } else {
            minX = nextPlayerPosition.x;
            minY = nextPlayerPosition.y;
        }

        boundingBox.set(minX, minY, width, height);
    }

    private void loadDefaultSprite(){
        Texture texture = Utility.getTextureAsset(defaultSpritePath);
        TextureRegion[][] textureFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
        frameSprite = new Sprite(textureFrames[0][0].getTexture(), 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        currentFrame = textureFrames[0][0];
    }

    private void loadAllAnimations(){
        //Walking animation
        Texture texture = Utility.getTextureAsset(defaultSpritePath);
        TextureRegion[][] textureFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
        walkDownFrames = new Array<TextureRegion>(4);
        walkLeftFrames = new Array<TextureRegion>(4);
        walkRightFrames = new Array<TextureRegion>(4);
        walkUpFrames = new Array<TextureRegion>(4);
        walkUpRightFrames = new Array<TextureRegion>(4);
        walkUpLeftFrames = new Array<TextureRegion>(4);
        walkDownRightFrames = new Array<TextureRegion>(4);
        walkDownLeftFrames = new Array<TextureRegion>(4);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                TextureRegion region = textureFrames[i][j];
                if( region == null ){
                    Gdx.app.debug(TAG, "Got null animation frame " + i + "," + j);
                }
                switch(i)
                {
                    case 0:
                        walkDownFrames.insert(j, region);
                        break;
                    case 1:
                        walkLeftFrames.insert(j, region);
                        break;
                    case 2:
                        walkRightFrames.insert(j, region);
                        break;
                    case 3:
                        walkUpFrames.insert(j, region);
                        break;
                    case 4:
                        walkDownLeftFrames.insert(j, region);
                        break;
                    case 5:
                        walkUpLeftFrames.insert(j, region);
                        break;
                    case 6:
                        walkDownRightFrames.insert(j, region);
                        break;
                    case 7:
                        walkUpRightFrames.insert(j, region);
                        break;
                }
            }
        }

        walkDownAnimation = new Animation(0.25f, walkDownFrames, Animation.PlayMode.LOOP);
        walkLeftAnimation = new Animation(0.25f, walkLeftFrames, Animation.PlayMode.LOOP);
        walkRightAnimation = new Animation(0.25f, walkRightFrames, Animation.PlayMode.LOOP);
        walkUpAnimation = new Animation(0.25f, walkUpFrames, Animation.PlayMode.LOOP);
        walkDownLeftAnimation = new Animation(0.25f, walkDownLeftFrames, Animation.PlayMode.LOOP);
        walkDownRightAnimation = new Animation(0.25f, walkDownRightFrames, Animation.PlayMode.LOOP);
        walkUpLeftAnimation = new Animation(0.25f, walkUpLeftFrames, Animation.PlayMode.LOOP);
        walkUpRightAnimation = new Animation(0.25f, walkUpRightFrames, Animation.PlayMode.LOOP);
    }

    public void dispose(){
        Utility.unloadAsset(defaultSpritePath);
    }

    public void setState(State state){
        this.state = state;
    }

    public Sprite getFrameSprite(){
        return frameSprite;
    }

    public TextureRegion getFrame(){
        return currentFrame;
    }

    public Vector2 getCurrentPosition(){
        return currentPlayerPosition;
    }

    public void setCurrentPosition(float currentPositionX, float currentPositionY){
        frameSprite.setX(currentPositionX);
        frameSprite.setY(currentPositionY);
        this.currentPlayerPosition.x = currentPositionX;
        this.currentPlayerPosition.y = currentPositionY;
    }

    public void setDirection(Direction direction,  float deltaTime){
        this.previousDirection = this.currentDirection;
        this.currentDirection = direction;

        //Look into the appropriate variable when changing position

        switch (currentDirection) {
            case DOWN :
                currentFrame = walkDownAnimation.getKeyFrame(frameTime);
                break;
            case LEFT :
                currentFrame = walkLeftAnimation.getKeyFrame(frameTime);
                break;
            case UP :
                currentFrame = walkUpAnimation.getKeyFrame(frameTime);
                break;
            case RIGHT :
                currentFrame = walkRightAnimation.getKeyFrame(frameTime);
                break;
            case DOWNLEFT:
                currentFrame = walkDownLeftAnimation.getKeyFrame(frameTime);
                break;
            case DOWNRIGHT:
                currentFrame = walkDownRightAnimation.getKeyFrame(frameTime);
                break;
            case UPLEFT:
                currentFrame = walkUpLeftAnimation.getKeyFrame(frameTime);
                break;
            case UPRIGHT:
                currentFrame = walkUpRightAnimation.getKeyFrame(frameTime);
                break;
            default:
                break;
        }
    }

    public void setNextPositionToCurrent(){
        setCurrentPosition(nextPlayerPosition.x, nextPlayerPosition.y);
    }

    public void calculateNextPosition(Direction currentDirection, float deltaTime){
        float testX = currentPlayerPosition.x;
        float testY = currentPlayerPosition.y;

        velocity.scl(deltaTime);

        if(currentDirection == Direction.LEFT) {
            testX -= velocity.x;
        }

        if(currentDirection == Direction.RIGHT) {
            testX += velocity.x;
        }

        if(currentDirection == Direction.UP) {
            testY += velocity.y;
        }

        if(currentDirection == Direction.DOWN) {
            testY -= velocity.y;
        }

        if(currentDirection == Direction.UPRIGHT) {
            testY += velocity.y;
            testX += velocity.x;
        }

        if(currentDirection == Direction.UPLEFT) {
            testY += velocity.y;
            testX -= velocity.x;
        }

        if(currentDirection == Direction.DOWNRIGHT) {
            testY -= velocity.y;
            testX += velocity.x;
        }

        if(currentDirection == Direction.DOWNLEFT) {
            testY -= velocity.y;
            testX -= velocity.x;
        }

        nextPlayerPosition.x = testX;
        nextPlayerPosition.y = testY;

        //velocity
        velocity.scl(1 / deltaTime);
    }

    public void pathToward(Vector2 goal, float deltaTime){
        if(pathing == false){
            setState(State.IDLE);
            return;
        }
        Vector2 prevPosition = currentPlayerPosition.cpy();
        float xComponent = goal.x - currentPlayerPosition.x;
        float yComponent = goal.y - currentPlayerPosition.y;
        float mathy = MathUtils.atan2(yComponent, xComponent);
        mathy = mathy * MathUtils.radiansToDegrees;

        if(mathy < 0){
            mathy = -mathy;
            mathy = 360 - mathy;
        }

        // at this point, mathy is an angle, 0 points directly left, counterclockwise
        // first, do any diagonal pathing
        // then, do any normal pathing
        if(mathy > 337.5 || mathy <= 22.5 ){
            // go right
            setState(State.WALKING);
            setDirection(Direction.RIGHT, deltaTime);
            calculateNextPosition(Direction.RIGHT, deltaTime);
        } else if (mathy > 22.5 && mathy <= 67.5 ){
            // go upright
            setState(State.WALKING);
            setDirection(Direction.UPRIGHT, deltaTime);
            calculateNextPosition(Direction.UPRIGHT, deltaTime);
        }else if (mathy > 76.5 && mathy <= 112.5 ){
            // go up
            setState(State.WALKING);
            setDirection(Direction.UP, deltaTime);
            calculateNextPosition(Direction.UP, deltaTime);
        }else if (mathy > 112.5 && mathy <= 157.5 ){
            // go upleft
            setState(State.WALKING);
            setDirection(Direction.UPLEFT, deltaTime);
            calculateNextPosition(Direction.UPLEFT, deltaTime);
        }else if (mathy > 157.5 && mathy <= 202.5 ){
            // go left
            setState(State.WALKING);
            setDirection(Direction.LEFT, deltaTime);
            calculateNextPosition(Direction.LEFT, deltaTime);
        }else if (mathy > 202.5 && mathy <= 247.5 ){
            // go downleft
            setState(State.WALKING);
            setDirection(Direction.DOWNLEFT, deltaTime);
            calculateNextPosition(Direction.DOWNLEFT, deltaTime);
        }else if (mathy > 247.5 && mathy <= 292.5 ){
            // go down
            setState(State.WALKING);
            setDirection(Direction.DOWN, deltaTime);
            calculateNextPosition(Direction.DOWN, deltaTime);
        }else if (mathy > 292.5 && mathy <= 337.5 ){
            // go downright
            setState(State.WALKING);
            setDirection(Direction.DOWNRIGHT, deltaTime);
            calculateNextPosition(Direction.DOWNRIGHT, deltaTime);
        } else{
            // do nothing
        }

        if(pathing){
            setCurrentPosition(nextPlayerPosition.x, nextPlayerPosition.y);
        }

        if(MainGameScreen.isCollisionWithMapLayer(boundingBox)){
            pathing = false;
        }

        // check if at destination
        float xDist = (currentPlayerPosition.x) - goal.x;
        float yDist = currentPlayerPosition.y - goal.y;
        if(xDist < 0){
            xDist = -xDist;
        }
        if(yDist < 0){
            yDist = -yDist;
        }

        if(xDist < 0.1 && yDist < 0.1){
            pathing = false;
            setState(State.IDLE);
            // you are close enough lol
            System.out.println("reached goal so stopped pathing, distance: " + xDist + "," + yDist);
        }

        //if(currentPlayerPosition.epsilonEquals(prevPosition, Float.MIN_VALUE)){
         //   pathing = false;
         //   System.out.println("blocked so no more pathing");
       // }



    }

}
