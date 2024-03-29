/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;


import com.jme.scene.Node;
import com.jme.math.Vector3f;

import java.util.*;
import java.io.*;
/**
 *
 * @author Martin
 */
public class Simulation implements Serializable
{
    private List<Korn> corns=new ArrayList<Korn>();
    private List<MyLight> lights=new ArrayList<MyLight>();
    private List<Solid> solids=new ArrayList<Solid>();
    private List<Material> materials=new ArrayList<Material>();
    private CameraInterface camera;
    private transient Render3D renderer;
    private transient MainForm form;
    public transient Object point_mutex;    
    private boolean stopped=false;
    private transient float speed;
    private long lastsimtime;
    private float simulatedtime;
    private transient boolean stop_current_operation;
    private int simulation_steps;
    
    public Simulation(Render3D renderer, MainForm form)
    {
        init(renderer, form);
    }
    
    public void init(Render3D renderer, MainForm form)
    {
        point_mutex=new Object();
        
        this.renderer=renderer;
        
        {
            ListIterator<Korn> it=corns.listIterator();
            while(it.hasNext())
            {
                Korn k=it.next();
                k.init(renderer, true, this);
                form.addCorn(k);
            }
        }
        
        {
            ListIterator<MyLight> it=lights.listIterator();
            while(it.hasNext())
            {
                MyLight l=it.next();
                l.init(renderer);
                form.addLight(l);
            }
        }
        
        {
            ListIterator<Solid> it=solids.listIterator();
            while(it.hasNext())
            {
                Solid s=it.next();
                s.init(renderer);
                form.addSolid(s, null);
            }
        }
        
        {
            ListIterator<Material> it=materials.listIterator();
            while(it.hasNext())
            {
                Material s=it.next();
                s.init(renderer, this);
                form.addMaterial(s, null);
            }
        }
        
        if( camera instanceof CameraRotation )
        {
            CameraRotation cam=(CameraRotation)camera;
            cam.init(renderer.getCamera(), renderer);
            renderer.setCamera(camera);
        }
        
        speed=0;
    }
    
    public synchronized void stopCurrentOperation()
    {
        if( stopped==true )
        {
            stop_current_operation=true;
        }
    }
    
    public synchronized boolean shouldStopOperation()
    {
        boolean b=stop_current_operation;
        stop_current_operation=false;
        return b;
    }
    
    
    public void addLight(MyLight l)
    {
        lights.add(l);
    }
    
    public void addSolid(Solid s)
    {
        solids.add(s);
    }
    
    public void addCorn(Korn k)
    {
        corns.add(k);
    }
    
    public void addMaterial(Material m)
    {
        materials.add(m);
    }
    
    public int getNumCorns()
    {
        return corns.size();
    }
    
    public float getSpeed()
    {
        return speed;
    }
    
    public void setSpeed(float f)
    {
        speed=f;
        lastsimtime=0;
    }
    
    public Korn getCorn(int i)
    {
        return corns.listIterator(i).next();
    }
    
    public void setCamera(CameraInterface cam)
    {
        camera=cam;
    }
    
    public CameraInterface getCamera()
    {
        return camera;
    }
    
    public void update()
    {
        {
            ListIterator<Korn> it=corns.listIterator();
            while(it.hasNext())
            {
                Korn k=it.next();
                k.update();
            }
        }
        
        {
            ListIterator<Solid> it=solids.listIterator();
            while(it.hasNext())
     
            {
                Solid s=it.next();
                s.update();
            }
        }
        
        {
            ListIterator<Material> it=materials.listIterator();
            while(it.hasNext())
            {
                Material s=it.next();
                s.update();
            }
        }
    }
    
    public boolean saveState(File f)
    {
        update();
        try
        {
            FileOutputStream fos=new FileOutputStream(f);
            ObjectOutputStream out=new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();     
            return true;
        }
        catch(IOException e)
        {
            System.out.println("Exception while writing Objects: "+e);
            return false;
        }
    }
    
