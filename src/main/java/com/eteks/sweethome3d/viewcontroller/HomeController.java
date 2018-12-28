/*
 * HomeController.java 15 mai 2006
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DamagedHomeRecorderException;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.NotEnoughSpaceRecorderException;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * A MVC controller for the home view.
 * @author Emmanuel Puybaret
 */
public class HomeController implements Controller {
  private final Home                  home;
  private final UserPreferences       preferences;
  private final HomeApplication       application;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private final UndoManager           undoManager;
  private HomeView                    homeView;
  private List<Controller>            childControllers;
  private FurnitureCatalogController  furnitureCatalogController;
  private FurnitureController         furnitureController;
  private PlanController              planController;
  private HomeController3D            homeController3D;
  private static HelpController       helpController;  // Only one help controller
  private int                         saveUndoLevel;
  private boolean                     notUndoableModifications;
  private View                        focusedView;

  private static final Content REPAIRED_IMAGE_CONTENT = new ResourceURLContent(HomeController.class, "resources/repairedImage.png");
  private static final Content REPAIRED_ICON_CONTENT = new ResourceURLContent(HomeController.class, "resources/repairedIcon.png");
  private static final Content REPAIRED_MODEL_CONTENT = new ResourceURLContent(HomeController.class, "resources/repairedModel.obj");

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   * @param viewFactory a factory able to create views.
   * @param contentManager the content manager of the application.
   */
  public HomeController(Home home,
                        HomeApplication application,
                        ViewFactory    viewFactory,
                        ContentManager contentManager) {
    this(home, application.getUserPreferences(), viewFactory,
        contentManager, application);
  }

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   * @param viewFactory a factory able to create views.
   */
  public HomeController(Home home,
                        HomeApplication application,
                        ViewFactory viewFactory) {
    this(home, application.getUserPreferences(), viewFactory, null, application);
  }

  /**
   * Creates the controller of home view.
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   * @param viewFactory a factory able to create views.
   */
  public HomeController(Home home,
                        UserPreferences preferences,
                        ViewFactory viewFactory) {
    this(home, preferences, viewFactory, null, null);
  }

  /**
   * Creates the controller of home view.
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   * @param viewFactory a factory able to create views.
   * @param contentManager the content manager of the application.
   */
  public HomeController(Home home,
                        UserPreferences preferences,
                        ViewFactory    viewFactory,
                        ContentManager contentManager) {
    this(home, preferences, viewFactory, contentManager, null);
  }

  private HomeController(final Home home,
                         final UserPreferences preferences,
                         ViewFactory    viewFactory,
                         ContentManager contentManager,
                         HomeApplication application) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.application = application;

    this.undoSupport = new UndoableEditSupport() {
        @Override
        protected void _postEdit(UndoableEdit edit) {
          // Ignore not significant compound edit
          if (!(edit instanceof CompoundEdit)
              || edit.isSignificant()) {
            super._postEdit(edit);
          }
        }
      };
    this.undoManager = new UndoManager();
    this.undoSupport.addUndoableEditListener(this.undoManager);

