/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package stereo.processor;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.PaintMode;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import com.jme3.util.TempVars;
import com.jme3.water.ReflectionProcessor;
import com.jme3.water.WaterUtils;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.jme3.math.FastMath.HALF_PI;
import static com.jme3.math.FastMath.PI;

public class ModifiedWaterProcessor implements SceneProcessor {

    protected RenderManager rm;
    protected ViewPort vp;
    protected Spatial reflectionScene;
    protected ViewPort reflectionView;
    protected ViewPort refractionView;
    protected FrameBuffer reflectionBuffer;
    protected FrameBuffer refractionBuffer;
    protected Camera reflectionCam;
    protected Camera refractionCam;
    protected Texture2D reflectionTexture;
    protected Texture2D refractionTexture;
    protected Texture2D depthTexture;
    protected Texture2D normalTexture;
    protected Texture2D dudvTexture;
    protected int renderWidth = 512;
    protected int renderHeight = 512;
    protected Plane plane = new Plane(Vector3f.UNIT_Y, Vector3f.ZERO.dot(Vector3f.UNIT_Y));
    protected float speed = 0.05f;

    private FrameBuffer fb;
    private ByteBuffer byteBuf;
    private IntBuffer intBuf;
    private BufferedImage img;

    protected AssetManager manager;
    protected Material material;
    protected float waterDepth = 1;
    protected float waterTransparency = 0.4f;
    protected boolean debug = false;

    private Picture dispRefraction;
    private Picture dispReflection;
    private Picture dispDepth;

    private Plane reflectionClipPlane;
    private Plane refractionClipPlane;

    private float refractionClippingOffset = 0.3f;
    private float reflectionClippingOffset = -5f;        

    private float distortionScale = 0.2f;
    private float distortionMix = 0.5f;
    private float texScale = 1f;

    private int widthPicture = 600;
    private int heightPicture = 600;



    /**
     * Creates a ModifiedWaterProcessor
     * @param manager the asset manager
     */
    public ModifiedWaterProcessor(AssetManager manager) {
        this.manager = manager;
        material = new Material(manager, "Common/MatDefs/Water/SimpleWater.j3md");
        material.setFloat("waterDepth", waterDepth);
        material.setFloat("waterTransparency", waterTransparency / 10);
        material.setColor("waterColor", ColorRGBA.White);
        material.setVector3("lightPos", new Vector3f(1, -1, 1));
        
        material.setFloat("distortionScale", distortionScale);
        material.setFloat("distortionMix", distortionMix);
        material.setFloat("texScale", texScale);
        updateClipPlanes();


    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;

        loadTextures(manager);
        createTextures();
        applyTextures(material);

        createPreViews();

        material.setVector2("FrustumNearFar", new Vector2f(vp.getCamera().getFrustumNear(), vp.getCamera().getFrustumFar()));

        if (debug) {
            dispRefraction = new Picture("dispRefraction");
            dispRefraction.setTexture(manager, refractionTexture, false);
            dispReflection = new Picture("dispRefraction");
            dispReflection.setTexture(manager, reflectionTexture, false);
            dispDepth = new Picture("depthTexture");
            dispDepth.setTexture(manager, depthTexture, false);
        }

        byteBuf = BufferUtils.ensureLargeEnough(byteBuf, widthPicture * heightPicture * 4);
        intBuf = byteBuf.asIntBuffer();

        if (fb != null) {
            fb.dispose();
            fb = null;
        }

        fb = new FrameBuffer(widthPicture, heightPicture, 1);
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return rm != null;
    }
    float time = 0;
    float savedTpf = 0;

    public void preFrame(float tpf) {
        time = time + (tpf * speed);
        if (time > 1f) {
            time = 0;
        }
        material.setFloat("time", time);
        savedTpf = tpf;
    }

    public void postQueue(RenderQueue rq) {
        Camera sceneCam = rm.getCurrentCamera();

//        refractionCam.setFrustum(sceneCam.getFrustumNear(),
//                sceneCam.getFrustumFar(),
//                sceneCam.getFrustumLeft(),
//                sceneCam.getFrustumRight(),
//                sceneCam.getFrustumTop(),
//                sceneCam.getFrustumBottom());
//        reflectionCam.setFrustum(sceneCam.getFrustumNear(),
//                sceneCam.getFrustumFar(),
//                sceneCam.getFrustumLeft(),
//                sceneCam.getFrustumRight(),
//                sceneCam.getFrustumTop(),
//                sceneCam.getFrustumBottom());
//
//        refractionCam.setParallelProjection(sceneCam.isParallelProjection());
//        reflectionCam.setParallelProjection(sceneCam.isParallelProjection());


        //Rendering reflection and refraction
        rm.renderViewPort(reflectionView, savedTpf);
        rm.renderViewPort(refractionView, savedTpf);
        rm.getRenderer().setFrameBuffer(vp.getOutputFrameBuffer());
        rm.setCamera(sceneCam, false);

    }

