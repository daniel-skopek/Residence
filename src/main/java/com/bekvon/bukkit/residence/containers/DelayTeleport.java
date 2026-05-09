package com.bekvon.bukkit.residence.containers;

import org.bukkit.Location;

import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class DelayTeleport {
    private CMITask messageTask = null;
    private CMITask teleportTask = null;
    private int remainingTime = 0;
    private Location startLocation = null;

    public DelayTeleport() {
    }

    public DelayTeleport(CMITask task, int remainingTime) {
        this.messageTask = task;
        this.remainingTime = remainingTime;
    }

    public CMITask getMessageTask() {
        return messageTask;
    }

    public void setMessageTask(CMITask task) {
        this.messageTask = task;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void lowerRemainingTime() {
        remainingTime -= 1;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public CMITask getTeleportTask() {
        return teleportTask;
    }

    public void setTeleportTask(CMITask teleportTask) {
        this.teleportTask = teleportTask;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public boolean hasMoved(Location current) {
        if (startLocation == null || current == null)
            return false;
        if (!startLocation.getWorld().equals(current.getWorld()))
            return true;
        if (startLocation.getBlockX() != current.getBlockX())
            return true;
        if (startLocation.getBlockY() != current.getBlockY())
            return true;
        if (startLocation.getBlockZ() != current.getBlockZ())
            return true;
        return false;
    }
}
