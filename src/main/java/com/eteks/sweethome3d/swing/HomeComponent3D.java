/*
 * HomeComponent3D.java 24 ao?t 2006
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
package com.eteks.sweethome3d.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.J3DGraphics2D;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PointArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransformInterpolator;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.Object3DBranch;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.j3d.Wall3D;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * A component that displays home walls, rooms and furniture with Java 3D.
 * @author Emmanuel Puybaret
 */
public class HomeComponent3D extends JComponent implements com.eteks.sweethome3d.viewcontroller.View, Printable {
  private enum ActionType {MOVE_CAMERA_FORWARD, MOVE_CAMERA_FAST_FORWARD, MOVE_CAMERA_BACKWARD, MOVE_CAMERA_FAST_BACKWARD,
      MOVE_CAMERA_LEFT, MOVE_CAMERA_FAST_LEFT, MOVE_CAMERA_RIGHT, MOVE_CAMERA_FAST_RIGHT,
      ROTATE_CAMERA_YAW_LEFT, ROTATE_CAMERA_YAW_FAST_LEFT, ROTATE_CAMERA_YAW_RIGHT, ROTATE_CAMERA_YAW_FAST_RIGHT,
      ROTATE_CAMERA_PITCH_UP, ROTATE_CAMERA_PITCH_FAST_UP, ROTATE_CAMERA_PITCH_DOWN, ROTATE_CAMERA_PITCH_FAST_DOWN,
      ELEVATE_CAMERA_UP, ELEVATE_CAMERA_FAST_UP, ELEVATE_CAMERA_DOWN, ELEVATE_CAMERA_FAST_DOWN}

  private static final boolean JAVA3D_1_5 = VirtualUniverse.getProperties().get("j3d.version") != null
      && ((String)VirtualUniverse.getProperties().get("j3d.version")).startsWith("1.5");

  private final Home                               home;
  private final boolean                            displayShadowOnFloor;
  private final Object3DFactory                    object3dFactory;
  private final Map<Selectable, Object3DBranch>    homeObjects = new HashMap<Selectable, Object3DBranch>();
  private Light []                                 sceneLights;
  private Collection<Selectable>                   homeObjectsToUpdate;
  private Collection<Selectable>                   lightScopeObjectsToUpdate;
  private Component                                component3D;
  private SimpleUniverse                           onscreenUniverse;
  private Camera                                   camera;
  // Listeners bound to home that updates 3D scene objects
  private PropertyChangeListener                   cameraChangeListener;
  private PropertyChangeListener                   homeCameraListener;
  private PropertyChangeListener                   backgroundChangeListener;
  private PropertyChangeListener                   groundChangeListener;
  private PropertyChangeListener                   backgroundLightColorListener;
  private PropertyChangeListener                   lightColorListener;
  private PropertyChangeListener                   subpartSizeListener;
  private PropertyChangeListener                   elevationChangeListener;
  private PropertyChangeListener                   wallsAlphaListener;
  private PropertyChangeListener                   drawingModeListener;
  private CollectionListener<Level>                levelListener;
  private PropertyChangeListener                   levelChangeListener;
  private CollectionListener<Wall>                 wallListener;
  private PropertyChangeListener                   wallChangeListener;
  private CollectionListener<HomePieceOfFurniture> furnitureListener;
  private PropertyChangeListener                   furnitureChangeListener;
  private CollectionListener<Room>                 roomListener;
  private PropertyChangeListener                   roomChangeListener;
  private CollectionListener<Polyline>             polylineListener;
  private PropertyChangeListener                   polylineChangeListener;
  private CollectionListener<Label>                labelListener;
  private PropertyChangeListener                   labelChangeListener;
  // Offscreen printed image cache
  // Creating an offscreen buffer is a quite lengthy operation so we keep the last printed image in this field
  // This image should be set to null each time the 3D view changes
  private BufferedImage                            printedImageCache;
  private BoundingBox                              approximateHomeBoundsCache;
  private SimpleUniverse                           offscreenUniverse;

