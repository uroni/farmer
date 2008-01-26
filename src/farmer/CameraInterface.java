/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.awt.Point;

/**
 *
 * @author urpc
 */
public interface CameraInterface
{
    public void setMousePosition(Point p);
    public void updateMousePosition(Point p);
    public void update();
    public void setCenter(Vector3f vec);
    public void setViewDistance(float dist);
    public void changeViewDistance(float chdist);
}
