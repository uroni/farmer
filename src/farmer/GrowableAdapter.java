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
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

/**
 *
 * @author Martin
 */
public abstract class GrowableAdapter implements Positionable, Serializable
{
    protected Vector3f position=new Vector3f(0,0,0);
    protected Vector3f rotation=new Vector3f(0,0,0);
    protected float speed=1.f;
    protected boolean showArrow=false;
    protected transient Arrow arrow;
    protected transient Render3D renderer;
    protected transient String name;
    protected transient Korn korn;
    protected RootPoints rp;
    protected Vector3f curr_position,curr_direction;
    protected Vector3f gravity;
    protected transient Simulation sim;
    protected boolean first_sim;
    protected float sim_length;
    protected float sim_start;
    protected float gravity_time;
    protected boolean isChild;
    protected JunctionManager jmgr;
    protected float next_junction_time;
    protected float last_intersection_direction_age;
    protected boolean root;
    
    public GrowableAdapter(String name, Render3D renderer, Korn k, Simulation sim, boolean child, boolean root)
    {
        init(name, renderer, k, sim, child, root, false);
    }
    
    public void init(String name, Render3D renderer, Korn k, Simulation sim, boolean child, boolean root, boolean load)
    {
        this.renderer=renderer;
        this.name=name;
        this.sim=sim;
        if( !load)
            this.first_sim=!child;
        else
            this.first_sim=false;
        this.isChild=child;
        this.root=root;
        korn=k;
        
        arrow=new Arrow("arrow", Settings.view_root_arrow_length, Settings.view_root_arrow_width);
        //k.addNode(arrow);
        
        if( !load)
            rp=new RootPoints(sim, renderer, korn, root);
        else
            rp.init(sim, renderer, korn, root, true);
        
        if(load )
        {
            GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE).enqueue(new Callable(){
                public Object call()
                {
                    rp.updateAll();
                    return null;
                }
            });
        }
        
        k.addNode(rp.getNode());
        
        if(!load)
        {
            curr_position=position.clone();
            curr_direction=new Vector3f(0,1,0);
            gravity=new Vector3f();
            last_intersection_direction_age=0;
            next_junction_time=-1;
        }
        
        
        setRotation(rotation);
        recalculateGravity();
        //updateCircle();
        
        //first_sim=true;
        
        if( !load)
            jmgr=new JunctionManager(renderer, korn, rp);
        else
            jmgr.init(renderer, korn, rp);
        
        
        if( !load)
        {
            if( child==true)
            {
                sim_start=sim.getSimulatedTime();
                gravity_time=sim_start+Settings.sim_root_junction_no_gravity_time;
            }
            else
                gravity_time=sim.getSimulatedTime();
        }
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
    
    public void reset()
    {
        first_sim=true;
        rp.reset();
        sim_start=sim.getSimulatedTime();
        sim_length=0;
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
            if( root)
                arrow.setSolidColor(ColorRGBA.red);
            else
                arrow.setSolidColor(ColorRGBA.blue);
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