  private JComponent                               navigationPanel;
  private ComponentListener                        navigationPanelListener;
  private BufferedImage                            navigationPanelImage;
  private Area                                     lightScopeOutsideWallsAreaCache;

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
   * with no controller.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home) {
    this(home, null);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home, HomeController3D controller) {
    this(home, null, controller);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture,
   * with shadows on the floor.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home,
                         UserPreferences  preferences,
                         boolean displayShadowOnFloor) {
    this(home, preferences, new Object3DBranchFactory(preferences), displayShadowOnFloor, null);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home,
                         UserPreferences  preferences,
                         HomeController3D controller) {
    this(home, preferences, new Object3DBranchFactory(preferences), false, controller);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
   * @param home the home to display in this component
   * @param preferences user preferences
   * @param object3dFactory a factory able to create 3D objects from <code>home</code> items.
   *            The {@link Object3DFactory#createObject3D(Home, Selectable, boolean) createObject3D} of
   *            this factory is expected to return an instance of {@link Object3DBranch} in current implementation.
   * @param controller the controller that manages modifications in <code>home</code>.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home,
                         UserPreferences  preferences,
                         Object3DFactory  object3dFactory,
                         HomeController3D controller) {
    this(home, preferences, object3dFactory, false, controller);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
   * @throws IllegalStateException  if the 3D component couldn't be created.
   */
  public HomeComponent3D(Home home,
                         UserPreferences  preferences,
                         Object3DFactory  object3dFactory,
                         boolean displayShadowOnFloor,
                         HomeController3D controller) {
    this.home = home;
    this.displayShadowOnFloor = displayShadowOnFloor;
    this.object3dFactory = object3dFactory != null
        ? object3dFactory
        : new Object3DBranchFactory(preferences);

    if (controller != null) {
      createActions(controller);
      installKeyboardActions();
      // Let this component manage focus
      setFocusable(true);
      SwingTools.installFocusBorder(this);
    }

    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (graphicsEnvironment.getScreenDevices().length == 1) {
      // If only one screen device is available, create canvas 3D immediately,
      // otherwise create it once the screen device of the parent is known
      createComponent3D(graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration(), preferences, controller);
    }

    // Add an ancestor listener to create canvas 3D and its universe once this component is made visible
    // and clean up universe once its parent frame is disposed
    addAncestorListener(preferences, controller, displayShadowOnFloor);
  }

  /**
   * Adds an ancestor listener to this component to manage the creation of the canvas and its universe
   * and clean up the universe.
   */
  private void addAncestorListener(final UserPreferences preferences,
                                   final HomeController3D controller,
                                   final boolean displayShadowOnFloor) {
    addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          if (offscreenUniverse != null) {
            throw new IllegalStateException("Can't listen to home changes offscreen and onscreen at the same time");
          }

          // Create component 3D only once it's visible
          Insets insets = getInsets();
          if (getHeight() <= insets.top + insets.bottom
              || getWidth() <= insets.left + insets.right) {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent ev) {
                  removeComponentListener(this);
                  // If 3D view is still in component hierarchy, create component children
                  if (SwingUtilities.getRoot(HomeComponent3D.this) != null) {
                    ancestorAdded(null);
                  }
                }
              });
            return;
          } else if (ev == null) {
            // Force a resize event to make the component 3D appear
            Component root = SwingUtilities.getRoot(HomeComponent3D.this);
            root.dispatchEvent(new ComponentEvent(root, ComponentEvent.COMPONENT_RESIZED));
          }

          // Create component 3D only once the graphics configuration of its parent is known
          if (component3D == null) {
            createComponent3D(getGraphicsConfiguration(), preferences, controller);
          }
          if (onscreenUniverse == null) {
            onscreenUniverse = createUniverse(displayShadowOnFloor, true, false);
            Canvas3D canvas3D;
            if (component3D instanceof Canvas3D) {
              canvas3D = (Canvas3D)component3D;
            } else {
              try {
                // Call JCanvas3D#getOffscreenCanvas3D by reflection to be able to run under Java 3D 1.3
                canvas3D = (Canvas3D)Class.forName("com.sun.j3d.exp.swing.JCanvas3D").getMethod("getOffscreenCanvas3D").invoke(component3D);
              } catch (Exception ex) {
                UnsupportedOperationException ex2 = new UnsupportedOperationException();
                ex2.initCause(ex);
                throw ex2;
              }
            }
            // Bind universe to canvas3D
            onscreenUniverse.getViewer().getView().addCanvas3D(canvas3D);
            component3D.setFocusable(false);
            updateNavigationPanelImage();
          }
        }

        public void ancestorRemoved(AncestorEvent ev) {
          if (onscreenUniverse != null) {
            onscreenUniverse.cleanup();
            removeHomeListeners();
            onscreenUniverse = null;
          }
          if (component3D != null) {
            removeAll();
            for (MouseListener l : component3D.getMouseListeners()) {
              component3D.removeMouseListener(l);
            }
            for (MouseMotionListener l : component3D.getMouseMotionListeners()) {
              component3D.removeMouseMotionListener(l);
            }
            for (MouseWheelListener l : component3D.getMouseWheelListeners()) {
              component3D.removeMouseWheelListener(l);
            }
            component3D = null;
            navigationPanel = null;
          }
        }

        public void ancestorMoved(AncestorEvent ev) {
        }
      });
  }

  /**
   * Creates the 3D component associated with the given <code>configuration</code> device.
   */
  private void createComponent3D(GraphicsConfiguration configuration,
                                 UserPreferences  preferences,
                                 HomeController3D controller) {
    if (Boolean.valueOf(System.getProperty("com.eteks.sweethome3d.j3d.useOffScreen3DView", "false"))) {
      GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
      template.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
      // Request depth size equal to 24 if supported
      int defaultDepthSize = template.getDepthSize();
      template.setDepthSize(24);
      if (!template.isGraphicsConfigSupported(
          GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration())) {
        template.setDepthSize(defaultDepthSize);
      }
      try {
        // Instantiate JCanvas3DWithNavigationPanel inner class by reflection
        // to be able to run under Java 3D 1.3
        this.component3D = (Component)Class.forName(getClass().getName() + "$JCanvas3DWithNavigationPanel").
            getConstructor(getClass(), GraphicsConfigTemplate3D.class).newInstance(this, template);
        this.component3D.setSize(1, 1);
      } catch (ClassNotFoundException ex) {
        throw new UnsupportedOperationException("Java 3D 1.5 required to display an offscreen 3D view");
      } catch (Exception ex) {
        UnsupportedOperationException ex2 = new UnsupportedOperationException();
        ex2.initCause(ex);
        throw ex2;
      }
    } else {
      this.component3D = Component3DManager.getInstance().getOnscreenCanvas3D(configuration,
          new Component3DManager.RenderingObserver() {
              private Shape3D dummyShape;

              public void canvas3DSwapped(Canvas3D canvas3D) {
              }

              public void canvas3DPreRendered(Canvas3D canvas3D) {
              }

              public void canvas3DPostRendered(Canvas3D canvas3D) {
                // Copy reference to navigation panel image to avoid concurrency problems
                // if it's modified in the EDT while this method draws it
                BufferedImage navigationPanelImage = HomeComponent3D.this.navigationPanelImage;
                // Render navigation panel upon canvas 3D if it exists
                if (navigationPanelImage != null) {
                  if (JAVA3D_1_5) {
                    // Render trivial transparent shape to reset the possible transformation set on the last rendered texture
                    // See https://jogamp.org/bugzilla/show_bug.cgi?id=1006#c1
                    if (this.dummyShape == null) {
                      PointArray dummyGeometry = new PointArray(1, PointArray.COORDINATES);
                      dummyGeometry.setCoordinates(0, new float [] {0, 0, 0});
                      Appearance appearance = new Appearance();
                      appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 1));
                      this.dummyShape = new Shape3D(dummyGeometry, appearance);
                    }
                    canvas3D.getGraphicsContext3D().draw(this.dummyShape);
                  }
                  J3DGraphics2D g2D = canvas3D.getGraphics2D();
                  g2D.drawImage(navigationPanelImage, null, 0, 0);
                  g2D.flush(true);
                }
              }
            });
    }
    this.component3D.setBackground(new Color(230, 230, 230));

    JPanel canvasPanel = new JPanel(new LayoutManager() {
        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
          return component3D.getPreferredSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
          return component3D.getMinimumSize();
        }

        public void layoutContainer(Container parent) {
          component3D.setBounds(0, 0, Math.max(1, parent.getWidth()), Math.max(1, parent.getHeight()));
          if (navigationPanel != null
              && navigationPanel.isVisible()) {
            // Ensure that navigationPanel is always in top corner
            Dimension preferredSize = navigationPanel.getPreferredSize();
            navigationPanel.setBounds(0, 0, preferredSize.width, preferredSize.height);
          }
        }
      });

    canvasPanel.add(this.component3D);
    setLayout(new GridLayout());
    add(canvasPanel);
    if (controller != null) {
      addMouseListeners(controller, this.component3D);
      // Add mouse listeners again to ensure 3D component will receive events
      for (MouseListener l : getMouseListeners()) {
        super.removeMouseListener(l);
        addMouseListener(l);
      }
      for (MouseMotionListener l : getMouseMotionListeners()) {
        super.removeMouseMotionListener(l);
        addMouseMotionListener(l);
      }
      if (preferences != null
          && (!OperatingSystem.isMacOSX()
              || OperatingSystem.isMacOSXLeopardOrSuperior())) {
        // No support for navigation panel under Mac OS X Tiger
        // (too unstable, may crash system at 3D view resizing)
        this.navigationPanel = createNavigationPanel(this.home, preferences, controller);
        setNavigationPanelVisible(preferences.isNavigationPanelVisible() && isVisible());
        preferences.addPropertyChangeListener(UserPreferences.Property.NAVIGATION_PANEL_VISIBLE,
            new NavigationPanelChangeListener(this));
      }
      createActions(controller);
      installKeyboardActions();
      // Let this component manage focus
      setFocusable(true);
      SwingTools.installFocusBorder(this);
    }
  }

  /**
   * A <code>JCanvas</code> canvas that displays the navigation panel of a home component 3D upon it.
   */
  private static class JCanvas3DWithNavigationPanel extends JCanvas3D {
    private final HomeComponent3D homeComponent3D;

    public JCanvas3DWithNavigationPanel(HomeComponent3D homeComponent3D,
                                        GraphicsConfigTemplate3D template) {
      super(template);
      this.homeComponent3D = homeComponent3D;
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(this.homeComponent3D.navigationPanelImage, 0, 0, this);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (this.component3D != null) {
      this.component3D.setVisible(visible);
    }
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  private static class NavigationPanelChangeListener implements PropertyChangeListener {
    private final WeakReference<HomeComponent3D>  homeComponent3D;

    public NavigationPanelChangeListener(HomeComponent3D homeComponent3D) {
      this.homeComponent3D = new WeakReference<HomeComponent3D>(homeComponent3D);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomeComponent3D homeComponent3D = this.homeComponent3D.get();
      if (homeComponent3D == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.NAVIGATION_PANEL_VISIBLE, this);
      } else {
        homeComponent3D.setNavigationPanelVisible((Boolean)ev.getNewValue() && homeComponent3D.isVisible());
      }
    }
  }

  /**
   * Returns the component displayed as navigation panel by this 3D view.
   */
  private JComponent createNavigationPanel(Home home,
                                           UserPreferences preferences,
                                           HomeController3D controller) {
    JPanel navigationPanel = new JPanel(new GridBagLayout()) {
        @Override
        public void applyComponentOrientation(ComponentOrientation o) {
          // Ignore orientation
        }
      };
    String navigationPanelIconPath = preferences.getLocalizedString(HomeComponent3D.class, "navigationPanel.icon");
    final ImageIcon nagivationPanelIcon = navigationPanelIconPath.length() > 0
        ? new ImageIcon(HomeComponent3D.class.getResource(navigationPanelIconPath))
        : null;
    navigationPanel.setBorder(new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
          if (nagivationPanelIcon != null) {
            nagivationPanelIcon.paintIcon(c, g, x, y);
          } else {
            // Draw a surrounding oval if no navigation panel icon is defined
            Graphics2D g2D = (Graphics2D)g;
            g2D.setColor(Color.BLACK);
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.drawOval(x + 3, y + 3, width - 6, height - 6);
          }
        }

        public Insets getBorderInsets(Component c) {
          return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
          return false;
        }
      });
    navigationPanel.setOpaque(false);
    navigationPanel.add(new NavigationButton(0, -(float)Math.PI / 36, 0, "TURN_LEFT", preferences, controller),
        new GridBagConstraints(0, 1, 1, 2, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
    navigationPanel.add(new NavigationButton(12.5f, 0, 0, "GO_FORWARD", preferences, controller),
        new GridBagConstraints(1, 0, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    navigationPanel.add(new NavigationButton(0, (float)Math.PI / 36, 0, "TURN_RIGHT", preferences, controller),
        new GridBagConstraints(2, 1, 1, 2, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    navigationPanel.add(new NavigationButton(-12.5f, 0, 0, "GO_BACKWARD", preferences, controller),
        new GridBagConstraints(1, 3, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
    navigationPanel.add(new NavigationButton(0, 0, -(float)Math.PI / 100, "TURN_UP", preferences, controller),
        new GridBagConstraints(1, 1, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
    navigationPanel.add(new NavigationButton(0, 0, (float)Math.PI / 100, "TURN_DOWN", preferences, controller),
        new GridBagConstraints(1, 2, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 1, 0), 0, 0));
    return navigationPanel;
  }

  /**
   * An icon button that changes camera location and angles when pressed.
   */
  private static class NavigationButton extends JButton {
    private boolean shiftDown;

    public NavigationButton(final float moveDelta,
                            final float yawDelta,
                            final float pitchDelta,
                            String actionName,
                            UserPreferences preferences,
                            final HomeController3D controller) {
      super(new ResourceAction(preferences, HomeComponent3D.class, actionName, true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            // Manage auto repeat button with mouse listener
          }
        });
      // Create a darker press icon
      setPressedIcon(new ImageIcon(createImage(new FilteredImageSource(
          ((ImageIcon)getIcon()).getImage().getSource(),
          new RGBImageFilter() {
            {
              canFilterIndexColorModel = true;
            }

            public int filterRGB (int x, int y, int rgb) {
              // Return darker color
              int alpha = rgb & 0xFF000000;
              int darkerRed = ((rgb & 0xFF0000) >> 1) & 0xFF0000;
              int darkerGreen  = ((rgb & 0x00FF00) >> 1) & 0x00FF00;
              int darkerBlue  = (rgb & 0x0000FF) >> 1;
              return alpha | darkerRed | darkerGreen | darkerBlue;
            }
          }))));

      // Track shift key press
      addMouseMotionListener(new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent ev) {
            shiftDown = ev.isShiftDown();
          }
        });
      addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            shiftDown = ev.isShiftDown();
            SwingUtilities.getAncestorOfClass(HomeComponent3D.class,
                NavigationButton.this).requestFocusInWindow();
          }
        });

      // Create a timer that will update camera angles and location
      final Timer timer = new Timer(50, new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.moveCamera(shiftDown ? moveDelta : moveDelta / 5);
            controller.rotateCameraYaw(shiftDown ? yawDelta : yawDelta / 5);
            controller.rotateCameraPitch(pitchDelta);
          }
        });
      timer.setInitialDelay(0);

      // Update camera when button is armed
      addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (getModel().isArmed()
                && !timer.isRunning()) {
              timer.restart();
            } else if (!getModel().isArmed()
                       && timer.isRunning()) {
              timer.stop();
            }
          }
        });
      setFocusable(false);
      setBorder(null);
      setContentAreaFilled(false);
      // Force preferred size to ensure button isn't larger
      setPreferredSize(new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight()));
      addPropertyChangeListener(JButton.ICON_CHANGED_PROPERTY, new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // Reset border when icon is reset after a resource action change
            setBorder(null);
          }
        });
    }
  }

  /**
   * Sets the component that will be drawn upon the heavyweight 3D component shown by this component.
   * Mouse events will targeted to the navigation panel when needed.
   * Supports transparent components.
   */
  private void setNavigationPanelVisible(boolean visible) {
    if (this.navigationPanel != null) {
      this.navigationPanel.setVisible(visible);
      if (visible) {
        // Add a component listener that updates navigation panel image
        this.navigationPanelListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
              updateNavigationPanelImage();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
              updateNavigationPanelImage();
            }
          };
        this.navigationPanel.addComponentListener(this.navigationPanelListener);
        // Add the navigation panel to this component to be able to paint it
        // but show it behind canvas 3D
        this.component3D.getParent().add(this.navigationPanel);
      } else {
        this.navigationPanel.removeComponentListener(this.navigationPanelListener);
        if (this.navigationPanel.getParent() != null) {
          this.navigationPanel.getParent().remove(this.navigationPanel);
        }
      }
      revalidate();
      updateNavigationPanelImage();
      this.component3D.repaint();
    }
  }

  /**
   * Updates the image of the components that may overlap canvas 3D
   * (with a Z order smaller than the one of the canvas 3D).
   */
  private void updateNavigationPanelImage() {
    if (this.navigationPanel != null
        && this.navigationPanel.isVisible()) {
      Rectangle componentBounds = this.navigationPanel.getBounds();
      Rectangle imageSize = new Rectangle(this.component3D.getX(), this.component3D.getY());
      imageSize.add(componentBounds.x + componentBounds.width,
          componentBounds.y + componentBounds.height);
      if (!imageSize.isEmpty()) {
        BufferedImage updatedImage = this.navigationPanelImage;
        // Consider that no navigation panel image is available
        // while it's updated
        this.navigationPanelImage = null;
        Graphics2D g2D;
        if (updatedImage == null
            || updatedImage.getWidth() != imageSize.width
            || updatedImage.getHeight() != imageSize.height) {
          updatedImage = new BufferedImage(
              imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
          g2D = (Graphics2D)updatedImage.getGraphics();
        } else {
          // Clear image
          g2D = (Graphics2D)updatedImage.getGraphics();
          Composite oldComposite = g2D.getComposite();
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0));
          g2D.fill(new Rectangle2D.Double(0, 0, imageSize.width, imageSize.height));
          g2D.setComposite(oldComposite);
        }
        this.navigationPanel.paintAll(g2D);
        g2D.dispose();
        // Navigation panel image ready to be displayed
        this.navigationPanelImage = updatedImage;
        return;
      }
    }
    this.navigationPanelImage = null;
  }

  /**
   * Returns a new 3D universe that displays <code>home</code> objects.
   */
  private SimpleUniverse createUniverse(boolean displayShadowOnFloor,
                                        boolean listenToHomeUpdates,
                                        boolean waitForLoading) {
    // Create a universe bound to no canvas 3D
    ViewingPlatform viewingPlatform = new ViewingPlatform();
    // Add an interpolator to view transform to get smooth transition
    TransformGroup viewPlatformTransform = viewingPlatform.getViewPlatformTransform();
    CameraInterpolator cameraInterpolator = new CameraInterpolator(viewPlatformTransform);
    cameraInterpolator.setSchedulingBounds(new BoundingSphere(new Point3d(), 1E7));
    viewPlatformTransform.addChild(cameraInterpolator);
    viewPlatformTransform.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

    Viewer viewer = new Viewer(new Canvas3D [0]);
    SimpleUniverse universe = new SimpleUniverse(viewingPlatform, viewer);

    View view = viewer.getView();
    view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);

    // Update field of view from current camera
    updateView(view, this.home.getCamera());

    // Update point of view from current camera
    updateViewPlatformTransform(viewPlatformTransform, this.home.getCamera(), false);

    // Add camera listeners to update later point of view from camera
    if (listenToHomeUpdates) {
      addCameraListeners(view, viewPlatformTransform);
    }

    // Link scene matching home to universe
    universe.addBranchGraph(createSceneTree(
        displayShadowOnFloor, listenToHomeUpdates, waitForLoading));

    return universe;
  }

  /**
   * Remove all listeners bound to home that updates 3D scene objects.
   */
  private void removeHomeListeners() {
    this.home.removePropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_COLOR, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_TEXTURE, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.backgroundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D, this.groundChangeListener);
    this.home.removePropertyChangeListener(Home.Property.BACKGROUND_IMAGE, this.groundChangeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.backgroundLightColorListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT, this.subpartSizeListener);
    this.home.getCamera().removePropertyChangeListener(this.cameraChangeListener);
    this.home.removePropertyChangeListener(Home.Property.CAMERA, this.elevationChangeListener);
    this.home.getCamera().removePropertyChangeListener(this.elevationChangeListener);
    this.home.removeLevelsListener(this.levelListener);
    for (Level level : this.home.getLevels()) {
      level.removePropertyChangeListener(this.levelChangeListener);
    }
    this.home.removeWallsListener(this.wallListener);
    for (Wall wall : this.home.getWalls()) {
      wall.removePropertyChangeListener(this.wallChangeListener);
    }
    this.home.removeFurnitureListener(this.furnitureListener);
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      piece.removePropertyChangeListener(this.furnitureChangeListener);
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
          childPiece.removePropertyChangeListener(this.furnitureChangeListener);
        }
      }
    }
    this.home.removeRoomsListener(this.roomListener);
    for (Room room : this.home.getRooms()) {
      room.removePropertyChangeListener(this.roomChangeListener);
    }
    this.home.removePolylinesListener(this.polylineListener);
    for (Polyline polyline : this.home.getPolylines()) {
      polyline.removePropertyChangeListener(this.polylineChangeListener);
    }
    this.home.removeLabelsListener(this.labelListener);
    for (Label label : this.home.getLabels()) {
      label.removePropertyChangeListener(this.labelChangeListener);
    }
  }

  /**
   * Prints this component to make it fill <code>pageFormat</code> imageable size.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex == 0) {
      // Compute printed image size to render 3D view in 150 dpi
      double printSize = Math.min(pageFormat.getImageableWidth(),
          pageFormat.getImageableHeight());
      int printedImageSize = (int)(printSize / 72 * 150);
      if (this.printedImageCache == null
          || this.printedImageCache.getWidth() != printedImageSize) {
        try {
          this.printedImageCache = getOffScreenImage(printedImageSize, printedImageSize);
        } catch (IllegalRenderingStateException ex) {
          // If off screen canvas failed, consider that 3D view page doesn't exist
          return NO_SUCH_PAGE;
        }
      }

      Graphics2D g2D = (Graphics2D)g.create();
      // Center the 3D view in component
      g2D.translate(pageFormat.getImageableX() + (pageFormat.getImageableWidth() - printSize) / 2,
          pageFormat.getImageableY() + (pageFormat.getImageableHeight() - printSize) / 2);
      double scale = printSize / printedImageSize;
      g2D.scale(scale, scale);
      g2D.drawImage(this.printedImageCache, 0, 0, this);
      g2D.dispose();

      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }

  /**
   * Optimizes this component for the creation of a sequence of multiple off screen images.
   * Once off screen images are generated with {@link #getOffScreenImage(int, int) getOffScreenImage},
   * call {@link #endOffscreenImagesCreation() endOffscreenImagesCreation} method to free resources.
   */
  public void startOffscreenImagesCreation() {
    if (this.offscreenUniverse == null) {
      if (this.onscreenUniverse != null) {
        throw new IllegalStateException("Can't listen to home changes offscreen and onscreen at the same time");
      }
      this.offscreenUniverse = createUniverse(this.displayShadowOnFloor, true, true);
      // Replace textures by clones because Java 3D doesn't accept all the time
      // to share textures between offscreen and onscreen environments
      Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
      for (Enumeration it = this.offscreenUniverse.getLocale().getAllBranchGraphs(); it.hasMoreElements(); ) {
        cloneTexture((Node)it.nextElement(), replacedTextures);
      }
    }
  }

  /**
   * Returns an image of the home viewed by this component at the given size.
   */
  public BufferedImage getOffScreenImage(int width, int height) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    SimpleUniverse offScreenImageUniverse = null;
    try {
      View view;
      if (this.offscreenUniverse == null) {
        offScreenImageUniverse = createUniverse(this.displayShadowOnFloor, false, true);
        view = offScreenImageUniverse.getViewer().getView();
        // Replace textures by clones because Java 3D doesn't accept all the time
        // to share textures between offscreen and onscreen environments
        Map<Texture, Texture> replacedTextures = new HashMap<Texture, Texture>();
        for (Enumeration it = offScreenImageUniverse.getLocale().getAllBranchGraphs(); it.hasMoreElements(); ) {
          cloneTexture((Node)it.nextElement(), replacedTextures);
        }
      } else {
        view = this.offscreenUniverse.getViewer().getView();
      }

      updateView(view, this.home.getCamera(), width, height);

      // Empty temporarily selection to create the off screen image
      List<Selectable> emptySelection = Collections.emptyList();
      this.home.setSelectedItems(emptySelection);
      return Component3DManager.getInstance().getOffScreenImage(view, width, height);
    } finally {
      // Restore selection
      this.home.setSelectedItems(selectedItems);
      if (offScreenImageUniverse != null) {
        offScreenImageUniverse.cleanup();
      }
    }
  }

  /**
   * Replace the textures set on node shapes by clones.
   */
  private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        cloneTexture((Node)enumeration.nextElement(), replacedTextures);
      }
    } else if (node instanceof Link) {
      cloneTexture(((Link)node).getSharedGroup(), replacedTextures);
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        Texture texture = appearance.getTexture();
        if (texture != null) {
          Texture replacedTexture = replacedTextures.get(texture);
          if (replacedTexture == null) {
            replacedTexture = (Texture)texture.cloneNodeComponent(false);
            replacedTextures.put(texture, replacedTexture);
          }
          appearance.setTexture(replacedTexture);
        }
      }
    }
  }

  /**
   * Frees unnecessary resources after the creation of a sequence of multiple offscreen images.
   */
  public void endOffscreenImagesCreation() {
    if (this.offscreenUniverse != null) {
      this.offscreenUniverse.cleanup();
      removeHomeListeners();
      this.offscreenUniverse = null;
    }
  }

  /**
   * Adds listeners to home to update point of view from current camera.
   */
  private void addCameraListeners(final View view,
                                  final TransformGroup viewPlatformTransform) {
    this.cameraChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          // Update view transform later to avoid flickering in case of multiple camera changes
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              updateView(view, home.getCamera());
              updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), true);
            }
          });
        }
      };
    this.home.getCamera().addPropertyChangeListener(this.cameraChangeListener);
    this.homeCameraListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateView(view, home.getCamera());
          updateViewPlatformTransform(viewPlatformTransform, home.getCamera(), false);
          // Add camera change listener to new active camera
          ((Camera)ev.getOldValue()).removePropertyChangeListener(cameraChangeListener);
          home.getCamera().addPropertyChangeListener(cameraChangeListener);
        }
      };
    this.home.addPropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
  }

  /**
   * Updates <code>view</code> from <code>camera</code> field of view.
   */
  private void updateView(View view, Camera camera) {
    if (this.component3D != null) {
      updateView(view, camera, this.component3D.getWidth(), this.component3D.getHeight());
    } else {
      updateView(view, camera, 0, 0);
    }
  }

  private void updateView(View view, Camera camera, int width, int height) {
    float fieldOfView = camera.getFieldOfView();
    if (fieldOfView == 0) {
      fieldOfView = (float)(Math.PI * 63 / 180);
    }
    view.setFieldOfView(fieldOfView);
    double frontClipDistance = 2.5f;
    float frontBackDistanceRatio = 500000; // More than 10 km for a 2.5 cm front distance
    if (Component3DManager.getInstance().getDepthSize() <= 16) {
      // It's recommended to keep ratio between back and front clip distances under 3000 for a 16 bit Z-buffer
      frontBackDistanceRatio = 3000;
      BoundingBox approximateHomeBounds = getApproximateHomeBounds();
      // If camera is out of home bounds, adjust the front clip distance to the distance to home bounds
      if (approximateHomeBounds != null
          && !approximateHomeBounds.intersect(new Point3d(camera.getX(), camera.getY(), camera.getZ()))) {
        float distanceToClosestBoxSide = getDistanceToBox(camera.getX(), camera.getY(), camera.getZ(), approximateHomeBounds);
        if (!Float.isNaN(distanceToClosestBoxSide)) {
          frontClipDistance = Math.max(frontClipDistance, 0.1f * distanceToClosestBoxSide);
        }
      }
    }
    if (camera.getZ() > 0 && width != 0 && height != 0) {
      float halfVerticalFieldOfView = (float)Math.atan(Math.tan(fieldOfView / 2) * height / width);
      float fieldOfViewBottomAngle = camera.getPitch() + halfVerticalFieldOfView;
      // If the horizon is above the frustrum bottom, take into account the distance to the ground
      if (fieldOfViewBottomAngle > 0) {
        float distanceToGroundAtFieldOfViewBottomAngle = (float)(camera.getZ() / Math.sin(fieldOfViewBottomAngle));
        frontClipDistance = Math.min(frontClipDistance, 0.35f * distanceToGroundAtFieldOfViewBottomAngle);
        if (frontClipDistance * frontBackDistanceRatio < distanceToGroundAtFieldOfViewBottomAngle) {
          // Ensure the ground is always visible at the back clip distance
          frontClipDistance = distanceToGroundAtFieldOfViewBottomAngle / frontBackDistanceRatio;
        }
      }
    }
    // Update front and back clip distance
    view.setFrontClipDistance(frontClipDistance);
    view.setBackClipDistance(frontClipDistance * frontBackDistanceRatio);
    clearPrintedImageCache();
  }

  /**
   * Returns quickly computed bounds of the objects in home.
   */
  private BoundingBox getApproximateHomeBounds() {
    if (this.approximateHomeBoundsCache == null) {
      BoundingBox approximateHomeBounds = null;
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isVisible()
            && (piece.getLevel() == null
                || piece.getLevel().isViewable())) {
          float halfMaxDimension = Math.max(piece.getWidthInPlan(), piece.getDepthInPlan()) / 2;
          float elevation = piece.getGroundElevation();
          Point3d pieceLocation = new Point3d(
              piece.getX() - halfMaxDimension, piece.getY() - halfMaxDimension, elevation);
          if (approximateHomeBounds == null) {
            approximateHomeBounds = new BoundingBox(pieceLocation, pieceLocation);
          } else {
            approximateHomeBounds.combine(pieceLocation);
          }
          approximateHomeBounds.combine(new Point3d(
              piece.getX() + halfMaxDimension, piece.getY() + halfMaxDimension, elevation + piece.getHeightInPlan()));
        }
      }
      for (Wall wall : this.home.getWalls()) {
        if (wall.getLevel() == null
            || wall.getLevel().isViewable()) {
          Point3d startPoint = new Point3d(wall.getXStart(), wall.getYStart(),
              wall.getLevel() != null ? wall.getLevel().getElevation() : 0);
          if (approximateHomeBounds == null) {
            approximateHomeBounds = new BoundingBox(startPoint, startPoint);
          } else {
            approximateHomeBounds.combine(startPoint);
          }
          approximateHomeBounds.combine(new Point3d(wall.getXEnd(), wall.getYEnd(),
              startPoint.z + (wall.getHeight() != null ? wall.getHeight() : this.home.getWallHeight())));
        }
      }
      for (Room room : this.home.getRooms()) {
        if (room.getLevel() == null
            || room.getLevel().isViewable()) {
          Point3d center = new Point3d(room.getXCenter(), room.getYCenter(),
              room.getLevel() != null ? room.getLevel().getElevation() : 0);
          if (approximateHomeBounds == null) {
            approximateHomeBounds = new BoundingBox(center, center);
          } else {
            approximateHomeBounds.combine(center);
          }
        }
      }
      for (Label label : this.home.getLabels()) {
        if ((label.getLevel() == null
              || label.getLevel().isViewable())
            && label.getPitch() != null) {
          Point3d center = new Point3d(label.getX(), label.getY(), label.getGroundElevation());
          if (approximateHomeBounds == null) {
            approximateHomeBounds = new BoundingBox(center, center);
          } else {
            approximateHomeBounds.combine(center);
          }
        }
      }
      this.approximateHomeBoundsCache = approximateHomeBounds;
    }
    return this.approximateHomeBoundsCache;
  }

  /**
   * Returns the distance between the point at the given coordinates (x,y,z) and the closest side of <code>box</code>.
   */
  private float getDistanceToBox(float x, float y, float z, BoundingBox box) {
    Point3f point = new Point3f(x, y, z);
    Point3d lower = new Point3d();
    box.getLower(lower);
    Point3d upper = new Point3d();
    box.getUpper(upper);
    Point3f [] boxVertices = {
      new Point3f((float)lower.x, (float)lower.y, (float)lower.z),
      new Point3f((float)upper.x, (float)lower.y, (float)lower.z),
      new Point3f((float)lower.x, (float)upper.y, (float)lower.z),
      new Point3f((float)upper.x, (float)upper.y, (float)lower.z),
      new Point3f((float)lower.x, (float)lower.y, (float)upper.z),
      new Point3f((float)upper.x, (float)lower.y, (float)upper.z),
      new Point3f((float)lower.x, (float)upper.y, (float)upper.z),
      new Point3f((float)upper.x, (float)upper.y, (float)upper.z)};
    float [] distancesToVertex = new float [boxVertices.length];
    for (int i = 0; i < distancesToVertex.length; i++) {
      distancesToVertex [i] = point.distanceSquared(boxVertices [i]);
    }
    float [] distancesToSide = {
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 1, 3, 2, 2),
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 1, 5, 4, 1),
        getDistanceToSide(point, boxVertices, distancesToVertex, 0, 2, 6, 4, 0),
        getDistanceToSide(point, boxVertices, distancesToVertex, 4, 5, 7, 6, 2),
        getDistanceToSide(point, boxVertices, distancesToVertex, 2, 3, 7, 6, 1),
        getDistanceToSide(point, boxVertices, distancesToVertex, 1, 3, 7, 5, 0)};
    float distance = distancesToSide [0];
    for (int i = 1; i < distancesToSide.length; i++) {
      distance = Math.min(distance, distancesToSide [i]);
    }
    return distance;
  }

  /**
   * Returns the distance between the given <code>point</code> and the plane defined by four vertices.
   */
  private float getDistanceToSide(Point3f point, Point3f [] boxVertices, float [] distancesSquaredToVertex,
                                  int index1, int index2, int index3, int index4, int axis) {
    switch (axis) {
      case 0 : // Normal along x axis
        if (point.y <= boxVertices [index1].y) {
          if (point.z <= boxVertices [index1].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.z >= boxVertices [index4].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.y >= boxVertices [index2].y) {
          if (point.z <= boxVertices [index2].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.z >= boxVertices [index3].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.z <= boxVertices [index1].z) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.z >= boxVertices [index4].z) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
      case 1 : // Normal along y axis
        if (point.x <= boxVertices [index1].x) {
          if (point.z <= boxVertices [index1].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.z >= boxVertices [index4].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.x >= boxVertices [index2].x) {
          if (point.z <= boxVertices [index2].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.z >= boxVertices [index3].z) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.z <= boxVertices [index1].z) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.z >= boxVertices [index4].z) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
      case 2 : // Normal along z axis
        if (point.x <= boxVertices [index1].x) {
          if (point.y <= boxVertices [index1].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index1]);
          } else if (point.y >= boxVertices [index4].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index4]);
          } else {
            return getDistanceToLine(point, boxVertices [index1], boxVertices [index4]);
          }
        } else if (point.x >= boxVertices [index2].x) {
          if (point.y <= boxVertices [index2].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index2]);
          } else if (point.y >= boxVertices [index3].y) {
            return (float)Math.sqrt(distancesSquaredToVertex [index3]);
          } else {
            return getDistanceToLine(point, boxVertices [index2], boxVertices [index3]);
          }
        } else if (point.y <= boxVertices [index1].y) {
          return getDistanceToLine(point, boxVertices [index1], boxVertices [index2]);
        } else if (point.y >= boxVertices [index4].y) {
          return getDistanceToLine(point, boxVertices [index3], boxVertices [index4]);
        }
        break;
    }

    // Return distance to plane
    // from https://fr.wikipedia.org/wiki/Distance_d%27un_point_�_un_plan
    Vector3f vector1 = new Vector3f(boxVertices [index2].x - boxVertices [index1].x,
        boxVertices [index2].y - boxVertices [index1].y,
        boxVertices [index2].z - boxVertices [index1].z);
    Vector3f vector2 = new Vector3f(boxVertices [index3].x - boxVertices [index1].x,
        boxVertices [index3].y - boxVertices [index1].y,
        boxVertices [index3].z - boxVertices [index1].z);
    Vector3f normal = new Vector3f();
    normal.cross(vector1, vector2);
    return Math.abs(normal.dot(new Vector3f(boxVertices [index1].x - point.x, boxVertices [index1].y - point.y, boxVertices [index1].z - point.z))) /
        normal.length();
  }

  /**
   * Returns the distance between the given <code>point</code> and the line defined by two points.
   */
  private float getDistanceToLine(Point3f point, Point3f point1, Point3f point2) {
    // From https://fr.wikipedia.org/wiki/Distance_d%27un_point_�_une_droite#Dans_l.27espace
    Vector3f lineDirection = new Vector3f(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z);
    Vector3f vector = new Vector3f(point.x - point1.x, point.y - point1.y, point.z - point1.z);
    Vector3f crossProduct = new Vector3f();
    crossProduct.cross(lineDirection, vector);
    return crossProduct.length() / lineDirection.length();
  }

  /**
   * Frees printed image kept in cache.
   */
  private void clearPrintedImageCache() {
    this.printedImageCache = null;
  }

  /**
   * Updates <code>viewPlatformTransform</code> transform from <code>camera</code> angles and location.
   */
  private void updateViewPlatformTransform(TransformGroup viewPlatformTransform,
                                           Camera camera, boolean updateWithAnimation) {
    if (updateWithAnimation) {
      // Get the camera interpolator
      CameraInterpolator cameraInterpolator =
          (CameraInterpolator)viewPlatformTransform.getChild(viewPlatformTransform.numChildren() - 1);
      cameraInterpolator.moveCamera(camera);
    } else {
      Transform3D transform = new Transform3D();
      updateViewPlatformTransform(transform, camera.getX(), camera.getY(),
          camera.getZ(), camera.getYaw(), camera.getPitch());
      viewPlatformTransform.setTransform(transform);
    }
    clearPrintedImageCache();
  }

  /**
   * An interpolator that computes smooth camera moves.
   */
  private class CameraInterpolator extends TransformInterpolator {
    private final ScheduledExecutorService scheduledExecutor;
    private Camera initialCamera;
    private Camera finalCamera;

    public CameraInterpolator(TransformGroup transformGroup) {
      this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
      setTarget(transformGroup);
    }

    /**
     * Moves the camera to a new location.
     */
    public void moveCamera(Camera finalCamera) {
      if (this.finalCamera == null
          || this.finalCamera.getX() != finalCamera.getX()
          || this.finalCamera.getY() != finalCamera.getY()
          || this.finalCamera.getZ() != finalCamera.getZ()
          || this.finalCamera.getYaw() != finalCamera.getYaw()
          || this.finalCamera.getPitch() != finalCamera.getPitch()) {
        synchronized (this) {
          Alpha alpha = getAlpha();
          if (alpha == null || alpha.finished()) {
            this.initialCamera = new Camera(camera.getX(), camera.getY(), camera.getZ(),
                camera.getYaw(), camera.getPitch(), camera.getFieldOfView());
          } else if (alpha.value() < 0.3) {
            Transform3D finalTransformation = new Transform3D();
            // Jump directly to final location
            updateViewPlatformTransform(finalTransformation, this.finalCamera.getX(), this.finalCamera.getY(), this.finalCamera.getZ(),
                this.finalCamera.getYaw(), this.finalCamera.getPitch());
            getTarget().setTransform(finalTransformation);
            this.initialCamera = this.finalCamera;
          } else {
            // Compute initial location from current alpha value
            this.initialCamera = new Camera(this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha.value(),
                this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha.value(),
                this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha.value(),
                this.initialCamera.getYaw() + (this.finalCamera.getYaw() - this.initialCamera.getYaw()) * alpha.value(),
                this.initialCamera.getPitch() + (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha.value(),
                finalCamera.getFieldOfView());
          }
          this.finalCamera = new Camera(finalCamera.getX(), finalCamera.getY(), finalCamera.getZ(),
              finalCamera.getYaw(), finalCamera.getPitch(), finalCamera.getFieldOfView());

          // Create an animation that will interpolate camera location
          // between initial camera and final camera in 150 ms
          if (alpha == null) {
            alpha = new Alpha(1, 150);
            setAlpha(alpha);
          }
          // Start animation now
          alpha.setStartTime(System.currentTimeMillis());
          // In case system is overloaded computeTransform won't be called
          // ensure final location will always be set after 150 ms
          this.scheduledExecutor.schedule(new Runnable() {
              public void run() {
                if (getAlpha().value() == 1) {
                  Transform3D transform = new Transform3D();
                  computeTransform(1, transform);
                  getTarget().setTransform(transform);
                }
              }
            }, 150, TimeUnit.MILLISECONDS);
        }
      }
    }

    @Override
    public synchronized void computeTransform(float alpha, Transform3D transform) {
      updateViewPlatformTransform(transform,
          this.initialCamera.getX() + (this.finalCamera.getX() - this.initialCamera.getX()) * alpha,
          this.initialCamera.getY() + (this.finalCamera.getY() - this.initialCamera.getY()) * alpha,
          this.initialCamera.getZ() + (this.finalCamera.getZ() - this.initialCamera.getZ()) * alpha,
          this.initialCamera.getYaw() + (this.finalCamera.getYaw() - this.initialCamera.getYaw()) * alpha,
          this.initialCamera.getPitch() + (this.finalCamera.getPitch() - this.initialCamera.getPitch()) * alpha);
    }
  }

  /**
   * Updates <code>viewPlatformTransform</code> transform from camera angles and location.
   */
  private void updateViewPlatformTransform(Transform3D transform,
                                           float cameraX, float cameraY, float cameraZ,
                                           float cameraYaw, float cameraPitch) {
    Transform3D yawRotation = new Transform3D();
    yawRotation.rotY(-cameraYaw + Math.PI);

    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(-cameraPitch);
    yawRotation.mul(pitchRotation);

    transform.setIdentity();
    transform.setTranslation(new Vector3f(cameraX, cameraZ, cameraY));
    transform.mul(yawRotation);

    this.camera = new Camera(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch, 0);
  }

  /**
   * Adds AWT mouse listeners to <code>component3D</code> that calls back <code>controller</code> methods.
   */
  private void addMouseListeners(final HomeController3D controller, final Component component3D) {
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int        xLastMouseMove;
        private int        yLastMouseMove;
        private Component  grabComponent;
        private Component  previousMouseEventTarget;

        @Override
        public void mousePressed(MouseEvent ev) {
          if (!retargetMouseEventToNavigationPanelChildren(ev)) {
            if (ev.isPopupTrigger()) {
              mouseReleased(ev);
            } else if (isEnabled()) {
              requestFocusInWindow();
              this.xLastMouseMove = ev.getX();
              this.yLastMouseMove = ev.getY();
            }
          }
        }

        @Override
        public void mouseReleased(MouseEvent ev) {
          if (!retargetMouseEventToNavigationPanelChildren(ev)) {
            if (ev.isPopupTrigger()) {
              JPopupMenu componentPopupMenu = getComponentPopupMenu();
              if (componentPopupMenu != null) {
                componentPopupMenu.show(HomeComponent3D.this, ev.getX(), ev.getY());
              }
            }
          }
        }

        @Override
        public void mouseClicked(MouseEvent ev) {
          retargetMouseEventToNavigationPanelChildren(ev);
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
          retargetMouseEventToNavigationPanelChildren(ev);
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          if (!retargetMouseEventToNavigationPanelChildren(ev)) {
            if (isEnabled()) {
              if (ev.isAltDown()) {
                // Mouse move along Y axis while alt is down changes camera location
                float delta = 1.25f * (this.yLastMouseMove - ev.getY());
                // Multiply delta by 5 if shift is down
                if (ev.isShiftDown()) {
                  delta *= 5;
                }
                controller.moveCamera(delta);
              } else {
                final float ANGLE_FACTOR = 0.005f;
                // Mouse move along X axis changes camera yaw
                float yawDelta = ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove);
                // Multiply yaw delta by 5 if shift is down
                if (ev.isShiftDown()) {
                  yawDelta *= 5;
                }
                controller.rotateCameraYaw(yawDelta);

                // Mouse move along Y axis changes camera pitch
                float pitchDelta = ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove);
                controller.rotateCameraPitch(pitchDelta);
              }

              this.xLastMouseMove = ev.getX();
              this.yLastMouseMove = ev.getY();
            }
          }
        }

        /**
         * Retargets to the first component of navigation panel able to manage the given event
         * and returns <code>true</code> if a component consumed the event
         * or needs to be repainted (meaning its state changed).
         * This implementation doesn't cover all the possible cases (mouseEntered and mouseExited
         * events are managed only during mouseDragged event).
         */
        private boolean retargetMouseEventToNavigationPanelChildren(MouseEvent ev) {
          if (navigationPanel != null
              && navigationPanel.isVisible()) {
            if (this.grabComponent != null
                && (ev.getID() == MouseEvent.MOUSE_RELEASED
                    || ev.getID() == MouseEvent.MOUSE_DRAGGED)) {
              Point point = SwingUtilities.convertPoint(ev.getComponent(), ev.getPoint(), this.grabComponent);
              dispatchRetargetedEvent(deriveEvent(ev, this.grabComponent, ev.getID(), point.x, point.y));
              if (ev.getID() == MouseEvent.MOUSE_RELEASED) {
                this.grabComponent = null;
              } else {
                if (this.previousMouseEventTarget == null
                    && this.grabComponent.contains(point)) {
                  dispatchRetargetedEvent(deriveEvent(ev, this.grabComponent, MouseEvent.MOUSE_ENTERED, point.x, point.y));
                  this.previousMouseEventTarget = this.grabComponent;
                } else if (this.previousMouseEventTarget != null
                    && !this.grabComponent.contains(point)) {
                  dispatchRetargetedEvent(deriveEvent(ev, this.grabComponent, MouseEvent.MOUSE_EXITED, point.x, point.y));
                  this.previousMouseEventTarget = null;
                }
              }
              return true;
            } else {
              Component mouseEventTarget = retargetMouseEvent(navigationPanel, ev);
              if (mouseEventTarget != null) {
                this.previousMouseEventTarget = mouseEventTarget;
                return true;
              }
            }
          }
          return false;
        }

        private Component retargetMouseEvent(Component component, MouseEvent ev) {
          if (component.getBounds().contains(ev.getPoint())) {
            if (component instanceof Container) {
              Container container = (Container)component;
              for (int i = container.getComponentCount() - 1; i >= 0; i--) {
                Component c = container.getComponent(i);
                MouseEvent retargetedEvent = deriveEvent(ev, component, ev.getID(),
                    ev.getX() - component.getX(), ev.getY() - component.getY());
                Component mouseEventTarget = retargetMouseEvent(c, retargetedEvent);
                if (mouseEventTarget != null) {
                  return mouseEventTarget;
                }
              }
            }
            int newX = ev.getX() - component.getX();
            int newY = ev.getY() - component.getY();
            if (dispatchRetargetedEvent(deriveEvent(ev, component, ev.getID(), newX, newY))) {
              if (ev.getID() == MouseEvent.MOUSE_PRESSED) {
                this.grabComponent = component;
              }
              return component;
            }
          }
          return null;
        }

        /**
         * Dispatches the given event to its component and returns <code>true</code> if component needs to be redrawn.
         */
        private boolean dispatchRetargetedEvent(MouseEvent ev) {
          ev.getComponent().dispatchEvent(ev);
          if (!RepaintManager.currentManager(ev.getComponent()).getDirtyRegion((JComponent)ev.getComponent()).isEmpty()) {
            updateNavigationPanelImage();
            component3D.repaint();
            return true;
          }
          return false;
        }

        /**
         * Returns a new <code>MouseEvent</code> derived from the one given in parameter.
         */
        private MouseEvent deriveEvent(MouseEvent ev, Component component, int id, int x, int y) {
          return new MouseEvent(component, id, ev.getWhen(),
              ev.getModifiersEx() | ev.getModifiers(), x, y,
              ev.getClickCount(), ev.isPopupTrigger(), ev.getButton());
        }
      };
    MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent ev) {
          if (isEnabled()) {
            // Mouse wheel changes camera location
            float delta = -2.5f * ev.getWheelRotation();
            // Multiply delta by 10 if shift is down
            if (ev.isShiftDown()) {
              delta *= 5;
            }
            controller.moveCamera(delta);
          }
        }
      };

    component3D.addMouseListener(mouseListener);
    component3D.addMouseMotionListener(mouseListener);
    component3D.addMouseWheelListener(mouseWheelListener);
    // Add a mouse listener to this component to request focus in case user clicks in component border
    super.addMouseListener(new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          requestFocusInWindow();
        }
      });
  }

  /**
   * Installs keys bound to actions.
   */
  private void installKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    // Tolerate alt modifier for forward and backward moves with UP and DOWN keys to avoid
    // the user to release the alt key when he wants to alternate forward/backward and sideways moves
    inputMap.put(KeyStroke.getKeyStroke("shift UP"), ActionType.MOVE_CAMERA_FAST_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift alt UP"), ActionType.MOVE_CAMERA_FAST_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift W"), ActionType.MOVE_CAMERA_FAST_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_CAMERA_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("alt UP"), ActionType.MOVE_CAMERA_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("W"), ActionType.MOVE_CAMERA_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift alt DOWN"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift S"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_CAMERA_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("alt DOWN"), ActionType.MOVE_CAMERA_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("S"), ActionType.MOVE_CAMERA_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift alt LEFT"), ActionType.MOVE_CAMERA_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("alt LEFT"), ActionType.MOVE_CAMERA_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift alt RIGHT"), ActionType.MOVE_CAMERA_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("alt RIGHT"), ActionType.MOVE_CAMERA_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift A"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.ROTATE_CAMERA_YAW_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("A"), ActionType.ROTATE_CAMERA_YAW_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift D"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("D"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift PAGE_UP"), ActionType.ROTATE_CAMERA_PITCH_FAST_UP);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), ActionType.ROTATE_CAMERA_PITCH_UP);
    inputMap.put(KeyStroke.getKeyStroke("shift PAGE_DOWN"), ActionType.ROTATE_CAMERA_PITCH_FAST_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), ActionType.ROTATE_CAMERA_PITCH_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("shift HOME"), ActionType.ELEVATE_CAMERA_FAST_UP);
    inputMap.put(KeyStroke.getKeyStroke("HOME"), ActionType.ELEVATE_CAMERA_UP);
    inputMap.put(KeyStroke.getKeyStroke("shift END"), ActionType.ELEVATE_CAMERA_FAST_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("END"), ActionType.ELEVATE_CAMERA_DOWN);
  }

  /**
   * Creates actions that calls back <code>controller</code> methods.
   */
  private void createActions(final HomeController3D controller) {
    // Move camera action mapped to arrow keys
    class MoveCameraAction extends AbstractAction {
      private final float delta;

      public MoveCameraAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.moveCamera(this.delta);
      }
    }
    // Move camera sideways action mapped to arrow keys
    class MoveCameraSidewaysAction extends AbstractAction {
      private final float delta;

      public MoveCameraSidewaysAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.moveCameraSideways(this.delta);
      }
    }
    // Elevate camera action mapped to arrow keys
    class ElevateCameraAction extends AbstractAction {
      private final float delta;

      public ElevateCameraAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.elevateCamera(this.delta);
      }
    }
    // Rotate camera yaw action mapped to arrow keys
    class RotateCameraYawAction extends AbstractAction {
      private final float delta;

      public RotateCameraYawAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.rotateCameraYaw(this.delta);
      }
    }
    // Rotate camera pitch action mapped to arrow keys
    class RotateCameraPitchAction extends AbstractAction {
      private final float delta;

      public RotateCameraPitchAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.rotateCameraPitch(this.delta);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.MOVE_CAMERA_FORWARD, new MoveCameraAction(6.5f));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_FORWARD, new MoveCameraAction(32.5f));
    actionMap.put(ActionType.MOVE_CAMERA_BACKWARD, new MoveCameraAction(-6.5f));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_BACKWARD, new MoveCameraAction(-32.5f));
    actionMap.put(ActionType.MOVE_CAMERA_LEFT, new MoveCameraSidewaysAction(-2.5f));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_LEFT, new MoveCameraSidewaysAction(-10f));
    actionMap.put(ActionType.MOVE_CAMERA_RIGHT, new MoveCameraSidewaysAction(2.5f));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_RIGHT, new MoveCameraSidewaysAction(10f));
    actionMap.put(ActionType.ELEVATE_CAMERA_DOWN, new ElevateCameraAction(-2.5f));
    actionMap.put(ActionType.ELEVATE_CAMERA_FAST_DOWN, new ElevateCameraAction(-10f));
    actionMap.put(ActionType.ELEVATE_CAMERA_UP, new ElevateCameraAction(2.5f));
    actionMap.put(ActionType.ELEVATE_CAMERA_FAST_UP, new ElevateCameraAction(10f));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_LEFT, new RotateCameraYawAction(-(float)Math.PI / 60));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_FAST_LEFT, new RotateCameraYawAction(-(float)Math.PI / 12));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_RIGHT, new RotateCameraYawAction((float)Math.PI / 60));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT, new RotateCameraYawAction((float)Math.PI / 12));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_UP, new RotateCameraPitchAction(-(float)Math.PI / 120));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_FAST_UP, new RotateCameraPitchAction(-(float)Math.PI / 24));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_DOWN, new RotateCameraPitchAction((float)Math.PI / 120));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_FAST_DOWN, new RotateCameraPitchAction((float)Math.PI / 24));
  }

  @Override
  public void addMouseMotionListener(final MouseMotionListener l) {
    super.addMouseMotionListener(l);
    if (this.component3D != null) {
      this.component3D.addMouseMotionListener(new MouseMotionListener() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }

          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }
        });
    }
  }

  @Override
  public void removeMouseMotionListener(final MouseMotionListener l) {
    if (this.component3D != null) {
      this.component3D.removeMouseMotionListener(l);
    }
    super.removeMouseMotionListener(l);
  }

  @Override
  public void addMouseListener(final MouseListener l) {
    super.addMouseListener(l);
    if (this.component3D != null) {
      this.component3D.addMouseListener(new MouseListener() {
          public void mouseReleased(MouseEvent ev) {
            l.mouseReleased(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }

          public void mousePressed(MouseEvent ev) {
            l.mousePressed(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }

          public void mouseExited(MouseEvent ev) {
            l.mouseExited(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }

          public void mouseEntered(MouseEvent ev) {
            l.mouseEntered(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }

          public void mouseClicked(MouseEvent ev) {
            l.mouseClicked(SwingUtilities.convertMouseEvent(component3D, ev, HomeComponent3D.this));
          }
        });
    }
  }

  @Override
  public void removeMouseListener(final MouseListener l) {
    if (this.component3D != null) {
      this.component3D.removeMouseListener(l);
    }
    super.removeMouseListener(l);
  }

  /**
   * Returns the closest {@link Selectable} object at screen coordinates (x, y),
   * or <code>null</code> if not found.
   */
  public Selectable getClosestItemAt(int x, int y) {
    if (this.component3D != null) {
      Canvas3D canvas;
      if (this.component3D instanceof JCanvas3D) {
        canvas = ((JCanvas3D)this.component3D).getOffscreenCanvas3D();
      } else {
        canvas = (Canvas3D)this.component3D;
      }
      PickCanvas pickCanvas = new PickCanvas(canvas, this.onscreenUniverse.getLocale());
      pickCanvas.setMode(PickCanvas.GEOMETRY);
      Point canvasPoint = SwingUtilities.convertPoint(this, x, y, this.component3D);
      pickCanvas.setShapeLocation(canvasPoint.x, canvasPoint.y);
      PickResult result = pickCanvas.pickClosest();
      if (result != null) {
        Node pickedNode = result.getNode(PickResult.SHAPE3D);
        while (!this.homeObjects.containsValue(pickedNode)
               && pickedNode.getParent() != null) {
          pickedNode = pickedNode.getParent();
        }
        if (pickedNode != null) {
          for (Map.Entry<Selectable, Object3DBranch> entry : this.homeObjects.entrySet()) {
            if (entry.getValue() == pickedNode) {
              return entry.getKey();
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns a new scene tree root.
   */
  private BranchGroup createSceneTree(boolean displayShadowOnFloor,
                                      boolean listenToHomeUpdates,
                                      boolean waitForLoading) {
    BranchGroup root = new BranchGroup();
    // Build scene tree
    root.addChild(createHomeTree(displayShadowOnFloor, listenToHomeUpdates, waitForLoading));
    Node backgroundNode = createBackgroundNode(listenToHomeUpdates, waitForLoading);
    root.addChild(backgroundNode);
    Node groundNode = createGroundNode(-0.5E7f, -0.5E7f, 1E7f, 1E7f, listenToHomeUpdates, waitForLoading);
    root.addChild(groundNode);

    this.sceneLights = createLights(groundNode, listenToHomeUpdates);
    for (Light light : this.sceneLights) {
      root.addChild(light);
    }

    return root;
  }

  /**
   * Returns a new background node.
   */
  private Node createBackgroundNode(boolean listenToHomeUpdates, final boolean waitForLoading) {
    final Appearance skyBackgroundAppearance = new Appearance();
    ColoringAttributes skyBackgroundColoringAttributes = new ColoringAttributes();
    skyBackgroundAppearance.setColoringAttributes(skyBackgroundColoringAttributes);
    TextureAttributes skyBackgroundTextureAttributes = new TextureAttributes();
    skyBackgroundAppearance.setTextureAttributes(skyBackgroundTextureAttributes);
    // Allow sky color and texture to change
    skyBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    skyBackgroundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    skyBackgroundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    skyBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
    skyBackgroundTextureAttributes.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

    Geometry topHalfSphereGeometry = createHalfSphereGeometry(true);
    final Shape3D topHalfSphere = new Shape3D(topHalfSphereGeometry, skyBackgroundAppearance);
    BranchGroup backgroundBranch = new BranchGroup();
    backgroundBranch.addChild(topHalfSphere);

    final Appearance bottomAppearance = new Appearance();
    final RenderingAttributes bottomRenderingAttributes = new RenderingAttributes();
    bottomRenderingAttributes.setVisible(false);
    bottomAppearance.setRenderingAttributes(bottomRenderingAttributes);
    bottomRenderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
    Shape3D bottomHalfSphere = new Shape3D(createHalfSphereGeometry(false), bottomAppearance);
    backgroundBranch.addChild(bottomHalfSphere);

    // Add two planes at ground level to complete landscape at the horizon when camera is above horizon
    // (one at y = -0.01 to fill the horizon and a lower one to fill the lower part of the scene)
    final Appearance groundBackgroundAppearance = new Appearance();
    TextureAttributes groundBackgroundTextureAttributes = new TextureAttributes();
    groundBackgroundTextureAttributes.setTextureMode(TextureAttributes.MODULATE);
    groundBackgroundAppearance.setTextureAttributes(groundBackgroundTextureAttributes);
    groundBackgroundAppearance.setTexCoordGeneration(
        new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2,
            new Vector4f(1E5f, 0, 0, 0), new Vector4f(0, 0, 1E5f, 0)));
    final RenderingAttributes groundRenderingAttributes = new RenderingAttributes();
    groundBackgroundAppearance.setRenderingAttributes(groundRenderingAttributes);
    // Allow ground color and texture to change
    groundBackgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    groundBackgroundAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
    groundRenderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);

    GeometryInfo geometryInfo = new GeometryInfo (GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(new Point3f [] {
          new Point3f(-1f, -0.01f, -1f),
          new Point3f(-1f, -0.01f, 1f),
          new Point3f(1f, -0.01f, 1f),
          new Point3f(1f, -0.01f, -1f),
          new Point3f(-1f, -0.1f, -1f),
          new Point3f(-1f, -0.1f, 1f),
          new Point3f(1f, -0.1f, 1f),
          new Point3f(1f, -0.1f, -1f)});
    geometryInfo.setCoordinateIndices(new int [] {0, 1, 2, 3, 4, 5, 6, 7});
    geometryInfo.setNormals(new Vector3f [] {new Vector3f(0, 1, 0)});
    geometryInfo.setNormalIndices(new int [] {0, 0, 0, 0, 0, 0, 0, 0});
    Shape3D groundBackground = new Shape3D(geometryInfo.getIndexedGeometryArray(), groundBackgroundAppearance);
    backgroundBranch.addChild(groundBackground);

    // Add its own lights to background to ensure they have an effect
    for (Light light : createBackgroundLights(listenToHomeUpdates)) {
      backgroundBranch.addChild(light);
    }

    final Background background = new Background(backgroundBranch);
    updateBackgroundColorAndTexture(skyBackgroundAppearance, groundBackgroundAppearance, this.home, waitForLoading);
    background.setApplicationBounds(new BoundingBox(
        new Point3d(-1E7, -1E7, -1E7),
        new Point3d(1E7, 1E7, 1E7)));

    if (listenToHomeUpdates) {
      // Add a listener on sky color and texture properties change
      this.backgroundChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateBackgroundColorAndTexture(skyBackgroundAppearance, groundBackgroundAppearance, home, waitForLoading);
          }
        };
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.SKY_COLOR, this.backgroundChangeListener);
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.SKY_TEXTURE, this.backgroundChangeListener);
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.GROUND_COLOR, this.backgroundChangeListener);
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.GROUND_TEXTURE, this.backgroundChangeListener);
      // Make groundBackground invisible and bottom half sphere visible if camera is below the ground
      this.elevationChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getSource() == home) {
              // Move listener to the new camera
              ((Camera)ev.getOldValue()).removePropertyChangeListener(this);
              home.getCamera().addPropertyChangeListener(this);
            }
            if (ev.getSource() == home
                || Camera.Property.Z.name().equals(ev.getPropertyName())) {
              groundRenderingAttributes.setVisible(home.getCamera().getZ() >= 0);
              bottomRenderingAttributes.setVisible(home.getCamera().getZ() < 0);
            }
          }
        };
      this.home.getCamera().addPropertyChangeListener(this.elevationChangeListener);
      this.home.addPropertyChangeListener(Home.Property.CAMERA, this.elevationChangeListener);
    }
    return background;
  }

  /**
   * Returns a half sphere oriented inward and with texture ordinates
   * that spread along an hemisphere.
   */
  private Geometry createHalfSphereGeometry(boolean top) {
    final int divisionCount = 48;
    Point3f [] coords = new Point3f [divisionCount * divisionCount];
    TexCoord2f [] textureCoords = top ? new TexCoord2f [divisionCount * divisionCount] : null;
    Color3f [] colors = top ? null : new Color3f [divisionCount * divisionCount];
    for (int i = 0, k = 0; i < divisionCount; i++) {
      double alpha = i * 2 * Math.PI / divisionCount;
      float cosAlpha = (float)Math.cos(alpha);
      float sinAlpha = (float)Math.sin(alpha);
      double nextAlpha = (i  + 1) * 2 * Math.PI / divisionCount;
      float cosNextAlpha = (float)Math.cos(nextAlpha);
      float sinNextAlpha = (float)Math.sin(nextAlpha);
      for (int j = 0, max = divisionCount / 4; j < max; j++) {
        double beta = 2 * j * Math.PI / divisionCount;
        float cosBeta = (float)Math.cos(beta);
        float sinBeta = (float)Math.sin(beta);
        // Correct the bottom of the hemisphere to avoid seeing a bottom hemisphere at the horizon
        float y = j != 0 ? (top ? sinBeta : -sinBeta) : -0.01f;
        double nextBeta = 2 * (j + 1) * Math.PI / divisionCount;
        if (!top) {
          nextBeta = -nextBeta;
        }
        float cosNextBeta = (float)Math.cos(nextBeta);
        float sinNextBeta = (float)Math.sin(nextBeta);
        if (top) {
          coords [k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
          textureCoords [k++] = new TexCoord2f((float)i / divisionCount, (float)j / max);

          coords [k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
          textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, (float)j / max);

          coords [k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
          textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, (float)(j + 1) / max);

          coords [k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
          textureCoords [k++] = new TexCoord2f((float)i / divisionCount, (float)(j + 1) / max);
        } else {
          coords [k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
          float color1 = .9f + y * .5f;
          colors [k++] = new Color3f(color1, color1, color1);

          coords [k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
          float color2 = .9f + sinNextBeta * .5f;
          colors [k++] = new Color3f(color2, color2, color2);

          coords [k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
          colors [k++] = new Color3f(color2, color2, color2);

          coords [k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
          colors [k++] = new Color3f(color1, color1, color1);
        }
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(coords);
    if (textureCoords != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords);
    }
    if (colors != null) {
      geometryInfo.setColors(colors);
    }
    geometryInfo.indexify();
    geometryInfo.compact();
    Geometry halfSphereGeometry = geometryInfo.getIndexedGeometryArray();
    return halfSphereGeometry;
  }

  /**
   * Updates <code>backgroundAppearance</code> color and texture from <code>home</code> sky color and texture.
   */
  private void updateBackgroundColorAndTexture(final Appearance skyBackgroundAppearance,
                                               final Appearance groundBackgroundAppearance,
                                               Home home,
                                               boolean waitForLoading) {
    Color3f skyColor = new Color3f(new Color(home.getEnvironment().getSkyColor()));
    skyBackgroundAppearance.getColoringAttributes().setColor(skyColor);
    HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
    if (skyTexture != null) {
      final Transform3D transform = new Transform3D();
      transform.setTranslation(new Vector3f(-skyTexture.getXOffset(), 0, 0));
      TextureManager textureManager = TextureManager.getInstance();
      if (waitForLoading) {
        // Don't share the background texture otherwise if might not be rendered correctly
        skyBackgroundAppearance.setTexture(textureManager.loadTexture(skyTexture.getImage()));
        skyBackgroundAppearance.getTextureAttributes().setTextureTransform(transform);
      } else {
        textureManager.loadTexture(skyTexture.getImage(), waitForLoading,
            new TextureManager.TextureObserver() {
                public void textureUpdated(Texture texture) {
                  // Use a copy of the texture in case it's used in an other universe
                  skyBackgroundAppearance.setTexture((Texture)texture.cloneNodeComponent(false));
                  skyBackgroundAppearance.getTextureAttributes().setTextureTransform(transform);
                }
              });
      }
    } else {
      skyBackgroundAppearance.setTexture(null);
    }

    HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
    if (groundTexture != null) {
      groundBackgroundAppearance.setMaterial(new Material(
          new Color3f(1, 1, 1), new Color3f(), new Color3f(1, 1, 1), new Color3f(0, 0, 0), 1));
      TextureManager textureManager = TextureManager.getInstance();
      if (waitForLoading) {
        groundBackgroundAppearance.setTexture(textureManager.loadTexture(groundTexture.getImage()));
      } else {
        textureManager.loadTexture(groundTexture.getImage(), waitForLoading,
            new TextureManager.TextureObserver() {
                public void textureUpdated(Texture texture) {
                  // Use a copy of the texture in case it's used in an other universe
                  groundBackgroundAppearance.setTexture((Texture)texture.cloneNodeComponent(false));
                }
              });
      }
    } else {
      int groundColor = home.getEnvironment().getGroundColor();
      Color3f color = new Color3f(((groundColor >>> 16) & 0xFF) / 255.f,
                                  ((groundColor >>> 8) & 0xFF) / 255.f,
                                   (groundColor & 0xFF) / 255.f);
      groundBackgroundAppearance.setMaterial(new Material(color, new Color3f(), color, new Color3f(0, 0, 0), 1));
      groundBackgroundAppearance.setTexture(null);
    }

    clearPrintedImageCache();
  }

  /**
   * Returns a new ground node.
   */
  private Node createGroundNode(final float groundOriginX,
                                final float groundOriginY,
                                final float groundWidth,
                                final float groundDepth,
                                boolean listenToHomeUpdates,
                                boolean waitForLoading) {
    final Ground3D ground3D = new Ground3D(this.home,
        groundOriginX, groundOriginY, groundWidth, groundDepth, waitForLoading);
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3f(0, -0.2f, 0));
    TransformGroup transformGroup = new TransformGroup(translation);
    transformGroup.addChild(ground3D);

    if (listenToHomeUpdates) {
      // Add a listener on ground color and texture properties change
      this.groundChangeListener = new PropertyChangeListener() {
          private Runnable updater;
          public void propertyChange(PropertyChangeEvent ev) {
            if (this.updater == null) {
              // Group updates
              EventQueue.invokeLater(this.updater = new Runnable () {
                public void run() {
                  ground3D.update();
                  updater = null;
                }
              });
            }
            clearPrintedImageCache();
          }
        };
      HomeEnvironment homeEnvironment = this.home.getEnvironment();
      homeEnvironment.addPropertyChangeListener(
          HomeEnvironment.Property.GROUND_COLOR, this.groundChangeListener);
      homeEnvironment.addPropertyChangeListener(
          HomeEnvironment.Property.GROUND_TEXTURE, this.groundChangeListener);
      homeEnvironment.addPropertyChangeListener(
          HomeEnvironment.Property.BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D, this.groundChangeListener);
      this.home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, this.groundChangeListener);
    }

    return transformGroup;
  }

  /**
   * Returns the lights used for the background.
   */
  private Light [] createBackgroundLights(boolean listenToHomeUpdates) {
    final Light [] lights = {
        // Use just one direct light for background because only one horizontal plane is under light
        new DirectionalLight(new Color3f(1.435f, 1.435f, 1.435f), new Vector3f(0f, -1f, 0f)),
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
    for (int i = 0; i < lights.length - 1; i++) {
      // Allow directional lights color and influencing bounds to change
      lights [i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
      // Store default color in user data
      Color3f defaultColor = new Color3f();
      lights [i].getColor(defaultColor);
      lights [i].setUserData(defaultColor);
      updateLightColor(lights [i]);
    }

    final Bounds defaultInfluencingBounds = new BoundingSphere(new Point3d(), 2);
    for (Light light : lights) {
      light.setInfluencingBounds(defaultInfluencingBounds);
    }

    if (listenToHomeUpdates) {
      // Add a listener on light color property change to home
      this.backgroundLightColorListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateLightColor(lights [0]);
          }
        };
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.LIGHT_COLOR, this.backgroundLightColorListener);
    }

    return lights;
  }

  /**
   * Returns the lights of the scene.
   */
  private Light [] createLights(final Node groundNode, boolean listenToHomeUpdates) {
    final Light [] lights = {
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(1.5f, -0.8f, -1)),
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(-1.5f, -0.8f, -1)),
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(0, -0.8f, 1)),
        new DirectionalLight(new Color3f(0.7f, 0.7f, 0.7f), new Vector3f(0, 1f, 0)),
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
    for (int i = 0; i < lights.length - 1; i++) {
      // Allow directional lights color and influencing bounds to change
      lights [i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
      lights [i].setCapability(DirectionalLight.ALLOW_SCOPE_WRITE);
      // Store default color in user data
      Color3f defaultColor = new Color3f();
      lights [i].getColor(defaultColor);
      lights [i].setUserData(defaultColor);
      updateLightColor(lights [i]);
    }

    final Bounds defaultInfluencingBounds = new BoundingSphere(new Point3d(), 1E7);
    for (Light light : lights) {
      light.setInfluencingBounds(defaultInfluencingBounds);
    }

    if (listenToHomeUpdates) {
      // Add a listener on light color property change to home
      this.lightColorListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            for (int i = 0; i < lights.length - 1; i++) {
              updateLightColor(lights [i]);
            }
            updateObjects(getHomeObjects(HomeLight.class));
          }
        };
      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);

      // Add a listener on subpart size property change to home
      this.subpartSizeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev != null) {
              // Update 3D objects if not at initialization
              Collection<Selectable> homeItems = new ArrayList<Selectable>(home.getWalls());
              homeItems.addAll(home.getRooms());
              homeItems.addAll(getHomeObjects(HomeLight.class));
              updateObjects(homeItems);
              clearPrintedImageCache();
            }

            // Update default lights scope
            List<Group> scope = null;
            if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
              Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
              scope = new ArrayList<Group>();
              for (Wall wall : home.getWalls()) {
                Object3DBranch wall3D = homeObjects.get(wall);
                if (wall3D instanceof Wall3D) {
                  // Add left and/or right side of the wall to scope
                  float [][] points = wall.getPoints();
                  if (!lightScopeOutsideWallsArea.contains(points [0][0], points [0][1])) {
                    scope.add((Group)wall3D.getChild(1));
                  }
                  if (!lightScopeOutsideWallsArea.contains(points [points.length - 1][0], points [points.length - 1][1])) {
                    scope.add((Group)wall3D.getChild(4));
                  }
                }
                // Add wall top and bottom groups to scope
                scope.add((Group)wall3D.getChild(0));
                scope.add((Group)wall3D.getChild(2));
                scope.add((Group)wall3D.getChild(3));
                scope.add((Group)wall3D.getChild(5));
              }
              List<Selectable> otherItems = new ArrayList<Selectable>(home.getRooms());
              otherItems.addAll(getHomeObjects(HomePieceOfFurniture.class));
              for (Selectable item : otherItems) {
                // Add item to scope if one of its points don't belong to lightScopeWallsArea
                for (float [] point : item.getPoints()) {
                  if (!lightScopeOutsideWallsArea.contains(point [0], point [1])) {
                    Group object3D = homeObjects.get(item);
                    if (object3D instanceof HomePieceOfFurniture3D) {
                      // Add the direct parent of the shape that will be added once loaded
                      // otherwise scope won't be updated automatically
                      object3D = (Group)object3D.getChild(0);
                    }
                    scope.add(object3D);
                    break;
                  }
                }
              }
            } else {
              lightScopeOutsideWallsAreaCache = null;
            }

            for (Light light : lights) {
              if (light instanceof DirectionalLight) {
                light.removeAllScopes();
                if (scope != null) {
                  light.addScope((Group)groundNode);
                  for (Group group : scope) {
                    light.addScope(group);
                  }
                }
              }
            }
          }
        };

      this.home.getEnvironment().addPropertyChangeListener(
          HomeEnvironment.Property.SUBPART_SIZE_UNDER_LIGHT, this.subpartSizeListener);
      this.subpartSizeListener.propertyChange(null);
    }

    return lights;
  }

  /**
   * Returns the home objects displayed by this component of the given class.
   */
  private <T> List<T> getHomeObjects(Class<T> objectClass) {
    return Home.getSubList(new ArrayList<Selectable>(homeObjects.keySet()), objectClass);
  }

  /**
   * Updates<code>light</code> color from <code>home</code> light color.
   */
  private void updateLightColor(Light light) {
    Color3f defaultColor = (Color3f)light.getUserData();
    int lightColor = this.home.getEnvironment().getLightColor();
    light.setColor(new Color3f(((lightColor >>> 16) & 0xFF) / 255f * defaultColor.x,
                                ((lightColor >>> 8) & 0xFF) / 255f * defaultColor.y,
                                        (lightColor & 0xFF) / 255f * defaultColor.z));
    clearPrintedImageCache();
  }

  /**
   * Returns walls area used for light scope outside.
   */
  private Area getLightScopeOutsideWallsArea() {
    if (this.lightScopeOutsideWallsAreaCache == null) {
      // Compute a smaller area surrounding all walls at all levels
      Area wallsPath = new Area();
      for (Wall wall : this.home.getWalls()) {
        Wall thinnerWall = wall.clone();
        thinnerWall.setThickness(Math.max(thinnerWall.getThickness() - 0.1f, 0.08f));
        wallsPath.add(new Area(getShape(thinnerWall.getPoints())));
      }
      Area lightScopeOutsideWallsArea = new Area();
      List<float []> points = new ArrayList<float[]>();
      for (PathIterator it = wallsPath.getPathIterator(null, 1); !it.isDone(); it.next()) {
        float [] point = new float[2];
        switch (it.currentSegment(point)) {
          case PathIterator.SEG_MOVETO :
          case PathIterator.SEG_LINETO :
            points.add(point);
            break;
          case PathIterator.SEG_CLOSE :
            if (points.size() > 2) {
              float [][] pointsArray = points.toArray(new float [points.size()][]);
              if (new Room(pointsArray).isClockwise()) {
                lightScopeOutsideWallsArea.add(new Area(getShape(pointsArray)));
              }
            }
            points.clear();
            break;
        }
      }
      this.lightScopeOutsideWallsAreaCache = lightScopeOutsideWallsArea;
    }
    return this.lightScopeOutsideWallsAreaCache;
  }

  /**
   * Returns a <code>home</code> new tree node, with branches for each wall
   * and piece of furniture of <code>home</code>.
   */
  private Node createHomeTree(boolean displayShadowOnFloor,
                              boolean listenToHomeUpdates,
                              boolean waitForLoading) {
    Group homeRoot = createHomeRoot();
    // Add walls, pieces, rooms, polylines and labels already available
    for (Label label : this.home.getLabels()) {
      addObject(homeRoot, label, listenToHomeUpdates, waitForLoading);
    }
    for (Polyline polyline : this.home.getPolylines()) {
      addObject(homeRoot, polyline, listenToHomeUpdates, waitForLoading);
    }
    for (Room room : this.home.getRooms()) {
      addObject(homeRoot, room, listenToHomeUpdates, waitForLoading);
    }
    for (Wall wall : this.home.getWalls()) {
      addObject(homeRoot, wall, listenToHomeUpdates, waitForLoading);
    }
    Map<HomePieceOfFurniture, Node> pieces3D = new HashMap<HomePieceOfFurniture, Node>();
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
          if (!(childPiece instanceof HomeFurnitureGroup)) {
            pieces3D.put(childPiece, addObject(homeRoot, childPiece, listenToHomeUpdates, waitForLoading));
          }
        }
      } else {
        pieces3D.put(piece, addObject(homeRoot, piece, listenToHomeUpdates, waitForLoading));
      }
    }

    if (displayShadowOnFloor) {
      addShadowOnFloor(homeRoot, pieces3D);
    }

    if (listenToHomeUpdates) {
      // Add level, wall, furniture, room listeners to home for further update
      addLevelListener(homeRoot);
      addWallListener(homeRoot);
      addFurnitureListener(homeRoot);
      addRoomListener(homeRoot);
      addPolylineListener(homeRoot);
      addLabelListener(homeRoot);
      // Add environment listeners
      addEnvironmentListeners();
      // Should update shadow on floor too but in the facts
      // User Interface doesn't propose to modify the furniture of a home
      // that displays shadow on floor yet
    }
    return homeRoot;
  }

  /**
   * Returns a new group at home subtree root.
   */
  private Group createHomeRoot() {
    Group homeGroup = new Group();
    //  Allow group to have new children
    homeGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    homeGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    return homeGroup;
  }

  /**
   * Adds a level listener to home levels that updates the children of the given
   * <code>group</code>, each time a level is added, updated or deleted.
   */
  private void addLevelListener(final Group group) {
    this.levelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.VISIBLE.name().equals(ev.getPropertyName())
              || Level.Property.VIEWABLE.name().equals(ev.getPropertyName())) {
            Set<Selectable> objects = homeObjects.keySet();
            ArrayList<Selectable> updatedItems = new ArrayList<Selectable>(objects.size());
            for (Selectable item : objects) {
              if (item instanceof Room // 3D rooms depend on rooms at other levels
                  || !(item instanceof Elevatable)
                  || ((Elevatable)item).isAtLevel((Level)ev.getSource())) {
                updatedItems.add(item);
              }
            }
            updateObjects(updatedItems);
            groundChangeListener.propertyChange(null);
          } else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())) {
            updateObjects(homeObjects.keySet());
            groundChangeListener.propertyChange(null);
          } else if (Level.Property.BACKGROUND_IMAGE.name().equals(ev.getPropertyName())) {
            groundChangeListener.propertyChange(null);
          } else if (Level.Property.FLOOR_THICKNESS.name().equals(ev.getPropertyName())) {
            updateObjects(home.getWalls());
            updateObjects(home.getRooms());
          } else if (Level.Property.HEIGHT.name().equals(ev.getPropertyName())) {
            updateObjects(home.getRooms());
          }
        }
      };
    for (Level level : this.home.getLevels()) {
      level.addPropertyChangeListener(this.levelChangeListener);
    }
    this.levelListener = new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          Level level = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              level.addPropertyChangeListener(levelChangeListener);
              break;
            case DELETE :
              level.removePropertyChangeListener(levelChangeListener);
              break;
          }
          updateObjects(home.getRooms());
        }
      };
    this.home.addLevelsListener(this.levelListener);
  }

  /**
   * Adds a wall listener to home walls that updates the children of the given
   * <code>group</code>, each time a wall is added, updated or deleted.
   */
  private void addWallListener(final Group group) {
    this.wallChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (!Wall.Property.PATTERN.name().equals(propertyName)) {
            Wall updatedWall = (Wall)ev.getSource();
            updateWall(updatedWall);
            List<Level> levels = home.getLevels();
            if (updatedWall.getLevel() == null
                || updatedWall.isAtLevel(levels.get(levels.size() - 1))) {
              // Update rooms which ceiling height may need an update at last level
              updateObjects(home.getRooms());
            }
            if (updatedWall.getLevel() != null && updatedWall.getLevel().getElevation() < 0) {
              groundChangeListener.propertyChange(null);
            }
            if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
              if (Wall.Property.X_START.name().equals(propertyName)
                  || Wall.Property.Y_START.name().equals(propertyName)
                  || Wall.Property.X_END.name().equals(propertyName)
                  || Wall.Property.Y_END.name().equals(propertyName)
                  || Wall.Property.ARC_EXTENT.name().equals(propertyName)
                  || Wall.Property.THICKNESS.name().equals(propertyName)) {
                lightScopeOutsideWallsAreaCache = null;
                updateObjectsLightScope(null);
              }
            }
          }
        }
      };
    for (Wall wall : this.home.getWalls()) {
      wall.addPropertyChangeListener(this.wallChangeListener);
    }
    this.wallListener = new CollectionListener<Wall>() {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          Wall wall = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addObject(group, wall, true, false);
              wall.addPropertyChangeListener(wallChangeListener);
              break;
            case DELETE :
              deleteObject(wall);
              wall.removePropertyChangeListener(wallChangeListener);
              break;
          }
          lightScopeOutsideWallsAreaCache = null;
          updateObjects(home.getRooms());
          groundChangeListener.propertyChange(null);
          updateObjectsLightScope(null);
        }
      };
    this.home.addWallsListener(this.wallListener);
  }

  /**
   * Adds a furniture listener to home that updates the children of the given <code>group</code>,
   * each time a piece of furniture is added, updated or deleted.
   */
  private void addFurnitureListener(final Group group) {
    this.furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          HomePieceOfFurniture updatedPiece = (HomePieceOfFurniture)ev.getSource();
          String propertyName = ev.getPropertyName();
          if (HomePieceOfFurniture.Property.X.name().equals(propertyName)
              || HomePieceOfFurniture.Property.Y.name().equals(propertyName)
              || HomePieceOfFurniture.Property.ANGLE.name().equals(propertyName)
              || HomePieceOfFurniture.Property.ROLL.name().equals(propertyName)
              || HomePieceOfFurniture.Property.PITCH.name().equals(propertyName)
              || HomePieceOfFurniture.Property.WIDTH.name().equals(propertyName)
              || HomePieceOfFurniture.Property.DEPTH.name().equals(propertyName)) {
            updatePieceOfFurnitureGeometry(updatedPiece, propertyName, (Float)ev.getOldValue());
            updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture [] {updatedPiece}));
          } else if (HomePieceOfFurniture.Property.HEIGHT.name().equals(propertyName)
              || HomePieceOfFurniture.Property.ELEVATION.name().equals(propertyName)
              || HomePieceOfFurniture.Property.MODEL_MIRRORED.name().equals(propertyName)
              || HomePieceOfFurniture.Property.MODEL_TRANSFORMATIONS.name().equals(propertyName)
              || HomePieceOfFurniture.Property.VISIBLE.name().equals(propertyName)
              || HomePieceOfFurniture.Property.LEVEL.name().equals(propertyName)) {
            updatePieceOfFurnitureGeometry(updatedPiece, null, null);
          } else if (HomePieceOfFurniture.Property.COLOR.name().equals(propertyName)
              || HomePieceOfFurniture.Property.TEXTURE.name().equals(propertyName)
              || HomePieceOfFurniture.Property.MODEL_MATERIALS.name().equals(propertyName)
              || HomePieceOfFurniture.Property.SHININESS.name().equals(propertyName)
              || (HomeLight.Property.POWER.name().equals(propertyName)
                  && home.getEnvironment().getSubpartSizeUnderLight() > 0)) {
            updateObjects(Arrays.asList(new HomePieceOfFurniture [] {updatedPiece}));
          }
        }

        private void updatePieceOfFurnitureGeometry(HomePieceOfFurniture piece, String propertyName, Float oldValue) {
          updateObjects(Arrays.asList(new HomePieceOfFurniture [] {piece}));
          if (containsDoorsAndWindows(piece)) {
            if (oldValue != null) {
              HomePieceOfFurniture oldPiece = piece.clone();
              // Reset the modified property to its old value
              if (HomePieceOfFurniture.Property.X.name().equals(propertyName)) {
                oldPiece.setX(oldValue);
              } else if (HomePieceOfFurniture.Property.Y.name().equals(propertyName)) {
                oldPiece.setY(oldValue);
              } else if (HomePieceOfFurniture.Property.ANGLE.name().equals(propertyName)) {
                oldPiece.setAngle(oldValue);
              } else if (HomePieceOfFurniture.Property.WIDTH.name().equals(propertyName)) {
                oldPiece.setWidth(oldValue);
              } else if (HomePieceOfFurniture.Property.DEPTH.name().equals(propertyName)) {
                oldPiece.setDepth(oldValue);
              }
              // For doors and windows, propertyName can't be equal to ROLL or PITCH

              // Update walls which intersect the piece with its old property value and the one with the new value
              updateIntersectingWalls(oldPiece, piece);
            } else {
              // Property value change won't influence the walls that intersect the door or window
              updateIntersectingWalls(piece);
            }
          } else if (containsStaircases(piece)) {
            updateObjects(home.getRooms());
          }
          if (piece.getLevel() != null && piece.getLevel().getElevation() < 0) {
            groundChangeListener.propertyChange(null);
          }
        }
      };
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
          childPiece.addPropertyChangeListener(this.furnitureChangeListener);
        }
      } else {
        piece.addPropertyChangeListener(this.furnitureChangeListener);
      }
    }
    this.furnitureListener = new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getItem();
          switch (ev.getType()) {
            case ADD :
              if (piece instanceof HomeFurnitureGroup) {
                for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                  if (!(childPiece instanceof HomeFurnitureGroup)) {
                    addObject(group, childPiece, true, false);
                    childPiece.addPropertyChangeListener(furnitureChangeListener);
                  }
                }
              } else {
                addObject(group, piece, true, false);
                piece.addPropertyChangeListener(furnitureChangeListener);
              }
              break;
            case DELETE :
              if (piece instanceof HomeFurnitureGroup) {
                for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                  if (!(childPiece instanceof HomeFurnitureGroup)) {
                    deleteObject(childPiece);
                    childPiece.removePropertyChangeListener(furnitureChangeListener);
                  }
                }
              } else {
                deleteObject(piece);
                piece.removePropertyChangeListener(furnitureChangeListener);
              }
              break;
          }
          // If piece is or contains a door or a window, update walls that intersect with piece
          if (containsDoorsAndWindows(piece)) {
            updateIntersectingWalls(piece);
          } else if (containsStaircases(piece)) {
            updateObjects(home.getRooms());
          } else {
            approximateHomeBoundsCache = null;
          }
          groundChangeListener.propertyChange(null);
          updateObjectsLightScope(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
      };
    this.home.addFurnitureListener(this.furnitureListener);
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> is or contains a door or window.
   */
  private boolean containsDoorsAndWindows(HomePieceOfFurniture piece) {
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        if (containsDoorsAndWindows(groupPiece)) {
          return true;
        }
      }
      return false;
    } else {
      return piece.isDoorOrWindow();
    }
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> is or contains a staircase
   * with a top cut out shape.
   */
  private boolean containsStaircases(HomePieceOfFurniture piece) {
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        if (containsStaircases(groupPiece)) {
          return true;
        }
      }
      return false;
    } else {
      return piece.getStaircaseCutOutShape() != null;
    }
  }

  /**
   * Adds a room listener to home rooms that updates the children of the given
   * <code>group</code>, each time a room is added, updated or deleted.
   */
  private void addRoomListener(final Group group) {
    this.roomChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Room updatedRoom = (Room)ev.getSource();
          String propertyName = ev.getPropertyName();
          if (Room.Property.FLOOR_COLOR.name().equals(propertyName)
              || Room.Property.FLOOR_TEXTURE.name().equals(propertyName)
              || Room.Property.FLOOR_SHININESS.name().equals(propertyName)
              || Room.Property.CEILING_COLOR.name().equals(propertyName)
              || Room.Property.CEILING_TEXTURE.name().equals(propertyName)
              || Room.Property.CEILING_SHININESS.name().equals(propertyName)) {
            updateObjects(Arrays.asList(new Room [] {updatedRoom}));
          } else if (Room.Property.FLOOR_VISIBLE.name().equals(propertyName)
              || Room.Property.CEILING_VISIBLE.name().equals(propertyName)
              || Room.Property.LEVEL.name().equals(propertyName)) {
            updateObjects(home.getRooms());
            groundChangeListener.propertyChange(null);
          } else if (Room.Property.POINTS.name().equals(propertyName)) {
            if (homeObjectsToUpdate != null) {
              // Don't try to optimize if more than one room to update
              updateObjects(home.getRooms());
            } else {
              updateObjects(Arrays.asList(new Room [] {updatedRoom}));
              updateObjects(getHomeObjects(HomeLight.class));
              // Search the rooms that overlap the updated one
              Area oldArea = new Area(getShape((float [][])ev.getOldValue()));
              Area newArea = new Area(getShape((float [][])ev.getNewValue()));
              Level updatedRoomLevel = updatedRoom.getLevel();
              for (Room room : home.getRooms()) {
                Level roomLevel = room.getLevel();
                if (room != updatedRoom
                    && (roomLevel == null
                        || Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight() - (roomLevel.getElevation() + roomLevel.getHeight())) < 1E-5
                        || Math.abs(updatedRoomLevel.getElevation() + updatedRoomLevel.getHeight() - (roomLevel.getElevation() - roomLevel.getFloorThickness())) < 1E-5)) {
                  Area roomAreaIntersectionWithOldArea = new Area(getShape(room.getPoints()));
                  Area roomAreaIntersectionWithNewArea = new Area(roomAreaIntersectionWithOldArea);
                  roomAreaIntersectionWithNewArea.intersect(newArea);
                  if (!roomAreaIntersectionWithNewArea.isEmpty()) {
                    updateObjects(Arrays.asList(new Room [] {room}));
                  } else {
                    roomAreaIntersectionWithOldArea.intersect(oldArea);
                    if (!roomAreaIntersectionWithOldArea.isEmpty()) {
                      updateObjects(Arrays.asList(new Room [] {room}));
                    }
                  }
                }
              }
            }
            groundChangeListener.propertyChange(null);
            updateObjectsLightScope(Arrays.asList(new Room [] {updatedRoom}));
            updateObjectsLightScope(getHomeObjects(HomeLight.class));
          }
        }
      };
    for (Room room : this.home.getRooms()) {
      room.addPropertyChangeListener(this.roomChangeListener);
    }
    this.roomListener = new CollectionListener<Room>() {
        public void collectionChanged(CollectionEvent<Room> ev) {
          Room room = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              // Add room to its group at the index indicated by the event
              // to ensure the 3D rooms are drawn in the same order as in the plan
              addObject(group, room, ev.getIndex(), true, false);
              room.addPropertyChangeListener(roomChangeListener);
              break;
            case DELETE :
              deleteObject(room);
              room.removePropertyChangeListener(roomChangeListener);
              break;
          }
          updateObjects(home.getRooms());
          groundChangeListener.propertyChange(null);
          updateObjectsLightScope(Arrays.asList(new Room [] {room}));
          updateObjectsLightScope(getHomeObjects(HomeLight.class));
        }
      };
    this.home.addRoomsListener(this.roomListener);
  }

  /**
   * Returns the path matching points.
   */
  private GeneralPath getShape(float [][] points) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    path.closePath();
    return path;
  }

  /**
   * Adds a polyline listener to home polylines that updates the children of the given
   * <code>group</code>, each time a polyline is added, updated or deleted.
   */
  private void addPolylineListener(final Group group) {
    this.polylineChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Polyline polyline = (Polyline)ev.getSource();
          updateObjects(Arrays.asList(new Polyline [] {polyline}));
        }
      };
    for (Polyline polyline : this.home.getPolylines()) {
      polyline.addPropertyChangeListener(this.polylineChangeListener);
    }
    this.polylineListener = new CollectionListener<Polyline>() {
        public void collectionChanged(CollectionEvent<Polyline> ev) {
          Polyline polyline = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addObject(group, polyline, true, false);
              polyline.addPropertyChangeListener(polylineChangeListener);
              break;
            case DELETE :
              deleteObject(polyline);
              polyline.removePropertyChangeListener(polylineChangeListener);
              break;
          }
        }
      };
    this.home.addPolylinesListener(this.polylineListener);
  }

  /**
   * Adds a label listener to home labels that updates the children of the given
   * <code>group</code>, each time a label is added, updated or deleted.
   */
  private void addLabelListener(final Group group) {
    this.labelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Label label = (Label)ev.getSource();
          updateObjects(Arrays.asList(new Label [] {label}));
        }
      };
    for (Label label : this.home.getLabels()) {
      label.addPropertyChangeListener(this.labelChangeListener);
    }
    this.labelListener = new CollectionListener<Label>() {
        public void collectionChanged(CollectionEvent<Label> ev) {
          Label label = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addObject(group, label, true, false);
              label.addPropertyChangeListener(labelChangeListener);
              break;
            case DELETE :
              deleteObject(label);
              label.removePropertyChangeListener(labelChangeListener);
              break;
          }
        }
      };
    this.home.addLabelsListener(this.labelListener);
  }

  /**
   * Adds a walls alpha change listener and drawing mode change listener to home
   * environment that updates the home scene objects appearance.
   */
  private void addEnvironmentListeners() {
    this.wallsAlphaListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateObjects(home.getWalls());
          updateObjects(home.getRooms());
        }
      };
    this.home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
    this.drawingModeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateObjects(home.getWalls());
          updateObjects(home.getRooms());
          updateObjects(getHomeObjects(HomePieceOfFurniture.class));
        }
      };
    this.home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
  }

  /**
   * Adds to <code>group</code> a branch matching <code>homeObject</code>.
   */
  private Node addObject(Group group, Selectable homeObject, boolean listenToHomeUpdates, boolean waitForLoading) {
    return addObject(group, homeObject, -1, listenToHomeUpdates, waitForLoading);
  }

  /**
   * Adds to <code>group</code> a branch matching <code>homeObject</code> at a given <code>index</code>.
   * If <code>index</code> is equal to -1, <code>homeObject</code> will be added at the end of the group.
   */
  private Node addObject(Group group, Selectable homeObject, int index,
                         boolean listenToHomeUpdates, boolean waitForLoading) {
    Object3DBranch object3D = createObject3D(homeObject, waitForLoading);
    if (listenToHomeUpdates) {
      this.homeObjects.put(homeObject, object3D);
    }
    if (index == -1) {
      group.addChild(object3D);
    } else {
      group.insertChild(object3D, index);
    }
    clearPrintedImageCache();
    return object3D;
  }

  /**
   * Returns the 3D object matching the given home object. If <code>waitForLoading</code>
   * is <code>true</code> the resources used by the returned 3D object should be ready to be displayed.
   * @deprecated Subclasses which used to override this method must be updated to create an instance of
   *    a {@link Object3DFactory factory} and give it as parameter to the constructor of this class.
   */
  private Object3DBranch createObject3D(Selectable homeObject,
                                        boolean waitForLoading) {
    return (Object3DBranch)this.object3dFactory.createObject3D(this.home, homeObject, waitForLoading);
  }

  /**
   * Detaches from the scene the branch matching <code>homeObject</code>.
   */
  private void deleteObject(Selectable homeObject) {
    this.homeObjects.get(homeObject).detach();
    this.homeObjects.remove(homeObject);
    clearPrintedImageCache();
  }

  /**
   * Updates 3D <code>objects</code> later. Should be invoked from Event Dispatch Thread.
   */
  private void updateObjects(Collection<? extends Selectable> objects) {
    if (this.homeObjectsToUpdate != null) {
      this.homeObjectsToUpdate.addAll(objects);
    } else {
      this.homeObjectsToUpdate = new HashSet<Selectable>(objects);
      // Invoke later the update of objects of homeObjectsToUpdate
      EventQueue.invokeLater(new Runnable () {
        public void run() {
          for (Selectable object : homeObjectsToUpdate) {
            Object3DBranch objectBranch = homeObjects.get(object);
            // Check object wasn't deleted since updateObjects call
            if (objectBranch != null) {
              objectBranch.update();
            }
          }
          homeObjectsToUpdate = null;
        }
      });
    }
    clearPrintedImageCache();
    this.approximateHomeBoundsCache = null;
  }

  /**
   * Updates walls that may intersect from the given doors or window.
   */
  private void updateIntersectingWalls(HomePieceOfFurniture ... doorOrWindows) {
    Collection<Wall> walls = this.home.getWalls();
    int wallCount = 0;
    if (this.homeObjectsToUpdate != null) {
      for (Selectable object : this.homeObjectsToUpdate) {
        if (object instanceof Wall) {
          wallCount++;
        }
      }
    }
    // Check if some more walls may require an update
    if (wallCount != walls.size()) {
      List<Wall> updatedWalls = new ArrayList<Wall>();
      Rectangle2D doorOrWindowBounds = null;
      // Compute the approximate bounds of the doors and windows
      for (HomePieceOfFurniture doorOrWindow : doorOrWindows) {
        float [][] points = doorOrWindow.getPoints();
        if (doorOrWindowBounds == null) {
          doorOrWindowBounds = new Rectangle2D.Float(points [0][0], points [0][1], 0, 0);
        } else {
          doorOrWindowBounds.add(points [0][0], points [0][1]);
        }
        for (int i = 1; i < points.length; i++) {
          doorOrWindowBounds.add(points [i][0], points [i][1]);
        }
      }
      // Search walls that intersect the bounds
      for (Wall wall : walls) {
        if (wall.intersectsRectangle((float)doorOrWindowBounds.getX(), (float)doorOrWindowBounds.getY(),
            (float)doorOrWindowBounds.getX() + (float)doorOrWindowBounds.getWidth(),
            (float)doorOrWindowBounds.getY() + (float)doorOrWindowBounds.getHeight())) {
          updatedWalls.add(wall);
        }
      }
      updateObjects(updatedWalls);
    }
  }

  /**
   * Updates <code>wall</code> geometry and the walls at its end or start.
   */
  private void updateWall(Wall wall) {
    Collection<Wall> wallsToUpdate = new ArrayList<Wall>(3);
    wallsToUpdate.add(wall);
    if (wall.getWallAtStart() != null) {
      wallsToUpdate.add(wall.getWallAtStart());
    }
    if (wall.getWallAtEnd() != null) {
      wallsToUpdate.add(wall.getWallAtEnd());
    }
    updateObjects(wallsToUpdate);
  }

  /**
   * Updates the <code>object</code> scope under light later. Should be invoked from Event Dispatch Thread.
   */
  private void updateObjectsLightScope(Collection<? extends Selectable> objects) {
    if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
      if (this.lightScopeObjectsToUpdate != null) {
        if (objects == null) {
          this.lightScopeObjectsToUpdate.clear();
          this.lightScopeObjectsToUpdate.add(null);
        } else if (!this.lightScopeObjectsToUpdate.contains(null)) {
          this.lightScopeObjectsToUpdate.addAll(objects);
        }
      } else {
        this.lightScopeObjectsToUpdate = new HashSet<Selectable>();
        if (objects == null) {
          this.lightScopeObjectsToUpdate.add(null);
        } else {
          this.lightScopeObjectsToUpdate.addAll(objects);
        }
        // Invoke later the update of objects of lightScopeObjectsToUpdate
        EventQueue.invokeLater(new Runnable () {
          public void run() {
            if (lightScopeObjectsToUpdate.contains(null)) {
              subpartSizeListener.propertyChange(null);
            } else if (home.getEnvironment().getSubpartSizeUnderLight() > 0) {
              Area lightScopeOutsideWallsArea = getLightScopeOutsideWallsArea();
              for (Selectable object : lightScopeObjectsToUpdate) {
                Group object3D = homeObjects.get(object);
                if (object3D instanceof HomePieceOfFurniture3D) {
                  // Add the direct parent of the shape that will be added once loaded
                  // otherwise scope won't be updated automatically
                  object3D = (Group)object3D.getChild(0);
                }
                // Check object wasn't deleted since updateObjects call
                if (object3D != null) {
                  // Add item to scope if one of its points don't belong to lightScopeOutsideWallsArea
                  boolean objectInOutsideLightScope = false;
                  for (float [] point : object.getPoints()) {
                    if (!lightScopeOutsideWallsArea.contains(point [0], point [1])) {
                      objectInOutsideLightScope = true;
                      break;
                    }
                  }
                  for (Light light : sceneLights) {
                    if (light instanceof DirectionalLight) {
                      if (objectInOutsideLightScope && light.indexOfScope(object3D) == -1) {
                        light.addScope(object3D);
                      } else if (!objectInOutsideLightScope && light.indexOfScope(object3D) != -1) {
                        light.removeScope(object3D);
                      }
                    }
                  }
                }
              }
            }
            lightScopeObjectsToUpdate = null;
          }
        });
      }
    }
  }

  /**
   * Adds to <code>homeRoot</code> shapes matching the shadow of furniture at their level.
   */
  private void addShadowOnFloor(Group homeRoot, Map<HomePieceOfFurniture, Node> pieces3D) {
    Comparator<Level> levelComparator = new Comparator<Level>() {
        public int compare(Level level1, Level level2) {
          return Float.compare(level1.getElevation(), level2.getElevation());
        }
      };
    Map<Level, Area> areasOnLevel = new TreeMap<Level, Area>(levelComparator);
    // Compute union of the areas of pieces at ground level that are not lights, doors or windows
    for (Map.Entry<HomePieceOfFurniture, Node> object3DEntry : pieces3D.entrySet()) {
      if (object3DEntry.getKey() instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = object3DEntry.getKey();
        // This operation can be lengthy, so give up if thread is interrupted
        if (Thread.currentThread().isInterrupted()) {
          return;
        }
        if (piece.getElevation() == 0
            && !piece.isDoorOrWindow()
            && !(piece instanceof com.eteks.sweethome3d.model.Light)) {
          Area pieceAreaOnFloor = ModelManager.getInstance().getAreaOnFloor(object3DEntry.getValue());
          Level level = piece.getLevel();
          if (piece.getLevel() == null) {
            level = new Level("Dummy", 0, 0, 0);
          }
          if (level.isViewableAndVisible()) {
            Area areaOnLevel = areasOnLevel.get(level);
            if (areaOnLevel == null) {
              areaOnLevel = new Area();
              areasOnLevel.put(level, areaOnLevel);
            }
            areaOnLevel.add(pieceAreaOnFloor);
          }
        }
      }
    }

    // Create the 3D shape matching computed areas
    Shape3D shadow = new Shape3D();
    for (Map.Entry<Level, Area> levelArea : areasOnLevel.entrySet()) {
      List<Point3f> coords = new ArrayList<Point3f>();
      List<Integer> stripCounts = new ArrayList<Integer>();
      int pointsCount = 0;
      float [] modelPoint = new float[2];
      for (PathIterator it = levelArea.getValue().getPathIterator(null); !it.isDone(); ) {
        if (it.currentSegment(modelPoint) == PathIterator.SEG_CLOSE) {
          stripCounts.add(pointsCount);
          pointsCount = 0;
        } else {
          coords.add(new Point3f(modelPoint [0], levelArea.getKey().getElevation() + 0.49f, modelPoint [1]));
          pointsCount++;
        }
        it.next();
      }

      if (coords.size() > 0) {
        GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        geometryInfo.setCoordinates (coords.toArray(new Point3f [coords.size()]));
        int [] stripCountsArray = new int [stripCounts.size()];
        for (int i = 0; i < stripCountsArray.length; i++) {
          stripCountsArray [i] = stripCounts.get(i);
        }
        geometryInfo.setStripCounts(stripCountsArray);
        shadow.addGeometry(geometryInfo.getIndexedGeometryArray());
      }
    }

    Appearance shadowAppearance = new Appearance();
    shadowAppearance.setColoringAttributes(new ColoringAttributes(new Color3f(), ColoringAttributes.SHADE_FLAT));
    shadowAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.7f));
    shadow.setAppearance(shadowAppearance);
    homeRoot.addChild(shadow);
  }
}