    public void postFrame(FrameBuffer out) {
        if (debug) {
            displayMap(rm.getRenderer(), dispRefraction, 40);
            displayMap(rm.getRenderer(), dispReflection, 640);
            //displayMap(rm.getRenderer(), dispDepth, 448);

//            byteBuf.clear();
//            rm.getRenderer().readFrameBuffer(fb, byteBuf);
//            intBuf = byteBuf.asIntBuffer();
            //Screenshots.convertScreenShot2(intBuf, img);

        }
    }

    public void cleanup() {
    }

    //debug only : displays maps
    protected void displayMap(Renderer r, Picture pic, int left) {
        Camera cam = vp.getCamera();
        rm.setCamera(cam, true);
        int h = cam.getHeight();
        pic.setPosition(left, h / 20f);
        pic.setWidth(widthPicture);
        pic.setHeight(heightPicture);
        pic.updateGeometricState();
        rm.renderGeometry(pic);
        rm.setCamera(cam, false);
    }

    protected void loadTextures(AssetManager manager) {
        normalTexture = (Texture2D) manager.loadTexture("Common/MatDefs/Water/Textures/water_normalmap.png");
        dudvTexture = (Texture2D) manager.loadTexture("Common/MatDefs/Water/Textures/dudv_map.jpg");
        normalTexture.setWrap(WrapMode.Repeat);
        dudvTexture.setWrap(WrapMode.Repeat);
    }

    protected void createTextures() {
        reflectionTexture = new Texture2D(renderWidth, renderHeight, Format.RGBA8);
        refractionTexture = new Texture2D(renderWidth, renderHeight, Format.RGBA8);
        
        reflectionTexture.setMinFilter(Texture.MinFilter.Trilinear);
        reflectionTexture.setMagFilter(Texture.MagFilter.Bilinear);
        
        refractionTexture.setMinFilter(Texture.MinFilter.Trilinear);
        refractionTexture.setMagFilter(Texture.MagFilter.Bilinear);
        
        depthTexture = new Texture2D(renderWidth, renderHeight, Format.Depth);
    }

    protected void applyTextures(Material mat) {
        mat.setTexture("water_reflection", reflectionTexture);
        mat.setTexture("water_refraction", refractionTexture);
        mat.setTexture("water_depthmap", depthTexture);
        mat.setTexture("water_normalmap", normalTexture);
        mat.setTexture("water_dudvmap", dudvTexture);
    }

    protected void createPreViews() {
        reflectionCam = new Camera(renderWidth, renderHeight);
        refractionCam = new Camera(renderWidth, renderHeight);

        refractionCam.setLocation(new Vector3f(10, 20, 0));
        refractionCam.setRotation(new Quaternion().fromAngles(HALF_PI, 0, -0.5f));
        reflectionCam.setLocation(new Vector3f(- 10, 20, 0));
        reflectionCam.setRotation(new Quaternion().fromAngles(HALF_PI, 0, 0.5f));

        final float frustum = 0.5522848f;
        refractionCam.setFrustum(1,
                1000,
                -frustum, frustum, frustum, -frustum);
        reflectionCam.setFrustum(1,
                1000,
                -frustum, frustum, frustum, -frustum);

        refractionCam.setParallelProjection(false);
        reflectionCam.setParallelProjection(false);



//        reflectionCam.setLocation(new Vector3f(0, 100, 0));
//        reflectionCam.setRotation(new Quaternion().fromAngles(HALF_PI, PI, 0));

        // create a pre-view. a view that is rendered before the main view
        reflectionView = new ViewPort("Reflection View", reflectionCam);
        reflectionView.setClearFlags(true, true, true);
        reflectionView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        reflectionBuffer = new FrameBuffer(renderWidth, renderHeight, 1);
        //setup framebuffer to use texture
        reflectionBuffer.setDepthBuffer(Format.Depth);
        reflectionBuffer.setColorTexture(reflectionTexture);

        //set viewport to render to offscreen framebuffer
        reflectionView.setOutputFrameBuffer(reflectionBuffer);

        //reflectionView.addProcessor(new ReflectionProcessor(reflectionCam, reflectionBuffer, reflectionClipPlane));
        // attach the scene to the viewport to be rendered
        reflectionView.attachScene(reflectionScene);

        // create a pre-view. a view that is rendered before the main view
        refractionView = new ViewPort("Refraction View", refractionCam);
        refractionView.setClearFlags(true, true, true);
        refractionView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        refractionBuffer = new FrameBuffer(renderWidth, renderHeight, 1);
        //setup framebuffer to use texture
        refractionBuffer.setDepthBuffer(Format.Depth);
        refractionBuffer.setColorTexture(refractionTexture);
        refractionBuffer.setDepthTexture(depthTexture);
        //set viewport to render to offscreen framebuffer
        refractionView.setOutputFrameBuffer(refractionBuffer);

        //refractionView.addProcessor(new RefractionProcessor());
        // attach the scene to the viewport to be rendered
        refractionView.attachScene(reflectionScene);
    }

