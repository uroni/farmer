/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JPanel;

/**
 *
 * @author Martin
 */
class IRSave
{
    JPanel panel;
    int type;
}

public class InformationRenderer
{
    private PositionControl pc;
    private List<IRSave> panels=new LinkedList<IRSave>();
    private MainForm mf;
    
    public static int TYPE_WURZEL=1;
    public static int TYPE_KORN=2;
    
    public InformationRenderer(PositionControl pc, MainForm mf)
    {
        this.pc=pc;     
        this.mf=mf;
    }
    
    public void addPanel(JPanel panel, int type)
    {
        IRSave s=new IRSave();
        s.panel=panel;
        s.type=type;
        panels.add(s);
    }
    
    public void update()
    {
        ListIterator<IRSave> it=panels.listIterator();
        
        int seltype=getType(pc.sel);
        while(it.hasNext())
        {
            IRSave s=it.next();
            if( seltype!=s.type)
            {
                if( s.panel!=null)
                    s.panel.setVisible(false);
            }
            else
            {
                if( s.panel!=null)
                    s.panel.setVisible(true);
                updateInformation(pc.sel, seltype);
            }
        }
    }
    
    public int getType(Positionable pos)
    {
        if( pos instanceof Wurzel)
        {
            return TYPE_WURZEL;
        }
        else if(pos instanceof Korn)
        {
            return TYPE_KORN;
        }
        else
            return 0;
    }
    
    public void updateInformation(Positionable pos, int type)
    {
        if(type==TYPE_WURZEL)
        {
            mf.updateInformationWurzel((Wurzel)pos);
        }
        else if(type==TYPE_KORN)
        {
            mf.updateInformationKorn((Korn)pos);
        }
    }
}
