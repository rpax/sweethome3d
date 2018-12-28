/*
 * PhotoRenderer.java 22 janv. 2009
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
package com.eteks.sweethome3d.j3d;

import java.awt.AlphaComposite;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.IndexedLineStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
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
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.sunflow.PluginRegistry;
import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.Instance;
import org.sunflow.core.ParameterList;
import org.sunflow.core.ParameterList.InterpolationType;
import org.sunflow.core.light.SphereLight;
import org.sunflow.core.light.SunSkyLight;
import org.sunflow.core.primitive.TriangleMesh;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;
import org.sunflow.system.UI;
import org.sunflow.system.ui.SilentInterface;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;

/**
 * A renderer able to create a photo realistic image of a home.
 * @author Emmanuel Puybaret
 * @author Fr�d�ric Mantegazza (Sun location algorithm)
 */
public class PhotoRenderer {
  public enum Quality {LOW, HIGH}

  private final Home home;
  private final Object3DFactory object3dFactory;
  private final Quality quality;
  private final Compass compass;
  private final int homeLightColor;

  private final SunflowAPI sunflow;
  private boolean useSunSky;
  private boolean useSunskyLight;
  private String  sunSkyLightName;
  private String  sunLightName;
  private final Map<Selectable, String []>         homeItemsNames     = new HashMap<Selectable, String []>();
  private final Map<TransparentTextureKey, String> textureImagesCache = new HashMap<TransparentTextureKey, String>();
  private Thread renderingThread;

  static {
    // Ignore logs
    UI.set(new SilentInterface());
    // Use small triangles for better rendering
    TriangleMesh.setSmallTriangles(true);
    PluginRegistry.lightSourcePlugins.registerPlugin("sphere", SphereLightWithNoRepresentation.class);
  }

  /**
   * Creates an instance ready to render the scene matching the given <code>home</code>.
   * @throws IOException if texture image files required in the scene couldn't be created.
   */
  public PhotoRenderer(Home home, Quality quality) throws IOException {
    this(home, new PhotoObject3DFactory(), quality);
  }

