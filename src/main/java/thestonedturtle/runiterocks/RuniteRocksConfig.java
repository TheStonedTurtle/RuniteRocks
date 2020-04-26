package thestonedturtle.runiterocks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RuniteRocksConfig.GROUP)
public interface RuniteRocksConfig extends Config
{
	String GROUP = "runiterocks";

	@ConfigItem(
		keyName = "respawnCounter",
		name = "Respawn Counter",
		description = "<html>If enabled shows a ticking countdown to the respawn time" +
			"<br/>If disabled shows the time at which the rock should respawn</html>"
	)
	default boolean respawnCounter()
	{
		return true;
	}

	@ConfigItem(
		keyName = "visitCounter",
		name = "Last Visit Counter",
		description = "<html>If enabled shows a ticking timer for how long since you checked on that rock" +
			"<br/>If disabled shows the time at which you last checked on that rock</html>"
	)
	default boolean visitCounter()
	{
		return false;
	}
}
