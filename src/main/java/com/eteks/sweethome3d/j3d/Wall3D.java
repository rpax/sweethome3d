/*
 * Wall3D.java 23 jan. 09
 *
 * Sweet Home 3D, Copyright (c) 2007-2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.j3d;

import java.awt.EventQueue;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Wall;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Root of wall branch.
 */
public class Wall3D extends Object3DBranch {
  private static final float LEVEL_ELEVATION_SHIFT = 0.1f;
  private static final Area  FULL_FACE_CUT_OUT_AREA = new Area(new Rectangle2D.Float(-0.5f, 0.5f, 1, 1));

  private static final int WALL_LEFT_SIDE  = 0;
  private static final int WALL_RIGHT_SIDE = 1;

  private static Map<HomePieceOfFurniture, ModelRotationTuple> doorOrWindowRotatedModels = new WeakHashMap<HomePieceOfFurniture, ModelRotationTuple>();
  private static Map<ModelRotationTuple, Area>                 rotatedModelsFrontAreas   = new WeakHashMap<ModelRotationTuple, Area>();

  private final Home home;

  /**
   * Creates the 3D wall matching the given home <code>wall</code>.
   */
  public Wall3D(Wall wall, Home home) {
    this(wall, home, false, false);
  }

  /**
   * Creates the 3D wall matching the given home <code>wall</code>.
   */
  public Wall3D(Wall wall, Home home, boolean ignoreDrawingMode,
                boolean waitModelAndTextureLoadingEnd) {
    setUserData(wall);
    this.home = home;

    // Allow wall branch to be removed from its parent
    setCapability(BranchGroup.ALLOW_DETACH);
    // Allow to read branch shape children
    setCapability(BranchGroup.ALLOW_CHILDREN_READ);

    // Add wall bottom, baseboard, main and top shapes to branch for left and right side
    for (int i = 0; i < 8; i++) {
      Group wallSideGroup = new Group();
      wallSideGroup.setCapability(Group.ALLOW_CHILDREN_READ);
      wallSideGroup.addChild(createWallPartShape(false));
      if (!ignoreDrawingMode) {
        // Add wall left and right empty outline shapes to branch
        wallSideGroup.addChild(createWallPartShape(true));
      }
      addChild(wallSideGroup);
    }

    // Set wall shape geometry and appearance
    updateWallGeometry(waitModelAndTextureLoadingEnd);
    updateWallAppearance(waitModelAndTextureLoadingEnd);
  }

  /**
   * Returns a new wall part shape with no geometry
   * and a default appearance with a white material.
   */
  private Node createWallPartShape(boolean outline) {
    Shape3D wallShape = new Shape3D();
    // Allow wall shape to change its geometry
    wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
    wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    wallShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

    Appearance wallAppearance = new Appearance();
    wallShape.setAppearance(wallAppearance);
    wallAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
    TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
    transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
    transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
    wallAppearance.setTransparencyAttributes(transparencyAttributes);
    wallAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    RenderingAttributes renderingAttributes = new RenderingAttributes();
    renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
    wallAppearance.setRenderingAttributes(renderingAttributes);

    if (outline) {
      wallAppearance.setColoringAttributes(Object3DBranch.OUTLINE_COLORING_ATTRIBUTES);
      wallAppearance.setPolygonAttributes(Object3DBranch.OUTLINE_POLYGON_ATTRIBUTES);
      wallAppearance.setLineAttributes(Object3DBranch.OUTLINE_LINE_ATTRIBUTES);
    } else {
      wallAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      wallAppearance.setMaterial(DEFAULT_MATERIAL);
      wallAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
      wallAppearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
      wallAppearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
    }
    return wallShape;
  }

  @Override
  public void update() {
    updateWallGeometry(false);
    updateWallAppearance(false);
  }

  /**
   * Sets the 3D geometry of this wall shapes that matches its 2D geometry.
   */
  private void updateWallGeometry(boolean waitDoorOrWindowModelsLoadingEnd) {
    updateWallSideGeometry(WALL_LEFT_SIDE, waitDoorOrWindowModelsLoadingEnd);
    updateWallSideGeometry(WALL_RIGHT_SIDE, waitDoorOrWindowModelsLoadingEnd);
  }

  private void updateWallSideGeometry(int wallSide,
                                      boolean waitDoorOrWindowModelsLoadingEnd) {
    Wall wall = (Wall)getUserData();
    HomeTexture wallTexture;
    Baseboard baseboard;
    if (wallSide == WALL_LEFT_SIDE) {
      wallTexture = wall.getLeftSideTexture();
      baseboard = wall.getLeftSideBaseboard();
    } else {
      wallTexture = wall.getRightSideTexture();
      baseboard = wall.getRightSideBaseboard();
    }
    Group [] wallSideGroups = {(Group)getChild(wallSide),      // Bottom group    (0 or 1)
                               (Group)getChild(wallSide + 2),  // Baseboard group (2 or 3)
                               (Group)getChild(wallSide + 4),  // Main group      (4 or 5)
                               (Group)getChild(wallSide + 6)}; // Top group       (6 or 7)
    Shape3D [] wallFilledShapes = new Shape3D [wallSideGroups.length];
    Shape3D [] wallOutlineShapes = new Shape3D [wallSideGroups.length];
    int [] currentGeometriesCounts = new int [wallSideGroups.length];
    for (int i = 0; i < wallSideGroups.length; i++) {
      wallFilledShapes  [i] = (Shape3D)wallSideGroups [i].getChild(0);
      wallOutlineShapes [i] = wallSideGroups [i].numChildren() > 1
          ? (Shape3D)wallSideGroups [i].getChild(1)
          : null;
      currentGeometriesCounts [i] = wallFilledShapes [i].numGeometries();
    }
    if (wall.getLevel() == null || wall.getLevel().isViewableAndVisible()) {
      List [] wallGeometries = {new ArrayList<Geometry>(),
                                new ArrayList<Geometry>(),
                                new ArrayList<Geometry>(),
                                new ArrayList<Geometry>()};
      // Create geometries of the wall side
      createWallGeometries(wallGeometries [0], wallGeometries [2], wallGeometries [3], wallSide,
          null, wallTexture, waitDoorOrWindowModelsLoadingEnd);
      if (baseboard != null) {
        HomeTexture baseboardTexture = baseboard.getTexture();
        if (baseboardTexture == null
            && baseboard.getColor() == null) {
          baseboardTexture = wallTexture;
        }
        // Create geometries of its baseboard
        createWallGeometries(wallGeometries [1], wallGeometries [1], wallGeometries [1], wallSide,
            baseboard, baseboardTexture, waitDoorOrWindowModelsLoadingEnd);
      }
      for (int i = 0; i < wallSideGroups.length; i++) {
        for (Geometry wallGeometry : (List<Geometry>)wallGeometries [i]) {
          if (wallGeometry != null) {
            wallFilledShapes [i].addGeometry(wallGeometry);
            if (wallOutlineShapes [i] != null) {
              wallOutlineShapes [i].addGeometry(wallGeometry);
            }
          }
        }
      }
    }
    for (int i = 0; i < wallSideGroups.length; i++) {
      for (int j = currentGeometriesCounts [i] - 1; j >= 0; j--) {
        wallFilledShapes [i].removeGeometry(j);
        if (wallOutlineShapes [i] != null) {
          wallOutlineShapes [i].removeGeometry(j);
        }
      }
    }
  }

