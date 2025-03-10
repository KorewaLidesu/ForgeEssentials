package com.forgeessentials.questioner;

import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class CommandNegative extends ForgeEssentialsCommandBase {

    @Override
    public String getCommandName()
    {
        return "no";
    }

    @Override
    public List<String> getCommandAliases()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("decline");
        list.add("deny");
        list.add("take");
        return list;
    }

    @Override
    public void processCommandPlayer(EntityPlayer sender, String[] args)
    {
        QuestionCenter.processAnswer(sender, false);
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public boolean canPlayerUseCommand(EntityPlayer player)
    {
        return true;
    }

    @Override
    public String getCommandPerm()
    {
        return "fe.questioner.no";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/no Answer no to a question";
    }

    @Override
    public RegGroup getReggroup()
    {

        return RegGroup.GUESTS;
    }

}
