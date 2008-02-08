/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.Vector3f;
import com.jme.math.Matrix4f;
import com.jme.math.*;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author urpc
 */
public class Math3D
{
    public static float PI=FastMath.PI;
    public static float GRAD_PI=180.0f/PI;
    public static float BOG=PI/180.0f;
    public static float e=2.71828183f;
    
    public static Vector3f getHorizontalAngle(Vector3f vec)
    {
        Vector3f angle=new Vector3f(0,0,0);
        
        angle.y=FastMath.atan2(vec.x, vec.z);
        angle.y*=GRAD_PI;
        
        if (angle.y < 0.0f) angle.y += 360.0f;
        if (angle.y >= 360.0f) angle.y -= 360.0f;
        
        float z1;
        z1 = FastMath.sqrt(vec.x*vec.x + vec.z*vec.z);
        
        angle.x = FastMath.atan2(z1,vec.y);
        angle.x *= GRAD_PI;
        angle.x -= 90.0f;
        
        if (angle.x < 0.0f) angle.x+= 360.0f;
        if (angle.x >= 360) angle.x -= 360.0f;

        return angle;
    }
    
    public static Vector3f getRotationToTarget(Vector3f position, Vector3f target)
    {
        if( !position.equals(target))
        {
            Vector3f RelativeRotation=new Vector3f(0,0,0);
            Vector3f vect=target.subtract(position);
            vect=Math3D.getHorizontalAngle(vect);
            RelativeRotation.x=vect.x;
            RelativeRotation.y=vect.y;
            return RelativeRotation;
        }
        else
            return new Vector3f(0,0,0);
    }
    
    public static float MakeDegreeNormal(float deg)
    {
            if(deg>0)
            {
                    while(deg-360>0)
                    {
                            deg=deg-360;
                    }
            }
            if(deg<0)
            {
                    while(deg+360<0)
                    {
                            deg=deg+360;
                    }
                    deg=360+deg;
            }
            return deg;
    }
    
    public static Vector3f getHorizontalAngle2(Vector3f vec)
    {
            Vector3f angle=new Vector3f(0,0,0);

            float x=Math.abs(vec.x);
            float z=Math.abs(vec.z);

            if( x!=0 )
                    angle.y = (float)Math.atan(z/x);
            else
                    angle.y=0;
            
            angle.y *= FastMath.RAD_TO_DEG;

            float z1;
            z1 = FastMath.sqrt(vec.x*vec.x + vec.z*vec.z);

            if(z1!=0 )
                    angle.x = (float)Math.atan(vec.y/z1);
            else
                    angle.x=0;

            angle.x *= FastMath.RAD_TO_DEG;

            if( vec.y<=0 && vec.x >0)
                    angle.x =(360.0f-angle.x);
            else if( vec.y>=0 && vec.x<0 )
                    angle.x=(180-angle.x)+180;
            else if( vec.y<=0 && vec.x<0 )
                    angle.x=180+angle.x;
            else if( vec.x==0 && vec.z>0)
                    angle.x=360-angle.x;
            else if( vec.x==0 && vec.z<0 )
                    angle.x-=180;
            else if( vec.y>0 && vec.x>0 )
                    angle.x+=180;

            if( angle.x<90 || angle.x>270 )
            {
                    if( vec.z>0 && vec.x>0 )
                            angle.y=90-angle.y;
                    else if( vec.z>0 && vec.x<0 )
                            angle.y=270+angle.y;
                    else if( vec.z<0 && vec.x<0)
                            angle.y=270-angle.y;
                    else if( vec.z<0 && vec.x>0)
                            angle.y=90+angle.y;
            }
            else
            {
                    if( vec.z>0 && vec.x>0 )
                            angle.y=270-angle.y;
                    else if( vec.z>0 && vec.x<0 )
                            angle.y=90+angle.y;
                    else if( vec.z<0 && vec.x<0)
                            angle.y=90-angle.y;
                    else if( vec.z<0 && vec.x>0)
                            angle.y=270+angle.y;
            }

            angle.y=MakeDegreeNormal(angle.y);
            angle.x=MakeDegreeNormal(angle.x);

            return angle;
    }
    
    public static Vector3f getRotationToTarget2(Vector3f position, Vector3f target)
    {
            if(!position.equals(target))
            {
                    Vector3f RelativeRotation=new Vector3f(0,0,0);
                    Vector3f vect = target.subtract(position);


                    Vector3f rot = getHorizontalAngle2(vect);

                    RelativeRotation.x = rot.x;
                    RelativeRotation.y = rot.y;
                    return RelativeRotation;
            }
            else
                    return new Vector3f(0,0,0);
    }
    
