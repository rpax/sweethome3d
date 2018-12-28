/*
 * ModelManager.java 4 juil. 07
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
package com.eteks.sweethome3d.j3d;

import java.awt.EventQueue;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Light;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.SimpleURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.lw3d.Lw3dLoader;

/**
 * Singleton managing 3D models cache.
 * This manager supports 3D models with an OBJ, DAE, 3DS or LWS format by default.
 * Additional classes implementing Java 3D <code>Loader</code> interface may be
 * specified in the <code>com.eteks.sweethome3d.j3d.additionalLoaderClasses</code>
 * (separated by a space or a colon :) to enable the support of other formats.<br>
 * Note: this class is compatible with Java 3D 1.3.
 * @author Emmanuel Puybaret
 */
public class ModelManager {
  /**
   * <code>Shape3D</code> user data prefix for window pane shapes.
   */
  public static final String    WINDOW_PANE_SHAPE_PREFIX = "sweethome3d_window_pane";
  /**
   * <code>Shape3D</code> user data prefix for mirror shapes.
   */
  public static final String    MIRROR_SHAPE_PREFIX = "sweethome3d_window_mirror";
  /**
   * <code>Shape3D</code> user data prefix for lights.
   */
  public static final String    LIGHT_SHAPE_PREFIX = "sweethome3d_light";
  /**
   * <code>Node</code> user data prefix for mannequin parts.
   */
  public static final String    MANNEQUIN_ABDOMEN_PREFIX        = "sweethome3d_mannequin_abdomen";
  public static final String    MANNEQUIN_CHEST_PREFIX          = "sweethome3d_mannequin_chest";
  public static final String    MANNEQUIN_PELVIS_PREFIX         = "sweethome3d_mannequin_pelvis";
  public static final String    MANNEQUIN_NECK_PREFIX           = "sweethome3d_mannequin_neck";
  public static final String    MANNEQUIN_HEAD_PREFIX           = "sweethome3d_mannequin_head";
  public static final String    MANNEQUIN_LEFT_SHOULDER_PREFIX  = "sweethome3d_mannequin_left_shoulder";
  public static final String    MANNEQUIN_LEFT_ARM_PREFIX       = "sweethome3d_mannequin_left_arm";
  public static final String    MANNEQUIN_LEFT_ELBOW_PREFIX     = "sweethome3d_mannequin_left_elbow";
  public static final String    MANNEQUIN_LEFT_FOREARM_PREFIX   = "sweethome3d_mannequin_left_forearm";
  public static final String    MANNEQUIN_LEFT_WRIST_PREFIX     = "sweethome3d_mannequin_left_wrist";
  public static final String    MANNEQUIN_LEFT_HAND_PREFIX      = "sweethome3d_mannequin_left_hand";
  public static final String    MANNEQUIN_LEFT_HIP_PREFIX       = "sweethome3d_mannequin_left_hip";
  public static final String    MANNEQUIN_LEFT_THIGH_PREFIX     = "sweethome3d_mannequin_left_thigh";
  public static final String    MANNEQUIN_LEFT_KNEE_PREFIX      = "sweethome3d_mannequin_left_knee";
  public static final String    MANNEQUIN_LEFT_LEG_PREFIX       = "sweethome3d_mannequin_left_leg";
  public static final String    MANNEQUIN_LEFT_ANKLE_PREFIX     = "sweethome3d_mannequin_left_ankle";
  public static final String    MANNEQUIN_LEFT_FOOT_PREFIX      = "sweethome3d_mannequin_left_foot";
  public static final String    MANNEQUIN_RIGHT_SHOULDER_PREFIX = "sweethome3d_mannequin_right_shoulder";
  public static final String    MANNEQUIN_RIGHT_ARM_PREFIX      = "sweethome3d_mannequin_right_arm";
  public static final String    MANNEQUIN_RIGHT_ELBOW_PREFIX    = "sweethome3d_mannequin_right_elbow";
  public static final String    MANNEQUIN_RIGHT_FOREARM_PREFIX  = "sweethome3d_mannequin_right_forearm";
  public static final String    MANNEQUIN_RIGHT_WRIST_PREFIX    = "sweethome3d_mannequin_right_wrist";
  public static final String    MANNEQUIN_RIGHT_HAND_PREFIX     = "sweethome3d_mannequin_right_hand";
  public static final String    MANNEQUIN_RIGHT_HIP_PREFIX      = "sweethome3d_mannequin_right_hip";
  public static final String    MANNEQUIN_RIGHT_THIGH_PREFIX    = "sweethome3d_mannequin_right_thigh";
  public static final String    MANNEQUIN_RIGHT_KNEE_PREFIX     = "sweethome3d_mannequin_right_knee";
  public static final String    MANNEQUIN_RIGHT_LEG_PREFIX      = "sweethome3d_mannequin_right_leg";
  public static final String    MANNEQUIN_RIGHT_ANKLE_PREFIX    = "sweethome3d_mannequin_right_ankle";
  public static final String    MANNEQUIN_RIGHT_FOOT_PREFIX     = "sweethome3d_mannequin_right_foot";

  public static final String    MANNEQUIN_ABDOMEN_CHEST_PREFIX  = "sweethome3d_mannequin_abdomen_chest";
  public static final String    MANNEQUIN_ABDOMEN_PELVIS_PREFIX = "sweethome3d_mannequin_abdomen_pelvis";
  /**
   * <code>Node</code> user data prefix for ball / rotating  joints.
   */
  public static final String    BALL_PREFIX                 = "sweethome3d_ball_";
  public static final String    ARM_ON_BALL_PREFIX          = "sweethome3d_arm_on_ball_";
  /**
   * <code>Node</code> user data prefix for hinge / rotating opening joints.
   */
  public static final String    HINGE_PREFIX                = "sweethome3d_hinge_";
  public static final String    OPENING_ON_HINGE_PREFIX     = "sweethome3d_opening_on_hinge_";
  public static final String    WINDOW_PANE_ON_HINGE_PREFIX = "sweethome3d_window_pane_on_hinge_";
  /**
   * <code>Node</code> user data prefix for rail / sliding opening joints.
   */
  public static final String    UNIQUE_RAIL_PREFIX          = "sweethome3d_unique_rail";
  public static final String    RAIL_PREFIX                 = "sweethome3d_rail_";
  public static final String    OPENING_ON_RAIL_PREFIX      = "sweethome3d_opening_on_rail_";
  public static final String    WINDOW_PANE_ON_RAIL_PREFIX  = "sweethome3d_window_pane_on_rail_";
  /**
   * Deformable group suffix.
   */
  public static final String    DEFORMABLE_TRANSFORM_GROUP_SUFFIX = "_transformation";

