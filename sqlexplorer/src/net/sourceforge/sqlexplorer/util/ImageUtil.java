package net.sourceforge.sqlexplorer.util;

import java.net.URL;
import java.util.HashMap;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ImageUtil {

    private static HashMap _imageCount = new HashMap();

    private static HashMap _images = new HashMap();


    /**
     * Dispose of an image in cache. Once there are no more open handles to the
     * image it will be disposed of.
     * 
     */
    public static void disposeImage(String propertyName) {

        try {

            Image image = (Image) _images.get(propertyName);

            if (image == null) {
                return;
            }

            image.dispose();
            _images.remove(propertyName);

            // decrease image handle count by one

            Integer handleCount = (Integer) _imageCount.get(propertyName);

            if (handleCount == null) {
                handleCount = new Integer(0);
            } else {
                handleCount = new Integer(handleCount.intValue() - 1);
            }
            _imageCount.put(propertyName, handleCount);

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error disposing images", e);
        }
    }


    /**
     * Create an image descriptor for the given image property in the
     * text.properties file.
     * 
     * @param propertyName
     * @return
     */
    public static ImageDescriptor getDescriptor(String propertyName) {

        try {

            if (propertyName == null) {
                return null;
            }
            
            // get image path
            String path = Messages.getString(propertyName);

            if (path == null || path.trim().length() == 0) {
                SQLExplorerPlugin.error("Missing image path for " + propertyName, null);
                return null;
            }

            // create image
            URL url = URLUtil.getResourceURL(path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + propertyName, e);
            return null;
        }

    }


    public static ImageDescriptor getFragmentDescriptor(String fragmentId, String path) {
        
        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }
            
            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + fragmentId + ": " + path, e);
            return null;
        }
        
    }

    /**
     * Get an image object from cache or create one if it doesn't exist yet.
     * Everytime an object is retrieved, it should be disposed of using the
     * ImageUtil.disposeImage method.
     * 
     * @param propertyName
     */
    public static Image getImage(String propertyName) {

        Image image = (Image) _images.get(propertyName);

        if (image == null) {
            image = getDescriptor(propertyName).createImage();

            if (image == null) {
                return null;
            }

            _images.put(propertyName, image);
        }

        // increase image handle count by one

        Integer handleCount = (Integer) _imageCount.get(propertyName);

        if (handleCount == null) {
            handleCount = new Integer(1);
        } else {
            handleCount = new Integer(handleCount.intValue() + 1);
        }
        _imageCount.put(propertyName, handleCount);

        return image;
    }
    
    public static Image getFragmentImage(String fragmentId, String path) {
        
        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }
            
            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            if (descriptor == null) {
                return null;
            }
            return descriptor.createImage();

        } catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't create image for " + fragmentId + ": " + path, e);
            return null;
        }
    }
}
