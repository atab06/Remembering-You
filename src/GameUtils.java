import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import com.badlogic.gdx.physics.box2d.Contact;


import java.util.Arrays;
import java.util.List;

public class GameUtils
{
    // creates an Animation from a single sprite sheet
    public static Animation parseSpriteSheet(String fileName, int frameCols, int frameRows, 
    float frameDuration, PlayMode mode)
    {
        Texture t = new Texture(Gdx.files.internal(fileName), true);
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        int frameWidth = t.getWidth() / frameCols;
        int frameHeight = t.getHeight() / frameRows;

        TextureRegion[][] temp = TextureRegion.split(t, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];

        int index = 0;
        for (int i = 0; i < frameRows; i++) 
        {
            for (int j = 0; j < frameCols; j++) 
            {
                frames[index] = temp[i][j];
                index++;
            }
        }

        Array<TextureRegion> framesArray = new Array<TextureRegion>(frames);
        return new Animation(frameDuration, framesArray, mode);
    }

    // creates an Animation from a single sprite sheet
    //  with a subset of the frames, specified by an array
    public static Animation parseSpriteSheet(String fileName, int frameCols, int frameRows, 
    int[] frameIndices, float frameDuration, PlayMode mode)
    {
        Texture t = new Texture(Gdx.files.internal(fileName), true);
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        int frameWidth = t.getWidth() / frameCols;
        int frameHeight = t.getHeight() / frameRows;

        TextureRegion[][] temp = TextureRegion.split(t, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];

        int index = 0;
        for (int i = 0; i < frameRows; i++) 
        {
            for (int j = 0; j < frameCols; j++) 
            {
                frames[index] = temp[i][j];
                index++;
            }
        }

        Array<TextureRegion> framesArray = new Array<TextureRegion>();
        for (int n = 0; n < frameIndices.length; n++)
        {
            int i = frameIndices[n];
            framesArray.add( frames[i] );
        }

        return new Animation(frameDuration, framesArray, mode);
    }

    // creates an Animation from a set of image files
    // name format: fileNamePrefix + N + fileNameSuffix, where 0 <= N < frameCount
    public static Animation parseImageFiles(String fileNamePrefix, String fileNameSuffix,
    int frameCount, float frameDuration, PlayMode mode)
    {
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int n = 0; n < frameCount; n++)
        {   
            String fileName = fileNamePrefix + n + fileNameSuffix;
            Texture tex = new Texture(Gdx.files.internal(fileName));
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            frames[n] = new TextureRegion( tex );
        }

        Array<TextureRegion> framesArray = new Array<TextureRegion>(frames);
        return new Animation(frameDuration, framesArray, mode);
    }

    // searches Contact for given named objects; returns null if not found
    // assumes Body user data stores reference to associated object
    public static Box2DActor getContactObject(Contact theContact, String... names)
    {
        Box2DActor objA = (Box2DActor)theContact.getFixtureA().getBody().getUserData();
        Box2DActor objB = (Box2DActor)theContact.getFixtureB().getBody().getUserData();

        for (String name : names)
        {
            if ( objA.getName().equals(name) )
                return objA;
            if ( objB.getName().equals(name) )
                return objB;
        }

        // when name not found
        return null;
    }

    // searches Contact for given named objects; returns null if not found
    // assumes Body user data stores reference to associated object
    // assumes Fixture user data stores String name
    public static Box2DActor getContactObjectFixture(Contact theContact, String objectName, String fixtureName)
    {
        Box2DActor objA  = (Box2DActor)theContact.getFixtureA().getBody().getUserData();
        String nameA     = (String)theContact.getFixtureA().getUserData();
        Box2DActor objB  = (Box2DActor)theContact.getFixtureB().getBody().getUserData();
        String nameB     = (String)theContact.getFixtureB().getUserData();

        if ( objA.getName().equals(objectName) && nameA.equals(fixtureName) )
            return objA;

        if ( objB.getName().equals(objectName) && nameB.equals(fixtureName) )
            return objB;

        // when name not found
        return null;
    }

    // returns anything not included on the list
    public static Box2DActor getOtherContactObject(Contact theContact, String... names)
    {
        Box2DActor objA = (Box2DActor)theContact.getFixtureA().getBody().getUserData();
        Box2DActor objB = (Box2DActor)theContact.getFixtureB().getBody().getUserData();

        List<String> nameList = Arrays.asList(names);
        if ( !nameList.contains( objA.getName() ) )
            return objA;

        if ( !nameList.contains( objB.getName() ) )
            return objB;

        // when name not found
        return null;
    }

}