  /**
   * Creates <code>wall</code> or baseboard geometries computed with windows or doors
   * that intersect wall.
   */
  private void createWallGeometries(List<Geometry> bottomGeometries,
                                    final List<Geometry> sideGeometries,
                                    final List<Geometry> topGeometries,
                                    final int wallSide,
                                    final Baseboard baseboard,
                                    final HomeTexture texture,
                                    final boolean waitDoorOrWindowModelsLoadingEnd) {
    final Wall wall = (Wall)getUserData();
    Shape wallShape = getShape(wall.getPoints());
    final float [][] wallSidePoints = getWallSidePoints(wallSide);
    Shape wallSideShape = getShape(wallSidePoints);
    final float [][] wallSideOrBaseboardPoints = baseboard == null
        ? wallSidePoints
        : getWallBaseboardPoints(wallSide);
    Shape wallSideOrBaseboardShape = getShape(wallSideOrBaseboardPoints);
    Area wallSideOrBaseboardArea = new Area(wallSideOrBaseboardShape);
    final float [] textureReferencePoint = wallSide == WALL_LEFT_SIDE
        ? wallSideOrBaseboardPoints [0].clone()
        : wallSideOrBaseboardPoints [wallSideOrBaseboardPoints.length - 1].clone();
    final float wallElevation = getWallElevation(baseboard != null);
    float topElevationAtStart;
    float topElevationAtEnd;
    if (baseboard == null) {
      topElevationAtStart = getWallTopElevationAtStart();
      topElevationAtEnd = getWallTopElevationAtEnd();
    } else {
      topElevationAtStart =
      topElevationAtEnd = getBaseboardTopElevation(baseboard);
    }
    float maxTopElevation = Math.max(topElevationAtStart, topElevationAtEnd);

    // Compute wall angles and top line factors
    double wallYawAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), wall.getXEnd() - wall.getXStart());
    final double cosWallYawAngle = Math.cos(wallYawAngle);
    final double sinWallYawAngle = Math.sin(wallYawAngle);
    double wallXStartWithZeroYaw = cosWallYawAngle * wall.getXStart() + sinWallYawAngle * wall.getYStart();
    double wallXEndWithZeroYaw = cosWallYawAngle * wall.getXEnd() + sinWallYawAngle * wall.getYEnd();
    Float arcExtent = wall.getArcExtent();
    boolean roundWall = arcExtent != null && arcExtent.floatValue() != 0;
    final double topLineAlpha;
    final double topLineBeta;
    if (topElevationAtStart == topElevationAtEnd) {
      topLineAlpha = 0;
      topLineBeta = topElevationAtStart;
    } else {
      topLineAlpha = (topElevationAtEnd - topElevationAtStart) / (wallXEndWithZeroYaw - wallXStartWithZeroYaw);
      topLineBeta = topElevationAtStart - topLineAlpha * wallXStartWithZeroYaw;
    }

    // Search which doors or windows intersect with this wall side or its baseboard
    List<DoorOrWindowArea> windowIntersections = new ArrayList<DoorOrWindowArea>();
    List<HomePieceOfFurniture> intersectingDoorOrWindows = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : getVisibleDoorsAndWindows(this.home.getFurniture())) {
      float pieceElevation = piece.getGroundElevation();
      if (pieceElevation + piece.getHeight() > wallElevation
          && pieceElevation < maxTopElevation) {
        Area pieceArea = new Area(getShape(piece.getPoints()));
        Area intersectionArea = new Area(wallShape);
        intersectionArea.intersect(pieceArea);
        if (!intersectionArea.isEmpty()) {
          HomePieceOfFurniture deeperPiece = null;
          if (piece.isParallelToWall(wall)) {
            if (baseboard != null) {
              // Increase piece depth to ensure baseboard will be cut even if the window is as thick as the wall
              deeperPiece = piece.clone();
              deeperPiece.setDepthInPlan(deeperPiece.getDepth() + 2 * baseboard.getThickness());
            }
            if (piece instanceof HomeDoorOrWindow) {
              HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
              if (doorOrWindow.isWallCutOutOnBothSides()) {
                if (deeperPiece == null) {
                  deeperPiece = piece.clone();
                }
                // Increase piece depth to ensure the wall will be cut on both sides
                deeperPiece.setDepthInPlan(deeperPiece.getDepth() + 4 * wall.getThickness());
              }
            }
          }
          // Recompute intersection on wall side shape only
          if (deeperPiece != null) {
            pieceArea = new Area(getShape(deeperPiece.getPoints()));
            intersectionArea = new Area(wallSideOrBaseboardShape);
            intersectionArea.intersect(pieceArea);
          } else {
            intersectionArea = new Area(wallSideShape);
            intersectionArea.intersect(pieceArea);
          }
          if (!intersectionArea.isEmpty()) {
            windowIntersections.add(new DoorOrWindowArea(intersectionArea, Arrays.asList(new HomePieceOfFurniture [] {piece})));
            intersectingDoorOrWindows.add(piece);
            // Remove from wall area the piece shape
            wallSideOrBaseboardArea.subtract(pieceArea);
          }
        }
      }
    }
    // Refine intersections in case some doors or windows are superimposed
    if (windowIntersections.size() > 1) {
      // Search superimposed windows
      for (int windowIndex = 0; windowIndex < windowIntersections.size(); windowIndex++) {
        DoorOrWindowArea windowIntersection = windowIntersections.get(windowIndex);
        List<DoorOrWindowArea> otherWindowIntersections = new ArrayList<DoorOrWindowArea>();
        int otherWindowIndex = 0;
        for (DoorOrWindowArea otherWindowIntersection : windowIntersections) {
          if (windowIntersection.getArea().isEmpty()) {
            break;
          } else if (otherWindowIndex > windowIndex) { // Avoid search twice the intersection between two items
            Area windowsIntersectionArea = new Area(otherWindowIntersection.getArea());
            windowsIntersectionArea.intersect(windowIntersection.getArea());
            if (!windowsIntersectionArea.isEmpty()) {
              // Remove intersection from wall area
              otherWindowIntersection.getArea().subtract(windowsIntersectionArea);
              windowIntersection.getArea().subtract(windowsIntersectionArea);
              // Create a new area for the intersection
              List<HomePieceOfFurniture> doorsOrWindows = new ArrayList<HomePieceOfFurniture>(windowIntersection.getDoorsOrWindows());
              doorsOrWindows.addAll(otherWindowIntersection.getDoorsOrWindows());
              otherWindowIntersections.add(new DoorOrWindowArea(windowsIntersectionArea, doorsOrWindows));
            }
          }
          otherWindowIndex++;
        }
        windowIntersections.addAll(otherWindowIntersections);
      }
    }
    List<float[]> points = new ArrayList<float[]>(4);
    // Generate geometry for each wall part that doesn't contain a window
    float [] previousPoint = null;
    for (PathIterator it = wallSideOrBaseboardArea.getPathIterator(null); !it.isDone(); it.next()) {
      float [] wallPoint = new float[2];
      if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
        if (points.size() > 2) {
          // Remove last point if it's equal to first point
          if (Arrays.equals(points.get(0), points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
          }
          if (points.size() > 2) {
            float [][] wallPartPoints = points.toArray(new float[points.size()][]);
            // Compute geometry for vertical part
            sideGeometries.add(createVerticalPartGeometry(wall, wallPartPoints, wallElevation,
                cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, baseboard, texture,
                textureReferencePoint, wallSide));
            // Compute geometry for bottom part
            bottomGeometries.add(createHorizontalPartGeometry(wallPartPoints, wallElevation, true, roundWall));
            // Compute geometry for top part
            topGeometries.add(createTopPartGeometry(wallPartPoints,
                cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, roundWall));
          }
        }
        points.clear();
        previousPoint = null;
      } else if (previousPoint == null
                 || !Arrays.equals(wallPoint, previousPoint)) {
        points.add(wallPoint);
        previousPoint = wallPoint;
      }
    }

    // Generate geometry for each wall part above and below a window
    Level level = wall.getLevel();
    previousPoint = null;
    for (DoorOrWindowArea windowIntersection : windowIntersections) {
      if (!windowIntersection.getArea().isEmpty()) {
        for (PathIterator it = windowIntersection.getArea().getPathIterator(null); !it.isDone(); it.next()) {
          float [] wallPoint = new float[2];
          if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
            // Remove last point if it's equal to first point
            if (Arrays.equals(points.get(0), points.get(points.size() - 1))) {
              points.remove(points.size() - 1);
            }

            if (points.size() > 2) {
              float [][] wallPartPoints = points.toArray(new float[points.size()][]);
              List<HomePieceOfFurniture> doorsOrWindows = windowIntersection.getDoorsOrWindows();
              if (doorsOrWindows.size() > 1) {
                // Sort superimposed doors and windows by elevation and height
                Collections.sort(doorsOrWindows,
                    new Comparator<HomePieceOfFurniture>() {
                      public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
                        float piece1Elevation = piece1.getGroundElevation();
                        float piece2Elevation = piece2.getGroundElevation();
                        if (piece1Elevation < piece2Elevation) {
                          return -1;
                        } else if (piece1Elevation > piece2Elevation) {
                          return 1;
                        } else {
                          return 0;
                        }
                      }
                    });
              }
              HomePieceOfFurniture lowestDoorOrWindow = doorsOrWindows.get(0);
              float lowestDoorOrWindowElevation = lowestDoorOrWindow.getGroundElevation();
              // Generate geometry for wall part below window
              if (lowestDoorOrWindowElevation > wallElevation) {
                if (level != null
                    && level.getElevation() != wallElevation
                    && lowestDoorOrWindow.getElevation() < LEVEL_ELEVATION_SHIFT) {
                  // Give more chance to an overlapping room floor to be displayed
                  lowestDoorOrWindowElevation -= LEVEL_ELEVATION_SHIFT;
                }
                sideGeometries.add(createVerticalPartGeometry(wall, wallPartPoints, wallElevation,
                    cosWallYawAngle, sinWallYawAngle, 0, lowestDoorOrWindowElevation, baseboard, texture,
                    textureReferencePoint, wallSide));
                bottomGeometries.add(createHorizontalPartGeometry(wallPartPoints, wallElevation, true, roundWall));
                sideGeometries.add(createHorizontalPartGeometry(wallPartPoints,
                    lowestDoorOrWindowElevation, false, roundWall));
              }

              // Generate geometry for wall parts between superimposed windows
              for (int i = 0; i < doorsOrWindows.size() - 1; ) {
                HomePieceOfFurniture lowerDoorOrWindow = doorsOrWindows.get(i);
                float lowerDoorOrWindowElevation = lowerDoorOrWindow.getGroundElevation();
                HomePieceOfFurniture higherDoorOrWindow = doorsOrWindows.get(++i);
                float higherDoorOrWindowElevation = higherDoorOrWindow.getGroundElevation();
                // Ignore higher windows smaller than lower window
                while (lowerDoorOrWindowElevation + lowerDoorOrWindow.getHeight() >= higherDoorOrWindowElevation + higherDoorOrWindow.getHeight()
                    && ++i < doorsOrWindows.size()) {
                  higherDoorOrWindow = doorsOrWindows.get(i);
                }
                if (i < doorsOrWindows.size()
                    && lowerDoorOrWindowElevation + lowerDoorOrWindow.getHeight() < higherDoorOrWindowElevation) {
                  sideGeometries.add(createVerticalPartGeometry(wall, wallPartPoints, lowerDoorOrWindowElevation + lowerDoorOrWindow.getHeight(),
                      cosWallYawAngle, sinWallYawAngle, 0, higherDoorOrWindowElevation, baseboard, texture, textureReferencePoint, wallSide));
                  sideGeometries.add(createHorizontalPartGeometry(wallPartPoints,
                      lowerDoorOrWindowElevation + lowerDoorOrWindow.getHeight(), true, roundWall));
                  sideGeometries.add(createHorizontalPartGeometry(wallPartPoints, higherDoorOrWindowElevation, false, roundWall));
                }
              }

              HomePieceOfFurniture highestDoorOrWindow = doorsOrWindows.get(doorsOrWindows.size() - 1);
              float highestDoorOrWindowElevation = highestDoorOrWindow.getGroundElevation();
              for (int i = doorsOrWindows.size() - 2; i >= 0; i--) {
                HomePieceOfFurniture doorOrWindow = doorsOrWindows.get(i);
                if (doorOrWindow.getGroundElevation() + doorOrWindow.getHeight() > highestDoorOrWindowElevation + highestDoorOrWindow.getHeight()) {
                  highestDoorOrWindow = doorOrWindow;
                }
              }
              float doorOrWindowTop = highestDoorOrWindowElevation + highestDoorOrWindow.getHeight();
              boolean generateGeometry = true;
              // Translate points of wall part under doorOrWindowTop along sloping wall top
              for (int i = 0; i < wallPartPoints.length; i++) {
                double xTopPointWithZeroYaw = cosWallYawAngle * wallPartPoints[i][0] + sinWallYawAngle * wallPartPoints[i][1];
                double topPointWithZeroYawElevation = topLineAlpha * xTopPointWithZeroYaw + topLineBeta;
                if (doorOrWindowTop > topPointWithZeroYawElevation) {
                  if (topLineAlpha == 0 || roundWall) {
                    // Ignore geometry above window for flat wall or round sloping wall
                    generateGeometry = false;
                    break;
                  }
                  double translation = (doorOrWindowTop - topPointWithZeroYawElevation) / topLineAlpha;
                  wallPartPoints [i][0] += (float)(translation * cosWallYawAngle);
                  wallPartPoints [i][1] += (float)(translation * sinWallYawAngle);
                }
              }
              // Generate geometry for wall part above window
              if (generateGeometry) {
                sideGeometries.add(createVerticalPartGeometry(wall, wallPartPoints, doorOrWindowTop,
                    cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, baseboard, texture, textureReferencePoint, wallSide));
                sideGeometries.add(createHorizontalPartGeometry(
                    wallPartPoints, doorOrWindowTop, true, roundWall));
                topGeometries.add(createTopPartGeometry(wallPartPoints,
                    cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, roundWall));
              }
            }
            points.clear();
            previousPoint = null;
          } else if (previousPoint == null
                     || !Arrays.equals(wallPoint, previousPoint)) {
            points.add(wallPoint);
            previousPoint = wallPoint;
          }
        }
      }
    }

    // Generate geometry around non rectangular doors and windows placed in straight walls along the same axis
    if (!roundWall && intersectingDoorOrWindows.size() > 0) {
      final double epsilon = Math.PI / 720; // Quarter of a degree
      final ArrayList<HomeDoorOrWindow> missingModels = new ArrayList<HomeDoorOrWindow>(intersectingDoorOrWindows.size());
      // Compute geometry for doors or windows that have a known front area
      for (final HomePieceOfFurniture piece : intersectingDoorOrWindows) {
        if (piece instanceof HomeDoorOrWindow) {
          HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
          if (!PieceOfFurniture.DEFAULT_CUT_OUT_SHAPE.equals(doorOrWindow.getCutOutShape())
              || doorOrWindow.getWallWidth() != 1
              || doorOrWindow.getWallLeft() != 0
              || doorOrWindow.getWallHeight() != 1
              || doorOrWindow.getWallTop() != 0) {
            double angleDifference = Math.abs(wallYawAngle - doorOrWindow.getAngle()) % (2 * Math.PI);
            if (angleDifference < epsilon
                || angleDifference > 2 * Math.PI - epsilon
                || Math.abs(angleDifference - Math.PI) < epsilon) {
              final int frontOrBackSide = Math.abs(angleDifference - Math.PI) < epsilon ? 1 : -1;
              ModelRotationTuple rotatedModel = doorOrWindowRotatedModels.get(doorOrWindow);
              if (rotatedModel != null
                  && (missingModels.size() == 0 || !waitDoorOrWindowModelsLoadingEnd)) {
                createGeometriesSurroundingDoorOrWindow((HomeDoorOrWindow)doorOrWindow, rotatedModelsFrontAreas.get(rotatedModel), frontOrBackSide,
                    wall, sideGeometries, topGeometries,
                    wallSideOrBaseboardPoints, wallElevation, cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta,
                    texture, textureReferencePoint, wallSide);
              } else {
                missingModels.add(doorOrWindow);
              }
            }
          }
        }
      }
      if (missingModels.size() > 0) {
        final ModelManager modelManager = ModelManager.getInstance();
        for (final HomeDoorOrWindow doorOrWindow : (List<HomeDoorOrWindow>)missingModels.clone()) {
          double angleDifference = Math.abs(wallYawAngle - doorOrWindow.getAngle()) % (2 * Math.PI);
          final int frontOrBackSide = Math.abs(angleDifference - Math.PI) < epsilon ? 1 : -1;
          // Load the model of the door or window to compute its front area
          modelManager.loadModel(doorOrWindow.getModel(), waitDoorOrWindowModelsLoadingEnd,
              new ModelManager.ModelObserver() {
                public void modelUpdated(BranchGroup modelRoot) {
                  // Check again whether rotation model key and its front area weren't recently put in cache
                  ModelRotationTuple rotatedModel = doorOrWindowRotatedModels.get(doorOrWindow);
                  Area frontArea;
                  if (rotatedModel == null) {
                    rotatedModel = new ModelRotationTuple(doorOrWindow.getModel(), doorOrWindow.getModelRotation());
                    frontArea = rotatedModelsFrontAreas.get(rotatedModel);
                    if (frontArea == null) {
                      // Add rotated piece model scene to a normalized transform group
                      TransformGroup rotation = new TransformGroup(modelManager.getRotationTransformation(doorOrWindow.getModelRotation()));
                      rotation.addChild(modelRoot);
                      frontArea = modelManager.getFrontArea(doorOrWindow.getCutOutShape(), rotation);
                      // Keep front area for a model with a given rotation in cache to avoid multiple calls to getFrontArea
                      rotatedModelsFrontAreas.put(rotatedModel, frontArea);
                    } else {
                      // As doorOrWindowRotatedModels and rotatedModelsFrontAreas are both WeakHashMap instances,
                      // use the ModelRotationTuple key that already exists in rotatedModelsFrontAreas
                      // to avoid the deletion of the entry containing the new sibling when doorOrWindow is garbage collected
                      for (ModelRotationTuple key : rotatedModelsFrontAreas.keySet()) {
                        if (key.equals(rotatedModel)) {
                          rotatedModel = key;
                          break;
                        }
                      }
                    }
                    // Keep rotated model key in cache for future updates
                    doorOrWindowRotatedModels.put(doorOrWindow, rotatedModel);
                  } else {
                    frontArea = rotatedModelsFrontAreas.get(rotatedModel);
                  }
                  if (waitDoorOrWindowModelsLoadingEnd) {
                    createGeometriesSurroundingDoorOrWindow(doorOrWindow, frontArea, frontOrBackSide,
                        wall, sideGeometries, topGeometries,
                        wallSideOrBaseboardPoints, wallElevation, cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta,
                        texture, textureReferencePoint, wallSide);
                  } else {
                    missingModels.remove(doorOrWindow);
                    if (missingModels.size() == 0
                        && baseboard == null) {
                      EventQueue.invokeLater(new Runnable() {
                          public void run() {
                            // Request a new update only once all missing models are loaded
                            updateWallSideGeometry(wallSide, waitDoorOrWindowModelsLoadingEnd);
                          }
                        });
                    }
                  }
                }

                public void modelError(Exception ex) {
                  // In case of problem, ignore door or window geometry by using default full face cut out area
                  ModelRotationTuple rotatedModel = new ModelRotationTuple(doorOrWindow.getModel(), doorOrWindow.getModelRotation());
                  doorOrWindowRotatedModels.put(doorOrWindow, rotatedModel);
                  if (rotatedModelsFrontAreas.get(rotatedModel) == null) {
                    rotatedModelsFrontAreas.put(rotatedModel, FULL_FACE_CUT_OUT_AREA);
                  }
                  if (!waitDoorOrWindowModelsLoadingEnd) {
                    missingModels.remove(doorOrWindow);
                  }
                }
              });
        }
      }
    }
  }

  /**
   * Returns all the visible doors and windows in the given <code>furniture</code>.
   */
  private List<HomePieceOfFurniture> getVisibleDoorsAndWindows(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> visibleDoorsAndWindows = new ArrayList<HomePieceOfFurniture>(furniture.size());
    for (HomePieceOfFurniture piece : furniture) {
      if (piece.isVisible()
          && (piece.getLevel() == null
          || piece.getLevel().isViewableAndVisible())) {
        if (piece instanceof HomeFurnitureGroup) {
          visibleDoorsAndWindows.addAll(getVisibleDoorsAndWindows(((HomeFurnitureGroup)piece).getFurniture()));
        } else if (piece.isDoorOrWindow()) {
          visibleDoorsAndWindows.add(piece);
        }
      }
    }
    return visibleDoorsAndWindows;
  }

  /**
   * Returns the points of one of the side of this wall.
   */
  private float [][] getWallSidePoints(int wallSide) {
    Wall wall = (Wall)getUserData();
    float [][] wallPoints = wall.getPoints();

    if (wallSide == WALL_LEFT_SIDE) {
      for (int i = wallPoints.length / 2; i < wallPoints.length; i++) {
        wallPoints [i][0] = (wallPoints [i][0] + wallPoints [wallPoints.length - i - 1][0]) / 2;
        wallPoints [i][1] = (wallPoints [i][1] + wallPoints [wallPoints.length - i - 1][1]) / 2;
      }
    } else { // WALL_RIGHT_SIDE
      for (int i = 0, n = wallPoints.length / 2; i < n; i++) {
        wallPoints [i][0] = (wallPoints [i][0] + wallPoints [wallPoints.length - i - 1][0]) / 2;
        wallPoints [i][1] = (wallPoints [i][1] + wallPoints [wallPoints.length - i - 1][1]) / 2;
      }
    }
    return wallPoints;
  }

  /**
   * Returns the points of one of the baseboard of this wall.
   */
  private float [][] getWallBaseboardPoints(int wallSide) {
    Wall wall = (Wall)getUserData();
    float [][] wallPointsIncludingBaseboards = wall.getPoints(true);
    float [][] wallPoints = wall.getPoints();

    if (wallSide == WALL_LEFT_SIDE) {
      for (int i = wallPointsIncludingBaseboards.length / 2; i < wallPointsIncludingBaseboards.length; i++) {
        wallPointsIncludingBaseboards [i] = wallPoints [wallPoints.length - i - 1];
      }
    } else { // WALL_RIGHT_SIDE
      for (int i = 0, n = wallPoints.length / 2; i < n; i++) {
        wallPointsIncludingBaseboards [i] = wallPoints [wallPoints.length - i - 1];
      }
    }
    return wallPointsIncludingBaseboards;
  }

  /**
   * Returns the vertical rectangles that join each point of <code>points</code>
   * and spread from <code>minElevation</code> to a top line (y = ax + b) described by <code>topLineAlpha</code>
   * and <code>topLineBeta</code> factors in a vertical plan that is rotated around
   * vertical axis matching <code>cosWallYawAngle</code> and <code>sinWallYawAngle</code>.
   */
  private Geometry createVerticalPartGeometry(Wall wall,
                                              float [][] points, float minElevation,
                                              double cosWallYawAngle, double sinWallYawAngle,
                                              double topLineAlpha, double topLineBeta,
                                              Baseboard baseboard, HomeTexture texture,
                                              float [] textureReferencePoint,
                                              int wallSide) {
    final float subpartSize = this.home.getEnvironment().getSubpartSizeUnderLight();
    Float arcExtent = wall.getArcExtent();
    if ((arcExtent == null || arcExtent == 0)
        && subpartSize > 0) {
      // Subdivide points in smaller parts to ensure a smoother effect with point lights
      List<float []> pointsList = new ArrayList<float[]>(points.length * 2);
      pointsList.add(points [0]);
      for (int i = 1; i < points.length; i++) {
        double distance = Point2D.distance(points [i - 1][0], points [i - 1][1], points [i][0], points [i][1]) - subpartSize / 2;
        double angle = Math.atan2(points [i][1] - points [i - 1][1], points [i][0] - points [i - 1][0]);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        for (double d = 0; d < distance; d += subpartSize) {
          pointsList.add(new float [] {(float)(points [i - 1][0] + d * cosAngle), (float)(points [i - 1][1] + d * sinAngle)});
        }
        pointsList.add(points [i]);
      }
      points = pointsList.toArray(new float [pointsList.size()][]);
    }
    // Compute wall coordinates
    Point3f [] bottom = new Point3f [points.length];
    Point3f [] top    = new Point3f [points.length];
    Float   [] pointUCoordinates = new Float [points.length];
    float xStart = wall.getXStart();
    float yStart = wall.getYStart();
    float xEnd = wall.getXEnd();
    float yEnd = wall.getYEnd();
    float [] arcCircleCenter = null;
    float arcCircleRadius = 0;
    float referencePointAngle = 0;
    if (arcExtent != null && arcExtent != 0) {
      arcCircleCenter = new float [] {wall.getXArcCircleCenter(), wall.getYArcCircleCenter()};
      arcCircleRadius = (float)Point2D.distance(arcCircleCenter [0], arcCircleCenter [1],
          xStart, yStart);
      referencePointAngle = (float)Math.atan2(textureReferencePoint [1] - arcCircleCenter [1],
          textureReferencePoint [0] - arcCircleCenter [0]);
    }
    for (int i = 0; i < points.length; i++) {
      bottom [i] = new Point3f(points [i][0], minElevation, points [i][1]);
      // Compute vertical top point
      float topY = getWallPointElevation(points [i][0], points [i][1], cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta);
      top [i] = new Point3f(points [i][0], topY, points [i][1]);
    }
    double  [] distanceSqToWallMiddle = new double [points.length];
    for (int i = 0; i < points.length; i++) {
      if (arcCircleCenter == null) {
        distanceSqToWallMiddle [i] = Line2D.ptLineDistSq(xStart, yStart, xEnd, yEnd, bottom [i].x, bottom [i].z);
      } else {
        distanceSqToWallMiddle [i] = arcCircleRadius
            - Point2D.distance(arcCircleCenter [0], arcCircleCenter [1], bottom [i].x, bottom [i].z);
        distanceSqToWallMiddle [i] *= distanceSqToWallMiddle [i];
      }
    }
    int rectanglesCount = points.length;
    boolean [] usedRectangle = new boolean [points.length];
    if (baseboard == null) {
      // Search which rectangles should be ignored
      for (int i = 0; i < points.length - 1; i++) {
        usedRectangle [i] = distanceSqToWallMiddle [i] > 0.001f
            || distanceSqToWallMiddle [i + 1] > 0.001f;
        if (!usedRectangle [i]) {
          rectanglesCount--;
        }
      }
      usedRectangle [usedRectangle.length - 1] =  distanceSqToWallMiddle [0] > 0.001f
          || distanceSqToWallMiddle [points.length - 1] > 0.001f;
      if (!usedRectangle [usedRectangle.length - 1]) {
        rectanglesCount--;
      }
      if (rectanglesCount == 0) {
        return null;
      }
    } else {
      Arrays.fill(usedRectangle, true);
    }

    List<Point3f> coords = new ArrayList<Point3f> (rectanglesCount * 4);
    for (int index = 0; index < points.length; index++) {
      if (usedRectangle [index]) {
        float y = minElevation;
        Point3f point1 = bottom [index];
        int nextIndex = (index + 1) % points.length;
        Point3f point2 = bottom [nextIndex];
        if (subpartSize > 0) {
          for (float yMax = Math.min(top [index].y, top [nextIndex].y) - subpartSize / 2; y < yMax; y += subpartSize) {
            coords.add(point1);
            coords.add(point2);
            point1 = new Point3f(bottom [index].x, y, bottom [index].z);
            point2 = new Point3f(bottom [nextIndex].x, y, bottom [nextIndex].z);
            coords.add(point2);
            coords.add(point1);
          }
        }
        coords.add(point1);
        coords.add(point2);
        coords.add(top [nextIndex]);
        coords.add(top [index]);
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(coords.toArray(new Point3f [coords.size()]));

    // Compute wall texture coordinates
    if (texture != null) {
      float halfThicknessSq;
      if (baseboard != null) {
        halfThicknessSq = wall.getThickness() / 2 + baseboard.getThickness();
        halfThicknessSq *= halfThicknessSq;
      } else {
        halfThicknessSq = (wall.getThickness() * wall.getThickness()) / 4;
      }
      TexCoord2f [] textureCoords = new TexCoord2f [coords.size()];
      TexCoord2f firstTextureCoords = new TexCoord2f(0, minElevation);
      int j = 0;
      // Tolerate more error with round walls since arc points are approximative
      float epsilon = arcCircleCenter == null
          ? wall.getThickness() / 1E4f
          : halfThicknessSq / 4;
      for (int index = 0; index < points.length; index++) {
        if (usedRectangle [index]) {
          int nextIndex = (index + 1) % points.length;
          TexCoord2f textureCoords1;
          TexCoord2f textureCoords2;
          if (Math.abs(distanceSqToWallMiddle [index] - halfThicknessSq) < epsilon
              && Math.abs(distanceSqToWallMiddle [nextIndex] - halfThicknessSq) < epsilon) {
            // Compute texture coordinates of wall part parallel to wall middle
            // according to textureReferencePoint
            float firstHorizontalTextureCoords;
            float secondHorizontalTextureCoords;
            if (arcCircleCenter == null) {
              firstHorizontalTextureCoords = (float)Point2D.distance(textureReferencePoint [0], textureReferencePoint [1],
                  points [index][0], points [index][1]);
              secondHorizontalTextureCoords = (float)Point2D.distance(textureReferencePoint [0], textureReferencePoint [1],
                  points [nextIndex][0], points [nextIndex][1]);
            } else {
              if (pointUCoordinates [index] == null) {
                float pointAngle = (float)Math.atan2(points [index][1] - arcCircleCenter [1], points [index][0] - arcCircleCenter [0]);
                pointAngle = adjustAngleOnReferencePointAngle(pointAngle, referencePointAngle, arcExtent);
                pointUCoordinates [index] = (pointAngle - referencePointAngle) * arcCircleRadius;
              }
              if (pointUCoordinates [nextIndex] == null) {
                float pointAngle = (float)Math.atan2(points [nextIndex][1] - arcCircleCenter [1], points [nextIndex][0] - arcCircleCenter [0]);
                pointAngle = adjustAngleOnReferencePointAngle(pointAngle, referencePointAngle, arcExtent);
                pointUCoordinates [nextIndex] = (pointAngle - referencePointAngle) * arcCircleRadius;
              }

              firstHorizontalTextureCoords = pointUCoordinates [index];
              secondHorizontalTextureCoords = pointUCoordinates [nextIndex];
            }
            if (wallSide == WALL_LEFT_SIDE && texture.isLeftToRightOriented()) {
              firstHorizontalTextureCoords = -firstHorizontalTextureCoords;
              secondHorizontalTextureCoords = -secondHorizontalTextureCoords;
            }

            textureCoords1 = new TexCoord2f(firstHorizontalTextureCoords, minElevation);
            textureCoords2 = new TexCoord2f(secondHorizontalTextureCoords, minElevation);
          } else {
            textureCoords1 = firstTextureCoords;
            float horizontalTextureCoords = (float)Point2D.distance(points [index][0], points [index][1],
                points [nextIndex][0], points [nextIndex][1]);
            textureCoords2 = new TexCoord2f(horizontalTextureCoords, minElevation);
          }

          if (subpartSize > 0) {
            float y = minElevation;
            for (float yMax = Math.min(top [index].y, top [nextIndex].y) - subpartSize / 2; y < yMax; y += subpartSize) {
              textureCoords [j++] = textureCoords1;
              textureCoords [j++] = textureCoords2;
              textureCoords1 = new TexCoord2f(textureCoords1.x, y);
              textureCoords2 = new TexCoord2f(textureCoords2.x, y);
              textureCoords [j++] = textureCoords2;
              textureCoords [j++] = textureCoords1;
            }
          }
          textureCoords [j++] = textureCoords1;
          textureCoords [j++] = textureCoords2;
          textureCoords [j++] = new TexCoord2f(textureCoords2.x, top [nextIndex].y);
          textureCoords [j++] = new TexCoord2f(textureCoords1.x, top [index].y);
        }
      }
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords);
    }

    // Generate normals
    NormalGenerator normalGenerator = new NormalGenerator();
    if (arcCircleCenter == null) {
      normalGenerator.setCreaseAngle(0);
    }
    normalGenerator.generateNormals(geometryInfo);
    return geometryInfo.getIndexedGeometryArray();
  }

  /**
   * Returns the elevation of the wall at the given point.
   */
  private float getWallPointElevation(float xWallPoint, float yWallPoint,
                                      double cosWallYawAngle, double sinWallYawAngle,
                                      double topLineAlpha, double topLineBeta) {
    double xTopPointWithZeroYaw = cosWallYawAngle * xWallPoint + sinWallYawAngle * yWallPoint;
    return (float)(topLineAlpha * xTopPointWithZeroYaw + topLineBeta);
  }

  /**
   * Returns <code>pointAngle</code> plus or minus 2 PI to ensure <code>pointAngle</code> value
   * will be greater or lower than <code>referencePointAngle</code> depending on <code>arcExtent</code> direction.
   */
  private float adjustAngleOnReferencePointAngle(float pointAngle, float referencePointAngle, float arcExtent) {
    if (arcExtent > 0) {
      if ((referencePointAngle > 0
          && (pointAngle < 0
              || pointAngle < referencePointAngle))
        || (referencePointAngle < 0
            && pointAngle < referencePointAngle)) {
        pointAngle += 2 * (float)Math.PI;
      }
    } else {
      if ((referencePointAngle < 0
            && (pointAngle > 0
                || referencePointAngle < pointAngle))
          || (referencePointAngle > 0
              && referencePointAngle < pointAngle)) {
        pointAngle -= 2 * (float)Math.PI;
      }
    }
    return pointAngle;
  }

  /**
   * Returns the geometry of an horizontal part of a wall or a baseboard at <code>y</code>.
   */
  private Geometry createHorizontalPartGeometry(float [][] points, float y,
                                                boolean reverseOrder, boolean roundWall) {
    Point3f [] coords = new Point3f [points.length];
    for (int i = 0; i < points.length; i++) {
      coords [i] = new Point3f(points [i][0], y, points [i][1]);
    }
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (coords);
    geometryInfo.setStripCounts(new int [] {coords.length});
    if (reverseOrder) {
      geometryInfo.reverse();
    }
    // Generate normals
    NormalGenerator normalGenerator = new NormalGenerator();
    if (roundWall) {
      normalGenerator.setCreaseAngle(0);
    }
    normalGenerator.generateNormals(geometryInfo);
    return geometryInfo.getIndexedGeometryArray ();
  }

  /**
   * Returns the geometry of the top part of a wall or a baseboard.
   */
  private Geometry createTopPartGeometry(float [][] points,
                                         double cosWallYawAngle, double sinWallYawAngle,
                                         double topLineAlpha, double topLineBeta,
                                         boolean roundWall) {
    Point3f [] coords = new Point3f [points.length];
    for (int i = 0; i < points.length; i++) {
      double xTopPointWithZeroYaw = cosWallYawAngle * points [i][0] + sinWallYawAngle * points [i][1];
      float topY = (float)(topLineAlpha * xTopPointWithZeroYaw + topLineBeta);
      coords [i] = new Point3f(points [i][0], topY, points [i][1]);
    }
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (coords);
    geometryInfo.setStripCounts(new int [] {coords.length});
    // Generate normals
    NormalGenerator normalGenerator = new NormalGenerator();
    if (roundWall) {
      normalGenerator.setCreaseAngle(0);
    }
    normalGenerator.generateNormals(geometryInfo);
    return geometryInfo.getIndexedGeometryArray ();
  }

  /**
   * Creates the geometry surrounding the given non rectangular door or window.
   */
  private void createGeometriesSurroundingDoorOrWindow(HomeDoorOrWindow doorOrWindow,
                                                       Area doorOrWindowFrontArea,
                                                       float frontOrBackSide,
                                                       Wall wall, List<Geometry> wallGeometries,
                                                       List<Geometry> wallTopGeometries,
                                                       float [][] wallSidePoints,
                                                       float wallElevation,
                                                       double cosWallYawAngle, double sinWallYawAngle,
                                                       double topLineAlpha, double topLineBeta,
                                                       HomeTexture texture, float [] textureReferencePoint,
                                                       int wallSide) {
    if (doorOrWindow.getModelTransformations() != null) {
      doorOrWindowFrontArea = new Area(doorOrWindowFrontArea);
      doorOrWindowFrontArea.transform(AffineTransform.getTranslateInstance(0.5, 0.5));
      doorOrWindowFrontArea.transform(AffineTransform.getScaleInstance(
          doorOrWindow.getWidth() * doorOrWindow.getWallWidth(),
          doorOrWindow.getHeight() * doorOrWindow.getWallHeight()));
      doorOrWindowFrontArea.transform(AffineTransform.getTranslateInstance(
          doorOrWindow.getWallLeft() * doorOrWindow.getWidth(),
          (1 - doorOrWindow.getWallHeight() - doorOrWindow.getWallTop()) * doorOrWindow.getHeight()));
      doorOrWindowFrontArea.transform(AffineTransform.getScaleInstance(
          1 / doorOrWindow.getWidth(),
          1 / doorOrWindow.getHeight()));
      doorOrWindowFrontArea.transform(AffineTransform.getTranslateInstance(-0.5, -0.5));
    }

    Area fullFaceArea = new Area(FULL_FACE_CUT_OUT_AREA);
    fullFaceArea.subtract(doorOrWindowFrontArea);
    if (!fullFaceArea.isEmpty()) {
      // Compute the depth position of the surrounding wall part in normalized piece space
      float doorOrWindowDepth = doorOrWindow.getDepth();
      float xPieceSide = (float)(doorOrWindow.getX() - frontOrBackSide * doorOrWindowDepth / 2 * Math.sin(doorOrWindow.getAngle()));
      float yPieceSide = (float)(doorOrWindow.getY() + frontOrBackSide * doorOrWindowDepth / 2 * Math.cos(doorOrWindow.getAngle()));
      float [] wallFirstPoint = wallSide == WALL_LEFT_SIDE
          ? wallSidePoints [0]
          : wallSidePoints [wallSidePoints.length - 1];
      float [] wallSecondPoint = wallSide == WALL_LEFT_SIDE
          ? wallSidePoints [wallSidePoints.length / 2 - 1]
          : wallSidePoints [wallSidePoints.length / 2];
      float frontSideToWallDistance = (float)Line2D.ptLineDist(wallFirstPoint [0], wallFirstPoint [1],
              wallSecondPoint [0], wallSecondPoint [1], xPieceSide, yPieceSide);
      float position = (float)Line2D.relativeCCW(wallFirstPoint [0], wallFirstPoint [1],
          wallSecondPoint [0], wallSecondPoint [1], xPieceSide, yPieceSide);
      float depthTranslation = frontOrBackSide * (0.5f - position * frontSideToWallDistance / doorOrWindowDepth);

      // Compute surrounding part transformation matrix
      Transform3D frontAreaTransform = ModelManager.getInstance().getPieceOfFurnitureNormalizedModelTransformation(doorOrWindow, null);
      Transform3D frontAreaTranslation = new Transform3D();
      frontAreaTranslation.setTranslation(new Vector3f(0, 0, depthTranslation));
      frontAreaTransform.mul(frontAreaTranslation);

      // Generate wall side path in normalized piece space
      Transform3D invertedFrontAreaTransform = new Transform3D();
      invertedFrontAreaTransform.invert(frontAreaTransform);
      GeneralPath wallPath = new GeneralPath();
      Point3f wallPoint = new Point3f(wallFirstPoint [0], wallElevation, wallFirstPoint [1]);
      invertedFrontAreaTransform.transform(wallPoint);
      wallPath.moveTo(wallPoint.x, wallPoint.y);
      wallPoint = new Point3f(wallSecondPoint [0], wallElevation, wallSecondPoint [1]);
      invertedFrontAreaTransform.transform(wallPoint);
      wallPath.lineTo(wallPoint.x, wallPoint.y);
      Point3f topWallPoint1 = new Point3f(wallSecondPoint [0],
          getWallPointElevation(wallSecondPoint [0], wallSecondPoint [1],
              cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta), wallSecondPoint [1]);
      invertedFrontAreaTransform.transform(topWallPoint1);
      wallPath.lineTo(topWallPoint1.x, topWallPoint1.y);
      Point3f topWallPoint2 = new Point3f(wallFirstPoint [0],
          getWallPointElevation(wallFirstPoint [0], wallFirstPoint [1],
              cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta), wallFirstPoint [1]);
      invertedFrontAreaTransform.transform(topWallPoint2);
      wallPath.lineTo(topWallPoint2.x, topWallPoint2.y);
      wallPath.closePath();

      GeneralPath doorOrWindowSurroundingPath = new GeneralPath();
      doorOrWindowSurroundingPath.moveTo(-.5f, -.5f);
      doorOrWindowSurroundingPath.lineTo(-.5f,  .5f);
      doorOrWindowSurroundingPath.lineTo( .5f, .5f);
      doorOrWindowSurroundingPath.lineTo( .5f, -.5f);
      doorOrWindowSurroundingPath.closePath();

      // Retrieve the points of the surrounding area as polygons where holes are removed
      Area doorOrWindowSurroundingArea = new Area(doorOrWindowSurroundingPath);
      doorOrWindowSurroundingArea.intersect(new Area(wallPath));
      doorOrWindowSurroundingArea.subtract(doorOrWindowFrontArea);
      float flatness = 0.5f / (Math.max(doorOrWindow.getWidth(), doorOrWindow.getHeight()));
      if (!doorOrWindowSurroundingArea.isEmpty()) {
        boolean reversed = frontOrBackSide > 0 ^ wallSide == WALL_RIGHT_SIDE ^ doorOrWindow.isModelMirrored();
        List<float [][]> doorOrWindowSurroundingAreasPoints = getAreaPoints(doorOrWindowSurroundingArea, flatness, reversed);
        if (!doorOrWindowSurroundingAreasPoints.isEmpty()) {
          // Generate coordinates for the surrounding area, door or window border and missing top wall part in the wall thickness
          int [] stripCounts = new int [doorOrWindowSurroundingAreasPoints.size()];
          int vertexCount = 0;
          for (int i = 0; i < doorOrWindowSurroundingAreasPoints.size(); i++) {
            float [][] areaPoints = doorOrWindowSurroundingAreasPoints.get(i);
            stripCounts [i] = areaPoints.length + 1;
            vertexCount += stripCounts [i];
          }
          float halfWallThickness = wall.getThickness() / 2;
          float deltaXToWallMiddle = (float)(halfWallThickness * sinWallYawAngle);
          float deltaZToWallMiddle = -(float)(halfWallThickness * cosWallYawAngle);
          if (wallSide == WALL_LEFT_SIDE) {
            deltaXToWallMiddle *= -1;
            deltaZToWallMiddle *= -1;
          }
          Point3f [] coords = new Point3f [vertexCount];
          List<Point3f> borderCoords = new ArrayList<Point3f>(4 * vertexCount);
          List<Point3f> slopingTopCoords = new ArrayList<Point3f>();
          TexCoord2f [] textureCoords;
          List<TexCoord2f> borderTextureCoords;
          if (texture != null) {
            textureCoords = new TexCoord2f [coords.length];
            borderTextureCoords = new ArrayList<TexCoord2f>(4 * vertexCount);
          } else {
            textureCoords = null;
            borderTextureCoords = null;
          }
          int i = 0;
          for (float [][] areaPoints : doorOrWindowSurroundingAreasPoints) {
            Point3f point = new Point3f(areaPoints [0][0], areaPoints [0][1], 0);
            frontAreaTransform.transform(point);
            TexCoord2f textureCoord = null;
            if (texture != null) {
              // Compute texture coordinates of wall side according to textureReferencePoint
              float horizontalTextureCoords = (float)Point2D.distance(textureReferencePoint [0], textureReferencePoint [1],
                  point.x, point.z);
              if (wallSide == WALL_LEFT_SIDE && texture.isLeftToRightOriented()) {
                horizontalTextureCoords = -horizontalTextureCoords;
              }
              textureCoord = new TexCoord2f(horizontalTextureCoords, point.y);
            }
            double distanceToTop = Line2D.ptLineDistSq(topWallPoint1.x, topWallPoint1.y, topWallPoint2.x, topWallPoint2.y,
                areaPoints [0][0], areaPoints [0][1]);

            for (int j = 0; j < areaPoints.length; j++, i++) {
              // Store coordinates of the surrounding area
              coords [i] = point;
              if (texture != null) {
                textureCoords [i] = textureCoord;
              }

              // Generate coordinates of the door or window border or missing top wall part
              int nextPointIndex = j < areaPoints.length - 1
                  ? j + 1
                  : 0;
              // Select the coordinates list to which the next quadrilateral will be added
              List<Point3f> coordsList;
              double nextDistanceToTop = Line2D.ptLineDistSq(topWallPoint1.x, topWallPoint1.y, topWallPoint2.x, topWallPoint2.y,
                  areaPoints [nextPointIndex][0], areaPoints [nextPointIndex][1]);
              if (distanceToTop < 1E-10 && nextDistanceToTop < 1E-10) {
                coordsList = slopingTopCoords;
              } else {
                coordsList = borderCoords;
              }

              Point3f nextPoint = new Point3f(areaPoints [nextPointIndex][0], areaPoints [nextPointIndex][1], 0);
              frontAreaTransform.transform(nextPoint);
              coordsList.add(point);
              coordsList.add(new Point3f(point.x + deltaXToWallMiddle, point.y, point.z + deltaZToWallMiddle));
              coordsList.add(new Point3f(nextPoint.x + deltaXToWallMiddle, nextPoint.y, nextPoint.z + deltaZToWallMiddle));
              coordsList.add(nextPoint);

              TexCoord2f nextTextureCoord = null;
              if (texture != null) {
                float horizontalTextureCoords = (float)Point2D.distance(textureReferencePoint [0], textureReferencePoint [1],
                    nextPoint.x, nextPoint.z);
                if (wallSide == WALL_LEFT_SIDE && texture.isLeftToRightOriented()) {
                  horizontalTextureCoords = -horizontalTextureCoords;
                }
                nextTextureCoord = new TexCoord2f(horizontalTextureCoords, nextPoint.y);
                if (coordsList == borderCoords) {
                  borderTextureCoords.add(textureCoord);
                  borderTextureCoords.add(textureCoord);
                  borderTextureCoords.add(nextTextureCoord);
                  borderTextureCoords.add(nextTextureCoord);
                }
              }

              // Update values for next loop round
              distanceToTop = nextDistanceToTop;
              point = nextPoint;
              textureCoord = nextTextureCoord;
            }

            // Close the polygon with first point for special cases
            coords [i] = point;
            if (texture != null) {
              textureCoords [i] = textureCoord;
            }
            i++;
          }

          // Generate surrounding area geometry
          GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
          geometryInfo.setStripCounts(stripCounts);
          geometryInfo.setCoordinates(coords);
          if (texture != null) {
            geometryInfo.setTextureCoordinateParams(1, 2);
            geometryInfo.setTextureCoordinates(0, textureCoords);
          }
          new NormalGenerator().generateNormals(geometryInfo);
          wallGeometries.add(geometryInfo.getIndexedGeometryArray());

          if (borderCoords.size() > 0) {
            // Generate border geometry
            geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
            geometryInfo.setCoordinates(borderCoords.toArray(new Point3f [borderCoords.size()]));
            if (texture != null) {
              geometryInfo.setTextureCoordinateParams(1, 2);
              geometryInfo.setTextureCoordinates(0, borderTextureCoords.toArray(new TexCoord2f [borderTextureCoords.size()]));
            }
            new NormalGenerator(Math.PI / 2).generateNormals(geometryInfo);
            wallGeometries.add(geometryInfo.getIndexedGeometryArray());
          }

          if (slopingTopCoords.size() > 0) {
            // Generate wall top geometry
            geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
            geometryInfo.setCoordinates(slopingTopCoords.toArray(new Point3f [slopingTopCoords.size()]));
            new NormalGenerator().generateNormals(geometryInfo);
            wallTopGeometries.add(geometryInfo.getIndexedGeometryArray());
          }
        }
      }
    }
  }

  /**
   * Returns the elevation of the wall managed by this 3D object.
   */
  private float getWallElevation(boolean ignoreFloorThickness) {
    Wall wall = (Wall)getUserData();
    Level level = wall.getLevel();
    if (level == null) {
      return 0;
    } else if (ignoreFloorThickness) {
      return level.getElevation();
    } else {
      float floorThicknessBottomWall = getFloorThicknessBottomWall();
      if (floorThicknessBottomWall > 0) {
        // Shift a little wall elevation at upper floors to avoid their bottom part overlaps a room ceiling
        floorThicknessBottomWall -= LEVEL_ELEVATION_SHIFT;
      }
      return level.getElevation() - floorThicknessBottomWall;
    }
  }

  /**
   * Returns the floor thickness at the bottom of the wall managed by this 3D object.
   */
  private float getFloorThicknessBottomWall() {
    Wall wall = (Wall)getUserData();
    Level level = wall.getLevel();
    if (level == null) {
      return 0;
    } else {
      List<Level> levels = this.home.getLevels();
      if (!levels.isEmpty() && levels.get(0).getElevation() == level.getElevation()) {
        // Ignore floor thickness at first level
        return 0;
      } else {
        return level.getFloorThickness();
      }
    }
  }

  /**
   * Returns the elevation of the wall top at its start.
   */
  private float getWallTopElevationAtStart() {
    Float wallHeight = ((Wall)getUserData()).getHeight();
    float wallHeightAtStart;
    if (wallHeight != null) {
      wallHeightAtStart = wallHeight + getWallElevation(false) + getFloorThicknessBottomWall();
    } else {
      // If wall height isn't set, use home wall height
      wallHeightAtStart = this.home.getWallHeight() + getWallElevation(false) + getFloorThicknessBottomWall();
    }
    return wallHeightAtStart + getTopElevationShift();
  }

  private float getTopElevationShift() {
    Level level = ((Wall)getUserData()).getLevel();
    if (level != null) {
      List<Level> levels = this.home.getLevels();
      // Don't shift last level
      if (levels.get(levels.size() - 1) != level) {
        return LEVEL_ELEVATION_SHIFT;
      }
    }
    return 0;
  }

  /**
   * Returns the elevation of the wall top at its end.
   */
  private float getWallTopElevationAtEnd() {
    Wall wall = (Wall)getUserData();
    if (wall.isTrapezoidal()) {
      return wall.getHeightAtEnd() + getWallElevation(false) + getFloorThicknessBottomWall() + getTopElevationShift();
    } else {
      // If the wall isn't trapezoidal, use same height as at wall start
      return getWallTopElevationAtStart();
    }
  }

  /**
   * Returns the elevation of the given baseboard top.
   */
  private float getBaseboardTopElevation(Baseboard baseboard) {
    return baseboard.getHeight() + getWallElevation(true);
  }

  /**
   * Sets wall appearance with its color, texture and transparency.
   */
  private void updateWallAppearance(boolean waitTextureLoadingEnd) {
    Wall wall = (Wall)getUserData();
    Integer wallsTopColor = wall.getTopColor();
    Group [] wallLeftSideGroups  = {(Group)getChild(0),  // Bottom group
                                    (Group)getChild(2),  // Baseboard group
                                    (Group)getChild(4),  // Main group
                                    (Group)getChild(6)}; // Top group
    Group [] wallRightSideGroups = {(Group)getChild(1),  // Bottom group
                                    (Group)getChild(3),  // Baseboard group
                                    (Group)getChild(5),  // Main group
                                    (Group)getChild(7)}; // Top group
    for (int i = 0; i < wallLeftSideGroups.length; i++) {
      boolean ignoreDrawingMode = wallLeftSideGroups [i].numChildren() == 1;
      if (i == 1) {
        // Fill wall baseboards
        Baseboard leftSideBaseboard = wall.getLeftSideBaseboard();
        if (leftSideBaseboard != null) {
          HomeTexture texture = leftSideBaseboard.getTexture();
          Integer color = leftSideBaseboard.getColor();
          if (color == null && texture == null) {
            texture = wall.getLeftSideTexture();
            color = wall.getLeftSideColor();
          }
          updateFilledWallSideAppearance(((Shape3D)wallLeftSideGroups [i].getChild(0)).getAppearance(),
              texture, waitTextureLoadingEnd, color, wall.getLeftSideShininess(), ignoreDrawingMode);
        }
        Baseboard rightSideBaseboard = wall.getRightSideBaseboard();
        if (rightSideBaseboard != null) {
          HomeTexture texture = rightSideBaseboard.getTexture();
          Integer color = rightSideBaseboard.getColor();
          if (color == null && texture == null) {
            texture = wall.getRightSideTexture();
            color = wall.getRightSideColor();
          }
          updateFilledWallSideAppearance(((Shape3D)wallRightSideGroups [i].getChild(0)).getAppearance(),
              texture, waitTextureLoadingEnd, color, wall.getRightSideShininess(), ignoreDrawingMode);
        }
      } else if (i != 3 || wallsTopColor == null) {
        updateFilledWallSideAppearance(((Shape3D)wallLeftSideGroups [i].getChild(0)).getAppearance(),
            wall.getLeftSideTexture(), waitTextureLoadingEnd, wall.getLeftSideColor(), wall.getLeftSideShininess(), ignoreDrawingMode);
        updateFilledWallSideAppearance(((Shape3D)wallRightSideGroups [i].getChild(0)).getAppearance(),
            wall.getRightSideTexture(), waitTextureLoadingEnd, wall.getRightSideColor(), wall.getRightSideShininess(), ignoreDrawingMode);
      } else {
        // Fill wall top with a separate color
        updateFilledWallSideAppearance(((Shape3D)wallLeftSideGroups [i].getChild(0)).getAppearance(),
            null, waitTextureLoadingEnd, wallsTopColor, 0, ignoreDrawingMode);
        updateFilledWallSideAppearance(((Shape3D)wallRightSideGroups [i].getChild(0)).getAppearance(),
            null, waitTextureLoadingEnd, wallsTopColor, 0, ignoreDrawingMode);
      }
      if (wallLeftSideGroups [i].numChildren() > 1) {
        updateOutlineWallSideAppearance(((Shape3D)wallLeftSideGroups [i].getChild(1)).getAppearance());
        updateOutlineWallSideAppearance(((Shape3D)wallRightSideGroups [i].getChild(1)).getAppearance());
      }
    }
  }

  /**
   * Sets filled wall side appearance with its color, texture, transparency and visibility.
   */
  private void updateFilledWallSideAppearance(final Appearance wallSideAppearance,
                                              final HomeTexture wallSideTexture,
                                              boolean waitTextureLoadingEnd,
                                              Integer wallSideColor,
                                              float shininess,
                                              boolean ignoreDrawingMode) {
    if (wallSideTexture == null) {
      wallSideAppearance.setMaterial(getMaterial(wallSideColor, wallSideColor, shininess));
      wallSideAppearance.setTexture(null);
    } else {
      // Update material and texture of wall side
      wallSideAppearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, shininess));
      wallSideAppearance.setTextureAttributes(getTextureAttributes(wallSideTexture, true));
      final TextureManager textureManager = TextureManager.getInstance();
      textureManager.loadTexture(wallSideTexture.getImage(), waitTextureLoadingEnd,
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                wallSideAppearance.setTexture(getHomeTextureClone(texture, home));
              }
            });
    }
    // Update wall side transparency
    float wallsAlpha = this.home.getEnvironment().getWallsAlpha();
    TransparencyAttributes transparencyAttributes = wallSideAppearance.getTransparencyAttributes();
    transparencyAttributes.setTransparency(wallsAlpha);
    // If walls alpha is equal to zero, turn off transparency to get better results
    transparencyAttributes.setTransparencyMode(wallsAlpha == 0
        ? TransparencyAttributes.NONE
        : TransparencyAttributes.NICEST);
    // Update wall side visibility
    RenderingAttributes renderingAttributes = wallSideAppearance.getRenderingAttributes();
    HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
    renderingAttributes.setVisible(ignoreDrawingMode
        || drawingMode == null
        || drawingMode == HomeEnvironment.DrawingMode.FILL
        || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE);
  }

  /**
   * Sets outline wall side visibility.
   */
  private void updateOutlineWallSideAppearance(final Appearance wallSideAppearance) {
    // Update wall side visibility
    RenderingAttributes renderingAttributes = wallSideAppearance.getRenderingAttributes();
    HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
    renderingAttributes.setVisible(drawingMode == HomeEnvironment.DrawingMode.OUTLINE
        || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE);
  }

  /**
   * An area used to compute holes in walls.
   */
  private static class DoorOrWindowArea {
    private final Area area;
    private final List<HomePieceOfFurniture> doorsOrWindows;

    public DoorOrWindowArea(Area area, List<HomePieceOfFurniture> doorsOrWindows) {
      this.area = area;
      this.doorsOrWindows = doorsOrWindows;
    }

    public Area getArea() {
      return this.area;
    }

    public List<HomePieceOfFurniture> getDoorsOrWindows() {
      return this.doorsOrWindows;
    }
  }

  /**
   * A class used to store model and its rotation as a key.
   * @author Emmanuel Puybaret
   */
  private static class ModelRotationTuple {
    private Content    model;
    private float [][] rotation;

    public ModelRotationTuple(Content model, float [][] rotation) {
      this.model = model;
      this.rotation = rotation;
    }

    @Override
    public int hashCode() {
      int hashCode = 31 * this.model.hashCode();
      for (float [] table : this.rotation) {
        hashCode += Arrays.hashCode(table);
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof ModelRotationTuple) {
        ModelRotationTuple tuple = (ModelRotationTuple)obj;
        if (this.model.equals(tuple.model)
            && this.rotation.length == tuple.rotation.length) {
          return Arrays.deepEquals(this.rotation, tuple.rotation);
        }
      }
      return false;
    }
  }
}