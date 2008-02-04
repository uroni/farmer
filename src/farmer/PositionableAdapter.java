/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

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
public class PositionableAdapter implements Positionable, Serializable
{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private transient Render3D renderer;
    private String name="default";
    private int opacity=100;
    private transient Node node;
    private transient int currnum;
    private int scale=5;
    private boolean transparent=false;
    
    
    public PositionableAdapter(Render3D renderer)
    {
        init(renderer);
    }
    
    public void init(Render3D renderer)
    {
        this.renderer=renderer;
        node.setLocalScale(scale);
        setOpacity(opacity);
    }
    
    public void setOpacity(int pc)
    {
        if( pc==0 )
        {
            renderer.removeFromScene(node);
        }
        else
        {
            if(renderer.isInScene(node)==false)
                renderer.addtoScene(node);
            
            if( pc==100 )
            {
                renderer.enableLightning(node);
                if( transparent)
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
        return name;
    }    
    
    public float getRotStep(){ return Settings.ctrl_default_rot_step; }
    public float getPosStep(){ return Settings.ctrl_default_pos_step; }
    
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
