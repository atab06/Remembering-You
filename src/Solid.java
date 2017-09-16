// used to create rectangular wall-like objects
public class Solid extends Box2DActor
{
    public Solid(float x, float y, float w, float h)
    {  
        super();  
        setName("Solid");
        setPosition(x,y);
        setSize(w,h); 
        setStatic();
        setShapeRectangle();
    }
}