package com.etherblood.a.gui.soprettyboard;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.TranslucentBucketFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

public class ForestBoardAppstate extends AbstractAppState {

    private final Node rootNode;
    private final Vector3f lightDirection = new Vector3f(1, -5, -1).normalizeLocal();
    private final DirectionalLight directionalLight;
    private final AmbientLight ambientLight;
    private final TranslucentBucketFilter translucentBucketFilter;
    private WaterFilter waterFilter;
    private Spatial sky;
    private DirectionalLightShadowFilter shadowFilter;
    private TerrainQuad terrain;

    public ForestBoardAppstate(Node rootNode) {
        this.rootNode = rootNode;

        ambientLight = new AmbientLight(ColorRGBA.White.mult(0.4f));
        directionalLight = new DirectionalLight(lightDirection, ColorRGBA.White.mult(1.1f));
        translucentBucketFilter = new TranslucentBucketFilter();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        AssetManager assetManager = stateManager.getApplication().getAssetManager();
        initLightAndShadows(stateManager, assetManager);
        initSky("miramar", assetManager);
        initTerrain(assetManager);
        initWater(stateManager);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        cleanupWater(stateManager);
        cleanupTerrain();
        cleanupSky();
        cleanupLightAndShadows(stateManager);
    }

    private void cleanupLightAndShadows(AppStateManager stateManager) {
        stateManager.getState(PostFilterAppstate.class).removeFilter(shadowFilter);
        rootNode.removeLight(directionalLight);
        rootNode.removeLight(ambientLight);
    }

    private void cleanupSky() {
        rootNode.detachChild(sky);
    }

    private void cleanupTerrain() {
        rootNode.detachChild(terrain);
    }

    private void cleanupWater(AppStateManager stateManager) {
        stateManager.getState(PostFilterAppstate.class).removeFilter(waterFilter);
        stateManager.getState(PostFilterAppstate.class).removeFilter(translucentBucketFilter);
    }

    private void initLightAndShadows(AppStateManager stateManager, AssetManager assetManager) {
        rootNode.addLight(ambientLight);
        rootNode.addLight(directionalLight);

        if (shadowFilter == null) {
            shadowFilter = new DirectionalLightShadowFilter(assetManager, 2048, 2);
            shadowFilter.setLight(directionalLight);
            shadowFilter.setShadowIntensity(0.4f);
        }
        stateManager.getState(PostFilterAppstate.class).addFilter(shadowFilter);
    }

    private void initSky(String skyName, AssetManager assetManager) {
        if (sky == null) {
            Texture textureWest = assetManager.loadTexture("textures/skies/" + skyName + "/left.png");
            Texture textureEast = assetManager.loadTexture("textures/skies/" + skyName + "/right.png");
            Texture textureNorth = assetManager.loadTexture("textures/skies/" + skyName + "/front.png");
            Texture textureSouth = assetManager.loadTexture("textures/skies/" + skyName + "/back.png");
            Texture textureUp = assetManager.loadTexture("textures/skies/" + skyName + "/up.png");
            Texture textureDown = assetManager.loadTexture("textures/skies/" + skyName + "/down.png");
            sky = SkyFactory.createSky(assetManager, textureWest, textureEast, textureNorth, textureSouth, textureUp, textureDown);
        }
        rootNode.attachChild(sky);
    }

    private void initTerrain(AssetManager assetManager) {
        if (terrain == null) {
            Texture heightMapImage = assetManager.loadTexture("textures/boards/forest_height.png");
            AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
            heightmap.load();
            terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());

            Material material = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
            boolean triPlanarMapping = true;
            material.setBoolean("useTriPlanarMapping", true);
            material.setTexture("AlphaMap", assetManager.loadTexture("textures/boards/forest_alpha.png"));

            Texture grass = assetManager.loadTexture("textures/terrain/3dsa_fantasy_forest/green_grass.png");
            grass.setWrap(Texture.WrapMode.Repeat);
            material.setTexture("DiffuseMap", grass);
            material.setFloat("DiffuseMap_0_scale", getTextureScale(terrain, 16, triPlanarMapping));

            Texture dirt = assetManager.loadTexture("textures/terrain/3dsa_fantasy_forest/soil.png");
            dirt.setWrap(Texture.WrapMode.Repeat);
            material.setTexture("DiffuseMap_1", dirt);
            material.setFloat("DiffuseMap_1_scale", getTextureScale(terrain, 8, triPlanarMapping));

            Texture rock = assetManager.loadTexture("textures/terrain/3dsa_fantasy_forest/dry_leaves.png");
            rock.setWrap(Texture.WrapMode.Repeat);
            material.setTexture("DiffuseMap_2", rock);
            material.setFloat("DiffuseMap_2_scale", getTextureScale(terrain, 4, triPlanarMapping));

            terrain.setMaterial(material);
            terrain.setLocalTranslation(0, -0.8f, 0);
            terrain.setLocalScale(0.035f, 0.006f, 0.035f);
            terrain.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        }
        rootNode.attachChild(terrain);
    }

    private static float getTextureScale(TerrainQuad terrain, float scale, boolean triPlanarMapping) {
        return (triPlanarMapping ? (1 / (terrain.getTotalSize() / scale)) : scale);
    }

    private void initWater(AppStateManager stateManager) {
        if (waterFilter == null) {
            waterFilter = new WaterFilter(rootNode, lightDirection);
            waterFilter.setCenter(new Vector3f(0, 0, 1.4f));
            waterFilter.setWaterHeight(-0.45f);
            waterFilter.setShapeType(WaterFilter.AreaShape.Square);
            waterFilter.setRadius(9);
            waterFilter.setMaxAmplitude(0.15f);
            waterFilter.setFoamIntensity(0.2f);
            waterFilter.setFoamHardness(0.8f);
            waterFilter.setRefractionStrength(1.2f);
            waterFilter.setShininess(0.3f);
            waterFilter.setSpeed(0.2f);
            waterFilter.setUseRipples(false);
            waterFilter.setWaterTransparency(0.6f);
            waterFilter.setWaterColor(new ColorRGBA(0, 0.2f, 0.8f, 1));
            waterFilter.setColorExtinction(waterFilter.getColorExtinction().mult(0.07f));
            waterFilter.setShoreHardness(10);
            waterFilter.setUseHQShoreline(true);
        }
        stateManager.getState(PostFilterAppstate.class).addFilter(waterFilter);
        stateManager.getState(PostFilterAppstate.class).addFilter(translucentBucketFilter);
    }
}
