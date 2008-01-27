/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import java.util.*;

/**
 *
 * @author Martin
 */
public class PositionControl
{
    public final static int POS_DOWN=0;
    public final static int POS_UP=1;
    public final static int POS_LEFT=2;
    public final static int POS_RIGHT=3;
    public final static int ROT_LEFT=6;
    public final static int ROT_RIGHT=7;
    public final static int ROT_UP=8;
    public final static int ROT_DOWN=9;
    
    private float accel=1.f;
    
    
    private List<Positionable> pos=new ArrayList<Positionable>();
    Positionable sel=null;
    boolean pos_back=false;
    
    public int getNumPositionables()
    {
        return pos.size();
    }
    
    public void addPositionable(Positionable p)
    {
        pos.add(p);
    }
    
    public String getName(int idx)
    {
        ListIterator<Positionable> it=pos.listIterator(idx);
        return it.next().getName();
    }
    
    public void select(int idx)
    {
        ListIterator<Positionable> it=pos.listIterator(idx);
        sel=it.next();
    }
    
    public void pos_up()
    {
        if( sel==null )return;
        if(!pos_back)
        {    
            Vector3f p=sel.getPosition();
            p.y-=sel.getPosStep()*sel.getReversed()*accel;
            sel.setPosition(p);
        }
        else
        {
            Vector3f p=sel.getPosition();
            p.z-=sel.getPosStep()*sel.getReversed()*accel;
            sel.setPosition(p);
        }
    }
    
    public void pos_down()
    {
        if( sel==null )return;
        if(!pos_back)
        {
            Vector3f p=sel.getPosition();
            p.y+=sel.getPosStep()*sel.getReversed()*accel;
            sel.setPosition(p);
        }
        else
        {
            Vector3f p=sel.getPosition();
            p.z+=sel.getPosStep()*sel.getReversed()*accel;
            sel.setPosition(p);
        }
    }
    
    public void pos_left()
    {
        if( sel==null )return;
        Vector3f p=sel.getPosition();
        p.x+=sel.getPosStep()*sel.getReversed()*accel;
        sel.setPosition(p);
    }
    
    public void pos_right()
    {
        if( sel==null )return;
        Vector3f p=sel.getPosition();
        p.x-=sel.getPosStep()*sel.getReversed()*accel;
        sel.setPosition(p);
    }
    
    public void rot_up()
    {
        if( sel==null )return;
        Vector3f p=sel.getRotation();
        p.x+=sel.getRotStep()*sel.getReversed()*accel;
        sel.setRotation(p);
    }
    
    public void rot_down()
    {
        if( sel==null )return;
        Vector3f p=sel.getRotation();
        p.x-=sel.getRotStep()*sel.getReversed()*accel;
        sel.setRotation(p);
    }
    
    public void rot_left()
    {
        if( sel==null )return;
        Vector3f p=sel.getRotation();
        p.y-=sel.getRotStep()*sel.getReversed()*accel;
        sel.setRotation(p);
    }
    
    public void rot_right()
    {
        if( sel==null )return;
        Vector3f p=sel.getRotation();
        p.y+=sel.getRotStep()*sel.getReversed()*accel;
        sel.setRotation(p);
    }
    
    public void setBack(boolean b)
    {
        pos_back=b;
    }
    
    public void setOpacity(int pc)
    {
        if( sel==null )return;
        sel.setOpacity(pc);
    }
    
    public void setScale(int s)
    {
        if( sel==null )return;
        sel.setScale(s);
    }
    
    public void performAction(int a, float acc)
    {
        accel=acc;
        switch(a)
        {
            case POS_UP: pos_up(); accel=1.f; return;
            case POS_DOWN: pos_down(); accel=1.f; return;
            case POS_LEFT: pos_left(); accel=1.f; return;
            case POS_RIGHT: pos_right(); accel=1.f; return;
            case ROT_UP: rot_up(); accel=1.f; return;
            case ROT_DOWN: rot_down(); accel=1.f; return;
            case ROT_LEFT: rot_left(); accel=1.f; return;
            case ROT_RIGHT: rot_right(); accel=1.f; return;
        }
    }
}
