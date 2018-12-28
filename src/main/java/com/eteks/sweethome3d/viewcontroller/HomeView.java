/*
 * HomeView.java 28 oct 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.util.List;
import java.util.concurrent.Callable;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Selectable;

/**
 * The main view that displays a home.
 * @author Emmanuel Puybaret
 */
public interface HomeView extends View {
  /**
   * The actions proposed by the view to user.
   */
  public enum ActionType {
      NEW_HOME, NEW_HOME_FROM_EXAMPLE, CLOSE, OPEN, DELETE_RECENT_HOMES, SAVE, SAVE_AS, SAVE_AND_COMPRESS,
      PAGE_SETUP, PRINT_PREVIEW, PRINT, PRINT_TO_PDF, PREFERENCES, EXIT,
      UNDO, REDO, CUT, COPY, PASTE, PASTE_TO_GROUP, PASTE_STYLE, DELETE, SELECT_ALL, SELECT_ALL_AT_ALL_LEVELS,
      ADD_HOME_FURNITURE, ADD_FURNITURE_TO_GROUP, DELETE_HOME_FURNITURE, MODIFY_FURNITURE,
      IMPORT_FURNITURE, IMPORT_FURNITURE_LIBRARY, IMPORT_TEXTURE, IMPORT_TEXTURES_LIBRARY,
      SORT_HOME_FURNITURE_BY_CATALOG_ID, SORT_HOME_FURNITURE_BY_NAME, SORT_HOME_FURNITURE_BY_CREATOR,
      SORT_HOME_FURNITURE_BY_WIDTH, SORT_HOME_FURNITURE_BY_DEPTH, SORT_HOME_FURNITURE_BY_HEIGHT,
      SORT_HOME_FURNITURE_BY_X, SORT_HOME_FURNITURE_BY_Y, SORT_HOME_FURNITURE_BY_ELEVATION,
      SORT_HOME_FURNITURE_BY_ANGLE, SORT_HOME_FURNITURE_BY_LEVEL, SORT_HOME_FURNITURE_BY_MODEL_SIZE,
      SORT_HOME_FURNITURE_BY_COLOR, SORT_HOME_FURNITURE_BY_TEXTURE,
      SORT_HOME_FURNITURE_BY_MOVABILITY, SORT_HOME_FURNITURE_BY_TYPE, SORT_HOME_FURNITURE_BY_VISIBILITY,
      SORT_HOME_FURNITURE_BY_PRICE, SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE,
      SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED,
      SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
      DISPLAY_HOME_FURNITURE_CATALOG_ID, DISPLAY_HOME_FURNITURE_NAME, DISPLAY_HOME_FURNITURE_CREATOR,
      DISPLAY_HOME_FURNITURE_WIDTH, DISPLAY_HOME_FURNITURE_DEPTH, DISPLAY_HOME_FURNITURE_HEIGHT,
      DISPLAY_HOME_FURNITURE_X, DISPLAY_HOME_FURNITURE_Y, DISPLAY_HOME_FURNITURE_ELEVATION,
      DISPLAY_HOME_FURNITURE_ANGLE, DISPLAY_HOME_FURNITURE_LEVEL, DISPLAY_HOME_FURNITURE_MODEL_SIZE,
      DISPLAY_HOME_FURNITURE_COLOR, DISPLAY_HOME_FURNITURE_TEXTURE,
      DISPLAY_HOME_FURNITURE_MOVABLE, DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, DISPLAY_HOME_FURNITURE_VISIBLE,
      DISPLAY_HOME_FURNITURE_PRICE, DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE,
      DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED,
      ALIGN_FURNITURE_ON_TOP, ALIGN_FURNITURE_ON_BOTTOM, ALIGN_FURNITURE_ON_LEFT, ALIGN_FURNITURE_ON_RIGHT,
      ALIGN_FURNITURE_ON_FRONT_SIDE, ALIGN_FURNITURE_ON_BACK_SIDE, ALIGN_FURNITURE_ON_LEFT_SIDE, ALIGN_FURNITURE_ON_RIGHT_SIDE, ALIGN_FURNITURE_SIDE_BY_SIDE,
      DISTRIBUTE_FURNITURE_HORIZONTALLY, DISTRIBUTE_FURNITURE_VERTICALLY, RESET_FURNITURE_ELEVATION,
      GROUP_FURNITURE, UNGROUP_FURNITURE, EXPORT_TO_CSV,
      SELECT, PAN, CREATE_WALLS, CREATE_ROOMS, CREATE_DIMENSION_LINES, CREATE_POLYLINES, CREATE_LABELS, DELETE_SELECTION,
      LOCK_BASE_PLAN, UNLOCK_BASE_PLAN, FLIP_HORIZONTALLY, FLIP_VERTICALLY,
      MODIFY_COMPASS, MODIFY_WALL, JOIN_WALLS, REVERSE_WALL_DIRECTION, SPLIT_WALL,
      MODIFY_ROOM, ADD_ROOM_POINT, DELETE_ROOM_POINT, MODIFY_POLYLINE, MODIFY_LABEL,
      INCREASE_TEXT_SIZE, DECREASE_TEXT_SIZE, TOGGLE_BOLD_STYLE, TOGGLE_ITALIC_STYLE,
      IMPORT_BACKGROUND_IMAGE, MODIFY_BACKGROUND_IMAGE, HIDE_BACKGROUND_IMAGE, SHOW_BACKGROUND_IMAGE, DELETE_BACKGROUND_IMAGE,
      ADD_LEVEL, ADD_LEVEL_AT_SAME_ELEVATION, MAKE_LEVEL_VIEWABLE, MAKE_LEVEL_UNVIEWABLE,
      MAKE_LEVEL_ONLY_VIEWABLE_ONE, MAKE_ALL_LEVELS_VIEWABLE, MODIFY_LEVEL, DELETE_LEVEL,
      ZOOM_OUT, ZOOM_IN, EXPORT_TO_SVG,
      VIEW_FROM_TOP, VIEW_FROM_OBSERVER, MODIFY_OBSERVER, STORE_POINT_OF_VIEW, DELETE_POINTS_OF_VIEW, CREATE_PHOTOS_AT_POINTS_OF_VIEW, DETACH_3D_VIEW, ATTACH_3D_VIEW,
      DISPLAY_ALL_LEVELS, DISPLAY_SELECTED_LEVEL, MODIFY_3D_ATTRIBUTES, CREATE_PHOTO, CREATE_VIDEO, EXPORT_TO_OBJ,
      HELP, ABOUT}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}
  public enum OpenDamagedHomeAnswer {REMOVE_DAMAGED_ITEMS, REPLACE_DAMAGED_ITEMS, DO_NOT_OPEN_HOME}

  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public abstract void setEnabled(ActionType actionType,
                                  boolean enabled);

  /**
   * Sets the name and tool tip of undo and redo actions. If a parameter is <code>null</code>,
   * the properties will be reset to their initial values.
   */
  public abstract void setUndoRedoName(String undoText,
                                       String redoText);

  /**
   * Enables or disables transfer between components.
   */
  public abstract void setTransferEnabled(boolean enabled);


  /**
   * Detaches the given <code>view</code> from home view.
   */
  public abstract void detachView(View view);

  /**
   * Attaches the given <code>view</code> to home view.
   */
  public abstract void attachView(View view);

  /**
   * Displays a content chooser open dialog to choose the name of a home.
   */
  public abstract String showOpenDialog();

  /**
   * Displays a dialog that lets user choose what he wants
   * to do with a damaged home he tries to open it.
   * @since 4.4
   */
  public abstract OpenDamagedHomeAnswer confirmOpenDamagedHome(String homeName,
                                                               Home damagedHome,
                                                               List<Content> invalidContent);

  /**
   * Displays a dialog to let the user choose a home example.
   * @since 5.5
   */
  public abstract String showNewHomeFromExampleDialog();

  /**
   * Displays a content chooser open dialog to choose a language library.
   */
  public abstract String showImportLanguageLibraryDialog();

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing language library or not.
   */
  public abstract boolean confirmReplaceLanguageLibrary(String languageLibraryName);

  /**
   * Displays a content chooser open dialog to choose a furniture library.
   */
  public abstract String showImportFurnitureLibraryDialog();

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing furniture library or not.
   */
  public abstract boolean confirmReplaceFurnitureLibrary(String furnitureLibraryName);

  /**
   * Displays a content chooser open dialog to choose a textures library.
   */
  public abstract String showImportTexturesLibraryDialog();

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing textures library or not.
   */
  public abstract boolean confirmReplaceTexturesLibrary(String texturesLibraryName);

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing plug-in or not.
   */
  public abstract boolean confirmReplacePlugin(String pluginName);

  /**
   * Displays a content chooser save dialog to choose the name of a home.
   */
  public abstract String showSaveDialog(String homeName);

  /**
   * Displays a dialog that lets user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user chose to save home,
   * {@link SaveAnswer#DO_NOT_SAVE} if user don't want to save home,
   * or {@link SaveAnswer#CANCEL} if doesn't want to continue current operation.
   */
  public abstract SaveAnswer confirmSave(String homeName);

  /**
   * Displays a dialog that let user choose whether he wants to save
   * a home that was created with a newer version of Sweet Home 3D.
   * @return <code>true</code> if user confirmed to save.
   */
  public abstract boolean confirmSaveNewerHome(String homeName);

  /**
   * Displays a dialog that let user choose whether he wants to delete
   * the selected furniture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public abstract boolean confirmDeleteCatalogSelection();

  /**
   * Displays a dialog that let user choose whether he wants to exit
   * application or not.
   * @return <code>true</code> if user confirmed to exit.
   */
  public abstract boolean confirmExit();

  /**
   * Displays <code>message</code> in an error message box.
   */
  public abstract void showError(String message);

  /**
   * Displays <code>message</code> in a message box.
   */
  public abstract void showMessage(String message);

  /**
   * Displays the tip matching <code>actionTipKey</code> and
   * returns <code>true</code> if the user chose not to display again the tip.
   */
  public abstract boolean showActionTipMessage(String actionTipKey);

  /**
   * Displays an about dialog.
   */
  public abstract void showAboutDialog();

  /**
   * Shows a print dialog to print the home displayed by this pane.
   * @return a print task to execute or <code>null</code> if the user canceled print.
   *    The <code>call</code> method of the returned task may throw a
   *    {@link RecorderException RecorderException} exception if print failed
   *    or an {@link InterruptedRecorderException InterruptedRecorderException}
   *    exception if it was interrupted.
   */
  public abstract Callable<Void> showPrintDialog();

  /**
   * Shows a content chooser save dialog to print a home in a PDF file.
   */
  public abstract String showPrintToPDFDialog(String homeName);

  /**
   * Prints a home to a given PDF file. This method may be overridden
   * to write to another kind of output stream.
   * Caution !!! This method may be called from a threaded task.
   */
  public abstract void printToPDF(String pdfFile) throws RecorderException;

  /**
   * Shows a content chooser save dialog to export furniture list in a CSV file.
   */
  public abstract String showExportToCSVDialog(String name);

  /**
   * Exports furniture list to a given SVG file.
   * Caution !!! This method may be called from a threaded task.
   */
  public abstract void exportToCSV(String csvName) throws RecorderException;

  /**
   * Shows a content chooser save dialog to export a home plan in a SVG file.
   */
  public abstract String showExportToSVGDialog(String name);

  /**
   * Exports the plan objects to a given SVG file.
   * Caution !!! This method may be called from a threaded task.
   */
  public abstract void exportToSVG(String svgName) throws RecorderException;

  /**
   * Shows a content chooser save dialog to export a 3D home in a OBJ file.
   */
  public abstract String showExportToOBJDialog(String homeName);

  /**
   * Exports the 3D home objects to a given OBJ file.
   * Caution !!! This method may be called from a threaded task.
   */
  public abstract void exportToOBJ(String objFile) throws RecorderException;

  /**
   * Displays a dialog that lets the user choose a name for the current camera.
   */
  public abstract String showStoreCameraDialog(String cameraName);

  /**
   * Displays a dialog showing the list of cameras stored in home
   * and returns the ones selected by the user to be deleted.
   */
  public abstract List<Camera> showDeletedCamerasDialog();

   /**
   * Returns <code>true</code> if clipboard contains data that
   * components are able to handle.
   */
  public abstract boolean isClipboardEmpty();

  /**
   * Returns the list of selectable items that are currently in clipboard
   * or <code>null</code> if clipboard doesn't contain any selectable item.
   * @since 5.0
   */
  public abstract List<Selectable> getClipboardItems();

  /**
   * Displays the given message and returns <code>false</code> if the user
   * doesn't want to be informed of the displayed updates and <code>showOnlyMessage</code> is <code>false</code>.
   */
  public abstract boolean showUpdatesMessage(String updatesMessage, boolean showOnlyMessage);

  /**
   * Execute <code>runnable</code> asynchronously in the thread
   * that manages toolkit events.
   */
  public abstract void invokeLater(Runnable runnable);
}