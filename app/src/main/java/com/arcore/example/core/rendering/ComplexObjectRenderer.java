package com.arcore.example.core.rendering;

import android.content.Context;
import android.util.Log;

import com.arcore.example.arcoremanager.object.SimpleArCoreObjectDrawer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

/**
 * Renders an object loaded from an OBJ file in OpenGL. This takes into account
 * the material information that is contained in MTL files that are referred to
 * by the OBJ file, and splits the objects into multiple parts when necessary,
 * in order to be able to render it with OpenGL ES 2.0
 */
public class ComplexObjectRenderer {
    private static final String TAG = ComplexObjectRenderer.class.getSimpleName();

    /**
     * The list of ObjectRenderer instances that will render the individual
     * parts
     */
    private final List<ObjectRenderer> mMaterialGroupObjectRenderers;
    private String mParentDirectory = "";

    /**
     * Default constructor
     */
    public ComplexObjectRenderer() {
        this.mMaterialGroupObjectRenderers = new ArrayList<>();
    }

    /**
     * Creates and initializes OpenGL resources needed for rendering the model.
     *
     * @param context                Context for loading the shader and below-named model and
     *                               texture assets.
     * @param objAssetName           Name of the OBJ file containing the model geometry.
     * @param defaultTextureFileName Name of the PNG file containing the diffuse
     *                               texture map, to be used as a fallback when the asset does not have
     *                               an associated MTL file, or one of the textures referred to from
     *                               the MTL file cannot be found.
     * @throws IOException If an IO error occurs
     */
    public void createOnGlThread(Context context, String objAssetName, String defaultTextureFileName) throws IOException {

        // Read the obj file.
        InputStream objInputStream = context.getAssets().open(mParentDirectory + objAssetName);
        Obj obj = ObjReader.read(objInputStream);

        // Prepare the Obj so that its structure is suitable for
        // rendering with OpenGL:
        // 1. Triangulate it
        // 2. Make sure that texture coordinates are not ambiguous
        // 3. Make sure that normals are not ambiguous
        // 4. Convert it to single-indexed data
        Obj renderableObj = ObjUtils.convertToRenderable(obj);

        // When there are no material groups, then just render the object
        // using the default texture
        if (renderableObj.getNumMaterialGroups() == 0) {
            createRenderers(context, renderableObj, defaultTextureFileName);
        } else {
            // Otherwise, create one renderer for each material
            createMaterialBasedRenderers(context, renderableObj, defaultTextureFileName);
        }
    }

    public void setBlendMode(ObjectRenderer.BlendMode blendMode) {
        for (ObjectRenderer renderer : mMaterialGroupObjectRenderers) {
            renderer.setBlendMode(blendMode);
        }
    }

    /**
     * Creates the renderers for the given OBJ. If the given OBJ has less than
     * 65k vertices, a single renderer will be created. Otherwise, it will be
     * split into multiple parts, each having at most 65k vertices, and one
     * renderer for each part will be created.
     *
     * @param context         Context for loading the shader and below-named model and
     *                        texture assets.
     * @param obj             The OBJ
     * @param textureFileName The texture file name
     * @throws IOException If an IO error occurs
     */
    private void createRenderers(Context context, Obj obj, String textureFileName) throws IOException {
        if (obj.getNumVertices() <= 65000) {
            createRenderer(context, obj, textureFileName);
        } else {
            // If there are more than 65k vertices, then the object has to be
            // split into multiple parts, each having at most 65k vertices
            List<Obj> objParts = ObjSplitting.splitByMaxNumVertices(obj, 65000);
            for (int j = 0; j < objParts.size(); j++) {
                Obj objPart = objParts.get(j);
                createRenderer(context, objPart, textureFileName);
            }
        }
    }

