/*
 * Render3D.java
 *
 * Created on November 28, 2007, 5:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package farmer;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Canvas;
import com.jme.app.AbstractGame;
import com.jme.app.BaseGame;
import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.joystick.JoystickInput;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.*;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.geom.Debugger;
import com.jme.bounding.BoundingSphere;
import com.jmex.awt.JMECanvas;
import com.jmex.awt.JMECanvasImplementor;
import com.jme.scene.shape.Box;
import com.jme.bounding.BoundingBox;
import java.net.URL;
import java.net.URI;
import com.jmex.model.converters.*;
import java.io.*;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.renderer.Renderer;
import com.jme.light.PointLight;
import com.jme.util.geom.BufferUtils;
import java.nio.*;
import com.jme.bounding.CollisionTree;
import com.jme.bounding.CollisionTreeManager;
import com.jme.math.Ray;
import com.jme.intersection.TrianglePickResults;
import com.jme.image.Image;
import com.jmex.awt.swingui.ImageGraphics;
import com.jme.image.Texture;
import com.jme.intersection.PickData;
import com.jme.math.Plane;
import com.jme.scene.batch.TriangleBatch;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.GameTaskQueueManager;
import java.util.*;

/**
 *
 * @author urpc
 */
public class Render3D extends JMECanvasImplementor {

    // Items for scene
    protected Node rootNode, colNode, materialNode;
    
    protected Text fps;

    protected Timer timer;

    protected float tpf;

    protected Camera cam;

    protected int width, height;
    
    protected DisplaySystem display;
    
    protected static String fontLocation=Text.DEFAULT_FONT;
    
    private boolean bMoveCam;
    private Vector3f CameraCenter=new Vector3f(0,0,0);
    private Point last_mouse;
    private Vector3f CameraCenterRotation=new Vector3f(0,0,0);
    private boolean bCamChanged;
    private float CameraDistance;
    private Box box;
    
    private List<ImageTexture> updateTextures=new LinkedList<ImageTexture>();
    private boolean imageTexturesDirty=true;
    
    public CameraInterface camera;

