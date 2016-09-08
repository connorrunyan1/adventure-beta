package com.tkc.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.tkc.adventure.MapManager;
import com.tkc.adventure.Entity;
import com.tkc.adventure.PlayerController;
import com.tkc.adventure.Utility;
import com.tkc.appwarp.WarpController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainGameScreen implements Screen{
    private static final String TAG = MainGameScreen.class.getSimpleName();

    private static class VIEWPORT{
        static float viewportWidth;
        static float viewportHeight;
        static float virtualWidth;
        static float virtualHeight;
        static float physicalWidth;
        static float physicalHeight;
        static float aspectRatio;
    }

    private float timer;
    private float delay = 1/2;
    public boolean ready = false;

    private String username = UUID.randomUUID().toString();

    private PlayerController playerController;
    private TextureRegion currentPlayerFrame;
    private Sprite currentPlayerSprite;

    private OrthogonalTiledMapRenderer mapRenderer = null;

    private OrthographicCamera camera = null;
    //private static OrthographicCamera camClone = camera;
    private static MapManager mapManager;
    private static Map<String, Entity> otherPlayers = new HashMap<String, Entity>();

    public MainGameScreen(){
        mapManager = new MapManager();
    }

    private static Entity player;

    public WarpController warpController;

    private Sprite hud;

    @Override
    public void show(){
        // todo remove when make music manager
        Music mus = Gdx.audio.newMusic(Gdx.files.internal("boss.mp3"));
        mus.setLooping(true);
        mus.play();

        WarpController.getInstance().startApp(username);
        WarpController.getInstance().setCurScreen(this);

        hud = new Sprite(new Texture(Gdx.files.internal("testhud1.png")));
        hud.setPosition(-118, -13);
        hud.setScale(0.05f, 0.05f);

        //camera setup
        setupViewport(10,10);

        // get the current size
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
        camera.zoom = 1.5f;

        mapRenderer = new OrthogonalTiledMapRenderer(mapManager.getCurrentMap(), MapManager.UNIT_SCALE);
        mapRenderer.setView(camera);

        Gdx.app.debug(TAG, "UnitScale value is: "+mapRenderer.getUnitScale());

        player = new Entity("evil.png");
        player.init(mapManager.getPlayerStartUnitScaled().x, mapManager.getPlayerStartUnitScaled().y);

        currentPlayerSprite = player.getFrameSprite();
        playerController = new PlayerController(player, camera);
        Gdx.input.setInputProcessor(playerController);
        Utility.loadTextureAsset("Other.png");
    }

    @Override
    public void hide(){

    }

    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // preferable to lock and center the camera to the player's position
        camera.position.set(currentPlayerSprite.getX(), currentPlayerSprite.getY(), 0f);
        camera.update();

        player.update(delta);
        currentPlayerFrame = player.getFrame();

        updatePortalLayerActivation(player.boundingBox);

        if(!isCollisionWithMapLayer(player.boundingBox)){
            player.setNextPositionToCurrent();
        }
        playerController.update(delta);

        mapRenderer.setView(camera);
        mapRenderer.render();

        mapRenderer.getBatch().begin();
        for(Entity ent : otherPlayers.values()){
            //System.out.println("Trying to draw a user at " + ent.getCurrentPosition().x +"," + ent.getCurrentPosition().y);
            mapRenderer.getBatch().draw(ent.getFrame(), ent.getCurrentPosition().x, ent.getCurrentPosition().y, 1, 1);
        }
        mapRenderer.getBatch().draw(currentPlayerFrame, currentPlayerSprite.getX(), currentPlayerSprite.getY(), 1, 1);
        mapRenderer.getBatch().end();

        SpriteBatch meme = new SpriteBatch();
        meme.setProjectionMatrix(camera.projection);
        meme.begin();
        hud.draw(meme);
        meme.end();

        timer += delta;
        if(timer >= delay){
            sendPositionUpdate();
            timer = 0;
            ready = true;
        }
    }

    @Override
    public void resize(int width, int height){

    }

    @Override
    public void pause(){

    }

    @Override
    public void resume(){

    }

    @Override
    public void dispose(){
        player.dispose();
        playerController.dispose();
        Gdx.input.setInputProcessor(null);
        mapRenderer.dispose();
    }

    private void setupViewport(int width, int height){
        //Make the viewport a percentage of the total display area
        VIEWPORT.virtualWidth = width;
        VIEWPORT.virtualHeight = height;

        //Current viewport dimensions
        VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
        VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;

        //pixel dimensions of display
        VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
        VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

        //aspect ratio for current viewport
        VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);

        //update viewport if there could be skewing
        if( VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio){
            //Letterbox left and right
            VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth/VIEWPORT.physicalHeight);
            VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
        }else{
            //letterbox above and below
            VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
            VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight/VIEWPORT.physicalWidth);
        }

        Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")" );
        Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")" );
    }

    public static boolean isCollisionWithMapLayer(Rectangle boundingBox){
        MapLayer mapCollisionLayer =  mapManager.getCollisionLayer();

        if( mapCollisionLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapCollisionLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( boundingBox.overlaps(rectangle) ){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean updatePortalLayerActivation(Rectangle boundingBox){
        MapLayer mapPortalLayer =  mapManager.getPortalLayer();

        if( mapPortalLayer == null ){
            return false;
        }

        Rectangle rectangle = null;

        for( MapObject object: mapPortalLayer.getObjects()){
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();
                if( boundingBox.overlaps(rectangle) ){
                    String mapName = object.getName();
                    if( mapName == null ) {
                        return false;
                    }
                    mapManager.setClosestStartPositionFromScaledUnits(player.getCurrentPosition());
                    mapManager.loadMap(mapName);
                    player.init(mapManager.getPlayerStartUnitScaled().x, mapManager.getPlayerStartUnitScaled().y);
                    Gdx.app.debug(TAG, "Portal Activated");
                    return true;
                }
            }
        }
        return false;
    }

    public void newUserJoined(String userName){
        System.out.println("User Joined: "+ userName);
        otherPlayers.put(userName, new Entity("Other.png"));
        System.out.println("Stored users:" + otherPlayers.size());
    }
    public void userLeft(String userName){
        System.out.println("User Left: "+ userName);
        otherPlayers.remove(userName);
        System.out.println("Stored users:" + otherPlayers.size());
    }
    public void sendPositionUpdate(){
        if(otherPlayers.size() >= 1){
            WarpController.getInstance().sendGameUpdate(player.getCurrentPosition().x+"#@1"+
                    player.getCurrentPosition().y);
        }
    }
    public void recieveUpdate(String userName, String xData, String yData){
        Entity curPlayer = null;
        curPlayer = otherPlayers.get(userName);
        System.out.println(userName);
        System.out.println("recieve update called, num players:" + otherPlayers.size());
        if(curPlayer != null){
            curPlayer.setCurrentPosition(Float.parseFloat(xData), Float.parseFloat(yData));
            System.out.println("other player at:" + xData + "," + yData);
        }
    }




}