    protected void destroyViews() {
        //  rm.removePreView(reflectionView);
        rm.removePreView(refractionView);
    }

    /**
     * Get the water material from this processor, apply this to your water quad.
     * @return
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the reflected scene, should not include the water quad!
     * Set before adding processor.
     * @param spat
     */
    public void setReflectionScene(Spatial spat) {
        reflectionScene = spat;
    }

    /**
     * returns the width of the reflection and refraction textures
     * @return
     */
    public int getRenderWidth() {
        return renderWidth;
    }

    /**
     * returns the height of the reflection and refraction textures
     * @return
     */
    public int getRenderHeight() {
        return renderHeight;
    }

    /**
     * Set the reflection Texture render size,
     * set before adding the processor!
     * @param width
     * @param height
     */
    public void setRenderSize(int width, int height) {
        renderWidth = width;
        renderHeight = height;
    }

    /**
     * returns the water plane
     * @return
     */
    public Plane getPlane() {
        return plane;
    }

    /**
     * Set the water plane for this processor.
     * @param plane
     */
    public void setPlane(Plane plane) {
        this.plane.setConstant(plane.getConstant());
        this.plane.setNormal(plane.getNormal());
        updateClipPlanes();
    }

    /**
     * Set the water plane using an origin (location) and a normal (reflection direction).
     * @param origin Set to 0,-6,0 if your water quad is at that location for correct reflection
     * @param normal Set to 0,1,0 (Vector3f.UNIT_Y) for normal planar water
     */
    public void setPlane(Vector3f origin, Vector3f normal) {
        this.plane.setOriginNormal(origin, normal);
        updateClipPlanes();
    }

    private void updateClipPlanes() {
        reflectionClipPlane = plane.clone();
        reflectionClipPlane.setConstant(reflectionClipPlane.getConstant() + reflectionClippingOffset);
        refractionClipPlane = plane.clone();
        refractionClipPlane.setConstant(refractionClipPlane.getConstant() + refractionClippingOffset);

    }

    /**
     * Set the light Position for the processor
     * @param position
     */
    //TODO maybe we should provide a convenient method to compute position from direction
    public void setLightPosition(Vector3f position) {
        material.setVector3("lightPos", position);
    }

    /**
     * Set the color that will be added to the refraction texture.
     * @param color
     */
    public void setWaterColor(ColorRGBA color) {
        material.setColor("waterColor", color);
    }

    /**
     * Higher values make the refraction texture shine through earlier.
     * Default is 4
     * @param depth
     */
    public void setWaterDepth(float depth) {
        waterDepth = depth;
        material.setFloat("waterDepth", depth);
    }

    /**
     * return the water depth
     * @return
     */
    public float getWaterDepth() {
        return waterDepth;
    }

    /**
     * returns water transparency
     * @return
     */
    public float getWaterTransparency() {
        return waterTransparency;
    }

    /**
     * sets the water transparency default os 0.1f
     * @param waterTransparency
     */
    public void setWaterTransparency(float waterTransparency) {
        this.waterTransparency = Math.max(0, waterTransparency);
        material.setFloat("waterTransparency", waterTransparency / 10);
    }

    /**
     * Sets the speed of the wave animation, default = 0.05f.
     * @param speed
     */
    public void setWaveSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * returns the speed of the wave animation.
     * @return the speed
     */
    public float getWaveSpeed(){
        return speed;
    }
    
    /**
     * Sets the scale of distortion by the normal map, default = 0.2
     */
    public void setDistortionScale(float value) {
        distortionScale  = value;
        material.setFloat("distortionScale", distortionScale);
    }

    /**
     * Sets how the normal and dudv map are mixed to create the wave effect, default = 0.5
     */
    public void setDistortionMix(float value) {
        distortionMix = value;
        material.setFloat("distortionMix", distortionMix);
    }

    /**
     * Sets the scale of the normal/dudv texture, default = 1.
     * Note that the waves should be scaled by the texture coordinates of the quad to avoid animation artifacts,
     * use mesh.scaleTextureCoordinates(Vector2f) for that.
     */
    public void setTexScale(float value) {
        texScale = value;
        material.setFloat("texScale", texScale);
    }