    public static Simulation loadState(File f)
    {
        try
        {
            FileInputStream fis=new FileInputStream(f);
            ObjectInputStream in=new ObjectInputStream(fis);
            Simulation sim=(Simulation)in.readObject();
            in.close();
            return sim;
        }
        catch(IOException e)
        {
            System.out.println("Exception while reading Objects: "+e.getMessage() );
            return null;
        }
        catch( ClassNotFoundException e)
        {
            System.out.println("ClassNotFoundException while reading Objects: "+e.getMessage() );
            return null;
        }
    }
    
    public void caclulateDensity(float density)
    {
        final float dens=density;
        setStopped(true);
        Thread t1=new Thread(new Runnable(){
            public void run()
            {        
                synchronized(point_mutex)
                {
                    ListIterator<Material> it=materials.listIterator();
                    while(it.hasNext())
                    {
                        Material m=it.next();
                        m.calculateDensity(dens);
                    } 
                    setStopped(false);
                }
            }
        });
        t1.start();
    }
    
    public void initWater(float density)
    {
        final float dens=density;
        setStopped(true);
        Thread t1=new Thread(new Runnable(){
            public void run()
            {        
                synchronized(point_mutex)
                {
                    ListIterator<Material> it=materials.listIterator();
                    while(it.hasNext())
                    {
                        Material m=it.next();
                        m.searchWaterPoints(dens);
                    } 
                    setStopped(false);
                }
            }
        });
        t1.start();
    }
    
    public int getWaterAmount(Vector3f p)
    {
        int ret=0;
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            ret+=m.getWater(p)+128;
        }
        
        return ret;
    }
    
    public float getWaterAmountF(Vector3f p)
    {
        float ret=-1;
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            float r=m.getWaterF(p);
            if( r!=-1)
            {
                if( ret==-1)
                    ret=0;
                ret+=r;
            }
        }
        
        return ret;
    }
    
    public void setWaterAmountF(Vector3f p, float amount)
    {
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            m.setWaterF(p, amount);
        }
    }
   
    
    public void addWaterPoint(Vector3f p, float r, int amount)
    {
        ListIterator<Material> it=materials.listIterator();
        float a=(1.f/255.f)*amount;
        if(a>1.f)
            a=1.f;
        while(it.hasNext())
        {
            Material m=it.next();
            m.setWaterPoint(p,r, a);
        }
    }
    
    public float getDensity(Vector3f p, boolean dv)
    {
        int count=0;
        float density=0;
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            density+=m.getDensity(p, dv)+128;
            ++count;
        }
        
        if(count!=0)
        {
            density/=(float)count;
            density-=128.f;
        }
        
        return density;
    }
    
    public boolean isMaterialPoint(Vector3f p)
    {
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            if( m.materialPointExists(p))
                return true;
        }
        return false;
    }
    
    public void addWaterTop(int amount)
    {
        float famount=(1.f/255.f)*(float)amount;
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            it.next().addWaterTop(famount);
        }
    }
    
    public float getWaterPointSum()
    {
        float sum=0.f;
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            sum+=it.next().getWaterSum();
        }
        return sum;
    }
    
    public float getSimulatedTime()
    {
        return simulatedtime;
    }
    
    public synchronized void step()
    {
        if( lastsimtime==0 )
        {
            lastsimtime=System.currentTimeMillis();
            return;
        }
        
        float timeleft=(float)(System.currentTimeMillis()-lastsimtime)*speed;
        
        if( timeleft==0)
            return;
        
        simulatedtime+=timeleft;
        
        for(int i=0;i<this.getNumCorns();++i)
        {
            Korn k=this.getCorn(i);
            k.step(timeleft);
        }
        
        if( simulation_steps % Settings.sys_water_sim_gab==0)
        {
            ListIterator<Material> it=materials.listIterator();
            while(it.hasNext())
            {
                it.next().step(timeleft);
            }
            
            //System.out.println("Waterpointsum: "+this.getWaterPointSum());
        }
        
        ++simulation_steps;
        
        lastsimtime=System.currentTimeMillis();
    }
    
    public void reset()
    {
        simulatedtime=0;
        ListIterator<Korn> it=corns.listIterator();
        while(it.hasNext())
        {
           Korn k=it.next();
           k.reset();
        }        
    }
    
    public synchronized void setStopped(boolean b)
    {
        stopped=b;
    }
    
    public synchronized boolean isStopped()
    {
        return stopped;
    }
}
