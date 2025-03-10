package com.forgeessentials.protection;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.misc.UnfriendlyItemList;
import com.forgeessentials.core.moduleLauncher.FEModule;
import com.forgeessentials.permission.Permission;
import com.forgeessentials.util.events.modules.FEModuleInitEvent;
import com.forgeessentials.util.events.modules.FEModulePreInitEvent;
import com.forgeessentials.util.events.modules.FEModuleServerInitEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map.Entry;
import java.util.Set;

@FEModule(name = "protection", parentMod = ForgeEssentials.class, isCore = true, configClass = ConfigProtection.class)
public class ModuleProtection {
    public final static String PERM_EDITS = "fe.protection.allowEdits";
    public final static String PERM_ITEM_USE = "fe.protection.itemUse";
    public final static String PERM_INTERACT_BLOCK = "fe.protection.allowBlockInteractions";
    public final static String PERM_INTERACT_ENTITY = "fe.protection.allowEntityInteractions";
    public final static String PERM_OVERRIDE = "fe.protection.overrideProtection";
    public final static String PERM_PVP = "fe.protection.pvp";
    public final static String PERM_MOB_SPAWN_NATURAL = "fe.protection.mobSpawn.natural";
    public final static String PERM_MOB_SPAWN_FORCED = "fe.protection.mobSpawn.forced";
    public final static String PERM_DIMENSION = "fe.protection.dimension.";
    public final static String PERM_OVERRIDE_BANNEDITEMS = "fe.protection.overrideProtection.banneditems";
    public final static String PERMPROP_ZONE_GAMEMODE = "fe.protection.data.zonegamemode";

    @FEModule.Config
    public static ConfigProtection config;
    public static boolean enable;
    public static boolean enableMobSpawns;

    @FEModule.PreInit
    public void preLoad(FEModulePreInitEvent e)
    {
        if (FMLCommonHandler.instance().getSide().isClient() || !enable)
        {
            e.getModuleContainer().isLoadable = false;
            return;
        }
    }

    @FEModule.Init
    public void load(FEModuleInitEvent e)
    {
        MinecraftForge.EVENT_BUS.register(new ProtectionEventHandler());
    }

    @SuppressWarnings("unchecked")
    @FEModule.ServerInit
    public void registerPermissions(FEModuleServerInitEvent ev)
    {
        ev.registerServerCommand(new ProtectCommand());

        APIRegistry.permReg.registerPermissionLevel(PERM_PVP, RegGroup.GUESTS);
        APIRegistry.permReg.registerPermissionLevel(PERM_EDITS, RegGroup.MEMBERS);
        APIRegistry.permReg.registerPermissionLevel(PERM_INTERACT_BLOCK, RegGroup.MEMBERS);
        APIRegistry.permReg.registerPermissionLevel(PERM_INTERACT_ENTITY, RegGroup.MEMBERS);
        APIRegistry.permReg.registerPermissionLevel(PERM_OVERRIDE, RegGroup.OWNERS);
        APIRegistry.permReg.registerPermissionLevel(PERM_OVERRIDE_BANNEDITEMS, RegGroup.OWNERS);

        for (Entry<String, Class<?>> e : (Set<Entry<String, Class<?>>>) EntityList.stringToClassMapping.entrySet())
        {
            if (EntityLiving.class.isAssignableFrom(e.getValue()))
            {
                APIRegistry.permReg.registerPermission(PERM_MOB_SPAWN_NATURAL + "." + e.getKey());
                APIRegistry.permReg.registerPermission(PERM_MOB_SPAWN_FORCED + "." + e.getKey());
            }
        }
        APIRegistry.permReg.registerPermissionLevel(PERM_MOB_SPAWN_NATURAL + "." + Permission.ALL, RegGroup.ZONE);
        APIRegistry.permReg.registerPermissionLevel(PERM_MOB_SPAWN_FORCED + "." + Permission.ALL, RegGroup.ZONE);

        for (String perm : UnfriendlyItemList.getNameSet())
        {
            APIRegistry.permReg.registerPermissionLevel(PERM_ITEM_USE + "." + perm, RegGroup.MEMBERS);
        }

        APIRegistry.permReg.registerPermissionLevel(PERM_ITEM_USE + "." + Permission.ALL, RegGroup.MEMBERS);

        for (int i : DimensionManager.getIDs())
        {
            APIRegistry.permReg.registerPermissionLevel(PERM_DIMENSION + i, RegGroup.MEMBERS);
        }

        APIRegistry.permReg.registerPermissionProp(PERMPROP_ZONE_GAMEMODE, 0);
    }
}
