/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.awt.Point;
import com.jme.renderer.Camera;
import java.io.Serializable;

/**
 *
 * @author urpc
 */
public abstract class CameraInterface implements Positionable,Serializable
{
    public abstract void setMousePosition(Point p);
    public abstract void updateMousePosition(Point p);
    public abstract void update();
    public abstract void setCenter(Vector3f vec);
    public abstract void setViewDistance(float dist);
    public abstract float getViewDistance();
    public abstract void changeViewDistance(float chdist);
    public abstract void setCamera(Camera cam);
}
