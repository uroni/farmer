/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import java.io.Serializable;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.renderer.ColorRGBA;

/**
 *
 * @author Martin
 */
public class Wurzel implements Positionable, Serializable{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private float speed=1.f;
    private transient boolean showArrow=false;
    private transient Arrow arrow;
    private transient Render3D renderer;
    private transient String name;
    private transient Korn korn;
    
    public Wurzel(String name, Render3D renderer, Korn k)
    {
        init(name, renderer, k);
    }
    
    public void init(String name, Render3D renderer, Korn k)
    {
        this.renderer=renderer;
        this.name=name;
        korn=k;
        
        arrow=new Arrow("arrow", Settings.view_root_arrow_length, Settings.view_root_arrow_width);
        k.addNode(arrow);
        renderer.disableLightning(arrow);
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getReversed()
    {
        return -1;
    }
    
    public Vector3f getPosition()
    {
        return arrow.getLocalTranslation();
    }
    
    public void setPosition(Vector3f pos)
    {
        arrow.setLocalTranslation(pos);
        position=pos;
    }
    
    public void setRotation(Vector3f rot)
    {
        Math3D.setRotation(arrow, rot);
        rotation=rot;
    }
    
    public Vector3f getRotation()
    {
        return Math3D.toDegree(arrow.getLocalRotation());
    }
    
    public void setSpeed(float speed)
    {
        this.speed=speed;
    }
    
    public void setShowArrow(boolean b)
    {
        if( showArrow==false && b==true )
        {            
            arrow.setSolidColor(ColorRGBA.red);
            arrow.setLocalTranslation(position);
            Math3D.setRotation(arrow, rotation);
            
            //korn.addNode(arrow);
            
            showArrow=true;
        }
        else if( showArrow==true && b==false )
        {
            korn.removeNode(arrow);
            showArrow=false;
        }
    }
    
    public String toString()
    {
        return name;
    }
    
    public void setOpacity(int pc)
    {
        
    }
    
    public int getOpacity(){ return 100; }
    
    public float getRotStep(){ return Settings.ctrl_root_rot_step; }
    public float getPosStep(){ return Settings.ctrl_root_pos_step; }
    
    public int getScale()
    {
        return 5;
    }
    
    public void setScale(int s)
    {
        
    }
}
