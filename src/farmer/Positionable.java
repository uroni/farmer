/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;

/**
 *
 * @author Martin
 */
public interface Positionable {
    
    public void setPosition(Vector3f pos);
    public Vector3f getPosition();
    
    public void setRotation(Vector3f rot);
    public Vector3f getRotation();
    
    public float getRotStep();
    public float getPosStep();
    
    public String getName();
    
    public void setOpacity(int pc);
    public int getOpacity();
    
    public int getReversed();
    
    public int getScale();
    public void setScale(int s);
}
