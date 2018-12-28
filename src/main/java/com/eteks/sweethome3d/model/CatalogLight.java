/*
 * Light.java 12 mars 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.math.BigDecimal;
import java.util.Map;

/**
 * A light of the catalog.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class CatalogLight extends CatalogPieceOfFurniture implements Light {
  private final LightSource [] lightSources;

  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param icon content of the icon of the new light
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   */
  public CatalogLight(String id, String name, String description, Content icon, Content model, 
                                 float width, float depth, float height, float elevation, boolean movable, 
                                 LightSource [] lightSources,
                                 float [][] modelRotation, String creator,
                                 boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, null, model, width, depth, height, elevation, movable,   
        lightSources, modelRotation, creator, resizable, price, valueAddedTaxPercentage);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @since 2.2            
   */
  public CatalogLight(String id, String name, String description, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, boolean movable, 
                      LightSource [] lightSources,
                      float [][] modelRotation, String creator,
                      boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation, movable,   
        lightSources, modelRotation, creator, resizable, true, true, price, valueAddedTaxPercentage);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @since 3.0            
   */
  public CatalogLight(String id, String name, String description, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, boolean movable, 
                      LightSource [] lightSources,
                      float [][] modelRotation, String creator,
                      boolean resizable, boolean deformable, boolean texturable,
                      BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation, movable,   
        lightSources, null, modelRotation, creator, resizable, true, true, price, valueAddedTaxPercentage, null);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 3.4           
   */
  public CatalogLight(String id, String name, String description, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, boolean movable, 
                      LightSource [] lightSources, String staircaseCutOutShape,
                      float [][] modelRotation, String creator,
                      boolean resizable, boolean deformable, boolean texturable,
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, null, null, null, null, icon, planIcon, model, width, depth, height, elevation, movable, 
        lightSources, staircaseCutOutShape, modelRotation, creator, resizable, deformable, texturable, 
        price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param information additional information associated to the new light
   * @param tags tags associated to the new light
   * @param creationDate creation date of the new light in milliseconds since the epoch 
   * @param grade grade of the new light or <code>null</code>
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 3.6          
   */
  public CatalogLight(String id, String name, String description, 
                      String information, String [] tags, Long creationDate, Float grade, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, boolean movable,
                      LightSource [] lightSources, String staircaseCutOutShape, 
                      float [][] modelRotation, String creator, 
                      boolean resizable, boolean deformable, boolean texturable, 
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, 1f, movable, lightSources, 
        staircaseCutOutShape, modelRotation, creator, resizable, deformable, texturable, 
        price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param information additional information associated to the new light
   * @param tags tags associated to the new light
   * @param creationDate creation date of the new light in milliseconds since the epoch 
   * @param grade grade of the new light or <code>null</code>
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 4.4          
   */
  public CatalogLight(String id, String name, String description, 
                      String information, String [] tags, Long creationDate, Float grade, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, float dropOnTopElevation, 
                      boolean movable, LightSource [] lightSources, String staircaseCutOutShape, 
                      float [][] modelRotation, String creator, 
                      boolean resizable, boolean deformable, boolean texturable, 
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, lightSources, 
        staircaseCutOutShape, modelRotation, false, creator, resizable, deformable, texturable, 
        price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param information additional information associated to the new light
   * @param tags tags associated to the new light
   * @param creationDate creation date of the new light in milliseconds since the epoch 
   * @param grade grade of the new light or <code>null</code>
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 5.3          
   */
  public CatalogLight(String id, String name, String description, 
                      String information, String [] tags, Long creationDate, Float grade, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, float dropOnTopElevation, 
                      boolean movable, LightSource [] lightSources, String staircaseCutOutShape, 
                      float [][] modelRotation, boolean backFaceShown, String creator, 
                      boolean resizable, boolean deformable, boolean texturable, 
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, lightSources, 
        staircaseCutOutShape, modelRotation, backFaceShown, null, creator, resizable, deformable, texturable, true,
        price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param information additional information associated to the new light
   * @param tags tags associated to the new light
   * @param creationDate creation date of the new light in milliseconds since the epoch 
   * @param grade grade of the new light or <code>null</code>
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new light
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece 
   *            should not rotate around an horizontal axis
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 5.5          
   */
  public CatalogLight(String id, String name, String description, 
                      String information, String [] tags, Long creationDate, Float grade, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, float dropOnTopElevation, 
                      boolean movable, LightSource [] lightSources, String staircaseCutOutShape, 
                      float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator, 
                      boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, lightSources, 
        staircaseCutOutShape, modelRotation, backFaceShown, modelSize, creator, resizable, deformable, texturable, horizontallyRotatable,
        price, valueAddedTaxPercentage, currency, null);
  }
  
  /**
   * Creates an unmodifiable catalog light of the default catalog.
   * @param id    the id of the new light, or <code>null</code>
   * @param name  the name of the new light
   * @param description the description of the new light 
   * @param information additional information associated to the new light
   * @param tags tags associated to the new light
   * @param creationDate creation date of the new light in milliseconds since the epoch 
   * @param grade grade of the new light or <code>null</code>
   * @param icon content of the icon of the new light
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new light
   * @param width  the width in centimeters of the new light
   * @param depth  the depth in centimeters of the new light
   * @param height  the height in centimeters of the new light
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param elevation  the elevation in centimeters of the new light
   * @param movable if <code>true</code>, the new light is movable
   * @param lightSources the light sources of the new light
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect 
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the light model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new light
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new light may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece 
   *            should not rotate around an horizontal axis
   * @param price the price of the new light, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new light or <code>null</code> 
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @since 5.7          
   */
  public CatalogLight(String id, String name, String description, 
                      String information, String [] tags, Long creationDate, Float grade, 
                      Content icon, Content planIcon, Content model, 
                      float width, float depth, float height, float elevation, float dropOnTopElevation, 
                      boolean movable, LightSource [] lightSources, String staircaseCutOutShape, 
                      float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator, 
                      boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                      BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                      Map<String, String> properties) {
    super(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
        staircaseCutOutShape, modelRotation, backFaceShown, modelSize, creator, 
        resizable, deformable, texturable, horizontallyRotatable, 
        price, valueAddedTaxPercentage, currency, properties);
    this.lightSources = lightSources.length > 0
        ? lightSources.clone()
        : lightSources;
  }
      
  /**
   * Returns the sources managed by this light. Each light source point
   * is a percentage of the width, the depth and the height of this light,
   * with the abscissa origin at the left side of the piece,
   * the ordinate origin at the front side of the piece
   * and the elevation origin at the bottom side of the piece.
   * @return a copy of light sources array.
   */
  public LightSource [] getLightSources() {
    if (this.lightSources.length == 0) {
      return this.lightSources;
    } else {
      return this.lightSources.clone();
    }
  }
}
