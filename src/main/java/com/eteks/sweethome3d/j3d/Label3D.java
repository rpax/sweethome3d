/*
 * Label3D.java 7 avr. 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.UIManager;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.TextStyle;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;

/**
 * Root of a label branch.
 * @author Emmanuel Puybaret
 */
public class Label3D extends Object3DBranch {
  private static final TransparencyAttributes DEFAULT_TRANSPARENCY_ATTRIBUTES =
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0);
  private static final PolygonAttributes      DEFAULT_POLYGON_ATTRIBUTES =
      new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0, false);
  private static final TextureAttributes MODULATE_TEXTURE_ATTRIBUTES = new TextureAttributes();

  static {
    MODULATE_TEXTURE_ATTRIBUTES.setTextureMode(TextureAttributes.MODULATE);
  }

  private String      text;
  private TextStyle   style;
  private Integer     color;
  private Transform3D baseLineTransform;
  private Texture     texture;

  public Label3D(Label label, Home home, boolean waitForLoading) {
    setUserData(label);

    // Allow piece branch to be removed from its parent
    setCapability(BranchGroup.ALLOW_DETACH);
    setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

    update();
  }

  @Override
  public void update() {
    Label label = (Label)getUserData();
    Float pitch = label.getPitch();
    TextStyle style = label.getStyle();
    if (pitch != null
        && style != null
        && (label.getLevel() == null
            || label.getLevel().isViewableAndVisible())) {
      String text = label.getText();
      Integer color = label.getColor();
      Integer outlineColor = label.getOutlineColor();
      if (!text.equals(this.text)
          || (style == null && this.style != null)
          || (style != null && !style.equals(this.style))
          || (color == null && this.color != null)
          || (color != null && !color.equals(this.color))) {
        // If text, style and color changed, recompute label texture
        int fontStyle = Font.PLAIN;
        if (style.isBold()) {
          fontStyle = Font.BOLD;
        }
        if (style.isItalic()) {
          fontStyle |= Font.ITALIC;
        }
        Font defaultFont;
        if (style.getFontName() != null) {
          defaultFont = new Font(style.getFontName(), fontStyle, 1);
        } else {
          defaultFont = UIManager.getFont("TextField.font");
        }
        BasicStroke stroke = new BasicStroke(outlineColor != null ? style.getFontSize() * 0.05f : 0f);
        Font font = defaultFont.deriveFont(fontStyle, style.getFontSize() - stroke.getLineWidth());

        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D)dummyImage.getGraphics();
        FontMetrics fontMetrics = g2D.getFontMetrics(font);
        g2D.dispose();

        String [] lines = text.split("\n");
        float [] lineWidths = new float [lines.length];
        float textWidth = -Float.MAX_VALUE;
        float baseLineShift = 0;
        for (int i = 0; i < lines.length; i++) {
          Rectangle2D lineBounds = fontMetrics.getStringBounds(lines [i], null);
          if (i == 0) {
            baseLineShift = -(float)lineBounds.getY() + fontMetrics.getHeight() * (lines.length - 1);
          }
          lineWidths [i] = (float)lineBounds.getWidth() + 2 * stroke.getLineWidth();
          if (style.isItalic()) {
            lineWidths [i] += fontMetrics.getAscent() * 0.2;
          }
          textWidth = Math.max(lineWidths [i], textWidth);
        }

        float textHeight = (float)fontMetrics.getHeight() * lines.length + 2 * stroke.getLineWidth();
        float textRatio = (float)Math.sqrt((float)textWidth / textHeight);
        int width;
        int height;
        float scale;
        // Ensure that text image size is between 256x256 and 512x512 pixels
        if (textRatio > 1) {
          width = (int)Math.ceil(Math.max(255 * textRatio, Math.min(textWidth, 511 * textRatio)));
          scale = (float)(width / textWidth);
          height = (int)Math.ceil(scale * textHeight);
        } else {
          height = (int)Math.ceil(Math.max(255 * textRatio, Math.min(textHeight, 511 / textRatio)));
          scale = (float)(height / textHeight);
          width = (int)Math.ceil(scale * textWidth);
        }

        if (width > 0 && height > 0) {
          // Draw text in an image
          BufferedImage textureImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
          g2D = (Graphics2D)textureImage.getGraphics();
          g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

          g2D.setTransform(AffineTransform.getScaleInstance(scale, scale));
          g2D.translate(0, baseLineShift);
          for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines [i];
            float translationX;
            if (style.getAlignment() == TextStyle.Alignment.LEFT) {
              translationX = 0;
            } else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
              translationX = textWidth - lineWidths [i];
            } else { // CENTER
              translationX = (textWidth - lineWidths [i]) / 2;
            }
            translationX += stroke.getLineWidth() / 2;
            g2D.translate(translationX, 0);
            if (outlineColor != null) {
              g2D.setColor(new Color(outlineColor));
              g2D.setStroke(stroke);
              if (line.length() > 0) {
                TextLayout textLayout = new TextLayout(line, font, g2D.getFontRenderContext());
                g2D.draw(textLayout.getOutline(null));
              }
            }
            g2D.setFont(font);
            g2D.setColor(color != null ?  new Color(color) : UIManager.getColor("TextField.foreground"));
            g2D.drawString(line, 0f, 0f);
            g2D.translate(-translationX, -fontMetrics.getHeight());
          }
          g2D.dispose();

          Transform3D scaleTransform = new Transform3D();
          scaleTransform.setScale(new Vector3d(textWidth, 1, textHeight));
          // Move to the middle of base line
          this.baseLineTransform = new Transform3D();
          float translationX;
          if (style.getAlignment() == TextStyle.Alignment.LEFT) {
            translationX = textWidth / 2;
          } else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
            translationX = -textWidth / 2;
          } else { // CENTER
            translationX = 0;
          }
          this.baseLineTransform.setTranslation(new Vector3d(translationX, 0, textHeight / 2 - baseLineShift));
          this.baseLineTransform.mul(scaleTransform);
          this.texture = new TextureLoader(textureImage).getTexture();
          this.text = text;
          this.style = style;
          this.color = color;
        } else {
          clear();
        }
      }

      if (this.texture != null) {
        if (numChildren() == 0) {
          BranchGroup group = new BranchGroup();
          group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          group.setCapability(BranchGroup.ALLOW_DETACH);

          TransformGroup transformGroup = new TransformGroup();
          // Allow the change of the transformation that sets label size, position and orientation
          transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
          transformGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
          group.addChild(transformGroup);

          Appearance appearance = new Appearance();
          appearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, 0));
          appearance.setPolygonAttributes(DEFAULT_POLYGON_ATTRIBUTES);
          appearance.setTextureAttributes(MODULATE_TEXTURE_ATTRIBUTES);
          appearance.setTransparencyAttributes(DEFAULT_TRANSPARENCY_ATTRIBUTES);
          appearance.setTexCoordGeneration(new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
              TexCoordGeneration.TEXTURE_COORDINATE_2, new Vector4f(1, 0, 0, .5f), new Vector4f(0, 1, -1, .5f)));
          appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

          Box box = new Box(0.5f, 0f, 0.5f, appearance);
          Shape3D shape = box.getShape(Box.TOP);
          box.removeChild(shape);
          transformGroup.addChild(shape);

          addChild(group);
        }

        TransformGroup transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
        // Apply pitch rotation
        Transform3D pitchRotation = new Transform3D();
        pitchRotation.rotX(pitch);
        pitchRotation.mul(this.baseLineTransform);
        // Apply rotation around vertical axis
        Transform3D rotationY = new Transform3D();
        rotationY.rotY(-label.getAngle());
        rotationY.mul(pitchRotation);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(label.getX(), label.getGroundElevation() + (pitch == 0f && label.getElevation() < 0.1f ? 0.1f : 0), label.getY()));
        transform.mul(rotationY);
        transformGroup.setTransform(transform);
        ((Shape3D)transformGroup.getChild(0)).getAppearance().setTexture(this.texture);
      }
    } else {
      clear();
    }
  }

  /**
   * Removes children and clear fields.
   */
  private void clear() {
    removeAllChildren();
    this.text  = null;
    this.style = null;
    this.color = null;
    this.texture = null;
    this.baseLineTransform = null;
  }
}
