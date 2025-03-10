package com.forgeessentials.playerlogger.types;

import com.forgeessentials.api.TextFormatter;
import com.forgeessentials.api.json.JSONObject;
import com.forgeessentials.playerlogger.ModulePlayerLogger;
import com.forgeessentials.util.OutputHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class blockChangeLog extends logEntry {
    public blockChangeLog(blockChangeLogCategory cat, EntityPlayer player, String block, int X, int Y, int Z, TileEntity te)
    {
        super();
        SerialBlob teBlob = null;
        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);

            try
            {
                teBlob = new SerialBlob(new JSONObject().put(te.getClass().getName(), TextFormatter.toJSONnbtComp(nbt).toString()).toString().getBytes());
            }
            catch (Exception e)
            {
                OutputHandler.felog.severe(e.toString());
                e.printStackTrace();
            }
        }

        try
        {
            PreparedStatement ps = ModulePlayerLogger.getConnection().prepareStatement(getprepareStatementSQL());
            ps.setString(1, player.username);
            ps.setString(2, cat.toString());
            ps.setString(3, block);
            ps.setInt(4, player.dimension);
            ps.setInt(5, X);
            ps.setInt(6, Y);
            ps.setInt(7, Z);
            ps.setTimestamp(8, time);
            ps.setBlob(9, teBlob);
            ps.execute();
            ps.clearParameters();
            ps.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public blockChangeLog()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockChange";
    }

    @Override
    public String getTableCreateSQL()
    {
        return "CREATE TABLE IF NOT EXISTS " + getName()
                + "(id INT UNSIGNED NOT NULL AUTO_INCREMENT,PRIMARY KEY (id), player VARCHAR(32), category VARCHAR(32), block VARCHAR(32), Dim INT, X INT, Y INT, Z INT, time DATETIME, te LONGBLOB)";
    }

    @Override
    public String getprepareStatementSQL()
    {
        return "INSERT INTO " + getName() + " (player, category, block, Dim, X, Y, Z, time, te) VALUES (?,?,?,?,?,?,?,?,?);";
    }

    public enum blockChangeLogCategory {
        broke, placed, interact
    }
}
