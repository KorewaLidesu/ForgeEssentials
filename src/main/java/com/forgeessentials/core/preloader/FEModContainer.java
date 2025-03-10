package com.forgeessentials.core.preloader;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

import java.util.Arrays;

// Kindly do not reference any FE classes outside the coremod package in this class.

public class FEModContainer extends DummyModContainer {
    public static final String version = "%VERSION%";

    public boolean mod;

    public FEModContainer()
    {
        super(new ModMetadata());
        /* ModMetadata is the same as mcmod.info */
        ModMetadata myMeta = super.getMetadata();
        myMeta.authorList = Arrays.asList(new String[]
                { "See the ForgeEssentials author list." });
        myMeta.description = "Does needed setup for ForgeEssentials.";
        myMeta.modId = "FEPreLoader";
        myMeta.version = version;
        myMeta.name = "Forge Essentials|PreLoader";
        myMeta.url = "";
        myMeta.parent = "ForgeEssentials";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }
}
