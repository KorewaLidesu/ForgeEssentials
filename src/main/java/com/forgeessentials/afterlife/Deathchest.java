package com.forgeessentials.afterlife;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.query.PermQueryPlayer;
import com.forgeessentials.data.api.ClassContainer;
import com.forgeessentials.data.api.DataStorageManager;
import com.forgeessentials.util.AreaSelector.WorldPoint;
import com.forgeessentials.util.OutputHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet100OpenWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import java.util.HashMap;

public class Deathchest {
    /**
     * This permission is needed to get the skull, Default = members.
     */
    public static final String PERMISSION_MAKE = ModuleAfterlife.BASEPERM + ".deathchest.make";

    /**
     * This is the permission that allows you to bypass the protection timer.
     */
    public static final String PERMISSION_BYPASS = ModuleAfterlife.BASEPERM + ".deathchest.protectionBypass";

    public static boolean enable;
    public static boolean enableXP;
    public static boolean enableFencePost;
    public static int protectionTime;

    public HashMap<String, Grave> gravemap = new HashMap<String, Grave>();
    private ClassContainer graveType = new ClassContainer(Grave.class);

    public Deathchest()
    {
        TileEntity.addMapping(FEskullTe.class, "FESkull");
        MinecraftForge.EVENT_BUS.register(this);
        TickRegistry.registerScheduledTickHandler(new GraveProtectionTicker(this), Side.SERVER);
    }

    public void load()
    {
        for (Object obj : DataStorageManager.getReccomendedDriver().loadAllObjects(graveType))
        {
            Grave grave = (Grave) obj;
            gravemap.put(grave.point.toString(), grave);
        }
    }

    public void save()
    {
        for (Grave grave : gravemap.values())
        {
            DataStorageManager.getReccomendedDriver().saveObject(graveType, grave);
        }
    }

    @ForgeSubscribe
    public void handleDeath(PlayerDropsEvent e)
    {
        if (!enable)
        {
            return;
        }
        if (!APIRegistry.perms.checkPermAllowed(new PermQueryPlayer(e.entityPlayer, PERMISSION_MAKE)))
        {
            return;
        }
        WorldPoint point = new WorldPoint(e.entityPlayer);
        if (point.y < 0)
        {
            OutputHandler.chatWarning(e.entityPlayer, "No deathchest for you as you fell out of the world!");
            return;
        }
        World world = e.entityPlayer.worldObj;
        if (world.isRemote)
        {
            return;
        }
        if (enableFencePost)
        {
            while (world.getBlockMaterial(point.x, point.y, point.z) == Material.water || world.getBlockMaterial(point.x, point.y, point.z) == Material.lava)
            {
                point.y++;
            }
            if (world.getBlockMaterial(point.x, point.y, point.z).isReplaceable() && world.getBlockMaterial(point.x, point.y + 1, point.z).isReplaceable())
            {
                e.setCanceled(true);
                world.setBlock(point.x, point.y, point.z, Block.fence.blockID);
                point.y++;
                new Grave(point, e.entityPlayer, e.drops, this);
                world.setBlock(point.x, point.y, point.z, Block.skull.blockID, 1, 1);
                FEskullTe te = new FEskullTe();
                te.setSkullType(3, e.entityPlayer.username);
                world.setBlockTileEntity(point.x, point.y, point.z, te);
                return;
            }
        }
        else
        {
            while (world.getBlockMaterial(point.x, point.y, point.z) == Material.water || world.getBlockMaterial(point.x, point.y, point.z) == Material.lava)
            {
                point.y++;
            }
            if (world.getBlockMaterial(point.x, point.y, point.z).isReplaceable())
            {
                e.setCanceled(true);
                world.setBlock(point.x, point.y, point.z, Block.skull.blockID, 1, 1);
                FEskullTe te = new FEskullTe();
                te.setSkullType(3, e.entityPlayer.username);
                world.setBlockTileEntity(point.x, point.y, point.z, te);
                return;
            }
        }
    }

