/*
 * Home.java 15 mai 2006
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The home managed by the application with its furniture and walls.
 * @author Emmanuel Puybaret
 */
public class Home implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  /**
   * The current version of this home. Each time the field list is changed
   * in <code>Home</code> class or in one of the classes that it uses,
   * this number is increased.
   */
  public static final long CURRENT_VERSION = 6000;

  private static final boolean KEEP_BACKWARD_COMPATIBLITY = true;

  private static final Comparator<Level> LEVEL_ELEVATION_COMPARATOR = new Comparator<Level>() {
      public int compare(Level level1, Level level2) {
        int elevationComparison = Float.compare(level1.getElevation(), level2.getElevation());
        if (elevationComparison != 0) {
          return elevationComparison;
        } else {
          return level1.getElevationIndex() - level2.getElevationIndex();
        }
      }
    };

  /**
   * The properties of a home that may change. <code>PropertyChangeListener</code>s added
   * to a home will be notified under a property name equal to the name value of one these properties.
   */
  public enum Property {NAME, MODIFIED,
    FURNITURE_SORTED_PROPERTY, FURNITURE_DESCENDING_SORTED, FURNITURE_VISIBLE_PROPERTIES,
    BACKGROUND_IMAGE, CAMERA, PRINT, BASE_PLAN_LOCKED, STORED_CAMERAS, RECOVERED, REPAIRED,
    SELECTED_LEVEL, ALL_LEVELS_SELECTION};

  private List<HomePieceOfFurniture>                  furniture;
  private transient CollectionChangeSupport<HomePieceOfFurniture> furnitureChangeSupport;
  private transient List<Selectable>                  selectedItems;
  private transient List<SelectionListener>           selectionListeners;
  private transient boolean                           allLevelsSelection;
  private List<Level>                                 levels;
  private Level                                       selectedLevel;
  private transient CollectionChangeSupport<Level>    levelsChangeSupport;
  private List<Wall>                                  walls;
  private transient CollectionChangeSupport<Wall>     wallsChangeSupport;
  private List<Room>                                  rooms;
  private transient CollectionChangeSupport<Room>     roomsChangeSupport;
  private List<Polyline>                              polylines;
  private transient CollectionChangeSupport<Polyline> polylinesChangeSupport;
  private List<DimensionLine>                         dimensionLines;
  private transient CollectionChangeSupport<DimensionLine> dimensionLinesChangeSupport;
  private List<Label>                                 labels;
  private transient CollectionChangeSupport<Label>    labelsChangeSupport;
  private Camera                                      camera;
  private String                                      name;
  private final float                                 wallHeight;
  private transient boolean                           modified;
  private transient boolean                           recovered;
  private transient boolean                           repaired;
  private BackgroundImage                             backgroundImage;
  private ObserverCamera                              observerCamera;
  private Camera                                      topCamera;
  private List<Camera>                                storedCameras;
  private HomeEnvironment                             environment;
  private HomePrint                                   print;
  private String                                      furnitureSortedPropertyName;
  private List<String>                                furnitureVisiblePropertyNames;
  private boolean                                     furnitureDescendingSorted;
  private Map<String, Object>                         visualProperties;
  private Map<String, String>                         properties;
  private transient PropertyChangeSupport             propertyChangeSupport;
  private long                                        version;
  private boolean                                     basePlanLocked;
  private Compass                                     compass;
  // The 5 following environment fields are still declared for compatibility reasons
  private int                                         skyColor;
  private int                                         groundColor;
  private HomeTexture                                 groundTexture;
  private int                                         lightColor;
  private float                                       wallsAlpha;
  // The two following fields aren't transient for backward compatibility reasons
  private HomePieceOfFurniture.SortableProperty       furnitureSortedProperty;
  private List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties;
  // The following field is a temporary copy of furniture containing HomeDoorOrWindow instances
  // created at serialization time for backward compatibility reasons
  private List<HomePieceOfFurniture>                  furnitureWithDoorsAndWindows;
  // The following field is a temporary copy of furniture containing HomeFurnitureGroup instances
  // created at serialization time for backward compatibility reasons
  private List<HomePieceOfFurniture>                  furnitureWithGroups;

  /**
   * Creates a home with no furniture, no walls,
   * and a height equal to 250 cm.
   */
  public Home() {
    this(250);
  }

  /**
   * Creates a home with no furniture and no walls.
   * @param wallHeight default height for home walls
   */
  public Home(float wallHeight) {
    this(new ArrayList<HomePieceOfFurniture>(), wallHeight);
  }

  /**
   * Creates a home with the given <code>furniture</code>,
   * no walls and a height equal to 250 cm.
   */
  public Home(List<HomePieceOfFurniture> furniture) {
    this(furniture, 250);
  }

  private Home(List<HomePieceOfFurniture> furniture, float wallHeight) {
    this.furniture = new ArrayList<HomePieceOfFurniture>(furniture);
    this.walls = new ArrayList<Wall>();
    this.wallHeight = wallHeight;
    this.furnitureVisibleProperties = Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
        HomePieceOfFurniture.SortableProperty.NAME,
        HomePieceOfFurniture.SortableProperty.WIDTH,
        HomePieceOfFurniture.SortableProperty.DEPTH,
        HomePieceOfFurniture.SortableProperty.HEIGHT,
        HomePieceOfFurniture.SortableProperty.VISIBLE});
    // Init transient lists and other fields
    init(true);
    addModelListeners();
  }

  /**
   * Creates a home from an other one. All mutable data of the source <code>home</code>
   * is cloned to this home and listeners support is reset.
   * @since 5.0
   */
  protected Home(Home home) {
    this.wallHeight = home.getWallHeight();
    copyHomeData(home, this);
    initListenersSupport(this);
    addModelListeners();
  }

  /**
   * Initializes new and transient home fields to their default values
   * and reads home from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    init(false);
    in.defaultReadObject();

    if (KEEP_BACKWARD_COMPATIBLITY) {
      // Restore furnitureSortedProperty from furnitureSortedPropertyName
      if (this.furnitureSortedPropertyName != null) {
        try {
          this.furnitureSortedProperty =
              HomePieceOfFurniture.SortableProperty.valueOf(this.furnitureSortedPropertyName);
        } catch (IllegalArgumentException ex) {
          // Ignore malformed enum constant
        }
        this.furnitureSortedPropertyName = null;
      }
      // Restore furnitureVisibleProperties from furnitureVisiblePropertyNames
      if (this.furnitureVisiblePropertyNames != null) {
        this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>();
        for (String furnitureVisiblePropertyName : this.furnitureVisiblePropertyNames) {
          try {
            this.furnitureVisibleProperties.add(
                HomePieceOfFurniture.SortableProperty.valueOf(furnitureVisiblePropertyName));
          } catch (IllegalArgumentException ex) {
            // Ignore malformed enum constants
          }
        }
        this.furnitureVisiblePropertyNames = null;
      }

      // Ensure all wall have an height
      for (Wall wall : this.walls) {
        if (wall.getHeight() == null) {
          wall.setHeight(this.wallHeight);
        }
      }

      // Restore referenced HomeDoorOrWindow instances stored in a separate field
      // for backward compatibility reasons
      if (this.furnitureWithDoorsAndWindows != null) {
        this.furniture = this.furnitureWithDoorsAndWindows;
        this.furnitureWithDoorsAndWindows = null;
      }

      // Restore referenced HomeFurnitureGroup instances stored in a separate field
      // for backward compatibility reasons
      if (this.furnitureWithGroups != null) {
        this.furniture = this.furnitureWithGroups;
        this.furnitureWithGroups = null;
      }

      // Restore environment fields from home fields for compatibility reasons
      this.environment.setGroundColor(this.groundColor);
      this.environment.setGroundTexture(this.groundTexture);
      this.environment.setSkyColor(this.skyColor);
      this.environment.setLightColor(this.lightColor);
      this.environment.setWallsAlpha(this.wallsAlpha);

      if (this.version <= 3400) {
        // Automatically adjust ground color to a darker color
        int groundColor = this.environment.getGroundColor();
        this.environment.setGroundColor(
              ((((groundColor >> 16) & 0xFF) * 3 / 4) << 16)
            | ((((groundColor >> 8) & 0xFF) * 3 / 4) << 8)
            | ((groundColor & 0xFF) * 3 / 4));
      }

      // Assign level elevation index from current order if index is equal to -1
      if (this.levels.size() > 0) {
        Level previousLevel = this.levels.get(0);
        if (previousLevel.getElevationIndex() == -1) {
          previousLevel.setElevationIndex(0);
        }
        for (int i = 1; i < this.levels.size(); i++) {
          Level level = this.levels.get(i);
          if (level.getElevationIndex() == -1) {
            if (previousLevel.getElevation() == level.getElevation()) {
              level.setElevationIndex(previousLevel.getElevationIndex() + 1);
            } else {
              level.setElevationIndex(0);
            }
          }
          previousLevel = level;
        }
      }

      // Move known visual properties to string properties
      moveVisualProperty("com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogX");
      moveVisualProperty("com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogY");
      moveVisualProperty("com.eteks.sweethome3d.swing.PhotosPanel.PhotoDialogX");
      moveVisualProperty("com.eteks.sweethome3d.swing.PhotosPanel.PhotoDialogY");
      moveVisualProperty("com.eteks.sweethome3d.swing.VideoPanel.VideoDialogX");
      moveVisualProperty("com.eteks.sweethome3d.swing.VideoPanel.VideoDialogY");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedViewX");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedViewY");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedViewWidth");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedViewHeight");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedView");
      moveVisualProperty("com.eteks.sweethome3d.swing.HomeComponent3D.detachedViewDividerLocation");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.MainPaneDividerLocation");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.CatalogPaneDividerLocation");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.PlanPaneDividerLocation");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.PlanViewportX");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.PlanViewportY");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FurnitureViewportY");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.PlanScale");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.ExpandedGroups");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FrameX");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FrameY");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FrameWidth");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FrameHeight");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.FrameMaximized");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.ScreenWidth");
      moveVisualProperty("com.eteks.sweethome3d.SweetHome3D.ScreenHeight");
    }

    addModelListeners();
  }

  private void moveVisualProperty(String visualPropertyName) {
    if (this.visualProperties.containsKey(visualPropertyName)) {
      Object value = this.visualProperties.get(visualPropertyName);
      this.properties.put(visualPropertyName, value != null  ? String.valueOf(value)  : null);
      this.visualProperties.remove(visualPropertyName);
    }
  }

  private void init(boolean newHome) {
    // Initialize transient lists
    this.selectedItems = new ArrayList<Selectable>();
    initListenersSupport(this);

    if (this.furnitureVisibleProperties == null) {
      // Set the furniture properties that were visible before version 0.19
      this.furnitureVisibleProperties = Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
          HomePieceOfFurniture.SortableProperty.NAME,
          HomePieceOfFurniture.SortableProperty.WIDTH,
          HomePieceOfFurniture.SortableProperty.DEPTH,
          HomePieceOfFurniture.SortableProperty.HEIGHT,
          HomePieceOfFurniture.SortableProperty.COLOR,
          HomePieceOfFurniture.SortableProperty.MOVABLE,
          HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW,
          HomePieceOfFurniture.SortableProperty.VISIBLE});
    }
    // Create a default top camera that matches default point of view
    this.topCamera = new Camera(50, 1050, 1010,
        (float)Math.PI, (float)Math.PI / 4, (float)Math.PI * 63 / 180);
    // Create a default observer camera (use a 63� field of view equivalent to a 35mm lens for a 24x36 film)
    this.observerCamera = new ObserverCamera(50, 50, 170,
        7 * (float)Math.PI / 4, (float)Math.PI / 16, (float)Math.PI * 63 / 180);
    this.storedCameras = Collections.emptyList();
    // Initialize new fields
    this.environment = new HomeEnvironment();
    this.rooms = new ArrayList<Room>();
    this.polylines = new ArrayList<Polyline>();
    this.dimensionLines = new ArrayList<DimensionLine>();
    this.labels = new ArrayList<Label>();
    this.compass = new Compass(-100, 50, 100);
    this.levels = new ArrayList<Level>();
    // Let compass be visible only on new homes
    this.compass.setVisible(newHome);
    this.visualProperties = new HashMap<String, Object>();
    this.properties = new HashMap<String, String>();

    this.version = CURRENT_VERSION;
  }

  private static void initListenersSupport(Home home) {
    home.furnitureChangeSupport = new CollectionChangeSupport<HomePieceOfFurniture>(home);
    home.selectionListeners = new ArrayList<SelectionListener>();
    home.levelsChangeSupport = new CollectionChangeSupport<Level>(home);
    home.wallsChangeSupport = new CollectionChangeSupport<Wall>(home);
    home.roomsChangeSupport = new CollectionChangeSupport<Room>(home);
    home.polylinesChangeSupport = new CollectionChangeSupport<Polyline>(home);
    home.dimensionLinesChangeSupport = new CollectionChangeSupport<DimensionLine>(home);
    home.labelsChangeSupport = new CollectionChangeSupport<Label>(home);
    home.propertyChangeSupport = new PropertyChangeSupport(home);
  }

  /**
   * Adds listeners to model.
   */
  private void addModelListeners() {
    // Add listeners to levels to maintain its elevation order
    final PropertyChangeListener levelElevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
              || Level.Property.ELEVATION_INDEX.name().equals(ev.getPropertyName())) {
            Home.this.levels = new ArrayList<Level>(Home.this.levels);
            Collections.sort(Home.this.levels, LEVEL_ELEVATION_COMPARATOR);
          }
        }
      };
    for (Level level : this.levels) {
      level.addPropertyChangeListener(levelElevationChangeListener);
    }
    addLevelsListener(new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          switch (ev.getType()) {
            case ADD :
              ev.getItem().addPropertyChangeListener(levelElevationChangeListener);
              break;
            case DELETE :
              ev.getItem().removePropertyChangeListener(levelElevationChangeListener);
              break;
          }
        }
      });
  }

  /**
   * Sets the version of this home and writes it to <code>out</code> stream
   * with default writing method.
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    this.version = CURRENT_VERSION;

    if (KEEP_BACKWARD_COMPATIBLITY) {
      HomePieceOfFurniture.SortableProperty homeFurnitureSortedProperty = this.furnitureSortedProperty;
      List<HomePieceOfFurniture.SortableProperty> homeFurnitureVisibleProperties = this.furnitureVisibleProperties;
      List<HomePieceOfFurniture> homeFurniture = this.furniture;
      try {
        if (this.furnitureSortedProperty != null) {
          this.furnitureSortedPropertyName = this.furnitureSortedProperty.name();
          // Store in furnitureSortedProperty only backward compatible property
          if (!isFurnitureSortedPropertyBackwardCompatible(this.furnitureSortedProperty)) {
            this.furnitureSortedProperty = null;
          }
        }

        this.furnitureVisiblePropertyNames = new ArrayList<String>();
        // Store in furnitureVisibleProperties only backward compatible properties
        this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>();
        for (HomePieceOfFurniture.SortableProperty visibleProperty : homeFurnitureVisibleProperties) {
          this.furnitureVisiblePropertyNames.add(visibleProperty.name());
          if (isFurnitureSortedPropertyBackwardCompatible(visibleProperty)) {
            this.furnitureVisibleProperties.add(visibleProperty);
          }
        }

        // Store referenced HomeFurnitureGroup instances in a separate field
        // for backward compatibility reasons (version < 2.3)
        this.furnitureWithGroups = this.furniture;
        // Serialize a furnitureWithDoorsAndWindows field that contains only
        // HomePieceOfFurniture, HomeDoorOrWindow and HomeLight instances
        // for backward compatibility reasons (version < 1.7)
        this.furnitureWithDoorsAndWindows = new ArrayList<HomePieceOfFurniture>(this.furniture.size());
        // Serialize a furniture field that contains only HomePieceOfFurniture instances
        this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture.size());
        for (HomePieceOfFurniture piece : this.furnitureWithGroups) {
          if (piece.getClass() == HomePieceOfFurniture.class) {
            this.furnitureWithDoorsAndWindows.add(piece);
            this.furniture.add(piece);
          } else {
            if (piece.getClass() == HomeFurnitureGroup.class) {
              // Add the ungrouped pieces to furniture and furnitureWithDoorsAndWindows list
              for (HomePieceOfFurniture groupPiece : getGroupFurniture((HomeFurnitureGroup)piece)) {
                this.furnitureWithDoorsAndWindows.add(groupPiece);
                if (groupPiece.getClass() == HomePieceOfFurniture.class) {
                  this.furniture.add(groupPiece);
                } else {
                  // Create backward compatible instances
                  this.furniture.add(new HomePieceOfFurniture(groupPiece));
                }
              }
            } else {
              this.furnitureWithDoorsAndWindows.add(piece);
              // Create backward compatible instances
              this.furniture.add(new HomePieceOfFurniture(piece));
            }
          }
        }

        // Store environment fields in home fields for compatibility reasons
        this.groundColor = this.environment.getGroundColor();
        this.groundTexture = this.environment.getGroundTexture();
        this.skyColor = this.environment.getSkyColor();
        this.lightColor = this.environment.getLightColor();
        this.wallsAlpha = this.environment.getWallsAlpha();

        out.defaultWriteObject();
      } finally {
        // Restore home values
        this.furniture = homeFurniture;
        this.furnitureWithDoorsAndWindows = null;
        this.furnitureWithGroups = null;

        this.furnitureSortedProperty = homeFurnitureSortedProperty;
        this.furnitureVisibleProperties = homeFurnitureVisibleProperties;
        // Set furnitureSortedPropertyName and furnitureVisiblePropertyNames to null
        // (they are used only for serialization)
        this.furnitureSortedPropertyName = null;
        this.furnitureVisiblePropertyNames = null;
      }
    } else {
      out.defaultWriteObject();
    }
  }

  /**
   * Returns <code>true</code> if the given <code>property</code> is compatible
   * with the first set of sortable properties that existed in <code>HomePieceOfFurniture</code> class.
   */
  private boolean isFurnitureSortedPropertyBackwardCompatible(HomePieceOfFurniture.SortableProperty property) {
    switch (property) {
      case NAME :
      case WIDTH :
      case DEPTH :
      case HEIGHT :
      case MOVABLE :
      case DOOR_OR_WINDOW :
      case COLOR :
      case VISIBLE :
      case X :
      case Y :
      case ELEVATION :
      case ANGLE :
        return true;
      default :
        return false;
    }
  }

  /**
   * Returns all the pieces of the given <code>furnitureGroup</code>.
   */
  private List<HomePieceOfFurniture> getGroupFurniture(HomeFurnitureGroup furnitureGroup) {
    List<HomePieceOfFurniture> groupFurniture = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furnitureGroup.getFurniture()) {
      if (piece instanceof HomeFurnitureGroup) {
        groupFurniture.addAll(getGroupFurniture((HomeFurnitureGroup)piece));
      } else {
        groupFurniture.add(piece);
      }
    }
    return groupFurniture;
  }

  /**
   * Adds the level <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   * @since 3.4
   */
  public void addLevelsListener(CollectionListener<Level> listener) {
    this.levelsChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the level <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   * @since 3.4
   */
  public void removeLevelsListener(CollectionListener<Level> listener) {
    this.levelsChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the levels of this home.
   * @since 3.4
   */
  public List<Level> getLevels() {
    return Collections.unmodifiableList(this.levels);
  }

  /**
   * Adds the given <code>level</code> to the list of levels of this home.
   * Once the <code>level</code> is added, level listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param level  the level to add
   * @since 3.4
   */
  public void addLevel(Level level) {
    if (level.getElevationIndex() < 0) {
      // Search elevation index of the added level
      int elevationIndex = 0;
      for (Level homeLevel : this.levels) {
        if (homeLevel.getElevation() == level.getElevation()) {
          elevationIndex = homeLevel.getElevationIndex() + 1;
        } else if (homeLevel.getElevation() > level.getElevation()) {
          break;
        }
      }
      level.setElevationIndex(elevationIndex);
    }
    // Make a copy of the list to avoid conflicts in the list returned by getLevels
    this.levels = new ArrayList<Level>(this.levels);
    // Search at which index should be inserted the new level
    int index = Collections.binarySearch(this.levels, level, LEVEL_ELEVATION_COMPARATOR);
    int levelIndex;
    if (index >= 0) {
      levelIndex = index;
    } else {
      levelIndex = -(index + 1);
    }
    this.levels.add(levelIndex, level);
    this.levelsChangeSupport.fireCollectionChanged(level, levelIndex, CollectionEvent.Type.ADD);
  }

  /**
   * Removes the given <code>level</code> from the set of levels of this home
   * and all the furniture, walls, rooms, dimension lines and labels that belong to this level.
   * Once the <code>level</code> is removed, level listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * @param level  the level to remove
   * @since 3.4
   */
  public void deleteLevel(Level level) {
    int index = this.levels.indexOf(level);
    if (index != -1) {
      for (HomePieceOfFurniture piece : this.furniture) {
        if (piece.getLevel() == level) {
          deletePieceOfFurniture(piece);
        }
      }
      for (Room room : this.rooms) {
        if (room.getLevel() == level) {
          deleteRoom(room);
        }
      }
      for (Wall wall : this.walls) {
        if (wall.getLevel() == level) {
          deleteWall(wall);
        }
      }
      for (Polyline polyline : this.polylines) {
        if (polyline.getLevel() == level) {
          deletePolyline(polyline);
        }
      }
      for (DimensionLine dimensionLine : this.dimensionLines) {
        if (dimensionLine.getLevel() == level) {
          deleteDimensionLine(dimensionLine);
        }
      }
      for (Label label : this.labels) {
        if (label.getLevel() == level) {
          deleteLabel(label);
        }
      }
      if (this.selectedLevel == level) {
        if (this.levels.size() == 1) {
          setSelectedLevel(null);
          setAllLevelsSelection(false);
        } else {
          setSelectedLevel(this.levels.get(index >= 1 ? index - 1 : index + 1));
        }
      }
      // Make a copy of the list to avoid conflicts in the list returned by getLevels
      this.levels = new ArrayList<Level>(this.levels);
      this.levels.remove(index);
      this.levelsChangeSupport.fireCollectionChanged(level, index, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Returns the selected level in home or <code>null</code> if home has no level.
   * @since 3.4
   */
  public Level getSelectedLevel() {
    return this.selectedLevel;
  }

  /**
   * Sets the selected level in home and notifies listeners of the change.
   * @param selectedLevel  the level to select
   * @since 3.4
   */
  public void setSelectedLevel(Level selectedLevel) {
    if (selectedLevel != this.selectedLevel) {
      Level oldSelectedLevel = this.selectedLevel;
      this.selectedLevel = selectedLevel;
      this.propertyChangeSupport.firePropertyChange(Property.SELECTED_LEVEL.name(), oldSelectedLevel, selectedLevel);
    }
  }

  /**
   * Returns <code>true</code> if the selected items in this home are from all levels.
   * @since 4.4
   */
  public boolean isAllLevelsSelection() {
    return this.allLevelsSelection;
  }

  /**
   * Sets whether the selected items in this home are from all levels, and notifies listeners of the change.
   * @since 4.4
   */
  public void setAllLevelsSelection(boolean selectionAtAllLevels) {
    if (selectionAtAllLevels != this.allLevelsSelection) {
      this.allLevelsSelection = selectionAtAllLevels;
      this.propertyChangeSupport.firePropertyChange(Property.ALL_LEVELS_SELECTION.name(), !selectionAtAllLevels, selectionAtAllLevels);
    }
  }

  /**
   * Adds the furniture <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
    this.furnitureChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
    this.furnitureChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable list of the furniture managed by this home.
   * This furniture in this list is always sorted in the index order they were added to home.
   */
  public List<HomePieceOfFurniture> getFurniture() {
    return Collections.unmodifiableList(this.furniture);
  }

  /**
   * Adds the <code>piece</code> in parameter to this home at the end of the furniture list.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   * @param piece  the piece to add
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece) {
    addPieceOfFurniture(piece, this.furniture.size());
  }

  /**
   * Adds the <code>piece</code> in parameter at a given <code>index</code>.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   * @param piece  the piece to add
   * @param index  the index at which the piece will be added
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getFurniture
    this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture);
    piece.setLevel(this.selectedLevel);
    this.furniture.add(index, piece);
    this.furnitureChangeSupport.fireCollectionChanged(piece, index, CollectionEvent.Type.ADD);
  }

  /**
   * Adds the <code>piece</code> in parameter at the <code>index</code> in the given <code>group</code>.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification with an event {@link CollectionEvent#getIndex() index} equal to -1.
   * @param piece  the piece to add
   * @param group  the group to which the piece will be added
   * @param index  the index at which the piece will be added
   */
  public void addPieceOfFurnitureToGroup(HomePieceOfFurniture piece, HomeFurnitureGroup group, int index) {
    piece.setLevel(this.selectedLevel);
    group.addPieceOfFurniture(piece, index);
    this.furnitureChangeSupport.fireCollectionChanged(piece, CollectionEvent.Type.ADD);
  }

  /**
   * Deletes the <code>piece</code> in parameter from this home.
   * Once the <code>piece</code> is deleted, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification. If the removed <code>piece</code> belongs to a group, the
   * {@link CollectionEvent#getIndex() index} of the event will be -1.
   * @param piece  the piece to remove
   */
  public void deletePieceOfFurniture(HomePieceOfFurniture piece) {
    // Ensure selectedItems don't keep a reference to piece
    deselectItem(piece);
    int index = this.furniture.indexOf(piece);
    HomeFurnitureGroup group = index == -1
        ? getPieceOfFurnitureGroup(piece, null, this.furniture)
        : null;
    if (index != -1
        || group != null) {
      piece.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getFurniture
      this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture);
      if (group != null) {
        group.deletePieceOfFurniture(piece);
        this.furnitureChangeSupport.fireCollectionChanged(piece, CollectionEvent.Type.DELETE);
      } else {
        this.furniture.remove(index);
        this.furnitureChangeSupport.fireCollectionChanged(piece, index, CollectionEvent.Type.DELETE);
      }
    }
  }

  /**
   * Returns the furniture group that contains the given <code>piece</code> or <code>null</code>
   * if it can't be found.
   */
  private HomeFurnitureGroup getPieceOfFurnitureGroup(HomePieceOfFurniture piece,
                                                      HomeFurnitureGroup furnitureGroup,
                                                      List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture homePiece : furniture) {
      if (homePiece.equals(piece)) {
        return furnitureGroup;
      } else if (homePiece instanceof HomeFurnitureGroup) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece,
            (HomeFurnitureGroup)homePiece, ((HomeFurnitureGroup)homePiece).getFurniture());
        if (group != null) {
          return group;
        }
      }
    }
    return null;
  }

  /**
   * Adds the selection <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addSelectionListener(SelectionListener listener) {
    this.selectionListeners.add(listener);
  }

  /**
   * Removes the selection <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeSelectionListener(SelectionListener listener) {
    this.selectionListeners.remove(listener);
  }

  /**
   * Returns an unmodifiable list of the selected items in home.
   */
  public List<Selectable> getSelectedItems() {
    return Collections.unmodifiableList(this.selectedItems);
  }

  /**
   * Sets the selected items in home and notifies listeners selection change.
   * @param selectedItems the list of selected items
   */
  public void setSelectedItems(List<? extends Selectable> selectedItems) {
    // Make a copy of the list to avoid conflicts in the list returned by getSelectedItems
    this.selectedItems = new ArrayList<Selectable>(selectedItems);
    if (!this.selectionListeners.isEmpty()) {
      SelectionEvent selectionEvent = new SelectionEvent(this, getSelectedItems());
      // Work on a copy of selectionListeners to ensure a listener
      // can modify safely listeners list
      SelectionListener [] listeners = this.selectionListeners.
        toArray(new SelectionListener [this.selectionListeners.size()]);
      for (SelectionListener listener : listeners) {
        listener.selectionChanged(selectionEvent);
      }
    }
  }

  /**
   * Deselects <code>item</code> if it's selected and notifies listeners selection change.
   * @param item  the item to remove from selected items
   * @since 2.2
   */
  public void deselectItem(Selectable item) {
    int pieceSelectionIndex = this.selectedItems.indexOf(item);
    if (pieceSelectionIndex != -1) {
      List<Selectable> selectedItems = new ArrayList<Selectable>(getSelectedItems());
      selectedItems.remove(pieceSelectionIndex);
      setSelectedItems(selectedItems);
    }
  }

  /**
   * Adds the room <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addRoomsListener(CollectionListener<Room> listener) {
    this.roomsChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the room <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeRoomsListener(CollectionListener<Room> listener) {
    this.roomsChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the rooms of this home.
   */
  public List<Room> getRooms() {
    return Collections.unmodifiableList(this.rooms);
  }

  /**
   * Adds the given <code>room</code> at the end of the rooms list of this home.
   * Once the <code>room</code> is added, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param room   the room to add
   */
  public void addRoom(Room room) {
    addRoom(room, this.rooms.size());
  }

  /**
   * Adds the <code>room</code> in parameter at a given <code>index</code>.
   * Once the <code>room</code> is added, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param room   the room to add
   * @param index  the index at which the room will be added
   */
  public void addRoom(Room room, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getRooms
    this.rooms = new ArrayList<Room>(this.rooms);
    this.rooms.add(index, room);
    room.setLevel(this.selectedLevel);
    this.roomsChangeSupport.fireCollectionChanged(room, index, CollectionEvent.Type.ADD);
  }

  /**
   * Removes the given <code>room</code> from the set of rooms of this home.
   * Once the <code>room</code> is removed, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * @param room  the room to remove
   */
  public void deleteRoom(Room room) {
    //  Ensure selectedItems don't keep a reference to room
    deselectItem(room);
    int index = this.rooms.indexOf(room);
    if (index != -1) {
      room.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getRooms
      this.rooms = new ArrayList<Room>(this.rooms);
      this.rooms.remove(index);
      this.roomsChangeSupport.fireCollectionChanged(room, index, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Adds the wall <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addWallsListener(CollectionListener<Wall> listener) {
    this.wallsChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the wall <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeWallsListener(CollectionListener<Wall> listener) {
    this.wallsChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the walls of this home.
   */
  public Collection<Wall> getWalls() {
    return Collections.unmodifiableCollection(this.walls);
  }

  /**
   * Adds the given <code>wall</code> to the set of walls of this home.
   * Once the <code>wall</code> is added, wall listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param wall  the wall to add
   */
  public void addWall(Wall wall) {
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.add(wall);
    wall.setLevel(this.selectedLevel);
    this.wallsChangeSupport.fireCollectionChanged(wall, CollectionEvent.Type.ADD);
  }

  /**
   * Removes the given <code>wall</code> from the set of walls of this home.
   * Once the <code>wall</code> is removed, wall listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * If any wall is attached to <code>wall</code> they will be detached from it.
   * @param wall  the wall to remove
   */
  public void deleteWall(Wall wall) {
    //  Ensure selectedItems don't keep a reference to wall
    deselectItem(wall);
    // Detach any other wall attached to wall
    for (Wall otherWall : getWalls()) {
      if (wall.equals(otherWall.getWallAtStart())) {
        otherWall.setWallAtStart(null);
      } else if (wall.equals(otherWall.getWallAtEnd())) {
        otherWall.setWallAtEnd(null);
      }
    }
    int index = this.walls.indexOf(wall);
    if (index != -1) {
      wall.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getWalls
      this.walls = new ArrayList<Wall>(this.walls);
      this.walls.remove(index);
      this.wallsChangeSupport.fireCollectionChanged(wall, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Adds the polyline <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   * @since 5.0
   */
  public void addPolylinesListener(CollectionListener<Polyline> listener) {
    this.polylinesChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the polyline <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   * @since 5.0
   */
  public void removePolylinesListener(CollectionListener<Polyline> listener) {
    this.polylinesChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the polylines of this home.
   * @since 5.0
   */
  public List<Polyline> getPolylines() {
    return Collections.unmodifiableList(this.polylines);
  }

  /**
   * Adds a given <code>polyline</code> at the end of the polylines list of this home.
   * Once the <code>polyline</code> is added, polyline listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param polyline  the polyline to add
   * @since 5.0
   */
  public void addPolyline(Polyline polyline) {
    addPolyline(polyline, this.polylines.size());
  }

  /**
   * Adds a <code>polyline</code> at a given <code>index</code> of the set of polylines of this home.
   * Once the <code>polyline</code> is added, polyline listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param polyline  the polyline to add
   * @param index  the index at which the polyline will be added
   * @since 5.0
   */
  public void addPolyline(Polyline polyline, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getPolylines
    this.polylines = new ArrayList<Polyline>(this.polylines);
    this.polylines.add(index, polyline);
    polyline.setLevel(this.selectedLevel);
    this.polylinesChangeSupport.fireCollectionChanged(polyline, CollectionEvent.Type.ADD);
  }

  /**
   * Removes a given <code>polyline</code> from the set of polylines of this home.
   * Once the <code>polyline</code> is removed, polyline listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * @param polyline  the polyline to remove
   * @since 5.0
   */
  public void deletePolyline(Polyline polyline) {
    //  Ensure selectedItems don't keep a reference to polyline
    deselectItem(polyline);
    int index = this.polylines.indexOf(polyline);
    if (index != -1) {
      polyline.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getPolylines
      this.polylines = new ArrayList<Polyline>(this.polylines);
      this.polylines.remove(index);
      this.polylinesChangeSupport.fireCollectionChanged(polyline, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Adds the dimension line <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addDimensionLinesListener(CollectionListener<DimensionLine> listener) {
    this.dimensionLinesChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the dimension line <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeDimensionLinesListener(CollectionListener<DimensionLine> listener) {
    this.dimensionLinesChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the dimension lines of this home.
   */
  public Collection<DimensionLine> getDimensionLines() {
    return Collections.unmodifiableCollection(this.dimensionLines);
  }

  /**
   * Adds the given dimension line to the set of dimension lines of this home.
   * Once <code>dimensionLine</code> is added, dimension line listeners added
   * to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param dimensionLine  the dimension line to add
   */
  public void addDimensionLine(DimensionLine dimensionLine) {
    // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
    this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
    this.dimensionLines.add(dimensionLine);
    dimensionLine.setLevel(this.selectedLevel);
    this.dimensionLinesChangeSupport.fireCollectionChanged(dimensionLine, CollectionEvent.Type.ADD);
  }

  /**
   * Removes the given dimension line from the set of dimension lines of this home.
   * Once <code>dimensionLine</code> is removed, dimension line listeners added
   * to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * @param dimensionLine  the dimension line to remove
   */
  public void deleteDimensionLine(DimensionLine dimensionLine) {
    //  Ensure selectedItems don't keep a reference to dimension line
    deselectItem(dimensionLine);
    int index = this.dimensionLines.indexOf(dimensionLine);
    if (index != -1) {
      dimensionLine.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
      this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
      this.dimensionLines.remove(index);
      this.dimensionLinesChangeSupport.fireCollectionChanged(dimensionLine, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Adds the label <code>listener</code> in parameter to this home.
   * @param listener the listener to add
   */
  public void addLabelsListener(CollectionListener<Label> listener) {
    this.labelsChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the label <code>listener</code> in parameter from this home.
   * @param listener the listener to remove
   */
  public void removeLabelsListener(CollectionListener<Label> listener) {
    this.labelsChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Returns an unmodifiable collection of the labels of this home.
   */
  public Collection<Label> getLabels() {
    return Collections.unmodifiableCollection(this.labels);
  }

  /**
   * Adds the given label to the set of labels of this home.
   * Once <code>label</code> is added, label listeners added
   * to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   * @param label  the label to add
   */
  public void addLabel(Label label) {
    // Make a copy of the list to avoid conflicts in the list returned by getLabels
    this.labels = new ArrayList<Label>(this.labels);
    this.labels.add(label);
    label.setLevel(this.selectedLevel);
    this.labelsChangeSupport.fireCollectionChanged(label, CollectionEvent.Type.ADD);
  }

  /**
   * Removes the given label from the set of labels of this home.
   * Once <code>label</code> is removed, label listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type}
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * @param label  the label to remove
   */
  public void deleteLabel(Label label) {
    //  Ensure selectedItems don't keep a reference to label
    deselectItem(label);
    int index = this.labels.indexOf(label);
    if (index != -1) {
      label.setLevel(null);
      // Make a copy of the list to avoid conflicts in the list returned by getLabels
      this.labels = new ArrayList<Label>(this.labels);
      this.labels.remove(index);
      this.labelsChangeSupport.fireCollectionChanged(label, CollectionEvent.Type.DELETE);
    }
  }

  /**
   * Returns all the selectable and viewable items in this home, except the observer camera.
   * @return a list containing viewable walls, furniture, dimension lines, labels and compass.
   * @since 5.0
   */
  public List<Selectable> getSelectableViewableItems() {
    List<Selectable> homeItems = new ArrayList<Selectable>();
    addViewableItems(this.walls, homeItems);
    addViewableItems(this.rooms, homeItems);
    addViewableItems(this.dimensionLines, homeItems);
    addViewableItems(this.polylines, homeItems);
    addViewableItems(this.labels, homeItems);
    for (HomePieceOfFurniture piece : getFurniture()) {
      if (piece.isVisible()
          && (piece.getLevel() == null
              || piece.getLevel().isViewable())) {
        homeItems.add(piece);
      }
    }
    if (getCompass().isVisible()) {
      homeItems.add(getCompass());
    }
    return homeItems;
  }

  /**
   * Adds the viewable items to the set of selectable viewable items.
   */
  private <T extends Selectable> void addViewableItems(Collection<T> items,
                                                       List<Selectable> selectableViewableItems) {
    for (T item : items) {
      if (item instanceof Elevatable) {
        Elevatable elevatableItem = (Elevatable)item;
        if (elevatableItem.getLevel() == null
            || elevatableItem.getLevel().isViewable()) {
          selectableViewableItems.add(item);
        }
      }
    }
  }

  /**
   * Returns <code>true</code> if this home doesn't contain any item i.e.
   * no piece of furniture, no wall, no room, no dimension line and no label.
   * @since 2.2
   */
  public boolean isEmpty() {
    return this.furniture.isEmpty()
        && this.walls.isEmpty()
        && this.rooms.isEmpty()
        && this.dimensionLines.isEmpty()
        && this.polylines.isEmpty()
        && this.labels.isEmpty();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   * @param property the property to follow
   * @param listener the listener to add
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   * @param property the followed property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Returns the wall height of this home.
   */
  public float getWallHeight() {
    return this.wallHeight;
  }

  /**
   * Returns the name of this home.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this home and fires a <code>PropertyChangeEvent</code>.
   * @param name  the new name of this home
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
   * Returns whether the state of this home is modified or not.
   */
  public boolean isModified() {
    return this.modified;
  }

  /**
   * Sets the modified state of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setModified(boolean modified) {
    if (modified != this.modified) {
      this.modified = modified;
      this.propertyChangeSupport.firePropertyChange(
          Property.MODIFIED.name(), !modified, modified);
    }
  }

  /**
   * Returns whether this home was recovered or not.
   * @since 3.0
   */
  public boolean isRecovered() {
    return this.recovered;
  }

  /**
   * Sets whether this home was recovered or not and fires a <code>PropertyChangeEvent</code>.
   * @since 3.0
   */
  public void setRecovered(boolean recovered) {
    if (recovered != this.recovered) {
      this.recovered = recovered;
      this.propertyChangeSupport.firePropertyChange(
          Property.RECOVERED.name(), !recovered, recovered);
    }
  }

  /**
   * Returns whether this home was repaired or not.
   * @since 4.4
   */
  public boolean isRepaired() {
    return this.repaired;
  }

  /**
   * Sets whether this home is repaired or not and fires a <code>PropertyChangeEvent</code>.
   * @since 4.4
   */
  public void setRepaired(boolean repaired) {
    if (repaired != this.repaired) {
      this.repaired = repaired;
      this.propertyChangeSupport.firePropertyChange(
          Property.REPAIRED.name(), !repaired, repaired);
    }
  }

  /**
   * Returns the furniture property on which home is sorted or <code>null</code> if
   * home furniture isn't sorted.
   */
  public HomePieceOfFurniture.SortableProperty getFurnitureSortedProperty() {
    return this.furnitureSortedProperty;
  }

  /**
   * Sets the furniture property on which this home should be sorted
   * and fires a <code>PropertyChangeEvent</code>.
   * @param furnitureSortedProperty the new property
   */
  public void setFurnitureSortedProperty(HomePieceOfFurniture.SortableProperty furnitureSortedProperty) {
    if (furnitureSortedProperty != this.furnitureSortedProperty
        && (furnitureSortedProperty == null || !furnitureSortedProperty.equals(this.furnitureSortedProperty))) {
      HomePieceOfFurniture.SortableProperty oldFurnitureSortedProperty = this.furnitureSortedProperty;
      this.furnitureSortedProperty = furnitureSortedProperty;
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_SORTED_PROPERTY.name(),
          oldFurnitureSortedProperty, furnitureSortedProperty);
    }
  }

  /**
   * Returns whether furniture is sorted in ascending or descending order.
   */
  public boolean isFurnitureDescendingSorted() {
    return this.furnitureDescendingSorted;
  }

  /**
   * Sets the furniture sort order on which home should be sorted
   * and fires a <code>PropertyChangeEvent</code>.
   */
  public void setFurnitureDescendingSorted(boolean furnitureDescendingSorted) {
    if (furnitureDescendingSorted != this.furnitureDescendingSorted) {
      this.furnitureDescendingSorted = furnitureDescendingSorted;
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_DESCENDING_SORTED.name(),
          !furnitureDescendingSorted, furnitureDescendingSorted);
    }
  }

  /**
   * Returns an unmodifiable list of the furniture properties that are visible.
   */
  public List<HomePieceOfFurniture.SortableProperty> getFurnitureVisibleProperties() {
    if (this.furnitureVisibleProperties == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(this.furnitureVisibleProperties);
    }
  }

  /**
   * Sets the furniture properties that are visible and the order in which they are visible,
   * then fires a <code>PropertyChangeEvent</code>.
   * @param furnitureVisibleProperties  the properties to display
   */
  public void setFurnitureVisibleProperties(List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties) {
    if (furnitureVisibleProperties != this.furnitureVisibleProperties
        && (furnitureVisibleProperties == null || !furnitureVisibleProperties.equals(this.furnitureVisibleProperties))) {
      List<HomePieceOfFurniture.SortableProperty> oldFurnitureVisibleProperties = this.furnitureVisibleProperties;
      this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>(furnitureVisibleProperties);
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_VISIBLE_PROPERTIES.name(),
          Collections.unmodifiableList(oldFurnitureVisibleProperties),
          Collections.unmodifiableList(furnitureVisibleProperties));
    }
  }

  /**
   * Returns the plan background image of this home.
   */
  public BackgroundImage getBackgroundImage() {
    return this.backgroundImage;
  }

  /**
   * Sets the plan background image of this home and fires a <code>PropertyChangeEvent</code>.
   * @param backgroundImage  the new background image
   */
  public void setBackgroundImage(BackgroundImage backgroundImage) {
    if (backgroundImage != this.backgroundImage) {
      BackgroundImage oldBackgroundImage = this.backgroundImage;
      this.backgroundImage = backgroundImage;
      this.propertyChangeSupport.firePropertyChange(
          Property.BACKGROUND_IMAGE.name(), oldBackgroundImage, backgroundImage);
    }
  }

  /**
   * Returns the camera used to display this home from a top point of view.
   */
  public Camera getTopCamera() {
    return this.topCamera;
  }

  /**
   * Returns the camera used to display this home from an observer point of view.
   */
  public ObserverCamera getObserverCamera() {
    return this.observerCamera;
  }

  /**
   * Sets the camera used to display this home and fires a <code>PropertyChangeEvent</code>.
   * @param camera  the camera to use
   */
  public void setCamera(Camera camera) {
    if (camera != this.camera) {
      Camera oldCamera = this.camera;
      this.camera = camera;
      this.propertyChangeSupport.firePropertyChange(
          Property.CAMERA.name(), oldCamera, camera);
    }
  }

  /**
   * Returns the camera used to display this home.
   */
  public Camera getCamera() {
    if (this.camera == null) {
      // Use by default top camera
      this.camera = getTopCamera();
    }
    return this.camera;
  }

  /**
   * Sets the cameras stored by this home and fires a <code>PropertyChangeEvent</code>.
   * The list given as parameter is cloned but not the camera instances it contains.
   * @param storedCameras  the new list of cameras
   * @since 3.0
   */
  public void setStoredCameras(List<Camera> storedCameras) {
    if (!this.storedCameras.equals(storedCameras)) {
      List<Camera> oldStoredCameras = this.storedCameras;
      if (storedCameras == null) {
        this.storedCameras = Collections.emptyList();
      } else {
        this.storedCameras = new ArrayList<Camera>(storedCameras);
      }
      this.propertyChangeSupport.firePropertyChange(
          Property.STORED_CAMERAS.name(), Collections.unmodifiableList(oldStoredCameras), Collections.unmodifiableList(storedCameras));
    }
  }

  /**
   * Returns an unmodifiable list of the cameras stored by this home.
   * @since 3.0
   */
  public List<Camera> getStoredCameras() {
    return Collections.unmodifiableList(this.storedCameras);
  }

  /**
   * Returns the environment attributes of this home.
   */
  public HomeEnvironment getEnvironment() {
    return this.environment;
  }

  /**
   * Returns the compass associated to this home.
   * @since 3.0
   */
  public Compass getCompass() {
    return this.compass;
  }

  /**
   * Returns the print attributes of this home.
   */
  public HomePrint getPrint() {
    return this.print;
  }

  /**
   * Sets the print attributes of this home and fires a <code>PropertyChangeEvent</code>.
   * @param print  the new print attributes
   */
  public void setPrint(HomePrint print) {
    if (print != this.print) {
      HomePrint oldPrint = this.print;
      this.print = print;
      this.propertyChangeSupport.firePropertyChange(Property.PRINT.name(), oldPrint, print);
    }
    this.print = print;
  }

  /**
   * Returns the value of the visual property <code>name</code> associated with this home.
   * @deprecated {@link #getVisualProperty(String)} and {@link #setVisualProperty(String, Object)}
   *     should be replaced by calls to {@link #getProperty(String)} and {@link #setProperty(String, String)}
   *     to ensure they can be easily saved and read. Future file format might not save visual properties anymore.
   */
  public Object getVisualProperty(String name) {
    return this.visualProperties.get(name);
  }

  /**
   * Sets a visual property associated with this home.
   * @deprecated {@link #getVisualProperty(String)} and {@link #setVisualProperty(String, Object)}
   *     should be replaced by calls to {@link #getProperty(String)} and {@link #setProperty(String, String)}
   *     to ensure they can be easily saved and read. Future file format might not save visual properties anymore.
   */
  public void setVisualProperty(String name, Object value) {
    this.visualProperties.put(name, value);
  }

  /**
   * Returns the value of the property <code>name</code> associated with this home.
   * @return the value of the property or <code>null</code> if it doesn't exist.
   * @since 5.2
   */
  public String getProperty(String name) {
    return this.properties.get(name);
  }

  /**
   * Returns the numeric value of the property <code>name</code> associated with this home.
   * @return an instance of {@link Long}, {@link Double} or <code>null</code> if the property
   * doesn't exist or can't be parsed.
   * @since 5.2
   */
  public Number getNumericProperty(String name) {
    String value = this.properties.get(name);
    if (value != null) {
      try {
        return new Long (value);
      } catch (NumberFormatException ex) {
        try {
          return new Double (value);
        } catch (NumberFormatException ex1) {
        }
      }
    }
    return null;
  }

  /**
   * Sets a property associated with this home.
   * @param name   the name of the property to set
   * @param value  the new value of the property
   * @since 5.2
   */
  public void setProperty(String name, String value) {
    if (value == null) {
      if (this.properties.containsKey(name)) {
        this.properties.remove(name);
      }
    } else {
      this.properties.put(name, value);
    }
  }

  /**
   * Returns the property names.
   * @return a collection of all the names of the properties set with {@link #setProperty(String, String) setProperty}
   * @since 5.2
   */
  public Collection<String> getPropertyNames() {
    return this.properties.keySet();
  }

  /**
   * Returns <code>true</code> if the home objects belonging to the base plan
   * (generally walls, rooms, dimension lines and texts) are locked.
   * @since 1.8
   */
  public boolean isBasePlanLocked() {
    return this.basePlanLocked;
  }

  /**
   * Sets whether home objects belonging to the base plan (generally walls, rooms,
   * dimension lines and texts) are locked and fires a <code>PropertyChangeEvent</code>.
   * @since 1.8
   */
  public void setBasePlanLocked(boolean basePlanLocked) {
    if (basePlanLocked != this.basePlanLocked) {
      this.basePlanLocked = basePlanLocked;
      this.propertyChangeSupport.firePropertyChange(
          Property.BASE_PLAN_LOCKED.name(), !basePlanLocked, basePlanLocked);
    }
  }

  /**
   * Returns the version of this home, the last time it was serialized or
   * or {@link #CURRENT_VERSION} if it is not serialized yet or
   * was serialized with Sweet Home 3D 0.x.
   * Version is useful to know with which Sweet Home 3D version this home was saved
   * and warn user that he may lose information if he saves with
   * current application a home created by a more recent version.
   */
  public long getVersion() {
    return this.version;
  }

  /**
   * Sets the version of this home.
   * @return version  the new version
   * @since 5.2
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Returns a clone of this home and the objects it contains.
   * Listeners bound to this home aren't added to the returned home.
   * @since 2.3
   */
  @Override
  public Home clone() {
    try {
      Home clone = (Home)super.clone();
      copyHomeData(this, clone);
      initListenersSupport(clone);
      clone.addModelListeners();
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable");
    }
  }

  /**
   * Copies all data of a <code>source</code> home to a <code>destination</code> home.
   */
  private static void copyHomeData(Home source, Home destination) {
    // Copy non mutable data
    destination.allLevelsSelection = source.allLevelsSelection;
    destination.name = source.name;
    destination.modified = source.modified;
    destination.recovered = source.recovered;
    destination.repaired = source.repaired;
    destination.backgroundImage = source.backgroundImage;
    destination.print = source.print;
    destination.furnitureDescendingSorted = source.furnitureDescendingSorted;
    destination.version = source.version;
    destination.basePlanLocked = source.basePlanLocked;
    destination.skyColor = source.skyColor;
    destination.groundColor = source.groundColor;
    destination.lightColor = source.lightColor;
    destination.wallsAlpha = source.wallsAlpha;
    destination.furnitureSortedProperty = source.furnitureSortedProperty;

    // Deep copy selectable items
    destination.selectedItems = new ArrayList<Selectable>(source.selectedItems.size());
    destination.furniture = cloneSelectableItems(
        source.furniture, source.selectedItems, destination.selectedItems);
    for (int i = 0; i < source.furniture.size(); i++) {
      HomePieceOfFurniture piece = source.furniture.get(i);
      if (piece instanceof HomeDoorOrWindow
          && ((HomeDoorOrWindow)piece).isBoundToWall()) {
        ((HomeDoorOrWindow)destination.furniture.get(i)).setBoundToWall(true);
      }
    }
    destination.rooms = cloneSelectableItems(source.rooms, source.selectedItems, destination.selectedItems);
    destination.dimensionLines = cloneSelectableItems(
        source.dimensionLines, source.selectedItems, destination.selectedItems);
    destination.polylines = cloneSelectableItems(
        source.polylines, source.selectedItems, destination.selectedItems);
    destination.labels = cloneSelectableItems(source.labels, source.selectedItems, destination.selectedItems);
    // Deep copy walls
    destination.walls = Wall.clone(source.walls);
    for (int i = 0; i < source.walls.size(); i++) {
      Wall wall = source.walls.get(i);
      if (source.selectedItems.contains(wall)) {
        destination.selectedItems.add(destination.walls.get(i));
      }
    }
    // Clone levels and set the level of cloned objects
    destination.levels = new ArrayList<Level>();
    if (source.levels.size() > 0) {
      for (Level level : source.levels) {
        destination.levels.add(level.clone());
      }
      for (int i = 0; i < source.furniture.size(); i++) {
        Level pieceLevel = source.furniture.get(i).getLevel();
        if (pieceLevel != null) {
          // As soon as there's more than one level, every object is supposed to have its level set
          // but as level can still be null for a undetermined reason, prefer to keep level
          // to null in the cloned object and having errors further than throwing exception here
          destination.furniture.get(i).setLevel(destination.levels.get(source.levels.indexOf(pieceLevel)));
        }
      }
      for (int i = 0; i < source.rooms.size(); i++) {
        Level roomLevel = source.rooms.get(i).getLevel();
        if (roomLevel != null) {
          destination.rooms.get(i).setLevel(destination.levels.get(source.levels.indexOf(roomLevel)));
        }
      }
      for (int i = 0; i < source.dimensionLines.size(); i++) {
        Level dimensionLineLevel = source.dimensionLines.get(i).getLevel();
        if (dimensionLineLevel != null) {
          destination.dimensionLines.get(i).setLevel(destination.levels.get(source.levels.indexOf(dimensionLineLevel)));
        }
      }
      for (int i = 0; i < source.polylines.size(); i++) {
        Level polylineLevel = source.polylines.get(i).getLevel();
        if (polylineLevel != null) {
          destination.polylines.get(i).setLevel(destination.levels.get(source.levels.indexOf(polylineLevel)));
        }
      }
      for (int i = 0; i < source.labels.size(); i++) {
        Level labelLevel = source.labels.get(i).getLevel();
        if (labelLevel != null) {
          destination.labels.get(i).setLevel(destination.levels.get(source.levels.indexOf(labelLevel)));
        }
      }
      for (int i = 0; i < source.walls.size(); i++) {
        Level wallLevel = source.walls.get(i).getLevel();
        if (wallLevel != null) {
          destination.walls.get(i).setLevel(destination.levels.get(source.levels.indexOf(wallLevel)));
        }
      }
      if (source.selectedLevel != null) {
        destination.selectedLevel = destination.levels.get(source.levels.indexOf(source.selectedLevel));
      }
    }
    // Copy cameras
    destination.observerCamera = source.observerCamera.clone();
    destination.topCamera = source.topCamera.clone();
    if (source.camera == source.observerCamera) {
      destination.camera = destination.observerCamera;
      if (source.selectedItems.contains(source.observerCamera)) {
        destination.selectedItems.add(destination.observerCamera);
      }
    } else {
      destination.camera = destination.topCamera;
    }
    destination.storedCameras = new ArrayList<Camera>(source.storedCameras.size());
    for (Camera camera : source.storedCameras) {
      destination.storedCameras.add(camera.clone());
    }
    // Copy other mutable objects
    destination.environment = source.environment.clone();
    destination.compass = source.compass.clone();
    destination.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>(
        source.furnitureVisibleProperties);
    destination.visualProperties = new HashMap<String, Object>(source.visualProperties);
    destination.properties = new HashMap<String, String>(source.properties);
  }

  /**
   * Returns the list of cloned items in <code>source</code>.
   * If a cloned item is selected its clone will be selected too (ie added to
   * <code>destinationSelectedItems</code>).
   */
  @SuppressWarnings("unchecked")
  private static <T extends Selectable> List<T> cloneSelectableItems(List<T> source,
                                                                     List<Selectable> sourceSelectedItems,
                                                                     List<Selectable> destinationSelectedItems) {
    List<T> destination = new ArrayList<T>(source.size());
    for (T item : source) {
      T clone = (T)item.clone();
      destination.add(clone);
      if (sourceSelectedItems.contains(item)) {
        destinationSelectedItems.add(clone);
      } else if (item instanceof HomeFurnitureGroup) {
        // Check if furniture in group is selected
        List<HomePieceOfFurniture> sourceFurnitureGroup = ((HomeFurnitureGroup)item).getAllFurniture();
        List<HomePieceOfFurniture> destinationFurnitureGroup = null;
        for (int i = 0, n = sourceFurnitureGroup.size(); i < n; i++) {
          HomePieceOfFurniture piece = sourceFurnitureGroup.get(i);
          if (sourceSelectedItems.contains(piece)) {
            if (destinationFurnitureGroup == null) {
              destinationFurnitureGroup = ((HomeFurnitureGroup)clone).getAllFurniture();
            }
            destinationSelectedItems.add(destinationFurnitureGroup.get(i));
          }
        }
      }
    }
    return destination;
  }

  /**
   * Returns a deep copy of home selectable <code>items</code>.
   * Cloned items are at the same index as their original.
   * @param items  the items to duplicate
   */
  public static List<Selectable> duplicate(List<? extends Selectable> items) {
    List<Selectable> list = new ArrayList<Selectable>();
    // Clone first walls list with their walls at start and end point set
    List<Wall> clonedWalls = Wall.clone(getWallsSubList(items));
    int wallIndex = 0;
    for (Selectable item : items) {
      if (item instanceof Wall) {
        list.add(clonedWalls.get(wallIndex++));
      } else {
        list.add(item.clone());
      }
    }
    return list;
  }

  /**
   * Returns a sub list of <code>items</code> that contains only home furniture.
   * @param items  the items among which the search is done
   */
  public static List<HomePieceOfFurniture> getFurnitureSubList(List<? extends Selectable> items) {
    return getSubList(items, HomePieceOfFurniture.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only walls.
   * @param items  the items among which the search is done
   */
  public static List<Wall> getWallsSubList(List<? extends Selectable> items) {
    return getSubList(items, Wall.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only rooms.
   * @param items  the items among which the search is done
   */
  public static List<Room> getRoomsSubList(List<? extends Selectable> items) {
    return getSubList(items, Room.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only labels.
   * @param items  the items among which the search is done
   * @since 5.0
   */
  public static List<Polyline> getPolylinesSubList(List<? extends Selectable> items) {
    return getSubList(items, Polyline.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only dimension lines.
   * @param items  the items among which the search is done
   */
  public static List<DimensionLine> getDimensionLinesSubList(List<? extends Selectable> items) {
    return getSubList(items, DimensionLine.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only labels.
   * @param items  the items among which the search is done
   */
  public static List<Label> getLabelsSubList(List<? extends Selectable> items) {
    return getSubList(items, Label.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only instances of <code>subListClass</code>.
   * @param items         the items among which the search is done
   * @param subListClass  the class of the searched items
   * @since 2.2
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getSubList(List<? extends Selectable> items,
                                       Class<T> subListClass) {
    List<T> subList = new ArrayList<T>();
    for (Selectable item : items) {
      if (subListClass.isInstance(item)) {
        subList.add((T)item);
      }
    }
    return subList;
  }
}