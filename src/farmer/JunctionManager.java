/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
class Junc implements Serializable
{
    int curr;
}

public class JunctionManager implements Serializable
{
    private List<Junc> junctions=new LinkedList<Junc>();
    private transient Render3D renderer;
    private transient Korn korn;
    private transient RootPoints rpoints;
    
    public JunctionManager(Render3D renderer, Korn k, RootPoints rpoints)
    {
        init(renderer, k, rpoints);
    }
    
    public void init(Render3D renderer, Korn k, RootPoints rpoints)
    {
        this.renderer=renderer;
        korn=k;
        this.rpoints=rpoints;
    }
    
    public void addPoint(float time, int curr)
    {
        Vector3f old_point=rpoints.getPointPos(curr-1);
        Vector3f curr_point=rpoints.getPointPos(curr);
        Vector3f next_point=rpoints.getPointPos(curr+1);
        
        
        Vector3f nold=curr_point.subtract(old_point).normalizeLocal();
        Vector3f ncurr=next_point.subtract(curr_point).normalizeLocal();
        
        float angle=nold.angleBetween(ncurr);
        if( angle>=Settings.sim_root_junction_add_limit*time)
        {
            /*Junc j=new Junc();
            
            //j.dir=nold.multLocal(-1).addLocal(ncurr).multLocal(-1).normalizeLocal();
            j.dir=curr_point.add(nold.mult(-1)).add(curr_dir);
            j.dir=j.dir.subtract(curr_point).mult(-1);
            //nold.mult(-1).addLocal(ncurr).normalizeLocal().multLocal(-1);
            //j.dir=nold;
            j.start=curr_point.add(j.dir.mult(Settings.sim_root_thikness*Settings.sim_root_junction_add));
            j.angle=angle;
            
            junctions.add(j);*/
            
            //System.out.println("Added potential Junction point");
            
            Junc j=new Junc();
            
            j.curr=rpoints.getRealIdx(curr);
            
            junctions.add(j);
        }
    }
    
    public Vector3f getNormal(int curr)
    {
        Vector3f currp=rpoints.getPointPos(curr);
        Vector3f next=rpoints.getPointPos(curr+1);
        Vector3f prev=rpoints.getPointPos(curr-1);
        if( prev==null || next==null)return null;
        Vector3f nold=currp.subtract(prev).normalizeLocal();
        Vector3f ncurr=next.subtract(currp).normalizeLocal();
        
        return nold.multLocal(-1).addLocal(ncurr).normalizeLocal().multLocal(-1);
    }
    
    public Vector3f caclulateNormal(int curr)
    {
        Vector3f normal=null;
        float max_time=rpoints.getAge(curr)+Settings.sim_root_junction_interpolation_age;
        
        int i=curr;
        while(rpoints.getAge(i)<=max_time)
        {
            if( normal==null)
                normal=getNormal(i);
            else
            {
                Vector3f n=getNormal(i);
                if(n==null)break;
                normal.addLocal(n);
                normal.normalizeLocal();
            }
            --i;
        }
        
        float min_time=rpoints.getAge(curr)-Settings.sim_root_junction_interpolation_age;
        
        i=curr+1;
        while(rpoints.getAge(i)>=min_time)
        {
            if( normal==null)
                normal=getNormal(i);
            else
            {
                Vector3f n=getNormal(i);
                if(n==null)break;
                normal.addLocal(n);
                normal.normalizeLocal();
            }
            ++i;
        }
        
        return normal;
    }
    
    public boolean addJunction()
    {
        ListIterator<Junc> it=junctions.listIterator();
        
        while(it.hasNext())
        {
            Junc j=it.next();
            
            Vector3f prev=rpoints.getPointPos(j.curr-1);
            Vector3f curr=rpoints.getPointPos(j.curr);
            Vector3f next=rpoints.getPointPos(j.curr+1);
            
            Vector3f nold=curr.subtract(prev).normalizeLocal();
            Vector3f ncurr=next.subtract(curr).normalizeLocal();
            
            float angle=nold.angleBetween(ncurr);
            
            float r=(float)Math.random()*FastMath.PI;
            if( r<angle*Settings.sim_root_junction_prob_fac)
            {
                Vector3f dir=caclulateNormal(j.curr);
                Vector3f nextp=curr.add(dir.mult(Settings.sim_root_junction_col_check_distance));
                //Vector3f start=curr.add(dir.mult(Settings.sim_root_thikness*Settings.sim_root_junction_add));
                if( renderer.collides(korn.getWorldCoordinates(curr), korn.getWorldCoordinates(nextp), null, rpoints.getSegment(j.curr).getTriMesh(), true, 0.f)==null )
                {
                    korn.addWurzel(curr, dir);
                    
                    float edist=Settings.sim_root_junction_erase_distance*Settings.sim_root_junction_erase_distance;
                    
                    ListIterator<Junc> it2=junctions.listIterator();
                    
                    while(it2.hasNext())
                    {
                        if(rpoints.getPointPos(it2.next().curr).distanceSquared(curr)<=edist)
                        {
                            it2.remove();
                        }
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
}