  private static final TransparencyAttributes WINDOW_PANE_TRANSPARENCY_ATTRIBUTES =
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f);

  private static final Material DEFAULT_MATERIAL = new Material();

  private static final float    MINIMUM_SIZE = 0.001f;

  private static final String   ADDITIONAL_LOADER_CLASSES = "com.eteks.sweethome3d.j3d.additionalLoaderClasses";

  private static ModelManager instance;

  // Map storing loaded model nodes
  private Map<Content, BranchGroup> loadedModelNodes;
  // Map storing model nodes being loaded
  private Map<Content, List<ModelObserver>> loadingModelObservers;
  // Map storing the bounds of transformed model nodes
  private Map<Content, Map<Transform3D, BoundingBox>> transformedModelNodeBounds;
  // Executor used to load models
  private ExecutorService           modelsLoader;
  // List of additional loader classes
  private Class<Loader> []          additionalLoaderClasses;

  private ModelManager() {
    // This class is a singleton
    this.loadedModelNodes = new WeakHashMap<Content, BranchGroup>();
    this.loadingModelObservers = new HashMap<Content, List<ModelObserver>>();
    this.transformedModelNodeBounds = new WeakHashMap<Content, Map<Transform3D, BoundingBox>>();
    // Load other optional Loader classes
    List<Class<Loader>> loaderClasses = new ArrayList<Class<Loader>>();
    String loaderClassNames = System.getProperty(ADDITIONAL_LOADER_CLASSES);
    if (loaderClassNames != null) {
      for (String loaderClassName : loaderClassNames.split("\\s|:")) {
        try {
          loaderClasses.add(getLoaderClass(loaderClassName));
        } catch (IllegalArgumentException ex) {
          System.err.println("Invalid loader class " + loaderClassName + ":\n" + ex.getMessage());
        }
      }
    }
    this.additionalLoaderClasses = loaderClasses.toArray(new Class [loaderClasses.size()]);
  }

  /**
   * Returns the class of name <code>loaderClassName</code>.
   */
  @SuppressWarnings("unchecked")
  private Class<Loader> getLoaderClass(String loaderClassName) {
    try {
      Class<Loader> loaderClass = (Class<Loader>)getClass().getClassLoader().loadClass(loaderClassName);
      if (!Loader.class.isAssignableFrom(loaderClass)) {
        throw new IllegalArgumentException(loaderClassName + " not a subclass of " + Loader.class.getName());
      } else if (Modifier.isAbstract(loaderClass.getModifiers()) || !Modifier.isPublic(loaderClass.getModifiers())) {
        throw new IllegalArgumentException(loaderClassName + " not a public static class");
      }
      Constructor<Loader> constructor = loaderClass.getConstructor(new Class [0]);
      // Try to instantiate it now to see if it won't cause any problem
      constructor.newInstance(new Object [0]);
      return loaderClass;
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (InvocationTargetException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalArgumentException(loaderClassName + " constructor not accessible");
    } catch (InstantiationException ex) {
      throw new IllegalArgumentException(loaderClassName + " not a public static class");
    }
  }

  /**
   * Returns an instance of this singleton.
   */
  public static ModelManager getInstance() {
    if (instance == null) {
      instance = new ModelManager();
    }
    return instance;
  }

  /**
   * Shutdowns the multithreaded service that load models and clears loaded models cache.
   */
  public void clear() {
    if (this.modelsLoader != null) {
      this.modelsLoader.shutdownNow();
      this.modelsLoader = null;
    }
    synchronized (this.loadedModelNodes) {
      this.loadedModelNodes.clear();
    }
    this.loadingModelObservers.clear();
  }

  /**
   * Returns the minimum size of a model.
   */
  float getMinimumSize() {
    return MINIMUM_SIZE;
  }

  /**
   * Returns the size of 3D shapes of <code>node</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding
   * sphere for a scene.
   * @param node     the root of a model
   */
  public Vector3f getSize(Node node) {
    return getSize(node, new Transform3D());
  }

  /**
   * Returns the size of 3D shapes of <code>node</code> after an additional <code>transformation</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding
   * sphere for a scene.
   * @param node     the root of a model
   * @param transformation the transformation applied to the model
   *                 or <code>null</code> if no transformation should be applied to node.
   */
  public Vector3f getSize(Node node, Transform3D transformation) {
    BoundingBox bounds = getBounds(node, transformation);
    Point3d lower = new Point3d();
    bounds.getLower(lower);
    Point3d upper = new Point3d();
    bounds.getUpper(upper);
    return new Vector3f(Math.max(getMinimumSize(), (float)(upper.x - lower.x)),
        Math.max(getMinimumSize(), (float)(upper.y - lower.y)),
        Math.max(getMinimumSize(), (float)(upper.z - lower.z)));
  }

  /**
   * Returns the center of the bounds of <code>node</code> 3D shapes.
   * @param node  the root of a model
   */
  public Point3f getCenter(Node node) {
    BoundingBox bounds = getBounds(node);
    Point3d lower = new Point3d();
    bounds.getLower(lower);
    Point3d upper = new Point3d();
    bounds.getUpper(upper);
    return new Point3f((float)(lower.getX() + upper.getX()) / 2,
        (float)(lower.getY() + upper.getY()) / 2,
        (float)(lower.getZ() + upper.getZ()) / 2);
  }

  /**
   * Returns the bounds of the 3D shapes of <code>node</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding
   * sphere for a scene.
   * @param node  the root of a model
   */
  public BoundingBox getBounds(Node node) {
    return getBounds(node, new Transform3D());
  }

  /**
   * Returns the bounds of the 3D shapes of <code>node</code> with an additional <code>transformation</code>.
   * This method computes the exact box that contains all the shapes, contrary to <code>node.getBounds()</code>
   * that returns a bounding sphere for a scene.
   * @param node     the root of a model
   * @param transformation the transformation applied to the model
   *                 or <code>null</code> if no transformation should be applied to node.
   */
  public BoundingBox getBounds(Node node, Transform3D transformation) {
    BoundingBox objectBounds = new BoundingBox(
        new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    computeBounds(node, objectBounds, transformation, !isOrthogonalRotation(transformation), isDeformed(node));
    Point3d lower = new Point3d();
    objectBounds.getLower(lower);
    if (lower.x == Double.POSITIVE_INFINITY) {
      throw new IllegalArgumentException("Node has no bounds");
    }
    return objectBounds;
  }

  /**
   * Returns <code>true</code> if the rotation matrix matches only rotations of
   * a multiple of 90� degrees around x, y or z axis.
   */
  private boolean isOrthogonalRotation(Transform3D transformation) {
    Matrix3f matrix = new Matrix3f();
    transformation.get(matrix);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        // Return false if the matrix contains a value different from 0 1 or -1
        if (Math.abs(matrix.getElement(i, j)) > 1E-6
            && Math.abs(matrix.getElement(i, j) - 1) > 1E-6
            && Math.abs(matrix.getElement(i, j) + 1) > 1E-6) {
          return false;
        }
      }
    }
    return true;
  }

  private void computeBounds(Node node, BoundingBox bounds, Transform3D parentTransformation,
                             boolean transformShapeGeometry, boolean deformedGeometry) {
    if (node instanceof Group) {
      Map<Transform3D, BoundingBox> modelBounds = null;
      BoundingBox transformationModelBounds = null;
      if (node instanceof TransformGroup) {
        parentTransformation = new Transform3D(parentTransformation);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformation.mul(transform);
      } else if (transformShapeGeometry
                 && !deformedGeometry
                 && node instanceof BranchGroup
                 && node.getUserData() instanceof Content) {
        // Check if it's the node of a model
        modelBounds = this.transformedModelNodeBounds.get(node.getUserData());
        if (modelBounds != null) {
          // Retrieve the bounds that may have been previously computed for the requested transformation
          transformationModelBounds = modelBounds.get(parentTransformation);
        }
      }

      if (transformationModelBounds == null) {
        BoundingBox combinedBounds;
        if (modelBounds != null) {
          combinedBounds = new BoundingBox(
              new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
              new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
         } else {
           combinedBounds = bounds;
         }

        // Compute the bounds of all the node children
        Enumeration<?> enumeration = ((Group)node).getAllChildren();
        while (enumeration.hasMoreElements ()) {
          computeBounds((Node)enumeration.nextElement(), combinedBounds, parentTransformation,
              transformShapeGeometry, deformedGeometry);
        }

        if (modelBounds != null) {
          // Store the computed bounds of the model
          modelBounds.put(parentTransformation, transformationModelBounds = combinedBounds);
        }
      }

      if (transformationModelBounds != null) {
        bounds.combine(transformationModelBounds);
      }
    } else if (node instanceof Link) {
      computeBounds(((Link)node).getSharedGroup(), bounds, parentTransformation,
          transformShapeGeometry, deformedGeometry);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Bounds shapeBounds;
      if (transformShapeGeometry
          || deformedGeometry
             && !isOrthogonalRotation(parentTransformation)) {
        shapeBounds = computeTransformedGeometryBounds(shape, parentTransformation);
      } else {
        shapeBounds = shape.getBounds();
        shapeBounds.transform(parentTransformation);
      }
      bounds.combine(shapeBounds);
    }
  }

  private Bounds computeTransformedGeometryBounds(Shape3D shape, Transform3D transformation) {
    Point3d lower = new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    Point3d upper = new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    for (int i = 0, n = shape.numGeometries(); i < n; i++) {
      Geometry geometry = shape.getGeometry(i);
      if (geometry instanceof GeometryArray) {
        GeometryArray geometryArray = (GeometryArray)geometry;
        int vertexCount = geometryArray.getVertexCount();
        Point3f vertex = new Point3f();
        if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
          if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
            float [] vertexData = geometryArray.getInterleavedVertices();
            int vertexSize = vertexData.length / vertexCount;
            for (int index = 0, j = vertexSize - 3; index < vertexCount; j += vertexSize, index++) {
              vertex.x = vertexData [j];
              vertex.y = vertexData [j + 1];
              vertex.z = vertexData [j + 2];
              updateBounds(vertex, transformation, lower, upper);
            }
          } else {
            float [] vertexCoordinates = geometryArray.getCoordRefFloat();
            for (int index = 0, j = 0; index < vertexCount; j += 3, index++) {
              vertex.x = vertexCoordinates [j];
              vertex.y = vertexCoordinates [j + 1];
              vertex.z = vertexCoordinates [j + 2];
              updateBounds(vertex, transformation, lower, upper);
            }
          }
        } else {
          for (int index = 0; index < vertexCount; index++) {
            geometryArray.getCoordinate(index, vertex);
            updateBounds(vertex, transformation, lower, upper);
          }
        }
      } else {
        Bounds shapeBounds = shape.getBounds();
        shapeBounds.transform(transformation);
        return shapeBounds;
      }
    }
    Bounds shapeBounds = new BoundingBox(lower, upper);
    return shapeBounds;
  }

  private void updateBounds(Point3f vertex, Transform3D transformation, Point3d lower, Point3d upper) {
    transformation.transform(vertex);
    if (lower.x > vertex.x) {
      lower.x = vertex.x;
    }
    if (lower.y > vertex.y) {
      lower.y = vertex.y;
    }
    if (lower.z > vertex.z) {
      lower.z = vertex.z;
    }
    if (upper.x < vertex.x) {
      upper.x = vertex.x;
    }
    if (upper.y < vertex.y) {
      upper.y = vertex.y;
    }
    if (upper.z < vertex.z) {
      upper.z = vertex.z;
    }
  }

  /**
   * Returns a transform group that will transform the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   * @param modelRotation the rotation applied to the model before normalization
   *                 or <code>null</code> if no transformation should be applied to node
   * @param width    the width of the box
   */
  public TransformGroup getNormalizedTransformGroup(Node node, float [][] modelRotation, float width) {
    return new TransformGroup(getNormalizedTransform(node, modelRotation, width, true));
  }

  /**
   * Returns a transform group that will transform the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   * @param modelRotation the rotation applied to the model before normalization
   *                 or <code>null</code> if no transformation should be applied to node
   * @param width    the width of the box
   * @param modelCenteredAtOrigin if <code>true</code> center will be moved to match the origin
   *                 after the model rotation is applied
   */
  public TransformGroup getNormalizedTransformGroup(Node node, float [][] modelRotation, float width,
                                                    boolean modelCenteredAtOrigin) {
    return new TransformGroup(getNormalizedTransform(node, modelRotation, width, modelCenteredAtOrigin));
  }

  /**
   * Returns a transform that will transform the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   * @param modelRotation the rotation applied to the model before normalization
   *                 or <code>null</code> if no transformation should be applied to node
   * @param width    the width of the box
   */
  public Transform3D getNormalizedTransform(Node node, float [][] modelRotation, float width) {
    return getNormalizedTransform(node, modelRotation, width, true);
  }

 /**
   * Returns a transform that will transform the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   * @param modelRotation the rotation applied to the model before normalization
   *                 or <code>null</code> if no transformation should be applied to node
   * @param width    the width of the box
   * @param modelCenteredAtOrigin if <code>true</code> center will be moved to match the origin
   *                 after the model rotation is applied
   */
  public Transform3D getNormalizedTransform(Node node, float [][] modelRotation, float width,
                                            boolean modelCenteredAtOrigin) {
    // Get model bounding box size
    BoundingBox modelBounds = getBounds(node);
    Point3d lower = new Point3d();
    modelBounds.getLower(lower);
    Point3d upper = new Point3d();
    modelBounds.getUpper(upper);
    // Translate model to its center
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3d(
        -lower.x - (upper.x - lower.x) / 2,
        -lower.y - (upper.y - lower.y) / 2,
        -lower.z - (upper.z - lower.z) / 2));

    Transform3D modelTransform;
    if (modelRotation != null) {
      // Get model bounding box size with model rotation
      Transform3D rotationTransform = getRotationTransformation(modelRotation);
      rotationTransform.mul(translation);
      BoundingBox rotatedModelBounds = getBounds(node, rotationTransform);
      rotatedModelBounds.getLower(lower);
      rotatedModelBounds.getUpper(upper);
      modelTransform = new Transform3D();
      if (modelCenteredAtOrigin) {
        // Move model back to its new center
        modelTransform.setTranslation(new Vector3d(
            -lower.x - (upper.x - lower.x) / 2,
            -lower.y - (upper.y - lower.y) / 2,
            -lower.z - (upper.z - lower.z) / 2));
      }
      modelTransform.mul(rotationTransform);
    } else {
      modelTransform = translation;
    }

    // Scale model to make it fill a 1 unit wide box
    Transform3D scaleOneTransform = new Transform3D();
    scaleOneTransform.setScale (
        new Vector3d(width / Math.max(getMinimumSize(), upper.x -lower.x),
            width / Math.max(getMinimumSize(), upper.y - lower.y),
            width / Math.max(getMinimumSize(), upper.z - lower.z)));
    scaleOneTransform.mul(modelTransform);
    return scaleOneTransform;
  }

  /**
   * Returns a transformation matching the given rotation.
   */
  Transform3D getRotationTransformation(float [][] modelRotation) {
    Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
        modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
        modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
    Transform3D modelTransform = new Transform3D();
    modelTransform.setRotation(modelRotationMatrix);
    return modelTransform;
  }

  /**
   * Returns a transformation able to place in the scene the normalized model
   * of the given <code>piece</code>.
   * @param piece a piece of furniture
   * @param normalizedModelNode the node matching the normalized model of the piece.
   *            This parameter is required only if the piece is rotated horizontally.
   */
  Transform3D getPieceOfFurnitureNormalizedModelTransformation(HomePieceOfFurniture piece, Node normalizedModelNode) {
    // Set piece size
    Transform3D scale = new Transform3D();
    float pieceWidth = piece.getWidth();
    // If piece model is mirrored, inverse its width
    if (piece.isModelMirrored()) {
      pieceWidth *= -1;
    }
    scale.setScale(new Vector3d(pieceWidth, piece.getHeight(), piece.getDepth()));

    Transform3D modelTransform;
    float height;
    if (piece.isHorizontallyRotated() && normalizedModelNode != null) {
      Transform3D horizontalRotationAndScale = new Transform3D();
      // Change its angle around horizontal axis
      if (piece.getPitch() != 0) {
        horizontalRotationAndScale.rotX(-piece.getPitch());
      } else {
        horizontalRotationAndScale.rotZ(-piece.getRoll());
      }
      horizontalRotationAndScale.mul(scale);

      // Compute center location when the piece is rotated around horizontal axis
      BoundingBox rotatedModelBounds = getBounds(normalizedModelNode, horizontalRotationAndScale);
      Point3d lower = new Point3d();
      rotatedModelBounds.getLower(lower);
      Point3d upper = new Point3d();
      rotatedModelBounds.getUpper(upper);
      modelTransform = new Transform3D();
      modelTransform.setTranslation(new Vector3d(
          -lower.x - (upper.x - lower.x) / 2,
          -lower.y - (upper.y - lower.y) / 2,
          -lower.z - (upper.z - lower.z) / 2));
      modelTransform.mul(horizontalRotationAndScale);
      height = (float)Math.max(getMinimumSize(), upper.y - lower.y);
    } else {
      modelTransform = scale;
      height = piece.getHeight();
    }

    // Change its angle around vertical axis
    Transform3D verticalRotation = new Transform3D();
    verticalRotation.rotY(-piece.getAngle());
    verticalRotation.mul(modelTransform);

    // Translate it to its location
    Transform3D pieceTransform = new Transform3D();
    float levelElevation;
    if (piece.getLevel() != null) {
      levelElevation = piece.getLevel().getElevation();
    } else {
      levelElevation = 0;
    }
    pieceTransform.setTranslation(new Vector3f(
        piece.getX(),
        piece.getElevation() + height / 2 + levelElevation,
        piece.getY()));
    pieceTransform.mul(verticalRotation);
    return pieceTransform;
  }

  /**
   * Reads asynchronously a 3D node from <code>content</code> with supported loaders
   * and notifies the loaded model to the given <code>modelObserver</code> once available.
   * @param content an object containing a model
   * @param modelObserver the observer that will be notified once the model is available
   *    or if an error happens
   * @throws IllegalStateException if the current thread isn't the Event Dispatch Thread.
   */
  public void loadModel(Content content,
                        ModelObserver modelObserver) {
    loadModel(content, false, modelObserver);
  }

  /**
   * Reads a 3D node from <code>content</code> with supported loaders
   * and notifies the loaded model to the given <code>modelObserver</code> once available.
   * @param content an object containing a model
   * @param synchronous if <code>true</code>, this method will return only once model content is loaded
   * @param modelObserver the observer that will be notified once the model is available
   *    or if an error happens. When the model is loaded synchronously, the observer will be notified
   *    in the same thread as the caller, otherwise the observer will be notified in the Event
   *    Dispatch Thread and this method must be called in Event Dispatch Thread too.
   * @throws IllegalStateException if synchronous is <code>false</code> and the current thread isn't
   *    the Event Dispatch Thread.
   */
  public void loadModel(final Content content,
                        boolean synchronous,
                        ModelObserver modelObserver) {
    BranchGroup modelRoot;
    synchronized (this.loadedModelNodes) {
      modelRoot = this.loadedModelNodes.get(content);
    }
    if (modelRoot != null) {
      // Notify cached model to observer with a clone of the model
      modelObserver.modelUpdated((BranchGroup)cloneNode(modelRoot));
    } else if (synchronous) {
      try {
        modelRoot = loadModel(content);
        synchronized (this.loadedModelNodes) {
          // Store in cache model node for future copies
          this.loadedModelNodes.put(content, (BranchGroup)modelRoot);
          this.transformedModelNodeBounds.put(content, new WeakHashMap<Transform3D, BoundingBox>());
        }
        modelObserver.modelUpdated((BranchGroup)cloneNode(modelRoot));
      } catch (IOException ex) {
        modelObserver.modelError(ex);
      }
    } else if (!EventQueue.isDispatchThread()) {
      throw new IllegalStateException("Asynchronous call out of Event Dispatch Thread");
    } else {
      if (this.modelsLoader == null) {
        this.modelsLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      }
      List<ModelObserver> observers = this.loadingModelObservers.get(content);
      if (observers != null) {
        // If observers list exists, content model is already being loaded
        // register observer for future notification
        observers.add(modelObserver);
      } else {
        // Create a list of observers that will be notified once content model is loaded
        observers = new ArrayList<ModelObserver>();
        observers.add(modelObserver);
        this.loadingModelObservers.put(content, observers);

        // Load the model in an other thread
        this.modelsLoader.execute(new Runnable() {
          public void run() {
            try {
              final BranchGroup loadedModel = loadModel(content);
              synchronized (loadedModelNodes) {
                // Update loaded models cache and notify registered observers
                loadedModelNodes.put(content, loadedModel);
                transformedModelNodeBounds.put(content, new WeakHashMap<Transform3D, BoundingBox>());
              }
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    List<ModelObserver> observers = loadingModelObservers.remove(content);
                    if (observers != null) {
                      for (final ModelObserver observer : observers) {
                        observer.modelUpdated((BranchGroup)cloneNode(loadedModel));
                      }
                    }
                  }
                });
            } catch (final IOException ex) {
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    List<ModelObserver> observers = loadingModelObservers.remove(content);
                    if (observers != null) {
                      for (final ModelObserver observer : observers) {
                        observer.modelError(ex);
                      }
                    }
                  }
                });
            }
          }
        });
      }
    }
  }

  /**
   * Returns a clone of the given <code>node</code>.
   * All the children and the attributes of the given node are duplicated except the geometries
   * and the texture images of shapes.
   */
  public Node cloneNode(Node node) {
    // Clone node in a synchronized block because cloneNodeComponent is not thread safe
    synchronized (this.loadedModelNodes) {
      return cloneNode(node, new HashMap<SharedGroup, SharedGroup>());
    }
  }

  private Node cloneNode(Node node, Map<SharedGroup, SharedGroup> clonedSharedGroups) {
    if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Shape3D clonedShape = (Shape3D)shape.cloneNode(false);
      Appearance appearance = shape.getAppearance();
      if (appearance != null) {
        // Duplicate node's appearance except its texture
        Appearance clonedAppearance = (Appearance)appearance.cloneNodeComponent(false);
        Material material = appearance.getMaterial();
        if (material != null) {
          clonedAppearance.setMaterial((Material)material.cloneNodeComponent(true));
        }
        ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
        if (coloringAttributes != null) {
          clonedAppearance.setColoringAttributes((ColoringAttributes)coloringAttributes.cloneNodeComponent(true));
        }
        TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
        if (transparencyAttributes != null) {
          clonedAppearance.setTransparencyAttributes((TransparencyAttributes)transparencyAttributes.cloneNodeComponent(true));
        }
        RenderingAttributes renderingAttributes = appearance.getRenderingAttributes();
        if (renderingAttributes != null) {
          clonedAppearance.setRenderingAttributes((RenderingAttributes)renderingAttributes.cloneNodeComponent(true));
        }
        PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
        if (polygonAttributes != null) {
          clonedAppearance.setPolygonAttributes((PolygonAttributes)polygonAttributes.cloneNodeComponent(true));
        }
        LineAttributes lineAttributes = appearance.getLineAttributes();
        if (lineAttributes != null) {
          clonedAppearance.setLineAttributes((LineAttributes)lineAttributes.cloneNodeComponent(true));
        }
        PointAttributes pointAttributes = appearance.getPointAttributes();
        if (pointAttributes != null) {
          clonedAppearance.setPointAttributes((PointAttributes)pointAttributes.cloneNodeComponent(true));
        }
        TextureAttributes textureAttributes = appearance.getTextureAttributes();
        if (textureAttributes != null) {
          clonedAppearance.setTextureAttributes((TextureAttributes)textureAttributes.cloneNodeComponent(true));
        }
        TexCoordGeneration texCoordGeneration = appearance.getTexCoordGeneration();
        if (texCoordGeneration != null) {
          clonedAppearance.setTexCoordGeneration((TexCoordGeneration)texCoordGeneration.cloneNodeComponent(true));
        }

        clonedShape.setAppearance(clonedAppearance);
      }
      return clonedShape;
    } else if (node instanceof Link) {
      Link clonedLink = (Link)node.cloneNode(true);
      // Force duplication of shared groups too
      SharedGroup sharedGroup = clonedLink.getSharedGroup();
      if (sharedGroup != null) {
        SharedGroup clonedSharedGroup = clonedSharedGroups.get(sharedGroup);
        if (clonedSharedGroup == null) {
          clonedSharedGroup = (SharedGroup)cloneNode(sharedGroup, clonedSharedGroups);
          clonedSharedGroups.put(sharedGroup, clonedSharedGroup);
        }
        clonedLink.setSharedGroup(clonedSharedGroup);
      }
      return clonedLink;
    } else {
      Node clonedNode = node.cloneNode(true);
      if (node instanceof Group) {
        Group group = (Group)node;
        Group clonedGroup = (Group)clonedNode;
        for (int i = 0, n = group.numChildren(); i < n; i++) {
          Node clonedChild = cloneNode(group.getChild(i), clonedSharedGroups);
          clonedGroup.addChild(clonedChild);
        }
      }
      return clonedNode;
    }
  }

  /**
   * Returns the node loaded synchronously from <code>content</code> with supported loaders.
   * This method is threadsafe and may be called from any thread.
   * @param content an object containing a model
   */
  public BranchGroup loadModel(Content content) throws IOException {
    // Ensure we use a URLContent object
    URLContent urlContent;
    if (content instanceof URLContent) {
      urlContent = (URLContent)content;
    } else {
      urlContent = TemporaryURLContent.copyToTemporaryURLContent(content);
    }
    Loader []  defaultLoaders = new Loader [] {new OBJLoader(),
                                               new DAELoader(),
                                               new Max3DSLoader(),
                                               new Lw3dLoader()};
    Loader [] loaders = new Loader [defaultLoaders.length + this.additionalLoaderClasses.length];
    System.arraycopy(defaultLoaders, 0, loaders, 0, defaultLoaders.length);
    for (int i = 0; i < this.additionalLoaderClasses.length; i++) {
      try {
        loaders [defaultLoaders.length + i] = this.additionalLoaderClasses [i].newInstance();
      } catch (InstantiationException ex) {
        // Can't happen: getLoaderClass checked this class is instantiable
        throw new InternalError(ex.getMessage());
      } catch (IllegalAccessException ex) {
        // Can't happen: getLoaderClass checked this class is instantiable
        throw new InternalError(ex.getMessage());
      }
    }

    Exception lastException = null;
    Boolean useCaches = shouldUseCaches(urlContent);
    for (Loader loader : loaders) {
      boolean loadSynchronously = false;
      try {
        // Call setUseCaches(Boolean) by reflection
        loader.getClass().getMethod("setUseCaches", Boolean.class).invoke(loader, useCaches);
      } catch (NoSuchMethodException ex) {
        // If the method setUseCaches doesn't exist, set default cache use if different
        // from the required one and load models synchronously
        URLConnection connection = urlContent.getURL().openConnection();
        loadSynchronously = connection.getDefaultUseCaches() != useCaches;
      } catch (InvocationTargetException ex) {
        if (ex instanceof Exception) {
          lastException = (Exception)ex.getTargetException();
          continue;
        } else {
          ex.printStackTrace();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      try {
        // Ask loader to ignore lights, fogs...
        loader.setFlags(loader.getFlags()
            & ~(Loader.LOAD_LIGHT_NODES | Loader.LOAD_FOG_NODES
                | Loader.LOAD_BACKGROUND_NODES | Loader.LOAD_VIEW_GROUPS));
        // Return the first scene that can be loaded from model URL content
        Scene scene;
        if (loadSynchronously) {
          synchronized (this) {
            URLConnection connection = urlContent.getURL().openConnection();
            try {
              connection.setDefaultUseCaches(useCaches);
              scene = loader.load(urlContent.getURL());
            } finally {
              if (connection.getDefaultUseCaches() == useCaches) {
                // Restore the default global value only when it didn't change yet,
                // in case an other thread not synchronized on the same lock changed it
                connection.setDefaultUseCaches(!useCaches);
              }
            }
          }
        } else {
          scene = loader.load(urlContent.getURL());
        }

        BranchGroup modelNode = scene.getSceneGroup();
        // If model doesn't have any child, consider the file as wrong
        if (modelNode.numChildren() == 0) {
          throw new IllegalArgumentException("Empty model");
        }

        // Update transparency of scene window panes shapes
        updateShapeNamesAndWindowPanesTransparency(scene);
        // Turn off lights because some loaders don't take into account the ~LOAD_LIGHT_NODES flag
        turnOffLightsShareAndModulateTextures(modelNode, new IdentityHashMap<Texture, Texture>());
        updateDeformableModelHierarchy(modelNode);
        checkAppearancesName(modelNode);
        modelNode.setUserData(content);
        return modelNode;
      } catch (IllegalArgumentException ex) {
        lastException = ex;
      } catch (IncorrectFormatException ex) {
        lastException = ex;
      } catch (ParsingErrorException ex) {
        lastException = ex;
      } catch (IOException ex) {
        lastException = ex;
      } catch (RuntimeException ex) {
        // Take into account exceptions of Java 3D 1.5 ImageException class
        // in such a way program can run in Java 3D 1.3.1
        if (ex.getClass().getName().equals("com.sun.j3d.utils.image.ImageException")) {
          lastException = ex;
        } else {
          throw ex;
        }
      }
    }

    if (lastException instanceof IOException) {
      throw (IOException)lastException;
    } else if (lastException instanceof IncorrectFormatException) {
      IOException incorrectFormatException = new IOException("Incorrect format");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else if (lastException instanceof ParsingErrorException) {
      IOException incorrectFormatException = new IOException("Parsing error");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else {
      IOException otherException = new IOException();
      otherException.initCause(lastException);
      throw otherException;
    }
  }

  /**
   * Returns <code>true</code> if reading from the given content should be done using caches.
   */
  private boolean shouldUseCaches(URLContent urlContent) throws IOException {
    URLConnection connection = urlContent.getURL().openConnection();
    if (OperatingSystem.isWindows()
        && (connection instanceof JarURLConnection)) {
      JarURLConnection urlConnection = (JarURLConnection)connection;
      URL jarFileUrl = urlConnection.getJarFileURL();
      if (jarFileUrl.getProtocol().equalsIgnoreCase("file")) {
        try {
          if (new File(jarFileUrl.toURI()).canWrite()) {
            // Refuse to use caches to be able to delete the writable files accessed with jar protocol under Windows,
            // as suggested in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459
            return false;
          }
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException();
          ex2.initCause(ex);
          throw ex2;
        }
      }
    }
    return connection.getDefaultUseCaches();
  }

  /**
   * Updates the name of scene shapes and transparency window panes shapes.
   */
  @SuppressWarnings("unchecked")
  private void updateShapeNamesAndWindowPanesTransparency(Scene scene) {
    Map<String, Object> namedObjects = scene.getNamedObjects();
    for (Map.Entry<String, Object> entry : namedObjects.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Node) {
        // Assign node name to its user data
        ((Node)value).setUserData(name);
      }
      if (value instanceof Shape3D
          && (name.startsWith(WINDOW_PANE_SHAPE_PREFIX)
              || name.startsWith(WINDOW_PANE_ON_HINGE_PREFIX)
              || name.startsWith(WINDOW_PANE_ON_RAIL_PREFIX))) {
        Shape3D shape = (Shape3D)value;
        Appearance appearance = shape.getAppearance();
        if (appearance == null) {
          appearance = new Appearance();
          shape.setAppearance(appearance);
        }
        if (appearance.getTransparencyAttributes() == null) {
          appearance.setTransparencyAttributes(WINDOW_PANE_TRANSPARENCY_ATTRIBUTES);
        }
      }
    }
  }

  /**
   * Updates the hierarchy of nodes with intermediate pickable nodes to help deforming models.
   */
  private void updateDeformableModelHierarchy(Group group) {
    // Try to reorganize node hierarchy of mannequin model
    if (containsNode(group, MANNEQUIN_ABDOMEN_PREFIX)
        && containsNode(group, MANNEQUIN_CHEST_PREFIX)
        && containsNode(group, MANNEQUIN_PELVIS_PREFIX)
        && containsNode(group, MANNEQUIN_NECK_PREFIX)
        && containsNode(group, MANNEQUIN_HEAD_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_SHOULDER_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_ARM_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_ELBOW_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_FOREARM_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_WRIST_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_HAND_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_HIP_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_THIGH_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_KNEE_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_LEG_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_ANKLE_PREFIX)
        && containsNode(group, MANNEQUIN_LEFT_FOOT_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_SHOULDER_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_ARM_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_ELBOW_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_FOREARM_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_WRIST_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_HAND_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_HIP_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_THIGH_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_KNEE_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_LEG_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_ANKLE_PREFIX)
        && containsNode(group, MANNEQUIN_RIGHT_FOOT_PREFIX)) {
      // Head
      Node head = extractNodes(group, MANNEQUIN_HEAD_PREFIX, null);
      TransformGroup headGroup = createPickableTransformGroup(MANNEQUIN_NECK_PREFIX, head);

      // Left arm
      Node leftHand = extractNodes(group, MANNEQUIN_LEFT_HAND_PREFIX, null);
      TransformGroup leftHandGroup = createPickableTransformGroup(MANNEQUIN_LEFT_WRIST_PREFIX, leftHand);
      Node leftForearm = extractNodes(group, MANNEQUIN_LEFT_FOREARM_PREFIX, null);
      Node leftWrist = extractNodes(group, MANNEQUIN_LEFT_WRIST_PREFIX, null);
      TransformGroup leftForearmGroup = createPickableTransformGroup(MANNEQUIN_LEFT_ELBOW_PREFIX, leftForearm, leftWrist, leftHandGroup);
      Node leftArm = extractNodes(group, MANNEQUIN_LEFT_ARM_PREFIX, null);
      Node leftElbow = extractNodes(group, MANNEQUIN_LEFT_ELBOW_PREFIX, null);
      TransformGroup leftArmGroup = createPickableTransformGroup(MANNEQUIN_LEFT_SHOULDER_PREFIX, leftArm, leftElbow, leftForearmGroup);

      // Right arm
      Node rightHand = extractNodes(group, MANNEQUIN_RIGHT_HAND_PREFIX, null);
      TransformGroup rightHandGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_WRIST_PREFIX, rightHand);
      Node rightForearm = extractNodes(group, MANNEQUIN_RIGHT_FOREARM_PREFIX, null);
      Node rightWrist = extractNodes(group, MANNEQUIN_RIGHT_WRIST_PREFIX, null);
      TransformGroup rightForearmGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_ELBOW_PREFIX, rightForearm, rightWrist, rightHandGroup);
      Node rightArm = extractNodes(group, MANNEQUIN_RIGHT_ARM_PREFIX, null);
      Node rightElbow = extractNodes(group, MANNEQUIN_RIGHT_ELBOW_PREFIX, null);
      TransformGroup rightArmGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_SHOULDER_PREFIX, rightArm, rightElbow, rightForearmGroup);

      // Chest
      Node chest = extractNodes(group, MANNEQUIN_CHEST_PREFIX, null);
      Node leftShoulder = extractNodes(group, MANNEQUIN_LEFT_SHOULDER_PREFIX, null);
      Node rightShoulder = extractNodes(group, MANNEQUIN_RIGHT_SHOULDER_PREFIX, null);
      Node neck = extractNodes(group, MANNEQUIN_NECK_PREFIX, null);
      TransformGroup chestGroup = createPickableTransformGroup(MANNEQUIN_ABDOMEN_CHEST_PREFIX, chest, leftShoulder, leftArmGroup, rightShoulder, rightArmGroup, neck, headGroup);

      // Left leg
      Node leftFoot = extractNodes(group, MANNEQUIN_LEFT_FOOT_PREFIX, null);
      TransformGroup leftFootGroup = createPickableTransformGroup(MANNEQUIN_LEFT_ANKLE_PREFIX, leftFoot);
      Node leftLeg = extractNodes(group, MANNEQUIN_LEFT_LEG_PREFIX, null);
      Node leftAnkle = extractNodes(group, MANNEQUIN_LEFT_ANKLE_PREFIX, null);
      TransformGroup leftLegGroup = createPickableTransformGroup(MANNEQUIN_LEFT_KNEE_PREFIX, leftLeg, leftAnkle, leftFootGroup);
      Node leftThigh = extractNodes(group, MANNEQUIN_LEFT_THIGH_PREFIX, null);
      Node leftKnee = extractNodes(group, MANNEQUIN_LEFT_KNEE_PREFIX, null);
      TransformGroup leftThighGroup = createPickableTransformGroup(MANNEQUIN_LEFT_HIP_PREFIX, leftThigh, leftKnee, leftLegGroup);

      // Right leg
      Node rightFoot = extractNodes(group, MANNEQUIN_RIGHT_FOOT_PREFIX, null);
      TransformGroup rightFootGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_ANKLE_PREFIX, rightFoot);
      Node rightLeg = extractNodes(group, MANNEQUIN_RIGHT_LEG_PREFIX, null);
      Node rightAnkle = extractNodes(group, MANNEQUIN_RIGHT_ANKLE_PREFIX, null);
      TransformGroup rightLegGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_KNEE_PREFIX, rightLeg, rightAnkle, rightFootGroup);
      Node rightThigh = extractNodes(group, MANNEQUIN_RIGHT_THIGH_PREFIX, null);
      Node rightKnee = extractNodes(group, MANNEQUIN_RIGHT_KNEE_PREFIX, null);
      TransformGroup rightThighGroup = createPickableTransformGroup(MANNEQUIN_RIGHT_HIP_PREFIX, rightThigh, rightKnee, rightLegGroup);

      // Pelvis
      Node pelvis = extractNodes(group, MANNEQUIN_PELVIS_PREFIX, null);
      Node leftHip = extractNodes(group, MANNEQUIN_LEFT_HIP_PREFIX, null);
      Node rightHip = extractNodes(group, MANNEQUIN_RIGHT_HIP_PREFIX, null);
      TransformGroup pelvisGroup = createPickableTransformGroup(MANNEQUIN_ABDOMEN_PELVIS_PREFIX, pelvis, leftHip, leftThighGroup, rightHip, rightThighGroup);

      Node abdomen = extractNodes(group, MANNEQUIN_ABDOMEN_PREFIX, null);
      group.addChild(abdomen);
      group.addChild(chestGroup);
      group.addChild(pelvisGroup);
    } else {
      // Reorganize rotating openings
      updateSimpleDeformableModelHierarchy(group, null, HINGE_PREFIX, OPENING_ON_HINGE_PREFIX, WINDOW_PANE_ON_HINGE_PREFIX);
      updateSimpleDeformableModelHierarchy(group, null, BALL_PREFIX, ARM_ON_BALL_PREFIX, null);
      // Reorganize sliding openings
      updateSimpleDeformableModelHierarchy(group, UNIQUE_RAIL_PREFIX, RAIL_PREFIX, OPENING_ON_RAIL_PREFIX, WINDOW_PANE_ON_RAIL_PREFIX);
    }
  }

  private void updateSimpleDeformableModelHierarchy(Group group, String uniqueReferenceNodePrefix, String referenceNodePrefix,
                                                    String openingPrefix, String openingPanePrefix) {
    if (containsNode(group, openingPrefix + 1)
        || (openingPanePrefix != null && containsNode(group, openingPanePrefix + 1))) {
      if (containsNode(group, referenceNodePrefix + 1)) {
        // Reorganize openings with multiple reference nodes
        int i = 1;
        do {
          Node referenceNode = extractNodes(group, referenceNodePrefix + i, null);
          Node opening = extractNodes(group, openingPrefix + i, null);
          Node openingPane = openingPanePrefix != null ? extractNodes(group, openingPanePrefix + i, null) : null;
          TransformGroup openingGroup = createPickableTransformGroup(referenceNodePrefix + i, opening, openingPane);
          group.addChild(referenceNode);
          group.addChild(openingGroup);
          i++;
        } while (containsNode(group, referenceNodePrefix + i)
            && (containsNode(group, openingPrefix + i)
                || (openingPanePrefix != null && containsNode(group, openingPanePrefix + i))));
      } else if (uniqueReferenceNodePrefix != null
                 && containsNode(group, uniqueReferenceNodePrefix)) {
        // Reorganize openings with a unique reference node
        Node referenceNode = extractNodes(group, uniqueReferenceNodePrefix, null);
        group.addChild(referenceNode);
        int i = 1;
        do {
          Node opening = extractNodes(group, openingPrefix + i, null);
          Node openingPane = extractNodes(group, openingPanePrefix + i, null);
          group.addChild(createPickableTransformGroup(referenceNodePrefix + i, opening, openingPane));
          i++;
        } while (containsNode(group, openingPrefix + i)
                 || containsNode(group, openingPanePrefix + i));
      }
    }
  }

  /**
   * Returns <code>true</code> if the given <code>node</code> or a node in its hierarchy
   * contains a node which name, stored in user data, starts with <code>prefix</code>.
   */
  public boolean containsNode(Node node, String prefix) {
    Object userData = node.getUserData();
    if (userData instanceof String
        && ((String)userData).startsWith(prefix)) {
      return true;
    }
    if (node instanceof Group) {
      Group group = (Group)node;
      for (int i = group.numChildren() - 1; i >= 0; i--) {
        if (containsNode((Node)group.getChild(i), prefix)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Searches among the given <code>node</code> and its children the nodes which name, stored in user data, starts with <code>name</code>,
   * then returns a group containing the found nodes.
   */
  private Group extractNodes(Node node, String name, Group destinationGroup) {
    if (node.getUserData() != null
        && ((String)node.getUserData()).startsWith(name)) {
      ((Group)node.getParent()).removeChild(node);
      if (destinationGroup == null) {
        destinationGroup = new Group();
      }
      destinationGroup.addChild(node);
    }
    if (node instanceof Group) {
      // Enumerate children
      Group group = (Group)node;
      for (int i = group.numChildren() - 1; i >= 0; i--) {
        destinationGroup = extractNodes((Node)group.getChild(i), name, destinationGroup);
      }
    }
    return destinationGroup;
  }

  /**
   * Returns a pickable group with its <code>children</code> and the given reference node as user data.
   */
  private TransformGroup createPickableTransformGroup(String deformableGroupPrefix, Node ... children) {
    TransformGroup transformGroup = new TransformGroup();
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    transformGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
    transformGroup.setUserData(deformableGroupPrefix + DEFORMABLE_TRANSFORM_GROUP_SUFFIX);
    // Store the node around which objects should turn
    for (Node child : children) {
      if (child != null) {
        transformGroup.addChild(child);
      }
    }
    return transformGroup;
  }

  /**
   * Return <code>true</code> if the given <code>node</code> or its children contains at least a deformable group.
   * @param node  the root of a model
   */
  public boolean containsDeformableNode(Node node) {
    if (node instanceof TransformGroup
        && node.getUserData() instanceof String
        && ((String)node.getUserData()).endsWith(DEFORMABLE_TRANSFORM_GROUP_SUFFIX)) {
      return true;
    } else if (node instanceof Group) {
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        if (containsDeformableNode((Node)enumeration.nextElement())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return <code>true</code> if the given <code>node</code> or its children contains is a deformed transformed group.
   * @param node  a node
   */
  private boolean isDeformed(Node node) {
    if (node instanceof TransformGroup
        && node.getUserData() instanceof String
        && ((String)node.getUserData()).endsWith(DEFORMABLE_TRANSFORM_GROUP_SUFFIX)) {
      Transform3D transform = new Transform3D();
      ((TransformGroup)node).getTransform(transform);
      return (transform.getBestType() & Transform3D.IDENTITY) != Transform3D.IDENTITY;
    } else if (node instanceof Group) {
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        if (isDeformed((Node)enumeration.nextElement())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Turns off light nodes of <code>node</code> children,
   * modulates textures if needed and allows shapes to change their pickable property.
   */
  private void turnOffLightsShareAndModulateTextures(Node node,
                                                     Map<Texture, Texture> replacedTextures) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        turnOffLightsShareAndModulateTextures((Node)enumeration.nextElement(), replacedTextures);
      }
    } else if (node instanceof Link) {
      turnOffLightsShareAndModulateTextures(((Link)node).getSharedGroup(), replacedTextures);
    } else if (node instanceof Light) {
      ((Light)node).setEnable(false);
    } else if (node instanceof Shape3D) {
      node.setCapability(Node.ALLOW_PICKABLE_WRITE);
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        Texture texture = appearance.getTexture();
        if (texture != null) {
          // Share textures data as much as possible requesting TextureManager#shareTexture the less often possible
          Texture sharedTexture = replacedTextures.get(texture);
          if (sharedTexture == null) {
            sharedTexture = TextureManager.getInstance().shareTexture(texture);
            replacedTextures.put(texture, sharedTexture);
          }
          if (sharedTexture != texture) {
            appearance.setTexture(sharedTexture);
          }
          TextureAttributes textureAttributes = appearance.getTextureAttributes();
          if (textureAttributes == null) {
            // Mix texture and shape color
            textureAttributes = new TextureAttributes();
            textureAttributes.setTextureMode(TextureAttributes.MODULATE);
            appearance.setTextureAttributes(textureAttributes);
            // Check shape color is white
            Material material = appearance.getMaterial();
            if (material == null) {
              appearance.setMaterial((Material)DEFAULT_MATERIAL.cloneNodeComponent(true));
            } else {
              Color3f color = new Color3f();
              DEFAULT_MATERIAL.getDiffuseColor(color);
              material.setDiffuseColor(color);
              DEFAULT_MATERIAL.getAmbientColor(color);
              material.setAmbientColor(color);
            }
          }

          // If texture image supports transparency
          if (TextureManager.getInstance().isTextureTransparent(sharedTexture)) {
            if (appearance.getTransparencyAttributes() == null) {
              // Add transparency attributes to ensure transparency works
              appearance.setTransparencyAttributes(
                  new TransparencyAttributes(TransparencyAttributes.NICEST, 0));
            }
          }
        }
      }
    }
  }

  /**
   * Ensures that all the appearance of the children shapes of the
   * given <code>node</code> have a name.
   */
  public void checkAppearancesName(Node node) {
    // Search appearances used by node shapes keeping their enumeration order
    Set<Appearance> appearances = new LinkedHashSet<Appearance>();
    searchAppearances(node, appearances);
    int i = 0;
    for (Appearance appearance : appearances) {
      try {
        if (appearance.getName() == null) {
          appearance.setName("Texture_" + ++i);
        }
      } catch (NoSuchMethodError ex) {
        // Don't support HomeMaterial with Java 3D < 1.4 where appearance name was added
        break;
      }
    }
  }

  /**
   * Returns the materials used by the children shapes of the given <code>node</code>.
   */
  public HomeMaterial [] getMaterials(Node node) {
    return getMaterials(node, null);
  }

  /**
   * Returns the materials used by the children shapes of the given <code>node</code>,
   * attributing their <code>creator</code> to them.
   */
  public HomeMaterial [] getMaterials(Node node, String creator) {
    // Search appearances used by node shapes
    Set<Appearance> appearances = new HashSet<Appearance>();
    searchAppearances(node, appearances);
    Set<HomeMaterial> materials = new TreeSet<HomeMaterial>(new Comparator<HomeMaterial>() {
        public int compare(HomeMaterial m1, HomeMaterial m2) {
          String name1 = m1.getName();
          String name2 = m2.getName();
          if (name1 != null) {
            if (name2 != null) {
              return name1.compareTo(name2);
            } else {
              return 1;
            }
          } else if (name2 != null) {
            return -1;
          } else {
            return 0;
          }
        }
      });
    for (Appearance appearance : appearances) {
      Integer color = null;
      Float   shininess = null;
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f diffuseColor = new Color3f();
        material.getDiffuseColor(diffuseColor);
        color = 0xFF000000
            | ((int)(diffuseColor.x * 255) << 16)
            | ((int)(diffuseColor.y * 255) << 8)
            | (int)(diffuseColor.z * 255);
        shininess = material.getShininess() / 128;
      }
      Texture appearanceTexture = appearance.getTexture();
      HomeTexture texture = null;
      if (appearanceTexture != null) {
        URL textureImageUrl = (URL)appearanceTexture.getUserData();
        if (textureImageUrl != null) {
          Content textureImage = new SimpleURLContent(textureImageUrl);
          // Extract image name
          String textureImageName = textureImageUrl.getFile();
          textureImageName = textureImageName.substring(textureImageName.lastIndexOf('/') + 1);
          int lastPoint = textureImageName.lastIndexOf('.');
          if (lastPoint != -1) {
            textureImageName = textureImageName.substring(0, lastPoint);
          }
          texture = new HomeTexture(new CatalogTexture(null, textureImageName, textureImage, -1, -1, creator));
        }
      }
      try {
        materials.add(new HomeMaterial(appearance.getName(), color, texture, shininess));
      } catch (NoSuchMethodError ex) {
        // Don't support HomeMaterial with Java 3D < 1.4 where getName was added
        return new HomeMaterial [0];
      }
    }
    return materials.toArray(new HomeMaterial [materials.size()]);
  }

  private void searchAppearances(Node node, Set<Appearance> appearances) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        searchAppearances((Node)enumeration.nextElement(), appearances);
      }
    } else if (node instanceof Link) {
      searchAppearances(((Link)node).getSharedGroup(), appearances);
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        appearances.add(appearance);
      }
    }
  }

  /**
   * Returns the AWT shape matching the given cut out shape if not <code>null</code>
   * or the 2D area of the 3D shapes children of the <code>node</code>
   * projected on its front side. The returned area is normalized in a 1 unit square
   * centered at the origin.
   */
  Area getFrontArea(String cutOutShape, Node node) {
    Area frontArea;
    if (cutOutShape != null) {
      frontArea = new Area(getShape(cutOutShape));
      frontArea.transform(AffineTransform.getScaleInstance(1, -1));
      frontArea.transform(AffineTransform.getTranslateInstance(-.5, .5));
    } else {
      int vertexCount = getVertexCount(node);
      if (vertexCount < 1000000) {
        Area frontAreaWithHoles = new Area();
        computeBottomOrFrontArea(node, frontAreaWithHoles, new Transform3D(), false, false);
        // Remove holes and duplicated points
        frontArea = new Area();
        List<float []> currentPathPoints = new ArrayList<float[]>();
        float [] previousRoomPoint = null;
        for (PathIterator it = frontAreaWithHoles.getPathIterator(null, 1); !it.isDone(); it.next()) {
          float [] areaPoint = new float[2];
          switch (it.currentSegment(areaPoint)) {
            case PathIterator.SEG_MOVETO :
            case PathIterator.SEG_LINETO :
              if (previousRoomPoint == null
                  || areaPoint [0] != previousRoomPoint [0]
                  || areaPoint [1] != previousRoomPoint [1]) {
                currentPathPoints.add(areaPoint);
              }
              previousRoomPoint = areaPoint;
              break;
            case PathIterator.SEG_CLOSE :
              if (currentPathPoints.get(0) [0] == previousRoomPoint [0]
                  && currentPathPoints.get(0) [1] == previousRoomPoint [1]) {
                currentPathPoints.remove(currentPathPoints.size() - 1);
              }
              if (currentPathPoints.size() > 2) {
                float [][] pathPoints =
                    currentPathPoints.toArray(new float [currentPathPoints.size()][]);
                Room subRoom = new Room(pathPoints);
                if (subRoom.getArea() > 0) {
                  if (!subRoom.isClockwise()) {
                    // Ignore clockwise points that match holes
                    GeneralPath currentPath = new GeneralPath();
                    currentPath.moveTo(pathPoints [0][0], pathPoints [0][1]);
                    for (int i = 1; i < pathPoints.length; i++) {
                      currentPath.lineTo(pathPoints [i][0], pathPoints [i][1]);
                    }
                    currentPath.closePath();
                    frontArea.add(new Area(currentPath));
                  }
                }
              }
              currentPathPoints.clear();
              previousRoomPoint = null;
              break;
          }
        }
        Rectangle2D bounds = frontAreaWithHoles.getBounds2D();
        frontArea.transform(AffineTransform.getTranslateInstance(-bounds.getCenterX(), -bounds.getCenterY()));
        frontArea.transform(AffineTransform.getScaleInstance(1 / bounds.getWidth(), 1 / bounds.getHeight()));
      } else {
        frontArea = new Area(new Rectangle2D.Float(-.5f, -.5f, 1, 1));
      }
    }
    return frontArea;
  }


  /**
   * Returns the 2D area of the 3D shapes children of the given <code>node</code>
   * projected on the floor (plan y = 0).
   */
  public Area getAreaOnFloor(Node node) {
    Area modelAreaOnFloor;
    int vertexCount = getVertexCount(node);
    if (vertexCount < 10000) {
      modelAreaOnFloor = new Area();
      computeBottomOrFrontArea(node, modelAreaOnFloor, new Transform3D(), true, true);
    } else {
      List<float []> vertices = new ArrayList<float[]>(vertexCount);
      computeVerticesOnFloor(node, vertices, new Transform3D());
      if (vertices.size() > 0) {
        float [][] surroundingPolygon = getSurroundingPolygon(vertices.toArray(new float [vertices.size()][]));
        GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, surroundingPolygon.length);
        generalPath.moveTo(surroundingPolygon [0][0], surroundingPolygon [0][1]);
        for (int i = 0; i < surroundingPolygon.length; i++) {
          generalPath.lineTo(surroundingPolygon [i][0], surroundingPolygon [i][1]);
        }
        generalPath.closePath();
        modelAreaOnFloor = new Area(generalPath);
      } else {
        modelAreaOnFloor = new Area();
      }
    }
    return modelAreaOnFloor;
  }

  /**
   * Returns the total count of vertices in all geometries.
   */
  private int getVertexCount(Node node) {
    int count = 0;
    if (node instanceof Group) {
      // Enumerate all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        count += getVertexCount((Node)enumeration.nextElement());
      }
    } else if (node instanceof Link) {
      count = getVertexCount(((Link)node).getSharedGroup());
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null
          ? appearance.getRenderingAttributes() : null;
      if (renderingAttributes == null
          || renderingAttributes.getVisible()) {
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          Geometry geometry = shape.getGeometry(i);
          if (geometry instanceof GeometryArray) {
            count += ((GeometryArray)geometry).getVertexCount();
          }
        }
      }
    }
    return count;
  }

  /**
   * Computes the 2D area on floor or on front side of the 3D shapes children of <code>node</code>.
   */
  private void computeBottomOrFrontArea(Node node,
                                        Area nodeArea,
                                        Transform3D parentTransformations,
                                        boolean ignoreTransparentShapes,
                                        boolean bottom) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Compute all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        computeBottomOrFrontArea((Node)enumeration.nextElement(), nodeArea, parentTransformations, ignoreTransparentShapes, bottom);
      }
    } else if (node instanceof Link) {
      computeBottomOrFrontArea(((Link)node).getSharedGroup(), nodeArea, parentTransformations, ignoreTransparentShapes, bottom);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null
          ? appearance.getTransparencyAttributes() : null;
      if ((renderingAttributes == null
            || renderingAttributes.getVisible())
          && (!ignoreTransparentShapes
              || transparencyAttributes == null
              || transparencyAttributes.getTransparency() < 1)) {
        // Compute shape geometries area
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          computeBottomOrFrontGeometryArea(shape.getGeometry(i), nodeArea, parentTransformations, bottom);
        }
      }
    }
  }

  /**
   * Computes the bottom area of a 3D geometry if <code>bottom</code> is <code>true</code>,
   * and the front area if not.
   */
  private void computeBottomOrFrontGeometryArea(Geometry geometry,
                                                Area nodeArea,
                                                Transform3D parentTransformations,
                                                boolean bottom) {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;
      int vertexCount = geometryArray.getVertexCount();
      float [] vertices = new float [vertexCount * 2];
      Point3f vertex = new Point3f();
      if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
        if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
          float [] vertexData = geometryArray.getInterleavedVertices();
          int vertexSize = vertexData.length / vertexCount;
          // Store vertices coordinates
          for (int index = 0, i = vertexSize - 3; index < vertices.length; i += vertexSize) {
            vertex.x = vertexData [i];
            vertex.y = vertexData [i + 1];
            vertex.z = vertexData [i + 2];
            parentTransformations.transform(vertex);
            vertices [index++] = vertex.x;
            if (bottom) {
              vertices [index++] = vertex.z;
            } else {
              vertices [index++] = vertex.y;
            }
          }
        } else {
          // Store vertices coordinates
          float [] vertexCoordinates = geometryArray.getCoordRefFloat();
          for (int index = 0, i = 0; index < vertices.length; i += 3) {
            vertex.x = vertexCoordinates [i];
            vertex.y = vertexCoordinates [i + 1];
            vertex.z = vertexCoordinates [i + 2];
            parentTransformations.transform(vertex);
            vertices [index++] = vertex.x;
            if (bottom) {
              vertices [index++] = vertex.z;
            } else {
              vertices [index++] = vertex.y;
            }
          }
        }
      } else {
        // Store vertices coordinates
        for (int index = 0, i = 0; index < vertices.length; i++) {
          geometryArray.getCoordinate(i, vertex);
          parentTransformations.transform(vertex);
          vertices [index++] = vertex.x;
          if (bottom) {
            vertices [index++] = vertex.z;
          } else {
            vertices [index++] = vertex.y;
          }
        }
      }

      // Create path from triangles or quadrilaterals of geometry
      GeneralPath geometryPath = null;
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedTriangleArray) {
          IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, triangleIndex = 0, n = triangleArray.getIndexCount(); i < n; i += 3) {
            addIndexedTriangleToPath(triangleArray, i, i + 1, i + 2, vertices,
                geometryPath, triangleIndex++, nodeArea);
          }
        } else if (geometryArray instanceof IndexedQuadArray) {
          IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, quadrilateralIndex = 0, n = quadArray.getIndexCount(); i < n; i += 4) {
            addIndexedQuadrilateralToPath(quadArray, i, i + 1, i + 2, i + 3, vertices,
                geometryPath, quadrilateralIndex++, nodeArea);
          }
        } else if (geometryArray instanceof IndexedGeometryStripArray) {
          IndexedGeometryStripArray geometryStripArray = (IndexedGeometryStripArray)geometryArray;
          int [] stripIndexCounts = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripIndexCounts(stripIndexCounts);
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          int initialIndex = 0;

          if (geometryStripArray instanceof IndexedTriangleStripArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripIndexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripIndexCounts [strip] - 2, j = 0; i < n; i++, j++) {
                if (j % 2 == 0) {
                  addIndexedTriangleToPath(geometryStripArray, i, i + 1, i + 2, vertices,
                      geometryPath, triangleIndex++, nodeArea);
                } else { // Vertices of odd triangles are in reverse order
                  addIndexedTriangleToPath(geometryStripArray, i, i + 2, i + 1, vertices,
                      geometryPath, triangleIndex++, nodeArea);
                }
              }
              initialIndex += stripIndexCounts [strip];
            }
          } else if (geometryStripArray instanceof IndexedTriangleFanArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripIndexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripIndexCounts [strip] - 2; i < n; i++) {
                addIndexedTriangleToPath(geometryStripArray, initialIndex, i + 1, i + 2, vertices,
                    geometryPath, triangleIndex++, nodeArea);
              }
              initialIndex += stripIndexCounts [strip];
            }
          }
        }
      } else {
        if (geometryArray instanceof TriangleArray) {
          TriangleArray triangleArray = (TriangleArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, triangleIndex = 0; i < vertexCount; i += 3) {
            addTriangleToPath(triangleArray, i, i + 1, i + 2, vertices,
                geometryPath, triangleIndex++, nodeArea);
          }
        } else if (geometryArray instanceof QuadArray) {
          QuadArray quadArray = (QuadArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, quadrilateralIndex = 0; i < vertexCount; i += 4) {
            addQuadrilateralToPath(quadArray, i, i + 1, i + 2, i + 3, vertices,
                geometryPath, quadrilateralIndex++, nodeArea);
          }
        } else if (geometryArray instanceof GeometryStripArray) {
          GeometryStripArray geometryStripArray = (GeometryStripArray)geometryArray;
          int [] stripVertexCounts = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripVertexCounts(stripVertexCounts);
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          int initialIndex = 0;

          if (geometryStripArray instanceof TriangleStripArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripVertexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripVertexCounts [strip] - 2, j = 0; i < n; i++, j++) {
                if (j % 2 == 0) {
                  addTriangleToPath(geometryStripArray, i, i + 1, i + 2, vertices,
                      geometryPath, triangleIndex++, nodeArea);
                } else { // Vertices of odd triangles are in reverse order
                  addTriangleToPath(geometryStripArray, i, i + 2, i + 1, vertices,
                      geometryPath, triangleIndex++, nodeArea);
                }
              }
              initialIndex += stripVertexCounts [strip];
            }
          } else if (geometryStripArray instanceof TriangleFanArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripVertexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripVertexCounts [strip] - 2; i < n; i++) {
                addTriangleToPath(geometryStripArray, initialIndex, i + 1, i + 2, vertices,
                    geometryPath, triangleIndex++, nodeArea);
              }
              initialIndex += stripVertexCounts [strip];
            }
          }
        }
      }

      if (geometryPath != null) {
        nodeArea.add(new Area(geometryPath));
      }
    }
  }

  /**
   * Adds to <code>geometryPath</code> the triangle joining vertices at
   * vertexIndex1, vertexIndex2, vertexIndex3 indices.
   */
  private void addIndexedTriangleToPath(IndexedGeometryArray geometryArray,
                                    int vertexIndex1, int vertexIndex2, int vertexIndex3,
                                    float [] vertices,
                                    GeneralPath geometryPath, int triangleIndex, Area nodeArea) {
    addTriangleToPath(geometryArray, geometryArray.getCoordinateIndex(vertexIndex1),
        geometryArray.getCoordinateIndex(vertexIndex2),
        geometryArray.getCoordinateIndex(vertexIndex3), vertices, geometryPath, triangleIndex, nodeArea);
  }

  /**
   * Adds to <code>geometryPath</code> the quadrilateral joining vertices at
   * vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4 indices.
   */
  private void addIndexedQuadrilateralToPath(IndexedGeometryArray geometryArray,
                                         int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4,
                                         float [] vertices,
                                         GeneralPath geometryPath, int quadrilateralIndex, Area nodeArea) {
    addQuadrilateralToPath(geometryArray, geometryArray.getCoordinateIndex(vertexIndex1),
        geometryArray.getCoordinateIndex(vertexIndex2),
        geometryArray.getCoordinateIndex(vertexIndex3),
        geometryArray.getCoordinateIndex(vertexIndex4), vertices, geometryPath, quadrilateralIndex, nodeArea);
  }

  /**
   * Adds to <code>geometryPath</code> the triangle joining vertices at
   * vertexIndex1, vertexIndex2, vertexIndex3 indices,
   * only if the triangle has a positive orientation.
   */
  private void addTriangleToPath(GeometryArray geometryArray,
                             int vertexIndex1, int vertexIndex2, int vertexIndex3,
                             float [] vertices,
                             GeneralPath geometryPath, int triangleIndex, Area nodeArea) {
    float xVertex1 = vertices [2 * vertexIndex1];
    float yVertex1 = vertices [2 * vertexIndex1 + 1];
    float xVertex2 = vertices [2 * vertexIndex2];
    float yVertex2 = vertices [2 * vertexIndex2 + 1];
    float xVertex3 = vertices [2 * vertexIndex3];
    float yVertex3 = vertices [2 * vertexIndex3 + 1];
    if ((xVertex2 - xVertex1) * (yVertex3 - yVertex2) - (yVertex2 - yVertex1) * (xVertex3 - xVertex2) > 0) {
      if (triangleIndex > 0 && triangleIndex % 1000 == 0) {
        // Add now current path to area otherwise area gets too slow
        nodeArea.add(new Area(geometryPath));
        geometryPath.reset();
      }
      geometryPath.moveTo(xVertex1, yVertex1);
      geometryPath.lineTo(xVertex2, yVertex2);
      geometryPath.lineTo(xVertex3, yVertex3);
      geometryPath.closePath();
    }
  }

  /**
   * Adds to <code>geometryPath</code> the quadrilateral joining vertices at
   * vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4 indices,
   * only if the quadrilateral has a positive orientation.
   */
  private void addQuadrilateralToPath(GeometryArray geometryArray,
                                      int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4,
                                      float [] vertices,
                                      GeneralPath geometryPath, int quadrilateralIndex, Area nodeArea) {
    float xVertex1 = vertices [2 * vertexIndex1];
    float yVertex1 = vertices [2 * vertexIndex1 + 1];
    float xVertex2 = vertices [2 * vertexIndex2];
    float yVertex2 = vertices [2 * vertexIndex2 + 1];
    float xVertex3 = vertices [2 * vertexIndex3];
    float yVertex3 = vertices [2 * vertexIndex3 + 1];
    if ((xVertex2 - xVertex1) * (yVertex3 - yVertex2) - (yVertex2 - yVertex1) * (xVertex3 - xVertex2) > 0) {
      if (quadrilateralIndex > 0 && quadrilateralIndex % 1000 == 0) {
        // Add now current path to area otherwise area gets too slow
        nodeArea.add(new Area(geometryPath));
        geometryPath.reset();
      }
      geometryPath.moveTo(xVertex1, yVertex1);
      geometryPath.lineTo(xVertex2, yVertex2);
      geometryPath.lineTo(xVertex3, yVertex3);
      geometryPath.lineTo(vertices [2 * vertexIndex4], vertices [2 * vertexIndex4 + 1]);
      geometryPath.closePath();
    }
  }

  /**
   * Computes the vertices coordinates projected on floor of the 3D shapes children of <code>node</code>.
   */
  private void computeVerticesOnFloor(Node node, List<float []> vertices, Transform3D parentTransformations) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Compute all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        computeVerticesOnFloor((Node)enumeration.nextElement(), vertices, parentTransformations);
      }
    } else if (node instanceof Link) {
      computeVerticesOnFloor(((Link)node).getSharedGroup(), vertices, parentTransformations);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null
          ? appearance.getTransparencyAttributes() : null;
      if ((renderingAttributes == null
            || renderingAttributes.getVisible())
          && (transparencyAttributes == null
              || transparencyAttributes.getTransparency() < 1)) {
        // Compute shape geometries area
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          Geometry geometry = shape.getGeometry(i);
          if (geometry instanceof GeometryArray) {
            GeometryArray geometryArray = (GeometryArray)geometry;
            int vertexCount = geometryArray.getVertexCount();
            Point3f vertex = new Point3f();
            if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
              if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
                float [] vertexData = geometryArray.getInterleavedVertices();
                int vertexSize = vertexData.length / vertexCount;
                // Store vertices coordinates
                for (int index = 0, j = vertexSize - 3; index < vertexCount; j += vertexSize, index++) {
                  vertex.x = vertexData [j];
                  vertex.y = vertexData [j + 1];
                  vertex.z = vertexData [j + 2];
                  parentTransformations.transform(vertex);
                  vertices.add(new float [] {vertex.x, vertex.z});
                }
              } else {
                // Store vertices coordinates
                float [] vertexCoordinates = geometryArray.getCoordRefFloat();
                for (int index = 0, j = 0; index < vertexCount; j += 3, index++) {
                  vertex.x = vertexCoordinates [j];
                  vertex.y = vertexCoordinates [j + 1];
                  vertex.z = vertexCoordinates [j + 2];
                  parentTransformations.transform(vertex);
                  vertices.add(new float [] {vertex.x, vertex.z});
                }
              }
            } else {
              // Store vertices coordinates
              for (int index = 0, j = 0; index < vertexCount; j++, index++) {
                geometryArray.getCoordinate(j, vertex);
                parentTransformations.transform(vertex);
                vertices.add(new float [] {vertex.x, vertex.z});
              }
            }
          }
        }
      }
    }
  }

  /**
   * Returns the convex polygon that surrounds the given <code>vertices</code>.
   * From Andrew's monotone chain 2D convex hull algorithm described at
   * http://softsurfer.com/Archive/algorithm%5F0109/algorithm%5F0109.htm
   */
  private float [][] getSurroundingPolygon(float [][] vertices) {
    Arrays.sort(vertices, new Comparator<float []> () {
        public int compare(float [] vertex1, float [] vertex2) {
          if (vertex1 [0] == vertex2 [0]) {
            return (int)Math.signum(vertex2 [1] - vertex1 [1]);
          } else {
            return (int)Math.signum(vertex2 [0] - vertex1 [0]);
          }
        }
      });
    float [][] polygon = new float [vertices.length][];
    // The output array polygon [] will be used as the stack
    int bottom = 0, top = -1; // indices for bottom and top of the stack
    int i; // array scan index

    // Get the indices of points with min x-coord and min|max y-coord
    int minMin = 0, minMax;
    float xmin = vertices [0][0];
    for (i = 1; i < vertices.length; i++) {
      if (vertices [i][0] != xmin) {
        break;
      }
    }
    minMax = i - 1;
    if (minMax == vertices.length - 1) {
      // Degenerate case: all x-coords == xmin
      polygon [++top] = vertices [minMin];
      if (vertices [minMax][1] != vertices [minMin][1]) {
        // A nontrivial segment
        polygon [++top] = vertices [minMax];
      }
      // Add polygon end point
      polygon [++top] = vertices [minMin];
      float [][] surroundingPolygon = new float [top + 1][];
      System.arraycopy(polygon, 0, surroundingPolygon, 0, surroundingPolygon.length);
      return surroundingPolygon;
    }

    // Get the indices of points with max x-coord and min|max y-coord
    int maxMin, maxMax = vertices.length - 1;
    float xMax = vertices [vertices.length - 1][0];
    for (i = vertices.length - 2; i >= 0; i--) {
      if (vertices [i][0] != xMax) {
        break;
      }
    }
    maxMin = i + 1;

    // Compute the lower hull on the stack polygon
    polygon [++top] = vertices [minMin]; // push minmin point onto stack
    i = minMax;
    while (++i <= maxMin) {
      // The lower line joins points [minmin] with points [maxmin]
      if (isLeft(vertices [minMin], vertices [maxMin], vertices [i]) >= 0 && i < maxMin) {
        // ignore points [i] above or on the lower line
        continue;
      }

      while (top > 0) // There are at least 2 points on the stack
      {
        // Test if points [i] is left of the line at the stack top
        if (isLeft(polygon [top - 1], polygon [top], vertices [i]) > 0) {
          break; // points [i] is a new hull vertex
        } else {
          top--; // pop top point off stack
        }
      }
      polygon [++top] = vertices [i]; // push points [i] onto stack
    }

    // Next, compute the upper hull on the stack polygon above the bottom hull
    // If distinct xmax points
    if (maxMax != maxMin) {
      // Push maxmax point onto stack
      polygon [++top] = vertices [maxMax];
    }
    // The bottom point of the upper hull stack
    bottom = top;
    i = maxMin;
    while (--i >= minMax) {
      // The upper line joins points [maxmax] with points [minmax]
      if (isLeft(vertices [maxMax], vertices [minMax], vertices [i]) >= 0 && i > minMax) {
        // Ignore points [i] below or on the upper line
        continue;
      }

      // At least 2 points on the upper stack
      while (top > bottom)
      {
        // Test if points [i] is left of the line at the stack top
        if (isLeft(polygon [top - 1], polygon [top], vertices [i]) > 0) {
          // points [i] is a new hull vertex
          break;
        } else {
          // Pop top point off stack
          top--;
        }
      }
      // Push points [i] onto stack
      polygon [++top] = vertices [i];
    }
    if (minMax != minMin) {
      // Push joining endpoint onto stack
      polygon [++top] = vertices [minMin];
    }

    float [][] surroundingPolygon = new float [top + 1][];
    System.arraycopy(polygon, 0, surroundingPolygon, 0, surroundingPolygon.length);
    return surroundingPolygon;
  }

  private float isLeft(float [] vertex0, float [] vertex1, float [] vertex2) {
    return (vertex1 [0] - vertex0 [0]) * (vertex2 [1] - vertex0 [1])
         - (vertex2 [0] - vertex0 [0]) * (vertex1 [1] - vertex0 [1]);
  }

  /**
   * Returns the area on the floor of the given staircase.
   */
  public Area getAreaOnFloor(HomePieceOfFurniture staircase) {
    if (staircase.getStaircaseCutOutShape() == null) {
      throw new IllegalArgumentException("No cut out shape associated to piece");
    }
    Shape shape = getShape(staircase.getStaircaseCutOutShape());
    Area staircaseArea = new Area(shape);
    if (staircase.isModelMirrored()) {
      staircaseArea = getMirroredArea(staircaseArea);
    }
    AffineTransform staircaseTransform = AffineTransform.getTranslateInstance(
        staircase.getX() - staircase.getWidth() / 2,
        staircase.getY() - staircase.getDepth() / 2);
    staircaseTransform.concatenate(AffineTransform.getRotateInstance(staircase.getAngle(),
        staircase.getWidth() / 2, staircase.getDepth() / 2));
    staircaseTransform.concatenate(AffineTransform.getScaleInstance(staircase.getWidth(), staircase.getDepth()));
    staircaseArea.transform(staircaseTransform);
    return staircaseArea;
  }

  /**
   * Returns the mirror area of the given <code>area</code>.
   */
  private Area getMirroredArea(Area area) {
    // As applying a -1 scale transform reverses the holes / non holes interpretation of the points,
    // we have to create a mirrored shape by parsing points
    GeneralPath mirrorPath = new GeneralPath();
    float [] point = new float[6];
    for (PathIterator it = area.getPathIterator(null); !it.isDone(); it.next()) {
      switch (it.currentSegment(point)) {
        case PathIterator.SEG_MOVETO :
          mirrorPath.moveTo(1 - point[0], point[1]);
          break;
        case PathIterator.SEG_LINETO :
          mirrorPath.lineTo(1 - point[0], point[1]);
          break;
        case PathIterator.SEG_QUADTO :
          mirrorPath.quadTo(1 - point[0], point[1], 1 - point[2], point[3]);
          break;
        case PathIterator.SEG_CUBICTO :
          mirrorPath.curveTo(1 - point[0], point[1], 1 - point[2], point[3], 1 - point[4], point[5]);
          break;
        case PathIterator.SEG_CLOSE :
          mirrorPath.closePath();
          break;
      }
    }
    return new Area(mirrorPath);
  }

  /**
   * Returns the AWT shape matching the given <a href="http://www.w3.org/TR/SVG/paths.html">SVG path shape</a>.
   */
  public Shape getShape(String svgPathShape) {
    return ShapeTools.getShape(svgPathShape);
  }

  /**
   * An observer that receives model loading notifications.
   */
  public static interface ModelObserver {
    public void modelUpdated(BranchGroup modelRoot);

    public void modelError(Exception ex);
  }
}
