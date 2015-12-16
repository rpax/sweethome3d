package org.sunflow.image.readers;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.sunflow.image.Bitmap;
import org.sunflow.image.BitmapReader;
import org.sunflow.image.Color;
import org.sunflow.image.formats.BitmapRGB8;

public class JPGBitmapReader implements BitmapReader {
    public Bitmap load(String filename, boolean isLinear) throws IOException, BitmapFormatException {
        // EP : Try to read filename as an URL or as a file
        InputStream f;
        try {
            // Let's try first to read filename as an URL
            URLConnection connection = new URL(filename).openConnection();
            if (connection instanceof JarURLConnection) {
                JarURLConnection urlConnection = (JarURLConnection) connection;
                URL jarFileUrl = urlConnection.getJarFileURL();
                if (jarFileUrl.getProtocol().equalsIgnoreCase("file")) {
                    try {
                        if (new File(jarFileUrl.toURI()).canWrite()) {
                            // Refuse to use cache to be able to delete the writable files accessed with jar protocol,
                            // as suggested in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459
                            connection.setUseCaches(false);
                        }
                    } catch (URISyntaxException ex) {
                        throw new IOException(ex);
                    }
                }
            }
            f = connection.getInputStream();
        } catch (MalformedURLException ex) {
            // Let's try to read filename as a file
            f = new FileInputStream(filename);
        }

        BufferedImage bi;
        try {
            // regular image, load using Java api - ignore alpha channel
            bi = ImageIO.read(f);
        } finally {
            f.close();
        }
        
        if (bi.getType() != BufferedImage.TYPE_INT_RGB
            && bi.getType() != BufferedImage.TYPE_INT_ARGB) {
          // Transform as TYPE_INT_ARGB or TYPE_INT_RGB (much faster than calling image.getRGB())
          BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), 
                  bi.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
          Graphics2D g = (Graphics2D)tmp.getGraphics();
          g.drawImage(bi, null, 0, 0);
          g.dispose();
          bi = tmp;
        }
        // Retrieve image bits
        int [] imageBits = (int [])bi.getRaster().getDataElements(0, 0, bi.getWidth(), bi.getHeight(), null);
        // EP : End of modification

        int width = bi.getWidth();
        int height = bi.getHeight();
        byte[] pixels = new byte[3 * width * height];
        for (int y = 0, index = 0; y < height; y++) {
            for (int x = 0; x < width; x++, index += 3) {
                // EP : Retrieved image data with raster data 
                // int argb = bi.getRGB(x, height - 1 - y);
                int argb = imageBits [x + (height - 1 - y) * width];                
                // EP : End of modification
                pixels[index + 0] = (byte) (argb >> 16);
                pixels[index + 1] = (byte) (argb >> 8);
                pixels[index + 2] = (byte) argb;
            }
        }
        if (!isLinear) {
            for (int index = 0; index < pixels.length; index += 3) {
                pixels[index + 0] = Color.NATIVE_SPACE.rgbToLinear(pixels[index + 0]);
                pixels[index + 1] = Color.NATIVE_SPACE.rgbToLinear(pixels[index + 1]);
                pixels[index + 2] = Color.NATIVE_SPACE.rgbToLinear(pixels[index + 2]);
            }
        }
        return new BitmapRGB8(width, height, pixels);
    }
}