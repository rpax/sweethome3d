/*
 * FurnitureCatalogListPanel.java 10 janv 2010
 *
 * Sweet Home 3D, Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A furniture catalog view that displays furniture in a list, with a combo and search text field.
 * @author Emmanuel Puybaret
 */
public class FurnitureCatalogListPanel extends JPanel implements View {
  private ListSelectionListener listSelectionListener;
  private JLabel                categoryFilterLabel;
  private JComboBox             categoryFilterComboBox;
  private JLabel                searchLabel;
  private JTextField            searchTextField;
  private JList                 catalogFurnitureList;

  /**
   * Creates a panel that displays <code>catalog</code> furniture in a list with a filter combo box
   * and a search field.
   */
  public FurnitureCatalogListPanel(FurnitureCatalog catalog,
                                   UserPreferences preferences,
                                   FurnitureCatalogController controller) {
    super(new GridBagLayout());
    createComponents(catalog, preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }

  /**
   * Creates the components displayed by this panel.
   */
  private void createComponents(FurnitureCatalog catalog,
                                final UserPreferences preferences,
                                final FurnitureCatalogController controller) {
    final FurnitureCatalogListModel catalogListModel = new FurnitureCatalogListModel(catalog);
    this.catalogFurnitureList = new JList(catalogListModel) {
        private CatalogItemToolTip toolTip = new CatalogItemToolTip(false, preferences);
        private boolean mousePressed;
        private boolean firstScroll;

        {
          addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
              firstScroll = true;
              mousePressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
              mousePressed = false;
            }
          });
        }

        @Override
        public JToolTip createToolTip() {
          if (this.toolTip.isTipTextComplete()) {
            // Use toolTip object only for its text returned in getToolTipText
            return super.createToolTip();
          } else {
            this.toolTip.setComponent(this);
            return this.toolTip;
          }
        }

        @Override
        public String getToolTipText(MouseEvent ev) {
          // Return a tooltip for furniture pieces described in the list.
          int index = locationToIndex(ev.getPoint());
          if (index != -1) {
            this.toolTip.setCatalogItem((CatalogPieceOfFurniture)getModel().getElementAt(index));
            return this.toolTip.getTipText();
          } else {
            return null;
          }
        }

