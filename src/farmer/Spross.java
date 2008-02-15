/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 *
 * @author Martin
 */
public class Spross extends GrowableAdapter
{
    public Spross(String name, Render3D renderer, Korn k, Simulation sim, boolean child)
    {
        super(name, renderer, k, sim, child, false);
    }
    
    public void init(String name, Render3D renderer, Korn k, Simulation sim, boolean child, boolean load)
    {
        super.init(name, renderer, k, sim, child, false, load);
    }
    
    public boolean step(float time)
    {
        if( first_sim==true)
        {
           /*first_sim=false;
            curr_position=position;
            Vector3f crot=rotation.clone();
            crot.x-=90;
            curr_direction=Math3D.getTarget(position, crot, 1.f);
            sim_start=sim.getSimulatedTime();*/
            
            first_sim=false;
            sim_start=sim.getSimulatedTime();
            if( renderer.isInScene(arrow))
            {            
                /*curr_position=position;
                Vector3f crot=rotation.clone();
            
                curr_direction=Math3D.getTarget2(position, crot, 1.f);
                sim_start=sim.getSimulatedTime();*/
                
                curr_position=position.clone();
                Vector3f cpos=curr_position.clone();
                cpos.y-=1;
                Vector3f cposo=new Vector3f(),cpos2o=new Vector3f();
                Vector3f rr=rotation.clone();
                rr.x+=180;
                Math3D.setRotation(arrow, rr);
                arrow.localToWorld(curr_position, cposo);
                arrow.localToWorld(cpos, cpos2o);
                Math3D.setRotation(arrow, rotation);
                curr_direction=cpos2o.subtractLocal(cposo);
            }
        }
        
        float max_step=time*0.00005f;
        
        Vector3f target=curr_position.clone();
        
        Vector3f old_direction=curr_direction.clone();
        curr_direction.normalizeLocal();       
        Vector3f old_position=curr_position.clone();
        
        float deg=curr_direction.angleBetween(gravity.mult(-1).normalizeLocal())*FastMath.RAD_TO_DEG;
        curr_direction.multLocal(Settings.sim_spross_gravity_influence1);
        
        if(deg>90)
            deg=90;
        
        float a=Settings.sim_spross_gravity_max/(FastMath.exp(-1*Settings.sim_spross_gravity_k*0.000001f));
        float beug=Settings.sim_spross_gravity_max-a*FastMath.exp(-1*Settings.sim_spross_gravity_k*deg);
        if( beug>0)
        {
            Vector3f tmpg=gravity.mult(-1);
            tmpg.multLocal(beug*time*Settings.sim_spross_gravity_influence2);
            curr_direction.addLocal(tmpg);
        }
        
        curr_direction.normalizeLocal();
        curr_direction.multLocal(max_step);

        target.addLocal(curr_direction);
            
        curr_position=target;
        
        rp.addPoint(curr_position);
        
        return true;
    }

}