  /**
   * Creates an instance ready to render the scene matching the given <code>home</code>.
   * @param home the home to render
   * @param object3dFactory a factory able to create 3D objects from <code>home</code> items.
   *            The {@link Object3DFactory#createObject3D(Home, Selectable, boolean) createObject3D} of
   *            this factory is expected to return an instance of {@link Node} in current implementation.
   * @throws IOException if texture image files required in the scene couldn't be created.
   */
  public PhotoRenderer(Home home,
                       Object3DFactory object3dFactory,
                       Quality quality) throws IOException {
    this.home = home;
    this.compass = home.getCompass();
    this.quality = quality;
    this.sunflow = new SunflowAPI();

    this.useSunskyLight = !(home.getCamera() instanceof ObserverCamera);
    boolean silk = isSilkShaderUsed(quality);

    if (object3dFactory == null) {
      object3dFactory = new PhotoObject3DFactory();
    }
    this.object3dFactory = object3dFactory;

    HomeEnvironment homeEnvironment = home.getEnvironment();
    float subpartSize = homeEnvironment.getSubpartSizeUnderLight();
    // Dividing walls and rooms surface in subparts is useless
    homeEnvironment.setSubpartSizeUnderLight(0);

    // Export to SunFlow the Java 3D shapes and appearance of the ground, the walls, the furniture and the rooms
    List<HomeLight> lights = new ArrayList<HomeLight>();
    for (Selectable item : home.getSelectableViewableItems()) {
      if (item instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture piece : ((HomeFurnitureGroup)item).getAllFurniture()) {
          if (!(piece instanceof HomeFurnitureGroup)) {
            Node node = (Node)object3dFactory.createObject3D(home, piece, true);
            if (node != null) {
              this.homeItemsNames.put(piece, exportNode(node, false, silk));
              if (piece instanceof HomeLight) {
                lights.add((HomeLight)piece);
              }
            }
          }
        }
      } else {
        Node node = (Node)object3dFactory.createObject3D(home, item, true);
        if (node != null) {
          String [] itemNames = exportNode(node, item instanceof Wall || item instanceof Room, silk);
          this.homeItemsNames.put(item, itemNames);
          if (item instanceof HomeLight) {
            lights.add((HomeLight)item);
          }
        }
      }
    }
    // Create a 3D ground large enough to join the sky at the horizon
    Ground3D ground = new Ground3D(home, -1E7f / 2, -1E7f / 2, 1E7f, 1E7f, true);
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3f(0, -0.1f, 0));
    TransformGroup groundTransformGroup = new TransformGroup(translation);
    groundTransformGroup.addChild(ground);
    exportNode(groundTransformGroup, true, silk);
    homeEnvironment.setSubpartSizeUnderLight(subpartSize);

    HomeTexture skyTexture = homeEnvironment.getSkyTexture();
    this.useSunSky = skyTexture == null || this.useSunskyLight;
    if (!this.useSunSky) {
      // If observer camera is used with a sky texture,
      // create an image base light from sky texture
      InputStream skyImageStream = skyTexture.getImage().openStream();
      BufferedImage skyImage = ImageIO.read(skyImageStream);
      skyImageStream.close();
      // Create a temporary image base light twice as high that will contain sky image in the top part
      BufferedImage imageBaseLightImage = new BufferedImage(skyImage.getWidth(),
          skyImage.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2D = (Graphics2D)imageBaseLightImage.getGraphics();
      g2D.drawRenderedImage(skyImage, AffineTransform.getTranslateInstance(skyImage.getWidth() * skyTexture.getXOffset(), 0));
      g2D.drawRenderedImage(skyImage, AffineTransform.getTranslateInstance(skyImage.getWidth() * (skyTexture.getXOffset() - 1), 0));
      g2D.dispose();
      File imageFile = OperatingSystem.createTemporaryFile("ibl", ".png");
      ImageIO.write(imageBaseLightImage, "png", imageFile);
      // Add it to images cache with a null key just to delete it
      // with the other temporary images in dispose method
      this.textureImagesCache.put(null, imageFile.getAbsolutePath());

      this.sunflow.parameter("texture", imageFile.getAbsolutePath());
      this.sunflow.parameter("center", new Vector3(-1, 0, 0));
      this.sunflow.parameter("up", new Vector3(0, 1, 0));
      this.sunflow.parameter("fixed", true);
      this.sunflow.parameter("samples", 0);
      this.sunflow.light(UUID.randomUUID().toString(), "ibl");
    }

    // Set light settings
    int ceillingLightColor = homeEnvironment.getCeillingLightColor();
    this.homeLightColor = homeEnvironment.getLightColor();
    if (ceillingLightColor > 0) {
      // Add lights at the top of each room
      for (Room room : home.getRooms()) {
        Level roomLevel = room.getLevel();
        if (room.isCeilingVisible()
            && (roomLevel == null || roomLevel.isViewableAndVisible())) {
          float xCenter = room.getXCenter();
          float yCenter = room.getYCenter();

          double smallestDistance = Float.POSITIVE_INFINITY;
          float roomElevation = roomLevel != null
              ? roomLevel.getElevation()
              : 0;
          float roomHeight = roomElevation +
              (roomLevel == null ? home.getWallHeight() : roomLevel.getHeight());
          List<Level> levels = home.getLevels();
          if (roomLevel == null || levels.indexOf(roomLevel) == levels.size() - 1) {
            // Search the height of the wall closest to the point xCenter, yCenter
            for (Wall wall : home.getWalls()) {
              if ((wall.getLevel() == null || wall.getLevel().isViewable())
                  && wall.isAtLevel(roomLevel)) {
                float wallElevation = wall.getLevel() == null ? 0 : wall.getLevel().getElevation();
                Float wallHeightAtStart = wall.getHeight();
                float [][] points = wall.getPoints();
                for (int i = 0; i < points.length; i++) {
                  double distanceToWallPoint = Point2D.distanceSq(points [i][0], points [i][1], xCenter, yCenter);
                  if (distanceToWallPoint < smallestDistance) {
                    smallestDistance = distanceToWallPoint;
                    if (i == 0 || i == points.length - 1) { // Wall start
                      roomHeight = wallHeightAtStart != null
                          ? wallHeightAtStart
                          : home.getWallHeight();
                    } else { // Wall end
                      roomHeight = wall.isTrapezoidal()
                          ? wall.getHeightAtEnd()
                          : (wallHeightAtStart != null ? wallHeightAtStart : home.getWallHeight());
                    }
                    roomHeight += wallElevation;
                  }
                }
              }
            }
          }

          float power = (float)Math.sqrt(room.getArea()) / 3;
          this.sunflow.parameter("radiance", null,
              power * (ceillingLightColor >> 16) / 0xD0 * (this.homeLightColor >> 16) / 255,
              power * ((ceillingLightColor >> 8) & 0xFF) / 0xD0 * ((this.homeLightColor >> 8) & 0xFF) / 255,
              power * (ceillingLightColor & 0xFF) / 0xD0 * (this.homeLightColor & 0xFF) / 255);
          this.sunflow.parameter("center", new Point3(xCenter, roomHeight - 25, yCenter));
          this.sunflow.parameter("radius", 20f);
          this.sunflow.parameter("samples", 4);
          this.sunflow.light(UUID.randomUUID().toString(), "sphere");
        }
      }
    }

    final ModelManager modelManager = ModelManager.getInstance();
    // Add visible and turned on lights
    for (final HomeLight light : lights) {
      Level level = light.getLevel();
      if (light.getPower() > 0f
          && (level == null
              || level.isViewableAndVisible())) {
        if (light.isHorizontallyRotated()
            || light.getModelTransformations() != null) {
          // Retrieve the 3D model of the light to get the transformation with horizontal rotation
          modelManager.loadModel(light.getModel(), true,
              new ModelManager.ModelObserver() {
                public void modelUpdated(BranchGroup modelRoot) {
                  float [][] modelRotation = light.getModelRotation();

                  Transformation[] transformations = light.getModelTransformations();
                  Transform3D undeformedModelInvertedNormalization = null;
                  Node undeformedModel = null;
                  if (transformations != null) {
                    undeformedModel = modelManager.cloneNode(modelRoot);
                    undeformedModelInvertedNormalization = modelManager.
                        getNormalizedTransform(modelRoot, modelRotation, 1, light.isModelCenteredAtOrigin());
                    undeformedModelInvertedNormalization.invert();
                    updateModelTransformations(modelRoot, transformations);
                  }

                  // Compute normalization of the light
                  Transform3D normalization = modelManager.
                      getNormalizedTransform(modelRoot, modelRotation, 1, light.isModelCenteredAtOrigin());
                  TransformGroup normalizedModel = new TransformGroup(normalization);
                  normalizedModel.addChild(modelRoot);
                  // Get the transformation applied to the light model
                  Transform3D lightTransform = modelManager.getPieceOfFurnitureNormalizedModelTransformation(
                      light, normalizedModel);

                  if (transformations != null) {
                    for (LightSource lightSource : ((HomeLight)light).getLightSources()) {
                      int i;
                      for (i = 0; i < transformations.length; i++) {
                        Transformation transformation = transformations [i];
                        // Compute light source location in undeformed model space
                        Point3f lightSourceLocation = getNormalizedLightSourceLocation(lightSource);
                        undeformedModelInvertedNormalization.transform(lightSourceLocation);
                        BoundingSphere lightSphere = new BoundingSphere(new Point3d(lightSourceLocation), getLightSourceRadius(light, lightSource));
                        String transformUserData = transformation.getName() + ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX;
                        if (intersectsDeformedNode(undeformedModel, lightSphere, transformUserData)) {
                          Transform3D deformedLightSourceTransform = new Transform3D(lightTransform);
                          // Compute transformation of the light source in deformed model
                          deformedLightSourceTransform.mul(normalization);
                          Transform3D deformation = getDeformation(modelRoot, new Transform3D(), transformUserData);
                          deformedLightSourceTransform.mul(deformation);
                          deformedLightSourceTransform.mul(undeformedModelInvertedNormalization);
                          exportLightSource(light, lightSource, deformedLightSourceTransform);
                          break;
                        }
                      }
                      if (i == transformations.length) {
                        // Export light without deformation
                        exportLightSource(light, lightSource, lightTransform);
                      }
                    }
                  } else {
                    exportLightSources(light, lightTransform);
                  }
                }

                public void modelError(Exception ex) {
                  // In case of problem, ignore light
                }
              });
        } else {
          // Compute the light transformation without horizontal rotation
          Transform3D lightTransform =
              modelManager.getPieceOfFurnitureNormalizedModelTransformation(light, null);
          exportLightSources(light, lightTransform);
        }
      }
    }

    this.sunflow.parameter("depths.diffuse", Integer.parseInt(getRenderingParameterValue("diffusedBounces")));
    this.sunflow.parameter("depths.reflection", 4);
    this.sunflow.parameter("depths.refraction", 16);
    this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);

    Integer causticsEmit = new Integer(getRenderingParameterValue("causticsPhotons"));
    if (causticsEmit > 0) {
      this.sunflow.parameter("caustics.emit", causticsEmit);
      this.sunflow.parameter("caustics", "kd");
      this.sunflow.parameter("caustics.gather", 64);
      this.sunflow.parameter("caustics.radius", 0.5f);
      this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    }

    // Use a spiral computing
    this.sunflow.parameter("bucket.size", 64);
    this.sunflow.parameter("bucket.order", "spiral");
    this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
  }

  /**
   * Sets the transformations applied to <code>node</code> children.
   */
  private void updateModelTransformations(Node node, Transformation[] transformations) {
    for (Transformation transformation : transformations) {
      String transformUserData = transformation.getName() + ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX;
      updateTransformation(node, transformUserData, transformation.getMatrix());
    }
  }

  /**
   * Sets the transformation matrix of the children which user data is equal to <code>transformGroupUserData</code>.
   */
  private void updateTransformation(Node node, String transformGroupUserData, float[][] matrix) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup
          && transformGroupUserData.equals(node.getUserData())) {
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.setRow(0, matrix[0]);
        transformMatrix.setRow(1, matrix[1]);
        transformMatrix.setRow(2, matrix[2]);
        transformMatrix.setRow(3, new float [] {0, 0, 0, 1});
        ((TransformGroup)node).setTransform(new Transform3D(transformMatrix));
      } else {
        Enumeration<?> enumeration = ((Group)node).getAllChildren();
        while (enumeration.hasMoreElements()) {
          updateTransformation((Node)enumeration.nextElement(), transformGroupUserData, matrix);
        }
      }
    }
    // No Link parsing
  }

  /**
   * Returns <code>true</code> if the <code>node<code> or its children with a user data equal to
   * <code>transformGroupUserData</code> intersects with <code>lightBounds</code>.
   */
  private boolean intersectsDeformedNode(Node node, Bounds lightBounds,
                                         String transformGroupUserData) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        if (transformGroupUserData.equals(node.getUserData())
            && ModelManager.getInstance().getBounds(node).intersect(lightBounds)) {
          return true;
        }
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        if (intersectsDeformedNode((Node)enumeration.nextElement(), lightBounds, transformGroupUserData)) {
          return true;
        }
      }
    }
    // No Link parsing
    return false;
  }

  /**
   * Returns the transformation applied from <code>node<code> to its child which user data
   * is equal to <code>transformGroupUserData</code>.
   */
  private Transform3D getDeformation(Node node, Transform3D parentTransformation,
                                     String transformGroupUserData) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformation = new Transform3D(parentTransformation);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformation.mul(transform);
        if (transformGroupUserData.equals(node.getUserData())) {
          return parentTransformation;
        }
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        Transform3D transform = getDeformation((Node)enumeration.nextElement(), parentTransformation,
            transformGroupUserData);
        if (transform != null) {
          return transform;
        }
      }
    }
    // No Link parsing
    return null;
  }

  /**
   * Renders home in <code>image</code> at the given <code>camera</code> location and image size.
   * The rendered objects of the home are the same ones since last call to render or construction.
   */
  public void render(final BufferedImage image,
                     Camera camera,
                     final ImageObserver observer) {
    try {
      render(image, camera, null, observer);
    } catch (IOException ex) {
      // Exception can't happen, since there's no updated item
    }
  }

  /**
   * Renders home in <code>image</code> at the given <code>camera</code> location and image size.
   * The home objects listed in <code>updatedItems</code> will be updated in the renderer,
   * allowing animations or modifications of their appearance.
   */
  public void render(final BufferedImage image,
                     Camera camera,
                     List<? extends Selectable> updatedItems,
                     final ImageObserver observer) throws IOException {
    this.renderingThread = Thread.currentThread();

    if (updatedItems != null) {
      boolean silk = isSilkShaderUsed(this.quality);
      for (Selectable item : updatedItems) {
        // Remove from SunFlow updated objects
        String [] itemNames = this.homeItemsNames.get(item);
        if (itemNames != null) {
          for (String name : itemNames) {
            this.sunflow.remove(name);
          }
        }

        Node node = (Node)this.object3dFactory.createObject3D(home, item, true);
        if (node != null) {
          itemNames = exportNode(node, item instanceof Wall || item instanceof Room, silk);
          this.homeItemsNames.put(item, itemNames);
        }
      }
    }

    if (this.sunSkyLightName != null) {
      this.sunflow.remove(this.sunSkyLightName);
      this.sunSkyLightName = null;
    }
    if (this.sunLightName != null) {
      this.sunflow.remove(this.sunLightName);
      this.sunLightName = null;
    }
    // Possible values: default, path
    String globalIllumination = getRenderingParameterValue("globalIllumination");
    float [] sunDirection = getSunDirection(this.compass, Camera.convertTimeToTimeZone(camera.getTime(), this.compass.getTimeZone()));
    // Update Sun direction during daytime
    if (sunDirection [1] > -0.075f) {
      if (this.useSunSky) {
        this.sunflow.parameter("up", new Vector3(0, 1, 0));
        this.sunflow.parameter("east",
            new Vector3((float)Math.sin(compass.getNorthDirection()), 0, (float)Math.cos(compass.getNorthDirection())));
        this.sunflow.parameter("sundir", new Vector3(sunDirection [0], sunDirection [1], sunDirection [2]));
        this.sunflow.parameter("turbidity", 6f);
        this.sunflow.parameter("samples", this.useSunskyLight ? 12 : 0);
        this.sunSkyLightName = UUID.randomUUID().toString();
        this.sunflow.light(this.sunSkyLightName, "sunsky");
      }

      // Retrieve sun color
      SunSkyLight sunSkyLight = new SunSkyLight();
      ParameterList parameterList = new ParameterList();
      parameterList.addVectors("up", InterpolationType.NONE, new float [] {0, 1, 0});
      parameterList.addVectors("east", InterpolationType.NONE,
          new float [] {(float)Math.sin(compass.getNorthDirection()), 0, (float)Math.cos(compass.getNorthDirection())});
      parameterList.addVectors("sundir", InterpolationType.NONE,
          new float [] {sunDirection [0], sunDirection [1], sunDirection [2]});
      sunSkyLight.update(parameterList, this.sunflow);
      float [] sunColor = sunSkyLight.getSunColor().getRGB();

      // Simulate additional Sun with a faraway sphere light of a color depending of the hour of the day
      int sunPower = this.useSunskyLight ? 10 : 40;
      this.sunflow.parameter("radiance", null,
          (this.homeLightColor >> 16) * sunPower * (float)Math.sqrt(sunColor [0]),
          ((this.homeLightColor >> 8) & 0xFF) * sunPower * (float)Math.sqrt(sunColor [1]),
          (this.homeLightColor & 0xFF) * sunPower * (float)Math.sqrt(sunColor [2]));
      this.sunflow.parameter("center", new Point3(1000000 * sunDirection [0], 1000000 * sunDirection [1], 1000000 * sunDirection [2]));
      this.sunflow.parameter("radius", 10000f);
      this.sunflow.parameter("samples", 4);
      this.sunLightName = UUID.randomUUID().toString();
      this.sunflow.light(this.sunLightName, "sphere");

      if (!this.useSunskyLight
          && "default".equals(globalIllumination)) {
        this.sunflow.parameter("gi.engine", "ambocc");
        this.sunflow.parameter("gi.ambocc.bright", null, new float [] {1, 1, 1});
        // Use complementary color
        this.sunflow.parameter("gi.ambocc.dark", null,
            new float [] {(sunColor [1] + sunColor [2]) / 200,
                          (sunColor [0] + sunColor [2]) / 200,
                          (sunColor [0] + sunColor [1]) / 200});
        this.sunflow.parameter("gi.ambocc.samples", 1);
        this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
      }
    }

    if ("path".equals(globalIllumination)) {
      this.sunflow.parameter("gi.engine", "path");
      this.sunflow.parameter("gi.path.samples", 64);
      this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    }

    // Update camera lens
    final String CAMERA_NAME = "camera";
    switch (camera.getLens()) {
      case SPHERICAL:
        this.sunflow.camera(CAMERA_NAME, "spherical");
        break;
      case FISHEYE:
        this.sunflow.camera(CAMERA_NAME, "fisheye");
        break;
      case NORMAL:
        this.sunflow.parameter("focus.distance", new Float(getRenderingParameterValue("normalLens.focusDistance")));
        this.sunflow.parameter("lens.radius", new Float(getRenderingParameterValue("normalLens.radius")));
        this.sunflow.camera(CAMERA_NAME, "thinlens");
        break;
      case PINHOLE:
      default:
        this.sunflow.camera(CAMERA_NAME, "pinhole");
        break;
    }

    // Update camera location
    Point3 eye = new Point3(camera.getX(), camera.getZ(), camera.getY());
    Matrix4 transform;
    float yaw = camera.getYaw();
    float pitch;
    if (camera.getLens() == Camera.Lens.SPHERICAL) {
      pitch = 0;
    } else {
      pitch = camera.getPitch();
    }
    double pitchCos = Math.cos(pitch);
    if (Math.abs(pitchCos) > 1E-6) {
      // Set the point the camera is pointed to
      Point3 target = new Point3(
          camera.getX() - (float)(Math.sin(yaw) * pitchCos),
          camera.getZ() - (float)Math.sin(pitch),
          camera.getY() + (float)(Math.cos(yaw) * pitchCos));
      Vector3 up = new Vector3(0, 1, 0);
      transform = Matrix4.lookAt(eye, target, up);
    } else {
      // Compute matrix directly when the camera points to top or bottom
      transform = new Matrix4((float)-Math.cos(yaw), (float)-Math.sin(yaw), 0, camera.getX(),
          0, 0, (float)Math.signum(Math.sin(pitch)), camera.getZ(),
          (float)-Math.sin(yaw), (float)Math.cos(yaw), 0, camera.getY());
    }
    this.sunflow.parameter("transform", transform);
    this.sunflow.parameter("fov", (float)Math.toDegrees(camera.getFieldOfView()));
    this.sunflow.parameter("aspect", (float)image.getWidth() / image.getHeight());
    // Update camera
    this.sunflow.camera(CAMERA_NAME, null);

    // Set image size and quality
    this.sunflow.parameter("resolutionX", image.getWidth());
    this.sunflow.parameter("resolutionY", image.getHeight());

    int antiAliasingMin = Integer.parseInt(getRenderingParameterValue("antiAliasing.min"));
    int antiAliasingMax = Integer.parseInt(getRenderingParameterValue("antiAliasing.max"));
    String filter = getRenderingParameterValue("filter");
    this.sunflow.parameter("filter", filter);
    this.sunflow.parameter("aa.min", antiAliasingMin);
    this.sunflow.parameter("aa.max", antiAliasingMax);
    String samplerAlgorithm = getRenderingParameterValue("samplerAlgorithm");
    this.sunflow.parameter("sampler", samplerAlgorithm); // ipr, fast or bucket

    // Render image with default camera
    this.sunflow.parameter("camera", CAMERA_NAME);
    this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    this.sunflow.render(SunflowAPI.DEFAULT_OPTIONS, new BufferedImageDisplay(image, observer));
  }

  /**
   * Stops the rendering process.
   */
  public void stop() {
    if (this.renderingThread != null) {
      if (!this.renderingThread.isInterrupted()) {
        this.renderingThread.interrupt();
      }
      this.renderingThread = null;
    }
  }

  /**
   * Disposes temporary data that may be required to run this renderer.
   * Trying to use this renderer after a call to this method may lead to errors.
   */
  public void dispose() {
    // Clean up temporary images
    for (String imagePath : this.textureImagesCache.values()) {
      new File(imagePath).delete();
    }
    this.textureImagesCache.clear();
  }

  /**
   * Returns the value of the given rendering parameter.
   */
  private String getRenderingParameterValue(String parameterName) {
    // Try to retrieve overridden parameter value from System property
    // (for example: System property com.eteks.sweethome3d.j3d.PhotoRenderer.lowQuality.antiAliasing.min)
    String prefixedParameter = this.quality.name().toLowerCase(Locale.ENGLISH) + "Quality." + parameterName;
    String baseName = PhotoRenderer.class.getName();
    String value = System.getProperty(baseName + '.' + prefixedParameter);
    if (value != null) {
      return value;
    } else {
      // Return default value stored in properties resource file
      // (for example: property lowQuality.antiAliasing.min
      //  in com/eteks/sweethome3d/j3d/PhotoRenderer.properties file)
      return ResourceBundle.getBundle(baseName).getString(prefixedParameter);
    }
  }

  /**
   * Returns sun direction at a given <code>time</code>.
   * @author Fr�d�ric Mantegazza
   */
  private float [] getSunDirection(Compass compass, long time) {
    float elevation = compass.getSunElevation(time);
    float azimuth = compass.getSunAzimuth(time);
    azimuth += compass.getNorthDirection() - Math.PI / 2f;
    return new float [] {(float)(Math.cos(azimuth) * Math.cos(elevation)),
                         (float)Math.sin(elevation),
                         (float)(Math.sin(azimuth) * Math.cos(elevation))};
  }

  /**
   * Returns <code>true</code> if silk shader should be used.
   */
  private boolean isSilkShaderUsed(Quality quality) {
    // SunFlow produce too much white spots when silk shader is used with sun sky light
    // so use this shader only when observer is used
    boolean silk = !this.useSunskyLight && quality == Quality.HIGH;
    String shininessShader = getRenderingParameterValue("shininessShader");
    if ("glossy".equals(shininessShader)) {
      silk = false;
    } else if ("silk".equals(shininessShader)) {
      silk = true;
    }
    return silk;
  }

  /**
   * Exports the given Java 3D <code>node</code> and its children with SunFlow API,
   * then returns the SunFlow names that match this node.
   */
  private String [] exportNode(Node node, boolean ignoreTransparency, boolean silk) throws IOException {
    List<String> nodeNames = new ArrayList<String>();
    exportNode(node, ignoreTransparency, silk, nodeNames, new Transform3D());
    return nodeNames.toArray(new String [nodeNames.size()]);
  }

  /**
   * Exports all the 3D shapes children of <code>node</code> with SunFlow API.
   */
  private void exportNode(Node node,
                          boolean ignoreTransparency,
                          boolean silk,
                          List<String> nodeNames,
                          Transform3D parentTransformations) throws IOException {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Export all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        exportNode((Node)enumeration.nextElement(), ignoreTransparency, silk, nodeNames, parentTransformations);
      }
    } else if (node instanceof Link) {
      exportNode(((Link)node).getSharedGroup(), ignoreTransparency, silk, nodeNames, parentTransformations);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null
          ? appearance.getTransparencyAttributes() : null;
      // Ignore invisible shapes and fully transparency shapes without a texture
      if ((renderingAttributes == null
              || renderingAttributes.getVisible())
          && (transparencyAttributes == null
              || transparencyAttributes.getTransparency() != 1)) {
        String shapeName = (String)shape.getUserData();
        // Build a unique object name
        String uuid = UUID.randomUUID().toString();

        String appearanceName = null;
        TexCoordGeneration texCoordGeneration = null;
        Transform3D textureTransform = new Transform3D();
        int cullFace = PolygonAttributes.CULL_BACK;
        boolean backFaceNormalFlip = false;
        if (appearance != null) {
          PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
          if (polygonAttributes != null) {
            cullFace = polygonAttributes.getCullFace();
            backFaceNormalFlip = polygonAttributes.getBackFaceNormalFlip();
          }
          texCoordGeneration = appearance.getTexCoordGeneration();
          TextureAttributes textureAttributes = appearance.getTextureAttributes();
          if (textureAttributes != null) {
            textureAttributes.getTextureTransform(textureTransform);
          }
          appearanceName = "shader" + uuid;
          boolean mirror = shapeName != null
              && shapeName.startsWith(ModelManager.MIRROR_SHAPE_PREFIX);
          exportAppearance(appearance, appearanceName, mirror, ignoreTransparency, silk);
          nodeNames.add(appearanceName);
        }

        // Export object geometries
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          String objectNameBase = "object" + uuid + "-" + i;
          // Always ignore normals on walls
          String [] objectsName = exportNodeGeometry(shape.getGeometry(i), parentTransformations, texCoordGeneration,
              textureTransform, cullFace, backFaceNormalFlip, objectNameBase);
          if (objectsName != null) {
            for (String objectName : objectsName) {
              if (appearanceName != null) {
                this.sunflow.parameter("shaders", new String [] {appearanceName});
              }
              String instanceName = objectName + ".instance";
              this.sunflow.instance(instanceName, objectName);
              nodeNames.add(instanceName);
              nodeNames.add(objectName);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the names of the exported 3D geometries with SunFlow API.
   */
  private String [] exportNodeGeometry(Geometry geometry,
                                       Transform3D parentTransformations,
                                       TexCoordGeneration texCoordGeneration,
                                       Transform3D textureTransform,
                                       int cullFace,
                                       boolean backFaceNormalFlip,
                                       String objectNameBase) {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;

      // Create vertices indices array according to geometry class
      int [] verticesIndices = null;
      int [] stripVertexCount = null;
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedLineArray) {
          verticesIndices = new int [((IndexedGeometryArray)geometryArray).getIndexCount()];
        } else if (geometryArray instanceof IndexedTriangleArray) {
          verticesIndices = new int [((IndexedGeometryArray)geometryArray).getIndexCount()];
        } else if (geometryArray instanceof IndexedQuadArray) {
          verticesIndices = new int [((IndexedQuadArray)geometryArray).getIndexCount() * 3 / 2];
        } else if (geometryArray instanceof IndexedGeometryStripArray) {
          IndexedTriangleStripArray geometryStripArray = (IndexedTriangleStripArray)geometryArray;
          stripVertexCount = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripIndexCounts(stripVertexCount);
          if (geometryArray instanceof IndexedLineStripArray) {
            verticesIndices = new int [getLineCount(stripVertexCount) * 2];
          } else {
            verticesIndices = new int [getTriangleCount(stripVertexCount) * 3];
          }
        }
      } else {
        if (geometryArray instanceof LineArray) {
          verticesIndices = new int [((GeometryArray)geometryArray).getVertexCount()];
        } else if (geometryArray instanceof TriangleArray) {
          verticesIndices = new int [((GeometryArray)geometryArray).getVertexCount()];
        } else if (geometryArray instanceof QuadArray) {
          verticesIndices = new int [((QuadArray)geometryArray).getVertexCount() * 3 / 2];
        } else if (geometryArray instanceof GeometryStripArray) {
          GeometryStripArray geometryStripArray = (GeometryStripArray)geometryArray;
          stripVertexCount = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripVertexCounts(stripVertexCount);
          if (geometryArray instanceof LineStripArray) {
            verticesIndices = new int [getLineCount(stripVertexCount) * 2];
          } else {
            verticesIndices = new int [getTriangleCount(stripVertexCount) * 3];
          }
        }
      }

      if (verticesIndices != null) {
        boolean line = geometryArray instanceof IndexedLineArray
            || geometryArray instanceof IndexedLineStripArray
            || geometryArray instanceof LineArray
            || geometryArray instanceof LineStripArray;
        float [] vertices = new float [geometryArray.getVertexCount() * 3];
        float [] normals = !line && (geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0
            ? new float [geometryArray.getVertexCount() * 3]
            : null;
        // Store temporarily exported triangles to avoid to add their opposite triangles
        // (SunFlow doesn't render correctly a face and its opposite)
        Set<Triangle> exportedTriangles = line
            ? null
            : new HashSet<Triangle>(geometryArray.getVertexCount());

        boolean uvsGenerated = false;
        Vector4f planeS = null;
        Vector4f planeT = null;
        if (!line && texCoordGeneration != null) {
          uvsGenerated = texCoordGeneration.getGenMode() == TexCoordGeneration.OBJECT_LINEAR
              && texCoordGeneration.getEnable();
          if (uvsGenerated) {
            planeS = new Vector4f();
            planeT = new Vector4f();
            texCoordGeneration.getPlaneS(planeS);
            texCoordGeneration.getPlaneT(planeT);
          }
        }

        float [] uvs;
        if (uvsGenerated
            || (geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
          uvs = new float [geometryArray.getVertexCount() * 2];
        } else {
          uvs = null;
        }

        if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
          if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
            float [] vertexData = geometryArray.getInterleavedVertices();
            int vertexSize = vertexData.length / geometryArray.getVertexCount();
            // Export vertices coordinates
            for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount();
                 index < n; index++, i += vertexSize) {
              Point3f vertex = new Point3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
              exportVertex(parentTransformations, vertex, index, vertices);
            }
            // Export normals
            if (normals != null) {
              for (int index = 0, i = vertexSize - 6, n = geometryArray.getVertexCount();
                   index < n; index++, i += vertexSize) {
                Vector3f normal = new Vector3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
                exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
              }
            }
            // Export texture coordinates
            if (texCoordGeneration != null) {
              if (uvsGenerated) {
                for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount();
                      index < n; index++, i += vertexSize) {
                  TexCoord2f textureCoordinates = generateTextureCoordinates(
                      vertexData [i], vertexData [i + 1], vertexData [i + 2], planeS, planeT);
                  exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
                }
              }
            } else if (uvs != null) {
              for (int index = 0, i = 0, n = geometryArray.getVertexCount();
                    index < n; index++, i += vertexSize) {
                TexCoord2f textureCoordinates = new TexCoord2f(vertexData [i], vertexData [i + 1]);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          } else {
            // Export vertices coordinates
            float [] vertexCoordinates = geometryArray.getCoordRefFloat();
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
              Point3f vertex = new Point3f(vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2]);
              exportVertex(parentTransformations, vertex, index, vertices);
            }
            // Export normals
            if (normals != null) {
              float [] normalCoordinates = geometryArray.getNormalRefFloat();
              for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
                Vector3f normal = new Vector3f(normalCoordinates [i], normalCoordinates [i + 1], normalCoordinates [i + 2]);
                exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
              }
            }
            // Export texture coordinates
            if (texCoordGeneration != null) {
              if (uvsGenerated) {
                for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
                  TexCoord2f textureCoordinates = generateTextureCoordinates(
                      vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2], planeS, planeT);
                  exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
                }
              }
            } else if (uvs != null) {
              float [] textureCoordinatesArray = geometryArray.getTexCoordRefFloat(0);
              for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 2) {
                TexCoord2f textureCoordinates = new TexCoord2f(textureCoordinatesArray [i], textureCoordinatesArray [i + 1]);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          }
        } else {
          // Export vertices coordinates
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            Point3f vertex = new Point3f();
            geometryArray.getCoordinate(index, vertex);
            exportVertex(parentTransformations, vertex, index, vertices);
          }
          // Export normals
          if (normals != null) {
            for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
              Vector3f normal = new Vector3f();
              geometryArray.getNormal(index, normal);
              exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
            }
          }
          // Export texture coordinates
          if (texCoordGeneration != null) {
            if (uvsGenerated) {
              for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
                Point3f vertex = new Point3f();
                geometryArray.getCoordinate(index, vertex);
                TexCoord2f textureCoordinates = generateTextureCoordinates(
                    vertex.x, vertex.y, vertex.z, planeS, planeT);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          } else if (uvs != null) {
            for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
              TexCoord2f textureCoordinates = new TexCoord2f();
              geometryArray.getTextureCoordinate(0, index, textureCoordinates);
              exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
            }
          }
        }

        // Export lines, triangles or quadrilaterals according to the geometry
        if (geometryArray instanceof IndexedGeometryArray) {
          int [] normalsIndices = normals != null
              ? new int [verticesIndices.length]
              : null;
          int [] uvsIndices = uvs != null
              ? new int [verticesIndices.length]
              : null;

          if (geometryArray instanceof IndexedLineArray) {
            IndexedLineArray lineArray = (IndexedLineArray)geometryArray;
            for (int i = 0, n = lineArray.getIndexCount(); i < n; i += 2) {
              exportIndexedLine(lineArray, i, i + 1, verticesIndices, i);
            }
          } else {
            if (geometryArray instanceof IndexedTriangleArray) {
              IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
              for (int i = 0, n = triangleArray.getIndexCount(), triangleIndex = 0; i < n; i += 3) {
                triangleIndex = exportIndexedTriangle(triangleArray, i, i + 1, i + 2,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof IndexedQuadArray) {
              IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
              for (int i = 0, n = quadArray.getIndexCount(), triangleIndex = 0; i < n; i += 4) {
                triangleIndex = exportIndexedTriangle(quadArray, i, i + 1, i + 2,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                triangleIndex = exportIndexedTriangle(quadArray, i, i + 2, i + 3,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof IndexedLineStripArray) {
              IndexedLineStripArray lineStripArray = (IndexedLineStripArray)geometryArray;
              for (int initialIndex = 0, lineIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 1;
                     i < n; i++, lineIndex += 2) {
                   exportIndexedLine(lineStripArray, i, i + 1, verticesIndices, lineIndex);
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof IndexedTriangleStripArray) {
              IndexedTriangleStripArray triangleStripArray = (IndexedTriangleStripArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0;
                     i < n; i++, j++) {
                  if (j % 2 == 0) {
                    triangleIndex = exportIndexedTriangle(triangleStripArray, i, i + 1, i + 2,
                        verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  } else { // Vertices of odd triangles are in reverse order
                    triangleIndex = exportIndexedTriangle(triangleStripArray, i, i + 2, i + 1,
                        verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  }
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof IndexedTriangleFanArray) {
              IndexedTriangleFanArray triangleFanArray = (IndexedTriangleFanArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2;
                     i < n; i++) {
                  triangleIndex = exportIndexedTriangle(triangleFanArray, initialIndex, i + 1, i + 2,
                      verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                }
                initialIndex += stripVertexCount [strip];
              }
            }
          }

          if (normalsIndices != null && !Arrays.equals(verticesIndices, normalsIndices)
              || uvsIndices != null && !Arrays.equals(verticesIndices, uvsIndices)) {
            // Remove indirection in verticesIndices, normals and uvsIndices
            // because SunFlow uses only verticesIndices
            float [] directVertices = new float [verticesIndices.length * 3];
            float [] directNormals =  normalsIndices != null
                ? new float [verticesIndices.length * 3]
                : null;
            float [] directUvs =  uvsIndices != null
                ? new float [verticesIndices.length * 2]
                : null;
            int verticeIndex = 0;
            int normalIndex = 0;
            int uvIndex = 0;
            for (int i = 0; i < verticesIndices.length; i++) {
              int indirectIndex = verticesIndices [i] * 3;
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              if (normalsIndices != null) {
                indirectIndex = normalsIndices [i] * 3;
                directNormals [normalIndex++] = normals [indirectIndex++];
                directNormals [normalIndex++] = normals [indirectIndex++];
                directNormals [normalIndex++] = normals [indirectIndex++];
              }
              if (uvsIndices != null) {
                indirectIndex = uvsIndices [i] * 2;
                directUvs [uvIndex++] = uvs [indirectIndex++];
                directUvs [uvIndex++] = uvs [indirectIndex++];
              }
              verticesIndices [i] = i;
            }
            vertices = directVertices;
            normals = directNormals;
            uvs = directUvs;
          }
        } else {
          if (geometryArray instanceof LineArray) {
            LineArray lineArray = (LineArray)geometryArray;
            for (int i = 0, n = lineArray.getVertexCount(); i < n; i += 2) {
              exportLine(lineArray, i, i + 1, verticesIndices, i);
            }
          } else {
            if (geometryArray instanceof TriangleArray) {
              TriangleArray triangleArray = (TriangleArray)geometryArray;
              for (int i = 0, n = triangleArray.getVertexCount(), triangleIndex = 0; i < n; i += 3) {
                triangleIndex = exportTriangle(triangleArray, i, i + 1, i + 2,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof QuadArray) {
              QuadArray quadArray = (QuadArray)geometryArray;
              for (int i = 0, n = quadArray.getVertexCount(), triangleIndex = 0; i < n; i += 4) {
                triangleIndex = exportTriangle(quadArray, i, i + 1, i + 2,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                triangleIndex = exportTriangle(quadArray, i + 2, i + 3, i,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof LineStripArray) {
              LineStripArray lineStripArray = (LineStripArray)geometryArray;
              for (int initialIndex = 0, lineIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 1;
                     i < n; i++, lineIndex += 2) {
                  exportLine(lineStripArray, i, i + 1, verticesIndices, lineIndex);
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof TriangleStripArray) {
              TriangleStripArray triangleStripArray = (TriangleStripArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0;
                     i < n; i++, j++) {
                  if (j % 2 == 0) {
                    triangleIndex = exportTriangle(triangleStripArray, i, i + 1, i + 2,
                        verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  } else { // Vertices of odd triangles are in reverse order
                    triangleIndex = exportTriangle(triangleStripArray, i, i + 2, i + 1,
                        verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  }
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof TriangleFanArray) {
              TriangleFanArray triangleFanArray = (TriangleFanArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2;
                     i < n; i++) {
                  triangleIndex = exportTriangle(triangleFanArray, initialIndex, i + 1, i + 2, verticesIndices,
                      triangleIndex, vertices, exportedTriangles, cullFace);
                }
                initialIndex += stripVertexCount [strip];
              }
            }
          }
        }

        if (line) {
          String [] objectNames = new String [verticesIndices.length / 2];
          for (int startIndex = 0; startIndex < verticesIndices.length; startIndex += 2) {
            String objectName = objectNameBase + "-" + startIndex;
            objectNames [startIndex / 2] = objectName;

            // Get points coordinates of a segment
            float [] points = new float [6];
            int pointIndex = 0;
            for (int i = startIndex; i <= startIndex + 1; i++) {
              int indirectIndex = verticesIndices [i] * 3;
              points [pointIndex++] = vertices [indirectIndex++];
              points [pointIndex++] = vertices [indirectIndex++];
              points [pointIndex++] = vertices [indirectIndex];
            }

            // Create as many hairs as segments otherwise long hairs become invisible
            this.sunflow.parameter("segments", 1);
            this.sunflow.parameter("widths", 0.15f);
            this.sunflow.parameter("points", "point", "vertex", points);
            this.sunflow.geometry(objectName, "hair");
          }
          return objectNames;
        } else {
          int exportedTrianglesVertexCount = exportedTriangles.size() * 3;
          if (exportedTrianglesVertexCount < verticesIndices.length) {
            // Reduce verticesIndices array to contain only exported triangles
            int [] tmp = new int [exportedTrianglesVertexCount];
            System.arraycopy(verticesIndices, 0, tmp, 0, tmp.length);
            verticesIndices = tmp;
          }

          this.sunflow.parameter("triangles", verticesIndices);
          this.sunflow.parameter("points", "point", "vertex", vertices);
          if (normals != null) {
            // Check there's no NaN values in normals to avoid endless loop in SunFlow
            boolean noNaN = true;
            for (float val : normals) {
              if (Float.isNaN(val)) {
                noNaN = false;
                break;
              }
            }
            if (noNaN)  {
              this.sunflow.parameter("normals", "vector", "vertex", normals);
            }
          }
          if (uvs != null) {
            // Check there's no huge values in uvs to avoid problems in SunFlow
            boolean noHugeValues = true;
            for (float val : uvs) {
              if (Math.abs(val) > 1E9) {
                noHugeValues = false;
                break;
              }
            }
            if (noHugeValues)  {
              this.sunflow.parameter("uvs", "texcoord", "vertex", uvs);
            }
          }
          this.sunflow.geometry(objectNameBase, "triangle_mesh");
          return new String [] {objectNameBase};
        }
      }
    }
    return null;
  }

  /**
   * Returns texture coordinates generated with <code>texCoordGeneration</code> computed
   * as described in <code>TexCoordGeneration</code> javadoc.
   */
  private TexCoord2f generateTextureCoordinates(float x, float y, float z,
                                                Vector4f planeS,
                                                Vector4f planeT) {
    return new TexCoord2f(x * planeS.x + y * planeS.y + z * planeS.z + planeS.w,
        x * planeT.x + y * planeT.y + z * planeT.z + planeT.w);
  }

  /**
   * Returns the sum of line integers in <code>stripVertexCount</code> array.
   */
  private int getLineCount(int [] stripVertexCount) {
    int lineCount = 0;
    for (int strip = 0; strip < stripVertexCount.length; strip++) {
      lineCount += stripVertexCount [strip] - 1;
    }
    return lineCount;
  }

  /**
   * Returns the sum of triangle integers in <code>stripVertexCount</code> array.
   */
  private int getTriangleCount(int [] stripVertexCount) {
    int triangleCount = 0;
    for (int strip = 0; strip < stripVertexCount.length; strip++) {
      triangleCount += stripVertexCount [strip] - 2;
    }
    return triangleCount;
  }

  /**
   * Applies to <code>vertex</code> the given transformation, and stores it in <code>vertices</code>.
   */
  private void exportVertex(Transform3D transformationToParent,
                            Point3f vertex, int index,
                            float [] vertices) {
    transformationToParent.transform(vertex);
    index *= 3;
    vertices [index++] = vertex.x;
    vertices [index++] = vertex.y;
    vertices [index] = vertex.z;
  }

  /**
   * Applies to <code>normal</code> the given transformation, and stores it in <code>normals</code>.
   */
  private void exportNormal(Transform3D transformationToParent,
                            Vector3f normal, int index,
                            float [] normals,
                            boolean backFaceNormalFlip) {
    if (backFaceNormalFlip) {
      normal.negate();
    }
    transformationToParent.transform(normal);
    int i = index * 3;
    normals [i++] = normal.x;
    normals [i++] = normal.y;
    normals [i] = normal.z;
  }

  /**
   * Stores <code>textureCoordinates</code> in <code>uvs</code>.
   */
  private void exportTextureCoordinates(TexCoord2f textureCoordinates,
                                        Transform3D textureTransform,
                                        int index, float [] uvs) {
    index *= 2;
    if (textureTransform.getBestType() != Transform3D.IDENTITY) {
      Point3f transformedCoordinates = new Point3f(textureCoordinates.x, textureCoordinates.y, 0);
      textureTransform.transform(transformedCoordinates);
      uvs [index++] = transformedCoordinates.x;
      uvs [index] = transformedCoordinates.y;
    } else {
      uvs [index++] = textureCoordinates.x;
      uvs [index] = textureCoordinates.y;
    }
  }

  /**
   * Stores in <code>verticesIndices</code> the indices given at vertexIndex1, vertexIndex2.
   */
  private void exportIndexedLine(IndexedGeometryArray geometryArray,
                                 int vertexIndex1, int vertexIndex2,
                                 int [] verticesIndices,
                                 int index) {
    verticesIndices [index++] = geometryArray.getCoordinateIndex(vertexIndex1);
    verticesIndices [index] = geometryArray.getCoordinateIndex(vertexIndex2);
  }

  /**
   * Stores in <code>verticesIndices</code> the indices given at vertexIndex1, vertexIndex2, vertexIndex3.
   */
  private int exportIndexedTriangle(IndexedGeometryArray geometryArray,
                                    int vertexIndex1, int vertexIndex2, int vertexIndex3,
                                    int [] verticesIndices, int [] normalsIndices, int [] textureCoordinatesIndices,
                                    int index,
                                    float [] vertices,
                                    Set<Triangle> exportedTriangles,
                                    int cullFace) {
    if (cullFace == PolygonAttributes.CULL_FRONT) {
      // Reverse vertex order
      int tmp = vertexIndex1;
      vertexIndex1 = vertexIndex3;
      vertexIndex3 = tmp;
    }

    int coordinateIndex1 = geometryArray.getCoordinateIndex(vertexIndex1);
    int coordinateIndex2 = geometryArray.getCoordinateIndex(vertexIndex2);
    int coordinateIndex3 = geometryArray.getCoordinateIndex(vertexIndex3);
    Triangle exportedTriangle = new Triangle(vertices, coordinateIndex1, coordinateIndex2, coordinateIndex3);
    if (!exportedTriangles.contains(exportedTriangle)) {
      exportedTriangles.add(exportedTriangle);
      verticesIndices [index] = coordinateIndex1;
      verticesIndices [index + 1] = coordinateIndex2;
      verticesIndices [index + 2] = coordinateIndex3;
      if (normalsIndices != null) {
        normalsIndices [index] = geometryArray.getNormalIndex(vertexIndex1);
        normalsIndices [index + 1] = geometryArray.getNormalIndex(vertexIndex2);
        normalsIndices [index + 2] = geometryArray.getNormalIndex(vertexIndex3);
      }
      if (textureCoordinatesIndices != null) {
        textureCoordinatesIndices [index] = geometryArray.getTextureCoordinateIndex(0, vertexIndex1);
        textureCoordinatesIndices [index + 1] = geometryArray.getTextureCoordinateIndex(0, vertexIndex2);
        textureCoordinatesIndices [index + 2] = geometryArray.getTextureCoordinateIndex(0, vertexIndex3);
      }
      return index + 3;
    }
    return index;
  }

  /**
   * Stores in <code>verticesIndices</code> the indices vertexIndex1 and vertexIndex2.
   */
  private void exportLine(GeometryArray geometryArray,
                          int vertexIndex1, int vertexIndex2,
                          int [] verticesIndices, int index) {
    verticesIndices [index++] = vertexIndex1;
    verticesIndices [index] = vertexIndex2;
  }

  /**
   * Stores in <code>verticesIndices</code> the indices vertexIndex1, vertexIndex2, vertexIndex3.
   */
  private int exportTriangle(GeometryArray geometryArray,
                             int vertexIndex1, int vertexIndex2, int vertexIndex3,
                             int [] verticesIndices, int index,
                             float [] vertices,
                             Set<Triangle> exportedTriangles,
                             int cullFace) {
    if (cullFace == PolygonAttributes.CULL_FRONT) {
      // Reverse vertex order
      int tmp = vertexIndex1;
      vertexIndex1 = vertexIndex3;
      vertexIndex3 = tmp;
    }

    Triangle exportedTriangle = new Triangle(vertices, vertexIndex1, vertexIndex2, vertexIndex3);
    if (!exportedTriangles.contains(exportedTriangle)) {
      exportedTriangles.add(exportedTriangle);
      verticesIndices [index++] = vertexIndex1;
      verticesIndices [index++] = vertexIndex2;
      verticesIndices [index++] = vertexIndex3;
    }
    return index;
  }

  /**
   * Exports a Java3D appearance as a SunFlow shader.
   */
  private void exportAppearance(Appearance appearance,
                                String appearanceName,
                                boolean mirror,
                                boolean ignoreTransparency,
                                boolean silk) throws IOException {
    Texture texture = appearance.getTexture();
    if (mirror) {
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getDiffuseColor(color);
        this.sunflow.parameter("color", null, new float [] {color.x, color.y, color.z});
      }
      this.sunflow.shader(appearanceName, "mirror");
    } else if (texture != null) {
      // Check shape transparency
      TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
      float transparency;
      if (transparencyAttributes != null
          && transparencyAttributes.getTransparency() > 0
          && !ignoreTransparency) {
        transparency = 1 - transparencyAttributes.getTransparency();
      } else {
        transparency = 1;
      }

      TransparentTextureKey key = new TransparentTextureKey(texture, transparency);
      String imagePath = this.textureImagesCache.get(key);
      if (imagePath == null) {
        if (texture.getUserData() instanceof URL && transparency == 1) {
          imagePath = texture.getUserData().toString();
        } else {
          ImageComponent2D imageComponent = (ImageComponent2D)texture.getImage(0);
          RenderedImage image = imageComponent.getRenderedImage();
          if (transparency < 1) {
            // Compute a partially transparent image
            BufferedImage transparentImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D = (Graphics2D)transparentImage.getGraphics();
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g2D.drawRenderedImage(image, null);
            g2D.dispose();
            image = transparentImage;
          }
          File imageFile = OperatingSystem.createTemporaryFile("texture", ".png");
          ImageIO.write(image, "png", imageFile);
          imagePath = imageFile.getAbsolutePath();
        }
        this.textureImagesCache.put(key, imagePath);
      }
      Material material = appearance.getMaterial();
      float shininess;
      if (material != null
          && (shininess = material.getShininess()) > 1) {
        if (silk) {
          this.sunflow.parameter("diffuse.texture", imagePath);
          Color3f color = new Color3f();
          material.getSpecularColor(color);
          float [] specularColor = new float [] {
              (float)Math.sqrt(color.x) / 2, (float)Math.sqrt(color.y) / 2, (float)Math.sqrt(color.z) / 2};
          this.sunflow.parameter("specular", null, specularColor);
          this.sunflow.parameter("glossyness", (float)Math.pow(10, -Math.log(shininess) / Math.log(5)));
          this.sunflow.parameter("samples", 1);
          this.sunflow.shader(appearanceName, "uber");
        } else {
          this.sunflow.parameter("texture", imagePath);
          this.sunflow.parameter("shiny", shininess / 512f);
          this.sunflow.shader(appearanceName, "textured_shiny_diffuse");
        }
      } else {
        this.sunflow.parameter("texture", imagePath);
        this.sunflow.shader(appearanceName, "textured_diffuse");
      }
    } else {
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getDiffuseColor(color);
        float [] diffuseColor = new float [] {color.x, color.y, color.z};

        TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
        if (transparencyAttributes != null
            && transparencyAttributes.getTransparency() > 0
            && !ignoreTransparency) {
          if (material instanceof OBJMaterial
              && ((OBJMaterial)material).isOpticalDensitySet()) {
            float opticalDensity = ((OBJMaterial)material).getOpticalDensity();
            // To avoid rendering issues in SunFlow, use glass ETA for optical density equal to 1
            // (i.e. the index of refraction of vacuum that has no meaning for furniture parts)
            this.sunflow.parameter("eta", opticalDensity <= 1f ?  1.55f  : opticalDensity);
          } else {
            // Use glass ETA as default
            this.sunflow.parameter("eta", 1.55f);
          }
          float transparency = 1 - transparencyAttributes.getTransparency();
          this.sunflow.parameter("color", null,
              new float [] {(1 - transparency) + transparency * diffuseColor [0],
                            (1 - transparency) + transparency * diffuseColor [1],
                            (1 - transparency) + transparency * diffuseColor [2]});
          this.sunflow.parameter("absorption.color", null,
              new float [] {transparency * (1 - diffuseColor [0]),
                            transparency * (1 - diffuseColor [1]),
                            transparency * (1 - diffuseColor [2])});
          this.sunflow.shader(appearanceName, "glass");
        } else if (material.getLightingEnable()) {
          this.sunflow.parameter("diffuse", null, diffuseColor);
          float shininess = material.getShininess();
          if (shininess > 1) {
            if (silk) {
              material.getSpecularColor(color);
              float [] specularColor = new float [] {
                   (float)Math.sqrt(color.x) / 2, (float)Math.sqrt(color.y) / 2, (float)Math.sqrt(color.z) / 2};
              this.sunflow.parameter("specular", null, specularColor);
              this.sunflow.parameter("glossyness", (float)Math.pow(10, -Math.log(shininess) / Math.log(5)));
              this.sunflow.parameter("samples", 1);
              this.sunflow.shader(appearanceName, "uber");
            } else {
              this.sunflow.parameter("shiny", shininess / 512f);
              this.sunflow.shader(appearanceName, "shiny_diffuse");
            }
          } else {
            this.sunflow.shader(appearanceName, "diffuse");
          }
        } else {
          this.sunflow.parameter("color", null, diffuseColor);
          this.sunflow.shader(appearanceName, "constant");
        }
      } else {
        ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
        if (coloringAttributes != null) {
          Color3f color = new Color3f();
          coloringAttributes.getColor(color);
          this.sunflow.parameter("color", null, new float [] {color.x, color.y, color.z});
          this.sunflow.shader(appearanceName, "constant");
        }
      }
    }
  }

  /**
   * Exports the given light sources as SunFlow lights placed at the right location
   * with <code>lightTransform</code>.
   */
  private void exportLightSources(HomeLight light, Transform3D lightTransform) {
    for (LightSource lightSource : light.getLightSources()) {
      exportLightSource(light, lightSource, lightTransform);
    }
  }

  /**
   * Exports the given light source as SunFlow lights placed at the right location
   * with <code>lightTransform</code>.
   */
  private void exportLightSource(HomeLight light, LightSource lightSource, Transform3D lightTransform) {
    float lightPower = light.getPower();
    float lightRadius = getLightSourceRadius(light, lightSource);
    float power = 5 * lightPower * lightPower / (lightRadius * lightRadius);
    int lightColor = lightSource.getColor();
    this.sunflow.parameter("radiance", null,
        power * (lightColor >> 16) * (this.homeLightColor >> 16),
        power * ((lightColor >> 8) & 0xFF) * ((this.homeLightColor >> 8) & 0xFF),
        power * (lightColor & 0xFF) * (this.homeLightColor & 0xFF));
    Point3f lightSourceLocation = getNormalizedLightSourceLocation(lightSource);
    lightTransform.transform(lightSourceLocation);
    this.sunflow.parameter("center",
        new Point3(lightSourceLocation.getX(),
            lightSourceLocation.getY(),
            lightSourceLocation.getZ()));
    this.sunflow.parameter("radius", lightRadius);
    this.sunflow.parameter("samples", 4);
    this.sunflow.light(UUID.randomUUID().toString(), "sphere");
  }

  private float getLightSourceRadius(HomeLight light, LightSource lightSource) {
    return lightSource.getDiameter() != null
        ? lightSource.getDiameter() * light.getWidth() / 2
        : 3.25f; // Default radius compatible with most lights available before version 3.0
  }

  private Point3f getNormalizedLightSourceLocation(LightSource lightSource) {
    return new Point3f(lightSource.getX() - 0.5f, lightSource.getZ() - 0.5f, 0.5f - lightSource.getY());
  }

  /**
   * Default factory for photo creation with no ceiling for rooms when top camera is used.
   */
  private static class PhotoObject3DFactory extends Object3DBranchFactory {
    @Override
    public boolean isDrawingModeEnabled() {
      return false;
    }

    public Object createObject3D(Home home, Selectable item, boolean waitForLoading) {
      if (item instanceof Room) {
        // Never display ceiling with top camera
        return new Room3D((Room)item, home, !(home.getCamera() instanceof ObserverCamera), waitForLoading);
      } else {
        return super.createObject3D(home, item, waitForLoading);
      }
    }
  }

  /**
   * A SunFlow display that updates an existing image.
   * Implementation mostly copied from org.sunflow.system.ImagePanel.
   */
  private static final class BufferedImageDisplay implements Display {
    private static final int BASE_INFO_FLAGS = ImageObserver.WIDTH | ImageObserver.HEIGHT | ImageObserver.PROPERTIES;
    private static final int [] BORDERS = {Color.RED.toRGB(), Color.GREEN.toRGB(), Color.BLUE.toRGB(),
                                           Color.YELLOW.toRGB(), Color.CYAN.toRGB(), Color.MAGENTA.toRGB(),
                                           new Color(1, 0.5f, 0).toRGB(), new Color(0.5f, 1, 0).toRGB()};

    private final ImageObserver observer;
    private final BufferedImage image;

    private BufferedImageDisplay(BufferedImage image, ImageObserver observer) {
      this.observer = observer;
      this.image = image;
    }

    public synchronized void imageBegin(int width, int height, int bucketSize) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int rgba = this.image.getRGB(x, y);
          this.image.setRGB(x, y, ((rgba & 0xFEFEFEFE) >>> 1) + ((rgba & 0xFCFCFCFC) >>> 2));
        }
      }
      notifyObserver(ImageObserver.FRAMEBITS | BASE_INFO_FLAGS, 0, 0, width, height);
    }

    public synchronized void imagePrepare(int x, int y, int width, int height, int id) {
      int border = BORDERS [id % BORDERS.length] | 0xFF000000;
      for (int by = 0; by < height; by++) {
        for (int bx = 0; bx < width; bx++) {
          if (bx < 2 || bx > width - 3) {
            if (5 * by < height || 5 * (height - by - 1) < height) {
              this.image.setRGB(x + bx, y + by, border);
            }
          } else if (by < 2 || by > height - 3) {
            if (5 * bx < width || 5 * (width - bx - 1) < width) {
              this.image.setRGB(x + bx, y + by, border);
            }
          }
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public synchronized void imageUpdate(int x, int y, int width, int height, Color [] data, float [] alpha) {
      for (int j = 0, index = 0; j < height; j++) {
        for (int i = 0; i < width; i++, index++) {
          this.image.setRGB(x + i, y + j,
              data [index].copy().mul(1.0f / alpha [index]).toNonLinear().toRGBA(alpha [index]));
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public synchronized void imageFill(int x, int y, int width, int height, Color c, float alpha) {
      int rgba = c.copy().mul(1.0f / alpha).toNonLinear().toRGBA(alpha);
      for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
          this.image.setRGB(x + i, y + j, rgba);
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public void imageEnd() {
      notifyObserver(ImageObserver.FRAMEBITS | BASE_INFO_FLAGS,
            0, 0, this.image.getWidth(), this.image.getHeight());
    }

    private void notifyObserver(final int flags, final int x, final int y, final int width, final int height) {
      if (observer != null) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              observer.imageUpdate(image, flags, x, y, width, height);
            }
          });
      }
    }
  }

  /**
   * A SunFlow sphere light with no representation.
   */
  public static class SphereLightWithNoRepresentation extends SphereLight {
    public Instance createInstance() {
      return null;
    }
  }

  /**
   * A triangle used to remove faces cited more that once (opposite faces included).
   */
  private static class Triangle {
    private float [] point1;
    private float [] point2;
    private float [] point3;
    private int      hashCode;
    private boolean  hashCodeSet;

    public Triangle(float [] vertices, int index1, int index2, int index3) {
      this.point1 = new float [] {vertices [index1 * 3], vertices [index1 * 3 + 1], vertices [index1 * 3 + 2]};
      this.point2 = new float [] {vertices [index2 * 3], vertices [index2 * 3 + 1], vertices [index2 * 3 + 2]};
      this.point3 = new float [] {vertices [index3 * 3], vertices [index3 * 3 + 1], vertices [index3 * 3 + 2]};
    }

    @Override
    public int hashCode() {
      if (!this.hashCodeSet) {
        this.hashCode = 31 * Arrays.hashCode(this.point1)
            + 31 * Arrays.hashCode(this.point2)
            + 31 * Arrays.hashCode(this.point3);
        this.hashCodeSet = true;
      }
      return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof Triangle) {
        Triangle triangle = (Triangle)obj;
        // Compare first with point with opposite face
        return Arrays.equals(this.point1, triangle.point3)
               && Arrays.equals(this.point2, triangle.point2)
               && Arrays.equals(this.point3, triangle.point1)
            || Arrays.equals(this.point1, triangle.point2)
               && Arrays.equals(this.point2, triangle.point1)
               && Arrays.equals(this.point3, triangle.point3)
            || Arrays.equals(this.point1, triangle.point1)
               && Arrays.equals(this.point2, triangle.point3)
               && Arrays.equals(this.point3, triangle.point2)
            || Arrays.equals(this.point1, triangle.point1)
               && Arrays.equals(this.point2, triangle.point2)
               && Arrays.equals(this.point3, triangle.point3);
      }
      return false;
    }
  }

  /**
   * A key used to manage textures at different levels of transparency.
   */
  private static class TransparentTextureKey {
    private Texture texture;
    private float   transparency;

    public TransparentTextureKey(Texture texture, float transparency) {
      this.texture = texture;
      this.transparency = transparency;
    }

    @Override
    public boolean equals(Object obj) {
      return ((TransparentTextureKey)obj).texture.equals(this.texture)
          && ((TransparentTextureKey)obj).transparency == this.transparency;
    }

    @Override
    public int hashCode() {
      return this.texture.hashCode() + Float.floatToIntBits(this.transparency);
    }
  }
}
