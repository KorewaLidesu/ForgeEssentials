package com.forgeessentials.commands;

import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.commands.util.FEcmdModuleCommands;
import com.forgeessentials.util.OutputHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CommandColorize extends FEcmdModuleCommands {

    @Override
    public String getCommandName()
    {
        return "colorize";
    }

    @Override
    public void processCommandPlayer(EntityPlayer sender, String[] args)
    {
        sender.getEntityData().setBoolean("colorize", true);
        OutputHandler.chatConfirmation(sender, "Right click a sign to colourize it!");
    }

    @Override
    public void processCommandConsole(ICommandSender sender, String[] args)
    {
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public RegGroup getReggroup()
    {
        return RegGroup.MEMBERS;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/colorize Apply pre-existing colour codes to a sign.";
    }
}
