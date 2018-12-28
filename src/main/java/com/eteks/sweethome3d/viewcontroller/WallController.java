/*
 * WallController.java 30 mai 07
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

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for wall view.
 * @author Emmanuel Puybaret
 */
public class WallController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {X_START, Y_START, X_END, Y_END, LENGTH, DISTANCE_TO_END_POINT, EDITABLE_POINTS,
      LEFT_SIDE_COLOR, LEFT_SIDE_PAINT, LEFT_SIDE_SHININESS,
      RIGHT_SIDE_COLOR, RIGHT_SIDE_PAINT, RIGHT_SIDE_SHININESS,
      PATTERN, TOP_COLOR, TOP_PAINT,
      SHAPE, RECTANGULAR_WALL_HEIGHT, SLOPING_WALL_HEIGHT_AT_START, SLOPING_WALL_HEIGHT_AT_END,
      THICKNESS, ARC_EXTENT_IN_DEGREES}
  /**
   * The possible values for {@linkplain #getShape() wall shape}.
   */
  public enum WallShape {RECTANGULAR_WALL, SLOPING_WALL}
  /**
   * The possible values for {@linkplain #getLeftSidePaint() wall paint type}.
   */
  public enum WallPaint {DEFAULT, COLORED, TEXTURED}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private TextureChoiceController     leftSideTextureController;
  private BaseboardChoiceController   leftSideBaseboardController;
  private TextureChoiceController     rightSideTextureController;
  private BaseboardChoiceController   rightSideBaseboardController;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  wallView;

  private boolean      editablePoints;
  private Float        xStart;
  private Float        yStart;
  private Float        xEnd;
  private Float        yEnd;
  private Float        length;
  private Float        distanceToEndPoint;
  private Integer      leftSideColor;
  private WallPaint    leftSidePaint;
  private Float        leftSideShininess;
  private Integer      rightSideColor;
  private WallPaint    rightSidePaint;
  private Float        rightSideShininess;
  private TextureImage pattern;
  private Integer      topColor;
  private WallPaint    topPaint;
  private WallShape    shape;
  private Float        rectangularWallHeight;
  private Float        slopingWallHeightAtStart;
  private Float        sloppingWallHeightAtEnd;
  private Float        thickness;
  private Float        arcExtentInDegrees;

  /**
   * Creates the controller of wall view with undo support.
   */
  public WallController(final Home home,
                        UserPreferences preferences,
                        ViewFactory viewFactory,
                        ContentManager contentManager,
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
   * Returns the texture controller of the wall left side.
   */
  public TextureChoiceController getLeftSideTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.leftSideTextureController == null) {
      this.leftSideTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(WallController.class, "leftSideTextureTitle"),
          this.preferences, this.viewFactory, this.contentManager);
      this.leftSideTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setLeftSidePaint(WallPaint.TEXTURED);
            }
          });
    }
    return this.leftSideTextureController;
  }

  /**
   * Returns the controller of the wall left side baseboard.
   * @since 5.0
   */
  public BaseboardChoiceController getLeftSideBaseboardController() {
    // Create sub controller lazily only once it's needed
    if (this.leftSideBaseboardController == null) {
      this.leftSideBaseboardController = new BaseboardChoiceController(
          this.preferences, this.viewFactory, this.contentManager);
    }
    return this.leftSideBaseboardController;
  }

  /**
   * Returns the texture controller of the wall right side.
   */
  public TextureChoiceController getRightSideTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.rightSideTextureController == null) {
      this.rightSideTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(WallController.class, "rightSideTextureTitle"),
          this.preferences, this.viewFactory, this.contentManager);
      this.rightSideTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setRightSidePaint(WallPaint.TEXTURED);
            }
          });
    }
    return this.rightSideTextureController;
  }

  /**
   * Returns the controller of the wall right side baseboard.
   * @since 5.0
   */
  public BaseboardChoiceController getRightSideBaseboardController() {
    // Create sub controller lazily only once it's needed
    if (this.rightSideBaseboardController == null) {
      this.rightSideBaseboardController = new BaseboardChoiceController(
          this.preferences, this.viewFactory, this.contentManager);
    }
    return this.rightSideBaseboardController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.wallView == null) {
      this.wallView = this.viewFactory.createWallView(this.preferences, this);
    }
    return this.wallView;
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
   * Updates edited properties from selected walls in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Wall> selectedWalls = Home.getWallsSubList(this.home.getSelectedItems());
    if (selectedWalls.isEmpty()) {
      setXStart(null); // Nothing to edit
      setYStart(null);
      setXEnd(null);
      setYEnd(null);
      setEditablePoints(false);
      setLeftSideColor(null);
      getLeftSideTextureController().setTexture(null);
      setLeftSidePaint(null);
      setLeftSideShininess(null);
      getLeftSideBaseboardController().setVisible(null);
      getLeftSideBaseboardController().setThickness(null);
      getLeftSideBaseboardController().setHeight(null);
      getLeftSideBaseboardController().setColor(null);
      getLeftSideBaseboardController().getTextureController().setTexture(null);
      getLeftSideBaseboardController().setPaint(null);
      setRightSideColor(null);
      getRightSideTextureController().setTexture(null);
      setRightSidePaint(null);
      setRightSideShininess(null);
      getLeftSideBaseboardController().setVisible(null);
      getLeftSideBaseboardController().setThickness(null);
      getLeftSideBaseboardController().setHeight(null);
      getLeftSideBaseboardController().setColor(null);
      getLeftSideBaseboardController().getTextureController().setTexture(null);
      getLeftSideBaseboardController().setPaint(null);
      setRectangularWallHeight(null);
      setSlopingWallHeightAtStart(null);
      setSlopingWallHeightAtEnd(null);
      setShape(null);
      setThickness(null);
      setArcExtentInDegrees(null);
    } else {
      // Search the common properties among selected walls
      Wall firstWall = selectedWalls.get(0);
      boolean multipleSelection = selectedWalls.size() > 1;

      setEditablePoints(!multipleSelection);

      // Search the common xStart value among walls
      Float xStart = firstWall.getXStart();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!xStart.equals(selectedWalls.get(i).getXStart())) {
          xStart = null;
          break;
        }
      }
      setXStart(xStart);

      // Search the common yStart value among walls
      Float yStart = firstWall.getYStart();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!yStart.equals(selectedWalls.get(i).getYStart())) {
          yStart = null;
          break;
        }
      }
      setYStart(yStart);

      // Search the common xEnd value among walls
      Float xEnd = firstWall.getXEnd();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!xEnd.equals(selectedWalls.get(i).getXEnd())) {
          xEnd = null;
          break;
        }
      }
      setXEnd(xEnd);

      // Search the common yEnd value among walls
      Float yEnd = firstWall.getYEnd();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!yEnd.equals(selectedWalls.get(i).getYEnd())) {
          yEnd = null;
          break;
        }
      }
      setYEnd(yEnd);

      // Search the common left side color among walls
      Integer leftSideColor = firstWall.getLeftSideColor();
      if (leftSideColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!leftSideColor.equals(selectedWalls.get(i).getLeftSideColor())) {
            leftSideColor = null;
            break;
          }
        }
      }
      setLeftSideColor(leftSideColor);

      // Search the common left side texture among walls
      HomeTexture leftSideTexture = firstWall.getLeftSideTexture();
      if (leftSideTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!leftSideTexture.equals(selectedWalls.get(i).getLeftSideTexture())) {
            leftSideTexture = null;
            break;
          }
        }
      }
      getLeftSideTextureController().setTexture(leftSideTexture);

      boolean defaultColorsAndTextures = true;
      for (int i = 0; i < selectedWalls.size(); i++) {
        Wall wall = selectedWalls.get(i);
        if (wall.getLeftSideColor() != null
            || wall.getLeftSideTexture() != null) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (leftSideColor != null) {
        setLeftSidePaint(WallPaint.COLORED);
      } else if (leftSideTexture != null) {
        setLeftSidePaint(WallPaint.TEXTURED);
      } else if (defaultColorsAndTextures) {
        setLeftSidePaint(WallPaint.DEFAULT);
      } else {
        setLeftSidePaint(null);
      }

      // Search the common left side shininess value among walls
      Float leftSideShininess = firstWall.getLeftSideShininess();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!leftSideShininess.equals(selectedWalls.get(i).getLeftSideShininess())) {
          leftSideShininess = null;
          break;
        }
      }
      setLeftSideShininess(leftSideShininess);

      Boolean leftSideBaseboardVisible = firstWall.getLeftSideBaseboard() != null;
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (leftSideBaseboardVisible != (selectedWalls.get(i).getLeftSideBaseboard() != null)) {
          leftSideBaseboardVisible = null;
          break;
        }
      }
      getLeftSideBaseboardController().setVisible(leftSideBaseboardVisible);

      // Search the common thickness left baseboard among walls
      Baseboard firstWallLeftSideBaseboard = firstWall.getLeftSideBaseboard();
      Float leftSideBaseboardThickness = firstWallLeftSideBaseboard != null
          ? firstWallLeftSideBaseboard.getThickness()
          : this.preferences.getNewWallBaseboardThickness();
      for (int i = 1; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getLeftSideBaseboard();
        if (!leftSideBaseboardThickness.equals(baseboard != null
                ? baseboard.getThickness()
                : this.preferences.getNewWallBaseboardThickness())) {
          leftSideBaseboardThickness = null;
          break;
        }
      }
      getLeftSideBaseboardController().setThickness(leftSideBaseboardThickness);

      // Search the common height left baseboard among walls
      Float leftSideBaseboardHeight = firstWallLeftSideBaseboard != null
          ? firstWallLeftSideBaseboard.getHeight()
          : this.preferences.getNewWallBaseboardHeight();
      for (int i = 1; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getLeftSideBaseboard();
        if (!leftSideBaseboardHeight.equals(baseboard != null
                ? baseboard.getHeight()
                : this.preferences.getNewWallBaseboardHeight())) {
          leftSideBaseboardHeight = null;
          break;
        }
      }
      getLeftSideBaseboardController().setHeight(leftSideBaseboardHeight);

      // Search the common left baseboard color among walls
      Integer leftSideBaseboardColor = firstWallLeftSideBaseboard != null
          ? firstWallLeftSideBaseboard.getColor()
          : null;
      if (leftSideBaseboardColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          Baseboard baseboard = selectedWalls.get(i).getLeftSideBaseboard();
          if (baseboard == null
              || !leftSideBaseboardColor.equals(baseboard.getColor())) {
            leftSideBaseboardColor = null;
            break;
          }
        }
      }
      getLeftSideBaseboardController().setColor(leftSideBaseboardColor);

      // Search the common left baseboard texture among walls
      HomeTexture leftSideBaseboardTexture = firstWallLeftSideBaseboard != null
          ? firstWallLeftSideBaseboard.getTexture()
          : null;
      if (leftSideBaseboardTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          Baseboard baseboard = selectedWalls.get(i).getLeftSideBaseboard();
          if (baseboard == null
              || !leftSideBaseboardTexture.equals(baseboard.getTexture())) {
            leftSideBaseboardTexture = null;
            break;
          }
        }
      }
      getLeftSideBaseboardController().getTextureController().setTexture(leftSideBaseboardTexture);

      defaultColorsAndTextures = true;
      for (int i = 0; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getLeftSideBaseboard();
        if (baseboard != null
            && (baseboard.getColor() != null
                || baseboard.getTexture() != null)) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (leftSideBaseboardColor != null) {
        getLeftSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
      } else if (leftSideBaseboardTexture != null) {
        getLeftSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.TEXTURED);
      } else if (defaultColorsAndTextures) {
        getLeftSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.DEFAULT);
      } else {
        getLeftSideBaseboardController().setPaint(null);
      }

      // Search the common right side color among walls
      Integer rightSideColor = firstWall.getRightSideColor();
      if (rightSideColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!rightSideColor.equals(selectedWalls.get(i).getRightSideColor())) {
            rightSideColor = null;
            break;
          }
        }
      }
      setRightSideColor(rightSideColor);

      // Search the common right side texture among walls
      HomeTexture rightSideTexture = firstWall.getRightSideTexture();
      if (rightSideTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!rightSideTexture.equals(selectedWalls.get(i).getRightSideTexture())) {
            rightSideTexture = null;
            break;
          }
        }
      }
      getRightSideTextureController().setTexture(rightSideTexture);

      defaultColorsAndTextures = true;
      for (int i = 0; i < selectedWalls.size(); i++) {
        Wall wall = selectedWalls.get(i);
        if (wall.getRightSideColor() != null
            || wall.getRightSideTexture() != null) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (rightSideColor != null) {
        setRightSidePaint(WallPaint.COLORED);
      } else if (rightSideTexture != null) {
        setRightSidePaint(WallPaint.TEXTURED);
      } else if (defaultColorsAndTextures) {
        setRightSidePaint(WallPaint.DEFAULT);
      } else {
        setRightSidePaint(null);
      }

      // Search the common right side shininess value among walls
      Float rightSideShininess = firstWall.getRightSideShininess();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!rightSideShininess.equals(selectedWalls.get(i).getRightSideShininess())) {
          rightSideShininess = null;
          break;
        }
      }
      setRightSideShininess(rightSideShininess);

      Boolean rightSideBaseboardVisible = firstWall.getRightSideBaseboard() != null;
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (rightSideBaseboardVisible != (selectedWalls.get(i).getRightSideBaseboard() != null)) {
          rightSideBaseboardVisible = null;
          break;
        }
      }
      getRightSideBaseboardController().setVisible(rightSideBaseboardVisible);

      // Search the common thickness right baseboard among walls
      Baseboard firstWallRightSideBaseboard = firstWall.getRightSideBaseboard();
      Float rightSideBaseboardThickness = firstWallRightSideBaseboard != null
          ? firstWallRightSideBaseboard.getThickness()
          : this.preferences.getNewWallBaseboardThickness();
      for (int i = 1; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getRightSideBaseboard();
        if (!rightSideBaseboardThickness.equals(baseboard != null
                ? baseboard.getThickness()
                : this.preferences.getNewWallBaseboardThickness())) {
          rightSideBaseboardThickness = null;
          break;
        }
      }
      getRightSideBaseboardController().setThickness(rightSideBaseboardThickness);

      // Search the common height right baseboard among walls
      Float rightSideBaseboardHeight = firstWallRightSideBaseboard != null
          ? firstWallRightSideBaseboard.getHeight()
          : this.preferences.getNewWallBaseboardHeight();
      for (int i = 1; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getRightSideBaseboard();
        if (!rightSideBaseboardHeight.equals(baseboard != null
                ? baseboard.getHeight()
                : this.preferences.getNewWallBaseboardHeight())) {
          rightSideBaseboardHeight = null;
          break;
        }
      }
      getRightSideBaseboardController().setHeight(rightSideBaseboardHeight);

      // Search the common right baseboard color among walls
      Integer rightSideBaseboardColor = firstWallRightSideBaseboard != null
          ? firstWallRightSideBaseboard.getColor()
          : null;
      if (rightSideBaseboardColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          Baseboard baseboard = selectedWalls.get(i).getRightSideBaseboard();
          if (baseboard == null
              || !rightSideBaseboardColor.equals(baseboard.getColor())) {
            rightSideBaseboardColor = null;
            break;
          }
        }
      }
      getRightSideBaseboardController().setColor(rightSideBaseboardColor);

      // Search the common right baseboard texture among walls
      HomeTexture rightSideBaseboardTexture = firstWallRightSideBaseboard != null
          ? firstWallRightSideBaseboard.getTexture()
          : null;
      if (rightSideBaseboardTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          Baseboard baseboard = selectedWalls.get(i).getRightSideBaseboard();
          if (baseboard == null
              || !rightSideBaseboardTexture.equals(baseboard.getTexture())) {
            rightSideBaseboardTexture = null;
            break;
          }
        }
      }
      getRightSideBaseboardController().getTextureController().setTexture(rightSideBaseboardTexture);

      defaultColorsAndTextures = true;
      for (int i = 0; i < selectedWalls.size(); i++) {
        Baseboard baseboard = selectedWalls.get(i).getRightSideBaseboard();
        if (baseboard != null
            && (baseboard.getColor() != null
                || baseboard.getTexture() != null)) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (rightSideBaseboardColor != null) {
        getRightSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
      } else if (rightSideBaseboardTexture != null) {
        getRightSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.TEXTURED);
      } else if (defaultColorsAndTextures) {
        getRightSideBaseboardController().setPaint(BaseboardChoiceController.BaseboardPaint.DEFAULT);
      } else {
        getRightSideBaseboardController().setPaint(null);
      }

      // Search the common pattern among walls
      TextureImage pattern = firstWall.getPattern();
      if (pattern == null) {
        pattern = this.preferences.getWallPattern();
      }
      for (int i = 1; i < selectedWalls.size(); i++) {
        TextureImage otherPattern = selectedWalls.get(i).getPattern();
        if (otherPattern == null) {
          otherPattern = this.preferences.getWallPattern();
        }
        if (!pattern.equals(otherPattern)) {
          pattern = null;
          break;
        }
      }
      setPattern(pattern);

      // Search the common top color among walls
      Integer topColor = firstWall.getTopColor();
      boolean defaultTopColor;
      if (topColor != null) {
        defaultTopColor = false;
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!topColor.equals(selectedWalls.get(i).getTopColor())) {
            topColor = null;
            break;
          }
        }
      } else {
        defaultTopColor = true;
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (selectedWalls.get(i).getTopColor() != null) {
            defaultTopColor = false;
            break;
          }
        }
      }
      setTopColor(topColor);

      if (defaultTopColor) {
        setTopPaint(WallPaint.DEFAULT);
      } else if (topColor != null) {
        setTopPaint(WallPaint.COLORED);
      } else {
        setTopPaint(null);
      }

      // Search the common height among walls
      Float height = firstWall.getHeight();
      // If wall height was never set, use home wall height
      if (height == null && firstWall.getHeight() == null) {
        height = this.home.getWallHeight();
      }
      for (int i = 1; i < selectedWalls.size(); i++) {
        Wall wall = selectedWalls.get(i);
        float wallHeight = wall.getHeight() == null
            ? this.home.getWallHeight()
            : wall.getHeight();
        if (height != wallHeight) {
          height = null;
          break;
        }
      }
      setRectangularWallHeight(height);
      setSlopingWallHeightAtStart(height);

      // Search the common height at end among walls
      Float heightAtEnd = firstWall.getHeightAtEnd();
      if (heightAtEnd != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!heightAtEnd.equals(selectedWalls.get(i).getHeightAtEnd())) {
            heightAtEnd = null;
            break;
          }
        }
      }
      setSlopingWallHeightAtEnd(heightAtEnd == null && selectedWalls.size() == 1 ? height : heightAtEnd);

      boolean allWallsRectangular = !firstWall.isTrapezoidal();
      boolean allWallsTrapezoidal = firstWall.isTrapezoidal();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!selectedWalls.get(i).isTrapezoidal()) {
          allWallsTrapezoidal = false;
        } else {
          allWallsRectangular = false;
        }
      }
      if (allWallsRectangular) {
        setShape(WallShape.RECTANGULAR_WALL);
      } else if (allWallsTrapezoidal) {
        setShape(WallShape.SLOPING_WALL);
      } else {
        setShape(null);
      }

      // Search the common thickness among walls
      Float thickness = firstWall.getThickness();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (thickness != selectedWalls.get(i).getThickness()) {
          thickness = null;
          break;
        }
      }
      setThickness(thickness);

      // Search the common arc extent among walls
      Float arcExtent = firstWall.getArcExtent();
      if (arcExtent != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!arcExtent.equals(selectedWalls.get(i).getArcExtent())) {
            arcExtent = null;
            break;
          }
        }
      }
      if (arcExtent != null) {
        setArcExtentInDegrees((float)Math.toDegrees(arcExtent));
      } else {
        setArcExtentInDegrees(selectedWalls.size() == 1 ? new Float(0) : null);
      }
    }
  }

  /**
   * Sets the edited abscissa of the start point.
   */
  public void setXStart(Float xStart) {
    if (xStart != this.xStart) {
      Float oldXStart = this.xStart;
      this.xStart = xStart;
      this.propertyChangeSupport.firePropertyChange(Property.X_START.name(), oldXStart, xStart);
      updateLength();
      updateDistanceToEndPoint();
    }
  }

  /**
   * Returns the edited abscissa of the start point.
   */
  public Float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the edited ordinate of the start point.
   */
  public void setYStart(Float yStart) {
    if (yStart != this.yStart) {
      Float oldYStart = this.yStart;
      this.yStart = yStart;
      this.propertyChangeSupport.firePropertyChange(Property.Y_START.name(), oldYStart, yStart);
      updateLength();
      updateDistanceToEndPoint();
    }
  }

  /**
   * Returns the edited ordinate of the start point.
   */
  public Float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the edited abscissa of the end point.
   */
  public void setXEnd(Float xEnd) {
    if (xEnd != this.xEnd) {
      Float oldXEnd = this.xEnd;
      this.xEnd = xEnd;
      this.propertyChangeSupport.firePropertyChange(Property.X_END.name(), oldXEnd, xEnd);
      updateLength();
      updateDistanceToEndPoint();
    }
  }

  /**
   * Returns the edited abscissa of the end point.
   */
  public Float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the edited ordinate of the end point.
   */
  public void setYEnd(Float yEnd) {
    if (yEnd != this.yEnd) {
      Float oldYEnd = this.yEnd;
      this.yEnd = yEnd;
      this.propertyChangeSupport.firePropertyChange(Property.Y_END.name(), oldYEnd, yEnd);
      updateLength();
      updateDistanceToEndPoint();
    }
  }

  /**
   * Returns the edited ordinate of the end point.
   */
  public Float getYEnd() {
    return this.yEnd;
  }

  /**
   * Updates the edited length after its coordinates change.
   */
  private void updateLength() {
    Float xStart = getXStart();
    Float yStart = getYStart();
    Float xEnd = getXEnd();
    Float yEnd = getYEnd();
    if (xStart != null && yStart != null && xEnd != null && yEnd != null) {
      Wall wall = new Wall(xStart, yStart, xEnd, yEnd, 0, 0);
      Float arcExtent = getArcExtentInDegrees();
      if (arcExtent != null) {
        wall.setArcExtent((float)Math.toRadians(arcExtent));
      }
      setLength(wall.getLength(), false);
    } else {
      setLength(null, false);
    }
  }

  /**
   * Sets the edited length.
   */
  public void setLength(Float length) {
    setLength(length, true);
  }

  /**
   * Returns the edited length.
   */
  public Float getLength() {
    return this.length;
  }

  /**
   * Sets the edited length and updates the coordinates of the end point if
   * <code>updateEndPoint</code> is <code>true</code>.
   */
  private void setLength(Float length, boolean updateEndPoint) {
    if (length != this.length) {
      Float oldLength = this.length;
      this.length = length;
      this.propertyChangeSupport.firePropertyChange(Property.LENGTH.name(), oldLength, length);

      if (updateEndPoint) {
        Float xStart = getXStart();
        Float yStart = getYStart();
        Float xEnd = getXEnd();
        Float yEnd = getYEnd();
        if (xStart != null && yStart != null && xEnd != null && yEnd != null && length != null) {
          if (getArcExtentInDegrees() != null && getArcExtentInDegrees().floatValue() == 0) {
            double wallAngle = Math.atan2(yStart - yEnd, xEnd - xStart);
            setXEnd((float)(xStart + length * Math.cos(wallAngle)));
            setYEnd((float)(yStart - length * Math.sin(wallAngle)));
          } else {
            throw new UnsupportedOperationException(
                "Computing end point of a round wall from its length not supported");
          }
        } else {
          setXEnd(null);
          setYEnd(null);
        }
      }
    }
  }

  /**
   * Updates the edited distance to end point after its coordinates change.
   */
  private void updateDistanceToEndPoint() {
    Float xStart = getXStart();
    Float yStart = getYStart();
    Float xEnd = getXEnd();
    Float yEnd = getYEnd();
    if (xStart != null && yStart != null && xEnd != null && yEnd != null) {
      setDistanceToEndPoint((float)Point2D.distance(xStart, yStart, xEnd, yEnd), false);
    } else {
      setDistanceToEndPoint(null, false);
    }
  }

  /**
   * Sets the edited distance to end point.
   */
  public void setDistanceToEndPoint(Float distanceToEndPoint) {
    setDistanceToEndPoint(distanceToEndPoint, true);
  }

  /**
   * Sets the edited distance to end point and updates the coordinates of the end point if
   * <code>updateEndPoint</code> is <code>true</code>.
   */
  private void setDistanceToEndPoint(Float distanceToEndPoint, boolean updateEndPoint) {
    if (distanceToEndPoint != this.distanceToEndPoint) {
      Float oldDistance = this.distanceToEndPoint;
      this.distanceToEndPoint = distanceToEndPoint;
      this.propertyChangeSupport.firePropertyChange(Property.DISTANCE_TO_END_POINT.name(), oldDistance, distanceToEndPoint);

      if (updateEndPoint) {
        Float xStart = getXStart();
        Float yStart = getYStart();
        Float xEnd = getXEnd();
        Float yEnd = getYEnd();
        if (xStart != null && yStart != null && xEnd != null && yEnd != null && distanceToEndPoint != null) {
          double wallAngle = Math.atan2(yStart - yEnd, xEnd - xStart);
          setXEnd((float)(xStart + distanceToEndPoint * Math.cos(wallAngle)));
          setYEnd((float)(yStart - distanceToEndPoint * Math.sin(wallAngle)));
        } else {
          setXEnd(null);
          setYEnd(null);
        }
      }
    }
  }

  /**
   * Returns the edited distance to end point.
   */
  public Float getDistanceToEndPoint() {
    return this.distanceToEndPoint;
  }

  /**
   * Sets whether the point coordinates can be be edited or not.
   */
  public void setEditablePoints(boolean editablePoints) {
    if (editablePoints != this.editablePoints) {
      this.editablePoints = editablePoints;
      this.propertyChangeSupport.firePropertyChange(Property.EDITABLE_POINTS.name(), !editablePoints, editablePoints);
    }
  }

  /**
   * Returns whether the point coordinates can be be edited or not.
   */
  public boolean isEditablePoints() {
    return this.editablePoints;
  }

  /**
   * Sets the edited color of the left side.
   */
  public void setLeftSideColor(Integer leftSideColor) {
    if (leftSideColor != this.leftSideColor) {
      Integer oldLeftSideColor = this.leftSideColor;
      this.leftSideColor = leftSideColor;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_COLOR.name(), oldLeftSideColor, leftSideColor);

      setLeftSidePaint(WallPaint.COLORED);
    }
  }

  /**
   * Returns the edited color of the left side.
   */
  public Integer getLeftSideColor() {
    return this.leftSideColor;
  }

  /**
   * Sets whether the left side is colored, textured or unknown painted.
   */
  public void setLeftSidePaint(WallPaint leftSidePaint) {
    if (leftSidePaint != this.leftSidePaint) {
      WallPaint oldLeftSidePaint = this.leftSidePaint;
      this.leftSidePaint = leftSidePaint;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_PAINT.name(), oldLeftSidePaint, leftSidePaint);
    }
  }

  /**
   * Returns whether the left side is colored, textured or unknown painted.
   * @return {@link WallPaint#COLORED}, {@link WallPaint#TEXTURED} or <code>null</code>
   */
  public WallPaint getLeftSidePaint() {
    return this.leftSidePaint;
  }

  /**
   * Sets the edited left side shininess.
   */
  public void setLeftSideShininess(Float leftSideShininess) {
    if (leftSideShininess != this.leftSideShininess) {
      Float oldLeftSideShininess = this.leftSideShininess;
      this.leftSideShininess = leftSideShininess;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_SHININESS.name(), oldLeftSideShininess, leftSideShininess);
    }
  }

  /**
   * Returns the edited left side shininess.
   */
  public Float getLeftSideShininess() {
    return this.leftSideShininess;
  }

  /**
   * Sets the edited color of the right side.
   */
  public void setRightSideColor(Integer rightSideColor) {
    if (rightSideColor != this.rightSideColor) {
      Integer oldRightSideColor = this.rightSideColor;
      this.rightSideColor = rightSideColor;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_COLOR.name(), oldRightSideColor, rightSideColor);

      setRightSidePaint(WallPaint.COLORED);
    }
  }

  /**
   * Returns the edited color of the right side.
   */
  public Integer getRightSideColor() {
    return this.rightSideColor;
  }

  /**
   * Sets whether the right side is colored, textured or unknown painted.
   */
  public void setRightSidePaint(WallPaint rightSidePaint) {
    if (rightSidePaint != this.rightSidePaint) {
      WallPaint oldRightSidePaint = this.rightSidePaint;
      this.rightSidePaint = rightSidePaint;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_PAINT.name(), oldRightSidePaint, rightSidePaint);
    }
  }

  /**
   * Returns whether the right side is colored, textured or unknown painted.
   * @return {@link WallPaint#COLORED}, {@link WallPaint#TEXTURED} or <code>null</code>
   */
  public WallPaint getRightSidePaint() {
    return this.rightSidePaint;
  }

  /**
   * Sets the edited right side shininess.
   */
  public void setRightSideShininess(Float rightSideShininess) {
    if (rightSideShininess != this.rightSideShininess) {
      Float oldRightSideShininess = this.rightSideShininess;
      this.rightSideShininess = rightSideShininess;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_SHININESS.name(), oldRightSideShininess, rightSideShininess);
    }
  }

  /**
   * Returns the edited right side shininess.
   */
  public Float getRightSideShininess() {
    return this.rightSideShininess;
  }

  /**
   * Sets the pattern of edited wall in plan, and notifies
   * listeners of this change.
   */
  public void setPattern(TextureImage pattern) {
    if (this.pattern != pattern) {
      TextureImage oldPattern = this.pattern;
      this.pattern = pattern;
      this.propertyChangeSupport.firePropertyChange(Property.PATTERN.name(), oldPattern, pattern);
    }
  }

  /**
   * Returns the pattern of edited wall in plan.
   */
  public TextureImage getPattern() {
    return this.pattern;
  }

  /**
   * Sets the edited top color in the 3D view.
   */
  public void setTopColor(Integer topColor) {
    if (topColor != this.topColor) {
      Integer oldTopColor = this.topColor;
      this.topColor = topColor;
      this.propertyChangeSupport.firePropertyChange(Property.TOP_COLOR.name(), oldTopColor, topColor);
    }
  }

  /**
   * Returns the edited top color in the 3D view.
   */
  public Integer getTopColor() {
    return this.topColor;
  }

  /**
   * Sets whether the top of the wall in the 3D view uses default rendering, is colored, or unknown painted.
   */
  public void setTopPaint(WallPaint topPaint) {
    if (topPaint != this.topPaint) {
      WallPaint oldTopPaint = this.topPaint;
      this.topPaint = topPaint;
      this.propertyChangeSupport.firePropertyChange(Property.TOP_PAINT.name(), oldTopPaint, topPaint);
    }
  }

  /**
   * Returns whether the top of the wall in the 3D view uses default rendering, is colored, or unknown painted.
   * @return {@link WallPaint#DEFAULT}, {@link WallPaint#COLORED} or <code>null</code>
   */
  public WallPaint getTopPaint() {
    return this.topPaint;
  }

  /**
   * Sets whether the edited wall is a rectangular wall, a sloping wall or unknown.
   */
  public void setShape(WallShape shape) {
    if (shape != this.shape) {
      WallShape oldShape = this.shape;
      this.shape = shape;
      this.propertyChangeSupport.firePropertyChange(Property.SHAPE.name(), oldShape, shape);

      if (shape == WallShape.RECTANGULAR_WALL) {
        if (this.rectangularWallHeight != null) {
          getLeftSideBaseboardController().setMaxHeight(this.rectangularWallHeight);
          getRightSideBaseboardController().setMaxHeight(this.rectangularWallHeight);
        }
      } else if (shape == WallShape.SLOPING_WALL) {
        if (this.slopingWallHeightAtStart != null
            && this.sloppingWallHeightAtEnd != null) {
          float baseboardMaxHeight = Math.max(this.sloppingWallHeightAtEnd, this.slopingWallHeightAtStart);
          getLeftSideBaseboardController().setMaxHeight(baseboardMaxHeight);
          getRightSideBaseboardController().setMaxHeight(baseboardMaxHeight);
        } else if (this.slopingWallHeightAtStart != null) {
          getLeftSideBaseboardController().setMaxHeight(this.slopingWallHeightAtStart);
          getRightSideBaseboardController().setMaxHeight(this.slopingWallHeightAtStart);
        } else if (this.sloppingWallHeightAtEnd != null) {
          getLeftSideBaseboardController().setMaxHeight(this.sloppingWallHeightAtEnd);
          getRightSideBaseboardController().setMaxHeight(this.sloppingWallHeightAtEnd);
        }
      }
    }
  }

  /**
   * Returns whether the edited wall is a rectangular wall, a sloping wall or unknown.
   */
  public WallShape getShape() {
    return this.shape;
  }

  /**
   * Sets the edited height of a rectangular wall.
   */
  public void setRectangularWallHeight(Float rectangularWallHeight) {
    if (rectangularWallHeight != this.rectangularWallHeight) {
      Float oldRectangularWallHeight = this.rectangularWallHeight;
      this.rectangularWallHeight = rectangularWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.RECTANGULAR_WALL_HEIGHT.name(),
          oldRectangularWallHeight, rectangularWallHeight);

      setShape(WallShape.RECTANGULAR_WALL);
      if (rectangularWallHeight != null) {
        getLeftSideBaseboardController().setMaxHeight(rectangularWallHeight);
        getRightSideBaseboardController().setMaxHeight(rectangularWallHeight);
      }
    }
  }

  /**
   * Returns the edited height of a rectangular wall.
   */
  public Float getRectangularWallHeight() {
    return this.rectangularWallHeight;
  }

  /**
   * Sets the edited height at start of a sloping wall.
   */
  public void setSlopingWallHeightAtStart(Float slopingWallHeightAtStart) {
    if (slopingWallHeightAtStart != this.slopingWallHeightAtStart) {
      Float oldSlopingHeightHeightAtStart = this.slopingWallHeightAtStart;
      this.slopingWallHeightAtStart = slopingWallHeightAtStart;
      this.propertyChangeSupport.firePropertyChange(Property.SLOPING_WALL_HEIGHT_AT_START.name(),
          oldSlopingHeightHeightAtStart, slopingWallHeightAtStart);

      setShape(WallShape.SLOPING_WALL);
      if (slopingWallHeightAtStart != null) {
        float baseboardMaxHeight = this.sloppingWallHeightAtEnd != null
            ? Math.max(this.sloppingWallHeightAtEnd, slopingWallHeightAtStart)
            : slopingWallHeightAtStart;
        baseboardMaxHeight = Math.max(baseboardMaxHeight, this.preferences.getLengthUnit().getMinimumLength());
        getLeftSideBaseboardController().setMaxHeight(baseboardMaxHeight);
        getRightSideBaseboardController().setMaxHeight(baseboardMaxHeight);
      }
    }
  }

  /**
   * Returns the edited height at start of a sloping wall.
   */
  public Float getSlopingWallHeightAtStart() {
    return this.slopingWallHeightAtStart;
  }

  /**
   * Sets the edited height at end of a sloping wall.
   */
  public void setSlopingWallHeightAtEnd(Float sloppingWallHeightAtEnd) {
    if (sloppingWallHeightAtEnd != this.sloppingWallHeightAtEnd) {
      Float oldSlopingWallHeightAtEnd = this.sloppingWallHeightAtEnd;
      this.sloppingWallHeightAtEnd = sloppingWallHeightAtEnd;
      this.propertyChangeSupport.firePropertyChange(Property.SLOPING_WALL_HEIGHT_AT_END.name(),
          oldSlopingWallHeightAtEnd, sloppingWallHeightAtEnd);

      setShape(WallShape.SLOPING_WALL);
      if (sloppingWallHeightAtEnd != null) {
        float baseboardMaxHeight = this.slopingWallHeightAtStart != null
            ? Math.max(this.slopingWallHeightAtStart, sloppingWallHeightAtEnd)
            : sloppingWallHeightAtEnd;
        baseboardMaxHeight = Math.max(baseboardMaxHeight, this.preferences.getLengthUnit().getMinimumLength());
        getLeftSideBaseboardController().setMaxHeight(baseboardMaxHeight);
        getRightSideBaseboardController().setMaxHeight(baseboardMaxHeight);
      }
    }
  }

  /**
   * Returns the edited height at end of a sloping wall.
   */
  public Float getSlopingWallHeightAtEnd() {
    return this.sloppingWallHeightAtEnd;
  }

  /**
   * Sets the edited thickness.
   */
  public void setThickness(Float thickness) {
    if (thickness != this.thickness) {
      Float oldThickness = this.thickness;
      this.thickness = thickness;
      this.propertyChangeSupport.firePropertyChange(Property.THICKNESS.name(), oldThickness, thickness);
    }
  }

  /**
   * Returns the edited thickness.
   */
  public Float getThickness() {
    return this.thickness;
  }

  /**
   * Sets the edited arc extent.
   */
  public void setArcExtentInDegrees(Float arcExtentInDegrees) {
    if (arcExtentInDegrees != this.arcExtentInDegrees) {
      Float oldArcExtent = this.arcExtentInDegrees;
      this.arcExtentInDegrees = arcExtentInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.ARC_EXTENT_IN_DEGREES.name(), oldArcExtent, arcExtentInDegrees);
    }
  }

  /**
   * Returns the edited arc extent.
   */
  public Float getArcExtentInDegrees() {
    return this.arcExtentInDegrees;
  }

  /**
   * Returns the length of wall after applying the edited arc extent.
   * @return the arc length or null if data is missing to compute it
   * @since 6.0
   */
  public Float getArcLength() {
    Float xStart = getXStart();
    Float yStart = getYStart();
    Float xEnd = getXEnd();
    Float yEnd = getYEnd();
    Float arcExtentInDegrees = getArcExtentInDegrees();
    if (xStart != null && yStart != null && xEnd != null && yEnd != null && arcExtentInDegrees != null) {
      Wall wall = new Wall(xStart, yStart, xEnd, yEnd, 0.00001f, 0);
      wall.setArcExtent((float)Math.toRadians(arcExtentInDegrees));
      return wall.getLength();
    } else {
      return null;
    }
  }

  /**
   * Controls the modification of selected walls in edited home.
   */
  public void modifyWalls() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<Wall> selectedWalls = Home.getWallsSubList(oldSelection);
    if (!selectedWalls.isEmpty()) {
      Float xStart = getXStart();
      Float yStart = getYStart();
      Float xEnd = getXEnd();
      Float yEnd = getYEnd();
      WallPaint leftSidePaint = getLeftSidePaint();
      Integer leftSideColor = leftSidePaint == WallPaint.COLORED
          ? getLeftSideColor() : null;
      HomeTexture leftSideTexture = leftSidePaint == WallPaint.TEXTURED
          ? getLeftSideTextureController().getTexture() : null;
      Float leftSideShininess = getLeftSideShininess();
      Boolean leftSideBaseboardVisible = getLeftSideBaseboardController().getVisible();
      Float leftSideBaseboardThickness = getLeftSideBaseboardController().getThickness();
      Float leftSideBaseboardHeight = getLeftSideBaseboardController().getHeight();
      BaseboardChoiceController.BaseboardPaint leftSideBaseboardPaint = getLeftSideBaseboardController().getPaint();
      Integer leftSideBaseboardColor = leftSideBaseboardPaint == BaseboardChoiceController.BaseboardPaint.COLORED
          ? getLeftSideBaseboardController().getColor() : null;
      HomeTexture leftSideBaseboardTexture = leftSideBaseboardPaint == BaseboardChoiceController.BaseboardPaint.TEXTURED
          ? getLeftSideBaseboardController().getTextureController().getTexture()
          : null;
      WallPaint rightSidePaint = getRightSidePaint();
      Integer rightSideColor = rightSidePaint == WallPaint.COLORED
          ? getRightSideColor() : null;
      HomeTexture rightSideTexture = rightSidePaint == WallPaint.TEXTURED
          ? getRightSideTextureController().getTexture() : null;
      Float rightSideShininess = getRightSideShininess();
      Boolean rightSideBaseboardVisible = getRightSideBaseboardController().getVisible();
      Float rightSideBaseboardThickness = getRightSideBaseboardController().getThickness();
      Float rightSideBaseboardHeight = getRightSideBaseboardController().getHeight();
      BaseboardChoiceController.BaseboardPaint rightSideBaseboardPaint = getRightSideBaseboardController().getPaint();
      Integer rightSideBaseboardColor = rightSideBaseboardPaint == BaseboardChoiceController.BaseboardPaint.COLORED
          ? getRightSideBaseboardController().getColor() : null;
      HomeTexture rightSideBaseboardTexture = rightSideBaseboardPaint == BaseboardChoiceController.BaseboardPaint.TEXTURED
          ? getRightSideBaseboardController().getTextureController().getTexture()
          : null;
      TextureImage pattern = getPattern();
      boolean modifiedTopColor = getTopPaint() != null;
      Integer topColor = getTopPaint() == WallPaint.COLORED
          ? getTopColor() : null;
      Float thickness = getThickness();
      Float arcExtent = getArcExtentInDegrees();
      if (arcExtent != null) {
        arcExtent = (float)Math.toRadians(arcExtent);
      }
      Float height;
      if (getShape() == WallShape.SLOPING_WALL) {
        height = getSlopingWallHeightAtStart();
      } else if (getShape() == WallShape.RECTANGULAR_WALL) {
        height = getRectangularWallHeight();
      } else {
        height = null;
      }
      Float heightAtEnd;
      if (getShape() == WallShape.SLOPING_WALL) {
        heightAtEnd = getSlopingWallHeightAtEnd();
      } else if (getShape() == WallShape.RECTANGULAR_WALL) {
        heightAtEnd = getRectangularWallHeight();
      } else {
        heightAtEnd = null;
      }

      if (height != null && heightAtEnd != null) {
        float maxHeight = Math.max(height, heightAtEnd);
        if (leftSideBaseboardHeight != null) {
          leftSideBaseboardHeight = Math.min(leftSideBaseboardHeight, maxHeight);
        }
        if (rightSideBaseboardHeight != null) {
          rightSideBaseboardHeight = Math.min(rightSideBaseboardHeight, maxHeight);
        }
      }

      // Create an array of modified walls with their current properties values
      ModifiedWall [] modifiedWalls = new ModifiedWall [selectedWalls.size()];
      for (int i = 0; i < modifiedWalls.length; i++) {
        modifiedWalls [i] = new ModifiedWall(selectedWalls.get(i));
      }
      // Apply modification
      doModifyWalls(modifiedWalls,
          this.preferences.getNewWallBaseboardThickness(), this.preferences.getNewWallBaseboardHeight(),
          xStart, yStart, xEnd, yEnd,
          leftSidePaint, leftSideColor, leftSideTexture, leftSideShininess,
          leftSideBaseboardVisible, leftSideBaseboardThickness, leftSideBaseboardHeight,
          leftSideBaseboardPaint,  leftSideBaseboardColor, leftSideBaseboardTexture,
          rightSidePaint, rightSideColor, rightSideTexture, rightSideShininess,
          rightSideBaseboardVisible, rightSideBaseboardThickness, rightSideBaseboardHeight,
          rightSideBaseboardPaint, rightSideBaseboardColor, rightSideBaseboardTexture,
          pattern, modifiedTopColor, topColor,
          height, heightAtEnd, thickness, arcExtent);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new WallsModificationUndoableEdit(this.home,
            this.preferences, oldSelection, modifiedWalls,
            this.preferences.getNewWallBaseboardThickness(), this.preferences.getNewWallBaseboardHeight(),
            xStart, yStart, xEnd, yEnd,
            leftSidePaint, leftSideColor, leftSideTexture, leftSideShininess,
            leftSideBaseboardVisible, leftSideBaseboardThickness, leftSideBaseboardHeight,
            leftSideBaseboardPaint, leftSideBaseboardColor, leftSideBaseboardTexture,
            rightSidePaint, rightSideColor, rightSideTexture, rightSideShininess,
            rightSideBaseboardVisible, rightSideBaseboardThickness, rightSideBaseboardHeight,
            rightSideBaseboardPaint, rightSideBaseboardColor, rightSideBaseboardTexture,
            pattern, modifiedTopColor, topColor,
            height, heightAtEnd, thickness, arcExtent);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Undoable edit for walls modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class WallsModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final UserPreferences  preferences;
    private final List<Selectable> oldSelection;
    private final ModifiedWall []  modifiedWalls;
    private final float            newWallBaseboardThickness;
    private final float            newWallBaseboardHeight;
    private final Float            xStart;
    private final Float            yStart;
    private final Float            xEnd;
    private final Float            yEnd;
    private final WallPaint        leftSidePaint;
    private final Integer          leftSideColor;
    private final HomeTexture      leftSideTexture;
    private final Float            leftSideShininess;
    private final Boolean          leftSideBaseboardVisible;
    private final Float            leftSideBaseboardThickness;
    private final Float            leftSideBaseboardHeight;
    private final BaseboardChoiceController.BaseboardPaint leftSideBaseboardPaint;
    private final Integer          leftSideBaseboardColor;
    private final HomeTexture      leftSideBaseboardTexture;
    private final WallPaint        rightSidePaint;
    private final Integer          rightSideColor;
    private final HomeTexture      rightSideTexture;
    private final Float            rightSideShininess;
    private final Boolean          rightSideBaseboardVisible;
    private final Float            rightSideBaseboardThickness;
    private final Float            rightSideBaseboardHeight;
    private final BaseboardChoiceController.BaseboardPaint rightSideBaseboardPaint;
    private final Integer          rightSideBaseboardColor;
    private final HomeTexture      rightSideBaseboardTexture;
    private final TextureImage     pattern;
    private final boolean          modifiedTopColor;
    private final Integer          topColor;
    private final Float            height;
    private final Float            heightAtEnd;
    private final Float            thickness;
    private final Float            arcExtent;

    private WallsModificationUndoableEdit(Home home,
                                          UserPreferences preferences,
                                          List<Selectable> oldSelection, ModifiedWall [] modifiedWalls,
                                          float newWallBaseboardThickness, float newWallBaseboardHeight,
                                          Float xStart, Float yStart, Float xEnd, Float yEnd,
                                          WallPaint leftSidePaint, Integer leftSideColor,
                                          HomeTexture leftSideTexture, Float leftSideShininess,
                                          Boolean leftSideBaseboardVisible, Float leftSideBaseboardThickness, Float leftSideBaseboardHeight,
                                          BaseboardChoiceController.BaseboardPaint leftSideBaseboardPaint, Integer leftSideBaseboardColor, HomeTexture leftSideBaseboardTexture,
                                          WallPaint rightSidePaint, Integer rightSideColor,
                                          HomeTexture rightSideTexture, Float rightSideShininess,
                                          Boolean rightSideBaseboardVisible, Float rightSideBaseboardThickness, Float rightSideBaseboardHeight,
                                          BaseboardChoiceController.BaseboardPaint rightSideBaseboardPaint, Integer rightSideBaseboardColor, HomeTexture rightSideBaseboardTexture,
                                          TextureImage pattern,
                                          boolean modifiedTopColor,
                                          Integer topColor,
                                          Float height,
                                          Float heightAtEnd,
                                          Float thickness,
                                          Float arcExtent) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.modifiedWalls = modifiedWalls;
      this.newWallBaseboardThickness = newWallBaseboardThickness;
      this.newWallBaseboardHeight = newWallBaseboardHeight;
      this.xStart = xStart;
      this.yStart = yStart;
      this.xEnd = xEnd;
      this.yEnd = yEnd;
      this.leftSidePaint = leftSidePaint;
      this.leftSideColor = leftSideColor;
      this.leftSideShininess = leftSideShininess;
      this.leftSideBaseboardVisible = leftSideBaseboardVisible;
      this.leftSideBaseboardThickness = leftSideBaseboardThickness;
      this.leftSideBaseboardHeight = leftSideBaseboardHeight;
      this.leftSideBaseboardPaint = leftSideBaseboardPaint;
      this.leftSideBaseboardColor = leftSideBaseboardColor;
      this.leftSideBaseboardTexture = leftSideBaseboardTexture;
      this.rightSidePaint = rightSidePaint;
      this.rightSideColor = rightSideColor;
      this.rightSideTexture = rightSideTexture;
      this.leftSideTexture = leftSideTexture;
      this.rightSideShininess = rightSideShininess;
      this.rightSideBaseboardVisible = rightSideBaseboardVisible;
      this.rightSideBaseboardThickness = rightSideBaseboardThickness;
      this.rightSideBaseboardHeight = rightSideBaseboardHeight;
      this.rightSideBaseboardPaint = rightSideBaseboardPaint;
      this.rightSideBaseboardColor = rightSideBaseboardColor;
      this.rightSideBaseboardTexture = rightSideBaseboardTexture;
      this.pattern = pattern;
      this.modifiedTopColor = modifiedTopColor;
      this.topColor = topColor;
      this.height = height;
      this.heightAtEnd = heightAtEnd;
      this.thickness = thickness;
      this.arcExtent = arcExtent;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyWalls(this.modifiedWalls);
      this.home.setSelectedItems(this.oldSelection);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyWalls(this.modifiedWalls, this.newWallBaseboardThickness, this.newWallBaseboardHeight,
          this.xStart, this.yStart, this.xEnd, this.yEnd,
          this.leftSidePaint, this.leftSideColor, this.leftSideTexture, this.leftSideShininess,
          this.leftSideBaseboardVisible, this.leftSideBaseboardThickness, this.leftSideBaseboardHeight,
          this.leftSideBaseboardPaint, this.leftSideBaseboardColor, this.leftSideBaseboardTexture,
          this.rightSidePaint, this.rightSideColor, this.rightSideTexture, this.rightSideShininess,
          this.rightSideBaseboardVisible, this.rightSideBaseboardThickness, this.rightSideBaseboardHeight,
          this.rightSideBaseboardPaint, this.rightSideBaseboardColor, this.rightSideBaseboardTexture,
          this.pattern, this.modifiedTopColor, this.topColor,
          this.height, this.heightAtEnd, this.thickness, this.arcExtent);
      this.home.setSelectedItems(this.oldSelection);
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(WallController.class, "undoModifyWallsName");
    }
  }

  /**
   * Modifies walls properties with the values in parameter.
   */
  private static void doModifyWalls(ModifiedWall [] modifiedWalls, float newWallBaseboardThickness, float newWallBaseboardHeight,
                                    Float xStart, Float yStart, Float xEnd, Float yEnd,
                                    WallPaint leftSidePaint, Integer leftSideColor, HomeTexture leftSideTexture, Float leftSideShininess,
                                    Boolean leftSideBaseboardVisible, Float leftSideBaseboardThickness, Float leftSideBaseboardHeight,
                                    BaseboardChoiceController.BaseboardPaint leftSideBaseboardPaint, Integer leftSideBaseboardColor, HomeTexture leftSideBaseboardTexture,
                                    WallPaint rightSidePaint, Integer rightSideColor, HomeTexture rightSideTexture, Float rightSideShininess,
                                    Boolean rightSideBaseboardVisible, Float rightSideBaseboardThickness, Float rightSideBaseboardHeight,
                                    BaseboardChoiceController.BaseboardPaint rightSideBaseboardPaint, Integer rightSideBaseboardColor, HomeTexture rightSideBaseboardTexture,
                                    TextureImage pattern, boolean modifiedTopColor, Integer topColor,
                                    Float height, Float heightAtEnd, Float thickness, Float arcExtent) {
    for (ModifiedWall modifiedWall : modifiedWalls) {
      Wall wall = modifiedWall.getWall();
      moveWallPoints(wall, xStart, yStart, xEnd, yEnd);
      if (leftSidePaint != null) {
        switch (leftSidePaint) {
          case DEFAULT :
            wall.setLeftSideColor(null);
            wall.setLeftSideTexture(null);
            break;
          case COLORED :
            if (leftSideColor != null) {
              wall.setLeftSideColor(leftSideColor);
            }
            wall.setLeftSideTexture(null);
            break;
          case TEXTURED :
            wall.setLeftSideColor(null);
            if (leftSideTexture != null) {
              wall.setLeftSideTexture(leftSideTexture);
            }
            break;
        }
      }
      if (leftSideShininess != null) {
        wall.setLeftSideShininess(leftSideShininess);
      }
      if (leftSideBaseboardVisible == Boolean.FALSE) {
        wall.setLeftSideBaseboard(null);
      } else {
        Baseboard baseboard = wall.getLeftSideBaseboard();
        if (leftSideBaseboardVisible == Boolean.TRUE
            || baseboard != null) {
          float baseboardThickness = baseboard != null
              ? baseboard.getThickness()
              : newWallBaseboardThickness;
          float baseboardHeight = baseboard != null
              ? baseboard.getHeight()
              : newWallBaseboardHeight;
          Integer baseboardColor = baseboard != null
              ? baseboard.getColor()
              : null;
          HomeTexture baseboardTexture = baseboard != null
              ? baseboard.getTexture()
              : null;
          if (leftSideBaseboardPaint != null) {
            switch (leftSideBaseboardPaint) {
              case DEFAULT :
                baseboardColor = null;
                baseboardTexture = null;
                break;
              case COLORED :
                if (leftSideBaseboardColor != null) {
                  baseboardColor = leftSideBaseboardColor;
                }
                baseboardTexture = null;
                break;
              case TEXTURED :
                baseboardColor = null;
                if (leftSideBaseboardTexture != null) {
                  baseboardTexture = leftSideBaseboardTexture;
                }
                break;
            }
          }
          wall.setLeftSideBaseboard(Baseboard.getInstance(
              leftSideBaseboardThickness != null
                  ? leftSideBaseboardThickness
                  : baseboardThickness,
              leftSideBaseboardHeight != null
                  ? leftSideBaseboardHeight
                  : baseboardHeight,
              baseboardColor, baseboardTexture));
        }
      }
      if (rightSidePaint != null) {
        switch (rightSidePaint) {
          case DEFAULT :
            wall.setRightSideColor(null);
            wall.setRightSideTexture(null);
            break;
          case COLORED :
            if (rightSideColor != null) {
              wall.setRightSideColor(rightSideColor);
            }
            wall.setRightSideTexture(null);
            break;
          case TEXTURED :
            wall.setRightSideColor(null);
            if (rightSideTexture != null) {
              wall.setRightSideTexture(rightSideTexture);
            }
            break;
        }
      }
      if (rightSideShininess != null) {
        wall.setRightSideShininess(rightSideShininess);
      }
      if (rightSideBaseboardVisible == Boolean.FALSE) {
        wall.setRightSideBaseboard(null);
      } else {
        Baseboard baseboard = wall.getRightSideBaseboard();
        if (rightSideBaseboardVisible == Boolean.TRUE
            || baseboard != null) {
          float baseboardThickness = baseboard != null
              ? baseboard.getThickness()
              : newWallBaseboardThickness;
          float baseboardHeight = baseboard != null
              ? baseboard.getHeight()
              : newWallBaseboardHeight;
          Integer baseboardColor = baseboard != null
              ? baseboard.getColor()
              : null;
          HomeTexture baseboardTexture = baseboard != null
              ? baseboard.getTexture()
              : null;
          if (rightSideBaseboardPaint != null) {
            switch (rightSideBaseboardPaint) {
              case DEFAULT :
                baseboardColor = null;
                baseboardTexture = null;
                break;
              case COLORED :
                if (rightSideBaseboardColor != null) {
                  baseboardColor = rightSideBaseboardColor;
                }
                baseboardTexture = null;
                break;
              case TEXTURED :
                baseboardColor = null;
                if (rightSideBaseboardTexture != null) {
                  baseboardTexture = rightSideBaseboardTexture;
                }
                break;
            }
          }
          wall.setRightSideBaseboard(Baseboard.getInstance(
              rightSideBaseboardThickness != null
                  ? rightSideBaseboardThickness
                  : baseboardThickness,
              rightSideBaseboardHeight != null
                  ? rightSideBaseboardHeight
                  : baseboardHeight,
              baseboardColor, baseboardTexture));
        }
      }
      if (pattern != null) {
        wall.setPattern(pattern);
      }
      if (modifiedTopColor) {
        wall.setTopColor(topColor);
      }
      if (height != null) {
        wall.setHeight(height);
        if (heightAtEnd != null) {
          if (heightAtEnd.equals(height)) {
            wall.setHeightAtEnd(null);
          } else {
            wall.setHeightAtEnd(heightAtEnd);
          }
        }
      }
      if (thickness != null) {
        wall.setThickness(thickness.floatValue());
      }
      if (arcExtent != null) {
        if (arcExtent.floatValue() == 0) {
          wall.setArcExtent(null);
        } else {
          wall.setArcExtent(arcExtent);
        }
      }
    }
  }

  /**
   * Restores wall properties from the values stored in <code>modifiedWalls</code>.
   */
  private static void undoModifyWalls(ModifiedWall [] modifiedWalls) {
    for (ModifiedWall modifiedWall : modifiedWalls) {
      Wall wall = modifiedWall.getWall();
      moveWallPoints(wall, modifiedWall.getXStart(), modifiedWall.getYStart(),
          modifiedWall.getXEnd(), modifiedWall.getYEnd());
      wall.setLeftSideColor(modifiedWall.getLeftSideColor());
      wall.setLeftSideTexture(modifiedWall.getLeftSideTexture());
      wall.setLeftSideShininess(modifiedWall.getLeftSideShininess());
      wall.setLeftSideBaseboard(modifiedWall.getLeftSideBaseboard());
      wall.setRightSideColor(modifiedWall.getRightSideColor());
      wall.setRightSideTexture(modifiedWall.getRightSideTexture());
      wall.setRightSideShininess(modifiedWall.getRightSideShininess());
      wall.setRightSideBaseboard(modifiedWall.getRightSideBaseboard());
      wall.setPattern(modifiedWall.getPattern());
      wall.setTopColor(modifiedWall.getTopColor());
      wall.setHeight(modifiedWall.getHeight());
      wall.setHeightAtEnd(modifiedWall.getHeightAtEnd());
      wall.setThickness(modifiedWall.getThickness());
      wall.setArcExtent(modifiedWall.getArcExtent());
    }
  }

  private static void moveWallPoints(Wall wall, Float xStart, Float yStart, Float xEnd, Float yEnd) {
    Wall wallAtStart = wall.getWallAtStart();
    if (xStart != null) {
      wall.setXStart(xStart);
      // If wall is joined to a wall at its start
      if (wallAtStart != null) {
        // Move the wall start point or end point
        if (wallAtStart.getWallAtStart() == wall) {
          wallAtStart.setXStart(xStart);
        } else if (wallAtStart.getWallAtEnd() == wall) {
          wallAtStart.setXEnd(xStart);
        }
      }
    }
    if (yStart != null) {
      wall.setYStart(yStart);
      // If wall is joined to a wall at its start
      if (wallAtStart != null) {
        // Move the wall start point or end point
        if (wallAtStart.getWallAtStart() == wall) {
          wallAtStart.setYStart(yStart);
        } else if (wallAtStart.getWallAtEnd() == wall) {
          wallAtStart.setYEnd(yStart);
        }
      }
    }
    Wall wallAtEnd = wall.getWallAtEnd();
    if (xEnd != null) {
      wall.setXEnd(xEnd);
      // If wall is joined to a wall at its end
      if (wallAtEnd != null) {
        // Move the wall start point or end point
        if (wallAtEnd.getWallAtStart() == wall) {
          wallAtEnd.setXStart(xEnd);
        } else if (wallAtEnd.getWallAtEnd() == wall) {
          wallAtEnd.setXEnd(xEnd);
        }
      }
    }
    if (yEnd != null) {
      wall.setYEnd(yEnd);
      // If wall is joined to a wall at its end
      if (wallAtEnd != null) {
        // Move the wall start point or end point
        if (wallAtEnd.getWallAtStart() == wall) {
          wallAtEnd.setYStart(yEnd);
        } else if (wallAtEnd.getWallAtEnd() == wall) {
          wallAtEnd.setYEnd(yEnd);
        }
      }
    }
  }

  /**
   * Stores the current properties values of a modified wall.
   */
  private static final class ModifiedWall {
    private final Wall         wall;
    private final float        xStart;
    private final float        yStart;
    private final float        xEnd;
    private final float        yEnd;
    private final Integer      leftSideColor;
    private final HomeTexture  leftSideTexture;
    private final float        leftSideShininess;
    private final Baseboard    leftSideBaseboard;
    private final Integer      rightSideColor;
    private final HomeTexture  rightSideTexture;
    private final float        rightSideShininess;
    private final Baseboard    rightSideBaseboard;
    private final TextureImage pattern;
    private final Integer      topColor;
    private final Float        height;
    private final Float        heightAtEnd;
    private final float        thickness;
    private final Float        arcExtent;

    public ModifiedWall(Wall wall) {
      this.wall = wall;
      this.xStart = wall.getXStart();
      this.yStart = wall.getYStart();
      this.xEnd = wall.getXEnd();
      this.yEnd = wall.getYEnd();
      this.leftSideColor = wall.getLeftSideColor();
      this.leftSideTexture = wall.getLeftSideTexture();
      this.leftSideShininess = wall.getLeftSideShininess();
      this.leftSideBaseboard = wall.getLeftSideBaseboard();
      this.rightSideColor = wall.getRightSideColor();
      this.rightSideTexture = wall.getRightSideTexture();
      this.rightSideShininess = wall.getRightSideShininess();
      this.rightSideBaseboard = wall.getRightSideBaseboard();
      this.pattern = wall.getPattern();
      this.topColor = wall.getTopColor();
      this.height = wall.getHeight();
      this.heightAtEnd = wall.getHeightAtEnd();
      this.thickness = wall.getThickness();
      this.arcExtent = wall.getArcExtent();
    }

    public Wall getWall() {
      return this.wall;
    }

    public float getXStart() {
      return this.xStart;
    }

    public float getXEnd() {
      return this.xEnd;
    }

    public float getYStart() {
      return this.yStart;
    }

    public float getYEnd() {
      return this.yEnd;
    }

    public Float getHeight() {
      return this.height;
    }

    public Float getHeightAtEnd() {
      return this.heightAtEnd;
    }

    public Integer getLeftSideColor() {
      return this.leftSideColor;
    }

    public HomeTexture getLeftSideTexture() {
      return this.leftSideTexture;
    }

    public float getLeftSideShininess() {
      return this.leftSideShininess;
    }

    public Baseboard getLeftSideBaseboard() {
      return this.leftSideBaseboard;
    }

    public Integer getRightSideColor() {
      return this.rightSideColor;
    }

    public HomeTexture getRightSideTexture() {
      return this.rightSideTexture;
    }

    public float getRightSideShininess() {
      return this.rightSideShininess;
    }

    public Baseboard getRightSideBaseboard() {
      return this.rightSideBaseboard;
    }

    public TextureImage getPattern() {
      return this.pattern;
    }

    public Integer getTopColor() {
      return this.topColor;
    }

    public float getThickness() {
      return this.thickness;
    }

    public Float getArcExtent() {
      return this.arcExtent;
    }
  }
}
