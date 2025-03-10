package com.forgeessentials.teleport;

import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.core.PlayerInfo;
import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import com.forgeessentials.util.AreaSelector.Point;
import com.forgeessentials.util.AreaSelector.WarpPoint;
import com.forgeessentials.util.TeleportCenter;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.List;

public class CommandTppos extends ForgeEssentialsCommandBase {

    /**
     * Spawn point for each dimension
     */
    public static HashMap<Integer, Point> spawnPoints = new HashMap<Integer, Point>();

    @Override
    public String getCommandName()
    {
        return "tppos";
    }

    @Override
    public void processCommandPlayer(EntityPlayer sender, String[] args)
    {
        if (args.length == 3)
        {
            int x = parseInt(sender, args[0], sender.posX), y = parseInt(sender, args[1], sender.posY), z = parseInt(sender, args[2], sender.posZ);
            EntityPlayerMP player = (EntityPlayerMP) sender;
            PlayerInfo playerInfo = PlayerInfo.getPlayerInfo(player.username);
            playerInfo.back = new WarpPoint(player);
            CommandBack.justDied.remove(player.username);
            TeleportCenter.addToTpQue(new WarpPoint(player.dimension, x, y, z, player.cameraPitch, player.cameraYaw), player);
        }
        else
        {
            this.error(sender);
        }
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public String getCommandPerm()
    {
        return "fe.teleport.tppos";
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1 || args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, FMLCommonHandler.instance().getMinecraftServerInstance().getAllUsernames());
        }
        else
        {
            return null;
        }
    }

    @Override
    public RegGroup getReggroup()
    {
        return RegGroup.MEMBERS;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/tppos <x y z> Teleport to a position.";
    }
}
