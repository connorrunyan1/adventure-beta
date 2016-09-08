package com.tkc.adventure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.*;

import java.util.Hashtable;

public class MapManager {
    private static final String TAG = MapManager.class.getSimpleName();

    //All Maps for the game
    private Hashtable<String, String> mapTable;
    private Hashtable<String, Vector2> playerStartLocationTable;

    // maps
    private final static String TOP_WORLD = "TOP_WORLD";
    private final static String TOWN = "TOWN";
    private final static String CASTLE_OF_DOOM = "CASTLE_OF_DOOM";
    private final static String FOREST = "TEST";

    private final static String PLAYER_START = "PLAYER START";

    private Vector2 playerStartPositionRect;
    private Vector2 closestPlayerStartPosition;
    private Vector2 convertedUnits;

    private Vector2 playerStart;
    private TiledMap currentMap = null;
    private String currentMapName;
    private MapLayer collisionLayer = null;
    private MapLayer portalLayer = null;
    private MapLayer spawnsLayer = null;

    public final static float UNIT_SCALE  = 1/32f; // todo 16?

    public MapManager(){
        playerStart = new Vector2(0,0);
        mapTable = new Hashtable<String, String>();

        mapTable.put(TOP_WORLD, "maps/topworld.tmx");
        mapTable.put(TOWN, "maps/town.tmx");
        mapTable.put(CASTLE_OF_DOOM, "maps/castleofdoom.tmx");
        mapTable.put(FOREST, "TEST.tmx");

        playerStartLocationTable = new Hashtable<String, Vector2>();
        playerStartLocationTable.put(TOP_WORLD, playerStart.cpy());
        playerStartLocationTable.put(TOWN, playerStart.cpy());
        playerStartLocationTable.put(CASTLE_OF_DOOM, playerStart.cpy());
        playerStartLocationTable.put(FOREST, playerStart.cpy());

        playerStartPositionRect = new Vector2(0,0);
        closestPlayerStartPosition = new Vector2(0,0);
        convertedUnits = new Vector2(0,0);
    }

    public void loadMap(String mapName){
        playerStart.set(0,0);

        String mapFullPath = mapTable.get(mapName);

        if(mapFullPath == null || mapFullPath.isEmpty()){
            Gdx.app.debug(TAG, "Map is invalid");
            return;
        }

        if(currentMap != null){
            currentMap.dispose();
        }

        Utility.loadMapAsset(mapFullPath);
        if(Utility.isAssetLoaded(mapFullPath)){
            currentMap = Utility.getMapAsset(mapFullPath);
            currentMapName = mapName;
        } else {
            Gdx.app.debug(TAG, "Map not loaded");
            return;
        }

        collisionLayer = currentMap.getLayers().get("MAP_COLLISION_LAYER"); // todo need quotes?
        if(collisionLayer == null){
            Gdx.app.debug(TAG, "No collision layer");
        }

        portalLayer = currentMap.getLayers().get("MAP_PORTAL_LAYER"); // todo need quotes?
        if(portalLayer == null){
            Gdx.app.debug(TAG, "No portal layer");
        }

        spawnsLayer = currentMap.getLayers().get("MAP_SPAWNS_LAYER"); // todo need remove quotes?
        if(spawnsLayer == null){
            Gdx.app.debug(TAG, "No spawn layer");
        } else {
            Vector2 start = playerStartLocationTable.get(currentMapName);
            if(start.isZero()){
                setClosestStartPosition(playerStart);
                start = playerStartLocationTable.get(currentMapName);
            }
            playerStart.set(start.x, start.y);
        }

        Gdx.app.debug(TAG, "Player Start: (" + playerStart.x + ", " + playerStart.y + ")");
    }

    public TiledMap getCurrentMap(){
        if(currentMap == null){
            currentMapName = FOREST; // todo basically sets default map
            loadMap(currentMapName);
        }
        return currentMap;
    }

    public MapLayer getCollisionLayer(){
        return collisionLayer;
    }

    public MapLayer getPortalLayer(){
        return portalLayer;
    }

    public Vector2 getPlayerStartUnitScaled(){
        Vector2 playerStart1 = playerStart.cpy();
        playerStart1.set(playerStart1.x * UNIT_SCALE, playerStart1.y * UNIT_SCALE);
        return playerStart1;
    }

    private void setClosestStartPosition(final Vector2 position){
        // Get last known position on this map
        playerStartPositionRect.set(0,0);
        closestPlayerStartPosition.set(0,0);
        float shortestDistance = 0f;

        // Go through all player start positions and choose closest to last known position
        for(MapObject object : spawnsLayer.getObjects()){
            if(object.getName().equalsIgnoreCase(PLAYER_START)){
                ((RectangleMapObject) object).getRectangle().getPosition(playerStartPositionRect);
                float distance = position.dst2(playerStartPositionRect);

                if(distance < shortestDistance || shortestDistance == 0){
                    closestPlayerStartPosition.set(playerStartPositionRect);
                    shortestDistance = distance;
                }
            }
        }
        playerStartLocationTable.put(currentMapName, closestPlayerStartPosition.cpy());
    }

    public void setClosestStartPositionFromScaledUnits(Vector2 position){
        if(UNIT_SCALE <= 0){
            return;
        }

        convertedUnits.set(position.x/UNIT_SCALE, position.y/UNIT_SCALE);
        setClosestStartPosition(convertedUnits);
    }

}
