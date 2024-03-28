package net.runelite.client.plugins.gauntletmap;

import lombok.Getter;
import lombok.NonNull;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

@Getter
public class NodeGameObject
{
	private final GameObject gameObject;

	private final WorldPoint worldLocation;

	NodeGameObject(@NonNull GameObject gameObject)
	{
		this.gameObject = gameObject;
		this.worldLocation = gameObject.getWorldLocation();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof NodeGameObject))
		{
			return false;
		}

		final NodeGameObject that = (NodeGameObject) o;
		return gameObject.equals(that.gameObject);
	}
}
