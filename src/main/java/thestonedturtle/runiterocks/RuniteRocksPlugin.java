package thestonedturtle.runiterocks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

@Slf4j
@PluginDescriptor(
	name = "Runite Rocks"
)
public class RuniteRocksPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private RuniteRocksConfig config;

	@Inject
	private WorldService worldService;

	@Provides
	RuniteRocksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuniteRocksConfig.class);
	}

	private final Map<WorldPoint, GameObject> queue = new HashMap<>();
	@Getter
	private final Map<Integer, WorldTracker> worldMap = new HashMap<>();
	@Getter
	private WorldTracker tracker;

	private NavigationButton navButton;
	private RuniteRocksPanel panel;

	@Override
	protected void startUp() throws Exception
	{
		panel = new RuniteRocksPanel(this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Runite Rocks")
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (client.getGameState().equals(GameState.LOGGED_IN))
		{
			final World world = getWorld(client.getWorld());
			if (world == null)
			{
				log.warn("couldn't find world for id: {}", client.getWorld());
				return;
			}
			tracker = new WorldTracker(world);
			worldMap.put(client.getWorld(), tracker);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		panel = null;
		queue.clear();
		worldMap.clear();
		tracker = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOGGING_IN:
			case LOADING:
			case LOGGED_IN:
			case HOPPING:
				break;
			default:
				return;
		}

		final int currentWorld = client.getWorld();
		if (tracker != null)
		{
			if (currentWorld == tracker.getWorld().getId())
			{
				return;
			}

			panel.switchCurrentHighlight(currentWorld, tracker.getWorld().getId());
		}

		final World world = getWorld(currentWorld);
		if (world == null)
		{
			log.warn("couldn't find world for id: {}", currentWorld);
			return;
		}

		tracker = worldMap.getOrDefault(currentWorld, new WorldTracker(world));
		// Ensure it exists on the map since getOrDefault doesn't do that
		worldMap.put(currentWorld, tracker);
	}

	@Subscribe
	public void onGameObjectChanged(final GameObjectChanged e)
	{
		if (tracker == null)
		{
			return;
		}

		final WorldPoint tileLocation = e.getTile().getWorldLocation();
		if (Rock.getByWorldPoint(tileLocation) != null)
		{
			queue.put(tileLocation, e.getGameObject());
		}
	}

	@Subscribe
	public void onGameObjectSpawned(final GameObjectSpawned e)
	{
		if (tracker == null)
		{
			return;
		}

		final WorldPoint tileLocation = e.getTile().getWorldLocation();
		if (Rock.getByWorldPoint(tileLocation) != null)
		{
			queue.put(tileLocation, e.getGameObject());
		}
	}

	@Subscribe
	public void onGameObjectDespawned(final GameObjectDespawned e)
	{
		if (tracker == null)
		{
			return;
		}

		final WorldPoint tileLocation = e.getTile().getWorldLocation();
		if (Rock.getByWorldPoint(tileLocation) != null)
		{
			queue.put(tileLocation, e.getGameObject());
		}
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (tracker == null || queue.size() == 0)
		{
			return;
		}

		final Collection<RuniteRock> rocks = new ArrayList<>();
		for (final Map.Entry<WorldPoint, GameObject> entry : queue.entrySet())
		{
			final RuniteRock rock = tracker.updateRockState(entry.getKey(), entry.getValue());
			if (rock == null)
			{
				log.warn("Error updating rock state: {} | {}", entry.getKey(), entry.getValue());
				continue;
			}
			rocks.add(rock);
		}

		queue.clear();
		SwingUtilities.invokeLater(() -> panel.updateRuniteRocks(rocks));
	}

	@Nullable
	private World getWorld(final int worldNumber)
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult != null)
		{
			return worldResult.findWorld(worldNumber);
		}

		return null;
	}
}
