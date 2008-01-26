/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.awt.Point;
import com.jme.renderer.Camera;
import com.jme.scene.shape.Box;

/**
 *
 * @author urpc
 */
public class CameraFPS implements CameraInterface
{
    private Point last_mouse;
    private boolean changed;
    private Camera cam;
    private int xupdate,yupdate;
    private Quaternion rotation=new Quaternion();
    
    public CameraFPS(Camera c)
    {
        cam=c;
    }
    
    public void setMousePosition(Point p)
    {
        last_mouse=p;
    }
    
    public void updateMousePosition(Point p)
    {
        int xdist=p.x-last_mouse.x;
        int ydist=p.y-last_mouse.y;
        
        xupdate+=ydist;
        yupdate+=xdist;
                
        last_mouse=p;
        
        changed=true;
    }
    
    public void update()
    {
        if(changed)
        {
            float [] angles=new float[3];
            rotation.toAngles(angles);
            angles[0]+=(float)xupdate*Settings.input_mouse_fpscam_scale;
            angles[1]+=(float)yupdate*Settings.input_mouse_fpscam_scale;
            rotation.fromAngles(angles);
            
            Vector3f rot=new Vector3f(angles[0]*Math3D.GRAD_PI,angles[1]*Math3D.GRAD_PI, angles[2]*Math3D.GRAD_PI);
            
            Vector3f target=Math3D.getTarget(cam.getLocation(), rot, 300);
            
            Vector3f rotation2=Math3D.getRotationToTarget2(cam.getLocation(), target);
            
            angles[0]=rotation2.x*Math3D.BOG;
            angles[1]=rotation2.y*Math3D.BOG;
            rotation.fromAngles(angles);
            
            System.out.println("Rotation: "+rot);
            System.out.println("Target: "+target);
            MainForm.getRenderer().getBox().setLocalTranslation(target);
                              
            
            cam.setAxes(rotation);
            cam.update();
            changed=false;
            
            xupdate=0;
            yupdate=0;
        }
    }
    
    public void setCenter(Vector3f vec)
    {
        
    }
    
    public void setViewDistance(float dist)
    {
        
    }
    
    public void changeViewDistance(float chdist)
    {
    }
    
}
