package org.sunflow.core;

import org.sunflow.image.Color;

/**
 * A shader represents a particular light-surface interaction.
 */
public interface Shader extends RenderObject {
    /**
     * Gets the radiance for a specified rendering state. When this method is
     * called, you can assume that a hit has been registered in the state and
     * that the hit surface information has been computed.
     * 
     * @param state current render state
     * @return color emitted or reflected by the shader
     */
    public Color getRadiance(ShadingState state);

    /**
     * Scatter a photon with the specied power. Incoming photon direction is
     * specified by the ray attached to the current render state. This method
     * can safely do nothing if photon scattering is not supported or relevant
     * for the shader type.
     * 
     * @param state current state
     * @param power power of the incoming photon.
     */
    public void scatterPhoton(ShadingState state, Color power);

    // EP : Added transparency management  
    /**
     * Returns <code>true</code> if this shader is fully opaque. 
     * This gives a quick way to find out if a shader needs further processing 
     * when hit by a shadow ray. 
     */
    public boolean isOpaque(); 
    
    /**
     * Returns how much light is blocked by this shader.  
     */
    public Color getOpacity(ShadingState state);    
    // EP : End of modification
}