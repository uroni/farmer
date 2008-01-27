/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Martin
 */
public class Material implements Positionable, Serializable
{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private transient Render3D renderer;
    private String name;
    private int opacity=100;
    private transient Node node;
    private transient int currnum;
    private File file;
    private int scale=5;
    
    private static int numMat=0;
    
    public Node getMaterialNode()
    {
        return  node;
    }
    
    public Material(Render3D renderer, File file)
    {
        this.file=file;
        init(renderer);
    }
    
    public void init(Render3D renderer)
    {
        currnum=++numMat;
        this.renderer=renderer;
        node=renderer.loadMdl(file.getName());
        setOpacity(opacity);
        node.setLocalScale(new Vector3f(scale,scale,scale));
    }
    
    private boolean testPoint(Vector3f orig, Vector3f point)
    {
        boolean b=renderer.collides(orig, point, node, null);
        if( b )
           return false;
        return true;
    }
    
    public void calculateDensity(float density)
    {
        Vector3f center=node.getWorldBound().getCenter();    
        
        Points<Boolean> points=new Points<Boolean>();
        List<Vector3f> pointlist=new LinkedList<Vector3f>();
        
        List<Vector3f> queue=new LinkedList<Vector3f>();
        queue.add(center);
        pointlist.add(center);
        points.addPoint(center, true);
        
        while(queue.size()!=0)
        {
            Vector3f p=queue.get(0);
            queue.remove(0);
            
            Vector3f [] pts=Math3D.getSurroundingPoints(p, density);
            for(int i=0;i<pts.length;++i)
            {
                if( points.containsPoint(pts[i])==false && testPoint(p, pts[i]) )
                {
                    queue.add(pts[i]);
                    pointlist.add(pts[i]);
                    points.addPoint(p, true);
                }
            }
        }
        
        System.out.println("Found "+pointlist.size()+" Points");
    }
    
    public void setOpacity(int pc)
    {
        if( pc==0 )
        {
            renderer.removeFromScene(node);
        }
        else
        {
            if(renderer.isInScene(node)==false)
                renderer.addtoScene(node);
            
            if( pc==100 )
            {
                renderer.enableLightning(node);
            }
            else
            {
                renderer.setOpacy(node, pc);
            }            
        }
        
        opacity=pc;
    }
    
    public void update()
    {
        position=node.getLocalTranslation();
        rotation=Math3D.getRotation(node);
    }
    
    public String getName()
    {
        return "Material "+currnum;
    }    
    
    public float getRotStep(){ return Settings.ctrl_solid_rot_step; }
    public float getPosStep(){ return Settings.ctrl_solid_pos_step; }
    
    public void setPosition(Vector3f pos)
    {
        node.setLocalTranslation(pos);
    }
    
    public Vector3f getPosition()
    {
        return node.getLocalTranslation();
    }
    
    public void setRotation(Vector3f rot)
    {
        Math3D.setRotation(node, rot);
    }
    
    public Vector3f getRotation()
    {
        return Math3D.getRotation(node);
    }
    
    public int getReversed()
    {
        return 1;
    }
    
    public int getOpacity(){ return opacity; }
    
    public int getScale()
    {
        return scale;
    }
    public void setScale(int s)
    {
        scale=s;
        node.setLocalScale(new Vector3f(s,s,s));
    }
}
