package org.sunflow.core.shader;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;

public class ViewGlobalPhotonsShader implements Shader {
    public boolean update(ParameterList pl, SunflowAPI api) {
        return true;
    }

    public Color getRadiance(ShadingState state) {
        state.faceforward();
        return state.getGlobalRadiance();
    }

    public void scatterPhoton(ShadingState state, Color power) {
    }

    // EP : Added transparency management  
    public boolean isOpaque() {
        return true;
    }
    
    public Color getOpacity(ShadingState state) {
        return null;
    }
    // EP : End of modification
}