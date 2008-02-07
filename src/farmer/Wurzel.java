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
public class Wurzel implements Positionable, Serializable{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private float speed=1.f;
    private transient boolean showArrow=false;
    private transient Arrow arrow;
    private transient Render3D renderer;
    private transient String name;
    private transient Korn korn;
    private transient RootPoints rp;
    private transient Vector3f curr_position,curr_direction;
    private transient Vector3f gravity;
    private transient Simulation sim;
    private transient int straigt_timeleft;
    
    public Wurzel(String name, Render3D renderer, Korn k, Simulation sim)
    {
        init(name, renderer, k, sim);
    }
    
    public void init(String name, Render3D renderer, Korn k, Simulation sim)
    {
        this.renderer=renderer;
        this.name=name;
        this.sim=sim;
        korn=k;
        
        arrow=new Arrow("arrow", Settings.view_root_arrow_length, Settings.view_root_arrow_width);
        k.addNode(arrow);
        renderer.disableLightning(arrow);
        
        rp=new RootPoints(sim);
        k.addNode(rp.getNode());
        
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
    
    private Vector3f worldVector(Vector3f in)
    {
        Vector3f out=new Vector3f(0,0,0);
        korn.getNode().localToWorld(in, out);
        return out;
    }
    
    private Vector3f worldDirection(Vector3f in)
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
    
    private Vector3f localDirection(Vector3f in)
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
    
    public void step(float time)
    {
        float max_step=time*0.0001f;
        
        Vector3f target=curr_position.clone();
        
        curr_direction.normalizeLocal();       
        
        float deg=curr_direction.angleBetween(gravity.normalize())*FastMath.RAD_TO_DEG;
        curr_direction.multLocal(Settings.sim_root_gravity_influence1);
        
        if(deg>90)
            deg=90;
        
        int dir=1;
        if(deg<20)
        {
            deg=20+(20-deg);
            dir=-1;
        }
        
        float beug;
        if( Settings.sim_root_gravity_func==0)
        {
            float a=Settings.sim_root_gravity_max/(FastMath.exp(-1*Settings.sim_root_gravity_k*Settings.sim_root_gravity_min_deg));
            beug=Settings.sim_root_gravity_max-a*FastMath.exp(-1*Settings.sim_root_gravity_k*deg);
        }
        else
        {
            beug=Settings.sim_root_gravity_max*(1/(1+FastMath.exp((Settings.sim_root_gravity_add-deg)*Settings.sim_root_gravity_k2)));
        }
        if( beug>0)
        {
            Vector3f tmpg=gravity.clone();
            tmpg.multLocal(dir*beug*time*Settings.sim_root_gravity_influence2);
            curr_direction.addLocal(tmpg);
        }
        
        curr_direction.normalizeLocal();
        curr_direction.multLocal(max_step);
        
        target.addLocal(curr_direction);
        
        Vector3f old_position=curr_position.clone();
        curr_position=target;
        
        //intersection
        Vector3f []tri;
        boolean intersection=false;
        
        int t=0;
        do
        {
            Vector3f curr_oldpos_world=worldVector(old_position);
            Vector3f curr_position_world=worldVector(curr_position);
            tri=renderer.collides(curr_oldpos_world, curr_position_world, null, null, false, Settings.sim_collison_savety_distance);
            if( tri!=null)
            {
                Vector3f curr_dir_world=curr_position_world.subtract(curr_oldpos_world);//worldDirection(curr_direction);
                
                if(t==1)
                {
                    int bla=2;
                    ++bla;
                }

                Plane plane=new Plane();
                plane.setPlanePoints(tri[0], tri[1], tri[2]);
                
                Vector3f cp1=tri[3];

                Vector3f cp2=curr_oldpos_world.add(plane.getNormal().mult(-1*plane.pseudoDistance(curr_oldpos_world)) );

                cp1.subtractLocal(cp2);

                curr_direction=localDirection(cp1);
                curr_direction.normalizeLocal();
                curr_direction.multLocal(max_step);
                
                curr_position=old_position.clone();
                curr_position.addLocal(curr_direction);
                intersection=true;
            }
            ++t;
        }while(tri!=null && t<10);
        
        if( intersection==true)
        {
            straigt_timeleft=Settings.sim_collison_straigt_time;
        }
        
        if( straigt_timeleft>0)
        {
            straigt_timeleft-=time;
            
            curr_direction.normalizeLocal();
            
            int idx=-1;
            float r;
            while((r=rp.getAge(idx))<=Settings.sim_collision_straigt_time_back && r!=0)--idx;
            ++idx;
            
            ArrayList<Vector3f> points=new ArrayList<Vector3f>();
            points.ensureCapacity(idx*-1);
            
            for(int i=idx;i<-1;++i)
            {
                Vector3f curr=rp.getPointPos(i);
                Vector3f next=rp.getPointPos(i+1);
                
                float age=rp.getAge(i);
                if(age==0)
                    continue;
                
                Vector3f vec=next.subtract(curr);
                
                float fx=(1.0f/(FastMath.log(age*Settings.sim_collision_straigt_age_mult+1)));//*(1.0f/age);
                if(fx>1.f)
                    fx=1.f;
                
                Vector3f add=curr_direction.mult(fx*time*Settings.sim_collision_straigt_mult);
                if( points.size()>0)
                {
                    add.addLocal(vec);
                    add.normalizeLocal();
                    add.multLocal(vec.length());
                    
                    Vector3f neu=points.get(points.size()-1).add(add);                    
                    points.add(neu);
                }
                else
                {
                    points.add(curr);
                }
                    
            }
            
            ListIterator<Vector3f> it=points.listIterator();
            while(it.hasNext())
            {
                rp.setPoint(it.next(), idx);
                ++idx;
            }
            
            curr_position=points.listIterator(points.size()-1).next();
            int ttime=straigt_timeleft;
            straigt_timeleft=0;
            step(time);
            straigt_timeleft=ttime;
        }
        else        
            rp.addPoint(curr_position);
    }
}