    @ForgeSubscribe
    public void handleClick(PlayerInteractEvent e)
    {
        if (e.entity.worldObj.isRemote)
        {
            return;
        }

        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
        {
            WorldPoint point = new WorldPoint(e.entity.worldObj, e.x, e.y, e.z);
            if (gravemap.containsKey(point.toString()))
            {
                Grave grave = gravemap.get(point.toString());
                if (e.entity.worldObj.getBlockId(e.x, e.y, e.z) == Block.skull.blockID)
                {
                    if (!grave.canOpen(e.entityPlayer))
                    {
                        OutputHandler.chatWarning(e.entityPlayer, "This grave is still under divine protection.");
                        e.setCanceled(true);
                    }
                    else
                    {
                        EntityPlayerMP player = (EntityPlayerMP) e.entityPlayer;
                        if (grave.xp > 0)
                        {
                            player.addExperienceLevel(grave.xp);
                            grave.xp = 0;
                        }

                        if (player.openContainer != player.inventoryContainer)
                        {
                            player.closeScreen();
                        }
                        player.incrementWindowID();
                        grave.setOpen(true);

                        InventoryGrave invGrave = new InventoryGrave(grave);
                        player.playerNetServerHandler.sendPacketToPlayer(
                                new Packet100OpenWindow(player.currentWindowId, 0, invGrave.getInvName(), invGrave.getSizeInventory(), true));
                        player.openContainer = new ContainerChest(player.inventory, invGrave);
                        player.openContainer.windowId = player.currentWindowId;
                        player.openContainer.addCraftingToCrafters(player);

                        e.setCanceled(true);
                    }
                }
            }
        }
    }

    @ForgeSubscribe
    public void mineGrave(BreakEvent e)
    {
        WorldPoint point = new WorldPoint(e.world, e.x, e.y, e.z); // the grave, or fencepost if fence is enabled
        WorldPoint point2 = new WorldPoint(e.world, e.x, e.y + 1, e.z); // the grave, if fencepost is enabled
        if (e.world.isRemote)
        {
            return;
        }

        if (enableFencePost)
        {
            if (gravemap.containsKey(point2.toString()))
            {
                e.setCanceled(true);
                if (e.world.getBlockId(e.x, e.y, e.z) == Block.fence.blockID)
                {
                    OutputHandler.chatError(e.getPlayer(), "You may not defile the grave of a player.");
                }
                else
                {
                    Grave grave = gravemap.get(point2.toString());
                    removeGrave(grave, true);
                }
            }
            else if (gravemap.containsKey(point.toString()))
            {
                e.setCanceled(true);
                Grave grave = gravemap.get(point.toString());
                removeGrave(grave, true);
            }
        }

        else
        {
            if (gravemap.containsKey(point.toString()))
            {
                e.setCanceled(true);
                Grave grave = gravemap.get(point.toString());
                removeGrave(grave, true);
            }
        }
    }

    public void removeGrave(Grave grave, boolean mined)
    {
        if (grave == null)
        {
            return;
        }
        DataStorageManager.getReccomendedDriver().deleteObject(graveType, grave.point.toString());

        gravemap.remove(grave.point.toString());
        if (mined)
        {
            for (ItemStack is : grave.inv)
            {
                try
                {
                    EntityItem entity = new EntityItem(DimensionManager.getWorld(grave.point.dim), grave.point.x, grave.point.y, grave.point.z, is);
                    DimensionManager.getWorld(grave.point.dim).spawnEntityInWorld(entity);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        DimensionManager.getWorld(grave.point.dim).destroyBlock(grave.point.x, grave.point.y, grave.point.z, false);
        if (enableFencePost)
        {
            DimensionManager.getWorld(grave.point.dim).destroyBlock(grave.point.x, grave.point.y - 1, grave.point.z, false);
        }
    }
}
