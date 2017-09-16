import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

// box2d imports
import com.badlogic.gdx.physics.box2d.*;

// tilemap imports
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

//this class:
//- stores a tile map 
//--- Note the tilemap convention: two layers, called "TileData" and "ObjectData"
//- automatically generates and stores a corresponding Stage and World (useful for persistence)
//- TODO can place player at spawn location by name
public class Room 
{
	// assign these references when room loaded...
    public TiledMap roomTileMap;
    public Stage    roomMainStage;
    public World    roomWorld;

    private ArrayList<SpawnPoint> spawnList;
    
    public Room(TiledMap tm)
    {
        roomTileMap = tm;
        roomMainStage = new Stage( new FitViewport(BaseScreen.viewWidth, BaseScreen.viewHeight) );
        roomWorld = new World(new Vector2(0,0), true);
        spawnList = new ArrayList<SpawnPoint>();

        // calculate map dimensions from tilemap properties
        MapProperties props = roomTileMap.getProperties();
        int horizontalTileCount = (Integer)props.get("width");
        int   verticalTileCount = (Integer)props.get("height");
        int tw = (Integer)props.get("tilewidth");
        int th = (Integer)props.get("tileheight");

        // get(String) returns MapLayer, need to cast to TiledMapTileLayer
        TiledMapTileLayer groundData = (TiledMapTileLayer)roomTileMap.getLayers().get("GroundData");
        TiledMapTileLayer DecorationData = (TiledMapTileLayer)roomTileMap.getLayers().get("DecorationData");
        TiledMapTileLayer FurnitureData = (TiledMapTileLayer)roomTileMap.getLayers().get("FurnitureData");

        
        // Note: Tiled properties display default: off by one.
        //   (check the TMX file in Notepad++ to be sure)
        //   Try adding 1 to the value shown in Tiled.

        for (int tx = 0; tx < horizontalTileCount; tx++)
        {
            for (int ty = 0; ty < verticalTileCount; ty++)
            {
            	//FurnitureData Tiles
            	if ( FurnitureData.getCell(tx,ty) == null)
                    continue;

                TiledMapTile tile = FurnitureData.getCell(tx,ty).getTile();
                int tileID = tile.getId();
                float stageX = tx * tw;
                float stageY = ty * th;

                Solid box = new Solid(stageX, stageY, tw, th);
                box.initializePhysics(roomWorld);
                        
            }
        }

     // setting up objects with metadata requires object layer
        MapObjects objects = roomTileMap.getLayers().get("ObjectData").getObjects();
        for (MapObject object : objects) 
        {
            String name = object.getName();

            RectangleMapObject rectangleObject = (RectangleMapObject)object;
            Rectangle r = rectangleObject.getRectangle();

            if (name.equals("SpawnPoint"))
            {
                String spawnName = (String)object.getProperties().get("name");
                SpawnPoint sp = new SpawnPoint(spawnName, r.x + r.width/2, r.y + r.height/2);
                spawnList.add(sp);
                /*
                // create SpawnPoint object, stores name and coords.
                if ( spawnName.equals(playerSpawnName) )
                {
                // position player center at rectangle center
                player.setPosition(r.x + r.width/2 - player.getWidth()/2, r.y + r.height/2 - player.getHeight()/2);
                roomMainStage.addActor(player);
                player.initializePhysics(roomWorld);
                }
                 */
            }
           /* else if (name.equals("Portal"))
            {
                String destMap = (String)object.getProperties().get("map");
                String destSpawn = (String)object.getProperties().get("spawnpoint");
                Portal port = new Portal(r.x, r.y, r.width, r.height, destMap, destSpawn);
                port.initializePhysics(roomWorld);
            }*/
        }

    }
    
    public void createActor( Class cl, float stageX, float stageY )
    {
        try
        {
            Box2DActor r = (Box2DActor)cl.getConstructor().newInstance();
            r.setPosition(stageX, stageY);
            roomMainStage.addActor(r);
            r.initializePhysics(roomWorld);
        }
        catch (Exception e)
        {
            System.err.println("No constructor for class " + cl);
        }
    }

    public void createActor( Class cl, int cellX, int cellY, TiledMapTileLayer tileLayer, TiledMapTile replacementTile )
    {
        // replace tile graphic
        tileLayer.getCell(cellX,cellY).setTile( replacementTile );
        float stageX = cellX * 32;
        float stageY = cellY * 32;
        createActor(cl,stageX,stageY);
    }

    public SpawnPoint getSpawnPoint(String name)
    {
        for (SpawnPoint sp : spawnList)
        {
            if (sp.name.equals(name))
                return sp;
        }

        System.err.println("SpawnPoint name " + name + " not found on tilemap.");
        return null;
    }
}
