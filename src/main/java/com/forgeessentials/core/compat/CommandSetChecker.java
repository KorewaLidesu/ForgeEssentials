package com.forgeessentials.core.compat;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.util.OutputHandler;
import com.google.common.collect.HashMultimap;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class CommandSetChecker {
    public static final String[] FIELDNAME = { "commandSet", "b", "field_71561_b", "aa/b" };
    public static boolean removeDuplicateCommands;
    private static Map<String, RegGroup> permList = new HashMap<String, RegGroup>();

    public static void remove()
    {
        OutputHandler.felog.finest("Running duplicate command removal process!");
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server.getCommandManager() instanceof CommandHandler)
        {
            try
            {
                HashMap<String, ICommand> initials = new HashMap<String, ICommand>();
                HashMultimap<String, ICommand> duplicates = HashMultimap.create();

                Set<ICommand> cmdList = ReflectionHelper.getPrivateValue(CommandHandler.class, (CommandHandler) server.getCommandManager(), FIELDNAME);
                OutputHandler.felog.finer("commandSet size: " + cmdList.size());

                ICommand keep;
                for (ICommand cmd : cmdList)
                {
                    keep = initials.put(cmd.getCommandName(), cmd);
                    if (keep != null)
                    {
                        OutputHandler.felog.finer("Duplicate command found! Name:" + keep.getCommandName());
                        duplicates.put(cmd.getCommandName(), cmd);
                        duplicates.put(cmd.getCommandName(), keep);
                        continue;
                    }
                    else if (cmd.getClass().getName().startsWith("net.minecraft.command") && cmd instanceof CommandBase)
                    {
                        permList.put("mc." + cmd.getCommandName(), RegGroup.fromInt(((CommandBase) cmd).getRequiredPermissionLevel()));
                    }
                }

                Set<ICommand> toRemove = new HashSet<ICommand>();
                keep = null;
                Class<? extends ICommand> cmdClass;
                int kept = -1, other = -1;
                for (String name : duplicates.keySet())
                {
                    keep = null;
                    kept = -1;
                    other = -1;
                    cmdClass = null;

                    for (ICommand cmd : duplicates.get(name))
                    {
                        other = getCommandPriority(cmd);

                        if (keep == null)
                        {
                            kept = other;

                            if (kept == -1)
                            {
                                keep = null;
                                duplicates.remove(name, cmd);
                            }
                            else
                            {
                                keep = cmd;
                            }

                            continue;
                        }

                        if (kept > other)
                        {
                            toRemove.add(cmd);
                            cmdClass = cmd.getClass();
                            OutputHandler.felog.finer("Removing command '" + cmd.getCommandName() + "' from class: " + cmdClass.getName());
                        }
                        else
                        {
                            toRemove.add(keep);
                            cmdClass = keep.getClass();
                            OutputHandler.felog.finer("Removing command '" + keep.getCommandName() + "' from class: " + cmdClass.getName());

                            keep = cmd;
                            kept = other;
                        }

                    }
                }

                cmdList.removeAll(toRemove);
                OutputHandler.felog.finer("commandSet size: " + cmdList.size());
                ReflectionHelper.setPrivateValue(CommandHandler.class, (CommandHandler) server.getCommandManager(), cmdList, FIELDNAME);

            }
            catch (Exception e)
            {
                OutputHandler.felog.finer("Something broke: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    // 0 = vanilla. 1 = fe. 2 = other mods
    private static int getCommandPriority(ICommand cmd)
    {
        try
        {
            Class<?> cmdClass = cmd.getClass();
            Package pkg = cmdClass.getPackage();
            if (pkg == null || pkg.getName().contains("net.minecraft"))
            {
                return 0;
            }
            else if (pkg == null || pkg.getName().contains("forgeessentials"))
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }
        catch (Exception e)
        {
            OutputHandler.felog.finer("Can't remove " + cmd.getCommandName());
            OutputHandler.felog.finer("" + e.getLocalizedMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static void regMCOverrides()
    {
        Iterator it = permList.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry) it.next();
            APIRegistry.permReg.registerPermissionLevel((String) pairs.getKey(), (RegGroup) pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }

    }
}
