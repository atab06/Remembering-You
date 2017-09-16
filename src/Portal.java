// now you're thinking with portals!

public class Portal extends Box2DActor
{
    // stores name of map and spawn point to go to
    public String destinationMap;
    public String destinationSpawnPoint;
    
    public Portal(float x, float y, float w, float h, String destMap, String destSpawnPoint)
    {  
        super();  
        setName("Portal");
        setPosition(x,y);
        setSize(w,h); 
        setStatic();
        setSensor();
        setShapeRectangle();
        destinationMap = destMap;
        destinationSpawnPoint = destSpawnPoint;
    }
}