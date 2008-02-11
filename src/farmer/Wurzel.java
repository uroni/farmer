/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.FastMath;
import com.jme.math.Plane;
import com.jme.math.Ray;
import java.io.Serializable;

//JME includes
import com.jme.scene.Node;
import com.jme.scene.shape.*;
import com.jme.math.Vector3f;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.state.LightState;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Martin
 */
public class Wurzel extends GrowableAdapter
{
    private transient Vector3f last_intersection_direction;
    private transient int straigt_timeleft;
    private transient  Sphere []circle_nodes;
    
    public Wurzel(String name, Render3D renderer, Korn k, Simulation sim, boolean child)
    {
        super(name, renderer, k, sim, child);
    }
     
    public void updateCircle()
    {
        Vector3f rot=getRotation();
        Vector3f target=Math3D.getTarget(position, rot, 1.f);
        rot=Math3D.getRotationToTarget2(getPosition(), target);
        Vector3f []circle=Math3D.getCircleSegments(arrow.getLocalTranslation(), rot, 10.f);
        if( circle_nodes==null || circle_nodes.length!=circle.length)
            circle_nodes=new Sphere[circle.length];
        
        for(int i=0;i<circle.length;++i)
        {
            if( circle_nodes[i]==null)
            {
                circle_nodes[i]=new Sphere("circle", 10,10,0.5f);
                circle_nodes[i].setSolidColor(ColorRGBA.blue);
                korn.addNode(circle_nodes[i]);
            }
            circle_nodes[i].setLocalTranslation(circle[i]); 
        }
    }
    