    /**
     * returns the scale of distortion by the normal map, default = 0.2
     *
     * @return the distortion scale
     */
    public float getDistortionScale() {
        return distortionScale;
    }

    /**
     * returns how the normal and dudv map are mixed to create the wave effect,
     * default = 0.5
     *
     * @return the distortion mix
     */
    public float getDistortionMix() {
        return distortionMix;
    }

    /**
     * returns the scale of the normal/dudv texture, default = 1. Note that the
     * waves should be scaled by the texture coordinates of the quad to avoid
     * animation artifacts, use mesh.scaleTextureCoordinates(Vector2f) for that.
     *
     * @return the textures scale
     */
    public float getTexScale() {
        return texScale;
    }


    /**
     * retruns true if the waterprocessor is in debug mode
     * @return
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * set to true to display reflection and refraction textures in the GUI for debug purpose
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Creates a quad with the water material applied to it.
     * @param width
     * @param height
     * @return
     */
    public Geometry createWaterGeometry(float width, float height) {
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry("WaterGeometry", quad);
        geom.setLocalRotation(new Quaternion().fromAngleAxis(-HALF_PI, Vector3f.UNIT_X));
        geom.setMaterial(material);
        return geom;
    }

    /**
     * returns the reflection clipping plane offset
     * @return
     */
    public float getReflectionClippingOffset() {
        return reflectionClippingOffset;
    }

    /**
     * sets the reflection clipping plane offset
     * set a nagetive value to lower the clipping plane for relection texture rendering.
     * @param reflectionClippingOffset
     */
    public void setReflectionClippingOffset(float reflectionClippingOffset) {
        this.reflectionClippingOffset = reflectionClippingOffset;
        updateClipPlanes();
    }

    /**
     * returns the refraction clipping plane offset
     * @return
     */
    public float getRefractionClippingOffset() {
        return refractionClippingOffset;
    }

    /**
     * Sets the refraction clipping plane offset
     * set a positive value to raise the clipping plane for refraction texture rendering
     * @param refractionClippingOffset
     */
    public void setRefractionClippingOffset(float refractionClippingOffset) {
        this.refractionClippingOffset = refractionClippingOffset;
        updateClipPlanes();
    }

    /**
     * Refraction Processor
     */
    public class RefractionProcessor implements SceneProcessor {

        RenderManager rm;
        ViewPort vp;

        public void initialize(RenderManager rm, ViewPort vp) {
            this.rm = rm;
            this.vp = vp;
        }

        public void reshape(ViewPort vp, int w, int h) {
            System.out.println();
        }

        public boolean isInitialized() {
            return rm != null;
        }

        public void preFrame(float tpf) {
            refractionCam.setClipPlane(refractionClipPlane, Plane.Side.Negative);//,-1

        }

        public void postQueue(RenderQueue rq) {
        }

        public void postFrame(FrameBuffer out) {
        }

        public void cleanup() {
        }
    }


    public static void updateReflectionCam(Camera reflectionCam, Plane plane, Camera sceneCam){

        TempVars vars = TempVars.get();
        //Temp vects for reflection cam orientation calculation
        Vector3f sceneTarget =  vars.vect1;
        Vector3f  reflectDirection =  vars.vect2;
        Vector3f  reflectUp =  vars.vect3;
        Vector3f  reflectLeft = vars.vect4;
        Vector3f  camLoc = vars.vect5;
        camLoc = plane.reflect(sceneCam.getLocation(), camLoc);
        //reflectionCam.setLocation(camLoc);

        reflectionCam.setFrustum(sceneCam.getFrustumNear(),
                sceneCam.getFrustumFar(),
                sceneCam.getFrustumLeft(),
                sceneCam.getFrustumRight(),
                sceneCam.getFrustumTop(),
                sceneCam.getFrustumBottom());
        reflectionCam.setParallelProjection(sceneCam.isParallelProjection());

        sceneTarget.set(sceneCam.getLocation()).addLocal(sceneCam.getDirection(vars.vect6));
        reflectDirection = plane.reflect(sceneTarget, reflectDirection);
        reflectDirection.subtractLocal(camLoc);

        sceneTarget.set(sceneCam.getLocation()).subtractLocal(sceneCam.getUp(vars.vect6));
        reflectUp = plane.reflect(sceneTarget, reflectUp);
        reflectUp.subtractLocal(camLoc);

        sceneTarget.set(sceneCam.getLocation()).addLocal(sceneCam.getLeft(vars.vect6));
        reflectLeft = plane.reflect(sceneTarget, reflectLeft);
        reflectLeft.subtractLocal(camLoc);

        //reflectionCam.setAxes(reflectLeft, reflectUp, reflectDirection);

        vars.release();
    }
}
