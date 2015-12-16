package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.ShadingState;
import org.sunflow.core.Texture;
import org.sunflow.image.Color;

public class TexturedWardShader extends AnisotropicWardShader {
    private Texture tex;

    public TexturedWardShader() {
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
    public boolean isOpaque() {
        return !(tex.isTransparent());
    }
    
    @Override
    public Color getOpacity(ShadingState state) {
        return tex.getOpacity(state.getUV().x, state.getUV().y);
    }
    // EP : End of modification
}