    public boolean step(float time)
    {
        if( first_sim==true)
        {
            first_sim=false;
            curr_position=position;
            rotation.x-=90;
            curr_direction=Math3D.getTarget(position, rotation, 1.f);
            sim_start=sim.getSimulatedTime();
        }
        
        float max_step=time*0.0001f;
        
        Vector3f target=curr_position.clone();
        
        Vector3f old_direction=curr_direction.clone();
        curr_direction.normalizeLocal();       
        Vector3f old_position=curr_position.clone();
        
        float deg=curr_direction.angleBetween(gravity.normalize())*FastMath.RAD_TO_DEG;
        curr_direction.multLocal(Settings.sim_root_gravity_influence1);
        
        if(deg>90)
            deg=90;
        
        int dir=1;
        if(deg<20)
        {
            deg=20+(20-deg);
            dir=-1;
        }
        
        if( isChild==false || sim.getSimulatedTime()>=gravity_time)
        {
            float beug;
            if( Settings.sim_root_gravity_func==0)
            {
                float a=Settings.sim_root_gravity_max/(FastMath.exp(-1*Settings.sim_root_gravity_k*Settings.sim_root_gravity_min_deg));
                beug=Settings.sim_root_gravity_max-a*FastMath.exp(-1*Settings.sim_root_gravity_k*deg);
            }
            else
            {
                beug=Settings.sim_root_gravity_max*(1/(1+FastMath.exp((Settings.sim_root_gravity_add-deg)*Settings.sim_root_gravity_k2)));
            }
            if( beug>0)
            {
                Vector3f tmpg=gravity.clone();
                tmpg.multLocal(dir*beug*time*Settings.sim_root_gravity_influence2);
                curr_direction.addLocal(tmpg);
            }

            curr_direction.normalizeLocal();
            curr_direction.multLocal(max_step);

            target.addLocal(curr_direction);
            
            
            curr_position=target;
        }
        else
        {
            curr_direction.normalizeLocal();
            curr_direction.multLocal(max_step);
            target.addLocal(curr_direction);
            curr_position=target;
        }
        
        
        {
            float max_random=Settings.sim_root_density_probes_max_distance_mult*time;
            Vector3f min_dens_vec=curr_position;
            float min_dens=sim.getDensity(worldVector(curr_position), false);

            if( min_dens!=0)
            {

                for(int i=0;i<Settings.sim_root_density_probes;++i)
                {
                    Vector3f curr=curr_position.clone();
                    curr.x+=Math.random()*max_random-max_random/2.f;
                    curr.y+=Math.random()*max_random-max_random/2.f;
                    curr.z+=Math.random()*max_random-max_random/2.f;

                    float dens=sim.getDensity(worldVector(curr), false);
                    if( dens<min_dens)
                    {
                        min_dens=dens;
                        min_dens_vec=curr;
                    }
                }

                curr_position=min_dens_vec;
                curr_direction=curr_position.subtract(old_position);
            }
        }
        
        //intersection
        Vector3f []tri;
        boolean intersection=false;

        if( /*getAge()>Settings.sim_root_min_collision_age &&*/ rp.getSize()>5 && sim_length>=Settings.sim_root_min_collision_length)
        {
            int t=0;
            do
            {
                Vector3f curr_oldpos_world=worldVector(old_position);
                Vector3f curr_position_world=worldVector(curr_position);
                tri=renderer.collides(curr_oldpos_world, curr_position_world, null, null, false, Settings.sim_collison_savety_distance);
                if( tri!=null)
                {
                    Vector3f curr_dir_world=curr_position_world.subtract(curr_oldpos_world);//worldDirection(curr_direction);

                    Plane plane=new Plane();
                    plane.setPlanePoints(tri[0], tri[1], tri[2]);

                    Vector3f cp1=tri[3];

                    Vector3f cp2=curr_oldpos_world.add(plane.getNormal().mult(-1*plane.pseudoDistance(curr_oldpos_world)) );

                    cp1.subtractLocal(cp2);

                    curr_direction=localDirection(cp1);
                    curr_direction.normalizeLocal();
                    curr_direction.multLocal(max_step);

                    curr_position=old_position.clone();
                    curr_position.addLocal(curr_direction);
                    intersection=true;
                }
                ++t;
            }while(tri!=null && t<3);
        }

        
        if( intersection && Settings.sim_root_collision_quirk )
        {
            boolean set=false;
            if( last_intersection_direction_age!=0 && sim.getSimulatedTime()-last_intersection_direction_age<Settings.sim_collision_straigt_time_back)
            {
                if( last_intersection_direction.angleBetween(curr_direction.normalize())*FastMath.RAD_TO_DEG>90)
                {
                    float dlength=curr_direction.length();
                    curr_direction=last_intersection_direction.clone();
                    curr_direction.normalizeLocal();
                    curr_direction.multLocal(dlength);
                    last_intersection_direction_age=sim.getSimulatedTime();
                    set=true;
                }
            }
            if(set==false)
            {
                last_intersection_direction_age=sim.getSimulatedTime();
                last_intersection_direction=curr_direction.clone().normalize();
            }
        }
        
        
        
        /*if( intersection==true)
        {
            straigt_timeleft=Settings.sim_collison_straigt_time;
        }
        
        if( straigt_timeleft>0)
        {
            straigt_timeleft-=time;
            
            Vector3f curr_direction_copy=curr_direction.clone();
            
            curr_direction.normalizeLocal();
            
            int idx=-1;
            float r;
            while((r=rp.getAge(idx))<=Settings.sim_collision_straigt_time_back && r!=0)--idx;
            ++idx;
            
            ArrayList<Vector3f> points=new ArrayList<Vector3f>();
            points.ensureCapacity(idx*-1);
            points.add(rp.getPointPos(idx));
            
            for(int i=idx;i<-1;++i)
            {
                Vector3f curr=rp.getPointPos(i);
                Vector3f next=rp.getPointPos(i+1);
                
                float age=rp.getAge(i);
                if(age==0)
                    continue;
                
                Vector3f vec=next.subtract(curr);
                
                float fx=(1.0f/(FastMath.log(age*Settings.sim_collision_straigt_age_mult+1)));//*(1.0f/age);
                if(fx>1.f)
                    fx=1.f;
                
                //float fx=(10000.f)/(age);
                
                Vector3f add=curr_direction.mult(fx*time*Settings.sim_collision_straigt_mult);
                
                add.addLocal(vec);
                add.normalizeLocal();
                add.multLocal(vec.length());
                
                Vector3f neu=curr.add(add);                   
                points.add(neu);
                                    
            }
            
            ListIterator<Vector3f> it=points.listIterator();
            while(it.hasNext())
            {
                rp.setPoint(it.next(), idx);
                ++idx;
            }
            
            curr_position=rp.getPointPos(-1).add(curr_direction_copy);
        }*/
        float dlength=curr_direction.length();
        
        boolean new_point=true;
        
        if(intersection)
        {
            int idx=-1;
            float r;
            while((r=rp.getAge(idx))<=Settings.sim_collision_straigt_time_back && r!=0)--idx;
            ++idx;
            
            ArrayList<Vector3f> points=new ArrayList<Vector3f>();
            points.ensureCapacity(idx*-1);
            points.add(rp.getPointPos(idx));
            
            float allpc=0.f;
            
            for(int i=idx+1;i<=-1;++i)
            {
                Vector3f prev=rp.getPointPos(i-1);
                Vector3f curr=rp.getPointPos(i);
                
                float age=rp.getAge(i);
                if(age==0)
                    ++age;
                
                Vector3f vec=curr.subtract(prev);
                
                float fx=vec.length()*(Settings.sim_collision_straigt_mult2/age)+Settings.sim_collision_straigt_add;                                    
                if(fx>Settings.sim_collision_straigt_max)
                    fx=Settings.sim_collision_straigt_max;
                allpc+=fx;
            }
            
            Vector3f start=rp.getPointPos(idx).clone();
            for(int i=idx+1;i<=-1;++i)
            {
                Vector3f prev=rp.getPointPos(i-1);
                Vector3f curr=rp.getPointPos(i);
                
                float age=rp.getAge(i);
                if(age==0)
                    ++age;
                
                Vector3f vec=curr.subtract(prev);
                
                float vlength=vec.length();
                
                float fx=vlength*(Settings.sim_collision_straigt_mult2/age)+Settings.sim_collision_straigt_add;                                    
                if(fx>Settings.sim_collision_straigt_max)
                    fx=Settings.sim_collision_straigt_max;
                
                Vector3f add_dir=curr_direction.normalize();
                float adlength=(dlength/allpc)*fx;
                add_dir.multLocal(adlength);
                
                vec.addLocal(add_dir);
                vec.normalizeLocal().multLocal(vlength);
                start.addLocal(vec);
                points.add(start.clone());
            }
            
            ListIterator<Vector3f> it=points.listIterator();
            while(it.hasNext())
            {
                rp.setPoint(it.next(), idx);
                ++idx;
            }
            
            
            curr_position=rp.getPointPos(-1).clone();
            Vector3f curr_direction_neu=rp.getPointPos(-1).subtract(rp.getPointPos(-2));
            if( curr_direction_neu.normalize().angleBetween(curr_direction.normalize()) <=Settings.sim_collision_straigt_min_degree)
            {
                curr_position.addLocal(curr_direction.normalize().mult(dlength));
                sim_length+=dlength;
                
            }
            else
            {
                curr_direction=curr_direction_neu;
                new_point=false;
            }       
        }
        else        
        {
            sim_length+=dlength;
            
        }
        
        if( new_point)
        {
            rp.addPoint(curr_position);
            if( rp.getSize()>3)
            {
                jmgr.addPoint(time, -2);
            }
        }
        
        if( makeJunctions()==true )
            return false;
        return true;
    }
    
    public float getLength()
    {
        return sim_length;
    }
}

