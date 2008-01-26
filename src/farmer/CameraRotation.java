/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.awt.Point;
import com.jme.renderer.Camera;

/**
 *
 * @author urpc
 */
public class CameraRotation implements CameraInterface
{
    private Point last_mouse;
    private boolean changed;
    private Vector3f centerrotation=new Vector3f(0,1,0);
    private Vector3f center=new Vector3f(0,0,0);
    private float distance;
    private Camera cam;
    
    public CameraRotation(Camera c)
    {
        cam=c;
        distance=Settings.camera_initial_view_distance;
        changed=true;
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
    
    public void changeViewDistance(float chdist)
    {
        distance+=chdist;
        if( distance<Settings.camera_min_view_distance )
            distance=Settings.camera_min_view_distance;
        changed=true;
    }
}
