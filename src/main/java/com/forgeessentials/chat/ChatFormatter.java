package com.forgeessentials.chat;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.Zone;
import com.forgeessentials.api.permissions.query.PermQueryPlayer;
import com.forgeessentials.chat.commands.CommandPm;
import com.forgeessentials.core.PlayerInfo;
import com.forgeessentials.util.AreaSelector.WorldPoint;
import com.forgeessentials.util.ChatUtils;
import com.forgeessentials.util.FunctionHelper;
import com.google.common.base.Strings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {
    public static List<String> bannedWords = new ArrayList<String>();
    public static boolean censor;
    public static String censorSymbol;

    public static String gmS;
    public static String gmC;
    public static String gmA;
    public static int censorSlap;

    @ForgeSubscribe(priority = EventPriority.NORMAL)
    public void chatEvent(ServerChatEvent event)
    {
        // muting this should probably be done elsewhere
        if (event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("mute"))
        {
            event.setCanceled(true);
            ChatUtils.sendMessage(event.player, "You are currently muted.");
            return;
        }

        // PMs
        if (CommandPm.isMessagePersistent(event.player.getCommandSenderName()))
        {
            event.setCanceled(true);
            CommandPm.processChat(event.player, event.message.split(" "));
            return;
        }

        String message = event.message;
        String nickname = event.username;

        // censoring
        if (censor)
        {
            for (String word : bannedWords)
            {
                Pattern p = Pattern.compile("(?i)\\b" + word + "\\b");
                Matcher m = p.matcher(message);

                while (m.find())
                {
                    int startIndex = m.start();
                    int endIndex = m.end();

                    int length = endIndex - startIndex;
                    String replaceWith = Strings.repeat(censorSymbol, length);

                    message = m.replaceAll(replaceWith);

                    if (censorSlap != 0)
                    {
                        event.player.attackEntityFrom(DamageSource.generic, censorSlap);
                    }
                }
            }
        }

		/*
         * Nickname
		 */

        if (event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).hasKey("nickname"))
        {
            nickname = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("nickname");
        }

		/*
         * Colorize!
		 */
        if (event.message.contains("&"))
        {
            if (APIRegistry.perms.checkPermAllowed(new PermQueryPlayer(event.player, "ForgeEssentials.Chat.usecolor")))
            {
                message = FunctionHelper.formatColors(event.message);
            }
        }

        // replacing stuff...

        String rank = "";
        String zoneID = "";
        String gPrefix = "";
        String gSuffix = "";

        PlayerInfo info = PlayerInfo.getPlayerInfo(event.player.username);
        String playerPrefix = info.prefix == null ? "" : FunctionHelper.formatColors(info.prefix).trim();
        String playerSuffix = info.suffix == null ? "" : FunctionHelper.formatColors(info.suffix).trim();

        Zone zone = APIRegistry.zones.getWhichZoneIn(new WorldPoint(event.player));
        zoneID = zone.getZoneName();

        // Group stuff!!! DO NOT TOUCH!!!
        {
            rank = FunctionHelper.getGroupRankString(event.username);

            gPrefix = FunctionHelper.getGroupPrefixString(event.username);
            gPrefix = FunctionHelper.formatColors(gPrefix).trim();

            gSuffix = FunctionHelper.getGroupSuffixString(event.username);
            gSuffix = FunctionHelper.formatColors(gSuffix).trim();
        }

        // It may be beneficial to make this a public function. -RlonRyan
        String format = ConfigChat.chatFormat;
        format = ConfigChat.chatFormat == null || ConfigChat.chatFormat.trim().isEmpty() ? "<%username>%message" : ConfigChat.chatFormat;

		/*
         * if(enable_chat%){ format = replaceAllIngnoreCase(format, "%message",
		 * message); }
		 */
        // replace group, zone, and rank

        if (format.contains("%gm"))
        {
            String gmCode = "";
            if (event.player.theItemInWorldManager.getGameType().isCreative())
            {
                gmCode = gmC;
            }
            else if (event.player.theItemInWorldManager.getGameType().isAdventure())
            {
                gmCode = gmA;
            }
            else
            {
                gmCode = gmS;
            }

            format = FunctionHelper.replaceAllIgnoreCase(format, "%gm", gmCode);
        }

        float health = event.player.getHealth(); // No nice name for player health yet

        if (format.contains("%healthcolor"))
        {
            String c = "";
            if (health < 6)
            {
                c = "%red";
            }
            else if (health < 12)
            {
                c = "%yellow";
            }
            else
            {
                c = "%green";
            }
            format = FunctionHelper.replaceAllIgnoreCase(format, "%healthcolor", c);
        }

        format = FunctionHelper.replaceAllIgnoreCase(format, "%rank", rank);
        format = FunctionHelper.replaceAllIgnoreCase(format, "%zone", zoneID);
        format = FunctionHelper.replaceAllIgnoreCase(format, "%groupPrefix", gPrefix);
        format = FunctionHelper.replaceAllIgnoreCase(format, "%groupSuffix", gSuffix);

        // random nice things...
        format = FunctionHelper.replaceAllIgnoreCase(format, "%health", "" + health);

        format = FunctionHelper.format(format);

        // essentials
        format = FunctionHelper.replaceAllIgnoreCase(format, "%playerPrefix", playerPrefix);
        format = FunctionHelper.replaceAllIgnoreCase(format, "%playerSuffix", playerSuffix);
        format = FunctionHelper.replaceAllIgnoreCase(format, "%username", nickname);
        // if(!enable_chat%){ //whereas enable chat is a boolean that can be set
        // in the config or whatever
        // //allowing the use of %codes in chat
        format = FunctionHelper.replaceAllIgnoreCase(format, "%message", message);
        // }

        // finally make it the chat line.
        // TODO: This is probably incorrect with regards to coloring
        event.component = ChatMessageComponent.createFromText(format);

        if (ConfigChat.logchat && ModuleChat.chatLog != null)
        {
            ModuleChat.chatLog.printf(FunctionHelper.getCurrentDateString() + " " + FunctionHelper.getCurrentTimeString() + "[" + event.username + "] "
                    + event.message); // don't use event.line - it shows colour codes and everything
        }

    }
}
