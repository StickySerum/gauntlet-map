package com.gauntletmap;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GauntletMapTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GauntletMapPlugin.class);
		RuneLite.main(args);
	}
}