    /**
     * This class should be subclasses - not directly instantiated.
     * @param width canvas width
     * @param height canvas height
     */
    protected Render3D(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    //@Override
    public void doSetup() {
        if( setup )
            return;
        
        display = DisplaySystem.getDisplaySystem();
        try
        {
        display.initForCanvas(width, height);
        }catch(Exception e)
        {
            return;
        }
        renderer = display.getRenderer();

        /**
         * Create a camera specific to the DisplaySystem that works with the
         * width and height
         */
        
        CollisionTreeManager.getInstance().setTreeType(CollisionTree.AABB_TREE);
        
        cam = renderer.createCamera(width, height);

        cam.setFrustumPerspective(45.0f, (float) width / (float) height, 1,   10000);
        cam.setParallelProjection( false );
        Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
        Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
        cam.setFrame(loc, left, up, dir);
        cam.update();
        /** Assign the camera to this renderer. */
        renderer.setCamera(cam);
        
        
        /** Set a black background. */
        renderer.setBackgroundColor(ColorRGBA.black.clone());

        /** Get a high resolution timer for FPS updates. */
        timer = Timer.getTimer();

        if( rootNode==null)
        {
            try
            {
                com.jme.util.resource.SimpleResourceLocator srl=new com.jme.util.resource.SimpleResourceLocator(this.getClass().getClassLoader().getResource("."));
                com.jme.util.resource.ResourceLocatorTool.addResourceLocator(com.jme.util.resource.ResourceLocatorTool.TYPE_TEXTURE, srl);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            newRootNode();
        }
            
        
        if(camera==null)
        {
            camera=new CameraRotation(cam, this);
            MainForm.getMainForm().getPositionControl().addPositionable(camera);
            MainForm.getMainForm().updatePositionSelectBox();
            MainForm.getMainForm().getSimulation().setCamera(camera);
        }
        else
            camera.setCamera(cam);
        camera.update();
        
        /*box=new Box("MyBox", new Vector3f(0,0,0), 5,5,5);
        box.setModelBound(new BoundingBox());
        box.updateModelBound();               
        
        rootNode.attachChild(box);*/
       
        /*RPoint p1=new RPoint();
        p1.pos=new Vector3f(0,0,0);
        p1.korn=null;
        
        RPoint p2=new RPoint();
        p2.pos=new Vector3f(-15,-1,-5);
        p2.korn=null;
        
        RPoint p3=new RPoint();
        p3.pos=new Vector3f(15,-2,0);
        p3.korn=null;
        
        RPoint p4=new RPoint();
        p4.pos=new Vector3f(25,-10,0);
        p4.korn=null;
        
        RPoint p5=new RPoint();
        p5.pos=new Vector3f(35,-2,0);
        p5.korn=null;
        
        Segment s=new Segment(this, true ,MainForm.getMainForm().getSimulation());
        s.add(p2);
        s.add(p1);
        s.add(p3);
        s.add(p4);
        s.add(p5);
        s.update();*/
        
         
        /*Node n1=new Node("test1");
        Node n2=new Node("test2");
        
        Vector3f [] pts=Math3D.getCircleSegments(new Vector3f(0,0,0), new Vector3f(90,0,0), 5.f);
        for(int i=0;i<pts.length;++i)
        {
            Sphere ball1=new Sphere("ball", 10, 10, 0.1f);
            ball1.setLocalTranslation(pts[i]);
            ball1.setSolidColor(ColorRGBA.pink);
            n1.attachChild(ball1);
            
            Sphere ball2=new Sphere("ball", 10, 10, 0.1f);
            ball2.setLocalTranslation(pts[i]);
            ball2.setSolidColor(ColorRGBA.pink);
            n2.attachChild(ball2);
        }
        
        n2.setLocalTranslation(new Vector3f(15,5,0));
        
        n1.lookAt(n2.getLocalTranslation(), new Vector3f(0,1,0));
        n2.lookAt(n1.getLocalTranslation(), new Vector3f(0,1,0));
        
        this.addtoScene(n1);
        this.addtoScene(n2);*/
        
        
        /*PointLight l=new PointLight();
        l.setLocation(new Vector3f(0,100,0));
        l.setEnabled(true);
        
        LightState ls=renderer.createLightState();
        ls.attach(l);
        rootNode.setRenderState(ls);
        rootNode.updateRenderState();*/
        
        /*Node ps=this.loadMdl("Petrischale_v2.3ds");
        
        this.addtoScene(ps);
        this.makeTransparent( ps);*/

        //initView();
        setup = true;
        timer.reset();
    }
    
    public Box getBox(){ return box; }
    
    public void setCamera(CameraInterface ccam)
    {
        camera=ccam;
    }
    
    public CameraInterface getCameraInterface()
    {
        return camera;
    }
    
    public void initView()
    {
        /*AlphaState as1 = display.getRenderer().createAlphaState();
            
        as1.setBlendEnabled(true);
        as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as1.setDstFunction(AlphaState.DB_ONE);
        as1.setTestEnabled(true);
        as1.setTestFunction(AlphaState.TF_GREATER);
        as1.setEnabled(false);
        
        rootNode.setRenderState(as1);*/
       
        
    }
    
    public void doUpdate() {
        if(!setup)
            return;
        
        timer.update();
        /** Update tpf to time per frame according to the Timer. */
        tpf = timer.getTimePerFrame();

        rootNode.updateGeometricState(tpf, true);
        
        camera.update();
        
    }
    
    public java.awt.Canvas createCanvas(int width, int height)
    {
        java.awt.Canvas ret = DisplaySystem.getDisplaySystem("lwjgl").createCanvas(width, height);
        /*ret.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent ce) {
                    doResize();
                }
            });*/
        JMECanvas jmeCanvas = ( (JMECanvas) ret );
        jmeCanvas.setImplementor(this);
        return ret;
    }
    
    protected void doResize()
    {
        System.out.println("Resizing...");
        resizeCanvas(width, height);
    }
    
    public void doResize(int width, int height)
    {
        if( width!=this.width || height!=this.height)
        {
            this.width=width;
            this.height=height;
            if( setup)
            {
                resizeCanvas(width, height);
                setup=false;
            }
        }
    }

    public void doRender() {
        if( !setup)
            return;
        
        ListIterator<ImageTexture> it=updateTextures.listIterator();
        while(it.hasNext())
        {
            ImageTexture imgt=it.next();
            imgt.ig.update(imgt.tex, imageTexturesDirty);
        }
        
        if(imageTexturesDirty)
            imageTexturesDirty=false;
        
        renderer.clearBuffers();
        //renderer.renderQueue();
        renderer.draw(rootNode);
        
        renderer.displayBackBuffer();
    }

    public Camera getCamera() {
        return cam;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public float getTimePerFrame() {
        return tpf;
    }
    
    public void setMoveCamera(boolean b)
    {
        bMoveCam=b;
    }
    
    private static int nummdl=0;
    public Node loadMdl(String name)
    {
        
        Node ret=null;
        URL modelURL=this.getClass().getClassLoader().getResource( name);
        if( modelURL==null )
        {
            System.out.println("Resource "+name+" not found");
            return null;
        }
        FormatConverter converter = new MaxToJme();
        ByteArrayOutputStream BO=new ByteArrayOutputStream();
        try
        {
       
            converter.convert(modelURL.openStream(), BO);
            ret=(Node)BinaryImporter.getInstance().load(new ByteArrayInputStream(BO.toByteArray()));
            ret.setModelBound(new BoundingBox());
            ret.updateModelBound();
            ret.setName("3dsModel "+(++nummdl));
            
            
            MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
            ms.setAmbient(new ColorRGBA(1.f, 1.f, 1.f, 1.0f));
            ms.setDiffuse(new ColorRGBA(2.f, 2.f, 2.f, 1.0f));
            ms.setShininess(128);
            ms.setColorMaterial(MaterialState.CM_AMBIENT_AND_DIFFUSE);
            ms.setMaterialFace(MaterialState.MF_FRONT);
            ret.setRenderState(ms);
            ret.updateRenderState();
            
            ret.setLocalScale(5);
            Math3D.setRotation(ret, 0,0,180);
     
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        ret.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        return ret;
    }
    
    public void addtoScene(Spatial node)
    {
        rootNode.attachChild(node);
        rootNode.updateRenderState();
    }
    
    public void addtoSceneMat(Spatial node)
    {
        materialNode.attachChild(node);
        materialNode.updateRenderState();
    }
    
    public void addtoSceneCol(Spatial node)
    {
        colNode.attachChild(node);
        colNode.updateRenderState();
    }
    
    public void removeFromScene(Spatial node)
    {
        rootNode.detachChild(node);
        rootNode.updateRenderState();
    }
    
    public void removeFromSceneCol(Spatial node)
    {
        colNode.attachChild(node);
        colNode.updateRenderState();
    }
    
    public void removeFromSceneMat(Spatial node)
    {
        materialNode.attachChild(node);
        materialNode.updateRenderState();
    }
    
    public MaterialState createMaterialState()
    {
        return DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
    }
    
    public TextureState createTextureState()
    {
        return DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
    }
    
    public AlphaState createAlphaState()
    {
        return DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
    }
    
    public LightState createLightState()
    {
        return DisplaySystem.getDisplaySystem().getRenderer().createLightState();
    }
    
    public void makeTransparent(Node node)
    {
        for(int i=0;i<node.getQuantity();++i)
        {
            Spatial s=node.getChild(i);
            if( s instanceof Geometry)
            {
                Geometry g=(Geometry)s;
                AlphaState as=renderer.createAlphaState();
                as.setBlendEnabled(true);
                as.setSrcFunction(AlphaState.DB_SRC_ALPHA);
                as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
                as.setTestEnabled(false);
                as.setTestFunction(AlphaState.TF_GEQUAL);
                as.setEnabled(true);
                

                g.setRenderState(as);
                g.updateRenderState();
            }
        }
        
        node.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
    }
    
    public void enableLightning(Spatial node)
    {
        node.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        node.setLightCombineMode(LightState.INHERIT);
        node.updateRenderState();
    }
    
    public void disableLightning(Spatial node)
    {
        node.setLightCombineMode(LightState.OFF);
        node.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        node.updateRenderState();
    }
    
    public DisplaySystem getDisplay()
    {
        return display;
    }
    
    public void newRootNode()
    {
        rootNode = new Node("rootNode");

       
        ZBufferState buf = renderer.createZBufferState();
        buf.setEnabled(true);
        buf.setWritable(true);
        buf.setFunction(ZBufferState.CF_LEQUAL);

        rootNode.setRenderState(buf);
        
        
        
        rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        
        colNode=new Node("Collision Node");
        materialNode=new Node("Material Node");
        rootNode.attachChild(colNode);
        rootNode.attachChild(materialNode);
        
        colNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        materialNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        
        rootNode.updateGeometricState(0.0f, true);
        rootNode.updateRenderState();
        
        
    }
    
    public void setOpacy(Node node, int pc)
    {
        float op=(float)pc/100.f;
        ColorRGBA c=new ColorRGBA(1.f,1.f,1.f,op);
        
        for(int i=0;i<node.getQuantity();++i)
        {
            Spatial s=node.getChild(i);
            if( s instanceof Geometry)
            {
                Geometry g=(Geometry)s;
                AlphaState as=renderer.createAlphaState();
                as.setBlendEnabled(true);
                as.setSrcFunction(AlphaState.DB_SRC_ALPHA);
                as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
                as.setTestEnabled(false);
                as.setTestFunction(AlphaState.TF_GEQUAL);
                as.setEnabled(true);

                g.setRenderState(as);
                g.setDefaultColor(c);
                g.updateRenderState();
                g.updateGeometricState(0.f, false);
            }
            
            /*if( s instanceof Node)
            {
                setColor(c, (Node)s);
            }*/
        }
        
        node.setLightCombineMode(LightState.OFF);
        node.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        node.updateRenderState();
    }
    
    public boolean isInScene(Spatial node)
    {
        return rootNode.hasChild(node);
    }
    
    public boolean isInSceneMat(Spatial node)
    {
        return materialNode.hasChild(node);
    }
    
    public boolean isInSceneCol(Spatial node)
    {
        return colNode.hasChild(node);
    }
    
    public Vector3f getPick(Ray ray)
    {
        TrianglePickResults trp=new TrianglePickResults();
        trp.setCheckDistance(true);
        materialNode.findPick(ray, trp);
        
        if( trp.getNumber()>0)
        {
            PickData pData = trp.getPickData(0);
            ArrayList<Integer> al=pData.getTargetTris();
            TriangleBatch mesh=(TriangleBatch)pData.getTargetMesh();
            
            if( al.size()>0)
            {
                int triIndex=al.get(0);
                Vector3f []trivec=new Vector3f[3];
                mesh.getTriangle(triIndex, trivec);
                
                for(int i=0;i<trivec.length;++i)
                {
                    mesh.getParentGeom().localToWorld(trivec[i], trivec[i]);
                }

                Vector3f ret=new Vector3f(0,0,0);
                boolean b=ray.intersectWhere(trivec[0], trivec[1], trivec[2], ret);
                if(!b)
                    System.out.println("Unable to find intersection point in getPick!");

                return ret;
            }
        }
        
        return null;
    }
    
    public Vector3f[] collides(Vector3f lineStart, Vector3f lineEnd, Spatial target, Geometry exclude, boolean checkOnly, float add_distance)
    {    
        if( target==null)
            target=colNode;
        {
            Vector3f vec=lineEnd.clone();
            vec.subtractLocal(lineStart);
            
            float distance=vec.length()+add_distance*2;
            float distanceSQ=distance*distance;
            vec.normalizeLocal();
            if( add_distance!=0 )
            {
                lineStart=lineStart.add(vec.mult(-1*add_distance));
            }
            Ray ray=new Ray(lineStart, vec);
            TrianglePickResults trp=new TrianglePickResults();
            trp.setCheckDistance(true);
            target.findPick(ray, trp);
            
            for(int i=0;i<trp.getNumber();++i)
            {
                if( trp.getPickData(i).getDistance()<=distance )
                {
                    if( exclude!=null && exclude==trp.getPickData(i).getTargetMesh().getParentGeom() )//exclude.hasChild(trp.getPickData(i).getTargetMesh().getParentGeom()) )
                        continue;
                    
                    if( checkOnly == true)
                        return new Vector3f[0];
                    
                    PickData pData = trp.getPickData(i);
                    ArrayList<Integer> al=pData.getTargetTris();
                    TriangleBatch mesh=(TriangleBatch)pData.getTargetMesh();
                    
                    
                    for(int l=0;l<al.size();++l)
                    {
                        int triIndex=al.get(l);
                        Vector3f []trivec=new Vector3f[3];
                        mesh.getTriangle(triIndex, trivec);
                        Vector3f []out=new Vector3f[4];
                        for(int j=0;j<trivec.length;++j)
                        {
                            out[j]=new Vector3f();
                            mesh.getParentGeom().localToWorld(trivec[j], out[j]);
                        }
                        out[3]=new Vector3f(0,0,0);
                        boolean b=ray.intersectWhere(out[0], out[1], out[2], out[3]);
                        if(!b)
                            System.out.println("Unable to find intersection point in collides!");
                        
                        if(lineStart.distanceSquared(out[3])<=distanceSQ)                        
                            return out;                                                
                    }
                }
            }
            return null;            
        }
    }
    
    public void addUpdateTexture(ImageGraphics ig, Texture tex)
    {
        ImageTexture it=new ImageTexture();
        it.ig=ig;
        it.tex=tex;
        updateTextures.add(it);
    }
    
    public boolean removeUpdateTexture(ImageGraphics ig)
    {
        ListIterator<ImageTexture> it=updateTextures.listIterator();
        
        while(it.hasNext())
        {
            ImageTexture ff=it.next();
            
            if(ff.ig==ig)
            {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
    public void setImageTexturesDirty(boolean b)
    {
        imageTexturesDirty=b;
    }
}

class ImageTexture
{
    public ImageGraphics ig;
    public Texture tex;
}
