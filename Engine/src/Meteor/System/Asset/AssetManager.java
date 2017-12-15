package Meteor.System.Asset;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.sound.sampled.Clip;

import Meteor.Graphics.Bitmap;
import Meteor.Graphics.Sprites.Spritesheet;
import Meteor.System.Error;
import Meteor.System.Util;
import Meteor.System.Asset.Type.Audios.Audio;
import Meteor.System.Asset.Type.Audios.AudioManager;
import Meteor.System.Asset.Type.Fonts.Font;
import Meteor.System.Asset.Type.Fonts.FontManager;
import Meteor.System.Asset.Type.Images.Image;
import Meteor.System.Asset.Type.Images.ImageManager;

/**
 * <p>
 * The functional registrar of all assets used in the program.
 * Actual loading of assets is done in the loading menu.
 * </p>
 * <p>
 * <p>
 * Asset loading done in the same thread causes startup latency.
 * Larger assets (such as background music) will take significant
 * processing power. By utilizing deferred resource loading, the
 * resource reference can be declared prior to resource loading,
 * ensuring that the program launches responsively and in a more
 * user-friendly manner by showing a loading screen.
 * </p>
 */
public class AssetManager
{
    /**
     * The name of the class
     */
    private static final String CLASS_NAME = "assetManager";

    /**
     * The name of the hash-map
     */
    private static final String MAP_NAME = "registrar";

    /**
     * Dictionary-equivalent registrar for all program resources
     */
    private static final Map<String, Asset> REGISTRAR = new HashMap<>();

    /**
     * Indexing all deferred resource to be loaded during tile-up
     */
    private static final Queue<Asset> LOAD_QUEUE = new ArrayDeque<>();

    /**
     * An instance of AssetManager
     */
    public static AssetManager instance = new AssetManager();

    /**
     * Puts an asset into the registrar and (if unloaded) indexed to the load queue.
     * This method is invoked automatically in the `Asset` class.
     *
     * @param asset The asset to be registered.
     * @return Success indicator flag.
     */
    public static boolean register(Asset asset)
    {
        //Reject duplicated assets
        if (REGISTRAR.containsKey(asset.getKey()))
        {
            new Error(Error.KeyAlreadyExistsException(AssetManager.CLASS_NAME, asset.getKey(), AssetManager.MAP_NAME));
            return false;
        } else
        {
            //Register a new asset
            REGISTRAR.put(asset.getKey(), asset);
            Util.logAdd(AssetManager.CLASS_NAME, asset.getKey(), AssetManager.MAP_NAME);
            if (!asset.isLoaded()) LOAD_QUEUE.add(asset);
            return true;
        }
    }

    /**
     * Method used to cache an asset into memory.
     * <br>
     * Note: Must be called in load sequence or all assets will be null.
     */
    public static void load()
    {
        while (!isLoaded())
        {
            Asset asset = LOAD_QUEUE.peek();
            asset.load();
            Util.logCached(AssetManager.CLASS_NAME, asset.getFileName());
            AssetManager.distributeToManagers(asset);
            LOAD_QUEUE.remove(asset);
        }
    }

    /**
     * @return Weather or not the assets have been cached.
     */
    public static boolean isLoaded()
    {
        if (LOAD_QUEUE.isEmpty()) return true;
        else return false;
    }

    /**
     * Distributes an asset to its respective manager based on its type.
     *
     * @param asset The asset to be sent to its respective manager.
     */
    private static void distributeToManagers(Asset asset)
    {
        if (asset instanceof Image) ImageManager.add(asset.getKey(), (BufferedImage) asset.getData());
        else if (asset instanceof Audio) AudioManager.add(asset.getKey(), (Clip) asset.getData());
        else if (asset instanceof Font) FontManager.add(asset.getKey(), (Spritesheet) asset.getData());

    }

    /**
     * Removes a given asset from its respective manager by using its type.
     *
     * @param key  Unique identifier name.
     * @param type The type associated with the resource.
     */
    private static void removeFromManagers(String key, String type)
    {
        if (type.equalsIgnoreCase(Image.TYPE))
        {
            ImageManager.remove(key, true);
        } else if (type.equalsIgnoreCase(Audio.TYPE))
        {
            AudioManager.remove(key, true);
        } else if (type.equalsIgnoreCase(Font.TYPE))
        {
            FontManager.remove(key, true);
        }
    }

    /**
     * Removes a resource of a given type from the registrar.
     *
     * @param item The item to be removed.
     */
    public static void remove(Object item)
    {
        String type = "";

        if (item instanceof Image)
        {
            type = ((Image) item).getType();
        } else if (item instanceof Audio)
        {
            type = ((Audio) item).getType();
        } else if (item instanceof Font)
        {
            type = ((Font) item).getType();
        }

        remove(type, ((Asset) item).getName());
    }

    /**
     * Removes a resource with a given name from the registrar.
     *
     * @param type The type associated with the resource.
     * @param name The name of the resource.
     */
    public static void remove(String type, String name)
    {
        String key = createKey(type, name);

        //Check if key associated with the asset is in the registrar
        if (REGISTRAR.containsKey(key))
        {
            //Check the type of the asset and remove it from its respective manager
            removeFromManagers(key, type);
            REGISTRAR.remove(key);
            Util.logRemove(AssetManager.CLASS_NAME, key, AssetManager.MAP_NAME);
        } else new Error(Error.KeyNotFoundException(AssetManager.CLASS_NAME, key, AssetManager.MAP_NAME));
    }

    /**
     * Creates a unique identifer for a resource.
     *
     * @param type The type associated with the resource.
     * @param name The name of the resource.
     * @return A unique identifier for a resource.
     */
    public static String createKey(String type, String name)
    {
        return (type + ":" + name).toLowerCase();
    }

    /**
     * @return The asset load queue.
     */
    public synchronized static Queue<Asset> getLoadQueue()
    {
        return LOAD_QUEUE;
    }

    /**
     * Grabs a audio resource from the Registrar.
     *
     * @param name the unique identifier of the asset.
     * @return Audio resource.
     */
    public synchronized static Clip getAudio(String name)
    {
        return (Clip) REGISTRAR.get((Audio.TYPE + ":" + name).toLowerCase()).getData();
    }

    /**
     * Grabs an image resource from the Registrar.
     *
     * @param name the unique identifier of the asset.
     * @return Image resource.
     */
    public synchronized static Bitmap getImage(String name)
    {
        return (Bitmap) REGISTRAR.get((Image.TYPE + ":" + name).toLowerCase()).getData();
    }

    /**
     * Grabs a font resource from the Registrar.
     *
     * @param name the unique identifier of the asset.
     * @return Font resource.
     */
    public synchronized static Spritesheet getFont(String name)
    {
        return (Spritesheet) REGISTRAR.get((Font.TYPE + ":" + name).toLowerCase()).getData();
    }

    /**
     * @return The default font used by the engine.
     */
    public synchronized static Font getDefaultFont()
    {
        return (Font) REGISTRAR.get("$default");
    }

    /**
     * Method used to clean up memory used by
     * certain processes.
     */
    public synchronized static void cleanUp()
    {
        REGISTRAR.clear();
        LOAD_QUEUE.clear();

        ImageManager.cleanUp();
        AudioManager.cleanUp();
        FontManager.cleanUp();
    }

    /**
     * @return An instance of AssetManager.
     */
    public static AssetManager getInstance()
    {
        return instance;
    }
}