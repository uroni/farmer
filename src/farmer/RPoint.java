/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 *
 * @author Martin
 */
public class RPoint implements Serializable
{
    public float age;
    public Vector3f pos;
    public Korn korn;
    public Segment segment;
    public int dir;
}
