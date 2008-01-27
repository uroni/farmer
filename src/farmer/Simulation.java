/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;


import com.jme.scene.Node;

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
    private transient Render3D renderer;
    private transient MainForm form;
    
    public Simulation(Render3D renderer, MainForm form)
    {
        init(renderer, form);
    }
    
    public void init(Render3D renderer, MainForm form)
    {
        this.renderer=renderer;
        
        {
            ListIterator<Korn> it=corns.listIterator();
            while(it.hasNext())
            {
                Korn k=it.next();
                k.init(renderer, true);
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
                s.init(renderer);
                form.addMaterial(s, null);
            }
        }
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
    
    public Korn getCorn(int i)
    {
        return corns.listIterator(i).next();
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
        ListIterator<Material> it=materials.listIterator();
        while(it.hasNext())
        {
            Material m=it.next();
            m.calculateDensity(density);
        }
    }
    
    public void step(float time)
    {
        for(int i=0;i<this.getNumCorns();++i)
        {
            Korn k=this.getCorn(i);
            k.step(time);
        }
    }
}
