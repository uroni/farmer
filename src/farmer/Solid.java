/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;
import java.io.File;
import java.io.Serializable;


/**
 *
 * @author Martin
 */
public class Solid implements Positionable, Serializable
{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private transient Render3D renderer;
    private String name;
    private int opacity=100;
    private transient Node node;
    private transient int currnum;
    private File file;
    private int scale=5;
    
    private static int numSolid=0;
    
    
    public Solid(Render3D renderer, File file)
    {
        this.file=file;
        init(renderer);
    }
    
    public void init(Render3D renderer)
    {
        currnum=++numSolid;
        this.renderer=renderer;
        node=renderer.loadMdl(file.getName());
        node.setLocalScale(scale);
        setOpacity(opacity);
    }
    
    public void setOpacity(int pc)
    {
        if( pc==0 )
        {
            renderer.removeFromSceneCol(node);
        }
        else
        {
            if(renderer.isInSceneCol(node)==false)
                renderer.addtoSceneCol(node);
            
            if( pc==100 )
            {
                renderer.enableLightning(node);                
                renderer.makeTransparent(node);
            }
            else
            {
                renderer.setOpacy(node, pc);
            }            
        }
        
        opacity=pc;
    }
    
    public int getOpacity(){ return opacity; }
    
    public void update()
    {
        position=node.getLocalTranslation();
        rotation=Math3D.getRotation(node);
    }
    
    public String getName()
    {
        return "Solid "+currnum;
    }    
    
    public float getRotStep(){ return Settings.ctrl_solid_rot_step; }
    public float getPosStep(){ return Settings.ctrl_solid_pos_step; }
    
    public void setPosition(Vector3f pos)
    {
        node.setLocalTranslation(pos);
        node.updateGeometricState(0.f, false);
    }
    
    public Vector3f getPosition()
    {
        return node.getLocalTranslation();
    }
    
    public void setRotation(Vector3f rot)
    {
        Math3D.setRotation(node, rot);
        node.updateGeometricState(0.f, false);
    }
    
    public Vector3f getRotation()
    {
        return Math3D.getRotation(node);
    }
    
    public int getReversed()
    {
        return 1;
    }
    
    public int getScale()
    {
        return scale;
    }
    public void setScale(int s)
    {
        scale=s;
        node.setLocalScale(s);
    }
}
