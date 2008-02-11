/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;


import com.jme.math.FastMath;
import com.jme.math.Plane;
import com.jme.math.Ray;
import java.io.Serializable;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.state.LightState;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
public abstract class GrowableAdapter implements Positionable, Serializable
{
    protected Vector3f position=new Vector3f(0,0,0);
    protected Vector3f rotation=new Vector3f(0,0,0);
    protected float speed=1.f;
    protected transient boolean showArrow=false;
    protected transient Arrow arrow;
    protected transient Render3D renderer;
    protected transient String name;
    protected transient Korn korn;
    protected transient RootPoints rp;
    protected transient Vector3f curr_position,curr_direction;
    protected transient Vector3f gravity;
    protected transient Simulation sim;
    protected transient boolean first_sim;
    protected transient float sim_length;
    protected transient float sim_start;
    protected transient float gravity_time;
    protected transient boolean isChild;
    protected transient JunctionManager jmgr;
    protected transient float next_junction_time;
    protected transient float last_intersection_direction_age;
    
    public GrowableAdapter(String name, Render3D renderer, Korn k, Simulation sim, boolean child)
    {
        init(name, renderer, k, sim, child);
    }
    
    public void init(String name, Render3D renderer, Korn k, Simulation sim, boolean child)
    {
        this.renderer=renderer;
        this.name=name;
        this.sim=sim;
        this.first_sim=!child;
        this.isChild=child;
        korn=k;
        
        arrow=new Arrow("arrow", Settings.view_root_arrow_length, Settings.view_root_arrow_width);
        //k.addNode(arrow);
        
        rp=new RootPoints(sim, renderer, korn);
        k.addNode(rp.getNode());
        
        curr_position=position.clone();
        curr_direction=new Vector3f(0,1,0);
        
        gravity=new Vector3f();
        setRotation(rotation);
        recalculateGravity();
        //updateCircle();
        last_intersection_direction_age=0;
        //first_sim=true;
        
        jmgr=new JunctionManager(renderer, korn, rp);
        next_junction_time=-1;
        
        if( child==true)
        {
            sim_start=sim.getSimulatedTime();
            gravity_time=sim_start+Settings.sim_root_junction_no_gravity_time;
        }
        else
            gravity_time=sim.getSimulatedTime();
    }
    
    public void setCurrDirection(Vector3f dir)
    {
        curr_direction=dir;
    }
    
    public void setCurrPosition(Vector3f pos)
    {
        curr_position=pos;
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
        //updateCircle();
    }
    
    public void setRotation(Vector3f rot)
    {
        rotation=rot;
        Math3D.setRotation(arrow, rotation);
        //updateCircle();
    }
    
    public void updatePositions()
    {
        rp.updateAll();
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
            
            if( !korn.isInScene(arrow))
                korn.addNode(arrow);
            
            renderer.disableLightning(arrow);
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
    
    protected Vector3f worldVector(Vector3f in)
    {
        Vector3f out=new Vector3f(0,0,0);
        korn.getNode().localToWorld(in, out);
        return out;
    }
    
    protected Vector3f worldDirection(Vector3f in)
    {
        Vector3f nv=new Vector3f(0,0,0);
        Vector3f nvo=new Vector3f(0,0,0);
        Vector3f ino=new Vector3f(0,0,0);
        korn.getNode().worldToLocal(in, ino);
        korn.getNode().worldToLocal(nv, nvo);
        ino.subtractLocal(nvo);
        ino.normalizeLocal();
        ino.multLocal(in.length());
        return ino;
    }
    
    protected Vector3f localDirection(Vector3f in)
    {
        Vector3f start=new Vector3f(0,0,0);
        Vector3f starto=new Vector3f(0,0,0);
        Vector3f ino=new Vector3f(0,0,0);
        
        korn.getNode().localToWorld(start, starto);
        korn.getNode().localToWorld(in, ino);
        
        ino.subtractLocal(starto);
        ino.normalizeLocal();
        ino.multLocal(in.length());
        return ino;
    }
    
    public float getAge()
    {
        return sim.getSimulatedTime()-sim_start;
    }
    
    public void setNextJunction()
    {
        next_junction_time=sim.getSimulatedTime()+FastMath.rand.nextFloat()*(Settings.sim_root_junction_max_time_between-Settings.sim_root_junction_min_time_between)+Settings.sim_root_junction_min_time_between;
    }
    
    public boolean makeJunctions()
    {
        if( next_junction_time==-1)
        {
            if( getAge()>=Settings.sim_root_junction_min_timeleft)
            {
                setNextJunction();
            }
        }
        else if( sim.getSimulatedTime()>=next_junction_time)
        {
            setNextJunction();
            
            return jmgr.addJunction();
        }
        
        return false;
    }
    
    public abstract boolean step(float time);
    
    
    public float getLength()
    {
        return sim_length;
    }
}
