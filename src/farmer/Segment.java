/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.LineSegment;
import com.jme.math.Plane;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.util.geom.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
public class Segment
{
    private List<RPoint> points;
    private Render3D renderer;
    private float size;
    private TriMesh trimesh;
    private boolean inscene=false;
    private boolean use_detail;
    private Simulation simulation;
    private boolean collidable;
    private Segment previous;
    private Vector3f last_rotation=new Vector3f(0,0,0);
    private static int number=1;
    
    public Segment(Render3D renderer, boolean use_detail, Simulation sim, Segment previous)
    {
        
        init(renderer, use_detail, sim, previous);
    }
    
    public void init(Render3D renderer, boolean use_detail, Simulation sim, Segment previous)
    {
        points=new LinkedList<RPoint>();
        this.renderer=renderer;
        this.previous=previous;
        trimesh=new TriMesh("test"+number++);
        this.use_detail=use_detail;
        simulation=sim;
        //trimesh.setLightCombineMode(LightState.OFF);
    }
    
    public void setDetail(boolean b)
    {
        if( !b && use_detail)
        {
            ListIterator<RPoint> it=points.listIterator();
            
            RPoint last=it.next();
            while(it.hasNext())
            {
                RPoint curr=it.next();
                float d=last.pos.distance(curr.pos);
                if( d<Settings.view_root_detail && it.hasNext()==true)
                    it.remove();
                else
                    last=curr;
            }
        }
        
        use_detail=b;
    }
    
    public float getSize()
    {
        return size;
    }
    
    public float getAge()
    {
        if( points.size()>0)
        {
            RPoint p=points.get(points.size()-1);
            return simulation.getSimulatedTime()-p.age;
        }
        return 0.f;
    }
    
    public void add(RPoint p)
    {
        if( points.size()>0)
        {
            RPoint last=points.get(points.size()-1);
            if(use_detail==false)
            {
                if( last.pos.distance(p.pos)>=Settings.view_root_detail)
                {
                    size+=p.pos.subtract(last.pos).length();
                    points.add(p);
                }
            }
            else
            {
                size+=p.pos.subtract(last.pos).length();
                points.add(p);
            }
        }
        else
            points.add(p);
    }
    
    private float calculateRadius(RPoint p)
    {
        return Settings.sim_root_thikness*FastMath.log((simulation.getSimulatedTime()-p.age)*Settings.sim_root_pointness+1);
    }
    
    private void addBall(Vector3f pos)
    {
        Sphere ball=new Sphere("ball", 10, 10, 0.1f);
        ball.setLocalTranslation(pos);
        ball.setSolidColor(ColorRGBA.pink);
        renderer.addtoScene(ball);
    }
    
    private int addPoints(int idx, RPoint curr, RPoint prev, RPoint next, float[] vertices, float []normals, boolean first)
    {
        Vector3f rot;
        if(next!=null)
            rot=Math3D.getRotationToTarget(prev.pos, next.pos);
        else
        {
            if( first && previous!=null)
            {
                rot=previous.getLastRotation().clone();
            }
            else
            {
                rot=Math3D.getRotationToTarget(prev.pos, curr.pos);
                if(!first)
                    last_rotation=rot.clone();
            }                
        }
        rot.x+=90;
        Vector3f [] pts=Math3D.getCircleSegments(curr.pos, rot, calculateRadius(curr));
        Node node=null;
        Vector3f normal_start=new Vector3f();
        if( curr.korn!=null )
        {
            node=curr.korn.getNode();
            node.localToWorld(curr.pos, normal_start);
        }
        else
            normal_start.set(curr.pos);
        
        for(int i=0;i<pts.length;++i)
        {
            Vector3f out=new Vector3f();
            if( node!=null)
                node.localToWorld(pts[i], out);
            else
                out=pts[i];
            
            Vector3f normal=out.subtract(normal_start);
            normal.normalizeLocal();
            //normal.multLocal(-1);
            
            //if( i==0 || i==pts.length/4)
                //addBall(out);
            
            vertices[idx]=out.x;
            normals[idx]=normal.x;
            ++idx;
            vertices[idx]=out.y;
            normals[idx]=normal.y;
            ++idx;
            vertices[idx]=out.z;
            normals[idx]=normal.z;
            ++idx;
        }
        
        return idx;
    }
    
