package com.forgeessentials.client.network;

import com.forgeessentials.client.ForgeEssentialsClient;
import com.forgeessentials.client.util.ClientPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.WorldServer;

import java.io.DataInputStream;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class C0PacketSelectionUpdate extends ForgeEssentialsPacketClient {
    public static final byte packetID = 0;

    private Packet250CustomPayload packet;

    @Deprecated
    public C0PacketSelectionUpdate()
    {
        // should never be sent from the client..
    }

    public static void readServer(DataInputStream stream, WorldServer world, EntityPlayer player) throws IOException
    {
        // should never be received here.
    }

    @SideOnly(Side.CLIENT)
    public static void readClient(DataInputStream stream, WorldClient world, EntityPlayer player) throws IOException
    {
        // podouble 1 available.
        if (stream.readBoolean())
        {
            double x = stream.readDouble();
            double y = stream.readDouble();
            double z = stream.readDouble();

            ForgeEssentialsClient.getInfo().setPoint1(new ClientPoint(x, y, z));
        }
        else
        {
            ForgeEssentialsClient.getInfo().setPoint1(null);
        }

        // podouble 2 available
        if (stream.readBoolean())
        {
            double x = stream.readDouble();
            double y = stream.readDouble();
            double z = stream.readDouble();

            ForgeEssentialsClient.getInfo().setPoint2(new ClientPoint(x, y, z));
        }
        else
        {
            ForgeEssentialsClient.getInfo().setPoint2(null);
        }
    }

    @Override
    public Packet250CustomPayload getPayload()
    {
        return packet;
    }

}
