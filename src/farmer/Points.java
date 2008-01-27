/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.util.*;

/**
 *
 * @author Martin
 */
public class Points<T>
{
    private Map<Float, Map<Float, Map<Float, T> > > map=new TreeMap<Float, Map<Float, Map<Float, T> > >();
    
    void addPoint(Vector3f p, T t)
    {
       Map<Float, Map<Float, T> > val1=map.get(p.x);
       if( val1==null )
       {
           TreeMap m2=new TreeMap<Float, T>();
           m2.put(p.z, t);
           TreeMap m1=new TreeMap<Float, Map<Float, T> >();
           m1.put(p.y, m2);           
           map.put(p.x, m1);
       }
       else
       {
           Map<Float, T> val2=val1.get(p.y);
           
           if( val2==null)
           {
               TreeMap m2=new TreeMap<Float, T>();
               m2.put(p.z, t);
               val1.put(p.y, m2);
           }
           else
           {
               if( val2.containsKey(p.z))
               {
                   return;
               }
               else
               {
                   val2.put(p.z, t);
               }
           }
       }
    }
    
    public T getPointData(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(p.x);
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(p.y);
            if( val2!=null)
            {
                return val2.get(p.z);
            }
        }
        return null;
    }
    
    public boolean containsPoint(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(p.x);
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(p.y);
            if( val2!=null)
            {
                return val2.containsKey(p.z);
            }
        }
        return false;
    }
    
    public void removePoint(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(p.x);
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(p.y);
            if( val2!=null)
            {
                val2.remove(p.z);
            }
        }
    }
}
