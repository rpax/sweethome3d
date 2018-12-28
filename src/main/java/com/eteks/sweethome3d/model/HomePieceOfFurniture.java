/*
 * HomePieceOfFurniture.java 15 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A piece of furniture in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 */
public class HomePieceOfFurniture extends HomeObject implements PieceOfFurniture, Selectable, Elevatable {
  private static final long serialVersionUID = 1L;

  private static final double TWICE_PI = 2 * Math.PI;
  private static final double STRAIGHT_WALL_ANGLE_MARGIN  = Math.toRadians(1);
  private static final double ROUND_WALL_ANGLE_MARGIN     = Math.toRadians(10);

  /**
   * The properties of a piece of furniture that may change. <code>PropertyChangeListener</code>s added
   * to a piece of furniture will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {NAME, NAME_VISIBLE, NAME_X_OFFSET, NAME_Y_OFFSET, NAME_STYLE, NAME_ANGLE,
      DESCRIPTION, PRICE, VALUE_ADDED_TAX_PERCENTAGE, CURRENCY,
      WIDTH, WIDTH_IN_PLAN, DEPTH, DEPTH_IN_PLAN, HEIGHT, HEIGHT_IN_PLAN,
      COLOR, TEXTURE, MODEL_MATERIALS, SHININESS, VISIBLE, MODEL_TRANSFORMATIONS,
      X, Y, ELEVATION, ANGLE, PITCH, ROLL, MODEL_MIRRORED, MOVABLE, LEVEL};

  /**
   * The properties on which home furniture may be sorted.
   */
  public enum SortableProperty {CATALOG_ID, NAME, WIDTH, DEPTH, HEIGHT, MOVABLE,
                                DOOR_OR_WINDOW, COLOR, TEXTURE, VISIBLE, X, Y, ELEVATION, ANGLE, MODEL_SIZE, CREATOR,
                                PRICE, VALUE_ADDED_TAX, VALUE_ADDED_TAX_PERCENTAGE, PRICE_VALUE_ADDED_TAX_INCLUDED, LEVEL};
  private static final Map<SortableProperty, Comparator<HomePieceOfFurniture>> SORTABLE_PROPERTY_COMPARATORS;
  private static final float [][] IDENTITY = new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

