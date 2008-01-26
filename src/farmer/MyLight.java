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
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.AlphaState;
import com.jme.light.PointLight;
import com.jme.scene.state.LightState;
import com.jme.scene.shape.Sphere;
import java.io.Serializable;

/**
 *
 * @author Martin
 */
public class MyLight implements Positionable, Serializable
{
    private Vector3f position=new Vector3f(0,0,0);
    private transient Render3D renderer;
    private String name;
    private transient PointLight light;
    private transient Sphere ball;
    private transient static LightState ls;
    private int opacity=100;
    
    
    public MyLight(String name, Render3D renderer)
    {
        this.name=name;
        init(renderer);
    }
    
    public void init(Render3D renderer)
    {
        this.renderer=renderer;
        
        Node rootNode=renderer.getRootNode();
        
        light=new PointLight();
        light.setLocation(position);
        light.setEnabled(true);
        
        /*light.setSpecular(new ColorRGBA(0,0,0,0));
        light.setAmbient(new ColorRGBA(0.5f,0.5f,0.5f,0.5f));
        light.setDiffuse(new ColorRGBA(0,0,0,0));*/
        
        //light.setLightMask(LightState.MASK_DIFFUSE);
        light.setAttenuate(true);
        light.setConstant(0.f);
        light.setLinear(0.012f);
        light.setQuadratic(0.f);
        
        if( ls==null)
            ls=renderer.createLightState();
        ls.attach(light);
        
        rootNode.setRenderState(ls);
        rootNode.updateRenderState();
        
        ball=new Sphere(name+"-ball", 10,10,1.f);
        ball.setLocalTranslation(position);
        ball.setSolidColor(ColorRGBA.white);
        ball.setLightCombineMode(LightState.OFF);
        
        this.setOpacity(opacity);
    }
    
    public Vector3f getPosition()
    {
        return position;
    }
    
    public void setPosition(Vector3f pos)
    {
        position=pos;
        light.setLocation(pos);
        ball.setLocalTranslation(position);
    }
    
    public void setRotation(Vector3f rot)
    {
        
    }
    
    public Vector3f getRotation()
    {
        return new Vector3f(0,0,0);
    }
    
    public float getRotStep()
    {
        return 0.f;
    }
    
    public float getPosStep()
    {
        return Settings.ctrl_light_pos_step;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setOpacity(int pc)
    {
        if( pc==0 )
        {
            renderer.removeFromScene(ball);
        }
        else
        {
            if(renderer.isInScene(ball)==false)
                renderer.addtoScene(ball);            
        }
        
        light.setLinear(((float)(100-pc)/100.f)*0.062f);
        
        opacity=pc;
    }
    
    public int getReversed()
    {
        return 1;
    }
}
