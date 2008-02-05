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

/**
 *
 * @author urpc
 */
public class CLine
{
    private FloatBuffer points;
    private Line line;
    
    public CLine()
    {
        line=new Line("CLine");
        line.setMode(Line.CONNECTED);
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
    
    public Spatial getNode()
    {
        return line;
    }    
}