  static {
    final Collator collator = Collator.getInstance();
    // Init piece property comparators
    SORTABLE_PROPERTY_COMPARATORS = new HashMap<SortableProperty, Comparator<HomePieceOfFurniture>>();
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.CATALOG_ID, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.catalogId == piece2.catalogId) {
            return 0;
          } else if (piece1.catalogId == null) {
            return -1;
          } else if (piece2.catalogId == null) {
            return 1;
          } else {
            return collator.compare(piece1.catalogId, piece2.catalogId);
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.NAME, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.name == piece2.name) {
            return 0;
          } else if (piece1.name == null) {
            return -1;
          } else if (piece2.name == null) {
            return 1;
          } else {
            return collator.compare(piece1.name, piece2.name);
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.WIDTH, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.width, piece2.width);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.HEIGHT, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.height, piece2.height);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.DEPTH, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.depth, piece2.depth);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.MOVABLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.movable, piece2.movable);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.DOOR_OR_WINDOW, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.doorOrWindow, piece2.doorOrWindow);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.COLOR, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.color == piece2.color) {
            return 0;
          } else if (piece1.color == null) {
            return -1;
          } else if (piece2.color == null) {
            return 1;
          } else {
            return piece1.color - piece2.color;
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.TEXTURE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.texture == piece2.texture) {
            return 0;
          } else if (piece1.texture == null) {
            return -1;
          } else if (piece2.texture == null) {
            return 1;
          } else {
            return collator.compare(piece1.texture.getName(), piece2.texture.getName());
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VISIBLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.visible, piece2.visible);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.X, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.x, piece2.x);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.Y, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.y, piece2.y);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.ELEVATION, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.elevation, piece2.elevation);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.ANGLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.angle, piece2.angle);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.MODEL_SIZE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          Long piece1ModelSize = HomePieceOfFurniture.getComparableModelSize(piece1);
          Long piece2ModelSize = HomePieceOfFurniture.getComparableModelSize(piece2);
          if (piece1ModelSize == piece2ModelSize) {
            return 0;
          } else if (piece1ModelSize == null) {
            return -1;
          } else if (piece2ModelSize == null) {
            return 1;
          } else {
            return piece1ModelSize < piece2ModelSize
                ? -1
                : (piece1ModelSize.longValue() == piece2ModelSize.longValue() ? 0 : 1);
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.CREATOR, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.creator == piece2.creator) {
            return 0;
          } else if (piece1.creator == null) {
            return -1;
          } else if (piece2.creator == null) {
            return 1;
          } else {
            return collator.compare(piece1.creator, piece2.creator);
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.LEVEL, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.getLevel(), piece2.getLevel());
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.PRICE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.price, piece2.price);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VALUE_ADDED_TAX_PERCENTAGE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.valueAddedTaxPercentage, piece2.valueAddedTaxPercentage);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VALUE_ADDED_TAX, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.getValueAddedTax(), piece2.getValueAddedTax());
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.getPriceValueAddedTaxIncluded(), piece2.getPriceValueAddedTaxIncluded());
        }
      });
  }

  private static int compare(float value1, float value2) {
    return Float.compare(value1, value2);
  }

  private static int compare(boolean value1, boolean value2) {
    return value1 == value2
               ? 0
               : (value1 ? -1 : 1);
  }

  private static int compare(BigDecimal value1, BigDecimal value2) {
    if (value1 == value2) {
      return 0;
    } else if (value1 == null) {
      return -1;
    } else if (value2 == null) {
      return 1;
    } else {
      return value1.compareTo(value2);
    }
  }

  private static int compare(Level level1, Level level2) {
    if (level1 == level2) {
      return 0;
    } else if (level1 == null) {
      return -1;
    } else if (level2 == null) {
      return 1;
    } else {
      return Float.compare(level1.getElevation(), level2.getElevation());
    }
  }

  private static Long getComparableModelSize(HomePieceOfFurniture piece) {
    if (piece instanceof HomeFurnitureGroup) {
      Long biggestModelSize = null;
      for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        Long modelSize = getComparableModelSize(childPiece);
        if (modelSize != null
            && (biggestModelSize == null
                || biggestModelSize.longValue() < modelSize.longValue())) {
          biggestModelSize = modelSize;
        }
      }
      return biggestModelSize;
    } else {
      return piece.modelSize;
    }
  }

  private String                 catalogId;
  private String                 name;
  private boolean                nameVisible;
  private float                  nameXOffset;
  private float                  nameYOffset;
  private TextStyle              nameStyle;
  private float                  nameAngle;
  private String                 description;
  private String                 information;
  private Content                icon;
  private Content                planIcon;
  private Content                model;
  private Long                   modelSize;
  private float                  width;
  private float                  widthInPlan;
  private float                  depth;
  private float                  depthInPlan;
  private float                  height;
  private float                  heightInPlan;
  private float                  elevation;
  private float                  dropOnTopElevation;
  private boolean                movable;
  private boolean                doorOrWindow;
  private HomeMaterial []        modelMaterials;
  private Integer                color;
  private HomeTexture            texture;
  private Float                  shininess;
  private float [][]             modelRotation;
  private boolean                modelCenteredAtOrigin;
  private Transformation []      modelTransformations;
  private String                 staircaseCutOutShape;
  private String                 creator;
  private boolean                backFaceShown;
  private boolean                resizable;
  private boolean                deformable;
  private boolean                texturable;
  private boolean                horizontallyRotatable;
  private BigDecimal             price;
  private BigDecimal             valueAddedTaxPercentage;
  private String                 currency;
  private boolean                visible;
  private float                  x;
  private float                  y;
  private float                  angle;
  private float                  pitch;
  private float                  roll;
  private boolean                modelMirrored;
  private Level                  level;

  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private transient Shape shapeCache;


  /**
   * Creates a home piece of furniture from an existing piece.
   * @param piece the piece from which data are copied
   */
  public HomePieceOfFurniture(PieceOfFurniture piece) {
    this.name = piece.getName();
    this.description = piece.getDescription();
    this.information = piece.getInformation();
    this.icon = piece.getIcon();
    this.planIcon = piece.getPlanIcon();
    this.model = piece.getModel();
    this.modelSize = piece.getModelSize();
    this.width = piece.getWidth();
    this.depth = piece.getDepth();
    this.height = piece.getHeight();
    this.elevation = piece.getElevation();
    this.dropOnTopElevation = piece.getDropOnTopElevation();
    this.movable = piece.isMovable();
    this.doorOrWindow = piece.isDoorOrWindow();
    this.color = piece.getColor();
    this.modelRotation = piece.getModelRotation();
    this.staircaseCutOutShape = piece.getStaircaseCutOutShape();
    this.creator = piece.getCreator();
    this.backFaceShown = piece.isBackFaceShown();
    this.resizable = piece.isResizable();
    this.deformable = piece.isDeformable();
    this.texturable = piece.isTexturable();
    this.horizontallyRotatable = piece.isHorizontallyRotatable();
    this.price = piece.getPrice();
    this.valueAddedTaxPercentage = piece.getValueAddedTaxPercentage();
    this.currency = piece.getCurrency();
    if (piece instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture homePiece = (HomePieceOfFurniture)piece;
      this.catalogId = homePiece.getCatalogId();
      this.nameVisible = homePiece.isNameVisible();
      this.nameXOffset = homePiece.getNameXOffset();
      this.nameYOffset = homePiece.getNameYOffset();
      this.nameAngle = homePiece.getNameAngle();
      this.nameStyle = homePiece.getNameStyle();
      this.visible = homePiece.isVisible();
      this.widthInPlan = homePiece.getWidthInPlan();
      this.depthInPlan = homePiece.getDepthInPlan();
      this.heightInPlan = homePiece.getHeightInPlan();
      this.modelCenteredAtOrigin = homePiece.isModelCenteredAtOrigin();
      this.modelTransformations = homePiece.getModelTransformations();
      this.angle = homePiece.getAngle();
      this.pitch = homePiece.getPitch();
      this.roll = homePiece.getRoll();
      this.x = homePiece.getX();
      this.y = homePiece.getY();
      this.modelMirrored = homePiece.isModelMirrored();
      this.texture = homePiece.getTexture();
      this.shininess = homePiece.getShininess();
      this.modelMaterials = homePiece.getModelMaterials();
    } else {
      if (piece instanceof CatalogPieceOfFurniture) {
        this.catalogId = ((CatalogPieceOfFurniture)piece).getId();
      }
      this.visible = true;
      this.widthInPlan = this.width;
      this.depthInPlan = this.depth;
      this.heightInPlan = this.height;
      this.modelCenteredAtOrigin = true; // false by default for version < 5.5
      this.x = this.width / 2;
      this.y = this.depth / 2;
    }
  }

  /**
   * Initializes new piece fields to their default values
   * and reads piece from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.dropOnTopElevation = 1f;
    this.modelRotation = IDENTITY;
    this.resizable = true;
    this.deformable = true;
    this.texturable = true;
    this.horizontallyRotatable = true;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.widthInPlan =
    this.depthInPlan =
    this.heightInPlan = Float.NEGATIVE_INFINITY;
    in.defaultReadObject();

    // Ensure angle is always positive and between 0 and 2 PI
    this.angle = (float)((this.angle % TWICE_PI + TWICE_PI) % TWICE_PI);
    // Update new fields used to store dimensions of a piece in plan after pitch or roll is applied to it
    if (this.widthInPlan == Float.NEGATIVE_INFINITY
        || this.depthInPlan == Float.NEGATIVE_INFINITY
        || this.heightInPlan == Float.NEGATIVE_INFINITY) {
      this.widthInPlan = this.width;
      this.depthInPlan = this.depth;
      this.heightInPlan = this.height;
      this.pitch = 0;
      this.roll = 0;
    }
    if (!this.modelCenteredAtOrigin) {
      // Keep default value to false only if model rotation matrix isn't identity
      this.modelCenteredAtOrigin = Arrays.deepEquals(IDENTITY, this.modelRotation);
    }
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this piece.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this piece.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the catalog ID of this piece of furniture or <code>null</code> if it doesn't exist.
   */
  public String getCatalogId() {
    return this.catalogId;
  }

  /**
   * Returns the name of this piece of furniture.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setName(String name) {
    if (name != this.name
        && (name == null || !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }

  /**
   * Returns whether the name of this piece should be drawn or not.
   */
  public boolean isNameVisible() {
    return this.nameVisible;
  }

  /**
   * Sets whether the name of this piece is visible or not. Once this piece of furniture
   * is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameVisible(boolean nameVisible) {
    if (nameVisible != this.nameVisible) {
      this.nameVisible = nameVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_VISIBLE.name(), !nameVisible, nameVisible);
    }
  }

  /**
   * Returns the distance along x axis applied to piece abscissa to display piece name.
   */
  public float getNameXOffset() {
    return this.nameXOffset;
  }

  /**
   * Sets the distance along x axis applied to piece abscissa to display piece name.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameXOffset(float nameXOffset) {
    if (nameXOffset != this.nameXOffset) {
      float oldNameXOffset = this.nameXOffset;
      this.nameXOffset = nameXOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_X_OFFSET.name(), oldNameXOffset, nameXOffset);
    }
  }

  /**
   * Returns the distance along y axis applied to piece ordinate
   * to display piece name.
   */
  public float getNameYOffset() {
    return this.nameYOffset;
  }

  /**
   * Sets the distance along y axis applied to piece ordinate to display piece name.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameYOffset(float nameYOffset) {
    if (nameYOffset != this.nameYOffset) {
      float oldNameYOffset = this.nameYOffset;
      this.nameYOffset = nameYOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_Y_OFFSET.name(), oldNameYOffset, nameYOffset);
    }
  }

  /**
   * Returns the text style used to display piece name.
   */
  public TextStyle getNameStyle() {
    return this.nameStyle;
  }

  /**
   * Sets the text style used to display piece name.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameStyle(TextStyle nameStyle) {
    if (nameStyle != this.nameStyle) {
      TextStyle oldNameStyle = this.nameStyle;
      this.nameStyle = nameStyle;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_STYLE.name(), oldNameStyle, nameStyle);
    }
  }

  /**
   * Returns the angle in radians used to display the piece name.
   * @since 3.6
   */
  public float getNameAngle() {
    return this.nameAngle;
  }

  /**
   * Sets the angle in radians used to display the piece name. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @since 3.6
   */
  public void setNameAngle(float nameAngle) {
    // Ensure angle is always positive and between 0 and 2 PI
    nameAngle = (float)((nameAngle % TWICE_PI + TWICE_PI) % TWICE_PI);
    if (nameAngle != this.nameAngle) {
      float oldNameAngle = this.nameAngle;
      this.nameAngle = nameAngle;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_ANGLE.name(), oldNameAngle, nameAngle);
    }
  }

  /**
   * Returns the description of this piece of furniture.
   * The returned value may be <code>null</code>.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setDescription(String description) {
    if (description != this.description
        && (description == null || !description.equals(this.description))) {
      String oldDescription = this.description;
      this.description = description;
      this.propertyChangeSupport.firePropertyChange(Property.DESCRIPTION.name(), oldDescription, description);
    }
  }

  /**
   * Returns the additional information associated to this piece, or <code>null</code>.
   * @since 4.2
   */
  public String getInformation() {
    return this.information;
  }

  /**
   * Returns the depth of this piece of furniture.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Sets the depth of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setDepth(float depth) {
    if (isResizable()) {
      if (depth != this.depth) {
        float oldDepth = this.depth;
        this.depth = depth;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, depth);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the depth of this piece of furniture in the horizontal plan (after pitch or roll is applied to it).
   * @since 5.5
   */
  public float getDepthInPlan() {
    return this.depthInPlan;
  }

  /**
   * Sets the depth of this piece of furniture in the horizontal plan (after pitch or roll is applied to it).
   * listeners added to this piece will receive a change notification.
   * @since 5.5
   */
  public void setDepthInPlan(float depthInPlan) {
    if (depthInPlan != this.depthInPlan) {
      float oldDepth = this.depthInPlan;
      this.depthInPlan = depthInPlan;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH_IN_PLAN.name(), oldDepth, depthInPlan);
    }
  }

  /**
   * Returns the height of this piece of furniture.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the height of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setHeight(float height) {
    if (isResizable()) {
      if (height != this.height) {
        float oldHeight = this.height;
        this.height = height;
        this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the height of this piece of furniture from the horizontal plan (after pitch or roll is applied to it).
   * @since 5.5
   */
  public float getHeightInPlan() {
    return this.heightInPlan;
  }

  /**
   * Sets the height of this piece of furniture from the horizontal plan (after pitch or roll is applied to it).
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 5.5
   */
  public void setHeightInPlan(float heightInPlan) {
    if (heightInPlan != this.heightInPlan) {
      float oldHeight = this.heightInPlan;
      this.heightInPlan = heightInPlan;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT_IN_PLAN.name(), oldHeight, heightInPlan);
    }
  }

  /**
   * Returns the width of this piece of furniture.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Sets the width of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setWidth(float width) {
    if (isResizable()) {
      if (width != this.width) {
        float oldWidth = this.width;
        this.width = width;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the width of this piece of furniture in the horizontal plan (after pitch or roll is applied to it).
   * @since 5.5
   */
  public float getWidthInPlan() {
    return this.widthInPlan;
  }

  /**
   * Sets the width of this piece of furniture in the horizontal plan (after pitch or roll is applied to it).
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 5.5
   */
  public void setWidthInPlan(float widthInPlan) {
    if (widthInPlan != this.widthInPlan) {
      float oldWidth = this.widthInPlan;
      this.widthInPlan = widthInPlan;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH_IN_PLAN.name(), oldWidth, widthInPlan);
    }
  }

  /**
   * Scales this piece of furniture with the given <code>scale</code>.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 5.5
   */
  public void scale(float scale) {
    setWidth(getWidth() * scale);
    setDepth(getDepth() * scale);
    setHeight(getHeight() * scale);
  }

  /**
   * Returns the elevation of the bottom of this piece of furniture on its level.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Returns the elevation at which should be placed an object dropped on this piece.
   * @return a percentage of the height of this piece. A negative value means that the piece
   *         should be ignored when an object is dropped on it.
   * @since 4.4
   */
  public float getDropOnTopElevation() {
    return this.dropOnTopElevation;
  }

  /**
   * Returns the elevation of the bottom of this piece of furniture
   * from the ground according to the elevation of its level.
   * @since 3.4
   */
  public float getGroundElevation() {
    if (this.level != null) {
      return this.elevation + this.level.getElevation();
    } else {
      return this.elevation;
    }
  }

  /**
   * Sets the elevation of this piece of furniture on its level. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Sets whether this piece is movable or not.
   * @since 3.0
   */
  public void setMovable(boolean movable) {
    if (movable != this.movable) {
      this.movable = movable;
      this.propertyChangeSupport.firePropertyChange(Property.MOVABLE.name(), !movable, movable);
    }
  }

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   * As this method existed before {@linkplain HomeDoorOrWindow HomeDoorOrWindow} class,
   * you shouldn't rely on the value returned by this method to guess if a piece
   * is an instance of <code>DoorOrWindow</code> class.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Returns the icon of this piece of furniture.
   */
  public Content getIcon() {
    return this.icon;
  }

  /**
   * Returns the icon of this piece of furniture displayed in plan or <code>null</code>.
   * @since 2.2
   */
  public Content getPlanIcon() {
    return this.planIcon;
  }

  /**
   * Returns the 3D model of this piece of furniture.
   */
  public Content getModel() {
    return this.model;
  }

  /**
   * Sets the size of the 3D model of this piece of furniture.
   * This method should be called only to update a piece created with an older version.
   * @since 5.5
   */
  public void setModelSize(Long modelSize) {
    this.modelSize = modelSize;
  }

  /**
   * Returns the size of the 3D model of this piece of furniture.
   * @since 5.5
   */
  public Long getModelSize() {
    return this.modelSize;
  }

  /**
   * Sets the materials of the 3D model of this piece of furniture.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param modelMaterials the materials of the 3D model or <code>null</code> if they shouldn't be changed
   * @throws IllegalStateException if this piece of furniture isn't texturable
   * @since 4.0
   */
  public void setModelMaterials(HomeMaterial [] modelMaterials) {
    if (isTexturable()) {
      if (!Arrays.equals(modelMaterials, this.modelMaterials)) {
        HomeMaterial [] oldModelMaterials = this.modelMaterials;
        this.modelMaterials = modelMaterials != null
            ? modelMaterials.clone()
            : null;
        this.propertyChangeSupport.firePropertyChange(Property.MODEL_MATERIALS.name(), oldModelMaterials, modelMaterials);
      }
    } else {
      throw new IllegalStateException("Piece isn't texturable");
    }
  }

  /**
   * Returns the materials applied to the 3D model of this piece of furniture.
   * @return the materials of the 3D model or <code>null</code>
   * if the individual materials of the 3D model are not modified.
   * @since 4.0
   */
  public HomeMaterial [] getModelMaterials() {
    if (this.modelMaterials != null) {
      return this.modelMaterials.clone();
    } else {
      return null;
    }
  }

  /**
   * Returns the color of this piece of furniture.
   * @return the color of the piece as RGB code or <code>null</code> if piece color is unchanged.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets the color of this piece of furniture.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param color the color of this piece of furniture or <code>null</code> if piece color is the default one
   * @throws IllegalStateException if this piece of furniture isn't texturable
   */
  public void setColor(Integer color) {
    if (isTexturable()) {
      if (color != this.color
          && (color == null || !color.equals(this.color))) {
        Integer oldColor = this.color;
        this.color = color;
        this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
      }
    } else {
      throw new IllegalStateException("Piece isn't texturable");
    }
  }

  /**
   * Returns the texture of this piece of furniture.
   * @return the texture of the piece or <code>null</code> if piece texture is unchanged.
   * @since 2.3
   */
  public HomeTexture getTexture() {
    return this.texture;
  }

  /**
   * Sets the texture of this piece of furniture.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param texture the texture of this piece of furniture or <code>null</code> if piece texture is the default one
   * @throws IllegalStateException if this piece of furniture isn't texturable
   * @since 2.3
   */
  public void setTexture(HomeTexture texture) {
    if (isTexturable()) {
      if (texture != this.texture
          && (texture == null || !texture.equals(this.texture))) {
        HomeTexture oldTexture = this.texture;
        this.texture = texture;
        this.propertyChangeSupport.firePropertyChange(Property.TEXTURE.name(), oldTexture, texture);
      }
    } else {
      throw new IllegalStateException("Piece isn't texturable");
    }
  }

  /**
   * Returns the shininess of this piece of furniture.
   * @return a value between 0 (matt) and 1 (very shiny) or <code>null</code> if piece shininess is unchanged.
   * @since 3.0
   */
  public Float getShininess() {
    return this.shininess;
  }

  /**
   * Sets the shininess of this piece of furniture or <code>null</code> if piece shininess is unchanged.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't texturable
   * @since 3.0
   */
  public void setShininess(Float shininess) {
    if (isTexturable()) {
      if (shininess != this.shininess
          && (shininess == null || !shininess.equals(this.shininess))) {
        Float oldShininess = this.shininess;
        this.shininess = shininess;
        this.propertyChangeSupport.firePropertyChange(Property.SHININESS.name(), oldShininess, shininess);
      }
    } else {
      throw new IllegalStateException("Piece isn't texturable");
    }
  }

  /**
   * Returns <code>true</code> if this piece is resizable.
   */
  public boolean isResizable() {
    return this.resizable;
  }

  /**
   * Returns <code>true</code> if this piece is deformable.
   * @since 3.0
   */
  public boolean isDeformable() {
    return this.deformable;
  }

  /**
   * Returns <code>true</code> if this piece is deformable.
   * @since 5.5
   */
  public boolean isWidthDepthDeformable() {
    return isDeformable();
  }

  /**
   * Returns <code>false</code> if this piece should always keep the same color or texture.
   * @since 3.0
   */
  public boolean isTexturable() {
    return this.texturable;
  }

  /**
   * Returns <code>false</code> if this piece should not rotate around an horizontal axis.
   * @since 5.5
   */
  public boolean isHorizontallyRotatable() {
    return this.horizontallyRotatable;
  }

  /**
   * Returns the price of this piece of furniture or <code>null</code>.
   */
  public BigDecimal getPrice() {
    return this.price;
  }

  /**
   * Sets the price of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @since 4.0
   */
  public void setPrice(BigDecimal price) {
    if (price != this.price
        && (price == null || !price.equals(this.price))) {
      BigDecimal oldPrice = this.price;
      this.price = price;
      this.propertyChangeSupport.firePropertyChange(Property.PRICE.name(), oldPrice, price);
    }
  }

  /**
   * Returns the Value Added Tax percentage applied to the price of this piece of furniture.
   */
  public BigDecimal getValueAddedTaxPercentage() {
    return this.valueAddedTaxPercentage;
  }

  /**
   * Sets the Value Added Tax percentage applied to prices.
   * @since 6.0
   */
  public void setValueAddedTaxPercentage(BigDecimal valueAddedTaxPercentage) {
    if (valueAddedTaxPercentage != this.valueAddedTaxPercentage
        && (valueAddedTaxPercentage == null || !valueAddedTaxPercentage.equals(this.valueAddedTaxPercentage))) {
      BigDecimal oldValueAddedTaxPercentage = this.valueAddedTaxPercentage;
      this.valueAddedTaxPercentage = valueAddedTaxPercentage;
      this.propertyChangeSupport.firePropertyChange(Property.VALUE_ADDED_TAX_PERCENTAGE.name(), oldValueAddedTaxPercentage, valueAddedTaxPercentage);

    }
  }

  /**
   * Returns the Value Added Tax applied to the price of this piece of furniture.
   */
  public BigDecimal getValueAddedTax() {
    if (this.price != null && this.valueAddedTaxPercentage != null) {
      return this.price.multiply(this.valueAddedTaxPercentage).
          setScale(this.price.scale(), RoundingMode.HALF_UP);
    } else {
      return null;
    }
  }

  /**
   * Returns the price of this piece of furniture, Value Added Tax included.
   */
  public BigDecimal getPriceValueAddedTaxIncluded() {
    if (this.price != null && this.valueAddedTaxPercentage != null) {
      return this.price.add(getValueAddedTax());
    } else {
      return this.price;
    }
  }

  /**
   * Returns the price currency, noted with ISO 4217 code, or <code>null</code>
   * if it has no price or default currency should be used.
   * @since 3.4
   */
  public String getCurrency() {
    return this.currency;
  }

  /**
   * Sets the price currency, noted with ISO 4217 code. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @since 6.0
   */
  public void setCurrency(String currency) {
    if (currency != this.currency
        && (currency == null || !currency.equals(this.currency))) {
      String oldCurrency = this.currency;
      this.currency = currency;
      this.propertyChangeSupport.firePropertyChange(Property.CURRENCY.name(), oldCurrency, currency);
    }
  }

  /**
   * Returns <code>true</code> if this piece of furniture is visible.
   */
  public boolean isVisible() {
    return this.visible;
  }

  /**
   * Sets whether this piece of furniture is visible or not. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), !visible, visible);
    }
  }

  /**
   * Returns the abscissa of the center of this piece of furniture.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abscissa of the center of this piece. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }

  /**
   * Returns the ordinate of the center of this piece of furniture.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of the center of this piece. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the angle in radians of this piece around vertical axis.
   */
  public float getAngle() {
    return this.angle;
  }

  /**
   * Sets the angle of this piece around vertical axis. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   */
  public void setAngle(float angle) {
    // Ensure angle is always positive and between 0 and 2 PI
    angle = (float)((angle % TWICE_PI + TWICE_PI) % TWICE_PI);
    if (angle != this.angle) {
      float oldAngle = this.angle;
      this.angle = angle;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE.name(), oldAngle, angle);
    }
  }

  /**
   * Returns the pitch angle in radians of this piece of furniture.
   * @since 5.5
   */
  public float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the pitch angle in radians of this piece and notifies listeners of this change.
   * Pitch axis is horizontal lateral (or transverse) axis.
   * @since 5.5
   */
  public void setPitch(float pitch) {
    if (isHorizontallyRotatable()) {
      // Ensure pitch is always positive and between 0 and 2 PI
      pitch = (float)((pitch % TWICE_PI + TWICE_PI) % TWICE_PI);
      if (pitch != this.pitch) {
        float oldPitch = this.pitch;
        this.pitch = pitch;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
      }
    } else {
      throw new IllegalStateException("Piece can't be rotated around an horizontal axis");
    }
  }

  /**
   * Returns the roll angle in radians of this piece of furniture.
   * @since 5.5
   */
  public float getRoll() {
    return this.roll;
  }

  /**
   * Sets the roll angle in radians of this piece and notifies listeners of this change.
   * Roll axis is horizontal longitudinal axis.
   * @since 5.5
   */
  public void setRoll(float roll) {
    if (isHorizontallyRotatable()) {
      // Ensure roll is always positive and between 0 and 2 PI
      roll = (float)((roll % TWICE_PI + TWICE_PI) % TWICE_PI);
      if (roll != this.roll) {
        float oldRoll = this.roll;
        this.roll = roll;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.ROLL.name(), oldRoll, roll);
      }
    } else {
      throw new IllegalStateException("Piece can't be rotated around an horizontal axis");
    }
  }

  /**
   * Returns <code>true</code> if the pitch or roll angle of this piece is different from 0.
   * @since 5.5
   */
  public boolean isHorizontallyRotated() {
    return this.roll != 0 || this.pitch != 0;
  }

  /**
   * Returns <code>true</code> if the model of this piece should be mirrored.
   */
  public boolean isModelMirrored() {
    return this.modelMirrored;
  }

  /**
   * Sets whether the model of this piece of furniture is mirrored or not. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setModelMirrored(boolean modelMirrored) {
    if (isResizable()) {
      if (modelMirrored != this.modelMirrored) {
        this.modelMirrored = modelMirrored;
        this.propertyChangeSupport.firePropertyChange(Property.MODEL_MIRRORED.name(),
            !modelMirrored, modelMirrored);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures
   * its model is correctly oriented.
   */
  public float [][] getModelRotation() {
    // Return a deep copy to avoid any misuse of piece data
    return new float [][] {{this.modelRotation[0][0], this.modelRotation[0][1], this.modelRotation[0][2]},
                           {this.modelRotation[1][0], this.modelRotation[1][1], this.modelRotation[1][2]},
                           {this.modelRotation[2][0], this.modelRotation[2][1], this.modelRotation[2][2]}};
  }

  /**
   * Sets whether model center should be always centered at the origin
   * when model rotation isn't <code>null</code>.
   * This method should be called only to keep unchanged the (wrong) location
   * of a rotated model created with version < 5.5.
   * @since 5.5
   */
  public void setModelCenteredAtOrigin(boolean modelCenteredAtOrigin) {
    this.modelCenteredAtOrigin = modelCenteredAtOrigin;
  }



  /**
   * Returns <code>true</code> if model center should be always centered at the origin
   * when model rotation isn't <code>null</code>.
   * @return <code>false</code> by default if version < 5.5
   * @since 5.5
   */
  public boolean isModelCenteredAtOrigin() {
    return this.modelCenteredAtOrigin;
  }

  /**
   * Sets the transformations applied to some parts of the 3D model of this piece of furniture.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param modelTransformations the transformations of the 3D model or <code>null</code> if no transformation shouldn't be applied
   * @since 6.0
   */
  public void setModelTransformations(Transformation [] modelTransformations) {
    if (!Arrays.equals(modelTransformations, this.modelTransformations)) {
      Transformation [] oldModelTransformations = this.modelTransformations;
      this.modelTransformations = modelTransformations != null && modelTransformations.length > 0
          ? modelTransformations.clone()
          : null;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_MATERIALS.name(), oldModelTransformations, modelTransformations);
     }
  }

  /**
   * Returns the transformations applied to the 3D model of this piece of furniture.
   * @return the transformations of the 3D model or <code>null</code>
   * if the 3D model is not transformed.
   * @since 6.0
   */
  public Transformation [] getModelTransformations() {
    if (this.modelTransformations != null) {
      return this.modelTransformations.clone();
    } else {
      return null;
    }
  }

  /**
   * Returns the shape used to cut out upper levels when they intersect with the piece
   * like a staircase.
   * @since 3.4
   */
  public String getStaircaseCutOutShape() {
    return this.staircaseCutOutShape;
  }

  /**
   * Returns the creator of this piece.
   * @since 4.2
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public boolean isBackFaceShown() {
    return this.backFaceShown;
  }

  /**
   * Returns the level which this piece belongs to.
   * @since 3.4
   */
  public Level getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this piece of furniture. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @since 3.4
   */
  public void setLevel(Level level) {
    if (level != this.level) {
      Level oldLevel = this.level;
      this.level = level;
      this.propertyChangeSupport.firePropertyChange(Property.LEVEL.name(), oldLevel, level);
    }
  }

  /**
   * Returns <code>true</code> if this piece is at the given <code>level</code>
   * or at a level with the same elevation and a smaller elevation index
   * or if the elevation of its highest point is higher than <code>level</code> elevation.
   * @since 3.4
   */
  public boolean isAtLevel(Level level) {
    if (this.level == level) {
      return true;
    } else if (this.level != null && level != null) {
      float pieceLevelElevation = this.level.getElevation();
      float levelElevation = level.getElevation();
      return pieceLevelElevation == levelElevation
             && this.level.getElevationIndex() < level.getElevationIndex()
          || pieceLevelElevation < levelElevation
             && isTopAtLevel(level);
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the top of this piece is visible at the given level.
   */
  private boolean isTopAtLevel(Level level) {
    float topElevation = this.level.getElevation() + this.elevation + this.heightInPlan;
    if (this.staircaseCutOutShape != null) {
      // Consider the top of stair cases is at the given level if their elevation is higher or equal
      return topElevation >= level.getElevation();
    } else {
      return topElevation > level.getElevation();
    }
  }

  /**
   * Returns the points of each corner of a piece.
   * @return an array of the 4 (x,y) coordinates of the piece corners.
   */
  public float [][] getPoints() {
    float [][] piecePoints = new float[4][2];
    PathIterator it = getShape().getPathIterator(null);
    for (int i = 0; i < piecePoints.length; i++) {
      it.currentSegment(piecePoints [i]);
      it.next();
    }
    return piecePoints;
  }

  /**
   * Returns <code>true</code> if this piece intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0,
                                     float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }

  /**
   * Returns <code>true</code> if this piece contains
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    if (margin == 0) {
      return getShape().contains(x, y);
    } else {
      return getShape().intersects(x - margin, y - margin, 2 * margin, 2 * margin);
    }
  }

  /**
   * Returns <code>true</code> if one of the corner of this piece is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isPointAt(float x, float y, float margin) {
    for (float [] point : getPoints()) {
      if (Math.abs(x - point[0]) <= margin && Math.abs(y - point[1]) <= margin) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the top left point of this piece is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>,
   * and if that point is closer to top left point than to top right and bottom left points.
   */
  public boolean isTopLeftPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    double distanceSquareToTopLeftPoint = Point2D.distanceSq(x, y, points[0][0], points[0][1]);
    return distanceSquareToTopLeftPoint <= margin * margin
        && distanceSquareToTopLeftPoint < Point2D.distanceSq(x, y, points[1][0], points[1][1])
        && distanceSquareToTopLeftPoint < Point2D.distanceSq(x, y, points[3][0], points[3][1]);
  }

  /**
   * Returns <code>true</code> if the top right point of this piece is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>,
   * and if that point is closer to top right point than to top left and bottom right points.
   */
  public boolean isTopRightPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    double distanceSquareToTopRightPoint = Point2D.distanceSq(x, y, points[1][0], points[1][1]);
    return distanceSquareToTopRightPoint <= margin * margin
        && distanceSquareToTopRightPoint < Point2D.distanceSq(x, y, points[0][0], points[0][1])
        && distanceSquareToTopRightPoint < Point2D.distanceSq(x, y, points[2][0], points[2][1]);
  }

  /**
   * Returns <code>true</code> if the bottom left point of this piece is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>,
   * and if that point is closer to bottom left point than to top left and bottom right points.
   */
  public boolean isBottomLeftPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    double distanceSquareToBottomLeftPoint = Point2D.distanceSq(x, y, points[3][0], points[3][1]);
    return distanceSquareToBottomLeftPoint <= margin * margin
        && distanceSquareToBottomLeftPoint < Point2D.distanceSq(x, y, points[0][0], points[0][1])
        && distanceSquareToBottomLeftPoint < Point2D.distanceSq(x, y, points[2][0], points[2][1]);
  }

  /**
   * Returns <code>true</code> if the bottom right point of this piece is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>,
   * and if that point is closer to top left point than to top right and bottom left points.
   */
  public boolean isBottomRightPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    double distanceSquareToBottomRightPoint = Point2D.distanceSq(x, y, points[2][0], points[2][1]);
    return distanceSquareToBottomRightPoint <= margin * margin
        && distanceSquareToBottomRightPoint < Point2D.distanceSq(x, y, points[1][0], points[1][1])
        && distanceSquareToBottomRightPoint < Point2D.distanceSq(x, y, points[3][0], points[3][1]);
  }

  /**
   * Returns <code>true</code> if the center point at which is displayed the name
   * of this piece is equal to the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean isNameCenterPointAt(float x, float y, float margin) {
    return Math.abs(x - getX() - getNameXOffset()) <= margin
        && Math.abs(y - getY() - getNameYOffset()) <= margin;
  }

  /**
   * Returns <code>true</code> if the front side of this piece is parallel to the given <code>wall</code>
   * with a margin.
   * @since 5.5
   */
  public boolean isParallelToWall(Wall wall) {
    if (wall.getArcExtent() == null) {
      float deltaY = wall.getYEnd() - wall.getYStart();
      float deltaX = wall.getXEnd() - wall.getXStart();
      if (deltaX == 0 && deltaY == 0) {
        return false;
      } else {
        // Check parallelism with line joining wall ends
        double wallAngle = Math.atan2(deltaY, deltaX);
        double pieceWallAngle = Math.abs(wallAngle - getAngle()) % Math.PI;
        return pieceWallAngle <= STRAIGHT_WALL_ANGLE_MARGIN || (Math.PI - pieceWallAngle) <= STRAIGHT_WALL_ANGLE_MARGIN;
      }
    } else {
      // Tangent angle at piece center
      double tangentAngle = Math.PI / 2 + Math.atan2(
          wall.getYArcCircleCenter() - getY(), wall.getXArcCircleCenter() - getX());
      double pieceWallAngle = Math.abs(tangentAngle - getAngle()) % Math.PI;
      // Be more tolerant for angles along round walls
      return pieceWallAngle <= ROUND_WALL_ANGLE_MARGIN || (Math.PI - pieceWallAngle) <= ROUND_WALL_ANGLE_MARGIN;
    }
  }

  /**
   * Returns the shape matching this piece in the horizontal plan.
   */
  private Shape getShape() {
    if (this.shapeCache == null) {
      // Create the rectangle that matches piece bounds
      Rectangle2D pieceRectangle = new Rectangle2D.Float(
          getX() - getWidthInPlan() / 2,
          getY() - getDepthInPlan() / 2,
          getWidthInPlan(), getDepthInPlan());
      // Apply rotation to the rectangle
      AffineTransform rotation = AffineTransform.getRotateInstance(getAngle(), getX(), getY());
      PathIterator it = pieceRectangle.getPathIterator(rotation);
      GeneralPath pieceShape = new GeneralPath();
      pieceShape.append(it, false);
      // Cache shape
      this.shapeCache = pieceShape;
    }
    return this.shapeCache;
  }

  /**
   * Moves this piece of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setX(getX() + dx);
    setY(getY() + dy);
  }

  /**
   * Returns a clone of this piece.
   */
  @Override
  public HomePieceOfFurniture clone() {
    HomePieceOfFurniture clone = (HomePieceOfFurniture)super.clone();
    clone.propertyChangeSupport = new PropertyChangeSupport(clone);
    clone.level = null;
    return clone;
  }

  /**
   * Returns a comparator that compares furniture on a given <code>property</code> in ascending order.
   */
  public static Comparator<HomePieceOfFurniture> getFurnitureComparator(SortableProperty property) {
    return SORTABLE_PROPERTY_COMPARATORS.get(property);
  }
}