    private int minIdx(int source, int start_t2, int max_t2, float [] vertices, List<Integer> forbidden)
    {
        int min_start=-1;
        Vector3f startp=getVec(source, vertices);
        float min_start_distance=-1;//new Vector3f(vertices[start_t2*3],vertices[start_t2*3+1],vertices[start_t2*3+2]).subtractLocal(startp).lengthSquared();
        
        for(int i=start_t2+1;i<max_t2;++i)
        {
            float distance=getVec(i,vertices).subtractLocal(startp).lengthSquared();
            if( min_start_distance==-1 || distance<min_start_distance)
            {
                if( forbidden==null || forbidden.contains(i)==false)
                {
                    min_start_distance=distance;
                    min_start=i;                
                }
            }
        }       
        
        return min_start;
    }
    
    private Vector3f getVec(int idx, float[]vertices)
    {
        int d=idx*3;
        return new Vector3f(vertices[d],vertices[d+1],vertices[d+2]);
    }
    
    public Vector3f transformCoord(RPoint p)
    {
        if(p.korn!=null)
        {
            Vector3f v=new Vector3f();
            p.korn.getNode().localToWorld(p.pos, v);
            return v;
        }
        return p.pos;
    }
    
    private int calculateIndexes(int iidx, int idx, int[] indexes, float [] vertices, RPoint prev, RPoint curr)
    {
        int idx3=idx/3;
        int start_t1=idx3-2*Settings.sim_root_circle_segments;
        
        
        /*List<Integer> forbidden=new ArrayList<Integer>();
        int start_t2=minIdx(start_t1,idx3-Settings.sim_root_circle_segments, idx3, vertices, null );
        forbidden.add(start_t2);
        int next=start_t2;
        for(int i=1;next==start_t2;++i)
        {
            int lidx=start_t1+i*prev.dir;
            lidx=((lidx-start_t1)%Settings.sim_root_circle_segments);
            if(lidx<0)
                lidx=Settings.sim_root_circle_segments+lidx;
            lidx+=start_t1;
            next=minIdx(lidx, idx3-Settings.sim_root_circle_segments, idx3, vertices, forbidden);
        }
        
        int dir;
        if( next==start_t2+1)
            dir=-1;
        else
            dir=1;*/
        
        int t2start=idx3-Settings.sim_root_circle_segments;
        
        int pidx1=start_t1+Settings.sim_root_circle_segments/4;
        int pidx2=t2start+Settings.sim_root_circle_segments/4;
        
        int t11=start_t1;
        int t12=start_t1+Settings.sim_root_circle_segments/2;
        int t13=t2start+Settings.sim_root_circle_segments/2;
        
        /*int t21=start_t1;
        int t22=t2start;
        int t23=t2start+Settings.sim_root_circle_segments/2;*/
        
        Vector3f vec=getVec(pidx1, vertices);
        Vector3f direction=getVec(pidx2, vertices).subtractLocal(vec);
       
        Ray r=new Ray(vec, direction.normalizeLocal());
        
        Plane plane=new Plane();
        plane.setPlanePoints(getVec(t11,vertices), getVec(t12,vertices), getVec(t13,vertices));
        int dir;
       if( plane.whichSide(getVec(pidx1, vertices))!=plane.whichSide(getVec(pidx2, vertices)))//r.intersect(getVec(t11,vertices), getVec(t12,vertices), curr.pos))/*r.intersect(getVec(t11,vertices), getVec(t12,vertices), getVec(t13,vertices))
               // ||r.intersect(getVec(t21,vertices), getVec(t22,vertices), getVec(t23,vertices)) )*/
        {
            dir=-1;
        }
        else
            dir=1;
        
        //int pidx1;
        /*if( prev.dir==-1)
            pidx1=start_t1+Settings.sim_root_circle_segments/4+Settings.sim_root_circle_segments/2;
        else*/
        /*int pidx1=start_t1+Settings.sim_root_circle_segments/4;
        int pidx2=t2start+Settings.sim_root_circle_segments/4;
        int pidx3=t2start;
        int pidx4=start_t1;
        int pidx5=t2start+Settings.sim_root_circle_segments/4+Settings.sim_root_circle_segments/2;
        
        Vector3f v1=getVec(pidx1, vertices);
        
        float d1=getVec(pidx2, vertices).subtractLocal(v1).lengthSquared();
        float d2=getVec(pidx4, vertices).subtractLocal(getVec(pidx3, vertices)).lengthSquared();
        float d3=getVec(pidx5, vertices).subtractLocal(v1).lengthSquared();
        
        int dir;
        if( FastMath.abs(d1-d2)>FastMath.abs(d3-d2))
            dir=-1;
        else
            dir=1;*/
            
        int start_t2;
        if(dir==-11)
            start_t2=idx3-1;
        else
            start_t2=idx3-Settings.sim_root_circle_segments;
        
        /*int dir=-1;
        int r;
        int testidx;
        if( prev.dir==1 )
        {
            testidx=start_t1+Settings.sim_root_circle_segments/4;
        }
        else
            testidx=start_t1+Settings.sim_root_circle_segments/2+Settings.sim_root_circle_segments/4;
        
        if( (r=minIdx(testidx,idx3-Settings.sim_root_circle_segments, idx3, vertices)-(idx3-Settings.sim_root_circle_segments))!=(start_t2-Settings.sim_root_circle_segments/2-Settings.sim_root_circle_segments/4)%Settings.sim_root_circle_segments )
        {
            dir=1;
            start_t2=idx3-Settings.sim_root_circle_segments;
        } */
        
        
            
        curr.dir=dir;
        
        int curr_t1=start_t1;
        int curr_t2=start_t2;
        
        for(int i=0;i<Settings.sim_root_circle_segments;++i)
        {
            if( dir==-1)
            {
                indexes[iidx]=curr_t1;
                curr_t1+=1;
                curr_t1=start_t1+(curr_t1-start_t1)%Settings.sim_root_circle_segments;
                ++iidx;
                indexes[iidx]=curr_t2;
                ++iidx;
                indexes[iidx]=curr_t1;
                
                
                ++iidx;
                indexes[iidx]=curr_t1;
                ++iidx;
                indexes[iidx]=curr_t2;
                
                ++iidx;
                curr_t2+=dir;
                if( curr_t2<idx3-Settings.sim_root_circle_segments)
                    curr_t2=idx3-1;
                indexes[iidx]=curr_t2;
                ++iidx;
            }
            else
            {
                indexes[iidx]=curr_t1;
                curr_t1+=1;
                curr_t1=start_t1+(curr_t1-start_t1)%Settings.sim_root_circle_segments;
                ++iidx;
                indexes[iidx]=curr_t1;
                ++iidx;
                indexes[iidx]=curr_t2;
                
                
                
                ++iidx;
                indexes[iidx]=curr_t2;
                ++iidx;
                indexes[iidx]=curr_t1;
                
                ++iidx;
                curr_t2+=dir;
                if(  curr_t2>=idx3)
                    curr_t2=idx3-Settings.sim_root_circle_segments;
                indexes[iidx]=curr_t2;
                ++iidx;
            }
        }
        
        return iidx;
    }
    
