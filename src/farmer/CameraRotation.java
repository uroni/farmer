/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.awt.Point;
import com.jme.renderer.Camera;
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
 * @author urpc
 */
public class CameraRotation extends CameraInterface
{
    private Point last_mouse;
    private boolean changed=true;
    private Vector3f centerrotation=new Vector3f(0,1,0);
    private Vector3f center=new Vector3f(0,0,0);
    private float distance;
    private transient Camera cam;
    private transient Sphere ball;
    private transient Node node;
    private transient Render3D renderer;
    private int scale=1;
    private int opacity=0;
    
    public CameraRotation(Camera c, Render3D renderer)
    {
        distance=Settings.camera_initial_view_distance;
        init(c,renderer);
    }
    
    public void init(Camera c, Render3D renderer)
    {
        cam=c;
        changed=true;
        this.renderer=renderer;
        
        ball=new Sphere("CameraCenter-ball", 10,10,1.f);
        ball.setLocalTranslation(center);
        ball.setSolidColor(ColorRGBA.red);
        ball.setLightCombineMode(LightState.OFF);
        node=new Node();
        node.attachChild(ball);
        
        setOpacity(opacity);
        setScale(scale);
    }
    
    public void setMousePosition(Point p)
    {
        last_mouse=p;
    }
    
    public void updateMousePosition(Point p)
    {
        int xdist=p.x-last_mouse.x;
        int ydist=p.y-last_mouse.y;
        
        centerrotation.x+=ydist*Settings.input_mouse_rotation_scale;
        centerrotation.y+=xdist*Settings.input_mouse_rotation_scale;
        
        last_mouse=p;
        
        changed=true;
    }
    
    public void update()
    {
        if( changed )    
        {               
            Vector3f upvec=new Vector3f(0,1,0);
            
            if( centerrotation.x<-89.f)
                centerrotation.x=-89.f;
            else if( centerrotation.x>89.f)
                centerrotation.x=89.f;
            
                    
            Vector3f pos=Math3D.getTarget(center, centerrotation, distance);
            
            cam.setLocation(pos);
            
            cam.lookAt(center, new Vector3f(0,-1,0));
            
            changed=false;
        }
    }
    
    public void setCenter(Vector3f vec)
    {
        center=vec;
        changed=true;
    }
    
    public void setViewDistance(float dist)
    {
        distance=dist;
        changed=true;
    }
    
    public float getViewDistance()
    {
        return distance;
    }
    
    public void changeViewDistance(float chdist)
    {
        distance+=chdist;
        if( distance<Settings.camera_min_view_distance )
            distance=Settings.camera_min_view_distance;
        changed=true;
    }
    
    public void setCamera(Camera cam)
    {
        this.cam=cam;
        changed=true;
    }
    
    public void setPosition(Vector3f pos)
    {
        setCenter(pos);
    }
    
    public Vector3f getPosition()
    {
        return center;
    }
    
    public void setRotation(Vector3f rot)
    {
        
    }
    
    public Vector3f getRotation()
    {
        return new Vector3f(0,0,0);
    }
    
    public float getRotStep(){ return 0.f; }
    public float getPosStep(){ return Settings.ctrl_camera_pos_step; }
    
    public String getName(){ return "Kameraziel"; }
    
    public void setOpacity(int pc)
    {
        opacity=pc;
        if( pc<50 )
        {
            renderer.removeFromScene(node);
        }
        else
        {
            if( !renderer.isInScene(node))
                renderer.addtoScene(node);
            
        }
    }
    public int getOpacity(){return opacity; }
    
    public int getReversed(){ return 1;}
    
    public int getScale(){ return (int)ball.getLocalScale().x; }
    public void setScale(int s){ scale=s; ball.setLocalScale(s);}
}
