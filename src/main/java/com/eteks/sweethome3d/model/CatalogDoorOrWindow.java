/*
 * CatalogDoorOrWindow.java 8 mars 2009
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
 * A door or a window of the catalog.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class CatalogDoorOrWindow extends CatalogPieceOfFurniture implements DoorOrWindow {
  private final float   wallThickness;
  private final float   wallDistance;
  private final boolean wallCutOutOnBothSides;
  private final boolean widthDepthDeformable;
  private final Sash [] sashes;
  private final String  cutOutShape;

  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code> 
   */
  public CatalogDoorOrWindow(String id, String name, String description, Content icon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, null, model, width, depth, height, elevation, movable,   
        wallThickness, wallDistance, sashes, modelRotation, creator, resizable, price, valueAddedTaxPercentage);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @since 2.2 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation, movable,   
        wallThickness, wallDistance, sashes,
        modelRotation, creator, resizable, true, true, price, valueAddedTaxPercentage);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @since 3.0 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, boolean deformable, boolean texturable,
                             BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation, movable,   
        wallThickness, wallDistance, sashes,
        modelRotation, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, null);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 3.4 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, boolean deformable, boolean texturable,
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, null, null, null, null, icon, planIcon, model, width, depth, height, elevation, movable, 
        wallThickness, wallDistance, sashes, 
        modelRotation, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 3.6 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes, 
                             float [][] modelRotation, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, movable, 
        null, wallThickness, wallDistance, sashes, 
        modelRotation, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param cutOutShape the shape used to cut out walls that intersect the new door or window
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 4.2 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             String cutOutShape, float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, 1f, movable, 
        cutOutShape, wallThickness, wallDistance, sashes,
        modelRotation, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new door or window is movable
   * @param cutOutShape the shape used to cut out walls that intersect the new door or window
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 4.4 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, float dropOnTopElevation, boolean movable, 
                             String cutOutShape, float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
        cutOutShape, wallThickness, wallDistance, sashes,
        modelRotation, false, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);  
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new door or window is movable
   * @param cutOutShape the shape used to cut out walls that intersect the new door or window
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 5.3 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, float dropOnTopElevation, boolean movable, 
                             String cutOutShape, float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, boolean backFaceShown, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
        cutOutShape, wallThickness, wallDistance, true, true, sashes,
        modelRotation, backFaceShown, null, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);  
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new door or window is movable
   * @param cutOutShape the shape used to cut out walls that intersect the new door or window
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param wallCutOutOnBothSides  if <code>true</code> the new door or window should cut out 
   *            the both sides of the walls it intersects
   * @param widthDepthDeformable if <code>false</code>, the width and depth of the new door or window may 
   *            not be changed independently from each other
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new light
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @since 5.5
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, float dropOnTopElevation, boolean movable, 
                             String cutOutShape, float wallThickness, float wallDistance, 
                             boolean wallCutOutOnBothSides, boolean widthDepthDeformable, Sash [] sashes,
                             float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
        cutOutShape, wallThickness, wallDistance, wallCutOutOnBothSides, widthDepthDeformable, sashes,
        modelRotation, backFaceShown, modelSize, creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency, 
        null);  
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param information additional information associated to the new door or window
   * @param tags tags associated to the new door or window
   * @param creationDate creation date of the new door or window in milliseconds since the epoch 
   * @param grade grade of the new door or window or <code>null</code>
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param dropOnTopElevation a percentage of the height at which should be placed 
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new door or window is movable
   * @param cutOutShape the shape used to cut out walls that intersect the new door or window
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param wallCutOutOnBothSides  if <code>true</code> the new door or window should cut out 
   *            the both sides of the walls it intersects
   * @param widthDepthDeformable if <code>false</code>, the width and depth of the new door or window may 
   *            not be changed independently from each other
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new light
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may 
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code> 
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @since 5.7
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             String information, String [] tags, Long creationDate, Float grade, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, float dropOnTopElevation, boolean movable, 
                             String cutOutShape, float wallThickness, float wallDistance, 
                             boolean wallCutOutOnBothSides, boolean widthDepthDeformable, Sash [] sashes,
                             float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator, 
                             boolean resizable, boolean deformable, boolean texturable, 
                             BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                             Map<String, String> properties) {
    super(id, name, description, information, tags, creationDate, grade, 
        icon, planIcon, model, width, depth, height, elevation, dropOnTopElevation, movable, 
        null, modelRotation, backFaceShown, modelSize, creator, resizable, deformable, texturable, false,
        price, valueAddedTaxPercentage, currency, properties);
    this.cutOutShape = cutOutShape;
    this.wallThickness = wallThickness;
    this.wallDistance = wallDistance;
    this.wallCutOutOnBothSides = wallCutOutOnBothSides;
    this.widthDepthDeformable = widthDepthDeformable;
    this.sashes = sashes;
  }
         
  /**
   * Creates a modifiable catalog door or window with all its values.
   * @param name  the name of the new door or window
   * @param icon content of the icon of the new door or window
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param color the color of the door or window as RGB code or <code>null</code> 
   *        if door or window color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param iconYaw the yaw angle used to create the door or window icon
   * @param proportional if <code>true</code>, size proportions will be kept
   */
  public CatalogDoorOrWindow(String name, Content icon, Content model, 
                             float width, float depth, float height,
                             float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes, 
                             Integer color, float [][] modelRotation, boolean backFaceShown, 
                             float iconYaw, boolean proportional) {
    this(name, icon, model, width, depth, height, elevation, movable,   
        wallThickness, wallDistance, sashes, color, modelRotation, backFaceShown, null, null, iconYaw, proportional);
  }

  /**
   * Creates a modifiable catalog door or window with all its values.
   * @param name  the name of the new door or window
   * @param icon content of the icon of the new door or window
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param color the color of the door or window as RGB code or <code>null</code> 
   *        if door or window color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param iconYaw the yaw angle used to create the door or window icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @since 5.5
   */
  public CatalogDoorOrWindow(String name, Content icon, Content model, 
                             float width, float depth, float height,
                             float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes, 
                             Integer color, float [][] modelRotation, boolean backFaceShown, Long modelSize, 
                             String creator, float iconYaw, boolean proportional) {
    super(name, icon, model, width, depth, height, elevation, movable, null,   
        color, modelRotation, backFaceShown, modelSize, creator, iconYaw, proportional);
    this.wallThickness = wallThickness;
    this.wallDistance = wallDistance;
    this.wallCutOutOnBothSides = true;
    this.widthDepthDeformable = true;
    this.sashes = sashes.length > 0
        ? sashes.clone()
        : sashes;
    this.cutOutShape = null;
  }

  /**
   * Returns the default thickness of the wall in which this door or window should be placed.
   * @return a value in percentage of the depth of the door or the window.
   */
  public float getWallThickness() {
    return this.wallThickness;
  }
  
  /**
   * Returns the default distance that should lie at the back side of this door or window.
   * @return a distance in percentage of the depth of the door or the window.
   */
  public float getWallDistance() {
    return this.wallDistance;
  }

  /**
   * Returns <code>true</code> if this door or window should cut out the both sides
   * of the walls it intersects, even if its front or back side are within the wall thickness. 
   * @since 5.5 
   */
  public boolean isWallCutOutOnBothSides() {
    return this.wallCutOutOnBothSides;
  }

  /**
   * Returns <code>false</code> if the width and depth of the new door or window may 
   * not be changed independently from each other.
   * @since 5.5
   */
  public boolean isWidthDepthDeformable() {
    return this.widthDepthDeformable;
  }
  
  /**
   * Returns a copy of the sashes attached to this door or window.
   * If no sash is defined an empty array is returned. 
   */
  public Sash [] getSashes() {
    if (this.sashes.length == 0) {
      return this.sashes;
    } else {
      return this.sashes.clone();
    }
  }
  
  /**
   * Returns the shape used to cut out walls that intersect this new door or window.
   */
  public String getCutOutShape() {
    return this.cutOutShape;
  }

  /**
   * Returns always <code>true</code>.
   */
  @Override
  public boolean isDoorOrWindow() {
    return true;
  }
  
  /**
   * Returns always <code>false</code>.
   */
  @Override
  public boolean isHorizontallyRotatable() {
    return false;
  }
}
