/*
 * SwingTools.java 21 oct. 2008
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
package com.eteks.sweethome3d.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.j3d.ShapeTools;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret
 */
public class SwingTools {
  // Borders for focused views
  private static Border unfocusedViewBorder;
  private static Border focusedViewBorder;

  private SwingTools() {
    // This class contains only tools
  }

  /**
   * Updates the border of <code>component</code> with an empty border
   * changed to a colored border when it will gain focus.
   * If the <code>component</code> component is the child of a <code>JViewPort</code>
   * instance this border will be installed on its scroll pane parent.
   */
  public static void installFocusBorder(JComponent component) {
    if (unfocusedViewBorder == null) {
      Border unfocusedViewInteriorBorder = new AbstractBorder() {
          private Color  topLeftColor;
          private Color  botomRightColor;
          private Insets insets = new Insets(1, 1, 1, 1);

          {
            if (OperatingSystem.isMacOSX()) {
              this.topLeftColor = Color.GRAY;
              this.botomRightColor = Color.LIGHT_GRAY;
            } else {
              this.topLeftColor = UIManager.getColor("TextField.darkShadow");
              this.botomRightColor  = UIManager.getColor("TextField.shadow");
            }
          }

          public Insets getBorderInsets(Component c) {
            return this.insets;
          }

          public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color previousColor = g.getColor();
            Rectangle rect = getInteriorRectangle(c, x, y, width, height);
            g.setColor(topLeftColor);
            g.drawLine(rect.x - 1, rect.y - 1, rect.x + rect.width, rect.y - 1);
            g.drawLine(rect.x - 1, rect.y - 1, rect.x - 1, rect.y  + rect.height);
            g.setColor(botomRightColor);
            g.drawLine(rect.x, rect.y  + rect.height, rect.x + rect.width, rect.y  + rect.height);
            g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y  + rect.height);
            g.setColor(previousColor);
          }
        };

      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        unfocusedViewBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 2),
            unfocusedViewInteriorBorder);
        focusedViewBorder = new AbstractBorder() {
            private Insets insets = new Insets(3, 3, 3, 3);

            public Insets getBorderInsets(Component c) {
              return this.insets;
            }

            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
              Color previousColor = g.getColor();
              // Paint a gradient paint around component
              Rectangle rect = getInteriorRectangle(c, x, y, width, height);
              g.setColor(Color.GRAY);
              g.drawLine(rect.x - 1, rect.y - 1, rect.x + rect.width, rect.y - 1);
              g.drawLine(rect.x - 1, rect.y - 1, rect.x - 1, rect.y  + rect.height);
              g.setColor(Color.LIGHT_GRAY);
              g.drawLine(rect.x, rect.y  + rect.height, rect.x + rect.width, rect.y  + rect.height);
              g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y  + rect.height);
              Color focusColor = UIManager.getColor("Focus.color");
              int   transparencyOutline = 128;
              int   transparencyInline  = 180;
              if (focusColor == null) {
                focusColor = UIManager.getColor("textHighlight");
                transparencyOutline = 128;
                transparencyInline = 255;
              }
              g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), transparencyOutline));
              g.drawRoundRect(rect.x - 3, rect.y - 3, rect.width + 5, rect.height + 5, 6, 6);
              g.drawRect(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
              g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), transparencyInline));
              g.drawRoundRect(rect.x - 2, rect.y - 2, rect.width + 3, rect.height + 3, 4, 4);

              // Draw corners
              g.setColor(UIManager.getColor("Panel.background"));
              g.drawLine(rect.x - 3, rect.y - 3, rect.x - 2, rect.y - 3);
              g.drawLine(rect.x - 3, rect.y - 2, rect.x - 3, rect.y - 2);
              g.drawLine(rect.x + rect.width + 1, rect.y - 3, rect.x + rect.width + 2, rect.y - 3);
              g.drawLine(rect.x + rect.width + 2, rect.y - 2, rect.x + rect.width + 2, rect.y - 2);
              g.drawLine(rect.x - 3, rect.y + rect.height + 2, rect.x - 2, rect.y + rect.height + 2);
              g.drawLine(rect.x - 3, rect.y + rect.height + 1, rect.x - 3, rect.y + rect.height + 1);
              g.drawLine(rect.x + rect.width + 1, rect.y + rect.height + 2, rect.x + rect.width + 2, rect.y + rect.height + 2);
              g.drawLine(rect.x + rect.width + 2, rect.y + rect.height + 1, rect.x + rect.width + 2, rect.y + rect.height + 1);

              g.setColor(previousColor);
            }
          };
      } else {
        if (OperatingSystem.isMacOSX()) {
          unfocusedViewBorder = BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 1),
              unfocusedViewInteriorBorder);
        } else {
          unfocusedViewBorder = BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(1, 1, 1, 1),
              unfocusedViewInteriorBorder);
        }
        focusedViewBorder = BorderFactory.createLineBorder(UIManager.getColor("textHighlight"), 2);
      }
    }

    final JComponent feedbackComponent;
    if (component.getParent() instanceof JViewport
        && component.getParent().getParent() instanceof JScrollPane) {
      feedbackComponent = (JComponent)component.getParent().getParent();
    } else {
      feedbackComponent = component;
    }
    feedbackComponent.setBorder(unfocusedViewBorder);
    component.addFocusListener(new FocusListener() {
        public void focusLost(FocusEvent ev) {
          if (feedbackComponent.getBorder() == focusedViewBorder) {
            feedbackComponent.setBorder(unfocusedViewBorder);
          }
        }

        public void focusGained(FocusEvent ev) {
          if (feedbackComponent.getBorder() == unfocusedViewBorder) {
            feedbackComponent.setBorder(focusedViewBorder);
          }
        }
      });
  }

  /**
   * Updates the Swing resource bundles in use from the default Locale and class loader.
   */
  public static void updateSwingResourceLanguage() {
    updateSwingResourceLanguage(Arrays.asList(new ClassLoader [] {SwingTools.class.getClassLoader()}), null);
  }

  /**
   * Updates the Swing resource bundles in use from the preferences Locale and the class loaders of preferences.
   */
  public static void updateSwingResourceLanguage(UserPreferences preferences) {
    updateSwingResourceLanguage(preferences.getResourceClassLoaders(), preferences.getLanguage());
  }

  /**
   * Updates the Swing resource bundles in use from the preferences Locale and class loaders.
   */
  private static void updateSwingResourceLanguage(List<ClassLoader> classLoaders,
                                                  String language) {
    // Clear resource cache
    UIManager.getDefaults().removeResourceBundle(null);
    UIManager.getDefaults().setDefaultLocale(Locale.getDefault());
    // Read Swing localized properties because Swing doesn't update its internal strings automatically
    // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
    updateSwingResourceBundle("com.sun.swing.internal.plaf.metal.resources.metal", classLoaders, language);
    updateSwingResourceBundle("com.sun.swing.internal.plaf.basic.resources.basic", classLoaders, language);
    if (UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
      updateSwingResourceBundle("com.sun.java.swing.plaf.gtk.resources.gtk", classLoaders, language);
    } else if (UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel")) {
      updateSwingResourceBundle("com.sun.java.swing.plaf.motif.resources.motif", classLoaders, language);
    }
  }

  /**
   * Updates a Swing resource bundle in use from the current Locale.
   */
  private static void updateSwingResourceBundle(String swingResource,
                                                List<ClassLoader> classLoaders,
                                                String language) {
    ResourceBundle resource;
    try {
      Locale defaultLocale = language == null
          ? Locale.getDefault()
              : (language.indexOf('_') == -1
              ? new Locale(language)
              : new Locale(language.substring(0, 2), language.substring(3, 5)));
      resource = ResourceBundle.getBundle(swingResource, defaultLocale);
      for (ClassLoader classLoader : classLoaders) {
        ResourceBundle bundle = ResourceBundle.getBundle(swingResource, defaultLocale, classLoader);
        if (defaultLocale.equals(bundle.getLocale())) {
          resource = bundle;
          break;
        } else if (!resource.getLocale().getLanguage().equals(bundle.getLocale().getLanguage())
                  && defaultLocale.getLanguage().equals(bundle.getLocale().getLanguage())) {
          resource = bundle;
          // Don't break in case a bundle with language + country is found with an other class loader
        }
      }
    } catch (MissingResourceException ex) {
      resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
    }

    // Update UIManager properties
    final String textAndMnemonicSuffix = ".textAndMnemonic";
    for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements(); ) {
      String key = (String)it.nextElement();
      if (key.endsWith(textAndMnemonicSuffix)) {
        String value = resource.getString(key);
        UIManager.put(key, value);
        // Decompose property value like in javax.swing.UIDefaults.TextAndMnemonicHashMap because
        // UIDefaults#getResourceCache(Locale) doesn't store the correct localized value for non English resources
        // (.textAndMnemonic suffix appeared with Java 1.7)
        String text = value.replace("&", "");
        String keyPrefix = key.substring(0, key.length() - textAndMnemonicSuffix.length());
        UIManager.put(keyPrefix + "NameText", text);
        UIManager.put(keyPrefix + "Text", text);
        int index = value.indexOf('&');
        if (index >= 0 && index < value.length() - 1) {
          UIManager.put(key.replace(textAndMnemonicSuffix, "Mnemonic"),
              String.valueOf(Character.toUpperCase(value.charAt(index + 1))));
        }
      }
    }
    // Store other properties coming from read resource and give them a higher priority if already set in previous loop
    for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements(); ) {
      String key = (String)it.nextElement();
      if (!key.endsWith(textAndMnemonicSuffix)) {
        UIManager.put(key, resource.getString(key));
      }
    }
  }

  /**
   * Returns a localized text for menus items and labels depending on the system.
   */
  public static String getLocalizedLabelText(UserPreferences preferences,
                                             Class<?> resourceClass,
                                             String   resourceKey,
                                             Object ... resourceParameters) {
    String localizedString = preferences.getLocalizedString(resourceClass, resourceKey, resourceParameters);
    // Under Mac OS X, remove bracketed upper case roman letter used in oriental languages to indicate mnemonic
    String language = Locale.getDefault().getLanguage();
    if (OperatingSystem.isMacOSX()
        && (language.equals(Locale.CHINESE.getLanguage())
            || language.equals(Locale.JAPANESE.getLanguage())
            || language.equals(Locale.KOREAN.getLanguage())
            || language.equals("uk"))) {  // Ukrainian
      int openingBracketIndex = localizedString.indexOf('(');
      if (openingBracketIndex != -1) {
        int closingBracketIndex = localizedString.indexOf(')');
        if (openingBracketIndex == closingBracketIndex - 2) {
          char c = localizedString.charAt(openingBracketIndex + 1);
          if (c >= 'A' && c <= 'Z') {
            localizedString = localizedString.substring(0, openingBracketIndex)
                + localizedString.substring(closingBracketIndex + 1);
          }
        }
      }
    }
    return localizedString;
  }

  /**
   * Adds focus and mouse listeners to the given <code>textComponent</code> that will
   * select all its text when it gains focus by transfer.
   */
  public static void addAutoSelectionOnFocusGain(final JTextComponent textComponent) {
    // A focus and mouse listener able to select text field characters
    // when it gains focus after a focus transfer
    class SelectionOnFocusManager extends MouseAdapter implements FocusListener {
      private boolean mousePressedInTextField = false;
      private int selectionStartBeforeFocusLost = -1;
      private int selectionEndBeforeFocusLost = -1;

      @Override
      public void mousePressed(MouseEvent ev) {
        this.mousePressedInTextField = true;
        this.selectionStartBeforeFocusLost = -1;
      }

      public void focusLost(FocusEvent ev) {
        if (ev.getOppositeComponent() == null
            || SwingUtilities.getWindowAncestor(ev.getOppositeComponent())
                != SwingUtilities.getWindowAncestor(textComponent)) {
          // Keep selection indices when focus on text field is transfered
          // to an other window
          this.selectionStartBeforeFocusLost = textComponent.getSelectionStart();
          this.selectionEndBeforeFocusLost = textComponent.getSelectionEnd();
        } else {
          this.selectionStartBeforeFocusLost = -1;
        }
      }

      public void focusGained(FocusEvent ev) {
        if (this.selectionStartBeforeFocusLost != -1) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Reselect the same characters in text field
                textComponent.setSelectionStart(selectionStartBeforeFocusLost);
                textComponent.setSelectionEnd(selectionEndBeforeFocusLost);
              }
            });
        } else if (!this.mousePressedInTextField
                   && ev.getOppositeComponent() != null
                   && SwingUtilities.getWindowAncestor(ev.getOppositeComponent())
                       == SwingUtilities.getWindowAncestor(textComponent)) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Select all characters when text field got the focus because of a transfer
                textComponent.selectAll();
              }
            });
        }
        this.mousePressedInTextField = false;
      }
    };

    SelectionOnFocusManager selectionOnFocusManager = new SelectionOnFocusManager();
    textComponent.addFocusListener(selectionOnFocusManager);
    textComponent.addMouseListener(selectionOnFocusManager);
  }

  /**
   * Forces radio buttons to be deselected even if they belong to a button group.
   */
  public static void deselectAllRadioButtons(JRadioButton ... radioButtons) {
    for (JRadioButton radioButton : radioButtons) {
      if (radioButton != null) {
        ButtonGroup group = ((JToggleButton.ToggleButtonModel)radioButton.getModel()).getGroup();
        group.remove(radioButton);
        radioButton.setSelected(false);
        group.add(radioButton);
      }
    }
  }

  /**
   * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components.
   */
  public static int showConfirmDialog(JComponent parentComponent,
                                      JComponent messageComponent,
                                      String title,
                                      final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent,
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    parentComponent = SwingUtilities.getRootPane(parentComponent);
    if (parentComponent != null) {
      optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
    }
    final JDialog dialog = optionPane.createDialog(parentComponent, title);
    if (focusedComponent != null) {
      // Add a listener that transfer focus to focusedComponent when dialog is shown
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            requestFocusInWindow(focusedComponent);
            dialog.removeComponentListener(this);
          }
        });
    }
    dialog.setVisible(true);

    dialog.dispose();
    Object value = optionPane.getValue();
    if (value instanceof Integer) {
      return (Integer)value;
    } else {
      return JOptionPane.CLOSED_OPTION;
    }
  }

  /**
   * Requests the focus for the given component.
   */
  public static void requestFocusInWindow(final JComponent focusedComponent) {
    if (!focusedComponent.requestFocusInWindow()) {
      // Prefer to call requestFocusInWindow in a timer with a small delay
      // than calling it with EnventQueue#invokeLater to ensure it always works
      new Timer(50, new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            focusedComponent.requestFocusInWindow();
            ((Timer)ev.getSource()).stop();
          }
        }).start();
    }
  }

  /**
   * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components.
   */
  public static void showMessageDialog(JComponent parentComponent,
                                       JComponent messageComponent,
                                       String title,
                                       int messageType,
                                       final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent, messageType, JOptionPane.DEFAULT_OPTION);
    parentComponent = SwingUtilities.getRootPane(parentComponent);
    if (parentComponent != null) {
      optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
    }
    final JDialog dialog = optionPane.createDialog(parentComponent, title);
    if (focusedComponent != null) {
      // Add a listener that transfer focus to focusedComponent when dialog is shown
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            requestFocusInWindow(focusedComponent);
            dialog.removeComponentListener(this);
          }
        });
    }
    dialog.setVisible(true);
    dialog.dispose();
  }

  /**
   * Displays message in a dialog box, possibly adjusting font size if required.
   */
  public static int showOptionDialog(Component parentComponent,
                                     String message, String title,
                                     int optionType, int messageType,
                                     Object[] options, Object initialValue) {
   if (SwingTools.getResolutionScale() > 1
       && message.indexOf("<font size=\"-2\">") != -1) {
     Font font = UIManager.getFont("OptionPane.font");
     if (font != null) {
       message = message.replace("<font size=\"-2\">", "<font size=\"" + Math.round(font.getSize() / 5f) + "\">");
     }
   }
   return JOptionPane.showOptionDialog(parentComponent, message, title, optionType,
       messageType, null, options, initialValue);
 }

  private static Map<TextureImage, BufferedImage> patternImages;

  /**
   * Returns the image matching a given pattern.
   */
  public static BufferedImage getPatternImage(TextureImage pattern,
                                              Color backgroundColor,
                                              Color foregroundColor) {
    if (patternImages == null) {
      patternImages = new HashMap<TextureImage, BufferedImage>();
    }
    BufferedImage image = new BufferedImage((int)pattern.getWidth(),
        (int)pattern.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    imageGraphics.setColor(backgroundColor);
    imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    // Get pattern image from cache
    BufferedImage patternImage = patternImages.get(pattern);
    if (patternImage == null) {
      try {
        InputStream imageInput = pattern.getImage().openStream();
        patternImage = ImageIO.read(imageInput);
        imageInput.close();
        patternImages.put(pattern, patternImage);
      } catch (IOException ex) {
        throw new IllegalArgumentException("Can't read pattern image " + pattern.getName());
      }
    }
    // Draw the pattern image with foreground color
    final int foregroundColorRgb = foregroundColor.getRGB() & 0xFFFFFF;
    imageGraphics.drawImage(Toolkit.getDefaultToolkit().createImage(
        new FilteredImageSource(patternImage.getSource(),
        new RGBImageFilter() {
          {
            this.canFilterIndexColorModel = true;
          }

          @Override
          public int filterRGB(int x, int y, int argb) {
            // Always use foreground color and alpha
            return (argb & 0xFF000000) | foregroundColorRgb;
          }
        })), 0, 0, null);
    imageGraphics.dispose();
    return image;
  }

  /**
   * Returns the border of a component where a user may drop objects.
   */
  public static Border getDropableComponentBorder() {
    Border border = null;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      border = UIManager.getBorder("InsetBorder.aquaVariant");
    }
    if (border == null) {
      border = BorderFactory.createLoweredBevelBorder();
    }
    return border;
  }

  /**
   * Displays the image referenced by <code>imageUrl</code> in an AWT window
   * disposed once an instance of <code>JFrame</code> or <code>JDialog</code> is displayed.
   * If the <code>imageUrl</code> is incorrect, nothing happens.
   */
  public static void showSplashScreenWindow(URL imageUrl) {
    try {
      final BufferedImage image = ImageIO.read(imageUrl);
      // Try to find an image scale without getResolutionScale()
      // because look and feel is probably not set yet
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      final float scale = OperatingSystem.isMacOSX()
          ? 1f
          : (float)Math.min(2, Math.max(1, Math.min(screenSize.getWidth() / 5 / image.getWidth(), screenSize.getHeight() / 5 / image.getHeight())));
      final Window splashScreenWindow = new Window(new Frame()) {
          @Override
          public void paint(Graphics g) {
            ((Graphics2D)g).scale(scale, scale);
            g.drawImage(image, 0, 0, this);
          }
        };

      splashScreenWindow.setSize((int)(image.getWidth() * scale), (int)(image.getHeight() * scale));
      splashScreenWindow.setLocationRelativeTo(null);
      splashScreenWindow.setVisible(true);

      Executors.newSingleThreadExecutor().execute(new Runnable() {
          public void run() {
            try {
              Thread.sleep(500);
              while (splashScreenWindow.isVisible()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      // If a JFrame or JDialog is showing, dispose splash window
                      try {
                        for (Window window : (Window[])Window.class.getMethod("getWindows").invoke(null)) {
                          if ((window instanceof JFrame || window instanceof JDialog)
                              && window.isShowing()) {
                            splashScreenWindow.dispose();
                            break;
                          }
                        }
                      } catch (Exception ex) {
                        // Even if splash screen will disappear quicker,
                        // use Frame#getFrames under Java 1.5 where Window#getWindows doesn't exist
                        for (Frame frame : Frame.getFrames()) {
                          if (frame.isShowing()) {
                            splashScreenWindow.dispose();
                            break;
                          }
                        }
                      }
                    }
                  });
                Thread.sleep(200);
              }
            } catch (InterruptedException ex) {
              EventQueue.invokeLater(new Runnable() {
                public void run() {
                  splashScreenWindow.dispose();
                }
              });
            };
          }
        });
    } catch (IOException ex) {
      // Ignore splash screen
    }
  }

  /**
   * Returns a new panel with a border and the given <code>title</code>
   */
  public static JPanel createTitledPanel(String title) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    Border panelBorder = BorderFactory.createTitledBorder(title);
    // For systems different from Mac OS X 10.5, add an empty border
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      panelBorder = BorderFactory.createCompoundBorder(
          panelBorder, BorderFactory.createEmptyBorder(0, 2, 2, 2));
    } else if (OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
      // Enlarge space at the top of the border
      panelBorder = BorderFactory.createCompoundBorder(
          panelBorder, BorderFactory.createEmptyBorder(10, 0, 0, 0));
    }
    titledPanel.setBorder(panelBorder);
    return titledPanel;
  }

  /**
   * Returns a scroll pane containing the given <code>component</code>
   * that always displays scroll bars under Mac OS X.
   */
  public static JScrollPane createScrollPane(JComponent component) {
    JScrollPane scrollPane = new JScrollPane(component);
    if (OperatingSystem.isMacOSX()) {
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    installFocusBorder(component);
    scrollPane.setMinimumSize(new Dimension());
    return scrollPane;
  }

  /**
   * Returns a scroll bar adjustment listener bound to the given <code>scrollPane</code> view
   * that updates view tool tip when its vertical scroll bar is adjusted.
   */
  public static AdjustmentListener createAdjustmentListenerUpdatingScrollPaneViewToolTip(final JScrollPane scrollPane) {
    return new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent ev) {
          Point screenLocation = MouseInfo.getPointerInfo().getLocation();
          Point point = new Point(screenLocation);
          Component view = scrollPane.getViewport().getView();
          SwingUtilities.convertPointFromScreen(point, view);
          if (scrollPane.isShowing()
              && scrollPane.getViewport().getViewRect().contains(point)) {
            MouseEvent mouseEvent = new MouseEvent(view, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
                0, point.x, point.y, 0, false, MouseEvent.NOBUTTON);
            if (isToolTipShowing()) {
              ToolTipManager.sharedInstance().mouseMoved(mouseEvent);
            }
          }
        }
      };
  }

  /**
   * Returns <code>true</code> if a tool tip is showing.
   */
  public static boolean isToolTipShowing() {
    for (Frame frame : Frame.getFrames()) {
      if (isToolTipShowing(frame)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isToolTipShowing(Container container) {
    if (container instanceof Window) {
      for (Window window : ((Window)container).getOwnedWindows()) {
        if (isToolTipShowing(window)) {
          return true;
        }
      }
    }
    for (int i = 0; i < container.getComponentCount(); i++) {
      Component child = container.getComponent(i);
      if (child instanceof JToolTip
            && child.isShowing()
          || child instanceof Container
            && isToolTipShowing((Container)child)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a listener that will update the given popup menu to hide disabled menu items.
   */
  public static void hideDisabledMenuItems(JPopupMenu popupMenu) {
    popupMenu.addPopupMenuListener(new MenuItemsVisibilityListener());
  }

  /**
   * A popup menu listener that displays only enabled menu items.
   */
  private static class MenuItemsVisibilityListener implements PopupMenuListener {
    public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
      JPopupMenu popupMenu = (JPopupMenu)ev.getSource();
      hideDisabledMenuItems(popupMenu);
      // Ensure at least one item is visible
      boolean allItemsInvisible = true;
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        if (popupMenu.getComponent(i).isVisible()) {
          allItemsInvisible = false;
          break;
        }
      }
      if (allItemsInvisible
          && popupMenu.getComponentCount() > 0) {
        popupMenu.getComponent(0).setVisible(true);
      }
    }

    /**
     * Makes useless menu items invisible.
     */
    private void hideDisabledMenuItems(JPopupMenu popupMenu) {
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        Component component = popupMenu.getComponent(i);
        if (component instanceof JMenu) {
          boolean containsEnabledItems = containsEnabledItems((JMenu)component);
          component.setVisible(containsEnabledItems);
          if (containsEnabledItems) {
            hideDisabledMenuItems(((JMenu)component).getPopupMenu());
          }
        } else if (component instanceof JMenuItem) {
          Action action = ((JMenuItem)component).getAction();
          component.setVisible(component.isEnabled()
              && (action == null || !Boolean.FALSE.equals(action.getValue(ResourceAction.VISIBLE))));
        }
      }
      hideUselessSeparators(popupMenu);
    }

    /**
     * Makes useless separators invisible.
     */
    private void hideUselessSeparators(JPopupMenu popupMenu) {
      boolean allMenuItemsInvisible = true;
      int lastVisibleSeparatorIndex = -1;
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        Component component = popupMenu.getComponent(i);
        if (allMenuItemsInvisible && (component instanceof JMenuItem)) {
          if (component.isVisible()) {
            allMenuItemsInvisible = false;
          }
        } else if (component instanceof JSeparator) {
          component.setVisible(!allMenuItemsInvisible);
          if (!allMenuItemsInvisible) {
            lastVisibleSeparatorIndex = i;
          }
          allMenuItemsInvisible = true;
        }
      }
      if (lastVisibleSeparatorIndex != -1 && allMenuItemsInvisible) {
        // Check if last separator is the first visible component
        boolean allComponentsBeforeLastVisibleSeparatorInvisible = true;
        for (int i = lastVisibleSeparatorIndex - 1; i >= 0; i--) {
          if (popupMenu.getComponent(i).isVisible()) {
            allComponentsBeforeLastVisibleSeparatorInvisible = false;
            break;
          }
        }
        boolean allComponentsAfterLastVisibleSeparatorInvisible = true;
        for (int i = lastVisibleSeparatorIndex; i < popupMenu.getComponentCount(); i++) {
          if (popupMenu.getComponent(i).isVisible()) {
            allComponentsBeforeLastVisibleSeparatorInvisible = false;
            break;
          }
        }

        popupMenu.getComponent(lastVisibleSeparatorIndex).setVisible(
            !allComponentsBeforeLastVisibleSeparatorInvisible && !allComponentsAfterLastVisibleSeparatorInvisible);
      }
    }

    /**
     * Returns <code>true</code> if the given <code>menu</code> contains
     * at least one enabled menu item.
     */
    private boolean containsEnabledItems(JMenu menu) {
      boolean menuContainsEnabledItems = false;
      for (int i = 0; i < menu.getMenuComponentCount() && !menuContainsEnabledItems; i++) {
        Component component = menu.getMenuComponent(i);
        if (component instanceof JMenu) {
          menuContainsEnabledItems = containsEnabledItems((JMenu)component);
        } else if (component instanceof JMenuItem) {
          menuContainsEnabledItems = component.isEnabled();
        }
      }
      return menuContainsEnabledItems;
    }

    public void popupMenuCanceled(PopupMenuEvent ev) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
    }
  }

  /**
   * Attempts to display the given <code>url</code> in a browser and returns <code>true</code>
   * if it was done successfully.
   */
  public static boolean showDocumentInBrowser(URL url) {
    return BrowserSupport.showDocumentInBrowser(url);
  }

  /**
   * Separated static class to be able to exclude JNLP library from classpath.
   */
  private static class BrowserSupport {
    public static boolean showDocumentInBrowser(URL url) {
      try {
        // Lookup the javax.jnlp.BasicService object
        BasicService basicService = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
        // Ignore the basic service, if it doesn't support web browser
        if (basicService.isWebBrowserSupported()) {
          return basicService.showDocument(url);
        }
      } catch (UnavailableServiceException ex) {
        // Too bad : service is unavailable
      } catch (LinkageError ex) {
        // JNLP classes not available in classpath
        System.err.println("Can't show document in browser. JNLP classes not available in classpath.");
      }
      return false;
    }
  }

  /**
   * Returns the children of a component of the given class.
   */
  public static <T extends Component> List<T> findChildren(JComponent parent, Class<T> childrenClass) {
    List<T> children = new ArrayList<T>();
    findChildren(parent, childrenClass, children);
    return children;
  }

  private static <T extends Component> void findChildren(JComponent parent, Class<T> childrenClass, List<T> children) {
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component child = parent.getComponent(i);
      if (childrenClass.isInstance(child)) {
        children.add((T)child);
      } else if (child instanceof JComponent) {
        findChildren((JComponent)child, childrenClass, children);
      }
    }
  }

  /**
   * Returns <code>true</code> if the given rectangle is fully visible at screen.
   */
  public static boolean isRectangleVisibleAtScreen(Rectangle rectangle) {
    Area devicesArea = new Area();
    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (GraphicsDevice device : environment.getScreenDevices()) {
      devicesArea.add(new Area(device.getDefaultConfiguration().getBounds()));
    }
    return devicesArea.contains(rectangle);
  }

  /**
   * Returns a new custom cursor.
   */
  public static Cursor createCustomCursor(URL smallCursorImageUrl,
                                          URL largeCursorImageUrl,
                                          float xCursorHotSpot,
                                          float yCursorHotSpot,
                                          String cursorName,
                                          Cursor defaultCursor) {
    if (GraphicsEnvironment.isHeadless()) {
      return defaultCursor;
    }
    // Retrieve system cursor size
    Dimension cursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
    URL cursorImageResource;
    // If returned cursor size is 0, system doesn't support custom cursor
    if (cursorSize.width == 0) {
      return defaultCursor;
    } else {
      // Use a different cursor image depending on system cursor size
      if (cursorSize.width > 16) {
        cursorImageResource = largeCursorImageUrl;
      } else {
        cursorImageResource = smallCursorImageUrl;
      }
      try {
        // Read cursor image
        BufferedImage cursorImage = ImageIO.read(cursorImageResource);
        // Create custom cursor from image
        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage,
            new Point(Math.min(cursorSize.width - 1, Math.round(cursorSize.width * xCursorHotSpot)),
                      Math.min(cursorSize.height - 1, Math.round(cursorSize.height * yCursorHotSpot))),
            cursorName);
      } catch (IOException ex) {
        throw new IllegalArgumentException("Unknown resource " + cursorImageResource);
      }
    }
  }

  /**
   * Returns <code>image</code> size in pixels.
   * @return the size or <code>null</code> if the information isn't given in the meta data of the image
   */
  public static Dimension getImageSizeInPixels(Content image) throws IOException {
    InputStream in = null;
    try {
      in = image.openStream();
      ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
      Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputStream);
      if (it.hasNext()) {
        ImageReader reader = (ImageReader)it.next();
        reader.setInput(imageInputStream);
        int imageWidth = reader.getWidth(reader.getMinIndex());
        int imageHeight = reader.getHeight(reader.getMinIndex());
        reader.dispose();
        return new Dimension(imageWidth, imageHeight);
      }
      return null;
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  /**
   * Returns the line stroke matching the given line styles.
   */
  public static Stroke getStroke(float thickness,
                                 Polyline.CapStyle capStyle,
                                 Polyline.JoinStyle joinStyle,
                                 Polyline.DashStyle dashStyle) {
    return ShapeTools.getStroke(thickness, capStyle, joinStyle, dashStyle.getDashPattern(), 0);
  }

  private static Float defaultResolutionScale;

  /**
   * Updates Swing components default size according to resolution scale.
   */
  static void updateComponentDefaults() {
    if (defaultResolutionScale == null) {
      try {
        defaultResolutionScale = 1f;
        if ((OperatingSystem.isLinux()
              || OperatingSystem.isWindows() && !OperatingSystem.isJavaVersionGreaterOrEqual("1.9"))
            && UIManager.getLookAndFeel().getClass().isAssignableFrom(Class.forName(UIManager.getSystemLookAndFeelClassName()))) {
          int defaultPanelFontSize = new BasicLookAndFeel() {
              public String getDescription() {
                return null;
              }

              public String getID() {
                return null;
              }

              public String getName() {
                return null;
              }

              public boolean isNativeLookAndFeel() {
                return false;
              }

              public boolean isSupportedLookAndFeel() {
                return false;
              }
            }.getDefaults().getFont("Panel.font").getSize();
          // Try to guess current resolution scale by comparing default font size with the one of the look and feel
          defaultResolutionScale = (float)UIManager.getFont("Panel.font").getSize() / defaultPanelFontSize;
        }
      } catch (ClassNotFoundException ex) {
        // Issue with LAF classes
      }
    }

    float userResolutionScale = getUserResolutionScale();
    if (userResolutionScale != 1) {
      Font buttonFont = updateComponentFontSize("Button.font", userResolutionScale);
      updateComponentFontSize("ToggleButton.font", userResolutionScale);
      updateComponentFontSize("RadioButton.font", userResolutionScale);
      updateComponentFontSize("CheckBox.font", userResolutionScale);
      updateComponentFontSize("ColorChooser.font", userResolutionScale);
      updateComponentFontSize("ComboBox.font", userResolutionScale);
      updateComponentFontSize("InternalFrame.titleFont", userResolutionScale);
      Font labelFont = updateComponentFontSize("Label.font", userResolutionScale);
      updateComponentFontSize("List.font", userResolutionScale);
      updateComponentFontSize("MenuBar.font", userResolutionScale);
      updateComponentFontSize("MenuItem.font", userResolutionScale);
      updateComponentFontSize("MenuItem.acceleratorFont", userResolutionScale);
      updateComponentFontSize("RadioButtonMenuItem.font", userResolutionScale);
      updateComponentFontSize("RadioButtonMenuItem.acceleratorFont", userResolutionScale);
      updateComponentFontSize("CheckBoxMenuItem.font", userResolutionScale);
      updateComponentFontSize("CheckBoxMenuItem.acceleratorFont", userResolutionScale);
      updateComponentFontSize("Menu.font", userResolutionScale);
      updateComponentFontSize("Menu.acceleratorFont", userResolutionScale);
      updateComponentFontSize("PopupMenu.font", userResolutionScale);
      updateComponentFontSize("OptionPane.font", userResolutionScale);
      updateComponentFontSize("Panel.font", userResolutionScale);
      updateComponentFontSize("ProgressBar.font", userResolutionScale);
      updateComponentFontSize("ScrollPane.font", userResolutionScale);
      updateComponentFontSize("Viewport.font", userResolutionScale);
      updateComponentFontSize("Slider.font", userResolutionScale);
      updateComponentFontSize("Spinner.font", userResolutionScale);
      updateComponentFontSize("Table.font", userResolutionScale);
      updateComponentFontSize("TabbedPane.font", userResolutionScale);
      updateComponentFontSize("TableHeader.font", userResolutionScale);
      updateComponentFontSize("TextField.font", userResolutionScale);
      updateComponentFontSize("FormattedTextField.font", userResolutionScale);
      updateComponentFontSize("PasswordField.font", userResolutionScale);
      updateComponentFontSize("TextArea.font", userResolutionScale);
      updateComponentFontSize("EditorPane.font", userResolutionScale);
      updateComponentFontSize("TitledBorder.font", userResolutionScale);
      updateComponentFontSize("ToolBar.font", userResolutionScale);
      updateComponentFontSize("ToolTip.font", userResolutionScale);
      updateComponentFontSize("Tree.font", userResolutionScale);
      UIManager.put("OptionPane.messageFont", labelFont);
      UIManager.put("OptionPane.buttonFont", buttonFont);
    }
    updateComponentSize("SplitPane.dividerSize", getResolutionScale());
  }

  private static Font updateComponentFontSize(String fontKey, float resolutionScale) {
    Font font = UIManager.getFont(fontKey);
    if (font != null) {
      font = font.deriveFont(font.getSize() * resolutionScale);
      UIManager.put(fontKey, font);
    }
    return font;
  }

  private static int updateComponentSize(String sizeKey, float resolutionScale) {
    int size = UIManager.getInt(sizeKey);
    if (size != 0) {
      size = Math.round(size * resolutionScale);
      UIManager.put(sizeKey, size);
    }
    return size;
  }

  /**
   * Returns a scale factor used to adapt user interface items to screen resolution.
   */
  public static float getResolutionScale() {
    float defaultResolutionScale = SwingTools.defaultResolutionScale != null
        ? SwingTools.defaultResolutionScale
        : 1f;
    return defaultResolutionScale * getUserResolutionScale();
  }

  /**
   * Returns an additional user scale factor for the user interface items.
   */
  private static float getUserResolutionScale() {
    try {
      String resolutionScaleProperty = System.getProperty("com.eteks.sweethome3d.resolutionScale");
      if (resolutionScaleProperty != null) {
        return Float.parseFloat(resolutionScaleProperty.trim());
      } else {

      }
    } catch (AccessControlException ex) {
    } catch (NumberFormatException ex) {
      // Ignore resolution
    }
    return 1f;
  }

  /**
   * Returns an image icon scaled according to the value returned by {@link #getResolutionScale()}.
   */
  public static ImageIcon getScaledImageIcon(URL imageUrl) {
    float resolutionScale = getResolutionScale();
    if (resolutionScale == 1) {
      return new ImageIcon(imageUrl);
    } else {
      try {
        BufferedImage image = ImageIO.read(imageUrl);
        Image scaledImage = image.getScaledInstance(Math.round(image.getWidth() * resolutionScale),
            Math.round(image.getHeight() * resolutionScale), Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
      } catch (IOException ex) {
        return null;
      }
    }
  }
}
