package com.forgeessentials.api.permissions.query;

import com.forgeessentials.permission.PermissionChecker;
import com.forgeessentials.util.AreaSelector.WorldPoint;

public class PermQueryBlanketSpot extends PermQuery {
    public WorldPoint spot;

    /**
     * Assumes the Players position as the "doneTo" point.
     *
     * @param player
     * @param permission
     */
    public PermQueryBlanketSpot(WorldPoint spot, String permission)
    {
        this.spot = spot;
        checker = new PermissionChecker(permission);
        checkForward = false;
    }

    /**
     * Assumes the Players position as the "doneTo" point.
     *
     * @param player
     * @param permission
     * @param checkForward Specifies to only return allow if all the children of the
     *                     permission are allowed.
     */
    public PermQueryBlanketSpot(WorldPoint spot, String permission, boolean checkForward)
    {
        this.spot = spot;
        checker = new PermissionChecker(permission);
        this.checkForward = checkForward;
    }
}
