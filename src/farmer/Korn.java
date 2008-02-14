/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import java.io.Serializable;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.AlphaState;

import java.util.*;
/**
 *
 * @author Martin
 */
public class Korn implements Positionable, Serializable
{
    private transient Node model;
    private transient Render3D renderer;
    private List<Wurzel> wurzeln=new LinkedList<Wurzel>();
    private Spross spross;
    private transient int sel_wurzel_idx=-1;
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);;
    private int opacity=100;
    private int scale=3;
    private float start_time;
    
    private static int kornnum=0;
    private transient int curr_kornnum;
    private transient Simulation sim;
    private transient int curr_wurzelnum=0;

    
    public Korn( Render3D renderer, Simulation sim)
    {
        init(renderer, false, sim);
        
        
        for(int i=0;i<Settings.sim_corn_init_root_count;++i)
        {
            Wurzel w=new Wurzel("Korn "+curr_kornnum+" Wurzel "+(i+1), renderer, this, sim, false);
            wurzeln.add(w);
        }
        
        spross=new Spross("Korn "+curr_kornnum+" Spross", renderer, this, sim, false);
    }
    
    public Node getNode()
    {
        return model;
    }
    
    
    
    public void init(Render3D renderer, boolean load, Simulation sim)
    {
        curr_kornnum=++kornnum;
        this.renderer=renderer;
        this.sim=sim;
        
        model=renderer.loadMdl("korn_uv3.3ds");
        
        
        if(load)
        {
            model.setLocalScale(scale);
            model.setLocalTranslation(position);
            Math3D.setRotation(model, rotation);           
            
            for(int i=0;i<wurzeln.size();++i)
            {
                Wurzel w=wurzeln.listIterator(i).next();
                w.init("Korn "+curr_kornnum+" Wurzel "+(i+1), renderer, this, sim, false, true);
            }
            
            spross.init("Korn "+curr_kornnum+" Spross", renderer, this, sim, false, true);
        }
        
        renderer.addtoScene(model);
        this.setOpacity(opacity);
        setScale(scale);
        
        start_time=sim.getSimulatedTime();
    }
    
    public void addWurzel(Vector3f pos, Vector3f dir)
    {
        ++curr_wurzelnum;
        Wurzel w=new Wurzel("Korn "+curr_kornnum+" Wurzel "+(wurzeln.size()+curr_wurzelnum),  renderer, this, sim, true);
        w.setCurrDirection(dir);
        w.setCurrPosition(pos);
        /*w.setPosition(pos);
        Vector3f rot=Math3D.getRotationToTarget(pos, pos.add(dir));
        rot.x+=90;
        w.setRotation(rot);*/
        
        wurzeln.add(w);
    }
    
    public void reset()
    {
        {
            for(int i=0;i<wurzeln.size();++i)
            {
                Wurzel w=wurzeln.get(i);
                w.reset();
            }
        }
        spross.reset();
    }
    
    public void recalculateGravity()
    {
        for(int i=0;i<wurzeln.size();++i)
        {
            wurzeln.listIterator(i).next().recalculateGravity();
        }
        if( spross!=null )
            spross.recalculateGravity();
    }
    
    public float getAge()
    {
        return sim.getSimulatedTime()-start_time;
    }
    
    public void setPosition(Vector3f pos)
    {
        model.setLocalTranslation(pos);
        model.updateGeometricState(0.f, false);
        model.updateWorldVectors();
        recalculateGravity();
        updatePositions();
    }
    
    public Vector3f getPosition()
    {
        return model.getLocalTranslation().clone();
    }
    
    public void setRotation(Vector3f rot)
    {
        Math3D.setRotation(model, rot);
        model.updateGeometricState(0.f, false);
        model.updateWorldVectors();
        recalculateGravity();
        updatePositions();
    }
    
    public void updatePositions()
    {
        for(int i=0;i<wurzeln.size();++i)
        {
            wurzeln.get(i).updatePositions();
        }
        if( spross!=null )
            spross.updatePositions();
    }
    
    public Vector3f getRotation()
    {
        return Math3D.getRotation(model);
    }
    
    public Wurzel getSelected()
    {
        return wurzeln.listIterator(sel_wurzel_idx).next();
    }
    
    public void selectWurzel(int idx)
    {
        sel_wurzel_idx=idx;
    }
    
    public int getSelectedWurzelIdx()
    {
        return sel_wurzel_idx;
    }
    
    public int getWurzelCount()
    {
        return wurzeln.size();
    }
    
    public Spross getSpross()
    {
        return spross;
    }
    
    public Vector3f getWorldCoordinates(Vector3f in)
    {
        Vector3f ret=new Vector3f();
        model.localToWorld(in, ret);
        return ret;
    }
    
    public void setDisplayRootArrows(boolean b)
    {
        ListIterator<Wurzel> it=wurzeln.listIterator();
        
        while(it.hasNext())
        {
            Wurzel w=it.next();
            w.setShowArrow(b);
        }
    }
    
    public void setShowSprossArrow(boolean b)
    {
        spross.setShowArrow(b);
    }
    
    public String getName()
    {
        return "Korn "+curr_kornnum;
    }    
    
    public float getRotStep(){ return Settings.ctrl_corn_rot_step; }
    public float getPosStep(){ return Settings.ctrl_root_pos_step; }
    
    public void addNode(Spatial node)
    {
        model.attachChild(node);
    }
    
    public void removeNode(Spatial node)
    {
        model.detachChild(node);
    }
    
    public boolean isInScene(Spatial node)
    {
        return model.hasChild(node);
    }
    
    public void update()
    {
        position=model.getLocalTranslation();
        rotation=Math3D.getRotation(model);
    }
    
    public void setOpacity(int pc)
    {
        if( pc==0 )
        {
            renderer.removeFromScene(model);
        }
        else
        {
            if(renderer.isInScene(model)==false)
                renderer.addtoScene(model);
            
            if( pc==100 )
            {
                renderer.enableLightning(model);                
            }
            else
            {
                renderer.setOpacy(model, pc);
            }            
        }
        
        opacity=pc;
    }
    
    public int getOpacity(){ return opacity; }
    
    public int getReversed()
    {
        return 1;
    }
    
    public void step(float time)
    {
        ListIterator<Wurzel> it=wurzeln.listIterator();
        
        while(it.hasNext())
        {
            Wurzel w=it.next();
            
            boolean b=w.step(time);
            if(!b)
                break;
        }
        
        spross.step(time);
    }
    
    public int getScale()
    {
        return scale;
    }
    public void setScale(int s)
    {
        scale=s;
        model.setLocalScale(s);
        updatePositions();
    }
    
    
}
