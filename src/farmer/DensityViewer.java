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
import com.jmex.awt.swingui.ImageGraphics;
import com.jme.image.Texture;
import java.awt.*;
import com.jme.scene.state.TextureState;
import javax.swing.Timer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 *
 * @author Martin
 */
public class DensityViewer implements Positionable, Serializable
{
    private Vector3f position=new Vector3f(0,0,0);
    private Vector3f rotation=new Vector3f(0,0,0);
    private transient Render3D renderer;
    private String name;
    private int opacity=100;
    private transient Quad quad;
    private transient Node node=new Node();
    private transient int currnum;
    private int scale=5;
    private transient Simulation sim;
    private transient ImageGraphics ig;
    private transient Texture tex;
    private transient Timer timer;
    
    private transient Sphere []balls=new Sphere[3];
    
    private static int numDV=0;
    
    
    public DensityViewer(Render3D renderer, Simulation sim)
    {
        init(renderer, sim);
    }
    
    public void init(Render3D renderer, Simulation sim)
    {
        this.sim=sim;
        currnum=++numDV;
        this.renderer=renderer;
        quad=new Quad("Quad "+currnum, Settings.view_dens_width, Settings.view_dens_height);
        setOpacity(opacity);
        ig=ImageGraphics.createInstance(Settings.view_dens_pixel_size, Settings.view_dens_pixel_size, 0);
        tex=new Texture();
        tex.setMipmapState(Texture.MM_NONE);
        tex.setFilter(Texture.FM_LINEAR);
        tex.setImage(ig.getImage());
        
        /*ig.setClip(0,0, Settings.view_dens_pixel_size, Settings.view_dens_pixel_size);
        ig.setColor(Color.red);
        ig.fillRect(0, 0, Settings.view_dens_pixel_size, Settings.view_dens_pixel_size);*/
        
        ig.update();
        
        TextureState ts=renderer.createTextureState();
        ts.setTexture(tex);
        ts.setEnabled(true);
        
        quad.setRenderState(ts);
        quad.updateRenderState();
        
        node.attachChild(quad);
        node.setLocalScale(scale);
        
        balls[0]=new Sphere("sp1", 5,5,0.002f);
        balls[1]=new Sphere("sp2", 5,5,0.002f);
        balls[2]=new Sphere("sp3", 5,5,0.002f);
        
        balls[0].setLocalTranslation(new Vector3f(-Settings.view_dens_width/2.f,-Settings.view_dens_height/2.f,0));
        balls[1].setLocalTranslation(new Vector3f(-Settings.view_dens_width/2.f,Settings.view_dens_height/2.f,0));
        balls[2].setLocalTranslation(new Vector3f(Settings.view_dens_width/2.f,-Settings.view_dens_height/2.f,0));
        
        node.attachChild(balls[0]);
        node.attachChild(balls[1]);
        node.attachChild(balls[2]);
        
        renderer.addUpdateTexture(ig, tex);
        
        node.updateRenderState();
        
        final DensityViewer dv=this;
        
        timer=new Timer(Settings.view_dens_update_delay, new AbstractAction(){
            private DensityViewer ddv=dv;
            public void actionPerformed(ActionEvent e)
            {
                ddv.recalculateInt();    
                timer.stop();
            }            
        });
        
        
        renderer.setImageTexturesDirty(false);
        recalculate();
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
        }
        
        opacity=pc;
    }
    
    public int getOpacity(){ return opacity; }
    
    public void update()
    {
        position=node.getLocalTranslation();
        rotation=Math3D.getRotation(node);
    }
    
    public String getName()
    {
        return "Dichtemesser "+currnum;
    }    
    
    public float getRotStep(){ return Settings.ctrl_dv_rot_step; }
    public float getPosStep(){ return Settings.ctrl_dv_pos_step; }
    
    public void setPosition(Vector3f pos)
    {
        node.setLocalTranslation(pos);
        node.updateGeometricState(0.f, false);
        recalculate();
    }
    
    public Vector3f getPosition()
    {
        return node.getLocalTranslation();
    }
    
    public void setRotation(Vector3f rot)
    {
        Math3D.setRotation(node, rot);
        node.updateGeometricState(0.f, false);
        recalculate();
    }
    
    public Vector3f getRotation()
    {
        return Math3D.getRotation(node);
    }
    
    public int getReversed()
    {
        return 1;
    }
    
    public int getScale()
    {
        return scale;
    }
    public void setScale(int s)
    {
        scale=s;
        node.setLocalScale(s);
        recalculate();
    }
    
    public void recalculate()
    {
        if( Settings.view_dens_interpolate==false )
            timer.restart();
    }
    
    public void recalculateInt()
    {
        Thread t1=new Thread(new Runnable()
        {
            public void run()
            {
                synchronized(sim.point_mutex)
                {
                    recalculateInt2();
                }
            }
        });
        t1.start();
    }
    
    public void recalculateInt2()
    {
        balls[0].updateWorldVectors();
        balls[1].updateWorldVectors();
        balls[2].updateWorldVectors();
        Vector3f sv=balls[0].getWorldTranslation();
        Vector3f v1=balls[1].getWorldTranslation();  
        Vector3f v2=balls[2].getWorldTranslation();
        
        Vector3f d1=v1.subtract(sv);
        float l1=d1.length();
        float step1=l1/(float)Settings.view_dens_pixel_size;
        
        Vector3f d2=v2.subtract(sv);
        float l2=d2.length();
        float step2=l2/(float)Settings.view_dens_pixel_size;
        
        d1.normalizeLocal();
        d2.normalizeLocal();
        
        ig.clearRect(0, 0, Settings.view_dens_pixel_size, Settings.view_dens_pixel_size);
        for(int x=0;x<Settings.view_dens_pixel_size;++x)
        {
            for(int y=0;y<Settings.view_dens_pixel_size;++y)
            {
                Vector3f target=sv.clone();
                target.addLocal(d1.mult(y*step2));
                target.addLocal(d2.mult(x*step1));
                
                byte d=(byte)sim.getDensity(target);
                if(d!=0)
                    //ig.setColor(new Color((int)(m*(long)d+c),true));
                    ig.setColor(new Color((int)(d+128),(int)(d+128),(int)(d+128)));
                else 
                    ig.setColor(Color.green);
                
                //ig.setColor(new Color((int)(d+128), (int)(d+128),(int)(d+128)));
                ig.fillRect(x, y, 1, 1);
            }
        }
        
        /*java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                ig.update();
                renderer.setImageTexturesDirty(false);
            }
        });*/
        
    }
}
