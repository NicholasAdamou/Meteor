package Meteor.Objects;


import Meteor.GameEngine.Interfaces.Renderable;
import Meteor.GameEngine.Interfaces.Updatable;
import Meteor.GameEngine.Manager;
import Meteor.Graphics.Context;
import Meteor.Graphics.Sprites.Animation;
import Meteor.Graphics.Sprites.Sprite;
import Meteor.Level.Level;
import Meteor.Units.Tuple2i;

/**
 * {@code Object} is a generic object class.
 * <br>
 * This class is a generic representation of a object.
 */
public abstract class Object implements Updatable, Renderable
{
    public static final int ANIMATABLE = 0x0; //A type of object that has an animation
    public static final int STATIC = 0x1; //A type of object that does not have an animation

    protected Manager manager;  //The engine manager object
    protected String name; //The name of the object
    private int type;
    protected Level level; //The level attached to the object
    protected Tuple2i location; //The objects location

    protected Animation currentAnimation; //The currently selected animation
    protected Sprite sprite; //The sprite of the object if no animation is specified
    protected float scale; //The scaling ratio
    protected Bounds bounds; //The object collision bounds

    private boolean isRemoved = false; //Weather or not the object was removed from a level

    /**
     * Defines a object in the game with a name, type and location in the level.
     *
     * @param manager  The game manager object.
     * @param name     The name of the object (e.g. player).
     * @param type     The type of the object (e.g. {@code Object.ANIMATABLE}).
     * @param sprite   The sprite of the object.
     * @param location The x, y location of the object.
     * @param scale    The scaling ratio (1f is 1:1 ratio).
     */
    public Object(Manager manager, String name, int type, Sprite sprite, Tuple2i location, float scale)
    {
        this.manager = manager;
        this.name = name.toLowerCase();
        this.type = type;
        this.sprite = sprite;
        this.location = location;
        this.scale = scale;
    }

    /**
     * Defines a object in the game with a name, type and location in the level.
     *
     * @param manager  The game manager object.
     * @param name     The name of the object (e.g. player).
     * @param type     The type of the object (e.g. {@code Object.ANIMATABLE}).
     * @param location The x, y location of the object.
     * @param scale    The scaling ratio (1f is 1:1 ratio).
     */
    public Object(Manager manager, String name, int type, Tuple2i location, float scale)
    {
        this.manager = manager;
        this.name = name.toLowerCase();
        this.type = type;
        this.location = location;
        this.scale = scale;
    }

    /**
     * Defines a object in the game with a name, type and location in the level.
     *
     * @param manager  The game manager object.
     * @param name     The name of the object (e.g. player).
     * @param type     The type of the object (e.g. {@code Object.ANIMATABLE}).
     * @param location The x, y location of the object.
     */
    public Object(Manager manager, String name, int type, Tuple2i location)
    {
        this.manager = manager;
        this.name = name.toLowerCase();
        this.type = type;
        this.location = location;

        setScale(1.0f);
    }

    /**
     * Method used to add this object to the level.
     *
     * @param level The level attached to the object.
     */
    public void init(Level level)
    {
        this.level = level;
    }

    @Override
    public void update(Manager manager, double delta)
    {
        if (type == Object.ANIMATABLE)
        {
            setSprite(currentAnimation);
            setBounds(sprite, location);

            currentAnimation.update();
            bounds.update(manager, delta);
        } else if (type == Object.STATIC)
        {
            setBounds(sprite, location);
            bounds.update(manager, delta);
        }
    }

    @Override
    public void render(Manager manager, Context ctx)
    {
        if (type == Object.ANIMATABLE)
        {
            setSprite(currentAnimation);
            setBounds(sprite, location);

            currentAnimation.render(ctx, location.x, location.y);
            bounds.render(manager, ctx);
        } else if (type == Object.STATIC)
        {
            setBounds(sprite, location);

            bounds.render(manager, ctx);
            ctx.renderBitmap(sprite, location.x, location.y);
        }
    }

    /**
     * Method used to check weather or not an object is colliding with another object.
     *
     * @param xOffset The x-offset of the object.
     * @param yOffset The y-offset of the object.
     * @return Weather or not an object is colliding with another object.
     */
    protected boolean isCollidingWithObject(int xOffset, int yOffset)
    {
        for (Object object : manager.getObjectManager().objectList)
        {
            if (object.name.equalsIgnoreCase(this.name)) continue;

            if (object.bounds != null && this.bounds != null)
            {
                if (object.bounds.getBounds(0, 0).intersects(this.bounds.getBounds(xOffset, yOffset))) return true;
            }
        }

        return false;
    }

    /**
     * Sets the animation to be displayed on screen.
     *
     * @param currentAnimation The animation to be displayed on screen.
     */
    public void setCurrentAnimation(Animation currentAnimation)
    {
        this.currentAnimation = currentAnimation;
    }

    /**
     * @return Weather or not an object has been removed from the level.
     */
    public boolean isRemoved()
    {
        return isRemoved;
    }

    /**
     * Switches the 'isRemoved' variable to true to indicate that the object has been removed from the level.
     */
    public void remove()
    {
        isRemoved = true;
    }

    /**
     * @return The width of the object.
     */
    public int getX()
    {
        return location.x;
    }

    /**
     * @return The height of the object.
     */
    public int getY()
    {
        return location.y;
    }

    /**
     * Sets the scale of the object
     *
     * @param scale Scaling ratio (1f is 1:1 ratio).
     */
    public void setScale(float scale)
    {
        this.scale = scale;
    }

    /**
     * @return The scaling ratio
     */
    public float getScale()
    {
        return scale;
    }

    /**
     * Method used to update the properties of the rectangle (e.g.
     * the x-location of the rectangle.).
     *
     * @param sprite   The sprite object containing the precise collision-bounds (rectangle).
     * @param location The location of the object (e.g. x, y).
     */
    public void setBounds(Sprite sprite, Tuple2i location)
    {
        bounds.bounds.setBounds(sprite, location);
    }

    /**
     * Sets the current sprite based on the current frame of a given animation sequence.
     *
     * @param animation The currently running animation.
     */
    public void setSprite(Animation animation)
    {
        this.sprite = animation.getSprite();
    }

    /**
     * @return The sprite of the object.
     */
    public Sprite getSprite()
    {
        return sprite;
    }
}