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

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;

import java.io.File;

public class TwoCamerasWithHouse extends SimpleApplication {

    // set default for applets
    private static boolean useHttp = true;
    private Node characterNode;

    public static void main(String[] args) {
      
        TwoCamerasWithHouse app = new TwoCamerasWithHouse();
        app.setShowSettings(false);
        app.setSettings(new AppSettings(true));
        app.settings.setResolution(1280, 800);
        app.setDisplayStatView(false);
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(10);
        Node mainScene = new Node();
        cam.setLocation(new Vector3f(-27.0f, 1.0f, 75.0f));
        cam.setRotation(new Quaternion(0.03f, 0.9f, 0f, 0.4f));

        // load sky
        mainScene.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        
        File file = new File("wildhouse.zip");
        if (file.exists()) {
            useHttp = false;
        }
        // create the geometry and attach it
        // load the level from zip or http zip
        if (useHttp) {
            assetManager.registerLocator("https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/jmonkeyengine/wildhouse.zip", HttpZipLocator.class);
        } else {
            assetManager.registerLocator("wildhouse.zip", ZipLocator.class);
        }
        Spatial scene = assetManager.loadModel("main.scene");

        DirectionalLight sun = new DirectionalLight();
        Vector3f lightDir=new Vector3f(-0.37352666f, -0.50444174f, -0.7784704f);
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        scene.addLight(sun);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
           //add lightPos Geometry
        Sphere lite=new Sphere(8, 8, 3.0f);
        Geometry lightSphere=new Geometry("lightsphere", lite);
        lightSphere.setMaterial(mat);
        Vector3f lightPos=lightDir.multLocal(-400);
        lightSphere.setLocalTranslation(lightPos);
        rootNode.attachChild(lightSphere);


        TwoCamerasProcessor twoCamerasProcessor = new TwoCamerasProcessor(assetManager);
        //SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        twoCamerasProcessor.setScene(mainScene);
        twoCamerasProcessor.setDebug(false);
        twoCamerasProcessor.setLightPosition(lightPos);
        twoCamerasProcessor.setRefractionClippingOffset(1.0f);


        //setting the water plane
        Vector3f waterLocation=new Vector3f(0,-20,0);
        twoCamerasProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
        ModfiedWaterUi waterUi = new ModfiedWaterUi(inputManager, twoCamerasProcessor);
        //WaterUI  waterUi=new WaterUI (inputManager, waterProcessor);
        twoCamerasProcessor.setWaterColor(ColorRGBA.Brown);
        twoCamerasProcessor.setDebug(true);
        //lower render size for higher performance
//        waterProcessor.setRenderSize(128,128);
        //raise depth to see through water
//        waterProcessor.setWaterDepth(20);
        //lower the distortion scale if the waves appear too strong
//        waterProcessor.setDistortionScale(0.1f);
        //lower the speed of the waves if they are too fast
//        waterProcessor.setWaveSpeed(0.01f);

        Quad quad = new Quad(400,400);

        //the texture coordinates define the general size of the waves
        quad.scaleTextureCoordinates(new Vector2f(6f,6f));

        Geometry water=new Geometry("water", quad);
        water.setShadowMode(ShadowMode.Receive);
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setMaterial(twoCamerasProcessor.getMaterial());
        water.setLocalTranslation(-200, -20, 250);


        rootNode.attachChild(water);

        viewPort.addProcessor(twoCamerasProcessor);

        mainScene.attachChild(scene);
        rootNode.attachChild(mainScene);


        //simbad
        characterNode = new Node("character node");
        characterNode.setLocalTranslation(0,-15,20);
        Spatial model = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        model.scale(0.25f);
        characterNode.attachChild(model);
        mainScene.attachChild(characterNode);


    }



    float speed = 10;
    float x = 0;
    float z = 20;
    float time = 0;

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf;

        characterNode.setLocalTranslation((float) (speed * Math.sin(time)), -15,  (float)(speed * Math.cos(time)));
    }
}
