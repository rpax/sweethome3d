/*
 * Object3DBranchFactory.java 8 f�vr. 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;

/**
 * A factory able to create instances of {@link Object3DBranch Object3DBranch} class.
 * @author Emmanuel Puybaret
 */
public class Object3DBranchFactory implements Object3DFactory {
  private UserPreferences preferences;

  public Object3DBranchFactory() {
    this(null);
  }

  public Object3DBranchFactory(UserPreferences preferences) {
    this.preferences = preferences;
  }

  public boolean isDrawingModeEnabled() {
    return this.preferences != null && this.preferences.isDrawingModeEnabled();
  }

  /**
   * Returns the 3D object matching a given <code>item</code>.
   */
  public Object createObject3D(Home home, Selectable item, boolean waitForLoading) {
    if (item instanceof HomePieceOfFurniture) {
      return new HomePieceOfFurniture3D((HomePieceOfFurniture)item, home, !isDrawingModeEnabled(), waitForLoading);
    } else if (item instanceof Wall) {
      return new Wall3D((Wall)item, home, !isDrawingModeEnabled(), waitForLoading);
    } else if (item instanceof Room) {
      return new Room3D((Room)item, home, false, !isDrawingModeEnabled(), waitForLoading);
    } else if (item instanceof Polyline) {
      return new Polyline3D((Polyline)item, home);
    } else if (item instanceof Label) {
      return new Label3D((Label)item, home, waitForLoading);
    } else {
      return null;
    }
  }
}
