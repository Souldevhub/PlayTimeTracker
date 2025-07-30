
public Inventory getRewardsGUIForPlayer(long playtime, Set<String> claimed, int page) {
    int size = 54;
    Inventory inventory = Bukkit.createInventory(new PageAwareHolder(page, null), size, "Playtime Rewards - Page " + (page + 1));

    // Populate with reward buttons
    List<RewardSlot> allRewards = getRewardSlots();
    int rewardsPerPage = 45; // 5 rows * 9 columns - 9 slots for navigation buttons
    int startIndex = page * rewardsPerPage;
    int endIndex = Math.min(startIndex + rewardsPerPage, allRewards.size());

    for (int i = startIndex; i < endIndex; i++) {
        RewardSlot reward = allRewards.get(i);
        if (playtime >= reward.requiredPlaytime() && !claimed.contains(reward.id())) {
            ItemStack item = reward.createItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.id());
                item.setItemMeta(meta);
            }
            inventory.setItem(reward.slot(), item);
        } else {
            // Add this section to include placeholder items
            ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            if (placeholderMeta != null) {
                placeholderMeta.displayName(Component.text("No Reward Available"));
                placeholder.setItemMeta(placeholderMeta);
            }
            inventory.setItem(reward.slot(), placeholder);
        }
    }

    // Add navigation buttons
    ItemStack prevButton = new NavigationButton(Material.ARROW, "Previous Page", null).createItem();
    prevButton.getItemMeta().getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_PREV);
    inventory.setItem(45, prevButton);

    ItemStack nextButton = new NavigationButton(Material.ARROW, "Next Page", null).createItem();
    nextButton.getItemMeta().getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_NEXT);
    inventory.setItem(47, nextButton);

    ItemStack closeButton = new NavigationButton(Material.BARRIER, "Close", null).createItem();
    closeButton.getItemMeta().getPersistentDataContainer().set(navButtonTypeKey, PersistentDataType.STRING, NAV_CLOSE);
    inventory.setItem(49, closeButton);

    return inventory;
}
