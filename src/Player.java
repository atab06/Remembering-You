import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class Player extends Box2DActor
{
	public float speed;
	
	private Vector2 actionCenter;
	
	private ArrayList<Box2DActor> overlapList;

    private HashMap<String,Boolean> inventory;
    
    public Player() {
    	super();
    	
    	setName("Player");
    	
    	speed = 4;
        
        actionCenter = new Vector2();
        overlapList = new ArrayList<Box2DActor>();
    	
        Animation southAnim = GameUtils.parseImageFiles("Player/walk/south", ".png", 8, 0.10f, PlayMode.LOOP);
        storeAnimation( "south", southAnim );
        
        Animation northAnim = GameUtils.parseImageFiles("Player/walk/north", ".png", 8, 0.10f, PlayMode.LOOP);
        storeAnimation( "north", northAnim );

        Animation eastAnim = GameUtils.parseImageFiles("Player/walk/east", ".png", 8, 0.10f, PlayMode.LOOP);
        storeAnimation( "east", eastAnim );

        Animation westAnim = GameUtils.parseImageFiles("Player/walk/west", ".png", 8, 0.10f, PlayMode.LOOP);
        storeAnimation( "west", westAnim );

        /*Animation sNorthAnim = GameUtils.parseImageFiles("Player/sword/north", ".png", 5, 0.05f, PlayMode.NORMAL);
        storeAnimation( "swordnorth", sNorthAnim );

        Animation sSouthAnim = GameUtils.parseImageFiles("Player/sword/south", ".png", 5, 0.05f, PlayMode.NORMAL);
        storeAnimation( "swordsouth", sSouthAnim );

        Animation sEastAnim = GameUtils.parseImageFiles("Player/sword/east", ".png", 5, 0.05f, PlayMode.NORMAL);
        storeAnimation( "swordeast", sEastAnim );

        Animation sWestAnim = GameUtils.parseImageFiles("Player/sword/west", ".png", 5, 0.05f, PlayMode.NORMAL);
        storeAnimation( "swordwest", sWestAnim );*/
    }
    
 // uses data to initialize object and add to world
    public void initializePhysics(World world)
    {
        // no call to super. a straight-up overridden method.

        setDynamic();
        setPhysicsProperties(1, 0.5f, 0.0f);

        // set max speed in any direction (1.0 for apply forces, 1.5 for set velocity)
        setMaxSpeed(1.0f);

        // stop sliping and sliding
        setDamping(10);

        // must permit rotation, for action sensor
        // setFixedRotation();

        // body  position must be centered
        setOriginCenter();
        bodyDef.position.set( (getX() + getOriginX()) / 100, (getY() + getOriginY())/100 );

        // initialize a body; automatically added to world
        body = world.createBody(bodyDef);

        // store reference to this, so can access from collision
        body.setUserData(this);

        // actual hit area is much smaller than images.
        // get pixel dimensions, convert to physics dimensions
        float w = 30f / 100;
        float h = 30f / 100;
        // Create a circle shape and set its radius
        FixtureDef fixtureDef1 = new FixtureDef();
        CircleShape circ1 = new CircleShape();
        circ1.setRadius( w/2 ); // bit smaller than usual
        fixtureDef1.shape       = circ1;
        fixtureDef1.density     = fixtureDef.density;
        fixtureDef1.friction    = fixtureDef.friction;
        fixtureDef1.restitution = fixtureDef.restitution;
        // initialize a Fixture and attach it to the body
        Fixture f1 = body.createFixture(fixtureDef1);
        f1.setUserData("main");

        // sensor for "action" zone
        FixtureDef fixtureDef3 = new FixtureDef();
        PolygonShape actionZone = new PolygonShape();
        Vector2 boxCenter1 = new Vector2(w/2,0);
        float boxRotation1 = 0;

        actionZone.setAsBox( w/1.5f, w/1.5f, boxCenter1, boxRotation1 );
        fixtureDef3.shape = actionZone;
        fixtureDef3.isSensor = true;
        // because it is a sensor, density/friction/restitution irrelevant
        Fixture f3 = body.createFixture(fixtureDef3);
        f3.setUserData("action");

        circ1.dispose();
        //box1.dispose();
    }

    public void move(Vector2 direction)
    {  applyForce( direction.setLength(speed) );  }

    public void moveNorth()
    {   
        applyForce( new Vector2(0,speed) ); 
    }

    public void moveSouth()
    {  
       
        applyForce( new Vector2(0,-speed) );
     
    }

    public void moveEast()
    {  
        
        applyForce( new Vector2(speed,0) );
       
    }

    public void moveWest()
    {  
        
        applyForce( new Vector2(-speed,0) ); 
       
    }

    public float getAnimationAngle()
    {
        String name = getAnimationName();
        if (name.equals("east")) return 0 * MathUtils.degreesToRadians;
        else if (name.equals("north")) return 90 * MathUtils.degreesToRadians;
        else if (name.equals("west")) return 180 * MathUtils.degreesToRadians;
        else if (name.equals("south")) return 270 * MathUtils.degreesToRadians; 
        else 
        {
            System.out.println("nameToAngle: unknown direction name " + name );
            return -1; 
        }
    }
    
    public void act(float dt) 
    {
        super.act(dt);

        // do NOT rotate the actor (graphics), ever.
        setRotation(0);

        /*if ( isAttacking() )
        {
            setSpeed(0);
            if ( isAnimationFinished() )
            {
                String animName = getAnimationName().replace("sword", "");
                setActiveAnimation( animName );
            }
        }
        else // walking or standing...*/
        //{
            float angle = getAnimationAngle();
            // rotate the body according to the current animation's corresponding angle
            getBody().setTransform( getBody().getPosition(), angle );
            // don't allow spinning (might pick up velocity from friction even if position resets)
            getBody().setAngularVelocity(0);

            // base position + body center + extra distance
            actionCenter.set( 
                getX() + getWidth()/2  + 32 * MathUtils.cos(angle), 
                getY() + getHeight()/2 + 32 * MathUtils.sin(angle) );

            // when actually moving
            if ( getSpeed() > 0.01 )
            {
                //setActiveAnimation( angleToName(angle) );
                startAnimation();
            }
            else // standing still
            {
                setAnimationFrame(0);
                pauseAnimation();
            }
        //}
        // player should appear above any recently spawned objects
        toFront();
    }
    
 // location in pixels, for spawning actor objects
    public Vector2 getActionCenter()
    {  return actionCenter;  }

    public void addOverlap(Box2DActor ba)
    {  overlapList.add(ba);  }

    public void removeOverlap(Box2DActor ba)
    {  overlapList.remove(ba);  }

    public ArrayList<Box2DActor> getOverlapList()
    {  return overlapList;  }

    public void printOverlap()
    {
        System.out.print("Overlap: ");

        for (Box2DActor ba : overlapList)
            System.out.print( ba.getName() + ", " );

        System.out.println();
    }

    public void updateWorldPosition()
    {
        // ??? preserve angle between stages?
        // should this actually be the origin of the player??
        // Vector2 updatedWorldCenter x+w/2 / 100, ditto y
        getBody().setTransform( new Vector2(getX()/100f, getY()/100f), getAnimationAngle() );
    }
}
