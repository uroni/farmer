/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import java.awt.Component;
import java.util.*;

/**
 *
 * @author Martin
 */

class VisibilityComps
{
    Component show;
    Component hidden;
    Component showButton;
    Component hideButton;
    boolean hide;
}

public class VisibilityManager implements java.awt.event.ActionListener
{
    private List<VisibilityComps> vcomps=new LinkedList<VisibilityComps>();
    
    public void add(Component show, Component hidden, Component showButton, Component hideButton, boolean hide)
    {
        VisibilityComps vc=new VisibilityComps();
        vc.show=show;
        vc.hidden=hidden;
        vc.showButton=showButton;
        vc.hideButton=hideButton;
        vc.hide=!hide;
        
        vcomps.add(vc);
        
        java.awt.event.ActionEvent e;
        if( hide==false )
             e=new java.awt.event.ActionEvent(showButton, 0, "");
        else
             e=new java.awt.event.ActionEvent(hideButton, 0, "");
            
        actionPerformed(e);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        ListIterator<VisibilityComps> it=vcomps.listIterator();
        
        while(it.hasNext())
        {
            VisibilityComps cps=it.next();
            
            if( cps.showButton==e.getSource())
            {
                if( cps.hide==true)
                {
                    cps.hidden.setVisible(false);
                    cps.show.setVisible(true);
                    cps.hide=false;
                    return;
                }
            }
            else if( cps.hideButton==e.getSource())
            {
                if( cps.hide==false)
                {
                    cps.show.setVisible(false);
                    cps.hidden.setVisible(true);
                    cps.hide=true;
                    return;
                }
            }
        }
    }
    
}
