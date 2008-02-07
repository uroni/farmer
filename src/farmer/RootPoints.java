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
class RPoint
{
    public float age;
    public Vector3f pos;
}

public class RootPoints
{
    Simulation sim;
    List<RPoint> points=new LinkedList<RPoint>();
    CLine line=new CLine();
    int draw_mode=DRAW_LINE;
    public static int DRAW_LINE=0;
    
    
    public RootPoints(Simulation sim)
    {
        this.sim=sim;
    }
    
    public void addPoint(Vector3f pos)
    {
        RPoint p=new RPoint();
        p.age=sim.getSimulatedTime();
        p.pos=pos;
        points.add(p);
        line.addPoint(pos);
    }
    
    public Vector3f getPointPos(int idx)
    {
        return getPointPos(idx,0);
    }
    
    public Vector3f getPointPos(int idx, int maxage)
    {
        ListIterator<RPoint> it;
        try
        {
            if( idx>=0)
                it=points.listIterator(idx);
            else
                it=points.listIterator(points.size()+idx);
        }
        catch(IndexOutOfBoundsException e)
        {
            return null;
        }
        
        RPoint p=it.next();
        if( maxage==0 || (sim.getSimulatedTime()-p.age)<=maxage )
        {
            return p.pos;
        }
        else
            return null;
    }
    
    public float getAge(int idx)
    {
        ListIterator<RPoint> it;
        try
        {
            if( idx>=0)
                it=points.listIterator(idx);
            else
                it=points.listIterator(points.size()+idx);
        }
        catch(IndexOutOfBoundsException e)
        {
            return 0;
        }
        
        return sim.getSimulatedTime()-it.next().age;
    }
    
    
    
    public void setPoint(Vector3f pos, int idx)
    {
        int ridx=0;
        ListIterator<RPoint> it;
        try
        {
            if( idx>=0)
            {
                it=points.listIterator(idx);
                ridx=idx;
            }
            else
            {
                it=points.listIterator(points.size()+idx);
                ridx=points.size()+idx;
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            return;
        }
        
        it.next().pos=pos;
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
