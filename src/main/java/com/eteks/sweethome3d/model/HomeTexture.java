/*
 * HomeTexture.java 5 oct. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * An image used as texture on home 3D objects.
 * @author Emmanuel Puybaret
 */
public class HomeTexture implements TextureImage, Serializable {
  private static final long serialVersionUID = 1L;

  private final String  catalogId;
  private final String  name;
  private final String  creator;
  private final Content image;
  private final float   width;
  private final float   height;
  private final float   xOffset;
  private final float   yOffset;
  private final float   angle;
  private float         scale;
  private final boolean leftToRightOriented;

  /**
   * Creates a home texture from an existing one.
   * @param texture the texture from which data are copied
   */
  public HomeTexture(TextureImage texture) {
    this(texture, 0);
  }

  /**
   * Creates a home texture from an existing one with customized angle and offset.
   * @param texture the texture from which data are copied
   * @param angle   the rotation angle applied to the texture
   * @since 4.4
   */
  public HomeTexture(TextureImage texture, float angle) {
    // Texture is left to right oriented when applied on objects seen from front
    // added to homes with a version 3.4 and higher
    this(texture, angle, true);
  }

  /**
   * Creates a home texture from an existing one with customized angle and offset.
   * @param texture the texture from which data are copied
   * @param angle   the rotation angle applied to the texture
   * @param leftToRightOriented orientation used on the texture when applied on objects seen from front
   * @since 5.3
   */
  public HomeTexture(TextureImage texture, float angle, boolean leftToRightOriented) {
    this(texture, angle, 1, leftToRightOriented);
  }

  /**
   * Creates a home texture from an existing one with customized angle and offset.
   * @param texture the texture from which data are copied
   * @param angle   the rotation angle applied to the texture
   * @param scale   the scale applied to the texture
   * @param leftToRightOriented orientation used on the texture when applied on objects seen from front
   * @since 5.5
   */
  public HomeTexture(TextureImage texture, float angle, float scale, boolean leftToRightOriented) {
    this(texture, 0, 0, angle, scale, leftToRightOriented);
  }

  /**
   * Creates a home texture from an existing one with customized angle and offset.
   * @param texture the texture from which data are copied
   * @param xOffset the offset applied to the texture along X axis in percentage of its width
   * @param yOffset the offset applied to the texture along Y axis in percentage of its height
   * @param angle   the rotation angle applied to the texture
   * @param scale   the scale applied to the texture
   * @param leftToRightOriented orientation used on the texture when applied on objects seen from front
   * @since 6.0
   */
  public HomeTexture(TextureImage texture, float xOffset, float yOffset, float angle, float scale, boolean leftToRightOriented) {
    this.name = texture.getName();
    this.creator = texture.getCreator();
    this.image = texture.getImage();
    this.width = texture.getWidth();
    this.height = texture.getHeight();
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.angle = angle;
    this.scale = scale;
    this.leftToRightOriented = leftToRightOriented;
    if (texture instanceof HomeTexture) {
      this.catalogId = ((HomeTexture)texture).getCatalogId();
    } else if (texture instanceof CatalogTexture) {
      this.catalogId = ((CatalogTexture)texture).getId();
    } else {
      this.catalogId = null;
    }
  }

  /**
   * Initializes new fields
   * and reads texture from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.scale = 1f;
    in.defaultReadObject();
  }

  /**
   * Returns the catalog ID of this texture or <code>null</code> if it doesn't exist.
   * @since 4.4
   */
  public String getCatalogId() {
    return this.catalogId;
  }

  /**
   * Returns the name of this texture.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the creator of this texture.
   * @since 5.5
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * Returns the content of the image used for this texture.
   */
  public Content getImage() {
    return this.image;
  }

  /**
   * Returns the width of the image in centimeters.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Returns the height of the image in centimeters.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Returns the offset applied to the texture along X axis in percentage of its width.
   * @since 6.0
   */
  public float getXOffset() {
    return this.xOffset;
  }

  /**
   * Returns the offset applied to the texture along Y axis in percentage of its height.
   * @since 6.0
   */
  public float getYOffset() {
    return this.yOffset;
  }

  /**
   * Returns the angle of rotation in radians applied to this texture.
   * @since 4.4
   */
  public float getAngle() {
    return this.angle;
  }

  /**
   * Returns the scale applied to this texture.
   * @since 5.5
   */
  public float getScale() {
    return this.scale;
  }

  /**
   * Returns <code>true</code> if the objects using this texture should take into account
   * the orientation of the texture.
   * @since 3.4
   */
  public boolean isLeftToRightOriented() {
    return this.leftToRightOriented;
  }

  /**
   * Returns <code>true</code> if the object in parameter is equal to this texture.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof HomeTexture) {
      HomeTexture texture = (HomeTexture)obj;
      return (texture.name == this.name
              || texture.name != null && texture.name.equals(this.name))
          && (texture.image == this.image
              || texture.image != null && texture.image.equals(this.image))
          && texture.width == this.width
          && texture.height == this.height
          && texture.xOffset == this.xOffset
          && texture.yOffset == this.yOffset
          && texture.leftToRightOriented == this.leftToRightOriented
          && texture.angle == this.angle
          && texture.scale == this.scale;
    } else {
      return false;
    }
  }

  /**
   * Returns a hash code for this texture.
   */
  @Override
  public int hashCode() {
    return (this.name != null  ? this.name.hashCode()   : 0)
        + (this.image != null  ? this.image.hashCode()  : 0)
        + Float.floatToIntBits(this.width)
        + Float.floatToIntBits(this.height)
        + Float.floatToIntBits(this.xOffset)
        + Float.floatToIntBits(this.yOffset)
        + Float.floatToIntBits(this.angle)
        + Float.floatToIntBits(this.scale);
  }
}
