/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.FastMath;
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
    private transient CLine line;
    private transient Vector3f curr_position,curr_direction;
    private transient Vector3f gravity;
    
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
        
        line=new CLine();
        k.addNode(line.getNode());
        
        curr_position=position.clone();
        curr_direction=new Vector3f(0,1,0);
        
        gravity=new Vector3f();
        setRotation(rotation);
        recalculateGravity();
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
        rotation=rot;
        Math3D.setRotation(arrow, rotation);
    }
    
    public void recalculateGravity()
    {
        Vector3f nv=new Vector3f(0,0,0);
        Vector3f nvo=new Vector3f(0,0,0);
        korn.getNode().worldToLocal(Settings.sim_root_gravity, gravity);
        korn.getNode().worldToLocal(nv, nvo);
        gravity.subtractLocal(nvo);
        gravity.normalizeLocal();
        gravity.multLocal(Settings.sim_root_gravity.length());
    }
    
    public Vector3f getRotation()
    {
        return rotation;
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
            setRotation(rotation);
            
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
    
    public void step(float time)
    {
        float max_step=time*0.0001f;
        
        Vector3f target=curr_position.clone();
        
        curr_direction.normalizeLocal();       
        
        float deg=curr_direction.angleBetween(gravity.normalize())*FastMath.RAD_TO_DEG;
        curr_direction.multLocal(Settings.sim_root_gravity_influence);
        
        if(deg>90)
            deg=90;
        
        if(deg<20)
            deg-=1;
        
        float a=Settings.sim_root_gravity_max/(FastMath.exp(-1*Settings.sim_root_gravity_k*Settings.sim_root_gravity_min_deg));
        float beug=Settings.sim_root_gravity_max-a*FastMath.exp(-1*Settings.sim_root_gravity_k*deg);
        if( beug>0)
        {
            Vector3f tmpg=gravity.clone();
            tmpg.multLocal(beug);
            curr_direction.addLocal(tmpg);
        }
        
        curr_direction.normalizeLocal();
        curr_direction.multLocal(max_step);
        
        target.addLocal(curr_direction);       
        
        line.addPoint(target);
        curr_position=target;
    }
}
