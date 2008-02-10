/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farmer;

import com.jme.math.FastMath;
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
    public static int sim_corn_init_root_count=2;
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
    public static int sim_root_gravity_func=1; //0=(f(t)=max-a*e^(-k*t)) 1=max*(1/(1+exp((add-t)*k2))
    public static float sim_root_gravity_k2=0.1f;
    public static float sim_root_gravity_add=50.f;
    public static float sim_root_gravity_k=0.05f; //
    public static float sim_root_gravity_max=2.f;
    public static float sim_root_gravity_min_deg=20.f;
    public static float sim_root_gravity_influence1=800.f; //Größer ==> weniger
    public static float sim_root_gravity_influence2=0.03125f; //Kleiner ==> weniger
    public static float sim_collison_savety_distance=0.1f;
    public static int sim_collison_straigt_time=1000;
    public static int sim_collision_straigt_time_back=20000;
    public static float sim_collision_straigt_mult=0.0001f;
    public static float sim_collision_straigt_mult2=1.5f;
    public static float sim_collision_straigt_age_mult=1.f;
    public static int sim_collision_straigt_min_back=10;
    public static float sim_collision_straigt_add=0.000001f;
    public static float sim_collision_straigt_min_degree=0.1f;
    public static float sim_collision_straigt_max=0.00001f;
    public static int sim_root_circle_segments=8;
    public static float view_root_segment_size=2.f;
    public static boolean sim_root_collision_quirk=true;
    public static float view_root_ambient_pc=0.5f;
    public static float view_root_detail=0.1f; //Smaller=>more
    public static float sim_root_thikness=0.01f;
    public static float sim_root_pointness=0.01f; 
    public static float sim_root_stage1_updatetime=0.f;
    public static float sim_root_stage2_updatetime=10.f;
    public static float sim_root_stage3_updatetime=1000.f;
    public static float sim_root_stage4_updatetime=10000.f;
    public static float sim_root_stage5_updatetime=100000.f;
    public static float sim_root_stage6_updatetime=1000000.f;
    public static int sim_root_stage1_barrier=1000;
    public static int sim_root_stage2_barrier=10000;
    public static int sim_root_stage3_barrier=100000;
    public static int sim_root_stage4_barrier=1000000;
    public static int sim_root_stage5_barrier=10000000;
    public static int sim_root_stage6_barrier=100000000;
    public static int view_root_display_mode=2; //1=line 2=pipe 3=both
    public static int sim_root_density_probes=10;
    public static float sim_root_density_probes_max_distance_mult=0.0000005f;
    public static int view_root_color_green_start=255;
    public static int view_root_color_red_start=255;
    public static int view_root_color_blue_start=170;
    public static int view_root_color_green_end=130;
    public static int view_root_color_red_end=130;
    public static int view_root_color_blue_end=0;
    public static float view_root_end_age=1000000.f;
    public static float inf_units_to_centimeters=1.0f;
    public static float inf_simtime_to_time=1.5f;
    public static float sim_root_junction_add_limit=0.00001f;
    public static float sim_root_junction_add=0.f;
    public static float sim_root_junction_prob_fac=0.1f;
    public static float sim_root_junction_erase_distance=10.f;
    public static float sim_root_junction_min_timeleft=100000.f;
    public static float sim_root_junction_min_time_between=10000.f;
    public static float sim_root_junction_max_time_between=200000.f;
    public static float sim_root_min_collision_age=100.f;
    public static float sim_root_junction_no_gravity_time=100.f;
    public static float sim_root_junction_col_check_distance=4.f;
    public static float sim_calc_water_density_fac=2.f;
    public static byte sim_calc_water_default=1;
    public static float view_water_pick_sphere_size=10.f;
    public static boolean sim_calc_pointsearch_test_center=true;
    public static int sim_calc_density_random_percent=100;
    public static boolean sim_calc_density_stronger_interpolation=false;
    public static float sim_root_junction_interpolation_age=1000.f;
}
