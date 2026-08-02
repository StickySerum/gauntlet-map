package com.gauntletmap;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

public class GauntletMapConstants {

    public final static Map<Integer, Integer> ROOM_FOUND_VARBIT_MAP = Map.ofEntries(
            Map.entry(9240, 1),
            Map.entry(9241, 2),
            Map.entry(9242, 3),
            Map.entry(9243, 4),
            Map.entry(9244, 5),
            Map.entry(9245, 6),
            Map.entry(9246, 7),
            Map.entry(9247, 8),
            Map.entry(9248, 9),
            Map.entry(9249, 10),
            Map.entry(9250, 11),
            Map.entry(9251, 12),
            Map.entry(9252, 13),
            Map.entry(9253, 14),
            Map.entry(9254, 15),
            Map.entry(9255, 16),
            Map.entry(9256, 17),
            Map.entry(9257, 18),
            Map.entry(9258, 19),
            Map.entry(9259, 20),
            Map.entry(9260, 21),
            Map.entry(9261, 22),
            Map.entry(9262, 23),
            Map.entry(9263, 24),
            Map.entry(9264, 25),
            Map.entry(9265, 26),
            Map.entry(9266, 27),
            Map.entry(9267, 28),
            Map.entry(9268, 29),
            Map.entry(9269, 30),
            Map.entry(9270, 31),
            Map.entry(9271, 32),
            Map.entry(9272, 33),
            Map.entry(9273, 34),
            Map.entry(9274, 35),
            Map.entry(9275, 36),
            Map.entry(9276, 37),
            Map.entry(9277, 38),
            Map.entry(9278, 39),
            Map.entry(9279, 40),
            Map.entry(9280, 41),
            Map.entry(9281, 42),
            Map.entry(9282, 43),
            Map.entry(9283, 44),
            Map.entry(9284, 45),
            Map.entry(9285, 46),
            Map.entry(9286, 47),
            Map.entry(9287, 48),
            Map.entry(9288, 49)
    );

    public static final List<Integer> GUARANTEED_DEMI_ROOM_LIST = List.of(4, 22, 28, 46);
    public static final List<Integer> DEMI_ROOM_LIST = List.of(1, 2, 3, 4, 5, 6, 7, 8, 14, 15, 21, 22, 28, 29, 35, 36, 42, 43, 44, 45, 46, 47, 48, 49);


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
