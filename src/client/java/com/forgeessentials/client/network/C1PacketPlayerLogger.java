package com.forgeessentials.client.network;

import com.forgeessentials.client.ForgeEssentialsClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.WorldServer;

import java.io.DataInputStream;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class C1PacketPlayerLogger extends ForgeEssentialsPacketClient {
    public static final byte packetID = 1;

    private Packet250CustomPayload packet;

    @SideOnly(Side.CLIENT)
    public static void readClient(DataInputStream stream, WorldClient world, EntityPlayer player) throws IOException
    {
        ForgeEssentialsClient.getInfo().playerLogger = stream.readBoolean();
    }

    public static void readServer(DataInputStream stream, WorldServer world, EntityPlayer player) throws IOException
    {
        // should never be received here.
    }

    @Override
    public Packet250CustomPayload getPayload()
    {
        return packet;
    }
}
