package com.forgeessentials.permission;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.Group;
import com.forgeessentials.api.permissions.Zone;
import com.forgeessentials.api.permissions.query.PermQuery.PermResult;
import com.forgeessentials.api.permissions.query.PermQueryPlayer;
import com.forgeessentials.api.permissions.query.PermQueryPlayerArea;
import com.forgeessentials.api.permissions.query.PermQueryPlayerZone;
import com.forgeessentials.util.AreaSelector.AreaBase;
import com.forgeessentials.util.AreaSelector.WorldPoint;
import com.forgeessentials.util.FunctionHelper;

import java.util.ArrayList;

/**
 * This is the default catcher of all the ForgeEssentials Permission checks.
 * Mods can inherit from any of the ForgeEssentials Permissions and specify more
 * specific catchers to get first crack at handling them.
 * The handling performed here is limited to basic area permission checks, and
 * is not aware of anything else other mods add to the system.
 *
 * @author AbrarSyed
 */
public final class PermissionsPlayerHandler {
    private PermissionsPlayerHandler()
    {
        // do nothing
    }

    public static void parseQuery(PermQueryPlayer query)
    {
        doOpCheck(query);

        if (!query.getResult().equals(PermResult.UNKNOWN))
        {
            return;
        }

        checkPlayerSupers(query);

        if (!query.getResult().equals(PermResult.UNKNOWN))
        {
            return;
        }

        if (query instanceof PermQueryPlayerZone)
        {
            handleZone((PermQueryPlayerZone) query);
        }
        else if (query instanceof PermQueryPlayerArea)
        {
            handleArea((PermQueryPlayerArea) query);
        }
        else
        {
            handlePlayer(query);
        }
    }

    private static void doOpCheck(PermQueryPlayer event)
    {
        boolean isOp = FunctionHelper.isPlayerOp(event.doer.username.toLowerCase());
        event.setResult(isOp ? PermResult.ALLOW : PermResult.UNKNOWN);
    }

    private static void checkPlayerSupers(PermQueryPlayer event)
    {
        PermResult result = SqlHelper
                .getPermissionResult(event.doer.username, false, event.checker, APIRegistry.zones.getSUPER().getZoneName(), event.checkForward);
        if (!result.equals(PermResult.UNKNOWN))
        {
            event.setResult(result);
        }
    }

    private static void handlePlayer(PermQueryPlayer event)
    {
        Zone zone = APIRegistry.zones.getWhichZoneIn(new WorldPoint(event.doer));
        PermResult result = getResultFromZone(zone, event);
        event.setResult(result);
    }

    private static void handleZone(PermQueryPlayerZone event)
    {
        PermResult result = getResultFromZone(event.toCheck, event);
        event.setResult(result);
    }

    private static void handleArea(PermQueryPlayerArea event)
    {
        if (event.allOrNothing)
        {
            Zone zone = APIRegistry.zones.getWhichZoneIn(event.doneTo);
            PermResult result = getResultFromZone(zone, event);
            event.setResult(result);
        }
        else
        {
            event.applicable = getApplicableAreas(event.doneTo, event);
            if (event.applicable == null)
            {
                event.setResult(PermResult.DENY);
            }
            else if (event.applicable.isEmpty())
            {
                event.setResult(PermResult.ALLOW);
            }
            else
            {
                event.setResult(PermResult.PARTIAL);
            }
        }
    }

    /**
     * @param zone   Zone to check permissions in.
     * @param perm   The permission to check.
     * @param player Player to check/
     * @return the result for the perm.
     */
    private static PermResult getResultFromZone(Zone zone, PermQueryPlayer event)
    {
        ArrayList<Group> groups = APIRegistry.perms.getApplicableGroups(event.doer.username, false, zone.getZoneName());
        PermResult result = PermResult.UNKNOWN;
        Zone tempZone = zone;
        Group group;
        while (result.equals(PermResult.UNKNOWN))
        {
            // get the permissions... This automatically checks permission
            // parents...
            result = SqlHelper.getPermissionResult(event.doer.username, false, event.checker, tempZone.getZoneName(), event.checkForward);

            // if its unknown still
            if (result.equals(PermResult.UNKNOWN))
            {
                // iterates through the groups.
                for (int i = 0; result.equals(PermResult.UNKNOWN) && i < groups.size(); i++)
                {
                    group = groups.get(i);
                    while (group != null && result == PermResult.UNKNOWN)
                    {
                        // checks the permissions for the group.
                        result = SqlHelper.getPermissionResult(group.name, true, event.checker, tempZone.getZoneName(), event.checkForward);

                        // sets the group to its parent.
                        group = SqlHelper.getGroupForName(group.parent);
                    }
                }
            }

            // check defaults... unless it has the override..
            if (result.equals(PermResult.UNKNOWN) && !event.dOverride)
            {
                result = SqlHelper.getPermissionResult(APIRegistry.perms.getDEFAULT().name, true, event.checker, zone.getZoneName(), event.checkForward);
            }

            // still unknown? check parent zones.
            if (result.equals(PermResult.UNKNOWN))
            {
                if (tempZone == APIRegistry.zones.getGLOBAL())
                {
                    // default deny.
                    result = PermResult.DENY;
                }
                else
                {
                    // get the parent of the zone.
                    tempZone = APIRegistry.zones.getZone(tempZone.parent);
                }
            }
        }
        return result;
    }

    private static ArrayList<AreaBase> getApplicableAreas(AreaBase doneTo, PermQueryPlayer event)
    {
        ArrayList<AreaBase> applicable = new ArrayList<AreaBase>();

        Zone worldZone = APIRegistry.zones.getWorldZone(event.doer.worldObj);
        ArrayList<Zone> zones = new ArrayList<Zone>();

        // add all children
        for (Zone zone : APIRegistry.zones.getZoneList())
        {
            if (zone == null || zone.isGlobalZone() || zone.isWorldZone())
            {
                continue;
            }
            if (zone.intersectsWith(doneTo) && worldZone.isParentOf(zone))
            {
                zones.add(zone);
            }
        }

        switch (zones.size())
        {
        // no children of the world? return the worldZone
        case 0:
        {
            PermResult result = getResultFromZone(worldZone, event);
            if (result.equals(PermResult.ALLOW))
            {
                return applicable;
            }
            else
            {
                return null;
            }
        }
        // only 1 usable Zone? use it.
        case 1:
        {
            PermResult result = getResultFromZone(zones.get(0), event);
            if (result.equals(PermResult.ALLOW))
            {
                return applicable;
            }
            else
            {
                return null;
            }
        }
        // else.. get the applicable states.
        default:
        {
            for (Zone zone : zones)
            {
                if (getResultFromZone(zone, event).equals(PermResult.ALLOW))
                {
                    applicable.add(doneTo.getIntersection(zone));
                }
            }
        }
        }

        return applicable;
    }
}
