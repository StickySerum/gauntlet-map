package com.gauntletmap;

import lombok.AllArgsConstructor;

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
