/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

//JME includes
import com.jme.bounding.BoundingBox;
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
    private PointStoreFloat waterps;
    private List<Vector3f> waterpointlist;
    private transient Simulation sim;
    private float waterdist;
    
    private static int numMat=0;
    
    public Node getMaterialNode()
    {
        return  node;
    }
    
    public Material(Render3D renderer, File file, Simulation sim)
    {
        this.file=file;
        init(renderer, sim);
    }
    
    public void init(Render3D renderer, Simulation sim)
    {
        currnum=++numMat;
        this.renderer=renderer;
        this.sim=sim;
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
    
    public boolean materialPointExists(Vector3f p)
    {
        if( ps!=null)
        {
            return ps.isSet(p);
        }
        return false;
    }
    
    public void searchPoints(float density, Points<Boolean> points, List<Vector3f>pointlist, Vector3f min, Vector3f max)
    {
        Vector3f center=node.getWorldBound().getCenter(); 
        
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
                if( points.containsPoint(cp)==false  )
                {
                    if( (Settings.sim_calc_pointsearch_test_center && testPoint(center, cp)) || (!Settings.sim_calc_pointsearch_test_center && testPoint(p, cp)))
                    {
                        queue.add(cp);
                        pointlist.add(cp);
                        points.addPoint(cp, true);
                        
                        if( pointlist.size()%10000==0)
                        {
                            MainForm.setStatus(pointlist.size()+" Punkte gefunden...");
                        }

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
            
            if( sim.shouldStopOperation() )
            {
                break;
            }
        } 
        
        max.set(new Vector3f(maxx, maxy, maxz));
        min.set(new Vector3f(minx, miny, minz));
    }
    
    public void setWaterPoint(Vector3f p, float r, float amount)
    {
        ListIterator<Vector3f> it=waterpointlist.listIterator();
        
        float rr=r*r;
        while(it.hasNext())
        {
            Vector3f vec=it.next();
            
            if( vec.distanceSquared(p)<=rr)
            {
                waterps.setPoint(vec, amount);
            }
        }
    }
    
    public void searchWaterPoints(float density)
    {
        density*=Settings.sim_calc_water_density_fac;
        
        Points<Boolean> points=new Points<Boolean>();
        waterpointlist=new LinkedList<Vector3f>();
        
        Vector3f max=new Vector3f();
        Vector3f min=new Vector3f();
        
        searchPoints(density, points, waterpointlist, min, max);
        
        MainForm.setStatus("Setzte Wert "+Settings.sim_calc_water_default+" f�r "+waterpointlist.size()+" Punkte...");
        
        ListIterator<Vector3f> it=waterpointlist.listIterator();
        
        waterps=new PointStoreFloat(min, max, density);
        
        while(it.hasNext())
        {
            Vector3f curr=it.next();
            waterps.setPoint(curr, Settings.sim_calc_water_default);     
        }
        MainForm.setStatus("");
        
        waterdist=density;
    }
    
    public byte getWater(Vector3f point)
    {
        if( waterps!=null )
        {
            float r=waterps.getPoint(point);
            if(r==-1)
                return 0;
            else
            {
                byte b=(byte)(r*255.f-128.f);
                if(b==0)++b;
                return b;
            }
        }
        else
            return 0;
    }
    
    public float getWaterF(Vector3f point)
    {
        if( waterps!=null )
        {
            float r=waterps.getPoint(point);
            if(r==-1)
                return -1;
            else
            {
                return r;
            }
        }
        else
            return 0;
    }
    
    public void setWaterF(Vector3f p, float amount)
    {
        if( waterps!=null)
        {
            if( amount>1.f)
                amount=1.f;
            if( amount<0)
                amount=0;
            waterps.setPoint(p, amount);
        }
    }
            
    
    public void calculateDensity(float density)
    {       
        Points<Boolean> points=new Points<Boolean>();
        List<Vector3f> pointlist=new LinkedList<Vector3f>();
        
        Vector3f max=new Vector3f();
        Vector3f min=new Vector3f();
        
        searchPoints(density, points, pointlist, min, max);        
        
        
        MainForm.print("Found "+pointlist.size()+" Points");
        MainForm.print("Adding to PointStore...");
        MainForm.setStatus("Berechne Werte f�r "+pointlist.size()+" Punkte...");
        
        ps=new PointStore(min, max, density);
        
        while(pointlist.size()!=0)
        {
            MainForm.print("Populating with random values...");
            {
                ListIterator<Vector3f> it=pointlist.listIterator();

                while(it.hasNext())
                {
                    Vector3f pos=it.next();
                    
                    int set=0;
                    if(Settings.sim_calc_density_stronger_interpolation)
                    {
                        Vector3f []pts=Math3D.getSurroundingPoints(pos, density);
                        for(int i=0;i<pts.length;++i)
                        {
                            byte b;
                            if( ps.isSet(pts[i]) )
                            {
                                ++set;
                            }
                        }
                    }
                    if( set==0)
                    {
                        int rnd=(int)(Math.random()*100.f+0.5f);
                        if( rnd<Settings.sim_calc_density_random_percent)
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
            renderer.removeFromSceneMat(node);
        }
        else
        {
            if(renderer.isInSceneMat(node)==false)
                renderer.addtoSceneMat(node);
            
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
    
    public void step(float time)
    {
        if(waterpointlist!=null)
        {
            ListIterator<Vector3f> it=waterpointlist.listIterator();
            
            while(it.hasNext())
            {
                Vector3f vec=it.next();
                
                float a=waterps.getPoint(vec);
                if( a==-1)
                    continue;
                
                Vector3f []pts=Math3D.getSurroundingPoints(vec, waterdist);
                for(int i=0;i<pts.length;++i)
                {
                    float b=waterps.getPoint(pts[i]);
                    if(b==-1) continue;
                    float diff=Math.abs(a-b);
                    float ex=Settings.sim_calc_water_cappilaric*time*diff;
                    if( ex>Settings.sim_calc_water_cappilaric_min*time)
                    {
                        if( a>b && a-ex>=0.f && b+ex<=1.f)
                        {
                            a-=ex;
                            b+=ex;
                        }
                        else if( a<b && a+ex<=1.f && b-ex>=0.f)
                        {
                            a+=ex;
                            b-=ex;
                        }
                        waterps.setPoint(pts[i], b);
                    }
                }
                
                waterps.setPoint(vec, a);
            }
        }
    }
    
    public float getWaterSum()
    {
        if( waterpointlist!=null)
        {
            ListIterator<Vector3f> it=waterpointlist.listIterator();
            
            float sum=0.f;
            while(it.hasNext())
            {
                float r=waterps.getPoint(it.next());
                if( r!=-1)
                    sum+=r;
            }
            
            return sum;
        }
        return -1;
    }
    
    public void addWaterTop(float amount)
    {
        if( waterpointlist!=null)
        {
            /*{
                ListIterator<Vector3f> it=waterpointlist.listIterator();
                List<Float> data=new LinkedList<Float>();
                
                while(it.hasNext())
                {
                    Vector3f p=it.next();
                    float f=(float)Math.random();
                    data.add(f);
                    waterps.setPoint(p, f);
                }
                
                it=waterpointlist.listIterator();
                ListIterator<Float> it2=data.listIterator();
                while(it.hasNext())
                {
                    Vector3f p=it.next();
                    float f=it2.next();
                    
                    float r;
                    if( (r=waterps.getPoint(p))!=f)
                    {
                        if( r==-1)
                        {
                            System.out.println("point not found...");
                        }
                        System.out.println("Test failed at "+p);
                    }
                }
            }*/
            
            
            Vector3f min=waterps.getMin();
            
            ListIterator<Vector3f> it=waterpointlist.listIterator();
            
            while(it.hasNext())
            {
                Vector3f p=it.next();
                
                if( Points.roundP(p.y)==Points.roundP(min.y) )
                {
                    waterps.setPoint(p, amount);
                }
            }
        }        
    }
}
