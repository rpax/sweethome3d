package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.ShadingState;
import org.sunflow.core.Texture;
import org.sunflow.image.Color;
import org.sunflow.math.Vector3;

public class TexturedShinyDiffuseShader extends ShinyDiffuseShader {
    private Texture tex;

    public TexturedShinyDiffuseShader() {
        tex = null;
    }

    @Override
    public boolean update(ParameterList pl, SunflowAPI api) {
        String filename = pl.getString("texture", null);
        if (filename != null)
            // EP : Made texture cache local to a SunFlow API instance
            tex = api.getTextureCache().getTexture(api.resolveTextureFilename(filename), false);
        return tex != null && super.update(pl, api);
    }

    @Override
    public Color getDiffuse(ShadingState state) {
        return tex.getPixel(state.getUV().x, state.getUV().y);
    }

    // EP : Added transparency management  
    @Override
    public Color getRadiance(ShadingState state) {
        float alpha;
        if (isOpaque() || (alpha = tex.getOpacityAlpha(state.getUV().x, state.getUV().y)) > 0.99999) {
            // Pixel is fully opaque
            return super.getRadiance(state);
        } else {
            // Ignore shininess for half transparent pixels
            state.faceforward();
            state.initLightSamples();
            state.initCausticSamples();
            Color c = state.diffuse(getDiffuse(state));
            Vector3 refrDir = state.getRay().getDirection();
            Color refraction = state.traceRefraction(new Ray(state.getPoint(), refrDir), 0);
            return c.mul(alpha).madd(1 - alpha, refraction);
        }
    }
    
    @Override
    public boolean isOpaque() {
        return !(tex.isTransparent());
    }
    
    @Override
    public Color getOpacity(ShadingState state) {
        return tex.getOpacity(state.getUV().x, state.getUV().y);
    }
    // EP : End of modification
}