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
    private PointStore ps;
    
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
        if( renderer.collides(orig, point, node, null, true,0)==null )
            return true;
        else
            return false;
    }
    
    private void getPoints(Points<Boolean> points, float radiusSQ, Vector3f orig, Vector3f cp, List<Vector3f> tp)
    {
        Vector3f [] pts=Math3D.getSurroundingPointsPlusOwn(cp, ps.getDist());
        
        for(int i=0;i<pts.length;++i)
        {
            if( orig.distanceSquared(pts[i])<=radiusSQ && points.containsPoint(pts[i])==false )
            {
                points.addPoint(pts[i], true);
                tp.add(pts[i]);
                getPoints(points,radiusSQ,orig, pts[i], tp);
            }
        }
    }
    
    public float getDensity(Vector3f p, boolean dv)
    {
        if( ps!=null && (Settings.view_dens_interpolate||!dv) )
        {
            Vector3f np=ps.getNearesPoint(p);
            
            if( dv && ps.isSet(np)==false)return 0.f;
            float radius=ps.getDist()*Settings.view_dens_interpolation_radius;
            List<Vector3f> tl=new LinkedList<Vector3f>();
            getPoints(new Points<Boolean>(), radius*radius, p, np, tl);
            
            float color=0;
            float cdiv=0;
            
            ListIterator<Vector3f> it=tl.listIterator();
            
            
            while(it.hasNext())
            {
                Vector3f cp=it.next();
                
                float dist=p.distance(cp);
                dist=((-1)/radius)*dist+1.f;
                color+=(ps.getPoint(cp)*dist);
                cdiv+=dist;
            }
            
            if(cdiv!=0)
            {
                color/=cdiv;
                if((byte)color==0)
                    color=1.f;
            }
            
            return color;
        }
        else if(ps!=null)
        {
            return ps.getPoint(p) ;
        }
        else
            return 0;
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
        
        Float minx=null, miny=null, minz=null;
        Float maxx=null, maxy=null, maxz=null;
        
        MainForm.print("Suche Punkte...");
        MainForm.setStatus("Suche Punkte...");
        
        while(queue.size()!=0)
        {
            Vector3f p=queue.get(0);
            queue.remove(0);
            
            Vector3f [] pts=Math3D.getSurroundingPoints(p, density);
            for(int i=0;i<pts.length;++i)
            {
                Vector3f cp=pts[i];
                if( points.containsPoint(cp)==false && testPoint(p, cp) )
                {
                    queue.add(cp);
                    pointlist.add(cp);
                    points.addPoint(cp, true);
                    
                    if( minx==null || cp.x<minx)
                        minx=cp.x;
                    if( miny==null || cp.y<miny)
                        miny=cp.y;
                    if( minz==null || cp.z<minz)
                        minz=cp.z;
                    
                    if( maxx==null || cp.x>maxx)
                        maxx=cp.x;
                    if( maxy==null || cp.y>maxy)
                        maxy=cp.y;
                    if( maxz==null || cp.z>maxz)
                        maxz=cp.z;
                }
            }
        }
        
        Vector3f max=new Vector3f(maxx, maxy, maxz);
        Vector3f min=new Vector3f(minx, miny, minz);
        
        MainForm.print("Found "+pointlist.size()+" Points");
        MainForm.print("Adding to PointStore...");
        MainForm.setStatus("Berechne Werte für "+pointlist.size()+" Punkte...");
        
        ps=new PointStore(min, max, density);
        
        while(pointlist.size()!=0)
        {
            MainForm.print("Populating with random values...");
            {
                ListIterator<Vector3f> it=pointlist.listIterator();

                while(it.hasNext())
                {
                    Vector3f pos=it.next();
                    Vector3f []pts=Math3D.getSurroundingPoints(pos, density);
                    int set=0;
                    for(int i=0;i<pts.length;++i)
                    {
                        byte b;
                        if( ps.isSet(pts[i]) )
                        {
                            ++set;
                        }
                    }
                    if( set==0)
                    {
                        int rnd=(int)(Math.random()*100.f+0.5f);
                        if( rnd<10)
                        {
                            ps.setPoint(pos, (byte)(Math.random()*255.f-128.f+0.5f));
                            it.remove();
                        }
                    }
                    else
                    {
                        int rnd=(int)(Math.random()*100.f+0.5f);
                        if( rnd<1)
                        {
                            ps.setPoint(pos, (byte)(Math.random()*255.f-128.f+0.5f));
                            it.remove();
                        }
                    }
                }
            }
            
            MainForm.print("Interpolating...");
            while(pointlist.size()!=0)
            {   
                boolean found=false;
                ListIterator<Vector3f> it=pointlist.listIterator();

                while(it.hasNext())
                {
                    Vector3f p=it.next();

                    Vector3f []pts=Math3D.getSurroundingPoints(p, density);
                    int color=0;                
                    int set=0;
                    for(int i=0;i<pts.length;++i)
                    {
                        byte b;
                        if( (b=ps.getPoint(pts[i]))!=0 )
                        {
                            color+=(b+128);
                            ++set;
                        }
                    }
                    
                    if(set>=2)
                    {
                        color/=set;    
                        ps.setPoint(p, (byte)(color-128));
                        found=true;
                        it.remove();
                    }
                }
                
                if( found==false )
                    break;
            }
            
            MainForm.print(pointlist.size()+" items left");
        }
        
        /*Box b=new Box("Box",min, max);
        renderer.addtoScene(b);*/
        
        MainForm.print("Points created... done.");  
        MainForm.setStatus("");
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