    public static Vector3f getRotationToTarget3(Vector3f position, Vector3f target)
    {
        Vector3f up=new Vector3f(0,1,0);
        Quaternion q=new Quaternion();
        q.lookAt(target.subtract(position), up);
        float [] angles=new float[3];
        q.toAngles(angles);
        up.x=angles[0]*FastMath.RAD_TO_DEG;
        up.y=angles[1]*FastMath.RAD_TO_DEG;
        up.z=angles[2]*FastMath.RAD_TO_DEG;
        return up;
    }
    
    public static Vector3f getRotationToTarget4(Vector3f position, Vector3f target)
    {
        Vector3f direction=position.subtract( target ).normalizeLocal();
        Vector3f up=new Vector3f(0,1,0);
        Vector3f left=new Vector3f();
        left.set( up ).crossLocal( direction ).normalizeLocal();
        if( left.equals( Vector3f.ZERO ) )
            if( direction.x != 0 )
                left.set( direction.y, -direction.x, 0f );
            else
                left.set( 0f, direction.z, -direction.y );
        up.set( direction ).crossLocal( left ).normalizeLocal();
        
        left.x*=FastMath.RAD_TO_DEG;
        left.y*=FastMath.RAD_TO_DEG;
        left.z*=FastMath.RAD_TO_DEG;
        
        return left;        
    }
    
    public static Vector3f getTarget(Vector3f pos,Vector3f rot,float distance)
    {
            Vector3f target=new Vector3f(0,0,1);
            Matrix4f mat=new Matrix4f();
            
            rot.z=0;
            mat.angleRotation(rot);
            
            Math3D.transformVec(target, mat);
            target.normalizeLocal();

            Vector3f end=pos.add(target.mult(distance));

            return end;
    }
    
    public static Vector3f getTargetDistance(Vector3f position, Vector3f target, float distance)
    {
            Vector3f direction=target.subtract(position);
            direction.normalize();
            direction.multLocal(distance);
            return position.add(direction);
    }
    
    public static void setRotation(Spatial node, float x, float y, float z)
    {
        Quaternion q=new Quaternion();
        q.fromAngles(x*FastMath.DEG_TO_RAD, y*FastMath.DEG_TO_RAD, z*FastMath.DEG_TO_RAD);
        node.setLocalRotation(q);
    }
    
    public static void setRotation(Spatial node, Vector3f vec)
    {
        Quaternion q=new Quaternion();
        q.fromAngles(vec.x*FastMath.DEG_TO_RAD, vec.y*FastMath.DEG_TO_RAD, vec.z*FastMath.DEG_TO_RAD);
        node.setLocalRotation(q);
    }
    
    public static Vector3f getRotation(Spatial node)
    {
        Vector3f v=new Vector3f();
        Quaternion q=node.getLocalRotation();
        float []f=new float[3];
        q.toAngles(f);
        v.x=f[0]*FastMath.RAD_TO_DEG;
        v.y=f[1]*FastMath.RAD_TO_DEG;
        v.z=f[2]*FastMath.RAD_TO_DEG;
        return v;
    }
    
    public static Vector3f toDegree(Quaternion q)
    {
        Vector3f v=new Vector3f();
        float []f=new float[3];
        q.toAngles(f);
        v.x=f[0]*FastMath.RAD_TO_DEG;
        v.y=f[1]*FastMath.RAD_TO_DEG;
        v.z=f[2]*FastMath.RAD_TO_DEG;
        return v;
    }
    
    /*public static Vector3f getTarget2(Vector3f pos, Vector3f rot, float distance)
    {
            rot.x*=BOG;
            Vector3f target=new Vector3f(0,0,0);
            target.x=FastMath.cos(rot.x);
            target.z=FastMath.sin(rot.x);
            target.y=0;

            Matrix4f mat=new Matrix4f();
            rot.x=0;rot.z=0;
            mat.angleRotation(rot);
            Math3D.transformVec(target, mat);
            target.normalizeLocal();

            return pos.add(target.mult(distance));
    }*/
    
    public static void transformVec(Vector3f vec, Matrix4f mat)
    {
        float [] erg=new float[3];
        
        erg[0]=vec.x*mat.m00+vec.y*mat.m01+vec.z*mat.m02+mat.m03;
        erg[1]=vec.x*mat.m10+vec.y*mat.m11+vec.z*mat.m12+mat.m13;
        erg[2]=vec.x*mat.m20+vec.y*mat.m21+vec.z*mat.m22+mat.m23;
        
        vec.x=erg[0];
        vec.y=erg[1];
        vec.z=erg[2];
    }
    
    public static Vector3f[] getSurroundingPoints(Vector3f p, float dist)
    {
        Vector3f [] ret=new Vector3f[6];
        
        ret[0]=new Vector3f(p);
        ret[0].x+=dist;
        ret[1]=new Vector3f(p);
        ret[1].x-=dist;
        ret[2]=new Vector3f(p);
        ret[2].y+=dist;
        ret[3]=new Vector3f(p);
        ret[3].y-=dist;
        ret[4]=new Vector3f(p);
        ret[4].z+=dist;
        ret[5]=new Vector3f(p);
        ret[5].z-=dist;
        
        return ret;
    }
    
