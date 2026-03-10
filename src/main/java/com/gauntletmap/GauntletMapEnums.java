package net.runelite.client.plugins.gauntletmap;

import lombok.AllArgsConstructor;

public enum GauntletMapEnums {
    ;

    @AllArgsConstructor
    public enum MapIcons
    {
        PLAYER("player"),
        BOSS("hunllef"),
        ACTIVE_TILE("active"),
        DEMIBOSS_UNKNOWN("demi"),
        DEMIBOSS_MAGIC("demiboss/magic"),
        DEMIBOSS_MELEE("demiboss/melee"),
        DEMIBOSS_RANGED("demiboss/ranged"),
        FISHING_SPOT("resources/fish"),
        GRYM_ROOT("resources/grym"),
        FISHING_SPOT_GRYM_ROOT("resources/grym_fish");

        private final String path;

        @Override
        public String toString()
        {
            return path;
        }

    }

    @AllArgsConstructor
    public enum PanelVisibilityOptions
    {

        ALWAYS("Always show"),
        IN_GAUNTLET("Only in Gauntlet"),
        NEVER("Never show");

        private final String name;

        @Override
        public String toString()
        {
            return name;
        }

    }
}