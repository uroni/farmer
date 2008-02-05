/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import java.io.Serializable;
import com.jme.math.Vector3f;

/**
 *
 * @author urpc
 */
public class Settings implements Serializable
{
    static final long serialVersionUID = 10275539472837495L;
    
    public static int sys_initial_update_ms=100;    
    public static int sys_camera_update_ms=10;
    public static int sys_position_update_ms=10;
    public static int sys_update_simulation=10;
    public static float input_mouse_rotation_scale=1.f;
    public static float input_mouse_fpscam_scale=0.01f;
    public static float input_mouse_wheel_unit=3.f;
    public static float camera_initial_view_distance=25.0f;
    public static float camera_min_view_distance=10.f;
    public static float view_root_arrow_length=0.4f;
    public static float view_root_arrow_width=0.04f;
    public static int sim_corn_init_root_count=3;
    public static float ctrl_corn_pos_step=0.1f;
    public static float ctrl_corn_rot_step=0.6f;
    public static float ctrl_root_pos_step=0.02f;
    public static float ctrl_root_rot_step=0.6f;
    public static int ctrl_position_inital_delay=1000;
    public static int ctrl_position_delay=20;
    public static float ctrl_light_pos_step=0.5f;
    public static float ctrl_solid_pos_step=0.2f;
    public static float ctrl_solid_rot_step=0.6f;
    public static float ctrl_material_pos_step=0.2f;
    public static float ctrl_material_rot_step=0.6f;
    public static float ctrl_acceleration_per_sec=2.5f;
    public static float ctrl_dv_pos_step=0.2f;
    public static float ctrl_dv_rot_step=0.6f;
    public static float view_dens_width=5.f;
    public static float view_dens_height=5.f;
    public static int view_dens_update_delay=500;
    public static boolean view_dens_interpolate=false;
    public static float view_dens_interpolation_radius=1.5f;
    public static int view_dens_pixel_size=512;
    public static float ctrl_default_pos_step=0.2f;
    public static float ctrl_default_rot_step=0.6f;
    public static float calc_water_density_distance_multi=3.f;
    public static float ctrl_camera_pos_step=0.2f;
    public static float ctrl_fpscamera_pos_step=0.2f;
    public static Vector3f sim_root_gravity=new Vector3f(0,1,0);
    public static float sim_root_gravity_k=0.05f; //f(t)=max-a*e^(-k*t)
    public static float sim_root_gravity_max=2.f;
    public static float sim_root_gravity_min_deg=20.f;
    public static float sim_root_gravity_influence=800.f; //Größer ==> weniger
}