    // Update recent homes list
    if (home.getName() != null) {
      List<String> recentHomes = new ArrayList<String>(this.preferences.getRecentHomes());
      recentHomes.remove(home.getName());
      recentHomes.add(0, home.getName());
      updateUserPreferencesRecentHomes(recentHomes);

      // If home version is more recent than current version
      if (home.getVersion() > Home.CURRENT_VERSION) {
        // Warn the user that view will display a home created with a more recent version
        getView().invokeLater(new Runnable() {
            public void run() {
              String message = preferences.getLocalizedString(HomeController.class,
                  "moreRecentVersionHome", home.getName());
              getView().showMessage(message);
            }
          });
      }
    }
  }

  /**
   * Enables actions at controller instantiation.
   */
  private void enableDefaultActions(HomeView homeView) {
    boolean applicationExists = this.application != null;

    homeView.setEnabled(HomeView.ActionType.NEW_HOME, applicationExists);
    homeView.setEnabled(HomeView.ActionType.NEW_HOME_FROM_EXAMPLE, applicationExists);
    homeView.setEnabled(HomeView.ActionType.OPEN, applicationExists);
    homeView.setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES,
        applicationExists && !this.preferences.getRecentHomes().isEmpty());
    homeView.setEnabled(HomeView.ActionType.CLOSE, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE_AS, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE_AND_COMPRESS, applicationExists);
    homeView.setEnabled(HomeView.ActionType.PAGE_SETUP, true);
    homeView.setEnabled(HomeView.ActionType.PRINT_PREVIEW, true);
    homeView.setEnabled(HomeView.ActionType.PRINT, true);
    homeView.setEnabled(HomeView.ActionType.PRINT_TO_PDF, true);
    homeView.setEnabled(HomeView.ActionType.PREFERENCES, true);
    homeView.setEnabled(HomeView.ActionType.EXIT, applicationExists);
    homeView.setEnabled(HomeView.ActionType.IMPORT_FURNITURE, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_FURNITURE_LIBRARY, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_TEXTURE, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_TEXTURES_LIBRARY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_NAME, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_CREATOR, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_WIDTH, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DEPTH, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_X, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_Y, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_ANGLE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_LEVEL, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_MODEL_SIZE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_COLOR, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_TEXTURE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_TYPE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_PRICE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
        this.home.getFurnitureSortedProperty() != null);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_NAME, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_CREATOR, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_WIDTH, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_DEPTH, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_X, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_Y, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_ANGLE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_LEVEL, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_MODEL_SIZE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_COLOR, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_TEXTURE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_PRICE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED, true);
    homeView.setEnabled(HomeView.ActionType.EXPORT_TO_CSV, true);
    homeView.setEnabled(HomeView.ActionType.SELECT, true);
    homeView.setEnabled(HomeView.ActionType.PAN, true);
    homeView.setEnabled(HomeView.ActionType.LOCK_BASE_PLAN, true);
    homeView.setEnabled(HomeView.ActionType.UNLOCK_BASE_PLAN, true);
    homeView.setEnabled(HomeView.ActionType.MODIFY_COMPASS, true);
    Level selectedLevel = this.home.getSelectedLevel();
    enableBackgroungImageActions(homeView, selectedLevel != null
        ? selectedLevel.getBackgroundImage()
        : this.home.getBackgroundImage());
    homeView.setEnabled(HomeView.ActionType.ADD_LEVEL, true);
    homeView.setEnabled(HomeView.ActionType.ADD_LEVEL_AT_SAME_ELEVATION, true);
    List<Level> levels = this.home.getLevels();
    boolean homeContainsOneSelectedLevel = levels.size() > 1 && selectedLevel != null;
    homeView.setEnabled(HomeView.ActionType.SELECT_ALL_AT_ALL_LEVELS, levels.size() > 1);
    homeView.setEnabled(HomeView.ActionType.MAKE_LEVEL_VIEWABLE, homeContainsOneSelectedLevel);
    homeView.setEnabled(HomeView.ActionType.MAKE_LEVEL_UNVIEWABLE, homeContainsOneSelectedLevel);
    homeView.setEnabled(HomeView.ActionType.MAKE_LEVEL_ONLY_VIEWABLE_ONE, homeContainsOneSelectedLevel);
    homeView.setEnabled(HomeView.ActionType.MAKE_ALL_LEVELS_VIEWABLE, levels.size() > 1);
    homeView.setEnabled(HomeView.ActionType.MODIFY_LEVEL, homeContainsOneSelectedLevel);
    homeView.setEnabled(HomeView.ActionType.DELETE_LEVEL, homeContainsOneSelectedLevel);
    homeView.setEnabled(HomeView.ActionType.ZOOM_IN, true);
    homeView.setEnabled(HomeView.ActionType.ZOOM_OUT, true);
    homeView.setEnabled(HomeView.ActionType.EXPORT_TO_SVG, true);
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_TOP, true);
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_OBSERVER, true);
    homeView.setEnabled(HomeView.ActionType.MODIFY_OBSERVER, this.home.getCamera() == this.home.getObserverCamera());
    homeView.setEnabled(HomeView.ActionType.STORE_POINT_OF_VIEW, true);
    boolean emptyStoredCameras = home.getStoredCameras().isEmpty();
    homeView.setEnabled(HomeView.ActionType.DELETE_POINTS_OF_VIEW, !emptyStoredCameras);
    homeView.setEnabled(HomeView.ActionType.CREATE_PHOTOS_AT_POINTS_OF_VIEW, !emptyStoredCameras);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_ALL_LEVELS, levels.size() > 1);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_SELECTED_LEVEL, levels.size() > 1);
    homeView.setEnabled(HomeView.ActionType.DETACH_3D_VIEW, true);
    homeView.setEnabled(HomeView.ActionType.ATTACH_3D_VIEW, true);
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_OBSERVER, true);
    homeView.setEnabled(HomeView.ActionType.MODIFY_3D_ATTRIBUTES, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_PHOTO, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_VIDEO, true);
    homeView.setEnabled(HomeView.ActionType.EXPORT_TO_OBJ, true);
    homeView.setEnabled(HomeView.ActionType.HELP, true);
    homeView.setEnabled(HomeView.ActionType.ABOUT, true);
    enableCreationToolsActions(homeView);
    homeView.setTransferEnabled(true);
  }

  /**
   * Enables plan actions depending on the selected level is viewable or not.
   */
  private void enableCreationToolsActions(HomeView homeView) {
    Level selectedLevel = this.home.getSelectedLevel();
    boolean viewableLevel = selectedLevel == null || selectedLevel.isViewable();
    homeView.setEnabled(HomeView.ActionType.CREATE_WALLS, viewableLevel);
    homeView.setEnabled(HomeView.ActionType.CREATE_ROOMS, viewableLevel);
    homeView.setEnabled(HomeView.ActionType.CREATE_POLYLINES, viewableLevel);
    homeView.setEnabled(HomeView.ActionType.CREATE_DIMENSION_LINES, viewableLevel);
    homeView.setEnabled(HomeView.ActionType.CREATE_LABELS, viewableLevel);
  }

  /**
   * Returns the view associated with this controller.
   */
  public HomeView getView() {
    if (this.homeView == null) {
      this.homeView = this.viewFactory.createHomeView(this.home, this.preferences, this);
      enableDefaultActions(this.homeView);
      addListeners();
    }
    return this.homeView;
  }

  /**
   * Returns the content manager of this controller.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
  }

  /**
   * Returns the furniture catalog controller managed by this controller.
   */
  public FurnitureCatalogController getFurnitureCatalogController() {
    // Create sub controller lazily only once it's needed
    if (this.furnitureCatalogController == null) {
      this.furnitureCatalogController = new FurnitureCatalogController(
          this.preferences.getFurnitureCatalog(), this.preferences, this.viewFactory, this.contentManager);
    }
    return this.furnitureCatalogController;
  }

  /**
   * Returns the furniture controller managed by this controller.
   */
  public FurnitureController getFurnitureController() {
    // Create sub controller lazily only once it's needed
    if (this.furnitureController == null) {
      this.furnitureController = new FurnitureController(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.furnitureController;
  }

  /**
   * Returns the controller of home plan.
   */
  public PlanController getPlanController() {
    // Create sub controller lazily only once it's needed
    if (this.planController == null) {
      this.planController = new PlanController(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.planController;
  }

  /**
   * Returns the controller of home 3D view.
   */
  public HomeController3D getHomeController3D() {
    // Create sub controller lazily only once it's needed
    if (this.homeController3D == null) {
      this.homeController3D = new HomeController3D(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.homeController3D;
  }

  /**
   * Returns the undoable edit support managed by this controller.
   */
  protected final UndoableEditSupport getUndoableEditSupport() {
    return this.undoSupport;
  }

  /**
   * Adds listeners that updates the enabled / disabled state of actions.
   */
  private void addListeners() {
    // Save preferences when they change
    this.preferences.getFurnitureCatalog().addFurnitureListener(
        new FurnitureCatalogChangeListener(this));
    this.preferences.getTexturesCatalog().addTexturesListener(
        new TexturesCatalogChangeListener(this));
    UserPreferencesPropertiesChangeListener listener =
        new UserPreferencesPropertiesChangeListener(this);
    for (UserPreferences.Property property : UserPreferences.Property.values()) {
      this.preferences.addPropertyChangeListener(property, listener);
    }

    addCatalogSelectionListener();
    addHomeBackgroundImageListener();
    addNotUndoableModificationListeners();
    addHomeSelectionListener();
    addFurnitureSortListener();
    addUndoSupportListener();
    addHomeItemsListener();
    addLevelListeners();
    addStoredCamerasListener();
    addPlanControllerListeners();
    addLanguageListener();
  }

  /**
   * Super class of catalog listeners that writes preferences each time a piece of furniture or a texture
   * is deleted or added in furniture or textures catalog.
   */
  private abstract static class UserPreferencesChangeListener {
    // Stores the currently writing preferences
    private static Set<UserPreferences> writingPreferences = new HashSet<UserPreferences>();

    public void writePreferences(final HomeController controller) {
      if (!writingPreferences.contains(controller.preferences)) {
        writingPreferences.add(controller.preferences);
        // Write preferences later once all catalog modifications are notified
        controller.getView().invokeLater(new Runnable() {
            public void run() {
              try {
                controller.preferences.write();
                writingPreferences.remove(controller.preferences);
              } catch (RecorderException ex) {
                controller.getView().showError(controller.preferences.getLocalizedString(
                    HomeController.class, "savePreferencesError"));
              }
            }
          });
      }
    }
  }

  /**
   * Furniture catalog listener that writes preferences each time a piece of furniture
   * is deleted or added in furniture catalog. This listener is bound to this controller
   * with a weak reference to avoid strong link between catalog and this controller.
   */
  private static class FurnitureCatalogChangeListener extends UserPreferencesChangeListener
                                                      implements CollectionListener<CatalogPieceOfFurniture> {
    private WeakReference<HomeController> homeController;

    public FurnitureCatalogChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }

    public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((FurnitureCatalog)ev.getSource()).removeFurnitureListener(this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Textures catalog listener that writes preferences each time a texture
   * is deleted or added in textures catalog. This listener is bound to this controller
   * with a weak reference to avoid strong link between catalog and this controller.
   */
  private static class TexturesCatalogChangeListener extends UserPreferencesChangeListener
                                                     implements CollectionListener<CatalogTexture> {
    private WeakReference<HomeController> homeController;

    public TexturesCatalogChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }

    public void collectionChanged(CollectionEvent<CatalogTexture> ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((TexturesCatalog)ev.getSource()).removeTexturesListener(this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Properties listener that writes preferences each time the value of one of its properties changes.
   * This listener is bound to this controller with a weak reference to avoid strong link
   * between catalog and this controller.
   */
  private static class UserPreferencesPropertiesChangeListener extends UserPreferencesChangeListener
                                                               implements PropertyChangeListener {
    private WeakReference<HomeController> homeController;

    public UserPreferencesPropertiesChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.valueOf(ev.getPropertyName()), this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Adds a selection listener to catalog that enables / disables Add Furniture action.
   */
  private void addCatalogSelectionListener() {
    getFurnitureCatalogController().addSelectionListener(new SelectionListener() {
          public void selectionChanged(SelectionEvent ev) {
            enableActionsBoundToSelection();
          }
        });
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * undo and redo presentation names when preferred language changes.
   */
  private void addLanguageListener() {
    this.preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HomeController> homeController;

    public LanguageChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomeController homeController = this.homeController.get();
      if (homeController == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update undo and redo name
        homeController.getView().setUndoRedoName(
            homeController.undoManager.canUndo()
                ? homeController.undoManager.getUndoPresentationName()
                : null,
            homeController.undoManager.canRedo()
                ? homeController.undoManager.getRedoPresentationName()
                : null);
      }
    }
  }

  /**
   *  Adds a selection listener to home that enables / disables actions on selection.
   */
  private void addHomeSelectionListener() {
    if (this.home != null) {
      this.home.addSelectionListener(new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          enableActionsBoundToSelection();
        }
      });
    }
  }

  /**
   *  Adds a property change listener to home that enables / disables sort order action.
   */
  private void addFurnitureSortListener() {
    if (this.home != null) {
      this.home.addPropertyChangeListener(Home.Property.FURNITURE_SORTED_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            getView().setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
                ev.getNewValue() != null);
          }
        });
    }
  }

  /**
   *  Adds a property change listener to home that enables / disables background image actions.
   */
  private void addHomeBackgroundImageListener() {
    if (this.home != null) {
      this.home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              enableBackgroungImageActions(getView(), (BackgroundImage)ev.getNewValue());
            }
          });
    }
  }

  /**
   * Enables background image actions.
   */
  private void enableBackgroungImageActions(HomeView homeView, BackgroundImage backgroundImage) {
    Level selectedLevel = this.home.getSelectedLevel();
    boolean homeHasBackgroundImage = backgroundImage != null
        && (selectedLevel == null || selectedLevel.isViewable());
    getView().setEnabled(HomeView.ActionType.IMPORT_BACKGROUND_IMAGE, !homeHasBackgroundImage);
    getView().setEnabled(HomeView.ActionType.MODIFY_BACKGROUND_IMAGE, homeHasBackgroundImage);
    getView().setEnabled(HomeView.ActionType.HIDE_BACKGROUND_IMAGE,
        homeHasBackgroundImage && backgroundImage.isVisible());
    getView().setEnabled(HomeView.ActionType.SHOW_BACKGROUND_IMAGE,
        homeHasBackgroundImage && !backgroundImage.isVisible());
    getView().setEnabled(HomeView.ActionType.DELETE_BACKGROUND_IMAGE, homeHasBackgroundImage);
  }

  /**
   * Adds listeners to track property changes that are not undoable.
   */
  private void addNotUndoableModificationListeners() {
    if (this.home != null) {
      final PropertyChangeListener notUndoableModificationListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            notUndoableModifications = true;
            home.setModified(true);
          }
        };
      this.home.addPropertyChangeListener(Home.Property.STORED_CAMERAS, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.OBSERVER_CAMERA_ELEVATION_ADJUSTED, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.VIDEO_WIDTH, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.VIDEO_ASPECT_RATIO, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.VIDEO_FRAME_RATE, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.VIDEO_QUALITY, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.VIDEO_CAMERA_PATH, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.CEILING_LIGHT_COLOR, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_QUALITY, notUndoableModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_ASPECT_RATIO, notUndoableModificationListener);
      PropertyChangeListener photoSizeModificationListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (home.getEnvironment().getPhotoAspectRatio() != AspectRatio.VIEW_3D_RATIO) {
              // Ignore photo size modification with 3D view aspect ratio since it can change for various reasons
              notUndoableModificationListener.propertyChange(ev);
            }
          }
        };
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_WIDTH, photoSizeModificationListener);
      this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_HEIGHT, photoSizeModificationListener);
      PropertyChangeListener timeOrLensModificationListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getPropertyName().equals(Camera.Property.TIME.name())
                || ev.getPropertyName().equals(Camera.Property.LENS.name())) {
              notUndoableModificationListener.propertyChange(ev);
            }
          }
        };
      this.home.getObserverCamera().addPropertyChangeListener(timeOrLensModificationListener);
      this.home.getTopCamera().addPropertyChangeListener(timeOrLensModificationListener);
    }
  }

  /**
   * Enables or disables action bound to selection.
   * This method will be called when selection in plan or in catalog changes and when
   * focused component or modification state in plan changes.
   */
  protected void enableActionsBoundToSelection() {
    boolean modificationState = getPlanController().isModificationState();

    // Search if catalog selection contains at least one piece
    List<CatalogPieceOfFurniture> catalogSelectedItems =
        getFurnitureCatalogController().getSelectedFurniture();
    boolean catalogSelectionContainsFurniture = !catalogSelectedItems.isEmpty();
    boolean catalogSelectionContainsOneModifiablePiece = catalogSelectedItems.size() == 1
        && catalogSelectedItems.get(0).isModifiable();

    // Search if home selection contains at least one piece, one wall or one dimension line
    List<Selectable> selectedItems = this.home.getSelectedItems();
    boolean homeSelectionContainsDeletableItems = false;
    boolean homeSelectionContainsFurniture = false;
    boolean homeSelectionContainsDeletableFurniture = false;
    boolean homeSelectionContainsOneCopiableItemOrMore = false;
    boolean homeSelectionContainsOneMovablePieceOfFurnitureOrMore = false;
    boolean homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsTwoMovableGroupablePiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsThreeMovablePiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsOnlyOneGroup = selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomeFurnitureGroup;
    boolean homeSelectionContainsFurnitureGroup = false;
    boolean homeSelectionContainsWalls = false;
    boolean homeSelectionContainsOneWall = false;
    boolean homeSelectionContainsOneOrTwoWallsWithOneFreeEnd = false;
    boolean homeSelectionContainsRooms = false;
    boolean homeSelectionContainsPolylines = false;
    boolean homeSelectionContainsOnlyOneRoom = false;
    boolean homeSelectionContainsOnlyOneRoomWithFourPointsOrMore = false;
    boolean homeSelectionContainsLabels = false;
    boolean homeSelectionContainsItemsWithText = false;
    boolean homeSelectionContainsCompass = false;
    FurnitureController furnitureController = getFurnitureController();
    if (!modificationState) {
      for (Selectable item : selectedItems) {
        // Check item is deletable
        if (getPlanController().isItemDeletable(item)) {
          homeSelectionContainsDeletableItems = true;
          break;
        }
      }
      List<HomePieceOfFurniture> selectedFurniture = Home.getFurnitureSubList(selectedItems);
      homeSelectionContainsFurniture = !selectedFurniture.isEmpty();
      for (HomePieceOfFurniture piece : selectedFurniture) {
        // Check piece is deletable
        if (furnitureController.isPieceOfFurnitureDeletable(piece)) {
          homeSelectionContainsDeletableFurniture = true;
          break;
        }
      }
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (piece instanceof HomeFurnitureGroup) {
          homeSelectionContainsFurnitureGroup = true;
          break;
        }
      }
      int movablePiecesOfFurnitureCount = 0;
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (furnitureController.isPieceOfFurnitureMovable(piece)) {
          homeSelectionContainsOneMovablePieceOfFurnitureOrMore = true;
          movablePiecesOfFurnitureCount++;
          if (movablePiecesOfFurnitureCount >= 2) {
            homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore = true;
          }
          if (movablePiecesOfFurnitureCount >= 3) {
            homeSelectionContainsThreeMovablePiecesOfFurnitureOrMore = true;
            break;
          }
        }
      }
      if (homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore) {
        homeSelectionContainsTwoMovableGroupablePiecesOfFurnitureOrMore = true;
        List<HomePieceOfFurniture> furniture = this.home.getFurniture();
        // Allow to group only furniture that are not in subgroups
        for (HomePieceOfFurniture piece : selectedFurniture) {
          if (!furnitureController.isPieceOfFurnitureMovable(piece)
              || !furniture.contains(piece)) {
            homeSelectionContainsTwoMovableGroupablePiecesOfFurnitureOrMore = false;
            break;
          }
        }
      }
      List<Wall> selectedWalls = Home.getWallsSubList(selectedItems);
      homeSelectionContainsWalls = !selectedWalls.isEmpty();
      homeSelectionContainsOneWall = selectedWalls.size() == 1;
      if (selectedWalls.size() >= 2) {
        Wall [] wallsWithFreeEnd = {null, null, null};
        for (Wall wall : selectedWalls) {
          if ((wall.getArcExtent() == null
                  || wall.getArcExtent() == 0f)
              && (wall.getWallAtStart() == null
                  || wall.getWallAtEnd() == null)) {
            for (int i = 0; i < wallsWithFreeEnd.length; i++) {
              if (wallsWithFreeEnd [i] == null) {
                wallsWithFreeEnd [i] = wall;
                break;
              }
            }
            if (wallsWithFreeEnd [2] != null) {
              break;
            }
          }
        }
        homeSelectionContainsOneOrTwoWallsWithOneFreeEnd =
            wallsWithFreeEnd [2] == null
            && wallsWithFreeEnd [0] != null
            && (wallsWithFreeEnd [1] == null
                  && !selectedWalls.contains(wallsWithFreeEnd [0].getWallAtStart())
                  && !selectedWalls.contains(wallsWithFreeEnd [0].getWallAtEnd())
               || wallsWithFreeEnd [0].getWallAtEnd() != wallsWithFreeEnd [1]
                   && wallsWithFreeEnd [0].getWallAtStart() != wallsWithFreeEnd [1]);
      }
      List<Room> selectedRooms = Home.getRoomsSubList(selectedItems);
      homeSelectionContainsRooms = !selectedRooms.isEmpty();
      homeSelectionContainsOnlyOneRoom = selectedItems.size() == 1
          && selectedRooms.size() == 1;
      homeSelectionContainsOnlyOneRoomWithFourPointsOrMore = homeSelectionContainsOnlyOneRoom
          && selectedRooms.get(0).getPointCount() >= 4;
      boolean homeSelectionContainsDimensionLines = !Home.getDimensionLinesSubList(selectedItems).isEmpty();
      homeSelectionContainsPolylines = !Home.getPolylinesSubList(selectedItems).isEmpty();
      homeSelectionContainsLabels = !Home.getLabelsSubList(selectedItems).isEmpty();
      homeSelectionContainsCompass = selectedItems.contains(this.home.getCompass());
      homeSelectionContainsOneCopiableItemOrMore =
          homeSelectionContainsFurniture || homeSelectionContainsWalls
          || homeSelectionContainsRooms || homeSelectionContainsDimensionLines
          || homeSelectionContainsPolylines || homeSelectionContainsLabels
          || homeSelectionContainsCompass;
      homeSelectionContainsItemsWithText =
          homeSelectionContainsFurniture || homeSelectionContainsRooms
          || homeSelectionContainsDimensionLines || homeSelectionContainsLabels;
    }

    HomeView view = getView();
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      view.setEnabled(HomeView.ActionType.COPY,
          !modificationState && catalogSelectionContainsFurniture);
      view.setEnabled(HomeView.ActionType.CUT, false);
      view.setEnabled(HomeView.ActionType.DELETE, false);
      for (CatalogPieceOfFurniture piece : catalogSelectedItems) {
        if (piece.isModifiable()) {
          // Only modifiable catalog furniture may be deleted
          view.setEnabled(HomeView.ActionType.DELETE, true);
          break;
        }
      }
    } else if (this.focusedView == furnitureController.getView()) {
      view.setEnabled(HomeView.ActionType.COPY, homeSelectionContainsFurniture);
      view.setEnabled(HomeView.ActionType.CUT, homeSelectionContainsDeletableFurniture);
      view.setEnabled(HomeView.ActionType.DELETE, homeSelectionContainsDeletableFurniture);
    } else if (this.focusedView == getPlanController().getView()) {
      view.setEnabled(HomeView.ActionType.COPY, homeSelectionContainsOneCopiableItemOrMore);
      view.setEnabled(HomeView.ActionType.CUT, homeSelectionContainsDeletableItems);
      view.setEnabled(HomeView.ActionType.DELETE, homeSelectionContainsDeletableItems);
    } else {
      view.setEnabled(HomeView.ActionType.COPY, false);
      view.setEnabled(HomeView.ActionType.CUT, false);
      view.setEnabled(HomeView.ActionType.DELETE, false);
    }
    enablePasteToGroupAction();
    enablePasteStyleAction();

    Level selectedLevel = this.home.getSelectedLevel();
    boolean viewableLevel = selectedLevel == null || selectedLevel.isViewable();
    view.setEnabled(HomeView.ActionType.ADD_HOME_FURNITURE, catalogSelectionContainsFurniture
        && viewableLevel);
    view.setEnabled(HomeView.ActionType.ADD_FURNITURE_TO_GROUP, catalogSelectionContainsFurniture
        && viewableLevel && homeSelectionContainsOnlyOneGroup);
    // In creation mode all actions bound to selection are disabled
    view.setEnabled(HomeView.ActionType.DELETE_HOME_FURNITURE,
        homeSelectionContainsDeletableFurniture);
    view.setEnabled(HomeView.ActionType.DELETE_SELECTION,
        (catalogSelectionContainsFurniture
            && this.focusedView == getFurnitureCatalogController().getView())
        || (homeSelectionContainsDeletableItems
            && (this.focusedView == furnitureController.getView()
                || this.focusedView == getPlanController().getView()
                || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomeView.ActionType.MODIFY_FURNITURE,
        (catalogSelectionContainsOneModifiablePiece
             && this.focusedView == getFurnitureCatalogController().getView())
        || (homeSelectionContainsFurniture
             && (this.focusedView == furnitureController.getView()
                 || this.focusedView == getPlanController().getView()
                 || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomeView.ActionType.MODIFY_WALL,
        homeSelectionContainsWalls);
    view.setEnabled(HomeView.ActionType.FLIP_HORIZONTALLY,
        homeSelectionContainsOneCopiableItemOrMore);
    view.setEnabled(HomeView.ActionType.FLIP_VERTICALLY,
        homeSelectionContainsOneCopiableItemOrMore);
    view.setEnabled(HomeView.ActionType.JOIN_WALLS,
        homeSelectionContainsOneOrTwoWallsWithOneFreeEnd);
    view.setEnabled(HomeView.ActionType.REVERSE_WALL_DIRECTION,
        homeSelectionContainsWalls);
    view.setEnabled(HomeView.ActionType.SPLIT_WALL,
        homeSelectionContainsOneWall);
    view.setEnabled(HomeView.ActionType.MODIFY_ROOM,
        homeSelectionContainsRooms);
    view.setEnabled(HomeView.ActionType.MODIFY_POLYLINE,
        homeSelectionContainsPolylines);
    view.setEnabled(HomeView.ActionType.MODIFY_LABEL,
        homeSelectionContainsLabels);
    view.setEnabled(HomeView.ActionType.TOGGLE_BOLD_STYLE,
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.TOGGLE_ITALIC_STYLE,
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.INCREASE_TEXT_SIZE,
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.DECREASE_TEXT_SIZE,
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_TOP,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_BOTTOM,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_LEFT,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_RIGHT,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_FRONT_SIDE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_BACK_SIDE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_LEFT_SIDE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_RIGHT_SIDE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_SIDE_BY_SIDE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.DISTRIBUTE_FURNITURE_HORIZONTALLY,
        homeSelectionContainsThreeMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.DISTRIBUTE_FURNITURE_VERTICALLY,
        homeSelectionContainsThreeMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.RESET_FURNITURE_ELEVATION,
        homeSelectionContainsOneMovablePieceOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.GROUP_FURNITURE,
        homeSelectionContainsTwoMovableGroupablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.UNGROUP_FURNITURE,
        homeSelectionContainsFurnitureGroup);
    boolean selectionMode = getPlanController() != null
        && getPlanController().getMode() == PlanController.Mode.SELECTION;
    view.setEnabled(HomeView.ActionType.ADD_ROOM_POINT, homeSelectionContainsOnlyOneRoom && selectionMode);
    // Check minimum requirement for DELETE_ROOM_POINT action
    // and let home view check the coordinates of the deleted point
    view.setEnabled(HomeView.ActionType.DELETE_ROOM_POINT,
        homeSelectionContainsOnlyOneRoomWithFourPointsOrMore && selectionMode);
  }

  /**
   * Enables clipboard paste action if clipboard isn't empty.
   */
  public void enablePasteAction() {
    HomeView view = getView();
    boolean pasteEnabled = false;
    if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()) {
      Level selectedLevel = this.home.getSelectedLevel();
      pasteEnabled = (selectedLevel == null || selectedLevel.isViewable())
          && !getPlanController().isModificationState() && !view.isClipboardEmpty();
    }
    view.setEnabled(HomeView.ActionType.PASTE, pasteEnabled);
    enablePasteToGroupAction();
    enablePasteStyleAction();
  }

  /**
   * Enables paste to group action if clipboard contains furniture and
   * home selected item is a furniture group.
   */
  private void enablePasteToGroupAction() {
    HomeView view = getView();
    boolean pasteToGroupEnabled = false;
    if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()) {
      Level selectedLevel = this.home.getSelectedLevel();
      if ((selectedLevel == null || selectedLevel.isViewable())
          && !getPlanController().isModificationState()) {
        List<Selectable> selectedItems = this.home.getSelectedItems();
        if (selectedItems.size() == 1
            && selectedItems.get(0) instanceof HomeFurnitureGroup) {
          List<Selectable> clipboardItems = view.getClipboardItems();
          if (clipboardItems != null) {
            pasteToGroupEnabled = true;
            for (Selectable item : clipboardItems) {
              if (!(item instanceof HomePieceOfFurniture)) {
                pasteToGroupEnabled = false;
                break;
              }
            }
          }
        }
      }
    }
    view.setEnabled(HomeView.ActionType.PASTE_TO_GROUP, pasteToGroupEnabled);
  }

  /**
   * Enables clipboard paste style action if selection contains some items of a class
   * compatible with the clipboard item.
   */
  private void enablePasteStyleAction() {
    HomeView view = getView();
    boolean pasteStyleEnabled = false;
    if ((this.focusedView == getFurnitureController().getView()
          || this.focusedView == getPlanController().getView())
        && !getPlanController().isModificationState()) {
      List<Selectable> clipboardItems = view.getClipboardItems();
      if (clipboardItems != null
          && clipboardItems.size() == 1) {
        Selectable clipboardItem = clipboardItems.get(0);
        for (Selectable item : this.home.getSelectedItems()) {
          if (item instanceof HomePieceOfFurniture && clipboardItem instanceof HomePieceOfFurniture
              || item instanceof Wall && clipboardItem instanceof Wall
              || item instanceof Room && clipboardItem instanceof Room
              || item instanceof Polyline && clipboardItem instanceof Polyline
              || item instanceof Label && clipboardItem instanceof Label) {
            pasteStyleEnabled = true;
            break;
          }
        }
      }
    }
    view.setEnabled(HomeView.ActionType.PASTE_STYLE, pasteStyleEnabled);
  }

  /**
   * Enables select all action if home isn't empty.
   */
  protected void enableSelectAllAction() {
    HomeView view = getView();
    boolean modificationState = getPlanController().isModificationState();
    if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomeView.ActionType.SELECT_ALL,
          !modificationState
          && this.home.getFurniture().size() > 0);
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      boolean homeContainsOneSelectableItemOrMore = !this.home.isEmpty()
          || this.home.getCompass().isVisible();
      view.setEnabled(HomeView.ActionType.SELECT_ALL,
          !modificationState && homeContainsOneSelectableItemOrMore);
    } else {
      view.setEnabled(HomeView.ActionType.SELECT_ALL, false);
    }
  }

  /**
   * Enables zoom actions depending on current scale.
   */
  private void enableZoomActions() {
    PlanController planController = getPlanController();
    float scale = planController.getScale();
    HomeView view = getView();
    view.setEnabled(HomeView.ActionType.ZOOM_IN, scale < planController.getMaximumScale());
    view.setEnabled(HomeView.ActionType.ZOOM_OUT, scale > planController.getMinimumScale());
  }

  /**
   * Adds undoable edit listener to undo support that enables Undo action.
   */
  private void addUndoSupportListener() {
    getUndoableEditSupport().addUndoableEditListener(
      new UndoableEditListener () {
        public void undoableEditHappened(UndoableEditEvent ev) {
          HomeView view = getView();
          view.setEnabled(HomeView.ActionType.UNDO,
              !getPlanController().isModificationState());
          view.setEnabled(HomeView.ActionType.REDO, false);
          view.setUndoRedoName(ev.getEdit().getUndoPresentationName(), null);
          saveUndoLevel++;
          home.setModified(true);
        }
      });
   home.addPropertyChangeListener(Home.Property.MODIFIED, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        if (!home.isModified()) {
          // Change undo level and modification flag if home is set as unmodified
          saveUndoLevel = 0;
          notUndoableModifications = false;
        }
      }
    });
  }

  /**
   * Adds a furniture listener to home that enables / disables actions on furniture list change.
   */
  @SuppressWarnings("unchecked")
  private void addHomeItemsListener() {
    CollectionListener homeItemsListener =
        new CollectionListener() {
          public void collectionChanged(CollectionEvent ev) {
            if (ev.getType() == CollectionEvent.Type.ADD
                || ev.getType() == CollectionEvent.Type.DELETE) {
              enableSelectAllAction();
            }
          }
        };
    this.home.addFurnitureListener((CollectionListener<HomePieceOfFurniture>)homeItemsListener);
    this.home.addWallsListener((CollectionListener<Wall>)homeItemsListener);
    this.home.addRoomsListener((CollectionListener<Room>)homeItemsListener);
    this.home.addPolylinesListener((CollectionListener<Polyline>)homeItemsListener);
    this.home.addDimensionLinesListener((CollectionListener<DimensionLine>)homeItemsListener);
    this.home.addLabelsListener((CollectionListener<Label>)homeItemsListener);
    this.home.getCompass().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Compass.Property.VISIBLE.equals(ev.getPropertyName())) {
            enableSelectAllAction();
          }
        }
      });
    this.home.addPropertyChangeListener(Home.Property.CAMERA, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          getView().setEnabled(HomeView.ActionType.MODIFY_OBSERVER, home.getCamera() == home.getObserverCamera());
        }
      });
  }

  /**
   * Adds a property change listener to home to
   * enable/disable authorized actions according to selected level.
   */
  private void addLevelListeners() {
    final PropertyChangeListener selectedLevelListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Level selectedLevel = home.getSelectedLevel();
          if (!home.isAllLevelsSelection()) {
            // Keep in selection only items that are at this level
            List<Selectable> selectedItemsAtLevel = new ArrayList<Selectable>();
            for (Selectable item : home.getSelectedItems()) {
              if (!(item instanceof Elevatable)
                  || ((Elevatable)item).isAtLevel(selectedLevel)) {
                selectedItemsAtLevel.add(item);
              }
            }
            home.setSelectedItems(selectedItemsAtLevel);
          }
          enableCreationToolsActions(getView());
          enableBackgroungImageActions(getView(), selectedLevel == null
              ? home.getBackgroundImage()
              : selectedLevel.getBackgroundImage());
          List<Level> levels = home.getLevels();
          boolean homeContainsOneSelectedLevel = levels.size() > 1 && selectedLevel != null;
          getView().setEnabled(HomeView.ActionType.SELECT_ALL_AT_ALL_LEVELS, levels.size() > 1);
          getView().setEnabled(HomeView.ActionType.MAKE_LEVEL_VIEWABLE, homeContainsOneSelectedLevel);
          getView().setEnabled(HomeView.ActionType.MAKE_LEVEL_UNVIEWABLE, homeContainsOneSelectedLevel);
          getView().setEnabled(HomeView.ActionType.MAKE_LEVEL_ONLY_VIEWABLE_ONE, homeContainsOneSelectedLevel);
          getView().setEnabled(HomeView.ActionType.MAKE_ALL_LEVELS_VIEWABLE, levels.size() > 1);
          getView().setEnabled(HomeView.ActionType.MODIFY_LEVEL, homeContainsOneSelectedLevel);
          getView().setEnabled(HomeView.ActionType.DELETE_LEVEL, homeContainsOneSelectedLevel);
          getView().setEnabled(HomeView.ActionType.DISPLAY_ALL_LEVELS, levels.size() > 1);
          getView().setEnabled(HomeView.ActionType.DISPLAY_SELECTED_LEVEL, levels.size() > 1);
        }
      };
    this.home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, selectedLevelListener);
    final PropertyChangeListener backgroundImageChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.BACKGROUND_IMAGE.name().equals(ev.getPropertyName())) {
            enableBackgroungImageActions(getView(), (BackgroundImage)ev.getNewValue());
          } else if (Level.Property.VIEWABLE.name().equals(ev.getPropertyName())) {
            enableCreationToolsActions(getView());
            if (!(Boolean)ev.getNewValue()) {
              PlanController.Mode mode = getPlanController().getMode();
              if (mode != PlanController.Mode.SELECTION
                  && mode != PlanController.Mode.PANNING) {
                getPlanController().setMode(PlanController.Mode.SELECTION);
              }
            }
          }
        }
      };
    for (Level level : home.getLevels()) {
      level.addPropertyChangeListener(backgroundImageChangeListener);
    }
    this.home.addLevelsListener(new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          switch (ev.getType()) {
            case ADD :
              home.setSelectedLevel(ev.getItem());
              ev.getItem().addPropertyChangeListener(backgroundImageChangeListener);
              break;
            case DELETE :
              selectedLevelListener.propertyChange(null);
              ev.getItem().removePropertyChangeListener(backgroundImageChangeListener);
              break;
          }
        }
      });
  }

  /**
   * Adds a property change listener to home to
   * enable/disable authorized actions according to stored cameras change.
   */
  private void addStoredCamerasListener() {
    this.home.addPropertyChangeListener(Home.Property.STORED_CAMERAS, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          boolean emptyStoredCameras = home.getStoredCameras().isEmpty();
          getView().setEnabled(HomeView.ActionType.DELETE_POINTS_OF_VIEW, !emptyStoredCameras);
          getView().setEnabled(HomeView.ActionType.CREATE_PHOTOS_AT_POINTS_OF_VIEW, !emptyStoredCameras);
        }
      });
  }

  /**
   * Adds a property change listener to plan controller to
   * enable/disable authorized actions according to its modification state and the plan scale.
   */
  private void addPlanControllerListeners() {
    getPlanController().addPropertyChangeListener(PlanController.Property.MODIFICATION_STATE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableActionsBoundToSelection();
            enableSelectAllAction();
            HomeView view = getView();
            if (getPlanController().isModificationState()) {
              view.setEnabled(HomeView.ActionType.PASTE, false);
              view.setEnabled(HomeView.ActionType.UNDO, false);
              view.setEnabled(HomeView.ActionType.REDO, false);
            } else {
              enablePasteAction();
              view.setEnabled(HomeView.ActionType.UNDO, undoManager.canUndo());
              view.setEnabled(HomeView.ActionType.REDO, undoManager.canRedo());
            }
          }
        });
    getPlanController().addPropertyChangeListener(PlanController.Property.MODE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableActionsBoundToSelection();
          }
        });
    getPlanController().addPropertyChangeListener(PlanController.Property.SCALE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableZoomActions();
          }
        });
  }

  /**
   * Adds the selected furniture in catalog to home and selects it.
   */
  public void addHomeFurniture() {
    addFurniture(null);
  }

  /**
   * Adds the selected furniture in catalog to the selected group and selects it.
   * @since 5.0
   */
  public void addFurnitureToGroup() {
    addFurniture((HomeFurnitureGroup)this.home.getSelectedItems().get(0));
  }

  private void addFurniture(HomeFurnitureGroup group) {
    // Use automatically selection mode
    getPlanController().setMode(PlanController.Mode.SELECTION);
    List<CatalogPieceOfFurniture> selectedFurniture =
      getFurnitureCatalogController().getSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      List<HomePieceOfFurniture> addedFurniture = new ArrayList<HomePieceOfFurniture>();
      for (CatalogPieceOfFurniture piece : selectedFurniture) {
        addedFurniture.add(getFurnitureController().createHomePieceOfFurniture(piece));
      }
      // Add furniture to home with furnitureController
      if (group != null) {
        getFurnitureController().addFurnitureToGroup(addedFurniture, group);
      } else {
        getFurnitureController().addFurniture(addedFurniture);
      }
      adjustFurnitureSizeAndElevation(addedFurniture, false);
    }
  }

  /**
   * Modifies the selected furniture of the focused view.
   */
  public void modifySelectedFurniture() {
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      getFurnitureCatalogController().modifySelectedFurniture();
    } else if (this.focusedView == getFurnitureController().getView()
               || this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      getFurnitureController().modifySelectedFurniture();
    }
  }

  /**
   * Imports a language library chosen by the user.
   */
  public void importLanguageLibrary() {
    getView().invokeLater(new Runnable() {
        public void run() {
          final String languageLibraryName = getView().showImportLanguageLibraryDialog();
          if (languageLibraryName != null) {
            importLanguageLibrary(languageLibraryName);
          }
        }
      });
  }

  /**
   * Imports a given language library.
   */
  public void importLanguageLibrary(String languageLibraryName) {
    try {
      if (!this.preferences.languageLibraryExists(languageLibraryName)
          || getView().confirmReplaceLanguageLibrary(languageLibraryName)) {
        this.preferences.addLanguageLibrary(languageLibraryName);
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class,
          "importLanguageLibraryError", languageLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Imports furniture to the catalog or home depending on the focused view.
   */
  public void importFurniture() {
    // Always use selection mode after an import furniture operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      getFurnitureCatalogController().importFurniture();
    } else {
      getFurnitureController().importFurniture();
    }
  }

  /**
   * Imports a furniture library chosen by the user.
   */
  public void importFurnitureLibrary() {
    getView().invokeLater(new Runnable() {
        public void run() {
          final String furnitureLibraryName = getView().showImportFurnitureLibraryDialog();
          if (furnitureLibraryName != null) {
            importFurnitureLibrary(furnitureLibraryName);
          }
        }
      });
  }

  /**
   * Imports a given furniture library.
   */
  public void importFurnitureLibrary(String furnitureLibraryName) {
    try {
      if (!this.preferences.furnitureLibraryExists(furnitureLibraryName)
          || getView().confirmReplaceFurnitureLibrary(furnitureLibraryName)) {
        this.preferences.addFurnitureLibrary(furnitureLibraryName);
        getView().showMessage(this.preferences.getLocalizedString(HomeController.class, "importedFurnitureLibraryMessage",
            this.contentManager.getPresentationName(furnitureLibraryName, ContentManager.ContentType.FURNITURE_LIBRARY)));
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class,
          "importFurnitureLibraryError", furnitureLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Imports a texture to the texture catalog.
   * @since 4.0
   */
  public void importTexture() {
    new ImportedTextureWizardController(this.preferences,
        this.viewFactory, this.contentManager).displayView(getView());
  }

  /**
   * Imports a textures library chosen by the user.
   */
  public void importTexturesLibrary() {
    getView().invokeLater(new Runnable() {
        public void run() {
          final String texturesLibraryName = getView().showImportTexturesLibraryDialog();
          if (texturesLibraryName != null) {
            importTexturesLibrary(texturesLibraryName);
          }
        }
      });
  }

  /**
   * Imports a given textures library.
   */
  public void importTexturesLibrary(String texturesLibraryName) {
    try {
      if (!this.preferences.texturesLibraryExists(texturesLibraryName)
          || getView().confirmReplaceTexturesLibrary(texturesLibraryName)) {
        this.preferences.addTexturesLibrary(texturesLibraryName);
        getView().showMessage(this.preferences.getLocalizedString(HomeController.class, "importedTexturesLibraryMessage",
            this.contentManager.getPresentationName(texturesLibraryName, ContentManager.ContentType.TEXTURES_LIBRARY)));
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class,
          "importTexturesLibraryError", texturesLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Undoes last operation.
   */
  public void undo() {
    this.undoManager.undo();
    HomeView view = getView();
    boolean moreUndo = this.undoManager.canUndo();
    view.setEnabled(HomeView.ActionType.UNDO, moreUndo);
    view.setEnabled(HomeView.ActionType.REDO, true);
    if (moreUndo) {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(),
          this.undoManager.getRedoPresentationName());
    } else {
      view.setUndoRedoName(null, this.undoManager.getRedoPresentationName());
    }
    this.saveUndoLevel--;
    this.home.setModified(this.saveUndoLevel != 0 || this.notUndoableModifications);
  }

  /**
   * Redoes last undone operation.
   */
  public void redo() {
    this.undoManager.redo();
    HomeView view = getView();
    boolean moreRedo = this.undoManager.canRedo();
    view.setEnabled(HomeView.ActionType.UNDO, true);
    view.setEnabled(HomeView.ActionType.REDO, moreRedo);
    if (moreRedo) {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(),
          this.undoManager.getRedoPresentationName());
    } else {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(), null);
    }
    this.saveUndoLevel++;
    this.home.setModified(this.saveUndoLevel != 0 || this.notUndoableModifications);
  }

  /**
   * Deletes items and post a cut operation to undo support.
   */
  public void cut(List<? extends Selectable> items) {
    // Start a compound edit that deletes items and changes presentation name
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    getPlanController().deleteItems(items);
    // Add a undoable edit to change presentation name
    undoSupport.postEdit(new AbstractUndoableEdit() {
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(HomeController.class, "undoCutName");
        }
      });
    // End compound edit
    undoSupport.endUpdate();
  }

  /**
   * Adds items to home and posts a paste operation to undo support.
   */
  public void paste(final List<? extends Selectable> items) {
    // Check if pasted items and currently selected items overlap
    List<Selectable> selectedItems = this.home.getSelectedItems();
    float pastedItemsDelta = 0;
    if (items.size() == selectedItems.size()) {
      // The default delta used to be able to distinguish dropped items from previous selection
      pastedItemsDelta = 20;
      for (Selectable pastedItem : items) {
        // Search which item of selected items it may overlap
        float [][] pastedItemPoints = pastedItem.getPoints();
        boolean pastedItemOverlapSelectedItem = false;
        for (Selectable selectedItem : selectedItems) {
          if (Arrays.deepEquals(pastedItemPoints, selectedItem.getPoints())) {
            pastedItemOverlapSelectedItem = true;
            break;
          }
        }
        if (!pastedItemOverlapSelectedItem) {
          pastedItemsDelta = 0;
          break;
        }
      }
    }
    addPastedItems(items, pastedItemsDelta, pastedItemsDelta, false, "undoPasteName");
  }

  /**
   * Adds items to home, moves them of (dx, dy)
   * and posts a drop operation to undo support.
   */
  public void drop(final List<? extends Selectable> items, float dx, float dy) {
    drop(items, null, dx, dy);
  }

  /**
   * Adds items to home, moves them of (dx, dy)
   * and posts a drop operation to undo support.
   */
  public void drop(final List<? extends Selectable> items, View destinationView, float dx, float dy) {
    addPastedItems(items, dx, dy, destinationView == getPlanController().getView(), "undoDropName");
  }

  /**
   * Adds items to home.
   */
  private void addPastedItems(List<? extends Selectable> items,
                              float dx, float dy, final boolean isDropInPlanView,
                              final String presentationNameKey) {
    if (items.size() > 1
        || (items.size() == 1
            && !(items.get(0) instanceof Compass))) {
      // Remove Compass instance from copied items
      List<Compass> compassList = Home.getSubList(items, Compass.class);
      if (compassList.size() != 0) {
        items = new ArrayList<Selectable>(items);
        items.removeAll(compassList);
      }
      // Always use selection mode after a drop or a paste operation
      getPlanController().setMode(PlanController.Mode.SELECTION);
      // Start a compound edit that adds walls, furniture, rooms, dimension lines, polylines and labels to home
      UndoableEditSupport undoSupport = getUndoableEditSupport();
      undoSupport.beginUpdate();
      getPlanController().addItems(items);
      List<HomePieceOfFurniture> addedFurniture = Home.getFurnitureSubList(items);
      adjustFurnitureSizeAndElevation(addedFurniture, dx == 0 && dy == 0);
      getPlanController().moveItems(items, dx, dy);
      if (isDropInPlanView
          && this.preferences.isMagnetismEnabled()
          && items.size() == 1
          && addedFurniture.size() == 1) {
        // Adjust piece when it's dropped in plan view
        getPlanController().adjustMagnetizedPieceOfFurniture((HomePieceOfFurniture)items.get(0), dx, dy);
      }
      undoSupport.postEdit(new AbstractUndoableEdit() {
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(HomeController.class, presentationNameKey);
          }
        });

      // End compound edit
      undoSupport.endUpdate();
    }
  }

  /**
   * Adjusts furniture size and elevation if magnetism is enabled.
   * This method should be called after the given furniture is added to the plan,
   * to ensure its size in plan is adjusted too.
   */
  private void adjustFurnitureSizeAndElevation(List<HomePieceOfFurniture> furniture, boolean keepDoorsAndWindowDepth) {
    if (this.preferences.isMagnetismEnabled()) {
      for (HomePieceOfFurniture piece : furniture) {
        if (!(piece instanceof HomeFurnitureGroup)
            && piece.isResizable()) {
          piece.setWidth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getWidth(), 0.1f));
          // Don't adjust depth of doors or windows otherwise they may be misplaced in a wall
          if (!(piece instanceof HomeDoorOrWindow) || !keepDoorsAndWindowDepth) {
            piece.setDepth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getDepth(), 0.1f));
          }
          piece.setHeight(this.preferences.getLengthUnit().getMagnetizedLength(piece.getHeight(), 0.1f));
        }
        piece.setElevation(this.preferences.getLengthUnit().getMagnetizedLength(piece.getElevation(), 0.1f));
      }
    }
  }

  /**
   * Adds imported models to home, moves them of (dx, dy)
   * and post a drop operation to undo support.
   */
  public void dropFiles(final List<String> importableModels, float dx, float dy) {
    // Always use selection mode after a drop operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
    // Add to home a listener to track imported furniture
    final List<HomePieceOfFurniture> importedFurniture =
        new ArrayList<HomePieceOfFurniture>(importableModels.size());
    CollectionListener<HomePieceOfFurniture> addedFurnitureListener =
        new CollectionListener<HomePieceOfFurniture>() {
          public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
            importedFurniture.add(ev.getItem());
          }
        };
    this.home.addFurnitureListener(addedFurnitureListener);

    // Start a compound edit that adds furniture to home
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    // Import furniture
    for (String model : importableModels) {
      getFurnitureController().importFurniture(model);
    }
    this.home.removeFurnitureListener(addedFurnitureListener);

    if (importedFurniture.size() > 0) {
      getPlanController().moveItems(importedFurniture, dx, dy);
      this.home.setSelectedItems(importedFurniture);

      // Add a undoable edit that will select the imported furniture at redo
      undoSupport.postEdit(new AbstractUndoableEdit() {
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(importedFurniture);
          }

          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(HomeController.class, "undoDropName");
          }
        });
    }

    // End compound edit
    undoSupport.endUpdate();
  }

  /**
   * Paste the furniture in clipboard to the selected group in home.
   * @since 5.0
   */
  public void pasteToGroup() {
    // Start a compound edit that adds furniture
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    List<HomePieceOfFurniture> addedFurniture = Home.getFurnitureSubList(getView().getClipboardItems());
    getFurnitureController().addFurnitureToGroup(addedFurniture,
        (HomeFurnitureGroup)this.home.getSelectedItems().get(0));
    adjustFurnitureSizeAndElevation(addedFurniture, true);
    undoSupport.postEdit(new AbstractUndoableEdit() {
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(HomeController.class, "undoPasteToGroupName");
        }
      });

    // End compound edit
    undoSupport.endUpdate();
  }

  /**
   * Paste the style of the item in clipboard on selected items compatible with it.
   * @since 5.0
   */
  public void pasteStyle() {
    // Start a compound edit that modifies items with their controller
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    Selectable clipboardItem = getView().getClipboardItems().get(0);
    final List<Selectable> selectedItems = this.home.getSelectedItems();
    if (clipboardItem instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture clipboardPiece = (HomePieceOfFurniture)clipboardItem;
      HomeFurnitureController furnitureController = new HomeFurnitureController(
          this.home, this.preferences, this.viewFactory, this.contentManager, undoSupport);
      HomeMaterial [] materials = clipboardPiece.getModelMaterials();
      if (materials != null) {
        furnitureController.getModelMaterialsController().setMaterials(clipboardPiece.getModelMaterials());
        furnitureController.setPaint(HomeFurnitureController.FurniturePaint.MODEL_MATERIALS);
      } else if (clipboardPiece.getTexture() != null) {
        furnitureController.getTextureController().setTexture(clipboardPiece.getTexture());
        furnitureController.setPaint(HomeFurnitureController.FurniturePaint.TEXTURED);
      } else if (clipboardPiece.getColor() != null) {
        furnitureController.setColor(clipboardPiece.getColor());
        furnitureController.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
      } else {
        furnitureController.setPaint(HomeFurnitureController.FurniturePaint.DEFAULT);
      }
      Float shininess = clipboardPiece.getShininess();
      furnitureController.setShininess(shininess == null
          ? HomeFurnitureController.FurnitureShininess.DEFAULT
          : (shininess.floatValue() == 0
              ? HomeFurnitureController.FurnitureShininess.MATT
              : HomeFurnitureController.FurnitureShininess.SHINY));
      furnitureController.modifyFurniture();
    } else if (clipboardItem instanceof Wall) {
      Wall clipboardWall = (Wall)clipboardItem;
      WallController wallController = new WallController(this.home, this.preferences, this.viewFactory, this.contentManager, undoSupport);
      if (clipboardWall.getLeftSideColor() != null) {
        wallController.setLeftSideColor(clipboardWall.getLeftSideColor());
        wallController.setLeftSidePaint(WallController.WallPaint.COLORED);
      } else if (clipboardWall.getLeftSideTexture() != null) {
        wallController.getLeftSideTextureController().setTexture(clipboardWall.getLeftSideTexture());
        wallController.setLeftSidePaint(WallController.WallPaint.TEXTURED);
      } else {
        wallController.setLeftSidePaint(WallController.WallPaint.DEFAULT);
      }
      wallController.setLeftSideShininess(clipboardWall.getLeftSideShininess());
      wallController.getLeftSideBaseboardController().setBaseboard(clipboardWall.getLeftSideBaseboard());
      if (clipboardWall.getRightSideColor() != null) {
        wallController.setRightSideColor(clipboardWall.getRightSideColor());
        wallController.setRightSidePaint(WallController.WallPaint.COLORED);
      } else if (clipboardWall.getRightSideTexture() != null) {
        wallController.getRightSideTextureController().setTexture(clipboardWall.getRightSideTexture());
        wallController.setRightSidePaint(WallController.WallPaint.TEXTURED);
      } else {
        wallController.setRightSidePaint(WallController.WallPaint.DEFAULT);
      }
      wallController.setRightSideShininess(clipboardWall.getRightSideShininess());
      wallController.getRightSideBaseboardController().setBaseboard(clipboardWall.getRightSideBaseboard());
      wallController.setPattern(clipboardWall.getPattern());
      wallController.setTopColor(clipboardWall.getTopColor());
      wallController.setTopPaint(clipboardWall.getTopColor() != null
          ? WallController.WallPaint.COLORED
          : WallController.WallPaint.DEFAULT);
      wallController.modifyWalls();
    } else if (clipboardItem instanceof Room) {
      Room clipboardRoom = (Room)clipboardItem;
      RoomController roomController = new RoomController(this.home, this.preferences, this.viewFactory, this.contentManager, undoSupport);
      if (clipboardRoom.getFloorColor() != null) {
        roomController.setFloorColor(clipboardRoom.getFloorColor());
        roomController.setFloorPaint(RoomController.RoomPaint.COLORED);
      } else if (clipboardRoom.getFloorTexture() != null) {
        roomController.getFloorTextureController().setTexture(clipboardRoom.getFloorTexture());
        roomController.setFloorPaint(RoomController.RoomPaint.TEXTURED);
      } else {
        roomController.setFloorPaint(RoomController.RoomPaint.DEFAULT);
      }
      roomController.setFloorShininess(clipboardRoom.getFloorShininess());
      if (clipboardRoom.getCeilingColor() != null) {
        roomController.setCeilingColor(clipboardRoom.getCeilingColor());
        roomController.setCeilingPaint(RoomController.RoomPaint.COLORED);
      } else if (clipboardRoom.getCeilingTexture() != null) {
        roomController.getCeilingTextureController().setTexture(clipboardRoom.getCeilingTexture());
        roomController.setCeilingPaint(RoomController.RoomPaint.TEXTURED);
      } else {
        roomController.setCeilingPaint(RoomController.RoomPaint.DEFAULT);
      }
      roomController.setCeilingShininess(clipboardRoom.getCeilingShininess());
      roomController.modifyRooms();
    } else if (clipboardItem instanceof Polyline) {
      Polyline clipboardPolyline = (Polyline)clipboardItem;
      PolylineController polylineController = new PolylineController(
          this.home, this.preferences, this.viewFactory, this.contentManager, undoSupport);
      polylineController.setThickness(clipboardPolyline.getThickness());
      polylineController.setJoinStyle(clipboardPolyline.getJoinStyle());
      polylineController.setCapStyle(clipboardPolyline.getCapStyle());
      polylineController.setStartArrowStyle(clipboardPolyline.getStartArrowStyle());
      polylineController.setEndArrowStyle(clipboardPolyline.getEndArrowStyle());
      polylineController.setDashStyle(clipboardPolyline.getDashStyle());
      polylineController.setDashOffset(clipboardPolyline.getDashOffset());
      polylineController.setColor(clipboardPolyline.getColor());
      polylineController.modifyPolylines();
    } else if (clipboardItem instanceof Label) {
      Label clipboardLabel = (Label)clipboardItem;
      LabelController labelController = new LabelController(this.home, this.preferences, this.viewFactory, undoSupport);
      labelController.setColor(clipboardLabel.getColor());
      TextStyle labelStyle = clipboardLabel.getStyle();
      if (labelStyle != null) {
        labelController.setFontName(labelStyle.getFontName());
        labelController.setFontSize(labelStyle.getFontSize());
      } else {
        labelController.setFontName(null);
        labelController.setFontSize(this.preferences.getDefaultTextStyle(Label.class).getFontSize());
      }
      labelController.modifyLabels();
    }

    // Add a undoable edit to change presentation name
    undoSupport.postEdit(new AbstractUndoableEdit() {
        @Override
        public void redo() throws CannotRedoException {
          home.setSelectedItems(selectedItems);
          super.redo();
        }

        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(HomeController.class, "undoPasteStyleName");
        }
      });
    // End compound edit
    undoSupport.endUpdate();
  }

  /**
   * Returns the transfer data matching the requested types.
   */
   public void createTransferData(final TransferableView.TransferObserver observer,
                                  final TransferableView.DataType ... dataTypes) {
     final List<Object> data = new ArrayList<Object>();
     for (int i = 0; i < dataTypes.length; i++) {
       if (this.childControllers == null) {
         this.childControllers = new ArrayList<Controller>();
         this.childControllers.add(getFurnitureCatalogController());
         this.childControllers.add(getFurnitureController());
         this.childControllers.add(getPlanController());
         this.childControllers.add(getHomeController3D());
       }
       for (Controller childController : this.childControllers) {
         if (childController.getView() instanceof TransferableView) {
           data.add(((TransferableView)childController.getView()).createTransferData(dataTypes [i]));
         }
       }
     }
     observer.dataReady(data.toArray());
   }

  /**
   * Deletes the selection in the focused component.
   */
  public void delete() {
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      if (getView().confirmDeleteCatalogSelection()) {
        getFurnitureCatalogController().deleteSelection();
      }
    } else if (this.focusedView == getFurnitureController().getView()) {
      getFurnitureController().deleteSelection();
    } else if (this.focusedView == getPlanController().getView()) {
      getPlanController().deleteSelection();
    }
  }

  /**
   * Updates actions when focused view changed.
   */
  public void focusedViewChanged(View focusedView) {
    this.focusedView = focusedView;
    enableActionsBoundToSelection();
    enablePasteAction();
    enablePasteToGroupAction();
    enablePasteStyleAction();
    enableSelectAllAction();
  }

  /**
   * Selects everything in the focused component.
   */
  public void selectAll() {
    if (this.focusedView == getFurnitureController().getView()) {
      getFurnitureController().selectAll();
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      getPlanController().selectAll();
    }
  }

  /**
   * Creates a new home and adds it to application home list.
   */
  public void newHome() {
    Home home;
    if (this.application != null) {
      home = this.application.createHome();
    } else {
      home = new Home(this.preferences.getNewWallHeight());
    }
    this.application.addHome(home);
  }

  /**
   * Creates a new home from an example chosen by the user.
   */
  public void newHomeFromExample() {
    final String exampleName = getView().showNewHomeFromExampleDialog();
    if (exampleName != null) {
      // Read home in a threaded task
      Callable<Void> openTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              // Read home with application recorder
              Home openedHome = application.getHomeRecorder().readHome(exampleName);
              // Reset furniture names to their catalog one to simulate translation
              final Map<String, String> furnitureNames = getCatalogFurnitureNames(preferences.getFurnitureCatalog());
              String groupName = preferences.getLocalizedString(HomeController.class, "defaultGroupName");
              for (HomePieceOfFurniture piece : openedHome.getFurniture()) {
                renameToCatalogName(piece, furnitureNames, groupName);
              }
              openedHome.setName(null);
              addHomeToApplication(openedHome);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                ex.printStackTrace();
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(HomeController.class, 
                      "openError", exampleName, ex);
                  getView().showError(message);
                }
              }
            }
          };
      new ThreadedTaskController(openTask,
          this.preferences.getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Returns a map with entries containing furniture name associated to their id.
   */
  private Map<String, String> getCatalogFurnitureNames(FurnitureCatalog catalog) {
    Map<String, String> furnitureNames = new HashMap<String, String>();
    for (FurnitureCategory category : catalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        if (piece.getId() != null) {
          furnitureNames.put(piece.getId(), piece.getName());
        }
      }
    }
    return furnitureNames;
  }

  /**
   * Renames the given <code>piece</code> from the piece name with the same id in <code>furnitureNames</code>.
   */
  private void renameToCatalogName(HomePieceOfFurniture piece,
                                   Map<String, String> furnitureNames,
                                   String groupName) {
    if (piece instanceof HomeFurnitureGroup) {
      piece.setName(groupName);
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        renameToCatalogName(groupPiece, furnitureNames, groupName);
      }
    } else {
      String id = piece.getCatalogId();
      if (id != null) {
        piece.setName(furnitureNames.get(id));
      }
    }
  }

  /**
   * Opens a home. This method displays an {@link HomeView#showOpenDialog() open dialog}
   * in view, reads the home from the chosen name and adds it to application home list.
   */
  public void open() {
    getView().invokeLater(new Runnable() {
      public void run() {
        final String homeName = getView().showOpenDialog();
        if (homeName != null) {
          open(homeName);
        }
      }
    });
  }

  /**
   * Opens a given <code>homeName</code>home.
   */
  public void open(final String homeName) {
    // Check if requested home isn't already opened
    for (Home home : this.application.getHomes()) {
      if (homeName.equals(home.getName())) {
        String message = this.preferences.getLocalizedString(
            HomeController.class, "alreadyOpen", homeName);
        getView().showMessage(message);
        return;
      }
    }

    // Read home in a threaded task
    Callable<Void> openTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            // Read home with application recorder
            Home openedHome = application.getHomeRecorder().readHome(homeName);
            openedHome.setName(homeName);
            addHomeToApplication(openedHome);
            if (openedHome.isRepaired()) {
              getView().invokeLater(new Runnable() {
                  public void run() {
                    String message = preferences.getLocalizedString(HomeController.class, "openRepairedHomeMessage", homeName);
                    getView().showMessage(message);
                  }
                });
            }
            return null;
          }
        };
    ThreadedTaskController.ExceptionHandler exceptionHandler =
        new ThreadedTaskController.ExceptionHandler() {
          public void handleException(Exception ex) {
            if (!(ex instanceof InterruptedRecorderException)) {
              if (ex instanceof DamagedHomeRecorderException) {
                DamagedHomeRecorderException ex2 = (DamagedHomeRecorderException)ex;
                openDamagedHome(homeName, ex2.getDamagedHome(), ex2.getInvalidContent());
              } else {
                ex.printStackTrace();
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(HomeController.class, 
                      "openError", homeName, ex);
                  getView().showError(message);
                }
              }
            }
          }
        };
    new ThreadedTaskController(openTask,
        this.preferences.getLocalizedString(HomeController.class, "openMessage"), exceptionHandler,
        this.preferences, this.viewFactory).executeTask(getView());
  }

  /**
   * Adds the given home to application.
   */
  private void addHomeToApplication(final Home home) {
    getView().invokeLater(new Runnable() {
        public void run() {
          application.addHome(home);
        }
      });
  }

  /**
   * Prompts the user to choose an option to open the given damaged home,
   * fixes the damaged home accordingly and shows it.
   */
  private void openDamagedHome(final String homeName, Home damagedHome, List<Content> invalidContent) {
    HomeView.OpenDamagedHomeAnswer answer = getView().confirmOpenDamagedHome(
        homeName, damagedHome, invalidContent);
    switch (answer) {
      case REMOVE_DAMAGED_ITEMS:
        removeDamagedItems(damagedHome, invalidContent);
        break;
      case REPLACE_DAMAGED_ITEMS:
        replaceDamagedItems(damagedHome, invalidContent);
        break;
    }
    if (answer != HomeView.OpenDamagedHomeAnswer.DO_NOT_OPEN_HOME) {
      damagedHome.setName(homeName);
      damagedHome.setRepaired(true);
      addHomeToApplication(damagedHome);
    }
  }

  /**
   * Removes from the given <code>home</code> all the objects that reference the invalid content.
   */
  private void removeDamagedItems(Home home, List<Content> invalidContent) {
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (referencesInvalidContent(piece, invalidContent)) {
        home.deletePieceOfFurniture(piece);
      } else {
        removeInvalidTextures(piece, invalidContent);
      }
    }
    for (Wall wall : home.getWalls()) {
      if (referencesInvalidContent(wall.getLeftSideTexture(), invalidContent)) {
        wall.setLeftSideTexture(null);
      }
      if (referencesInvalidContent(wall.getRightSideTexture(), invalidContent)) {
        wall.setRightSideTexture(null);
      }
    }
    for (Room room : home.getRooms()) {
      if (referencesInvalidContent(room.getFloorTexture(), invalidContent)) {
        room.setFloorTexture(null);
      }
      if (referencesInvalidContent(room.getCeilingTexture(), invalidContent)) {
        room.setCeilingTexture(null);
      }
    }
    HomeEnvironment environment = home.getEnvironment();
    if (referencesInvalidContent(environment.getGroundTexture(), invalidContent)) {
      environment.setGroundTexture(null);
    }
    if (referencesInvalidContent(environment.getSkyTexture(), invalidContent)) {
      environment.setSkyTexture(null);
    }
    BackgroundImage backgroundImage = home.getBackgroundImage();
    if (backgroundImage != null && invalidContent.contains(backgroundImage.getImage())) {
      home.setBackgroundImage(null);
    }
    for (Level level : home.getLevels()) {
      backgroundImage = level.getBackgroundImage();
      if (backgroundImage != null && invalidContent.contains(backgroundImage.getImage())) {
        level.setBackgroundImage(null);
      }
    }
  }

  /**
   * Returns <code>true</code> if the model of the given <code>piece</code> and its icons are not valid.
   */
  private boolean referencesInvalidContent(HomePieceOfFurniture piece, List<Content> invalidContent) {
    if (invalidContent.contains(piece.getIcon())
        || invalidContent.contains(piece.getPlanIcon())
        || invalidContent.contains(piece.getModel())) {
      return true;
    } else if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        if (referencesInvalidContent(groupPiece, invalidContent)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Sets to <code>null</code> the invalid textures used by the given <code>piece</code>.
   */
  private void removeInvalidTextures(HomePieceOfFurniture piece, List<Content> invalidContent) {
    if (referencesInvalidContent(piece.getTexture(), invalidContent)) {
      piece.setTexture(null);
    }
    HomeMaterial [] materials = piece.getModelMaterials();
    if (materials != null) {
      for (int i = 0; i < materials.length; i++) {
        if (materials [i] != null
            && referencesInvalidContent(materials [i].getTexture(), invalidContent)) {
          materials [i] = null;
        }
        piece.setModelMaterials(materials);
      }
    }
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        removeInvalidTextures(groupPiece, invalidContent);
      }
    }
  }

  /**
   * Returns <code>true</code> if the given <code>texture</code> is not valid.
   */
  private boolean referencesInvalidContent(TextureImage texture, List<Content> invalidContent) {
    return texture != null && invalidContent.contains(texture.getImage());
  }

  /**
   * Replaces all the objects that reference an invalid content in the given <code>home</code>.
   */
  private void replaceDamagedItems(Home home, List<Content> invalidContent) {
    List<HomePieceOfFurniture> furniture = home.getFurniture();
    for (int i = furniture.size() - 1; i >= 0; i--) {
      HomePieceOfFurniture piece = furniture.get(i);
      if (referencesInvalidContent(piece, invalidContent)) {
        HomePieceOfFurniture replacingPiece = getFurnitureController().createHomePieceOfFurniture(
            new CatalogPieceOfFurniture(piece.getCatalogId(), piece.getName(), piece.getDescription(),
                piece.getInformation(), new String [0], null, null,
                REPAIRED_ICON_CONTENT, REPAIRED_IMAGE_CONTENT, REPAIRED_MODEL_CONTENT,
                piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.getElevation(), 1f,
                piece.isMovable(), piece.getStaircaseCutOutShape(), null, false, null, piece.getCreator(),
                piece.isResizable(), piece.isDeformable(), piece.isTexturable(), piece.isHorizontallyRotatable(),
                piece.getPrice(), piece.getValueAddedTaxPercentage(), piece.getCurrency()));
        replacingPiece.setNameVisible(piece.isNameVisible());
        replacingPiece.setNameXOffset(piece.getNameXOffset());
        replacingPiece.setNameYOffset(piece.getNameYOffset());
        replacingPiece.setNameStyle(piece.getNameStyle());
        replacingPiece.setVisible(piece.isVisible());
        replacingPiece.setAngle(piece.getAngle());
        replacingPiece.setX(piece.getX());
        replacingPiece.setY(piece.getY());
        home.addPieceOfFurniture(replacingPiece, i);
        if (replacingPiece.isHorizontallyRotatable()) {
          replacingPiece.setPitch(piece.getPitch());
          replacingPiece.setRoll(piece.getRoll());
        }
        replacingPiece.setLevel(piece.getLevel());
        home.deletePieceOfFurniture(piece);
      } else {
        replaceInvalidTextures(piece, invalidContent);
      }
    }
    for (Wall wall : home.getWalls()) {
      if (referencesInvalidContent(wall.getLeftSideTexture(), invalidContent)) {
        wall.setLeftSideTexture(getErrorTexture(wall.getLeftSideTexture()));
      }
      if (referencesInvalidContent(wall.getRightSideTexture(), invalidContent)) {
        wall.setRightSideTexture(getErrorTexture(wall.getRightSideTexture()));
      }
    }
    for (Room room : home.getRooms()) {
      if (referencesInvalidContent(room.getFloorTexture(), invalidContent)) {
        room.setFloorTexture(getErrorTexture(room.getFloorTexture()));
      }
      if (referencesInvalidContent(room.getCeilingTexture(), invalidContent)) {
        room.setCeilingTexture(getErrorTexture(room.getCeilingTexture()));
      }
    }
    HomeEnvironment environment = home.getEnvironment();
    if (referencesInvalidContent(environment.getGroundTexture(), invalidContent)) {
      environment.setGroundTexture(getErrorTexture(environment.getGroundTexture()));
    }
    if (referencesInvalidContent(environment.getSkyTexture(), invalidContent)) {
      environment.setSkyTexture(getErrorTexture(environment.getSkyTexture()));
    }
    BackgroundImage backgroundImage = home.getBackgroundImage();
    if (backgroundImage != null && invalidContent.contains(backgroundImage.getImage())) {
      home.setBackgroundImage(getErrorBackgroundImage(backgroundImage));
    }
    for (Level level : home.getLevels()) {
      backgroundImage = level.getBackgroundImage();
      if (backgroundImage != null && invalidContent.contains(backgroundImage.getImage())) {
        level.setBackgroundImage(getErrorBackgroundImage(backgroundImage));
      }
    }
  }

  /**
   * Replaces the invalid textures used by the given <code>piece</code>.
   */
  private void replaceInvalidTextures(HomePieceOfFurniture piece, List<Content> invalidContent) {
    if (referencesInvalidContent(piece.getTexture(), invalidContent)) {
      piece.setTexture(getErrorTexture(piece.getTexture()));
    }
    HomeMaterial [] materials = piece.getModelMaterials();
    if (materials != null) {
      for (int i = 0; i < materials.length; i++) {
        HomeMaterial material = materials [i];
        if (material != null
            && referencesInvalidContent(material.getTexture(), invalidContent)) {
          materials [i] = new HomeMaterial(material.getName(), material.getColor(),
              getErrorTexture(material.getTexture()), material.getShininess());
        }
        piece.setModelMaterials(materials);
      }
    }
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        replaceInvalidTextures(groupPiece, invalidContent);
      }
    }
  }

  /**
   * Returns a texture referencing a correct image.
   */
  private HomeTexture getErrorTexture(HomeTexture texture) {
    return new HomeTexture(new CatalogTexture(texture.getName(),
        REPAIRED_IMAGE_CONTENT, texture.getWidth(), texture.getHeight()));
  }

  /**
   * Returns a background image referencing a correct image.
   */
  private BackgroundImage getErrorBackgroundImage(BackgroundImage image) {
    return new BackgroundImage(REPAIRED_IMAGE_CONTENT,
        image.getScaleDistance(), image.getScaleDistanceXStart(), image.getScaleDistanceYStart(),
        image.getScaleDistanceXEnd(), image.getScaleDistanceYEnd(),
        image.getXOrigin(), image.getYOrigin(), image.isVisible());
  }

  /**
   * Updates user preferences <code>recentHomes</code> and write preferences.
   */
  private void updateUserPreferencesRecentHomes(List<String> recentHomes) {
    if (this.application != null) {
      // Check every recent home exists
      for (int i = recentHomes.size() - 1; i >= 0; i--) {
        try {
          if (!this.application.getHomeRecorder().exists(recentHomes.get(i))) {
            recentHomes.remove(i);
          }
        } catch (RecorderException ex) {
          // If homeName can't be checked ignore it
        }
      }
      this.preferences.setRecentHomes(recentHomes);
    }
  }

  /**
   * Returns a list of displayable recent homes.
   */
  public List<String> getRecentHomes() {
    if (this.application != null) {
      List<String> recentHomes = new ArrayList<String>();
      for (String homeName : this.preferences.getRecentHomes()) {
        try {
          if (this.application.getHomeRecorder().exists(homeName)) {
            recentHomes.add(homeName);
            if (recentHomes.size() == this.preferences.getRecentHomesMaxCount()) {
              break;
            }
          }
        } catch (RecorderException ex) {
          // If homeName can't be checked ignore it
        }
      }
      getView().setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES,
          !recentHomes.isEmpty());
      return Collections.unmodifiableList(recentHomes);
    } else {
      return new ArrayList<String>();
    }
  }

  /**
   * Returns the version of the application for display purpose.
   */
  public String getVersion() {
    if (this.application != null) {
      String applicationVersion = this.application.getVersion();
      try {
        String deploymentInformation = System.getProperty("com.eteks.sweethome3d.deploymentInformation");
        if (deploymentInformation != null) {
          applicationVersion += " " + deploymentInformation;
        }
      } catch (AccessControlException ex) {
        // Ignore com.eteks.sweethome3d.deploymentInformation property since it can't be read
      }
      return applicationVersion;
    } else {
      return "";
    }
  }

  /**
   * Deletes the list of recent homes in user preferences.
   */
  public void deleteRecentHomes() {
    updateUserPreferencesRecentHomes(new ArrayList<String>());
    getView().setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES, false);
  }

  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomeView#confirmSave(String) confirm}
   * in view whether home should be saved. Once home is actually saved,
   * home is removed from application homes list.
   */
  public void close() {
    close(null);
  }


  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomeView#confirmSave(String) confirm}
   * in view whether home should be saved. Once home is actually saved,
   * home is removed from application homes list and <code>postCloseTask</code>
   * is called if it's not <code>null</code>.
   * @since 5.0
   */
  public void close(final Runnable postCloseTask) {
    // Create a task that deletes home and run postCloseTask
    Runnable closeTask = new Runnable() {
        public void run() {
          home.setRecovered(false);
          application.deleteHome(home);
          if (postCloseTask != null) {
            postCloseTask.run();
          }
        }
      };

    if (this.home.isModified() || this.home.isRecovered() || this.home.isRepaired()) {
      switch (getView().confirmSave(this.home.getName())) {
        case SAVE   : save(HomeRecorder.Type.DEFAULT, closeTask); // Falls through
        case CANCEL : return;
      }
    }
    closeTask.run();
  }

  /**
   * Saves the home managed by this controller. If home name doesn't exist,
   * this method will act as {@link #saveAs() saveAs} method.
   */
  public void save() {
    save(HomeRecorder.Type.DEFAULT, null);
  }

  /**
   * Saves the home managed by this controller and executes <code>postSaveTask</code>
   * if it's not <code>null</code>.
   */
  private void save(HomeRecorder.Type recorderType, Runnable postSaveTask) {
    if (this.home.getName() == null
        || this.home.isRepaired()) {
      saveAs(recorderType, postSaveTask);
    } else {
      save(this.home.getName(), recorderType, postSaveTask);
    }
  }

  /**
   * Saves the home managed by this controller with a different name.
   * This method displays a {@link HomeView#showSaveDialog(String) save dialog} in  view,
   * and saves home with the chosen name if any.
   */
  public void saveAs() {
    saveAs(HomeRecorder.Type.DEFAULT, null);
  }

  /**
   * Saves the home managed by this controller with a different name.
   * Once home is actually saved, home is removed from application homes list
   * and <code>postCloseTask</code> is called if it's not <code>null</code>.
   * @since 4.4
   */
  protected void saveAs(HomeRecorder.Type recorderType, Runnable postSaveTask) {
    String newName = getView().showSaveDialog(this.home.getName());
    if (newName != null) {
      save(newName, recorderType, postSaveTask);
    }
  }

  /**
   * Saves the home managed by this controller and compresses it. If home name doesn't exist,
   * this method will prompt user to choose a home name.
   */
  public void saveAndCompress() {
    save(HomeRecorder.Type.COMPRESSED, null);
  }

  /**
   * Saves the home managed by this controller with a different name and compresses it.
   * This method displays a {@link HomeView#showSaveDialog(String) save dialog} in  view,
   * and saves home with the chosen name if any.
   * @since 4.2
   */
  public void saveAsAndCompress() {
    saveAs(HomeRecorder.Type.COMPRESSED, null);
  }

  /**
   * Actually saves the home managed by this controller and executes <code>postSaveTask</code>
   * if it's not <code>null</code>.
   */
  private void save(final String homeName,
                    final HomeRecorder.Type recorderType,
                    final Runnable postSaveTask) {
    // If home version is older than current version
    // or if home name is changed
    // or if user confirms to save a home created with a newer version
    if (this.home.getVersion() <= Home.CURRENT_VERSION
        || !homeName.equals(this.home.getName())
        || getView().confirmSaveNewerHome(homeName)) {
      final Home savedHome;
      try {
        // Clone home to save it safely in a threaded task
        savedHome = this.home.clone();
      } catch (RuntimeException ex) {
        // If home data is corrupted some way and couldn't be cloned
        // warn the user his home couldn't be saved
        getView().showError(preferences.getLocalizedString(
            HomeController.class, "saveError", homeName, ex));
        throw ex;
      }
      Callable<Void> saveTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              savedHome.setName(contentManager.getPresentationName(homeName, ContentManager.ContentType.SWEET_HOME_3D));
              // Write home with application recorder
              application.getHomeRecorder(recorderType).writeHome(savedHome, homeName);
              updateSavedHome(homeName, savedHome.getVersion(), postSaveTask);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                String cause = ex.toString();
                if (ex instanceof NotEnoughSpaceRecorderException) {
                  long missingSpace = ((NotEnoughSpaceRecorderException)ex).getMissingSpace();
                  float missingSpaceMegaByte = Math.max(0.1f, missingSpace / 1048576f);
                  cause = "Missing " + new DecimalFormat("#.#").format(missingSpaceMegaByte) + " MB to save home";
                } else if (ex instanceof RecorderException) {
                  cause = "RecorderException";
                  String message = ex.getMessage();
                  if (message != null) {
                    cause += ": " + message;
                  }
                  if (ex.getCause() != null) {
                    cause += "<br>" + ex.getCause();
                  }
                }
                ex.printStackTrace();
                getView().showError(preferences.getLocalizedString(
                    HomeController.class, "saveError", homeName, cause));
              }
            }
          };
      new ThreadedTaskController(saveTask,
          this.preferences.getLocalizedString(HomeController.class, "saveMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Updates the saved home and executes <code>postSaveTask</code>
   * if it's not <code>null</code>.
   */
  private void updateSavedHome(final String homeName,
                               final long savedVersion,
                               final Runnable postSaveTask) {
    getView().invokeLater(new Runnable() {
        public void run() {
          home.setName(homeName);
          home.setModified(false);
          home.setRecovered(false);
          home.setRepaired(false);
          home.setVersion(savedVersion);
          // Update recent homes list
          List<String> recentHomes = new ArrayList<String>(preferences.getRecentHomes());
          int homeNameIndex = recentHomes.indexOf(homeName);
          if (homeNameIndex >= 0) {
            recentHomes.remove(homeNameIndex);
          }
          recentHomes.add(0, homeName);
          updateUserPreferencesRecentHomes(recentHomes);

          if (postSaveTask != null) {
            postSaveTask.run();
          }
        }
      });
  }

  /**
   * Controls the export of the furniture list of current home to a CSV file.
   * @since 4.0
   */
  public void exportToCSV() {
    final String csvName = getView().showExportToCSVDialog(this.home.getName());
    if (csvName != null) {
      // Export furniture list in a threaded task
      Callable<Void> exportToCsvTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getView().exportToCSV(csvName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "exportToCSVError", csvName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToCsvTask,
          this.preferences.getLocalizedString(HomeController.class, "exportToCSVMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls the export of the current home plan to a SVG file.
   */
  public void exportToSVG() {
    final String svgName = getView().showExportToSVGDialog(this.home.getName());
    if (svgName != null) {
      // Export plan in a threaded task
      Callable<Void> exportToSvgTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getView().exportToSVG(svgName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "exportToSVGError", svgName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToSvgTask,
          this.preferences.getLocalizedString(HomeController.class, "exportToSVGMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls the export of the 3D view of current home to an OBJ file.
   */
  public void exportToOBJ() {
    final String objName = getView().showExportToOBJDialog(this.home.getName());
    if (objName != null) {
      // Export 3D view in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getView().exportToOBJ(objName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "exportToOBJError", objName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask,
          this.preferences.getLocalizedString(HomeController.class, "exportToOBJMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls the creation of multiple photo-realistic images at the stored cameras locations.
   */
  public void createPhotos() {
    PhotosController photosController = new PhotosController(this.home, this.preferences,
        getHomeController3D().getView(), this.viewFactory, this.contentManager);
    photosController.displayView(getView());
  }

  /**
   * Controls the creation of photo-realistic images.
   */
  public void createPhoto() {
    PhotoController photoController = new PhotoController(this.home, this.preferences,
        getHomeController3D().getView(), this.viewFactory, this.contentManager);
    photoController.displayView(getView());
  }

  /**
   * Controls the creation of 3D videos.
   */
  public void createVideo() {
    getPlanController().setMode(PlanController.Mode.SELECTION);
    getHomeController3D().viewFromObserver();
    VideoController videoController = new VideoController(this.home, this.preferences,
        this.viewFactory, this.contentManager);
    videoController.displayView(getView());
  }

  /**
   * Controls page setup.
   */
  public void setupPage() {
    new PageSetupController(this.home, this.preferences,
        this.viewFactory, getUndoableEditSupport()).displayView(getView());
  }

  /**
   * Controls the print preview.
   */
  public void previewPrint() {
    new PrintPreviewController(this.home, this.preferences,
        this, this.viewFactory).displayView(getView());
  }

  /**
   * Controls the print of this home.
   */
  public void print() {
    final Callable<Void> printTask = getView().showPrintDialog();
    if (printTask != null) {
      // Print in a threaded task
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "printError", home.getName());
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printTask,
          this.preferences.getLocalizedString(HomeController.class, "printMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls the print of this home in a PDF file.
   */
  public void printToPDF() {
    final String pdfName = getView().showPrintToPDFDialog(this.home.getName());
    if (pdfName != null) {
      // Print to PDF in a threaded task
      Callable<Void> printToPdfTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            getView().printToPDF(pdfName);
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "printToPDFError", pdfName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printToPdfTask,
          preferences.getLocalizedString(HomeController.class, "printToPDFMessage"), exceptionHandler,
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls application exit. If any home in application homes list is modified,
   * the user will be {@link HomeView#confirmExit() prompted} in view whether he wants
   * to discard his modifications or not.
   */
  public void exit() {
    for (Home home : this.application.getHomes()) {
      if (home.isModified() || home.isRecovered() || home.isRepaired()) {
        if (getView().confirmExit()) {
          break;
        } else {
          return;
        }
      }
    }
    // Remove all homes from application
    for (Home home : this.application.getHomes()) {
      home.setRecovered(false);
      this.application.deleteHome(home);
    }
    // Let application decide what to do when there's no more home
  }

  /**
   * Edits preferences and changes them if user agrees.
   */
  public void editPreferences() {
    new UserPreferencesController(this.preferences,
        this.viewFactory, this.contentManager, this).displayView(getView());
  }

  /**
   * Displays a tip message dialog depending on the given mode and
   * sets the active mode of the plan controller.
   */
  public void setMode(PlanController.Mode mode) {
    if (getPlanController().getMode() != mode) {
      final String actionKey;
      if (mode == PlanController.Mode.WALL_CREATION) {
        actionKey = HomeView.ActionType.CREATE_WALLS.name();
      } else if (mode == PlanController.Mode.ROOM_CREATION) {
        actionKey = HomeView.ActionType.CREATE_ROOMS.name();
      } else if (mode == PlanController.Mode.POLYLINE_CREATION) {
        actionKey = HomeView.ActionType.CREATE_POLYLINES.name();
      } else if (mode == PlanController.Mode.DIMENSION_LINE_CREATION) {
        actionKey = HomeView.ActionType.CREATE_DIMENSION_LINES.name();
      } else if (mode == PlanController.Mode.LABEL_CREATION) {
        actionKey = HomeView.ActionType.CREATE_LABELS.name();
      } else {
        actionKey = null;
      }
      // Display the tip message dialog matching mode
      if (actionKey != null
          && !this.preferences.isActionTipIgnored(actionKey)) {
        getView().invokeLater(new Runnable() {
            public void run() {
              // Show tip later to let the mode switch finish first
              if (getView().showActionTipMessage(actionKey)) {
                preferences.setActionTipIgnored(actionKey);
              }
            }
          });
      }
      getPlanController().setMode(mode);
    }
  }

  /**
   * Displays the wizard that helps to import home background image.
   */
  public void importBackgroundImage() {
    new BackgroundImageWizardController(this.home, this.preferences,
        this.viewFactory, this.contentManager, getUndoableEditSupport()).displayView(getView());
  }

  /**
   * Displays the wizard that helps to change home background image.
   */
  public void modifyBackgroundImage() {
    importBackgroundImage();
  }

  /**
   * Hides the home background image.
   */
  public void hideBackgroundImage() {
    toggleBackgroundImageVisibility("undoHideBackgroundImageName");
  }

  /**
   * Shows the home background image.
   */
  public void showBackgroundImage() {
    toggleBackgroundImageVisibility("undoShowBackgroundImageName");
  }

  /**
   * Toggles visibility of the background image and posts an undoable operation.
   */
  private void toggleBackgroundImageVisibility(final String presentationName) {
    final Level selectedLevel = this.home.getSelectedLevel();
    doToggleBackgroundImageVisibility();
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setSelectedLevel(selectedLevel);
          doToggleBackgroundImageVisibility();
        }

        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setSelectedLevel(selectedLevel);
          doToggleBackgroundImageVisibility();
        }

        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(HomeController.class, presentationName);
        }
      };
    getUndoableEditSupport().postEdit(undoableEdit);
  }

  /**
   * Toggles visibility of the background image.
   */
  private void doToggleBackgroundImageVisibility() {
    BackgroundImage backgroundImage = this.home.getSelectedLevel() != null
        ? this.home.getSelectedLevel().getBackgroundImage()
        : this.home.getBackgroundImage();
    backgroundImage = new BackgroundImage(backgroundImage.getImage(),
        backgroundImage.getScaleDistance(),
        backgroundImage.getScaleDistanceXStart(), backgroundImage.getScaleDistanceYStart(),
        backgroundImage.getScaleDistanceXEnd(), backgroundImage.getScaleDistanceYEnd(),
        backgroundImage.getXOrigin(), backgroundImage.getYOrigin(), !backgroundImage.isVisible());
    if (this.home.getSelectedLevel() != null) {
      this.home.getSelectedLevel().setBackgroundImage(backgroundImage);
    } else {
      this.home.setBackgroundImage(backgroundImage);
    }
  }

  /**
   * Deletes home background image and posts and posts an undoable operation.
   */
  public void deleteBackgroundImage() {
    final Level selectedLevel = this.home.getSelectedLevel();
    final BackgroundImage oldImage;
    if (selectedLevel != null) {
      oldImage = selectedLevel.getBackgroundImage();
      selectedLevel.setBackgroundImage(null);
    } else {
      oldImage = this.home.getBackgroundImage();
      this.home.setBackgroundImage(null);
    }
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        home.setSelectedLevel(selectedLevel);
        if (selectedLevel != null) {
          selectedLevel.setBackgroundImage(oldImage);
        } else {
          home.setBackgroundImage(oldImage);
        }
      }

      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        home.setSelectedLevel(selectedLevel);
        if (selectedLevel != null) {
          selectedLevel.setBackgroundImage(null);
        } else {
          home.setBackgroundImage(null);
        }
      }

      @Override
      public String getPresentationName() {
        return preferences.getLocalizedString(HomeController.class, "undoDeleteBackgroundImageName");
      }
    };
    getUndoableEditSupport().postEdit(undoableEdit);
  }

  /**
   * Zooms out in plan.
   */
  public void zoomOut() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() / 1.5f;
    planController.setScale(newScale);
    planController.getView().makeSelectionVisible();
  }

  /**
   * Zooms in in plan.
   */
  public void zoomIn() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() * 1.5f;
    planController.setScale(newScale);
    planController.getView().makeSelectionVisible();
  }

  /**
   * Prompts a name for the current camera and stores it in home.
   */
  public void storeCamera() {
    String now = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date());
    String name = getView().showStoreCameraDialog(now);
    if (name != null) {
      getHomeController3D().storeCamera(name);
    }
  }

  /**
   * Prompts stored cameras in home to be deleted and deletes the ones selected by the user.
   */
  public void deleteCameras() {
    List<Camera> deletedCameras = getView().showDeletedCamerasDialog();
    if (deletedCameras != null) {
      getHomeController3D().deleteCameras(deletedCameras);
    }
  }

  /**
   * Detaches the given <code>view</code> from home view.
   */
  public void detachView(View view) {
    if (view != null) {
      getView().detachView(view);
      this.notUndoableModifications = true;
      home.setModified(true);
    }
  }

  /**
   * Attaches the given <code>view</code> to home view.
   */
  public void attachView(View view) {
    if (view != null) {
      getView().attachView(view);
      this.notUndoableModifications = true;
      home.setModified(true);
    }
  }

  /**
   * Displays help window.
   */
  public void help() {
    if (helpController == null) {
      helpController = new HelpController(this.preferences, this.viewFactory);
    }
    helpController.displayView();
  }

  /**
   * Displays about dialog.
   */
  public void about() {
    getView().showAboutDialog();
  }

  /**
   * Controls the change of value of a visual property in home.
   * @deprecated {@link #setVisualProperty(String, Object) setVisualProperty} should be replaced by a call to
   * {@link #setHomeProperty(String, String)} to ensure the property can be easily saved and read.
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }

  /**
   * Controls the change of value of a property in home.
   * @since 5.2
   */
  public void setHomeProperty(String propertyName,
                                String propertyValue) {
    this.home.setProperty(propertyName, propertyValue);
  }

  /**
   * Checks if some application or libraries updates are available.
   * @since 4.0
   */
  public void checkUpdates(final boolean displayOnlyIfNewUpdates) {
    String updatesUrl = getPropertyValue("com.eteks.sweethome3d.updatesUrl", "updatesUrl");
    if (updatesUrl != null && updatesUrl.length() > 0) {
      final URL url;
      try {
        url = new URL(updatesUrl);
      } catch (MalformedURLException ex) {
        ex.printStackTrace();
        return;
      }

      final List<Library> libraries = this.preferences.getLibraries();
      final Long updatesMinimumDate = displayOnlyIfNewUpdates
          ? this.preferences.getUpdatesMinimumDate()
          : null;

      // Read updates from XML content in updatesUrl in a threaded task
      Callable<Void> checkUpdatesTask = new Callable<Void>() {
          public Void call() throws IOException, SAXException {
            final Map<Library, List<Update>> availableUpdates = readAvailableUpdates(url, libraries, updatesMinimumDate,
                displayOnlyIfNewUpdates ? 3000 : -1);
            getView().invokeLater(new Runnable () {
                public void run() {
                  if (availableUpdates.isEmpty()) {
                    if (!displayOnlyIfNewUpdates) {
                      getView().showMessage(preferences.getLocalizedString(HomeController.class, "noUpdateMessage"));
                    }
                  } else if (!getView().showUpdatesMessage(getUpdatesMessage(availableUpdates), !displayOnlyIfNewUpdates)) {
                    // Search the latest date among updates
                    long latestUpdateDate = Long.MIN_VALUE;
                    for (List<Update> libraryAvailableUpdates : availableUpdates.values()) {
                      for (Update update : libraryAvailableUpdates) {
                        latestUpdateDate = Math.max(latestUpdateDate, update.getDate().getTime());
                      }
                    }
                    preferences.setUpdatesMinimumDate(latestUpdateDate + 1);
                  }
                }
              });
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!displayOnlyIfNewUpdates && !(ex instanceof InterruptedIOException)) {
                if (ex instanceof IOException) {
                  getView().showError(preferences.getLocalizedString(HomeController.class, "checkUpdatesIOError", ex));
                } else if (ex instanceof SAXException) {
                  getView().showError(preferences.getLocalizedString(HomeController.class, "checkUpdatesXMLError", ex.getMessage()));
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };

      ViewFactory dummyThreadedTaskViewFactory = new ViewFactoryAdapter() {
          @Override
          public ThreadedTaskView createThreadedTaskView(String taskMessage, UserPreferences preferences,
                                                         ThreadedTaskController controller) {
            // Return a dummy view that doesn't do anything
            return new ThreadedTaskView() {
              public void setTaskRunning(boolean taskRunning, View executingView) {
              }

              public void invokeLater(Runnable runnable) {
                getView().invokeLater(runnable);
              }
            };
          }
        };
      new ThreadedTaskController(checkUpdatesTask,
          this.preferences.getLocalizedString(HomeController.class, "checkUpdatesMessage"), exceptionHandler,
          this.preferences, displayOnlyIfNewUpdates
            ? dummyThreadedTaskViewFactory
            : this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Returns the System property value of the given <code>propertyKey</code>, or the
   * the resource property value matching <code>resourceKey</code> or <code>null</code>
   * if none are defined.
   */
  private String getPropertyValue(String propertyKey, String resourceKey) {
    String propertyValue = System.getProperty(propertyKey);
    if (propertyValue != null) {
      return propertyValue;
    } else {
      try {
        return this.preferences.getLocalizedString(HomeController.class, resourceKey);
      } catch (IllegalArgumentException ex) {
        return null;
      }
    }
  }

  /**
   * Reads the available updates from the XML stream contained in the given <code>url</code>.
   * Caution : this method is called from a separate thread.
   */
  private Map<Library, List<Update>> readAvailableUpdates(URL url, List<Library> libraries, Long minDate, int timeout) throws IOException, SAXException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      SAXParser saxParser = factory.newSAXParser();
      UpdatesHandler updatesHandler = new UpdatesHandler(url);
      URLConnection connection = url.openConnection();
      if (timeout > 0) {
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
      }
      saxParser.parse(connection.getInputStream(), updatesHandler);

      // Filter updates according to application version and libraries version
      Map<Library, List<Update>> availableUpdates = new LinkedHashMap<Library, List<Update>>();
      long now = System.currentTimeMillis();
      if (this.application != null) {
        String applicationId = this.application.getId();
        List<Update> applicationUpdates = getAvailableUpdates(updatesHandler.getUpdates(applicationId),
            this.application.getVersion(), minDate, now);
        if (!applicationUpdates.isEmpty()) {
          availableUpdates.put(null, applicationUpdates);
        }
      }
      Set<String> updatedLibraryIds = new HashSet<String>();
      for (Library library : libraries) {
        if (Thread.interrupted()) {
          throw new InterruptedIOException();
        }
        String libraryId = library.getId();
        if (libraryId != null
            && !updatedLibraryIds.contains(libraryId)) {
          List<Update> libraryUpdates = getAvailableUpdates(updatesHandler.getUpdates(libraryId),
              library.getVersion(), minDate, now);
          if (!libraryUpdates.isEmpty()) {
            availableUpdates.put(library, libraryUpdates);
          }
          // Ignore older libraries with same ID
          updatedLibraryIds.add(libraryId);
        }
      }
      return availableUpdates;
    } catch (ParserConfigurationException ex) {
      throw new SAXException(ex);
    } catch (SAXException ex) {
      // If task was interrupted (see UpdatesHandler implementation), report the interruption
      if (ex.getCause() instanceof InterruptedIOException) {
        throw (InterruptedIOException)ex.getCause();
      } else {
        throw ex;
      }
    }
  }

  /**
   * Returns the updates sublist which match the given <code>version</code>.
   * If no update has a date greater that <code>minDate</code>, an empty list is returned.
   * Caution : this method is called from a separate thread.
   */
  private List<Update> getAvailableUpdates(List<Update> updates, String version, Long minDate, long maxDate) {
    if (updates != null) {
      boolean recentUpdates = false;
      List<Update> availableUpdates = new ArrayList<Update>();
      for (Update update : updates) {
        String minVersion = update.getMinVersion();
        String maxVersion = update.getMaxVersion();
        String operatingSystem = update.getOperatingSystem();
        if (OperatingSystem.compareVersions(version, update.getVersion()) < 0
            && (minVersion == null || OperatingSystem.compareVersions(minVersion, version) <= 0)
            && (maxVersion == null || OperatingSystem.compareVersions(version, maxVersion) < 0)
            && (operatingSystem == null || System.getProperty("os.name").matches(operatingSystem))) {
          Date date = update.getDate();
          if (date == null
              || ((minDate == null || date.getTime() >= minDate)
                  && date.getTime() < maxDate)) {
            availableUpdates.add(update);
            recentUpdates = true;
          }
        }
      }
      if (recentUpdates) {
        Collections.sort(availableUpdates, new Comparator<Update>() {
            public int compare(Update update1, Update update2) {
              return -OperatingSystem.compareVersions(update1.getVersion(), update2.getVersion());
            }
          });
        return availableUpdates;
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns the message for the given updates.
   */
  private String getUpdatesMessage(Map<Library, List<Update>> updates) {
    if (updates.isEmpty()) {
      return this.preferences.getLocalizedString(HomeController.class, "noUpdateMessage");
    } else {
      String message = "<html><head><style>"
          + this.preferences.getLocalizedString(HomeController.class, "updatesMessageStyleSheet")
          + " .separator { margin: 0px;}</style></head><body>"
          + this.preferences.getLocalizedString(HomeController.class, "updatesMessageTitle");
      String applicationUpdateMessage = this.preferences.getLocalizedString(HomeController.class, "applicationUpdateMessage");
      String libraryUpdateMessage = this.preferences.getLocalizedString(HomeController.class, "libraryUpdateMessage");
      String sizeUpdateMessage = this.preferences.getLocalizedString(HomeController.class, "sizeUpdateMessage");
      String downloadUpdateMessage = this.preferences.getLocalizedString(HomeController.class, "downloadUpdateMessage");
      String updatesMessageSeparator = this.preferences.getLocalizedString(HomeController.class, "updatesMessageSeparator");
      boolean firstUpdate = true;
      for (Map.Entry<Library, List<Update>> updateEntry : updates.entrySet()) {
        if (firstUpdate) {
          firstUpdate = false;
        } else {
          message += updatesMessageSeparator;
        }
        Library library = updateEntry.getKey();
        if (library == null) {
          // Application itself
          if (this.application != null) {
            message += getApplicationOrLibraryUpdateMessage(updateEntry.getValue(), this.application.getName(),
                applicationUpdateMessage, sizeUpdateMessage, downloadUpdateMessage);
          }
        } else {
          String name = library.getName();
          if (name == null) {
            name = library.getDescription();
            if (name == null) {
              name = library.getLocation();
            }
          }
          message += getApplicationOrLibraryUpdateMessage(updateEntry.getValue(), name,
              libraryUpdateMessage, sizeUpdateMessage, downloadUpdateMessage);
        }
      }

      message += "</body></html>";
      return message;
    }
  }

  /**
   * Returns the message for the update of the application or a library.
   */
  private String getApplicationOrLibraryUpdateMessage(List<Update> updates,
                                                      String applicationOrLibraryName,
                                                      String applicationOrLibraryUpdateMessage,
                                                      String sizeUpdateMessage,
                                                      String downloadUpdateMessage) {
    String message = "";
    boolean first = true;
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
    DecimalFormat megabyteSizeFormat = new DecimalFormat("#,##0.#");
    for (Update update : updates) {
      String size;
      if (update.getSize() != null) {
        // Format at MB format
        size = String.format(sizeUpdateMessage,
            megabyteSizeFormat.format(update.getSize() / (1024. * 1024.)));
      } else {
        size = "";
      }
      message += String.format(applicationOrLibraryUpdateMessage,
          applicationOrLibraryName, update.getVersion(), dateFormat.format(update.getDate()), size);
      if (first) {
        first = false;
        URL downloadPage = update.getDownloadPage();
        if (downloadPage == null) {
          downloadPage = update.getDefaultDownloadPage();
        }
        if (downloadPage != null) {
          message += String.format(downloadUpdateMessage, downloadPage);
        }
      }
      String comment = update.getComment();
      if (comment == null) {
        comment = update.getDefaultComment();
      }
      if (comment != null) {
        message += "<p class='separator'/>";
        message += comment;
        message += "<p class='separator'/>";
      }
    }
    return message;
  }

  /**
   * SAX handler used to parse updates XML files.
   * DTD used in updated files:<pre>
   * &lt;!ELEMENT updates (update*)>
   *
   * &lt;!ELEMENT update (downloadPage*, comment*)>
   * &lt;!ATTLIST update id CDATA #REQUIRED>
   * &lt;!ATTLIST update version CDATA #REQUIRED>
   * &lt;!ATTLIST update operatingSystem CDATA #IMPLIED>
   * &lt;!ATTLIST update date CDATA #REQUIRED>
   * &lt;!ATTLIST update minVersion CDATA #IMPLIED>
   * &lt;!ATTLIST update maxVersion CDATA #IMPLIED>
   * &lt;!ATTLIST update size CDATA #IMPLIED>
   * &lt;!ATTLIST update inherits CDATA #IMPLIED>
   *
   * &lt;!ELEMENT downloadPage EMPTY>
   * &lt;!ATTLIST downloadPage url CDATA #REQUIRED>
   * &lt;!ATTLIST downloadPage lang CDATA #IMPLIED>
   *
   * &lt;!ELEMENT comment (#PCDATA)>
   * &lt;!ATTLIST comment lang CDATA #IMPLIED>
   * </pre>
   * with <code>updates</code> as root element,
   * <code>operatingSystem</code> an optional regular expression for the target OS,
   * <code>inherits</code> the id of an other <code>update</code> element with the same version,
   * <code>date</code> using <code>yyyy-MM-ddThh:mm:ss<code> or <code>yyyy-MM-dd</code> format
   * at GMT and <code>comment</code> element possibly containing XHTML.
   */
  private class UpdatesHandler extends DefaultHandler {
    private final URL                       baseUrl;
    private final StringBuilder             comment = new StringBuilder();
    private final SimpleDateFormat          dateTimeFormat;
    private final SimpleDateFormat          dateFormat;
    private final Map<String, List<Update>> updates = new HashMap<String, List<Update>>();
    private Update                          update;
    private boolean                         inComment;
    private boolean                         inUpdate;
    private String                          language;

    public UpdatesHandler(URL baseUrl) {
      this.baseUrl = baseUrl;
      TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
      this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      this.dateTimeFormat.setTimeZone(gmtTimeZone);
      this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      this.dateFormat.setTimeZone(gmtTimeZone);
    }

    /**
     * Returns the update matching the given <code>id</code>.
     */
    private List<Update> getUpdates(String id) {
      return this.updates.get(id);
    }

    /**
     * Throws a <code>SAXException</code> exception initialized with a <code>InterruptedRecorderException</code>
     * cause if current thread is interrupted. The interrupted status of the current thread
     * is cleared when an exception is thrown.
     */
    private void checkCurrentThreadIsntInterrupted() throws SAXException {
      if (Thread.interrupted()) {
        throw new SAXException(new InterruptedIOException());
      }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      checkCurrentThreadIsntInterrupted();
      if (this.inComment) {
        // Reproduce comment content
        this.comment.append("<" + name);
        for (int i = 0; i < attributes.getLength(); i++) {
          this.comment.append(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
        }
        this.comment.append(">");
      } else if (this.inUpdate && "comment".equals(name)) {
        this.comment.setLength(0);
        this.language = attributes.getValue("lang");
        if (this.language == null || preferences.getLanguage().equals(this.language)) {
          this.inComment = true;
        }
      } else if (this.inUpdate && "downloadPage".equals(name)) {
        String url = attributes.getValue("url");
        if (url != null) {
          try {
            String language = attributes.getValue("lang");
            if (language == null) {
              this.update.setDefaultDownloadPage(new URL(this.baseUrl, url));
            } else if (preferences.getLanguage().equals(language)) {
              this.update.setDownloadPage(new URL(this.baseUrl, url));
            }
          } catch (MalformedURLException ex) {
            // Ignore bad URLs
          }
        }
      } else if (!this.inUpdate && "update".equals(name)) {
        String id = attributes.getValue("id");
        String version = attributes.getValue("version");
        if (id != null
            && version != null) {
          this.update = new Update(id, version);

          String inheritedUpdate = attributes.getValue("inherits");
          // If update inherits from an other update, search the update with the same id and version
          if (inheritedUpdate != null) {
            List<Update> updates = this.updates.get(inheritedUpdate);
            if (updates != null) {
              for (Update update : updates) {
                if (version.equals(update.getVersion())) {
                  this.update = update.clone();
                  this.update.setId(id);
                  break;
                }
              }
            }
          }

          String dateAttibute = attributes.getValue("date");
          if (dateAttibute != null) {
            try {
              this.update.setDate(this.dateTimeFormat.parse(dateAttibute));
            } catch (ParseException ex) {
              try {
                this.update.setDate(this.dateFormat.parse(dateAttibute));
              } catch (ParseException ex1) {
              }
            }
          }

          String minVersion = attributes.getValue("minVersion");
          if (minVersion != null) {
            this.update.setMinVersion(minVersion);
          }

          String maxVersion = attributes.getValue("maxVersion");
          if (maxVersion != null) {
            this.update.setMaxVersion(maxVersion);
          }

          String size = attributes.getValue("size");
          if (size != null) {
            try {
              this.update.setSize(new Long (size));
            } catch (NumberFormatException ex) {
              // Ignore malformed number
            }
          }

          String operatingSystem = attributes.getValue("operatingSystem");
          if (operatingSystem != null) {
            this.update.setOperatingSystem(operatingSystem);
          }

          List<Update> updates = this.updates.get(id);
          if (updates == null) {
            updates = new ArrayList<Update>();
            this.updates.put(id, updates);
          }
          updates.add(this.update);
          this.inUpdate = true;
        }
      }
    }

    @Override
    public void characters(char [] ch, int start, int length) throws SAXException {
      checkCurrentThreadIsntInterrupted();
      if (this.inComment) {
        // Reproduce comment content
        this.comment.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
      if (this.inComment) {
        if ("comment".equals(name)) {
          String comment = this.comment.toString().trim().replace('\n', ' ');
          if (comment.length() == 0) {
            comment = null;
          }
          if (this.language == null) {
            this.update.setDefaultComment(comment);
          } else {
            this.update.setComment(comment);
          }
          this.inComment = false;
        } else {
          // Reproduce comment content
          this.comment.append("</" + name + ">");
        }
      } else if (this.inUpdate && "update".equals(name)) {
        this.inUpdate = false;
      }
    }
  }

  /**
   * Update info.
   */
  private static class Update implements Cloneable {
    private String id;
    private final String version;
    private Date   date;
    private String minVersion;
    private String maxVersion;
    private Long   size;
    private String operatingSystem;
    private URL    defaultDownloadPage;
    private URL    downloadPage;
    private String defaultComment;
    private String comment;

    public Update(String id, String version) {
      this.id = id;
      this.version = version;
    }

    public String getId() {
      return this.id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getVersion() {
      return this.version;
    }

    public Date getDate() {
      return this.date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getMinVersion() {
      return this.minVersion;
    }

    public void setMinVersion(String minVersion) {
      this.minVersion = minVersion;
    }

    public String getMaxVersion() {
      return this.maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
      this.maxVersion = maxVersion;
    }

    public Long getSize() {
      return this.size;
    }

    public void setSize(Long size) {
      this.size = size;
    }

    public String getOperatingSystem() {
      return this.operatingSystem;
    }

    public void setOperatingSystem(String system) {
      this.operatingSystem = system;
    }

    public URL getDefaultDownloadPage() {
      return this.defaultDownloadPage;
    }

    public void setDefaultDownloadPage(URL defaultDownloadPage) {
      this.defaultDownloadPage = defaultDownloadPage;
    }

    public URL getDownloadPage() {
      return this.downloadPage;
    }

    public void setDownloadPage(URL downloadPage) {
      this.downloadPage = downloadPage;
    }

    public String getDefaultComment() {
      return this.defaultComment;
    }

    public void setDefaultComment(String defaultComment) {
      this.defaultComment = defaultComment;
    }

    public String getComment() {
      return this.comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    @Override
    protected Update clone() {
      try {
        return (Update)super.clone();
      } catch (CloneNotSupportedException ex) {
        throw new InternalError();
      }
    }
  }
}