    public void addCollidable()
    {
        renderer.removeFromScene(trimesh);
        renderer.addtoSceneCol(trimesh);
        trimesh.setModelBound(new BoundingBox());
        collidable=true;
    }
    
    public Vector3f getLastRotation()
    {
        return last_rotation;
    }
    
    public void update()
    {
        if(points.size()>1)
        {
            if( inscene==false )
            {
                renderer.addtoScene(trimesh);
                inscene=true;
            }
            float [] vertices=new float[points.size()*Settings.sim_root_circle_segments*3];
            float [] normals=new float[vertices.length];
            int [] indexes=new int[(points.size()-1)*Settings.sim_root_circle_segments*2*3];
            
            ListIterator<RPoint> it=points.listIterator();
            RPoint prev,curr, next=null;
            int idx=0;
            int iidx=0;
            
            prev=it.next();
            prev.dir=-1;
            curr=it.next();
            if( it.hasNext())
                next=it.next();
                
            idx=addPoints(idx, prev, curr, null, vertices, normals, true);

            boolean first=true;
            while( first || next!=null)
            {       
                if( !first)
                {
                    curr=next;
                    if( it.hasNext() )
                        next=it.next();
                    else
                        next=null;
                }
                else
                    first=false;
                
                idx=addPoints(idx,curr,prev, next, vertices, normals, false);           
                iidx=calculateIndexes(iidx, idx, indexes, vertices, prev, curr);
                
                prev=curr;
            }
            
            FloatBuffer fb_vertices=BufferUtils.createFloatBuffer(vertices);
            FloatBuffer fb_normals=BufferUtils.createFloatBuffer(normals);
            IntBuffer ib_indexes=BufferUtils.createIntBuffer(indexes);
            
            TriangleBatch batch=trimesh.getBatch(0);
            batch.setVertexCount(vertices.length/3);
            //fb_vertices.limit(batch.getVertexCount()*3);
            //fb_normals.limit(batch.getVertexCount()*3);
            batch.setTriangleQuantity(indexes.length/3);
            batch.setIndexBuffer(ib_indexes);
            batch.setVertexBuffer(fb_vertices);
            batch.setNormalBuffer(fb_normals);
            batch.setColorBuffer(null);
            trimesh.updateRenderState();
            trimesh.updateGeometricState(0.1f, true);
            
            setColor(calculateColor());//ColorRGBA.red);
            
            if( collidable)
            {
                trimesh.updateModelBound();
            }
        }
    }  
    
