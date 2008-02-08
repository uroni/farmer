/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
public class RootPoints
{
    Simulation sim;
    Render3D renderer;
    List<RPoint> points=new LinkedList<RPoint>();
    CLine line=new CLine();
    Segment spitze;
    List<Segment> segments=new LinkedList<Segment>();
    private Korn korn;
    int draw_mode=DRAW_LINE;
    public static int DRAW_LINE=0;
    
    private float stage1_updatetime,stage2_updatetime,stage3_updatetime,stage4_updatetime,stage5_updatetime,stage6_updatetime;
    
    
    public RootPoints(Simulation sim, Render3D renderer, Korn korn)
    {
        this.sim=sim;
        this.renderer=renderer;
        spitze=new Segment(renderer, true, sim);
        this.korn=korn;
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
    
    public void addPoint(Vector3f pos)
    {
        RPoint p=new RPoint();
        p.age=sim.getSimulatedTime();
        p.pos=pos;
        p.korn=korn;
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
                segments.add(spitze);
                spitze=new Segment(renderer, true, sim);
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
        
    }
    
    public void updateAll()
    {
        ListIterator<Segment> it=segments.listIterator();
        
        while(it.hasNext())
        {
            it.next().update();
        }
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
}
