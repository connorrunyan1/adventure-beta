package com.tkc.adventure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.assets.AssetManager;

public final class Utility {
    public static final AssetManager assetManager = new AssetManager();
    private static final String TAG = Utility.class.getSimpleName();
    private static InternalFileHandleResolver filePathResolver = new InternalFileHandleResolver();

    // Basic Asset Management Methods =============================================================
    public static void unloadAsset(String assetFilenamePath){
        //once the asset manager is done loading
        if(assetManager.isLoaded(assetFilenamePath)){
            assetManager.unload(assetFilenamePath);
        } else {
            Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: " + assetFilenamePath);
        }
    }

    public static float loadCompleted(){
        return assetManager.getProgress();
    }

    public static int numberAssetsQueued(){
        return assetManager.getQueuedAssets();
    }

    public static boolean updateAssetLoading(){
        return assetManager.update();
    }

    public static boolean isAssetLoaded(String fileName){
        return assetManager.isLoaded(fileName);
    }

    public static void loadMapAsset(String mapFileNamePath){
        if(mapFileNamePath == null || mapFileNamePath.isEmpty()){
            return;
        }

        // load asset
        if(filePathResolver.resolve(mapFileNamePath).exists()){
            assetManager.setLoader(TiledMap.class, new TmxMapLoader(filePathResolver));
            assetManager.load(mapFileNamePath, TiledMap.class);
            // TODO until we add loading screen just block until we load map
            assetManager.finishLoadingAsset(mapFileNamePath);
            Gdx.app.debug(TAG, "Map loaded: " + mapFileNamePath);
        } else {
            Gdx.app.debug(TAG, "Map doesn't exist: " + mapFileNamePath);
        }
    }

    public static TiledMap getMapAsset(String mapFileNamePath){
        TiledMap map = null;
        // once the asset manager is done loading
        if(assetManager.isLoaded(mapFileNamePath)){
            map = assetManager.get(mapFileNamePath, TiledMap.class);
        } else {
            Gdx.app.debug(TAG, "Map is not loaded: " + mapFileNamePath);
        }
        return map;
    }

    public static void loadTextureAsset(String textureFilenamePath){
        if(textureFilenamePath == null || textureFilenamePath.isEmpty()){
            return;
        }

        // load asset
        if(filePathResolver.resolve(textureFilenamePath).exists()){
            assetManager.setLoader(Texture.class, new TextureLoader(filePathResolver));
            assetManager.load(textureFilenamePath, Texture.class);
            // TODO until we add loading screen just block until we load texture
            assetManager.finishLoadingAsset(textureFilenamePath);
        } else {
            Gdx.app.debug(TAG, "Texture doesn't exist: " + textureFilenamePath);
        }
    }

    public static Texture getTextureAsset(String textureFilenamePath){
        Texture texture = null;

        // once the asset manager is done loading
        if(assetManager.isLoaded(textureFilenamePath)){
            texture = assetManager.get(textureFilenamePath, Texture.class);
        } else {
            Gdx.app.debug(TAG, "Texture is not loaded: " + textureFilenamePath);
        }
        return texture;
    }



}