    /**
     * Creates one renderer for each material group that is contained in the
     * given OBJ. This will read the MTL files that are associated with the
     * given OBJ and extract the texture file name from them (using the given
     * default as a fallback).
     *
     * @param context                Context for loading the shader and below-named model and
     *                               texture assets.
     * @param obj                    The OBJ
     * @param defaultTextureFileName The default texture file name
     * @throws IOException If an IO error occurs
     */
    private void createMaterialBasedRenderers(Context context, Obj obj, String defaultTextureFileName) throws IOException {

        // Read the MTL files that are referred to from the OBJ, and
        // extract all their MTL definitions
        List<String> mtlFileNames = obj.getMtlFileNames();
        List<Mtl> allMtls = new ArrayList<>();

        for (String mtlFileName : mtlFileNames) {
            InputStream mtlInputStream = context.getAssets().open(mParentDirectory + mtlFileName);
            List<Mtl> mtls = MtlReader.read(mtlInputStream);
            allMtls.addAll(mtls);
        }

        // Obtain the material groups from the OBJ, and create renderers for
        // each of them
        Map<String, Obj> materialGroupObjs = ObjSplitting.splitByMaterialGroups(obj);
        for (Map.Entry<String, Obj> entry : materialGroupObjs.entrySet()) {
            String materialName = entry.getKey();
            String textureFileName = findTextureFileName(materialName, allMtls, defaultTextureFileName);
            Obj materialGroupObj = entry.getValue();
            createRenderers(context, materialGroupObj, textureFileName);
        }
    }

    /**
     * Returns the texture file name (the "map_kd" property) of the MTL that has
     * the given name, or the default texture file name if no such MTL is found
     *
     * @param materialName           The material name
     * @param mtls                   The sequence of all available MTLs
     * @param defaultTextureFileName The default texture file name
     * @return The texture file name
     */
    private String findTextureFileName(String materialName, Iterable<? extends Mtl> mtls, String defaultTextureFileName) {
        for (Mtl mtl : mtls) {
            if (Objects.equals(materialName, mtl.getName())) {
                return mtl.getMapKd();
            }
        }
        return defaultTextureFileName;
    }

    /**
     * Create one ObjectRenderer for the given OBJ
     *
     * @param context         Context for loading the shader and below-named model and
     *                        texture assets.
     * @param obj             The OBJ
     * @param textureFileName The texture file name
     * @throws IOException If an IO error occurs
     */
    private void createRenderer(Context context, Obj obj, String textureFileName) throws IOException {

        Log.i(TAG, "Rendering part with " + obj.getNumVertices() + " vertices and " + textureFileName);

        ObjectRenderer objectRenderer = new ObjectRenderer();
        objectRenderer.setParentDirectory(mParentDirectory);
        objectRenderer.createOnGlThread(context, obj, textureFileName);
        mMaterialGroupObjectRenderers.add(objectRenderer);
    }

    /**
     * Draws the model.
     *
     * @param cameraView        A 4x4 view matrix, in column-major order.
     * @param cameraPerspective A 4x4 projection matrix, in column-major order.
     * @param lightIntensity    Illumination intensity. Combined with diffuse and
     *                          specular material properties.
     */
    public void draw(float[] cameraView, float[] cameraPerspective, float lightIntensity) {
        for (ObjectRenderer renderer : mMaterialGroupObjectRenderers) {
            renderer.draw(cameraView, cameraPerspective, lightIntensity);
        }
    }

    public void updateModelMatrix(float[] anchorMatrix, float scale, float rotationY, float rotationZ, float translationX, float translationZ) {
        for (ObjectRenderer renderer : mMaterialGroupObjectRenderers) {
            renderer.updateModelMatrix(anchorMatrix, scale, rotationY, rotationZ, translationX, translationZ);
        }
    }

    public void setMaterialProperties(float ambient, float diffuse, float specular, float specularPower) {
        for (ObjectRenderer renderer : mMaterialGroupObjectRenderers) {
            renderer.setMaterialProperties(ambient, diffuse, specular, specularPower);
        }
    }

    public void setParentDirectory(String parentDir) {
        mParentDirectory = parentDir;
    }
}