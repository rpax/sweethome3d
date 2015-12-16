package org.sunflow.image.readers;

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

public class BMPBitmapReader implements BitmapReader {
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
        // EP : End of modification

        int width = bi.getWidth();
        int height = bi.getHeight();
        byte[] pixels = new byte[3 * width * height];
        for (int y = 0, index = 0; y < height; y++) {
            for (int x = 0; x < width; x++, index += 3) {
                int argb = bi.getRGB(x, height - 1 - y);
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