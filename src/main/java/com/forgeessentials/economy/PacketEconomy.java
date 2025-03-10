package com.forgeessentials.economy;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.core.network.ForgeEssentialsPacket;
import com.forgeessentials.util.OutputHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.WorldServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketEconomy extends ForgeEssentialsPacket {

    private Packet250CustomPayload packet;

    public PacketEconomy(int amount)
    {
        packet = new Packet250CustomPayload();

        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);

        try
        {
            stream.write(3);

            stream.write(amount);

            stream.close();
            streambyte.close();

            packet.channel = FECHANNEL;
            packet.data = streambyte.toByteArray();
            packet.length = packet.data.length;
        }

        catch (Exception e)
        {
            OutputHandler.felog.info("Error creating packet >> " + this.getClass());
        }
    }

    public static void readServer(DataInputStream stream, WorldServer world,
            EntityPlayer player)
    {
        PacketEconomy packet = new PacketEconomy(APIRegistry.wallet.getWallet(player.username));
        PacketDispatcher.sendPacketToPlayer(packet.getPayload(), (Player) player);
    }

    @Override
    public Packet250CustomPayload getPayload()
    {

        return packet;
    }

}
