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

public class GameScreen extends BaseScreen 
{
	private Player player;
	private World world;
	private Box2DDebugRenderer debugRenderer;

    private ArrayList<Box2DActor> addList;
    private ArrayList<Box2DActor> removeList;

    private OrthographicCamera tiledCamera;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    
    private int[] tileLayer = {0,1,2};
	
 // game world dimensions; initialized in loadRoom method
    private int mapWidth; 
    private int mapHeight;

    private Label itemLabel;
    private Label dialogLabel;
    private Table dialogTable;

    public boolean viewDebugRenderer;

    // each Room contains a TileMap (to pass to TileRenderer), Stage, and World.
    public HashMap<String, Room> roomStorage;

    // create a reusable event listener that can be attached to multiple worlds
    ContactListener contactEvents;

    // used to indicate room switch (when not null)
    public Portal activePortal;
    
    public GameScreen(BaseGame g) {
    	super(g);
    }
    
    public void storeRoom(String fileName)
    {
        TiledMap tiledMap = new TmxMapLoader().load(fileName);
        // calculate map dimensions from tilemap properties
        MapProperties props = tiledMap.getProperties();
        String name = (String)props.get("name");
        System.out.println("Storing TileMap Room: " + name);
        Room roomy = new Room(tiledMap);
        roomStorage.put(name, roomy);
    }

    public void loadRoom(String roomName, String spawnPointName)
    {
        if (!roomStorage.containsKey(roomName) )
            System.err.println("Error: TileMap Room " + roomName + " not found in storage.");
        else
            loadRoom( roomStorage.get(roomName), spawnPointName );
    }

    public void loadRoom(Room theRoom, String spawnPointName)
    {
        // remove player from current stage/world
        player.removeFromStage();
        player.removeFromWorld();

        world = theRoom.roomWorld;
        world.setContactListener( contactEvents );

        mainStage = theRoom.roomMainStage;

        tiledMapRenderer.setMap(theRoom.roomTileMap);

        MapProperties props = theRoom.roomTileMap.getProperties();
        int horizontalTileCount = (Integer)props.get("width");
        int   verticalTileCount = (Integer)props.get("height");
        int tileWidth = (Integer)props.get("tilewidth");
        int tileHeight = (Integer)props.get("tileheight");
        mapWidth = horizontalTileCount * tileWidth;
        mapHeight = verticalTileCount * tileHeight;

        // add player to new stage/world
        SpawnPoint sp = theRoom.getSpawnPoint(spawnPointName);
        player.setPosition( sp.x - player.getWidth()/2, sp.y - player.getHeight()/2 );
        mainStage.addActor(player);
        player.initializePhysics(world);
    }
    
