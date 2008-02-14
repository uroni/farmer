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
    
    
    public static float roundP(float in)
    {
        in*=Settings.sys_pointstore_precicion;
        in=(float)Math.round(in);
        in/=Settings.sys_pointstore_precicion;
        return in;
    }
    
    void addPoint(Vector3f p, T t)
    {
       Map<Float, Map<Float, T> > val1=map.get(roundP(p.x));
       if( val1==null )
       {
           TreeMap m2=new TreeMap<Float, T>();
           m2.put(roundP(p.z), t);
           TreeMap m1=new TreeMap<Float, Map<Float, T> >();
           m1.put(roundP(p.y), m2);           
           map.put(roundP(p.x), m1);
       }
       else
       {
           Map<Float, T> val2=val1.get(roundP(p.y));
           
           if( val2==null)
           {
               TreeMap m2=new TreeMap<Float, T>();
               m2.put(roundP(p.z), t);
               val1.put(roundP(p.y), m2);
           }
           else
           {
               if( val2.containsKey(roundP(p.z)))
               {
                   return;
               }
               else
               {
                   val2.put(roundP(p.z), t);
               }
           }
       }
    }
    
    public T getPointData(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(roundP(p.x));
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(roundP(p.y));
            if( val2!=null)
            {
                return val2.get(roundP(p.z));
            }
        }
        return null;
    }
    
    public boolean containsPoint(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(roundP(p.x));
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(roundP(p.y));
            if( val2!=null)
            {
                return val2.containsKey(roundP(p.z));
            }
        }
        return false;
    }
    
    public void removePoint(Vector3f p)
    {
        Map<Float, Map<Float, T> > val1=map.get(roundP(p.x));
        if( val1!=null )
        {
            Map<Float, T> val2=val1.get(roundP(p.y));
            if( val2!=null)
            {
                val2.remove(p.z);
            }
        }
    }
}