    public static Vector3f[] getSurroundingPointsPlusOwn(Vector3f p, float dist)
    {
        Vector3f [] ret=new Vector3f[27];
        
        ret[0]=new Vector3f(p);
        ret[0].x+=dist;
        ret[1]=new Vector3f(p);
        ret[1].x-=dist;
        ret[2]=new Vector3f(p);
        ret[2].y+=dist;
        ret[3]=new Vector3f(p);
        ret[3].y-=dist;
        ret[4]=new Vector3f(p);
        ret[4].z+=dist;
        ret[5]=new Vector3f(p);
        ret[5].z-=dist;
        
        ret[6]=new Vector3f(p);
        ret[6].z-=dist;
        ret[6].y-=dist;
        ret[7]=new Vector3f(p);
        ret[7].z-=dist;
        ret[7].y+=dist;
        ret[8]=new Vector3f(p);
        ret[8].z-=dist;
        ret[8].x-=dist;
        ret[9]=new Vector3f(p);
        ret[9].z-=dist;
        ret[9].x+=dist;
        ret[10]=new Vector3f(p);
        ret[10].z+=dist;
        ret[10].y-=dist;
        ret[11]=new Vector3f(p);
        ret[11].z+=dist;
        ret[11].y+=dist;
        ret[12]=new Vector3f(p);
        ret[12].z+=dist;
        ret[12].x-=dist;
        ret[13]=new Vector3f(p);
        ret[13].z+=dist;
        ret[13].x+=dist;
        
        ret[14]=new Vector3f(p);
        ret[14].y-=dist;
        ret[14].x-=dist;
        ret[15]=new Vector3f(p);
        ret[15].y-=dist;
        ret[15].x+=dist;
        ret[16]=new Vector3f(p);
        ret[16].y+=dist;
        ret[16].x-=dist;
        ret[17]=new Vector3f(p);
        ret[17].y+=dist;
        ret[17].x+=dist;
        
        ret[18]=new Vector3f(p);
        ret[18].y-=dist;
        ret[18].x-=dist;
        ret[18].z-=dist;
        
        ret[19]=new Vector3f(p);
        ret[19].y+=dist;
        ret[19].x-=dist;
        ret[19].z-=dist;
        
        ret[20]=new Vector3f(p);
        ret[20].y-=dist;
        ret[20].x+=dist;
        ret[20].z-=dist;
        
        ret[21]=new Vector3f(p);
        ret[21].y-=dist;
        ret[21].x-=dist;
        ret[21].z+=dist;
        
        ret[22]=new Vector3f(p);
        ret[22].y+=dist;
        ret[22].x+=dist;
        ret[22].z-=dist;
        
        ret[23]=new Vector3f(p);
        ret[23].y+=dist;
        ret[23].x-=dist;
        ret[23].z+=dist;
        
        ret[24]=new Vector3f(p);
        ret[24].y-=dist;
        ret[24].x+=dist;
        ret[24].z+=dist;
        
        ret[25]=new Vector3f(p);
        ret[25].y+=dist;
        ret[25].x+=dist;
        ret[25].z+=dist;
        
        ret[26]=p.clone();
        
        return ret;
    }
    
    private static Vector3f []circle_points;
    
    public static Vector3f[] getCircleSegments(Vector3f pos, Vector3f rot, float r)
    {
        if( circle_points==null || circle_points.length!=Settings.sim_root_circle_segments)
        {
            circle_points=new Vector3f[Settings.sim_root_circle_segments];
            
            float astep=FastMath.TWO_PI/circle_points.length;
            for(int i=0;i<circle_points.length;++i)
            {
                float x=FastMath.sin(i*astep);
                float z=FastMath.cos(i*astep);
                circle_points[i]=new Vector3f(x,0,z);
            }
        }
        
        Vector3f []ret=new Vector3f[circle_points.length];
        
        for(int i=0;i<circle_points.length;++i)
        {
            ret[i]=transformVec(circle_points[i], rot, pos, new Vector3f(r,r,r));
        }
        
        return ret;
    }
    
    public static Vector3f intersection(Vector3f start, Vector3f dir, Plane plane)
    {
        float t2=plane.getNormal().dot(dir);
        
        if(t2==0)
            return null;
        
        float t= -(plane.getNormal().dot(start)+plane.getConstant())/t2;
        Vector3f out=start.add(dir.mult(t));
        return out;
    }
    
    public static Vector3f transformVec(Vector3f in, Vector3f rotation, Vector3f translation, Vector3f scale)
    {
        Quaternion q=new Quaternion();
        q.fromAngles(rotation.x*FastMath.DEG_TO_RAD, rotation.y*FastMath.DEG_TO_RAD, rotation.z*FastMath.DEG_TO_RAD);
        Vector3f ret=q.mult(in.mult( scale ) );
        ret.addLocal( translation);
        return ret;
    }
}