    private float ByteColorToFloat(float bc)
    {
        return bc*(1.0f/255.f);
    }
    
    public TriMesh getTriMesh()
    {
        return trimesh;
    }
    
    public ColorRGBA calculateColor()
    {
        float age=getAge();
        
        ColorRGBA color=new ColorRGBA();
        
        if( age>=Settings.view_root_end_age)
        {
            color.a=1.f;
            color.b=ByteColorToFloat(Settings.view_root_color_blue_end);
            color.r=ByteColorToFloat(Settings.view_root_color_red_end);
            color.g=ByteColorToFloat(Settings.view_root_color_green_end);
            return color;
        }
        
        {
            float m=(float)(Settings.view_root_color_blue_end-Settings.view_root_color_blue_start)/Settings.view_root_end_age;
            float c=(float)Settings.view_root_color_blue_start;
            
            color.b=ByteColorToFloat(m*age+c);
        }
        
        {
            float m=(float)(Settings.view_root_color_red_end-Settings.view_root_color_red_start)/Settings.view_root_end_age;
            float c=(float)Settings.view_root_color_red_start;
            
            color.r=ByteColorToFloat(m*age+c);
        }
        
        {
            float m=(float)(Settings.view_root_color_green_end-Settings.view_root_color_green_start)/Settings.view_root_end_age;
            float c=(float)Settings.view_root_color_green_start;
            
            color.g=ByteColorToFloat(m*age+c);
        }
        
        color.a=1.f;
        
        return color;
    }
    
    public void setColor(ColorRGBA color)
    {
        MaterialState ms=renderer.createMaterialState();
        ms.setDiffuse(color);
        ColorRGBA c=color.clone();
        //c.a=Settings.view_root_ambient_alpha;
        c.multLocal(Settings.view_root_ambient_pc);
        ms.setAmbient(c);
        //ms.setEmissive(color);
        
        trimesh.setSolidColor(color);
        trimesh.setRenderState(ms);
        trimesh.updateRenderState();
    }
}