    public void create() 
    {        
        // no gravity in a top-down adventure-style game
        world = new World(new Vector2(0,0), true);
        addList = new ArrayList<Box2DActor>();
        removeList = new ArrayList<Box2DActor>();
        debugRenderer = new Box2DDebugRenderer();
        viewDebugRenderer = false;
        roomStorage = new HashMap<String, Room>();
        activePortal = null;

        // player
        player = new Player();

        //////////////////
        // load tilemap //
        //////////////////

        tiledCamera = new OrthographicCamera();
        tiledCamera.setToOrtho(false,viewWidth,viewHeight);
        tiledCamera.update();

        storeRoom("TileMaps/oldForestSpawn.tmx"); // "main"

        tiledMapRenderer = new OrthogonalTiledMapRenderer(null);

        contactEvents = new ContactListener() 
        {
            public void beginContact(Contact contact) 
            {   
                Box2DActor actorA;
                Box2DActor actorB;

                actorA = GameUtils.getContactObjectFixture(contact, "Player", "action");
                actorB = GameUtils.getOtherContactObject(contact, "Player");
                if (actorA != null && actorB != null )
                {
                    player.addOverlap(actorB);
                }
            }

            public void endContact(Contact contact) 
            {
                Box2DActor actorA;
                Box2DActor actorB;

                actorA = GameUtils.getContactObjectFixture(contact, "Player", "action");
                actorB = GameUtils.getOtherContactObject(contact, "Player");
                if (actorA != null && actorB != null )
                {
                    player.removeOverlap(actorB);
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold) { }

            public void postSolve(Contact contact, ContactImpulse impulse) { }
        };

        // can only load first room after contact listener is initialized
        loadRoom("main", "start");

        ////////////////////
        // user interface //
        ////////////////////

        // the code below draws lines around Table elements
        //uiTable.debug();
        //dialogTable.debug();
    }
    
    public void update(float dt) 
    {   
        // carry out the physics simulation
        world.step(1/60f, 6, 2);

        if (activePortal != null)
        {
            loadRoom( activePortal.destinationMap, activePortal.destinationSpawnPoint );
            activePortal = null;
        }

        //if ( !player.isAttacking() )
        //{
            String playerAnim = null;
            if (Gdx.input.isKeyPressed(Keys.LEFT))
            {
                player.moveWest();
                playerAnim = "west";
            }
            if (Gdx.input.isKeyPressed(Keys.RIGHT))
            {
                player.moveEast();
                playerAnim = "east";
            }
            if ( Gdx.input.isKeyPressed(Keys.UP) )
            {
                player.moveNorth();
                playerAnim = "north";
            }
            if ( Gdx.input.isKeyPressed(Keys.DOWN) )
            {
                player.moveSouth();
                playerAnim = "south";
            }
            if (playerAnim != null)
                player.setActiveAnimation(playerAnim);
        //}

        for (Box2DActor ba : addList)
        {
            mainStage.addActor(ba);
            ba.initializePhysics(world);
        }

        for (Box2DActor ba : removeList)
        {
            String name = ba.getName();

            switch ( ba.getName() )
            {
                //case "Bush":
                //case "Stump":
                //case "Bullseye":
                case "RupeeDoor":
                {
                    ba.addAction( Actions.sequence(
                            Actions.fadeOut(0.5f),
                            Actions.removeActor()
                        ));
                    break;
                }

                //case "Rupee":
                //case "BombDrop":
                //case "ArrowDrop":
                //case "Heart":
                case "HeartContainer":
                {
                    ba.setOriginCenter();
                    ba.addAction( Actions.sequence(
                            Actions.scaleTo(0,0, 0.25f),
                            Actions.removeActor()
                        ));
                    break;
                }

                default:
                {
                    ba.removeFromStage();
                }
            }

            ba.removeFromWorld();
        }

        addList.clear();
        removeList.clear();

        // update user interface
        /*String dataString = "Health: " + player.health + " (of " + player.maxHealth + "), Rupees:" + player.rupeeCount;

        if (player.hasItem("Bow"))
            dataString += ", Arrows:" + player.arrowCount;

        if (player.hasItem("BombBag"))
            dataString += ", Bombs:" + player.bombCount;

        itemLabel.setText(dataString);*/
    }
    
 // this is the gameloop. call update methods first, then render scene.
    public void render(float dt) 
    {
        uiStage.act(dt);

        // only pause gameplay events, not UI events
        if ( !isPaused() )
        {
            mainStage.act(dt);
            update(dt);
        }

        // render
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera adjustment
        Camera mainCamera = mainStage.getCamera();
        
        // center camera on player
        mainCamera.position.x =  player.getX() + player.getOriginX();
        mainCamera.position.y =  player.getY() + player.getOriginY();
        
        // bound main camera to layout
        mainCamera.position.x = MathUtils.clamp(
            mainCamera.position.x, viewWidth/2,  mapWidth - viewWidth/2);
        mainCamera.position.y = MathUtils.clamp(
            mainCamera.position.y, viewHeight/2,  mapHeight - viewHeight/2);
        
        mainCamera.update();
        
        // adjust tilemap camera to stay in sync with main camera
        tiledCamera.position.x = mainCamera.position.x;
        tiledCamera.position.y = mainCamera.position.y;
        tiledCamera.update();
        tiledMapRenderer.setView(tiledCamera);
        tiledMapRenderer.render(tileLayer);

        mainStage.draw();
        uiStage.draw();

        if (viewDebugRenderer)
        {
            // the following code draws shapes around physics objects
            Matrix4 debugMatrix = new Matrix4(mainCamera.combined);
            // scale matrix by 100 as our box physics bodies are scaled down by 100
            debugMatrix.scale(100f, 100f, 1f);
            debugRenderer.render(world, debugMatrix);
        }
    }
    
 // process discrete input
    public boolean keyDown(int keycode)
    {
        if (keycode == Keys.P)    
            togglePaused();

        if (keycode == Keys.R)    
            game.setScreen( new GameScreen(game) );

        // NOTE: BE VERY CAREFUL WHEN SPAWNING THINGS.
        // THE EVENT LISTENER IS ASYNCHRONOUS WITH UPDATE, SO MAY CRASH PHYSICS SIMULATION.
        // MUST ADD LATER, USING LISTS OR DEFERRED ACT CALLS.
        
        return false;
    }
}
