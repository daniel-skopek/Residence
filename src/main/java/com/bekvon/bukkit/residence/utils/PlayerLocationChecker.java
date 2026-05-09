package com.bekvon.bukkit.residence.utils;

import java.util.ArrayDeque;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.playerTempData;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class PlayerLocationChecker {

    private final ArrayDeque<UUID> queue = new ArrayDeque<>();
    private CMITask task = null;

    public PlayerLocationChecker() {
    }

    public void start() {
        if (task != null)
            return;
        task = CMIScheduler.scheduleSyncRepeatingTask(Residence.getInstance(), this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        queue.clear();
    }

    private void tick() {
        if (queue.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                queue.add(p.getUniqueId());
            }
            if (queue.isEmpty())
                return;
        }

        int interval = Residence.getInstance().getConfigManager().getMinMoveUpdateInterval();
        int budget = 5;
        int processed = 0;

        while (!queue.isEmpty() && processed < budget) {
            UUID uuid = queue.poll();
            if (uuid == null)
                continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                playerTempData.get(uuid).setLastLocation(null);
                continue;
            }

            if (player.hasMetadata("NPC"))
                continue;

            playerTempData playerData = playerTempData.get(player);

            long now = System.currentTimeMillis();
            if (playerData.getLastCheck() + interval > now)
                continue;

            playerData.setLastCheck(now);
            processed++;

            CMIScheduler.runAtLocation(Residence.getInstance(), player.getLocation(), () -> {
                if (player == null || !player.isOnline())
                    return;

                Location current = player.getLocation();
                Location previous = playerData.getLastLocation(current);

                if (previous != null && !player.isFlying()) {
                    double deltaY = current.getY() - previous.getY();
                    if (deltaY == 0.41999998688697815D) {
                        applyJumpBoost(player);
                    }
                }

                if (previous == null || hasChanged(previous, current)) {
                    Residence.getInstance().getPlayerListener().handleNewLocation(player, current, true);
                }
            });
        }
    }

    private static boolean hasChanged(Location from, Location to) {
        if (!from.getWorld().equals(to.getWorld()))
            return true;
        if (from.getBlockX() != to.getBlockX())
            return true;
        if (from.getBlockY() != to.getBlockY())
            return true;
        if (from.getBlockZ() != to.getBlockZ())
            return true;
        return false;
    }

    private static void applyJumpBoost(Player player) {
        FlagPermissions perms = FlagPermissions.getPerms(player.getLocation());
        if (Flags.jump2.isGlobalyEnabled() && perms.has(Flags.jump2, FlagCombo.OnlyTrue))
            player.setVelocity(player.getVelocity().add(player.getVelocity().multiply(0.3)));
        else if (Flags.jump3.isGlobalyEnabled() && perms.has(Flags.jump3, FlagCombo.OnlyTrue))
            player.setVelocity(player.getVelocity().add(player.getVelocity().multiply(0.6)));
    }
}
