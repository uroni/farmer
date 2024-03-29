/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
public class RootPoints implements Serializable
{
    private transient Simulation sim;
    private transient Render3D renderer;
    List<RPoint> points=new LinkedList<RPoint>();
    List<RPoint> waterpoints=new LinkedList<RPoint>();
    CLine line=new CLine();
    Segment spitze;
    List<Segment> segments=new LinkedList<Segment>();
    private transient Korn korn;
    private transient int draw_mode=DRAW_LINE;
    public static int DRAW_LINE=0;
    
    private boolean root;
    
    private float stage1_updatetime,stage2_updatetime,stage3_updatetime,stage4_updatetime,stage5_updatetime,stage6_updatetime;
    
    
    public RootPoints(Simulation sim, Render3D renderer, Korn korn, boolean root)
    {
        init(sim, renderer, korn, root, false);
        spitze=new Segment(renderer, true, sim, null, korn, root);
    }
    
    public void init(Simulation sim, Render3D renderer, Korn korn, boolean root, boolean load)
    {
        this.sim=sim;
        this.renderer=renderer;
        this.root=root;
        this.korn=korn;
        
        if(load)
        {
            Segment last=null;
            ListIterator<Segment> it=segments.listIterator();
            
            while(it.hasNext())
            {
                Segment s=it.next();
                s.init(renderer, false, sim, last, korn, root);
                last=s;
                
                if( it.hasNext())
                {
                   s.addCollidable();
                }
            }
            
            spitze.init(renderer, true, sim, last, korn, root);
        }
    }
    
    private void updateSegments(float barrier)
    {
        if( segments.size()<1)
            return;
        ListIterator<Segment> it=segments.listIterator(segments.size()-1);
        Segment curr=it.next();
        while(true)
        {            
            float age=curr.getAge();
            
            if( age<barrier)
            {
                curr.update();
            }
            else
                break;
            if( it.hasPrevious())
                curr=it.previous();
            else
                break;
        }
    }
    
    public void reset()
    {
        points.clear();
        if(line!=null)
            line.reset();
        ListIterator<Segment> it=segments.listIterator();
        while(it.hasNext())
        {
            it.next().remove();
        }
        if(spitze!=null)
        {
            spitze.remove();
            spitze=new Segment(renderer, true, sim, null, korn, root);
        }
        segments.clear();
    }
    
    public void addPoint(Vector3f pos)
    {
        RPoint p=new RPoint();
        p.age=sim.getSimulatedTime();
        p.pos=pos;
        p.korn=korn;
        p.segment=spitze;
        points.add(p);
        if( Settings.view_root_display_mode==1 || Settings.view_root_display_mode==3)
            line.addPoint(pos);
        if( Settings.view_root_display_mode==2 || Settings.view_root_display_mode==3)
        {
            spitze.add(p);
            spitze.update();

            if( spitze.getSize()>=Settings.view_root_segment_size)
            {
                spitze.setDetail(false);
                if(segments.size()>0)
                {
                    segments.get(segments.size()-1).addCollidable();
                }
                segments.add(spitze);
                spitze=new Segment(renderer, true, sim, spitze,korn, root);
                spitze.add(p);
                spitze.update();
            } 



            float simtime=sim.getSimulatedTime();

            if( simtime-stage6_updatetime>Settings.sim_root_stage6_updatetime)
            {
                updateSegments(Settings.sim_root_stage6_barrier);
                stage6_updatetime=simtime;
            }
            else if( simtime-stage5_updatetime>Settings.sim_root_stage5_updatetime)
            {
                updateSegments(Settings.sim_root_stage5_barrier);
                stage5_updatetime=simtime;
            }
            else if( simtime-stage4_updatetime>Settings.sim_root_stage4_updatetime)
            {
                updateSegments(Settings.sim_root_stage4_barrier);
                stage4_updatetime=simtime;
            }
            else if( simtime-stage3_updatetime>Settings.sim_root_stage3_updatetime)
            {
                updateSegments(Settings.sim_root_stage3_barrier);
                stage3_updatetime=simtime;
            }
            else if( simtime-stage2_updatetime>Settings.sim_root_stage2_updatetime)
            {
                updateSegments(Settings.sim_root_stage2_barrier);
                stage2_updatetime=simtime;
            }
            else if( simtime-stage1_updatetime>Settings.sim_root_stage1_updatetime)
            {
                updateSegments(Settings.sim_root_stage1_barrier);
                stage1_updatetime=simtime;
            }
        }   
        
        if( waterpoints.size()>0)
        {
            if( waterpoints.get(waterpoints.size()-1).pos.distanceSquared(pos)>Settings.sim_corn_water_distance_squared)
            {
                waterpoints.add(p);
            }
        }
        else
            waterpoints.add(p);
        
    }
    
    public void updateAll()
    {
        ListIterator<Segment> it=segments.listIterator();
        
        while(it.hasNext())
        {
            it.next().update();
        }
        
        spitze.update();
    }
    
    public Vector3f getPointPos(int idx)
    {
        return getPointPos(idx,0);
    }
    
    public Vector3f getPointPos(int idx, int maxage)
    {
        RPoint p;
        try
        {
            if( idx>=0)
                p=points.get(idx);
            else
                p=points.get(points.size()+idx);
        }
        catch(IndexOutOfBoundsException e)
        {
            return null;
        }
        
        if( maxage==0 || (sim.getSimulatedTime()-p.age)<=maxage )
        {
            return p.pos;
        }
        else
            return null;
    }
    
    public float getAge(int idx)
    {
        RPoint p;
        try
        {
            if( idx>=0)
                p=points.get(idx);
            else
                p=points.get(points.size()+idx);
        }
        catch(IndexOutOfBoundsException e)
        {
            return 0;
        }
        
        return sim.getSimulatedTime()-p.age;
    }
    
    public Segment getSegment(int idx)
    {
        RPoint p;
        try
        {
            if( idx>=0)
                p=points.get(idx);
            else
                p=points.get(points.size()+idx);
        }
        catch(IndexOutOfBoundsException e)
        {
            return null;
        }
        
        return p.segment;
    }
    
    public int getRealIdx(int idx)
    {
        if( idx>=0)
        {
            return idx;
        }
        else
        {
            return points.size()+idx;
        }
    }
    
    
    public void setPoint(Vector3f pos, int idx)
    {
        int ridx=0;
        RPoint p;
        try
        {
            if( idx>=0)
            {
                p=points.get(idx);
                ridx=idx;
            }
            else
            {
                p=points.get(points.size()+idx);
                ridx=points.size()+idx;
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            return;
        }
        
        p.pos=pos;
        if( Settings.view_root_display_mode==1 || Settings.view_root_display_mode==3)
            line.updatePoint(pos, ridx);
    }
    
    public Spatial getNode()
    {
        if(draw_mode==DRAW_LINE)
        {
            return line.getNode();
        }
        else
            return null;
    }
    
    public int getSize()
    {
        return points.size();
    }
    
    public void step(float time)
    {
        ListIterator<RPoint> it=waterpoints.listIterator();
        
        while(it.hasNext())
        {
            RPoint p=it.next();
            
            float r=Segment.calculateRadius(p, root, sim);
            
            float water=sim.getWaterAmountF(p.pos);
            
            float a=r*time*Settings.sim_corn_water_consum_mult;
            water-=a;
            
            sim.setWaterAmountF(p.pos, water);
            
            korn.setWater(korn.getWater()+a);
        }
    }
}
