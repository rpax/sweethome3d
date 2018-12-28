/*
 * HomeFurnitureController.java 30 mai 07
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home furniture view.
 * @author Emmanuel Puybaret
 */
public class HomeFurnitureController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {ICON, NAME, NAME_VISIBLE, DESCRIPTION, PRICE, VALUE_ADDED_TAX_PERCENTAGE,
      X, Y, ELEVATION, BASE_PLAN_ITEM,
      ANGLE, ANGLE_IN_DEGREES, ROLL, PITCH, HORIZONTAL_AXIS, WIDTH, DEPTH, HEIGHT, PROPORTIONAL,
      COLOR, PAINT, SHININESS, VISIBLE, MODEL_MIRRORED, MODEL_TRANSFORMATIONS, LIGHT_POWER,
      RESIZABLE, DEFORMABLE, TEXTURABLE}

  /**
   * The possible values for {@linkplain #getPaint() paint type}.
   */
  public enum FurniturePaint {DEFAULT, COLORED, TEXTURED, MODEL_MATERIALS}

  /**
   * The possible values for {@linkplain #getShininess() shininess type}.
   */
  public enum FurnitureShininess {DEFAULT, MATT, SHINY}

  /**
   * The possible values for {@linkplain #getHorizontalAxis() horizontal axis}.
   * @since 5.5
   */
  public enum FurnitureHorizontalAxis {ROLL, PITCH}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private TextureChoiceController     textureController;
  private ModelMaterialsController    modelMaterialsController;
  private DialogView                  homeFurnitureView;

  private Content            icon;
  private String             name;
  private String             description;
  private boolean            priceEditable;
  private BigDecimal         price;
  private boolean            valueAddedTaxPercentageEditable;
  private BigDecimal         valueAddedTaxPercentage;
  private Boolean            nameVisible;
  private Float              x;
  private Float              y;
  private Float              elevation;
  private Integer            angleInDegrees;
  private Float              angle;
  private boolean            rollAndPitchEditable;
  private Float              roll;
  private Float              pitch;
  private FurnitureHorizontalAxis horizontalAxis;
  private Float              width;
  private Float              proportionalWidth;
  private Float              depth;
  private Float              proportionalDepth;
  private Float              height;
  private Float              proportionalHeight;
  private boolean            proportional;
  private Transformation []  modelTransformations;
  private Integer            color;
  private FurniturePaint     paint;
  private FurnitureShininess shininess;
  private Boolean            visible;
  private Boolean            modelMirrored;
  private Boolean            basePlanItem;
  private boolean            basePlanItemEnabled;
  private boolean            lightPowerEditable;
  private Float              lightPower;
  private boolean            resizable;
  private boolean            deformable;
  private boolean            widthDepthDeformable;
  private boolean            texturable;
  private boolean            visibleEditable;

  private boolean            doorOrWindow;
  private float              wallThickness;
  private float              wallDistance;
  private float              wallWidth;
  private float              wallLeft;
  private float              wallHeight;
  private float              wallTop;
  private Sash []            sashes;

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home,
                                 UserPreferences preferences,
                                 ViewFactory viewFactory,
                                 UndoableEditSupport undoSupport) {
    this(home, preferences, viewFactory, null, undoSupport);
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home,
                                 UserPreferences preferences,
                                 ViewFactory viewFactory,
                                 ContentManager  contentManager,
                                 UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);

    updateProperties();
  }

  /**
   * Returns the texture controller of the piece.
   */
  public TextureChoiceController getTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.textureController == null
        && this.contentManager != null) {
      this.textureController = new TextureChoiceController(
          this.preferences.getLocalizedString(HomeFurnitureController.class, "textureTitle"),
          this.preferences, this.viewFactory, this.contentManager);
      this.textureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setPaint(FurniturePaint.TEXTURED);
            }
          });
    }
    return this.textureController;
  }

  /**
   * Returns the model materials controller of the piece.
   */
  public ModelMaterialsController getModelMaterialsController() {
    // Create sub controller lazily only once it's needed
    if (this.modelMaterialsController == null
        && this.contentManager != null) {
      this.modelMaterialsController = new ModelMaterialsController(
          this.preferences.getLocalizedString(HomeFurnitureController.class, "modelMaterialsTitle"),
          this.preferences, this.viewFactory, this.contentManager);
      this.modelMaterialsController.addPropertyChangeListener(ModelMaterialsController.Property.MATERIALS,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setPaint(FurniturePaint.MODEL_MATERIALS);
            }
          });

      PropertyChangeListener sizeChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // Update model content size in materials controller
            if (getWidth() != null && getDepth() != null && getHeight() != null) {
              modelMaterialsController.setModelSize(getWidth(), getDepth(), getHeight());
            }
          }
        };
      addPropertyChangeListener(Property.WIDTH, sizeChangeListener);
      addPropertyChangeListener(Property.DEPTH, sizeChangeListener);
      addPropertyChangeListener(Property.HEIGHT, sizeChangeListener);
      addPropertyChangeListener(Property.MODEL_TRANSFORMATIONS, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // Update model transformations in materials controller
            if (getModelTransformations() != null) {
              modelMaterialsController.setModelTransformations(getModelTransformations());
            }
          }
        });
    }
    return this.modelMaterialsController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.homeFurnitureView == null) {
      this.homeFurnitureView = this.viewFactory.createHomeFurnitureView(
          this.preferences, this);
    }
    return this.homeFurnitureView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Updates edited properties from selected furniture in the home edited by this controller.
   */
  protected void updateProperties() {
    List<HomePieceOfFurniture> selectedFurniture =
        Home.getFurnitureSubList(this.home.getSelectedItems());
    TextureChoiceController textureController = getTextureController();
    ModelMaterialsController modelMaterialsController = getModelMaterialsController();
    if (selectedFurniture.isEmpty()) {
      setIcon(null);
      setName(null); // Nothing to edit
      setNameVisible(null);
      setDescription(null);
      setPrice(null, false);
      this.priceEditable = false;
      setValueAddedTaxPercentage(null);
      this.valueAddedTaxPercentageEditable = false;
      setX(null);
      setY(null);
      setElevation(null);
      this.basePlanItemEnabled = false;
      setAngleInDegrees(null);
      setRoll(null);
      setPitch(null);
      setHorizontalAxis(null);
      this.rollAndPitchEditable = false;
      setWidth(null, true, false, false);
      setDepth(null, true, false, false);
      setHeight(null, true, false);
      setColor(null);
      if (textureController != null) {
        textureController.setTexture(null);
      }
      if (modelMaterialsController != null) {
        modelMaterialsController.setMaterials(null);
        modelMaterialsController.setModel(null);
        modelMaterialsController.setModelCreator(null);
      }
      setPaint(null);
      setModelTransformations(null);
      this.doorOrWindow = false;
      this.wallThickness = 1;
      this.wallDistance = 0;
      this.wallWidth = 1;
      this.wallLeft = 0;
      this.wallHeight = 1;
      this.wallTop = 0;
      this.sashes = new Sash [0];
      setShininess(null);
      this.visibleEditable = false;
      setVisible(null);
      setModelMirrored(null);
      this.lightPowerEditable = false;
      setLightPower(null);
      setResizable(true);
      setDeformable(true);
      setTexturable(true);
      setProportional(false);
    } else {
      // Search the common properties among selected furniture
      HomePieceOfFurniture firstPiece = selectedFurniture.get(0);
      Content icon = firstPiece.getIcon();
      if (icon != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!icon.equals(selectedFurniture.get(i).getIcon())) {
            icon = null;
            break;
          }
        }
      }
      setIcon(icon);

      String name = firstPiece.getName();
      if (name != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!name.equals(selectedFurniture.get(i).getName())) {
            name = null;
            break;
          }
        }
      }
      setName(name);

      Boolean nameVisible = firstPiece.isNameVisible();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (nameVisible != selectedFurniture.get(i).isNameVisible()) {
          nameVisible = null;
          break;
        }
      }
      setNameVisible(nameVisible);

      String description = firstPiece.getDescription();
      if (description != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!description.equals(selectedFurniture.get(i).getDescription())) {
            description = null;
            break;
          }
        }
      }
      setDescription(description);

      boolean priceEditable = this.preferences.getCurrency() != null;
      if (priceEditable) {
        for (int i = 0; i < selectedFurniture.size(); i++) {
          if (selectedFurniture.get(i) instanceof HomeFurnitureGroup) {
            priceEditable = false;
            break;
          }
        }
      }
      this.priceEditable = priceEditable;

      if (priceEditable) {
        BigDecimal price = firstPiece.getPrice();
        if (price != null) {
          for (int i = 1; i < selectedFurniture.size(); i++) {
            if (!price.equals(selectedFurniture.get(i).getPrice())) {
              price = null;
              break;
            }
          }
        }
        setPrice(price, false);

        this.valueAddedTaxPercentageEditable = this.preferences.isValueAddedTaxEnabled();
        BigDecimal valueAddedTaxPercentage = firstPiece.getValueAddedTaxPercentage();
        if (valueAddedTaxPercentage != null) {
          for (int i = 1; i < selectedFurniture.size(); i++) {
            if (!valueAddedTaxPercentage.equals(selectedFurniture.get(i).getValueAddedTaxPercentage())) {
              valueAddedTaxPercentage = null;
              break;
            }
          }
        }
        setValueAddedTaxPercentage(valueAddedTaxPercentage);
      } else {
        setPrice(null, false);
        setValueAddedTaxPercentage(null);
        this.valueAddedTaxPercentageEditable = false;
      }

      Float x = firstPiece.getX();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (x.floatValue() != selectedFurniture.get(i).getX()) {
          x = null;
          break;
        }
      }
      setX(x);

      Float y = firstPiece.getY();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (y.floatValue() != selectedFurniture.get(i).getY()) {
          y = null;
          break;
        }
      }
      setY(y);

      Float elevation = firstPiece.getElevation();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (elevation.floatValue() != selectedFurniture.get(i).getElevation()) {
          elevation = null;
          break;
        }
      }
      setElevation(elevation);

      boolean basePlanItemEnabled = !firstPiece.isDoorOrWindow();
      for (int i = 1; !basePlanItemEnabled && i < selectedFurniture.size(); i++) {
        if (!selectedFurniture.get(i).isDoorOrWindow()) {
          basePlanItemEnabled = true;
        }
      }
      this.basePlanItemEnabled = basePlanItemEnabled;

      Boolean basePlanItem = !firstPiece.isMovable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (basePlanItem.booleanValue() != !selectedFurniture.get(i).isMovable()) {
          basePlanItem = null;
          break;
        }
      }
      setBasePlanItem(basePlanItem);

      Float angle = firstPiece.getAngle();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (angle.floatValue() != selectedFurniture.get(i).getAngle()) {
          angle = null;
          break;
        }
      }
      setAngle(angle);

      boolean rollAndPitchEditable = true;
      for (int i = 0; rollAndPitchEditable && i < selectedFurniture.size(); i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        rollAndPitchEditable = piece.isHorizontallyRotatable()
            && piece.getStaircaseCutOutShape() == null;
      }
      this.rollAndPitchEditable = rollAndPitchEditable;

      if (this.rollAndPitchEditable) {
        Float roll = firstPiece.getRoll();
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (roll.floatValue() != selectedFurniture.get(i).getRoll()) {
            roll = null;
            break;
          }
        }
        setRoll(roll);

        Float pitch = firstPiece.getPitch();
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (pitch.floatValue() != selectedFurniture.get(i).getPitch()) {
            pitch = null;
            break;
          }
        }
        setPitch(pitch);

        if (roll == null && pitch == null
            || (roll != null && roll != 0 && pitch != null && pitch != 0)
            || (roll != null && roll == 0 && pitch != null && pitch == 0)) {
          setHorizontalAxis(null);
        } else if (roll == null && pitch != null && pitch == 0
                   || roll != null && roll != 0) {
          setHorizontalAxis(FurnitureHorizontalAxis.ROLL);
        } else {
          setHorizontalAxis(FurnitureHorizontalAxis.PITCH);
        }
      } else {
        setRoll(null);
        setPitch(null);
        setHorizontalAxis(null);
      }

      Float width = firstPiece.getWidth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (width.floatValue() != selectedFurniture.get(i).getWidth()) {
          width = null;
          break;
        }
      }
      setWidth(width, true, false, false);

      Float depth = firstPiece.getDepth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (depth.floatValue() != selectedFurniture.get(i).getDepth()) {
          depth = null;
          break;
        }
      }
      setDepth(depth, true, false, false);

      Float height = firstPiece.getHeight();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (height.floatValue() != selectedFurniture.get(i).getHeight()) {
          height = null;
          break;
        }
      }
      setHeight(height, true, false);

      List<HomePieceOfFurniture> selectedFurnitureWithoutGroups = getFurnitureWithoutGroups(selectedFurniture);
      HomePieceOfFurniture firstPieceExceptGroup = selectedFurnitureWithoutGroups.get(0);
      Integer color = firstPieceExceptGroup.getColor();
      if (color != null) {
        for (int i = 1; i < selectedFurnitureWithoutGroups.size(); i++) {
          if (!color.equals(selectedFurnitureWithoutGroups.get(i).getColor())) {
            color = null;
            break;
          }
        }
      }
      setColor(color);

      HomeTexture texture = firstPieceExceptGroup.getTexture();
      if (texture != null) {
        for (int i = 1; i < selectedFurnitureWithoutGroups.size(); i++) {
          if (!texture.equals(selectedFurnitureWithoutGroups.get(i).getTexture())) {
            texture = null;
            break;
          }
        }
      }
      if (textureController != null) {
        // Texture management available since version 2.3 only
        textureController.setTexture(texture);
      }

      HomeMaterial [] modelMaterials = firstPieceExceptGroup.getModelMaterials();
      Content model = firstPieceExceptGroup.getModel();
      String creator = firstPieceExceptGroup.getCreator();
      if (model != null) {
        for (int i = 1; i < selectedFurnitureWithoutGroups.size(); i++) {
          HomePieceOfFurniture piece = selectedFurnitureWithoutGroups.get(i);
          if (!Arrays.equals(modelMaterials, piece.getModelMaterials())
              || model != piece.getModel()) {
            modelMaterials = null;
            model = null;
            creator = null;
            break;
          }
        }
      }
      if (modelMaterialsController != null) {
        // Materials management available since version 4.0 only
        modelMaterialsController.setMaterials(modelMaterials);
        modelMaterialsController.setModel(model);
        modelMaterialsController.setModelCreator(creator);
        // Set a default size from the first piece before checking whether the selected pieces have the same size
        modelMaterialsController.setModelSize(firstPieceExceptGroup.getWidth(), firstPieceExceptGroup.getDepth(), firstPieceExceptGroup.getHeight());
        modelMaterialsController.setModelRotation(firstPieceExceptGroup.getModelRotation());
        modelMaterialsController.setModelTransformations(firstPieceExceptGroup.getModelTransformations());
        modelMaterialsController.setBackFaceShown(firstPieceExceptGroup.isBackFaceShown());
      }

      boolean defaultColorsAndTextures = true;
      for (int i = 0; i < selectedFurnitureWithoutGroups.size(); i++) {
        HomePieceOfFurniture piece = selectedFurnitureWithoutGroups.get(i);
        if (piece.getColor() != null
            || piece.getTexture() != null
            || piece.getModelMaterials() != null) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (color != null) {
        setPaint(FurniturePaint.COLORED);
      } else if (texture != null) {
        setPaint(FurniturePaint.TEXTURED);
      } else if (modelMaterials != null) {
        setPaint(FurniturePaint.MODEL_MATERIALS);
      } else if (defaultColorsAndTextures) {
        setPaint(FurniturePaint.DEFAULT);
      } else {
        setPaint(null);
      }

      // Allow transformation change only on one piece at a time
      Transformation [] modelTransformations = firstPiece.getModelTransformations();
      if (selectedFurniture.size() != 1) {
        modelTransformations = null;
      } else {
        if (modelTransformations == null) {
          modelTransformations = new Transformation [0];
        }
        if (firstPiece instanceof HomeDoorOrWindow) {
          HomeDoorOrWindow editedDoorOrWindow = (HomeDoorOrWindow)firstPiece;
          this.doorOrWindow = true;
          this.wallThickness = editedDoorOrWindow.getWallThickness();
          this.wallDistance = editedDoorOrWindow.getWallDistance();
          this.wallWidth = editedDoorOrWindow.getWallWidth();
          this.wallLeft = editedDoorOrWindow.getWallLeft();
          this.wallHeight = editedDoorOrWindow.getWallHeight();
          this.wallTop = editedDoorOrWindow.getWallTop();
          this.sashes = editedDoorOrWindow.getSashes();
        }
      }
      setModelTransformations(modelTransformations);

      Float firstPieceShininess = firstPieceExceptGroup.getShininess();
      FurnitureShininess shininess = firstPieceShininess == null
          ? FurnitureShininess.DEFAULT
          : (firstPieceShininess.floatValue() == 0
              ? FurnitureShininess.MATT
              : FurnitureShininess.SHINY);
      for (int i = 1; i < selectedFurnitureWithoutGroups.size(); i++) {
        HomePieceOfFurniture piece = selectedFurnitureWithoutGroups.get(i);
        if (firstPieceShininess != piece.getShininess()
            || (firstPieceShininess != null && !firstPieceShininess.equals(piece.getShininess()))) {
          shininess = null;
          break;
        }
      }
      setShininess(shininess);

      boolean visibleEditable = true;
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (!homeFurniture.contains(piece)) {
          visibleEditable = false;
          break;
        }
      }
      this.visibleEditable = visibleEditable;

      if (visibleEditable) {
        Boolean visible = firstPiece.isVisible();
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (visible != selectedFurniture.get(i).isVisible()) {
            visible = null;
            break;
          }
        }
        setVisible(visible);
      } else {
        setVisible(null);
      }

      Boolean modelMirrored = firstPiece.isModelMirrored();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (modelMirrored != selectedFurniture.get(i).isModelMirrored()) {
          modelMirrored = null;
          break;
        }
      }
      setModelMirrored(modelMirrored);

      boolean lightPowerEditable = firstPiece instanceof HomeLight;
      for (int i = 1; lightPowerEditable && i < selectedFurniture.size(); i++) {
        lightPowerEditable = selectedFurniture.get(i) instanceof HomeLight;
      }
      this.lightPowerEditable = lightPowerEditable;

      if (lightPowerEditable) {
        Float lightPower = ((HomeLight)firstPiece).getPower();
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (lightPower.floatValue() != ((HomeLight)selectedFurniture.get(i)).getPower()) {
            lightPower = null;
            break;
          }
        }
        setLightPower(lightPower);
      } else {
        setLightPower(null);
      }

      // Enable size components only if all pieces are resizable
      Boolean resizable = firstPiece.isResizable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (resizable.booleanValue() != selectedFurniture.get(i).isResizable()) {
          resizable = null;
          break;
        }
      }
      setResizable(resizable != null && resizable.booleanValue());

      boolean deformable = true;
      for (int i = 0; deformable && i < selectedFurniture.size(); i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (piece instanceof HomeFurnitureGroup) {
          for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
            if (!childPiece.isDeformable()
                || childPiece.isHorizontallyRotated()
                || childPiece.getModelTransformations() != null) {
              // Make selection not deformable if it contains a group with pieces
              // which are be deformable or rotated around an horizontal axis
              deformable = false;
              break;
            }
          }
        } else {
          deformable = piece.isDeformable()
              && piece.getModelTransformations() == null;
        }
      }
      setDeformable(deformable);
      if (!isDeformable()) {
        setProportional(true);
      }

      this.widthDepthDeformable = true;
      for (int i = 0; this.widthDepthDeformable && i < selectedFurniture.size(); i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        this.widthDepthDeformable = piece.isWidthDepthDeformable();
      }

      Boolean texturable = firstPiece.isTexturable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (texturable.booleanValue() != selectedFurniture.get(i).isTexturable()) {
          texturable = null;
          break;
        }
      }
      setTexturable(texturable == null || texturable.booleanValue());
    }
  }

  /**
   * Returns all the pieces of the given <code>furniture</code> list except groups.
   */
  private List<HomePieceOfFurniture> getFurnitureWithoutGroups(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> pieces = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        pieces.addAll(getFurnitureWithoutGroups(((HomeFurnitureGroup)piece).getFurniture()));
      } else {
        pieces.add(piece);
      }
    }
    return pieces;
  }

  /**
   * Returns <code>true</code> if the given <code>property</code> is editable.
   * Depending on whether a property is editable or not, the view associated to this controller
   * may render it differently.
   * The implementation of this method always returns <code>true</code> except for <code>DESCRIPTION</code> and <code>PRICE</code> properties.
   */
  public boolean isPropertyEditable(Property property) {
    switch (property) {
      case DESCRIPTION :
        return false;
      case PRICE :
        return isPriceEditable();
      case VALUE_ADDED_TAX_PERCENTAGE :
        return isValueAddedTaxPercentageEditable();
      case ROLL :
      case PITCH :
        return isRollAndPitchEditable();
      case MODEL_TRANSFORMATIONS :
        return getModelTransformations() != null;
      case LIGHT_POWER :
        return isLightPowerEditable();
      case VISIBLE :
        return this.visibleEditable;
      default :
        return true;
    }
  }

  /**
   * Sets the edited icon.
   */
  private void setIcon(Content icon) {
    if (icon != this.icon) {
      Content oldIcon = this.icon;
      this.icon = icon;
      this.propertyChangeSupport.firePropertyChange(Property.ICON.name(), oldIcon, icon);
    }
  }

  /**
   * Returns the edited icon.
   */
  public Content getIcon() {
    return this.icon;
  }

  /**
   * Sets the edited name.
   */
  public void setName(String name) {
    if (name != this.name) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }

  /**
   * Returns the edited name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets whether furniture name is visible or not.
   */
  public void setNameVisible(Boolean nameVisible) {
    if (nameVisible != this.nameVisible) {
      Boolean oldNameVisible = this.nameVisible;
      this.nameVisible = nameVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_VISIBLE.name(), oldNameVisible, nameVisible);
    }
  }

  /**
   * Returns whether furniture name should be drawn or not.
   */
  public Boolean getNameVisible() {
    return this.nameVisible;
  }

  /**
   * Sets the edited description.
   * @since 4.0
   */
  public void setDescription(String description) {
    if (description != this.description) {
      String oldDescription = this.description;
      this.description = description;
      this.propertyChangeSupport.firePropertyChange(Property.DESCRIPTION.name(), oldDescription, description);
    }
  }

  /**
   * Returns the edited description.
   * @since 4.0
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the edited price.
   * @since 4.0
   */
  public void setPrice(BigDecimal price) {
    setPrice(price, true);
  }

  private void setPrice(BigDecimal price, boolean updateCurrencyAndValueAddedTaxPercentage) {
    if (price != this.price
        && (price == null || !price.equals(this.price))) {
      BigDecimal oldPrice = this.price;
      this.price = price;
      this.propertyChangeSupport.firePropertyChange(Property.PRICE.name(), oldPrice, price);
      if (updateCurrencyAndValueAddedTaxPercentage) {
        if (price != null
            && isValueAddedTaxPercentageEditable()
            && getValueAddedTaxPercentage() == null
            && Home.getFurnitureSubList(this.home.getSelectedItems()).size() == 1) {
          setValueAddedTaxPercentage(this.preferences.getDefaultValueAddedTaxPercentage());
        }
      }
    }
  }

  /**
   * Returns the edited price.
   * @since 4.0
   */
  public BigDecimal getPrice() {
    return this.price;
  }

  /**
   * Returns whether the price can be edited or not.
   * @since 6.0
   */
  public boolean isPriceEditable() {
    return this.priceEditable;
  }

  /**
   * Sets the edited Value Added Tax percentage.
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
   * Returns edited Value Added Tax percentage.
   * @since 6.0
   */
  public BigDecimal getValueAddedTaxPercentage() {
    return this.valueAddedTaxPercentage;
  }

  /**
   * Returns whether the Value Added Tax percentage can be edited or not.
   * @since 6.0
   */
  public boolean isValueAddedTaxPercentageEditable() {
    return this.valueAddedTaxPercentageEditable;
  }

  /**
   * Sets the edited abscissa.
   */
  public void setX(Float x) {
    if (x != this.x) {
      Float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }

  /**
   * Returns the edited abscissa.
   */
  public Float getX() {
    return this.x;
  }

  /**
   * Sets the edited ordinate.
   */
  public void setY(Float y) {
    if (y != this.y) {
      Float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the edited ordinate.
   */
  public Float getY() {
    return this.y;
  }

  /**
   * Sets the edited elevation.
   */
  public void setElevation(Float elevation) {
    if (elevation != this.elevation) {
      Float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns the edited elevation.
   */
  public Float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the edited angle in degrees.
   */
  public void setAngleInDegrees(Integer angleInDegrees) {
    setAngleInDegrees(angleInDegrees, true);
  }

  private void setAngleInDegrees(Integer angleInDegrees, boolean updateAngle) {
    if (angleInDegrees != this.angleInDegrees) {
      Integer oldAngleInDegrees = this.angleInDegrees;
      this.angleInDegrees = angleInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE_IN_DEGREES.name(), oldAngleInDegrees, angleInDegrees);
      if (updateAngle) {
        if (this.angleInDegrees == null) {
          setAngle(null, false);
        } else {
          setAngle(new Float(Math.toRadians(this.angleInDegrees)), false);
        }
      }
    }
  }

  /**
   * Returns the edited angle in degrees.
   */
  public Integer getAngleInDegrees() {
    return this.angleInDegrees;
  }

  /**
   * Sets the edited angle in radians.
   * @since 3.6
   */
  public void setAngle(Float angle) {
    setAngle(angle, true);
  }

  public void setAngle(Float angle, boolean updateAngleInDegrees) {
    if (angle != this.angle) {
      Float oldAngle = this.angle;
      this.angle = angle;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE.name(), oldAngle, angle);
      if (updateAngleInDegrees) {
        if (angle == null) {
          setAngleInDegrees(null, false);
        } else {
          setAngleInDegrees((int)(Math.round(Math.toDegrees(angle)) + 360) % 360, false);
        }
      }
    }
  }

  /**
   * Returns the edited angle in radians.
   * @since 3.6
   */
  public Float getAngle() {
    return this.angle;
  }

  /**
   * Returns whether roll and pitch angles can be edited.
   * @since 5.5
   */
  public boolean isRollAndPitchEditable() {
    return this.rollAndPitchEditable;
  }

  /**
   * Sets the edited roll angle in radians.
   * @since 5.5
   */
  public void setRoll(Float roll) {
    if (roll != this.roll) {
      Float oldRoll = this.roll;
      this.roll = roll;
      this.propertyChangeSupport.firePropertyChange(Property.ROLL.name(), oldRoll, roll);
    }
  }

  /**
   * Returns the edited roll angle in radians.
   * @since 5.5
   */
  public Float getRoll() {
    return this.roll;
  }

  /**
   * Sets the edited pitch in radians.
   * @since 5.5
   */
  public void setPitch(Float pitch) {
    if (pitch != this.pitch) {
      Float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
  }

  /**
   * Returns the edited pitch in radians.
   * @since 5.5
   */
  public Float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the edited horizontal axis.
   * @since 5.5
   */
  public void setHorizontalAxis(FurnitureHorizontalAxis horizontalAxis) {
    if (horizontalAxis != this.horizontalAxis) {
      FurnitureHorizontalAxis oldAxis = this.horizontalAxis;
      this.horizontalAxis = horizontalAxis;
      this.propertyChangeSupport.firePropertyChange(Property.HORIZONTAL_AXIS.name(), oldAxis, horizontalAxis);
    }
  }

  /**
   * Returns the edited horizontal axis.
   * @since 5.5
   */
  public FurnitureHorizontalAxis getHorizontalAxis() {
    return this.horizontalAxis;
  }

  /**
   * Returns <code>true</code> if base plan item is an enabled property.
   * @since 4.0
   */
  public boolean isBasePlanItemEnabled() {
    return this.basePlanItemEnabled;
  }

  /**
   * Returns <code>true</code> if base plan item is an enabled property.
   * @deprecated the method is wrongly named and should be replaced by <code>isBasePlanItemEnabled</code>.
   */
  public boolean isBasePlanItemEditable() {
    return this.basePlanItemEnabled;
  }

  /**
   * Sets whether furniture is a base plan item or not.
   */
  public void setBasePlanItem(Boolean basePlanItem) {
    if (basePlanItem != this.basePlanItem) {
      Boolean oldMovable = this.basePlanItem;
      this.basePlanItem = basePlanItem;
      this.propertyChangeSupport.firePropertyChange(Property.BASE_PLAN_ITEM.name(), oldMovable, basePlanItem);
    }
  }

  /**
   * Returns whether furniture is a base plan item or not.
   */
  public Boolean getBasePlanItem() {
    return this.basePlanItem;
  }

  /**
   * Sets the edited width.
   */
  public void setWidth(Float width) {
    setWidth(width, false, isProportional() || !this.widthDepthDeformable, isProportional());
  }

  private void setWidth(Float width, boolean keepProportionalWidthUnchanged, boolean updateDepth, boolean updateHeight) {
    Float adjustedWidth = width != null
        ? Math.max(width, 0.001f)
        : null;
    if (adjustedWidth == width
        || adjustedWidth != null && adjustedWidth.equals(width)
        || !keepProportionalWidthUnchanged) {
      this.proportionalWidth = width;
    }
    if (adjustedWidth == null && this.width != null
        || adjustedWidth != null && !adjustedWidth.equals(this.width)) {
      Float oldWidth = this.width;
      this.width = adjustedWidth;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, adjustedWidth);
      if (oldWidth != null && adjustedWidth != null) {
        float ratio = adjustedWidth / oldWidth;
        if (updateDepth && this.proportionalDepth != null) {
          setDepth(this.proportionalDepth * ratio, true, false, false);
        }
        if (updateHeight && this.proportionalHeight != null) {
          setHeight(this.proportionalHeight * ratio, true, false);
        }
      } else {
        // If dimensions are proportional, nullify existing depth and height to ensure the width criterion will be respected
        if (updateDepth) {
          setDepth(null, false, false, false);
        }
        if (updateHeight) {
          setHeight(null, false, false);
        }
      }
    }
  }

  /**
   * Returns the edited width.
   */
  public Float getWidth() {
    return this.width;
  }

  /**
   * Sets the edited depth.
   */
  public void setDepth(Float depth) {
    setDepth(depth, false, isProportional() || !this.widthDepthDeformable, isProportional());
  }

  private void setDepth(Float depth, boolean keepProportionalDepthUnchanged, boolean updateWidth, boolean updateHeight) {
    Float adjustedDepth = depth != null
        ? Math.max(depth, 0.001f)
        : null;
    if (adjustedDepth == depth
        || adjustedDepth != null && adjustedDepth.equals(depth)
        || !keepProportionalDepthUnchanged) {
      this.proportionalDepth = depth;
    }
    if (adjustedDepth == null && this.depth != null
        || adjustedDepth != null && !adjustedDepth.equals(this.depth)) {
      Float oldDepth = this.depth;
      this.depth = adjustedDepth;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, adjustedDepth);
      if (oldDepth != null && adjustedDepth != null) {
        float ratio = adjustedDepth / oldDepth;
        if (updateWidth && this.proportionalWidth != null) {
          setWidth(this.proportionalWidth * ratio, true, false, false);
        }
        if (updateHeight && this.proportionalHeight != null) {
          setHeight(this.proportionalHeight * ratio, true, false);
        }
      } else {
        // If dimensions are proportional, nullify existing width and height to ensure the depth criterion will be respected
        if (updateWidth) {
          setWidth(null, false, false, false);
        }
        if (updateHeight) {
          setHeight(null, false, false);
        }
      }
    }
  }

  /**
   * Returns the edited depth.
   */
  public Float getDepth() {
    return this.depth;
  }

  /**
   * Sets the edited height.
   */
  public void setHeight(Float height) {
    setHeight(height, false, isProportional());
  }

  private void setHeight(Float height, boolean keepProportionalHeightUnchanged, boolean updateWidthAndDepth) {
    Float adjustedHeight = height != null
        ? Math.max(height, 0.001f)
        : null;
    if (adjustedHeight == height
        || adjustedHeight != null && adjustedHeight.equals(height)
        || !keepProportionalHeightUnchanged) {
      this.proportionalHeight = height;
    }
    if (adjustedHeight == null && this.height != null
        || adjustedHeight != null && !adjustedHeight.equals(this.height)) {
      Float oldHeight = this.height;
      this.height = adjustedHeight;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, adjustedHeight);
      if (updateWidthAndDepth) {
        if (oldHeight != null && adjustedHeight != null) {
          float ratio = adjustedHeight / oldHeight;
          if (this.proportionalWidth != null) {
            setWidth(this.proportionalWidth * ratio, true, false, false);
          }
          if (this.proportionalDepth != null) {
            setDepth(this.proportionalDepth * ratio, true, false, false);
          }
        } else {
          // If dimensions are proportional, nullify existing width and depth to ensure the height criterion will be respected
          setWidth(null, false, false, false);
          setDepth(null, false, false, false);
        }
      }
    }
  }

  /**
   * Returns the edited height.
   */
  public Float getHeight() {
    return this.height;
  }

  /**
   * Sets whether furniture proportions should be kept.
   */
  public void setProportional(boolean proportional) {
    if (proportional != this.proportional) {
      boolean oldProportional = this.proportional;
      this.proportional = proportional;
      this.propertyChangeSupport.firePropertyChange(Property.PROPORTIONAL.name(), oldProportional, proportional);
    }
  }

  /**
   * Returns whether furniture proportions should be kept or not.
   */
  public boolean isProportional() {
    return this.proportional;
  }

  /**
   * Sets the edited color.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the edited color.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets whether the piece is colored, textured, uses customized materials or unknown painted.
   */
  public void setPaint(FurniturePaint paint) {
    if (paint != this.paint) {
      FurniturePaint oldPaint = this.paint;
      this.paint = paint;
      this.propertyChangeSupport.firePropertyChange(Property.PAINT.name(), oldPaint, paint);
    }
  }

  /**
   * Returns whether the piece is colored, textured, uses customized materials or unknown painted.
   */
  public FurniturePaint getPaint() {
    return this.paint;
  }

  /**
   * Sets model transformations.
   * @since 6.0
   */
  public void setModelTransformations(Transformation [] modelTransformations) {
    if (!Arrays.equals(modelTransformations, this.modelTransformations)) {
      Transformation [] oldModelTransformations = this.modelTransformations;
      this.modelTransformations = modelTransformations;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_TRANSFORMATIONS.name(), oldModelTransformations, modelTransformations);
      setDeformable(modelTransformations == null || modelTransformations.length == 0);
      setProportional(modelTransformations != null && modelTransformations.length > 0);
    }
  }

  /**
   * Sets model transformations and updated dimensions of the edited piece.
   * @since 6.0
   */
  public void setModelTransformations(Transformation [] transformations,
                                      float x,
                                      float y,
                                      float elevation,
                                      float width,
                                      float depth,
                                      float height) {
    if (this.doorOrWindow) {
      float currentX = getX();
      float currentY = getY();
      float currentElevation = getElevation();
      float currentWidth = getWidth();
      float currentDepth = getDepth();
      float currentHeight = getHeight();
      float angle = -getAngle();

      float currentXAlongWidth = (float)(currentX * Math.cos(angle) - currentY * Math.sin(angle));
      float updatedXAlongWidth = (float)(x * Math.cos(angle) - y * Math.sin(angle));
      float currentWallLeft = this.wallLeft * currentWidth;
      // wallWidth and xWallLeft don't change
      float wallWidth = this.wallWidth * currentWidth;
      float xWallLeft = currentXAlongWidth - currentWidth / 2 + currentWallLeft;
      float newWallLeft = xWallLeft - updatedXAlongWidth + width / 2;

      float currentYAlongDepth = (float)(currentX * Math.sin(angle) + currentY * Math.cos(angle));
      float updatedYAlongDepth = (float)(x * Math.sin(angle) + y * Math.cos(angle));
      float currentWallDistance = this.wallDistance * currentDepth;
      // wallThickness and yWallBack don't change
      float wallThickness = this.wallThickness * currentDepth;
      float yWallBack = currentYAlongDepth - currentDepth / 2 + currentWallDistance;
      float newWallDistance = yWallBack - updatedYAlongDepth + depth / 2;

      float currentWallTop = this.wallTop * currentHeight;
      // elevation don't change
      float wallHeight = this.wallHeight * currentHeight;
      float newWallTop = currentWallTop + elevation + height - (currentElevation + currentHeight);

      Sash [] sashes = this.sashes;
      for (int i = 0; i < sashes.length; i++) {
        Sash sash = sashes [i];
        float xAxis = sash.getXAxis() * currentWidth;
        xAxis += newWallLeft - currentWallLeft;
        float yAxis = sash.getYAxis() * currentDepth;
        yAxis += newWallDistance - currentWallDistance;
        sashes [i] = new Sash(xAxis / width, yAxis / depth,
            sash.getWidth() * currentWidth / width, sash.getStartAngle(), sash.getEndAngle());
      }
      this.wallThickness = wallThickness / depth;
      this.wallDistance = newWallDistance / depth;
      this.wallWidth = wallWidth / width;
      this.wallLeft = newWallLeft / width;
      this.wallHeight = wallHeight / height;
      this.wallTop = newWallTop / height;
      this.sashes  = sashes;
    }
    setModelTransformations(transformations);
    setX(x);
    setY(y);
    setWidth(width, false, false, false);
    setDepth(depth, false, false, false);
    setHeight(height, false, false);
  }

  /**
   * Returns model transformations.
   * @since 6.0
   */
  public Transformation [] getModelTransformations() {
    return this.modelTransformations;
  }

  /**
   * Sets whether the piece shininess is the default one, matt, shiny or unknown.
   */
  public void setShininess(FurnitureShininess shininess) {
    if (shininess != this.shininess) {
      FurnitureShininess oldShininess = this.shininess;
      this.shininess = shininess;
      this.propertyChangeSupport.firePropertyChange(Property.SHININESS.name(), oldShininess, shininess);
    }
  }

  /**
   * Returns whether the piece is shininess is the default one, matt, shiny or unknown.
   */
  public FurnitureShininess getShininess() {
    return this.shininess;
  }

  /**
   * Sets whether furniture is visible or not.
   */
  public void setVisible(Boolean visible) {
    if (visible != this.visible) {
      Boolean oldVisible = this.visible;
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), oldVisible, visible);
    }
  }

  /**
   * Returns whether furniture is visible or not.
   */
  public Boolean getVisible() {
    return this.visible;
  }

  /**
   * Sets whether furniture model is mirrored or not.
   */
  public void setModelMirrored(Boolean modelMirrored) {
    if (modelMirrored != this.modelMirrored) {
      Boolean oldModelMirrored = this.modelMirrored;
      this.modelMirrored = modelMirrored;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_MIRRORED.name(), oldModelMirrored, modelMirrored);
    }
  }

  /**
   * Returns whether furniture model is mirrored or not.
   */
  public Boolean getModelMirrored() {
    return this.modelMirrored;
  }

  /**
   * Returns <code>true</code> if light power is an editable property.
   */
  public boolean isLightPowerEditable() {
    return this.lightPowerEditable;
  }

  /**
   * Returns the edited light power.
   */
  public Float getLightPower() {
    return this.lightPower;
  }

  /**
   * Sets the edited light power.
   */
  public void setLightPower(Float lightPower) {
    if (lightPower != this.lightPower) {
      Float oldLightPower = this.lightPower;
      this.lightPower = lightPower;
      this.propertyChangeSupport.firePropertyChange(Property.LIGHT_POWER.name(), oldLightPower, lightPower);
    }
  }

  /**
   * Sets whether furniture model can be resized or not.
   */
  private void setResizable(boolean resizable) {
    if (resizable != this.resizable) {
      boolean oldResizable = this.resizable;
      this.resizable = resizable;
      this.propertyChangeSupport.firePropertyChange(Property.RESIZABLE.name(), oldResizable, resizable);
    }
  }

  /**
   * Returns whether furniture model can be resized or not.
   */
  public boolean isResizable() {
    return this.resizable;
  }

  /**
   * Sets whether furniture model can be deformed or not.
   */
  private void setDeformable(boolean deformable) {
    if (deformable != this.deformable) {
      boolean oldDeformable = this.deformable;
      this.deformable = deformable;
      this.propertyChangeSupport.firePropertyChange(Property.DEFORMABLE.name(), oldDeformable, deformable);
    }
  }

  /**
   * Returns whether furniture model can be deformed or not.
   */
  public boolean isDeformable() {
    return this.deformable;
  }

  /**
   * Sets whether the color or the texture of the furniture model can be changed or not.
   */
  private void setTexturable(boolean texturable) {
    if (texturable != this.texturable) {
      boolean oldTexturable = this.texturable;
      this.texturable = texturable;
      this.propertyChangeSupport.firePropertyChange(Property.TEXTURABLE.name(), oldTexturable, texturable);
    }
  }

  /**
   * Returns whether the color or the texture of the furniture model can be changed or not.
   */
  public boolean isTexturable() {
    return this.texturable;
  }

  /**
   * Controls the modification of selected furniture in the edited home.
   */
  public void modifyFurniture() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<HomePieceOfFurniture> selectedFurniture = Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      String name = getName();
      Boolean nameVisible = getNameVisible();
      String description = getDescription();
      BigDecimal price = getPrice();
      boolean removePrice = selectedFurniture.size() == 1 && price == null;
      BigDecimal valueAddedTaxPercentage = getValueAddedTaxPercentage();
      boolean removeValueAddedTaxPercentage = selectedFurniture.size() == 1 && valueAddedTaxPercentage == null;
      String currency = this.preferences.getCurrency();
      Float x = getX();
      Float y = getY();
      Float elevation = getElevation();
      Float angle = getAngle();
      Float roll = getRoll();
      Float pitch = getPitch();
      FurnitureHorizontalAxis horizontalAxis = getHorizontalAxis();
      Boolean basePlanItem = getBasePlanItem();
      Float width = getWidth();
      Float depth = getDepth();
      Float height = getHeight();
      boolean proportional = isProportional()
          && (width == null || depth == null || height == null);
      FurniturePaint paint = getPaint();
      Integer color = paint == FurniturePaint.COLORED
          ? getColor()
          : null;
      TextureChoiceController textureController = getTextureController();
      HomeTexture texture;
      if (textureController != null && paint == FurniturePaint.TEXTURED) {
        texture = textureController.getTexture();
      } else {
        texture = null;
      }
      ModelMaterialsController modelMaterialsController = getModelMaterialsController();
      HomeMaterial [] modelMaterials;
      if (modelMaterialsController != null && paint == FurniturePaint.MODEL_MATERIALS) {
        modelMaterials = modelMaterialsController.getMaterials();
      } else {
        modelMaterials = null;
      }
      Transformation [] modelTransformations = getModelTransformations();
      boolean defaultShininess = getShininess() == FurnitureShininess.DEFAULT;
      Float shininess = getShininess() == FurnitureShininess.SHINY
          ? new Float(0.5f)
          : (getShininess() == FurnitureShininess.MATT
              ? new Float(0) : null);
      Boolean visible = getVisible();
      Boolean modelMirrored = getModelMirrored();
      Float lightPower = getLightPower();

      // Create an array of modified furniture with their current properties values
      ModifiedPieceOfFurniture [] modifiedFurniture =
          new ModifiedPieceOfFurniture [selectedFurniture.size()];
      for (int i = 0; i < modifiedFurniture.length; i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (piece instanceof HomeLight) {
          modifiedFurniture [i] = new ModifiedLight((HomeLight)piece);
        } else if (piece instanceof HomeDoorOrWindow) {
          modifiedFurniture [i] = new ModifiedDoorOrWindow((HomeDoorOrWindow)piece);
        } else if (piece instanceof HomeFurnitureGroup) {
          modifiedFurniture [i] = new ModifiedFurnitureGroup((HomeFurnitureGroup)piece);
        } else {
          modifiedFurniture [i] = new ModifiedPieceOfFurniture(piece);
        }
      }
      // Apply modification
      doModifyFurniture(modifiedFurniture, name, nameVisible, description,
          price, removePrice, valueAddedTaxPercentage, removeValueAddedTaxPercentage, currency,
          x, y, elevation, angle, roll, pitch, horizontalAxis, basePlanItem,
          width, depth, height, proportional, modelTransformations,
          this.wallThickness, this.wallDistance, this.wallWidth, this.wallLeft, this.wallHeight, this.wallTop, this.sashes,
          paint, color, texture, modelMaterials, defaultShininess, shininess, visible, modelMirrored, lightPower);
      if (this.undoSupport != null) {
        List<Selectable> newSelection = this.home.getSelectedItems();
        UndoableEdit undoableEdit = new FurnitureModificationUndoableEdit(
            this.home, this.preferences, oldSelection, newSelection, modifiedFurniture,
            name, nameVisible, description, price, removePrice, valueAddedTaxPercentage, removeValueAddedTaxPercentage, currency,
            x, y, elevation, angle, roll, pitch, horizontalAxis, basePlanItem,
            width, depth, height, proportional, modelTransformations,
            this.wallThickness, this.wallDistance, this.wallWidth, this.wallLeft, this.wallHeight, this.wallTop, this.sashes,
            paint, color, texture, modelMaterials, defaultShininess, shininess, visible, modelMirrored, lightPower);
        this.undoSupport.postEdit(undoableEdit);
      }
      if (name != null) {
        this.preferences.addAutoCompletionString("HomePieceOfFurnitureName", name);
      }
      if (description != null) {
        this.preferences.addAutoCompletionString("HomePieceOfFurnitureDescription", description);
      }
      if (valueAddedTaxPercentage != null) {
        this.preferences.setDefaultValueAddedTaxPercentage(valueAddedTaxPercentage);
      }
    }
  }

  /**
   * Undoable edit for furniture modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class FurnitureModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home                        home;
    private final UserPreferences             preferences;
    private final ModifiedPieceOfFurniture [] modifiedFurniture;
    private final List<Selectable>            oldSelection;
    private final List<Selectable>            newSelection;
    private final String                      name;
    private final Boolean                     nameVisible;
    private final String                      description;
    private final BigDecimal                  price;
    private final boolean                     removePrice;
    private final String                      currency;
    private final BigDecimal                  valueAddedTaxPercentage;
    private final boolean                     removeValueAddedTaxPercentage;
    private final Float                       x;
    private final Float                       y;
    private final Float                       elevation;
    private final Float                       angle;
    private final Float                       roll;
    private final Float                       pitch;
    private final FurnitureHorizontalAxis     horizontalAxis;
    private final Boolean                     basePlanItem;
    private final Float                       width;
    private final Float                       depth;
    private final Float                       height;
    private final boolean                     proportional;
    private final Transformation []           modelTransformations;
    private final FurniturePaint              paint;
    private final Integer                     color;
    private final HomeTexture                 texture;
    private final HomeMaterial []             modelMaterials;
    private final boolean                     defaultShininess;
    private final Float                       shininess;
    private final Boolean                     visible;
    private final Boolean                     modelMirrored;
    private final Float                       lightPower;
    private final float                       wallThickness;
    private final float                       wallDistance;
    private final float                       wallWidth;
    private final float                       wallLeft;
    private final float                       wallHeight;
    private final float                       wallTop;
    private final Sash []                     sashes;

    private FurnitureModificationUndoableEdit(Home home,
                                              UserPreferences preferences,
                                              List<Selectable> oldSelection,
                                              List<Selectable> newSelection,
                                              ModifiedPieceOfFurniture [] modifiedFurniture,
                                              String name, Boolean nameVisible, String description,
                                              BigDecimal price, boolean removePrice, BigDecimal valueAddedTaxPercentage, boolean removeValueAddedTaxPercenage, String currency,
                                              Float x, Float y, Float elevation,
                                              Float angle, Float roll, Float pitch, FurnitureHorizontalAxis horizontalAxis, Boolean basePlanItem,
                                              Float width, Float depth, Float height, boolean proportional, Transformation [] modelTransformations,
                                              float wallThickness, float wallDistance, float wallWidth, float wallLeft, float wallHeight, float wallTop, Sash [] sashes,
                                              FurniturePaint paint, Integer color,
                                              HomeTexture texture, HomeMaterial [] modelMaterials,
                                              boolean defaultShininess, Float shininess,
                                              Boolean visible,
                                              Boolean modelMirrored,
                                              Float lightPower) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.newSelection = newSelection;
      this.modifiedFurniture = modifiedFurniture;
      this.name = name;
      this.nameVisible = nameVisible;
      this.description = description;
      this.price = price;
      this.removePrice = removePrice;
      this.valueAddedTaxPercentage = valueAddedTaxPercentage;
      this.removeValueAddedTaxPercentage = removeValueAddedTaxPercenage;
      this.currency = currency;
      this.x = x;
      this.y = y;
      this.elevation = elevation;
      this.angle = angle;
      this.roll = roll;
      this.pitch = pitch;
      this.horizontalAxis = horizontalAxis;
      this.basePlanItem = basePlanItem;
      this.width = width;
      this.depth = depth;
      this.height = height;
      this.proportional = proportional;
      this.modelTransformations = modelTransformations;
      this.wallThickness = wallThickness;
      this.wallDistance = wallDistance;
      this.wallWidth = wallWidth;
      this.wallLeft = wallLeft;
      this.wallHeight = wallHeight;
      this.wallTop = wallTop;
      this.sashes = sashes;
      this.paint = paint;
      this.color = color;
      this.texture = texture;
      this.modelMaterials = modelMaterials;
      this.defaultShininess = defaultShininess;
      this.shininess = shininess;
      this.visible = visible;
      this.modelMirrored = modelMirrored;
      this.lightPower = lightPower;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyFurniture(this.modifiedFurniture);
      this.home.setSelectedItems(this.oldSelection);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyFurniture(this.modifiedFurniture,
          this.name, this.nameVisible, this.description,
          this.price, this.removePrice, this.valueAddedTaxPercentage, this.removeValueAddedTaxPercentage, this.currency,
          this.x, this.y, this.elevation, this.angle, this.roll, this.pitch, this.horizontalAxis, this.basePlanItem,
          this.width, this.depth, this.height, this.proportional, this.modelTransformations,
          this.wallThickness, this.wallDistance, this.wallWidth, this.wallLeft, this.wallHeight, this.wallTop, this.sashes,
          this.paint, this.color, this.texture, this.modelMaterials,
          this.defaultShininess, this.shininess,
          this.visible, this.modelMirrored, this.lightPower);
      this.home.setSelectedItems(this.newSelection);
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(HomeFurnitureController.class,
          "undoModifyFurnitureName");
    }
  }

  /**
   * Modifies furniture properties with the values in parameter.
   */
  private static void doModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture,
                                        String name, Boolean nameVisible, String description,
                                        BigDecimal price, boolean removePrice, BigDecimal valueAddedTaxPercentage, boolean removeValueAddedTaxPercenage, String currency,
                                        Float x, Float y, Float elevation,
                                        Float angle, Float roll, Float pitch, FurnitureHorizontalAxis horizontalAxis, Boolean basePlanItem,
                                        Float width, Float depth, Float height, boolean proportional, Transformation [] modelTransformations,
                                        float wallThickness, float wallDistance, float wallWidth, float wallLeft, float wallHeight, float wallTop, Sash [] sashes,
                                        FurniturePaint paint, Integer color,
                                        HomeTexture texture, HomeMaterial [] modelMaterials,
                                        boolean defaultShininess, Float shininess,
                                        Boolean visible, Boolean modelMirrored, Float lightPower) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      if (name != null) {
        piece.setName(name);
      }
      if (nameVisible != null) {
        piece.setNameVisible(nameVisible);
      }
      if (description != null) {
        piece.setDescription(description);
      }
      if (!(piece instanceof HomeFurnitureGroup)) {
        if (price != null || removePrice) {
          if (price != piece.getPrice()
              && (price == null || !price.equals(piece.getPrice()))) {
            // Update currency when price changes
            piece.setCurrency(price != null ? currency : null);
          }
          if (price != null) {
            // Update scale according to fraction digits of the currency
            try {
              price = price.setScale(Currency.getInstance(currency).getDefaultFractionDigits(), RoundingMode.HALF_UP);
            } catch (IllegalArgumentException ex) {
              // Unknown currency
            }
          }
          piece.setPrice(price);
        }
        if (valueAddedTaxPercentage != null || removeValueAddedTaxPercenage) {
          piece.setValueAddedTaxPercentage(valueAddedTaxPercentage);
        }
      }
      if (x != null) {
        piece.setX(x);
      }
      if (y != null) {
        piece.setY(y);
      }
      if (elevation != null) {
        piece.setElevation(elevation);
      }
      if (angle != null) {
        piece.setAngle(angle);
      }
      if (horizontalAxis != null) {
        switch (horizontalAxis) {
          case ROLL :
            if (roll != null) {
              piece.setRoll(roll);
              piece.setPitch(0);
            }
            break;
          case PITCH :
            if (pitch != null) {
              piece.setPitch(pitch);
              piece.setRoll(0);
            }
            break;
        }
      }
      if (basePlanItem != null && !piece.isDoorOrWindow()) {
        piece.setMovable(!basePlanItem);
      }
      if (piece.isResizable()) {
        float oldWidth = piece.getWidth();
        float oldDepth = piece.getDepth();
        boolean deformable = !proportional && piece.isDeformable();
        if (deformable) {
          if (width != null) {
            piece.setWidth(width);
          } else if (depth != null
                     && !piece.isWidthDepthDeformable()) {
            piece.setWidth(piece.getWidth() * depth / oldDepth);
          }

          if (depth != null) {
            piece.setDepth(depth);
          } else if (width != null
                     && !piece.isWidthDepthDeformable()) {
            piece.setDepth(piece.getDepth() * width / oldWidth);
          }

          if (height != null) {
            piece.setHeight(height);
          }
        } else {
          if (width != null) {
            piece.scale(width / piece.getWidth());
          } else if (depth != null) {
            piece.scale(depth / piece.getDepth());
          } else if (height != null) {
            piece.scale(height / piece.getHeight());
          }
        }

        if (modelMirrored != null) {
          piece.setModelMirrored(modelMirrored);
        }

        if (piece instanceof HomeDoorOrWindow
            && modifiedFurniture.length == 1
            && !Arrays.deepEquals(piece.getModelTransformations(),
                    modelTransformations != null && modelTransformations.length > 0 ? modelTransformations : null)) {
          HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
          doorOrWindow.setWallThickness(wallThickness);
          doorOrWindow.setWallDistance(wallDistance);
          doorOrWindow.setWallWidth(wallWidth);
          doorOrWindow.setWallLeft(wallLeft);
          doorOrWindow.setWallHeight(wallHeight);
          doorOrWindow.setWallTop(wallTop);
          doorOrWindow.setSashes(sashes);
        }
      }
      if (piece.isTexturable()) {
        if (paint != null) {
          switch (paint) {
            case DEFAULT :
              piece.setColor(null);
              piece.setTexture(null);
              piece.setModelMaterials(null);
              break;
            case COLORED :
              piece.setColor(color);
              piece.setTexture(null);
              piece.setModelMaterials(null);
              break;
            case TEXTURED :
              piece.setColor(null);
              piece.setTexture(texture);
              piece.setModelMaterials(null);
              break;
            case MODEL_MATERIALS :
              piece.setColor(null);
              piece.setTexture(null);
              piece.setModelMaterials(modelMaterials);
              break;
          }
        }
        if (defaultShininess) {
          piece.setShininess(null);
        } else if (shininess != null) {
          piece.setShininess(shininess);
        }
      }
      if (modelTransformations != null) {
        piece.setModelTransformations(modelTransformations.length > 0 ? modelTransformations : null);
      }
      if (visible != null) {
        piece.setVisible(visible);
      }
      if (lightPower != null) {
        ((HomeLight)piece).setPower(lightPower);
      }
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedFurniture</code>.
   */
  private static void undoModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      modifiedPiece.reset();
    }
  }

  /**
   * Stores the current properties values of a modified piece of furniture.
   */
  private static class ModifiedPieceOfFurniture {
    private final HomePieceOfFurniture piece;
    private final String               name;
    private final boolean              nameVisible;
    private final String               description;
    private final BigDecimal           price;
    private final BigDecimal           valueAddedTaxPercentage;
    private final String               currency;
    private final float                x;
    private final float                y;
    private final float                elevation;
    private final float                angle;
    private final float                roll;
    private final float                pitch;
    private final boolean              movable;
    private final float                width;
    private final float                depth;
    private final float                height;
    private final Transformation []    modelTransformations;
    private final Integer              color;
    private final HomeTexture          texture;
    private final HomeMaterial []      modelMaterials;
    private final Float                shininess;
    private final boolean              visible;
    private final boolean              modelMirrored;

    public ModifiedPieceOfFurniture(HomePieceOfFurniture piece) {
      this.piece = piece;
      this.name = piece.getName();
      this.nameVisible = piece.isNameVisible();
      this.description = piece.getDescription();
      this.price = piece.getPrice();
      this.valueAddedTaxPercentage = piece.getValueAddedTaxPercentage();
      this.currency = piece.getCurrency();
      this.x = piece.getX();
      this.y = piece.getY();
      this.elevation = piece.getElevation();
      this.angle = piece.getAngle();
      this.roll = piece.getRoll();
      this.pitch = piece.getPitch();
      this.movable = piece.isMovable();
      this.width = piece.getWidth();
      this.depth = piece.getDepth();
      this.height = piece.getHeight();
      this.modelTransformations = piece.getModelTransformations();
      this.color = piece.getColor();
      this.texture = piece.getTexture();
      this.modelMaterials = piece.getModelMaterials();
      this.shininess = piece.getShininess();
      this.visible = piece.isVisible();
      this.modelMirrored = piece.isModelMirrored();
    }

    public HomePieceOfFurniture getPieceOfFurniture() {
      return this.piece;
    }

    public void reset() {
      this.piece.setName(this.name);
      this.piece.setNameVisible(this.nameVisible);
      this.piece.setDescription(this.description);
      if (!(this.piece instanceof HomeFurnitureGroup)) {
        this.piece.setPrice(this.price);
        this.piece.setValueAddedTaxPercentage(this.valueAddedTaxPercentage);
        this.piece.setCurrency(this.currency);
      }
      this.piece.setX(this.x);
      this.piece.setY(this.y);
      this.piece.setElevation(this.elevation);
      this.piece.setAngle(this.angle);
      if (this.piece.isHorizontallyRotatable()) {
        this.piece.setRoll(this.roll);
        this.piece.setPitch(this.pitch);
      }
      this.piece.setMovable(this.movable);
      if (this.piece.isResizable()) {
        this.piece.setWidth(this.width);
        this.piece.setDepth(this.depth);
        this.piece.setHeight(this.height);
        this.piece.setModelMirrored(this.modelMirrored);
      }
      this.piece.setModelTransformations(this.modelTransformations);
      if (this.piece.isTexturable()) {
        this.piece.setColor(this.color);
        this.piece.setTexture(this.texture);
        this.piece.setModelMaterials(this.modelMaterials);
        this.piece.setShininess(this.shininess);
      }
      this.piece.setVisible(this.visible);
    }
  }

  /**
   * Stores the current properties values of a modified door or window.
   */
  private static class ModifiedDoorOrWindow extends ModifiedPieceOfFurniture {
    private final boolean boundToWall;
    private final float   wallThickness;
    private final float   wallDistance;
    private final float   wallWidth;
    private final float   wallLeft;
    private final float   wallHeight;
    private final float   wallTop;
    private final Sash [] sashes;

    public ModifiedDoorOrWindow(HomeDoorOrWindow doorOrWindow) {
      super(doorOrWindow);
      this.boundToWall = doorOrWindow.isBoundToWall();
      this.wallThickness = doorOrWindow.getWallThickness();
      this.wallDistance = doorOrWindow.getWallDistance();
      this.wallWidth = doorOrWindow.getWallWidth();
      this.wallLeft = doorOrWindow.getWallLeft();
      this.wallHeight = doorOrWindow.getWallHeight();
      this.wallTop = doorOrWindow.getWallTop();
      this.sashes = doorOrWindow.getSashes();
    }

    public void reset() {
      super.reset();
      HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)getPieceOfFurniture();
      doorOrWindow.setBoundToWall(this.boundToWall);
      doorOrWindow.setWallThickness(this.wallThickness);
      doorOrWindow.setWallDistance(this.wallDistance);
      doorOrWindow.setWallWidth(this.wallWidth);
      doorOrWindow.setWallLeft(this.wallLeft);
      doorOrWindow.setWallHeight(this.wallHeight);
      doorOrWindow.setWallTop(this.wallTop);
      doorOrWindow.setSashes(this.sashes);
    }
  }

  /**
   * Stores the current properties values of a modified light.
   */
  private static class ModifiedLight extends ModifiedPieceOfFurniture {
    private final float power;

    public ModifiedLight(HomeLight light) {
      super(light);
      this.power = light.getPower();
    }

    public void reset() {
      super.reset();
      ((HomeLight)getPieceOfFurniture()).setPower(this.power);
    }
  }

  /**
   * Stores the current properties values of a modified group.
   */
  private static class ModifiedFurnitureGroup extends ModifiedPieceOfFurniture {
    private final float [] groupFurnitureX;
    private final float [] groupFurnitureY;
    private final float [] groupFurnitureWidth;
    private final float [] groupFurnitureDepth;
    private final Integer []     groupFurnitureColor;
    private final HomeTexture [] groupFurnitureTexture;
    private final HomeMaterial [][] groupFurnitureModelMaterials;
    private final Float []          groupFurnitureShininess;

    public ModifiedFurnitureGroup(HomeFurnitureGroup group) {
      super(group);
      List<HomePieceOfFurniture> groupFurniture = getGroupFurniture((HomeFurnitureGroup)group);
      this.groupFurnitureX = new float [groupFurniture.size()];
      this.groupFurnitureY = new float [groupFurniture.size()];
      this.groupFurnitureWidth = new float [groupFurniture.size()];
      this.groupFurnitureDepth = new float [groupFurniture.size()];
      this.groupFurnitureColor = new Integer [groupFurniture.size()];
      this.groupFurnitureTexture = new HomeTexture [groupFurniture.size()];
      this.groupFurnitureShininess = new Float [groupFurniture.size()];
      this.groupFurnitureModelMaterials = new HomeMaterial [groupFurniture.size()][];
      for (int i = 0; i < groupFurniture.size(); i++) {
        HomePieceOfFurniture groupPiece = groupFurniture.get(i);
        this.groupFurnitureX [i] = groupPiece.getX();
        this.groupFurnitureY [i] = groupPiece.getY();
        this.groupFurnitureWidth [i] = groupPiece.getWidth();
        this.groupFurnitureDepth [i] = groupPiece.getDepth();
        this.groupFurnitureColor [i] = groupPiece.getColor();
        this.groupFurnitureTexture [i] = groupPiece.getTexture();
        this.groupFurnitureShininess [i] = groupPiece.getShininess();
        this.groupFurnitureModelMaterials [i] = groupPiece.getModelMaterials();
      }
    }

    public void reset() {
      super.reset();
      HomeFurnitureGroup group = (HomeFurnitureGroup)getPieceOfFurniture();
      List<HomePieceOfFurniture> groupFurniture = getGroupFurniture(group);
      for (int i = 0; i < groupFurniture.size(); i++) {
        HomePieceOfFurniture groupPiece = groupFurniture.get(i);
        if (group.isResizable()) {
          // Restore group furniture location and size because resizing a group isn't reversible
          groupPiece.setX(this.groupFurnitureX [i]);
          groupPiece.setY(this.groupFurnitureY [i]);
          groupPiece.setWidth(this.groupFurnitureWidth [i]);
          groupPiece.setDepth(this.groupFurnitureDepth [i]);
        }
        if (group.isTexturable()
            && !(groupPiece instanceof HomeFurnitureGroup)) {
          // Restore group furniture color and texture
          groupPiece.setColor(this.groupFurnitureColor [i]);
          groupPiece.setTexture(this.groupFurnitureTexture [i]);
          groupPiece.setModelMaterials(this.groupFurnitureModelMaterials [i]);
          groupPiece.setShininess(this.groupFurnitureShininess [i]);
        }
      }
    }

    /**
     * Returns all the children of the given <code>furnitureGroup</code>.
     */
    private List<HomePieceOfFurniture> getGroupFurniture(HomeFurnitureGroup furnitureGroup) {
      List<HomePieceOfFurniture> pieces = new ArrayList<HomePieceOfFurniture>();
      for (HomePieceOfFurniture piece : furnitureGroup.getFurniture()) {
        pieces.add(piece);
        if (piece instanceof HomeFurnitureGroup) {
          pieces.addAll(getGroupFurniture((HomeFurnitureGroup)piece));
        }
      }
      return pieces;
    }
  }
}
