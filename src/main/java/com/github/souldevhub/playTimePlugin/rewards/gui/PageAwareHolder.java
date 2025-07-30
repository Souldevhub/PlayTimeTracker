package com.github.souldevhub.playTimePlugin.rewards.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class PageAwareHolder implements InventoryHolder {

    private final int page;
    private Inventory inventory;

    public PageAwareHolder(int page, Inventory inventory) {
        this.page = page;
        this.inventory = inventory;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }
    
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}