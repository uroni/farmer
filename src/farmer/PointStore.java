/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.util.*;
import java.io.Serializable;

/**
 *
 * @author Martin
 */
public class PointStore implements Serializable
{
    private Vector3f max;
    private Vector3f min;
    private int xdist;
    private int ydist;
    private float dist;
    private byte []store;
    private float maxdist;
    
    public PointStore(Vector3f min, Vector3f max, float dist)
    {
        int xdist2=(int)((max.x-min.x)/dist+0.5f);
        int ydist2=(int)((max.y-min.y)/dist+0.5f);
        int zdist2=(int)((max.y-min.y)/dist+0.5f);
        store=new byte[xdist2*ydist2*zdist2];
        this.dist=dist;
        this.min=min;
        this.max=max;
        
        this.xdist=ydist2*zdist2;
        this.ydist=zdist2;
        
        maxdist=(float)Math.sqrt(dist*dist+dist*dist+dist*dist);

    }
    
    public float getDist(){
        return dist;
    }
    
    public float getMaxDist()
    {
        return maxdist;
    }
    
    private int getIdx(Vector3f v)
    {        
        int xidx=(int)((v.x-min.x)/dist+0.5f);
        int yidx=(int)((v.y-min.y)/dist+0.5f);
        int zidx=(int)((v.z-min.z)/dist+0.5f);
        
        return xidx*xdist+yidx*ydist+zidx;
    }
    
    public boolean setPoint(Vector3f v, byte data)
    {
        int idx=getIdx(v);
        if( data==0)
            ++data;
        if( idx<store.length && idx>=0)
            store[idx]=data;
        /*else
            System.out.println("ArrayIndexOutOfBounds: "+idx+"/"+store.length);*/
        return true;
    }
    
    public boolean isSet(Vector3f v)
    { 
        return !(getPoint(v)==0);
    }
    
    public byte getPoint(Vector3f v)
    {
        if( v.x<max.x && v.y<max.y&&v.z<max.z && v.x>min.x && v.y>min.y && v.z>min.z)
        {
            int idx=getIdx(v);
            if( idx>=store.length || idx<0 )
                return 0;//throw new java.lang.ArrayIndexOutOfBoundsException();

            return store[idx];     
        }
        return 0;
    }
    
    public Vector3f getNearesPoint(Vector3f v)
    {
        int xidx=(int)((v.x-min.x)/dist+0.5f);
        int yidx=(int)((v.y-min.y)/dist+0.5f);
        int zidx=(int)((v.z-min.z)/dist+0.5f);
        
        Vector3f ret=new Vector3f(min.x+xidx*dist, min.y+yidx*dist, min.z+zidx*dist);
        return ret;
    }
}
