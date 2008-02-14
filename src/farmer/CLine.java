/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.util.geom.BufferUtils;
import java.nio.*;
import com.jme.scene.Line;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import java.io.Serializable;

/**
 *
 * @author urpc
 */
public class CLine implements Serializable
{
    private FloatBuffer points;
    private transient Line line;
    
    public CLine()
    {
        init();
    }
    
    public void init()
    {
        line=new Line("CLine");
        line.setMode(Line.CONNECTED);
    }
    
    public void reset()
    {
        if(points!=null)
        {
            points.clear();
            line.reconstruct(points, null, null, null);
            line.updateRenderState();
        }
    }
    
    public void addPoint(Vector3f vec)
    {
        int size=0;
        if(points!=null)
            size=points.capacity();
        size+=3;
        FloatBuffer nb=BufferUtils.createFloatBuffer(size);
        if( points!=null )
        {
            points.position(0);
            nb.put(points);
        }
        nb.put(vec.x);
        nb.put(vec.y);
        nb.put(vec.z);
        
        points=nb;
        
        line.reconstruct(nb, null, null, null);
        line.updateRenderState();
    }
    
    public void updatePoint(Vector3f pos, int idx)
    {
        points.position(idx*3);
        points.put(pos.x);
        points.put(pos.y);
        points.put(pos.z);
        
        line.reconstruct(points, null, null, null);
        line.updateRenderState();
    }
    
    public Spatial getNode()
    {
        return line;
    }    
}