        @Override
        public void scrollRectToVisible(Rectangle rectangle) {
          if (!this.mousePressed
              || this.firstScroll) {
            // During a drag and drop, let's accept only the first viewport adjustment provoked by UI
            this.firstScroll = false;
            super.scrollRectToVisible(rectangle);
          }
        }
      };
    this.catalogFurnitureList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    this.catalogFurnitureList.setCellRenderer(new CatalogCellRenderer());
    this.catalogFurnitureList.setAutoscrolls(false);
    if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
      this.catalogFurnitureList.setDragEnabled(true);
    }
    this.catalogFurnitureList.setTransferHandler(null);
    // Remove Select all action
    this.catalogFurnitureList.getActionMap().getParent().remove("selectAll");
    addDragListener(this.catalogFurnitureList);
    addMouseListeners(this.catalogFurnitureList, controller);

    catalogListModel.addListDataListener(new ListDataListener() {
        public void contentsChanged(ListDataEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }

        public void intervalAdded(ListDataEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }

        public void intervalRemoved(ListDataEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }
      });
    this.catalogFurnitureList.addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }

        public void ancestorMoved(AncestorEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }

        public void ancestorRemoved(AncestorEvent ev) {
        }
      });
    addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent ev) {
          spreadFurnitureIconsAlongListWidth();
        }
      });

    updateListSelectedFurniture(catalog, controller);
    addSelectionListeners(catalog, controller);

    this.categoryFilterLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
    List<FurnitureCategory> categories = new ArrayList<FurnitureCategory>();
    categories.add(null);
    categories.addAll(catalog.getCategories());
    this.categoryFilterComboBox = new JComboBox(new DefaultComboBoxModel(categories.toArray())) {
        @Override
        public Dimension getMinimumSize() {
          return new Dimension(60, super.getMinimumSize().height);
        }
      };
    this.categoryFilterComboBox.setMaximumRowCount(20);
    this.categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
          if (value == null) {
            return super.getListCellRendererComponent(list,
                preferences.getLocalizedString(FurnitureCatalogListPanel.class, "categoryFilterComboBox.noCategory"),
                index, isSelected, cellHasFocus);
          } else {
            return super.getListCellRendererComponent(list,
                ((FurnitureCategory)value).getName(), index, isSelected, cellHasFocus);
          }
        }
      });
    this.categoryFilterComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          catalogListModel.setFilterCategory((FurnitureCategory)categoryFilterComboBox.getSelectedItem());
          catalogFurnitureList.clearSelection();
        }
      });

    this.searchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        FurnitureCatalogListPanel.class, "searchLabel.text"));
    this.searchTextField = new JTextField(5);
    this.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          Object selectedValue = catalogFurnitureList.getSelectedValue();
          catalogListModel.setFilterText(searchTextField.getText());
          catalogFurnitureList.clearSelection();
          catalogFurnitureList.setSelectedValue(selectedValue, true);

          if (catalogListModel.getSize() == 1) {
            catalogFurnitureList.setSelectedIndex(0);
          }
        }

        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }

        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
    this.searchTextField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "deleteContent");
    this.searchTextField.getActionMap().put("deleteContent", new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
          searchTextField.setText("");
        }
      });
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      this.searchTextField.putClientProperty("JTextField.variant", "search");
    }

    PreferencesChangeListener preferencesChangeListener = new PreferencesChangeListener(this);
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesChangeListener);
    catalog.addFurnitureListener(preferencesChangeListener);
  }

  /**
   * Language and catalog listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  private static class PreferencesChangeListener implements PropertyChangeListener, CollectionListener<CatalogPieceOfFurniture> {
    private final WeakReference<FurnitureCatalogListPanel> furnitureCatalogPanel;

    public PreferencesChangeListener(FurnitureCatalogListPanel furnitureCatalogPanel) {
      this.furnitureCatalogPanel = new WeakReference<FurnitureCatalogListPanel>(furnitureCatalogPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If panel was garbage collected, remove this listener from preferences
      FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (furnitureCatalogPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        furnitureCatalogPanel.categoryFilterLabel.setText(SwingTools.getLocalizedLabelText(preferences,
            FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
        furnitureCatalogPanel.searchLabel.setText(SwingTools.getLocalizedLabelText(preferences,
            FurnitureCatalogListPanel.class, "searchLabel.text"));
        furnitureCatalogPanel.setMnemonics(preferences);
        // Categories listed in combo box are updated through collectionChanged
      }
    }

    public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
      // If panel was garbage collected, remove this listener from catalog
      FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
      FurnitureCatalog catalog = (FurnitureCatalog)ev.getSource();
      if (furnitureCatalogPanel == null) {
        catalog.removeFurnitureListener(this);
      } else {
        DefaultComboBoxModel model =
            (DefaultComboBoxModel)furnitureCatalogPanel.categoryFilterComboBox.getModel();
        FurnitureCategory category = ev.getItem().getCategory();
        List<FurnitureCategory> categories = catalog.getCategories();
        if (!categories.contains(category)) {
          model.removeElement(category);
          furnitureCatalogPanel.categoryFilterComboBox.setSelectedIndex(0);
        } else if (model.getIndexOf(category) == -1) {
          model.insertElementAt(category, categories.indexOf(category) + 1);
        }
      }
    }
  }

  /**
   * Adds mouse listeners that will select only the piece under mouse cursor in the furniture list
   * before the start of a drag operation, ensuring only one piece can be dragged at a time.
   */
  private void addDragListener(final JList catalogFurnitureList) {
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private CatalogPieceOfFurniture exportedPiece;

        @Override
        public void mousePressed(MouseEvent ev) {
          this.exportedPiece = null;
          if (SwingUtilities.isLeftMouseButton(ev)
              && catalogFurnitureList.getSelectedValue() != null
              && catalogFurnitureList.getTransferHandler() != null) {
            int index = catalogFurnitureList.locationToIndex(ev.getPoint());
            if (index != -1) {
              this.exportedPiece = (CatalogPieceOfFurniture)catalogFurnitureList.getModel().getElementAt(index);
            }
          }
        }

        public void mouseDragged(MouseEvent ev) {
          if (this.exportedPiece != null) {
            if (catalogFurnitureList.getSelectedIndices().length > 1) {
              catalogFurnitureList.clearSelection();
              catalogFurnitureList.setSelectedValue(this.exportedPiece, false);
            }
            if (!OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
              catalogFurnitureList.getTransferHandler().exportAsDrag(catalogFurnitureList, ev, DnDConstants.ACTION_COPY);
            }
            this.exportedPiece = null;
          }
        }
      };

    catalogFurnitureList.addMouseListener(mouseListener);
    catalogFurnitureList.addMouseMotionListener(mouseListener);
  }

  /**
   * Adds mouse listeners to the furniture list to modify selected furniture
   * and manage links in piece information.
   */
  private void addMouseListeners(final JList catalogFurnitureList,
                                 final FurnitureCatalogController controller) {
    final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    MouseInputAdapter mouseListener = new MouseInputAdapter () {
        @Override
        public void mouseClicked(MouseEvent ev) {
          if (SwingUtilities.isLeftMouseButton(ev)) {
            if (ev.getClickCount() == 2) {
              int clickedPieceIndex = catalogFurnitureList.locationToIndex(ev.getPoint());
              if (clickedPieceIndex != -1) {
                controller.modifySelectedFurniture();
              }
            } else {
              URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
              if (url != null) {
                SwingTools.showDocumentInBrowser(url);
              }
            }
          }
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
          final URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (url != null) {
                  setCursor(handCursor);
                } else {
                  setCursor(Cursor.getDefaultCursor());
                }
              }
            });
        }

        private URL getURLAt(Point point, JList list) {
          int pieceIndex = list.locationToIndex(point);
          if (pieceIndex != -1) {
            CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)list.getModel().getElementAt(pieceIndex);
            String information = piece.getInformation();
            if (information != null) {
              JComponent rendererComponent = (JComponent)list.getCellRenderer().
                  getListCellRendererComponent(list, piece, pieceIndex, list.isSelectedIndex(pieceIndex), false);
              for (JEditorPane pane : SwingTools.findChildren(rendererComponent, JEditorPane.class)) {
                Rectangle cellBounds = list.getCellBounds(pieceIndex, pieceIndex);
                point.x -= cellBounds.x;
                point.y -= cellBounds.y + pane.getY();
                if (point.x > 0 && point.y > 0) {
                  // Search in information pane if point is over a HTML link
                  int position = pane.viewToModel(point);
                  if (position > 1
                      && pane.getDocument() instanceof HTMLDocument) {
                    HTMLDocument hdoc = (HTMLDocument)pane.getDocument();
                    Element element = hdoc.getCharacterElement(position);
                    AttributeSet a = element.getAttributes();
                    AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
                    if (anchor != null) {
                      String href = (String)anchor.getAttribute(HTML.Attribute.HREF);
                      if (href != null) {
                        try {
                          return new URL(href);
                        } catch (MalformedURLException ex) {
                          // Ignore malformed URL
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          return null;
        }
      };
    catalogFurnitureList.addMouseListener(mouseListener);
    catalogFurnitureList.addMouseMotionListener(mouseListener);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.categoryFilterLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          FurnitureCatalogListPanel.class, "categoryFilterLabel.mnemonic")).getKeyCode());
      this.categoryFilterLabel.setLabelFor(this.categoryFilterComboBox);
      this.searchLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          FurnitureCatalogListPanel.class, "searchLabel.mnemonic")).getKeyCode());
      this.searchLabel.setLabelFor(this.searchTextField);
    }
  }

  /**
   * Layouts the components displayed by this panel.
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    // First row
    Insets labelInsets = new Insets(0, 2, standardGap, 3);
    Insets componentInsets = new Insets(0, 2, Math.round(3 * SwingTools.getResolutionScale()), 0);
    if (!OperatingSystem.isMacOSX()) {
      labelInsets.top = 2;
      componentInsets.top = 2;
      componentInsets.right = 2;
    }
    add(this.categoryFilterLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, labelInsets, 0, 0));
    add(this.categoryFilterComboBox, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    // Second row
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      add(this.searchTextField, new GridBagConstraints(
          0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, Math.round(3 * SwingTools.getResolutionScale()), 0), 0, 0));
    } else {
      add(this.searchLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.searchTextField, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    }
    // Last row
    final JScrollPane listScrollPane = SwingTools.createScrollPane(this.catalogFurnitureList);
    listScrollPane.getVerticalScrollBar().addAdjustmentListener(
        SwingTools.createAdjustmentListenerUpdatingScrollPaneViewToolTip(listScrollPane));
    if (OperatingSystem.isMacOSXHighSierraOrSuperior()
        && !OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
      // Add missing repaint calls on viewport when scroll bar is moved
      listScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
          public void adjustmentValueChanged(AdjustmentEvent ev) {
            listScrollPane.getViewport().repaint();
          }
        });
    }
    listScrollPane.setPreferredSize(new Dimension(250, 250));
    listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    add(listScrollPane,
        new GridBagConstraints(
        0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    setFocusTraversalPolicyProvider(true);
    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
        @Override
        public Component getDefaultComponent(Container aContainer) {
            EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Return furniture list only at the first request
                setFocusTraversalPolicyProvider(false);
              }
            });
          return catalogFurnitureList;
        }
      });
  }

  /**
   * Computes furniture list visible row count to ensure its horizontal scrollbar
   * won't be seen.
   */
  private void spreadFurnitureIconsAlongListWidth() {
    ListModel model = this.catalogFurnitureList.getModel();
    int size = model.getSize();
    int extentWidth = ((JViewport)this.catalogFurnitureList.getParent()).getExtentSize().width;
    ListCellRenderer cellRenderer = this.catalogFurnitureList.getCellRenderer();
    // Search max width and height
    int maxCellWidth = 1;
    int maxCellHeight = 0;
    for (int i = 0; i < size; i++) {
      Dimension cellPreferredSize = cellRenderer.getListCellRendererComponent(this.catalogFurnitureList, model.getElementAt(i),
          i, this.catalogFurnitureList.isSelectedIndex(i), false).getPreferredSize();
      maxCellWidth = Math.max(maxCellWidth, cellPreferredSize.width);
      maxCellHeight = Math.max(maxCellHeight, cellPreferredSize.height);
    }
    // Compute a fixed cell width that will spread
    int visibleItemsPerRow = Math.max(1, extentWidth / maxCellWidth);
    this.catalogFurnitureList.setVisibleRowCount(size % visibleItemsPerRow == 0
        ? size / visibleItemsPerRow
        : size / visibleItemsPerRow + 1);
    this.catalogFurnitureList.setFixedCellWidth(maxCellWidth + (extentWidth % maxCellWidth) / visibleItemsPerRow);
    // Set also cell height otherwise first calls to repaint done by icon manager won't repaint it
    // because the list have a null size at the beginning
    this.catalogFurnitureList.setFixedCellHeight(maxCellHeight);
  }

  /**
   * Adds the listeners that manage selection synchronization in this tree.
   */
  private void addSelectionListeners(final FurnitureCatalog catalog,
                                     final FurnitureCatalogController controller) {
    final SelectionListener modelSelectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent selectionEvent) {
          updateListSelectedFurniture(catalog, controller);
        }
      };
    this.listSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent ev) {
          // Updates selected furniture in catalog from selected nodes in tree.
          controller.removeSelectionListener(modelSelectionListener);
          controller.setSelectedFurniture(getSelectedFurniture());
          controller.addSelectionListener(modelSelectionListener);
        }
      };

    controller.addSelectionListener(modelSelectionListener);
    this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
  }

  /**
   * Updates selected items in list from <code>controller</code> selected furniture.
   */
  private void updateListSelectedFurniture(FurnitureCatalog catalog,
                                           FurnitureCatalogController controller) {
    if (this.listSelectionListener != null) {
      this.catalogFurnitureList.getSelectionModel().removeListSelectionListener(this.listSelectionListener);
    }

    this.catalogFurnitureList.clearSelection();
    List<CatalogPieceOfFurniture> selectedFurniture = controller.getSelectedFurniture();
    if (selectedFurniture.size() > 0) {
      ListModel model = this.catalogFurnitureList.getModel();
      List<Integer> selectedIndices = new ArrayList<Integer>();
      for (CatalogPieceOfFurniture piece : selectedFurniture) {
        for (int i = 0, n = model.getSize(); i < n; i++) {
          if (piece == model.getElementAt(i)) {
            selectedIndices.add(i);
            break;
          }
        }
      }
      if (selectedIndices.size() > 0) {
        int [] indices = new int [selectedIndices.size()];
        for (int i = 0; i < indices.length; i++) {
          indices [i] = selectedIndices.get(i);
        }
        this.catalogFurnitureList.setSelectedIndices(indices);
        this.catalogFurnitureList.ensureIndexIsVisible(indices [0]);
      }
    }

    if (this.listSelectionListener != null) {
      this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
    }
  }

  /**
   * Returns the selected furniture in list.
   */
  private List<CatalogPieceOfFurniture> getSelectedFurniture() {
    Object [] selectedValues = this.catalogFurnitureList.getSelectedValues();
    CatalogPieceOfFurniture [] selectedFurniture = new CatalogPieceOfFurniture [selectedValues.length];
    System.arraycopy(selectedValues, 0, selectedFurniture, 0, selectedValues.length);
    return Arrays.asList(selectedFurniture);
  }

  /**
   * Sets the transfer handler of the list displayed by this panel.
   */
  @Override
  public void setTransferHandler(TransferHandler handler) {
    this.catalogFurnitureList.setTransferHandler(handler);
  }

  /**
   * Returns the transfer handler of the list displayed by this panel.
   */
  @Override
  public TransferHandler getTransferHandler() {
    return this.catalogFurnitureList.getTransferHandler();
  }

  /**
   * Sets the popup menu of the list displayed by this panel.
   */
  @Override
  public void setComponentPopupMenu(JPopupMenu popup) {
    this.catalogFurnitureList.setComponentPopupMenu(popup);
  }

  /**
   * Returns the popup menu of the list displayed by this panel.
   */
  @Override
  public JPopupMenu getComponentPopupMenu() {
    return this.catalogFurnitureList.getComponentPopupMenu();
  }

  /**
   * Cell renderer for the furniture list.
   */
  private static class CatalogCellRenderer extends JComponent implements ListCellRenderer {
    private static final int DEFAULT_ICON_HEIGHT = Math.round(48 * SwingTools.getResolutionScale());
    private Font                    defaultFont;
    private Font                    modifiablePieceFont;
    private DefaultListCellRenderer nameLabel;
    private JEditorPane             informationPane;

    public CatalogCellRenderer() {
      setLayout(null);
      this.nameLabel = new DefaultListCellRenderer() {
          @Override
          public Dimension getPreferredSize() {
            return new Dimension(DEFAULT_ICON_HEIGHT * 3 / 2 + 5, super.getPreferredSize().height);
          }
        };
      this.nameLabel.setHorizontalTextPosition(JLabel.CENTER);
      this.nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
      this.nameLabel.setHorizontalAlignment(JLabel.CENTER);
      this.nameLabel.setText("-");
      this.nameLabel.setIcon(IconManager.getInstance().getWaitIcon(DEFAULT_ICON_HEIGHT));
      this.defaultFont = UIManager.getFont("ToolTip.font");
      this.modifiablePieceFont = new Font(this.defaultFont.getFontName(), Font.ITALIC, this.defaultFont.getSize());
      this.nameLabel.setFont(this.defaultFont);

      this.informationPane = new JEditorPane("text/html", "-");
      this.informationPane.setOpaque(false);
      this.informationPane.setEditable(false);
      String bodyRule = "body { font-family: " + this.defaultFont.getFamily() + "; "
          + "font-size: " + this.defaultFont.getSize() + "pt; "
          + "text-align: center; }";
      ((HTMLDocument)this.informationPane.getDocument()).getStyleSheet().addRule(bodyRule);

      add(this.nameLabel);
      add(this.informationPane);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
      // Configure name label with its icon, background and focus colors
      this.nameLabel.getListCellRendererComponent(list,
          value, index, isSelected, cellHasFocus);
      this.nameLabel.setText(" " + piece.getName() + " ");
      this.nameLabel.setIcon(getLabelIcon(list, piece.getIcon()));
      this.nameLabel.setFont(piece.isModifiable()
          ? this.modifiablePieceFont : this.defaultFont);

      this.informationPane.setText(piece.getInformation());
      return this;
    }

    @Override
    public void doLayout() {
      Dimension namePreferredSize = this.nameLabel.getPreferredSize();
      this.nameLabel.setSize(getWidth(), namePreferredSize.height);
      this.informationPane.setBounds(0, namePreferredSize.height,
          getWidth(), getHeight() - namePreferredSize.height);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension preferredSize = this.nameLabel.getPreferredSize();
      preferredSize.height += this.informationPane.getPreferredSize().height + 2;
      return preferredSize;
    }

    /**
     * The following methods are overridden for performance reasons.
     */
    @Override
    public void revalidate() {
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    @Override
    public void repaint(Rectangle r) {
    }

    @Override
    public void repaint() {
    }

    private Icon getLabelIcon(JList list, Content content) {
      return IconManager.getInstance().getIcon(content, DEFAULT_ICON_HEIGHT, list);
    }

    @Override
    protected void paintChildren(Graphics g) {
      // Force text anti aliasing on texts
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      super.paintChildren(g);
    }
  }

  /**
   * List model adaptor to CatalogPieceOfFurniture instances of catalog.
   */
  private static class FurnitureCatalogListModel extends AbstractListModel {
    private FurnitureCatalog                catalog;
    private List<CatalogPieceOfFurniture>   furniture;
    private FurnitureCategory               filterCategory;
    private String                          filterText;

    public FurnitureCatalogListModel(FurnitureCatalog catalog) {
      this.catalog = catalog;
      this.filterText = "";
      catalog.addFurnitureListener(new FurnitureCatalogListener(this));
    }

    public void setFilterCategory(FurnitureCategory filterCategory) {
      this.filterCategory = filterCategory;
      resetFurnitureList();
    }

    public void setFilterText(String filterText) {
      this.filterText = filterText;
      resetFurnitureList();
    }

    public Object getElementAt(int index) {
      checkFurnitureList();
      return this.furniture.get(index);
    }

    public int getSize() {
      checkFurnitureList();
      return this.furniture.size();
    }

    private void resetFurnitureList() {
      if (this.furniture != null) {
        this.furniture = null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              fireContentsChanged(this, -1, -1);
            }
          });
      }
    }

    private void checkFurnitureList() {
      if (this.furniture == null) {
        this.furniture = new ArrayList<CatalogPieceOfFurniture>();
        this.furniture.clear();
        for (FurnitureCategory category : this.catalog.getCategories()) {
          for (CatalogPieceOfFurniture piece : category.getFurniture()) {
            if ((this.filterCategory == null
                  || piece.getCategory().equals(this.filterCategory))
                && piece.matchesFilter(this.filterText)) {
              furniture.add(piece);
            }
          }
        }
        Collections.sort(this.furniture);
      }
    }

    /**
     * Catalog furniture listener bound to this list model with a weak reference to avoid
     * strong link between catalog and this list.
     */
    private static class FurnitureCatalogListener implements CollectionListener<CatalogPieceOfFurniture> {
      private WeakReference<FurnitureCatalogListModel>  listModel;

      public FurnitureCatalogListener(FurnitureCatalogListModel catalogListModel) {
        this.listModel = new WeakReference<FurnitureCatalogListModel>(catalogListModel);
      }

      public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
        // If catalog list model was garbage collected, remove this listener from catalog
        FurnitureCatalogListModel listModel = this.listModel.get();
        FurnitureCatalog catalog = (FurnitureCatalog)ev.getSource();
        if (listModel == null) {
          catalog.removeFurnitureListener(this);
        } else {
          listModel.resetFurnitureList();
        }
      }
    }
  }
}
