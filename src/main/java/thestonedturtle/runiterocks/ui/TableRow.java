/*
 * Copyright (c) 2018, Psikoi <https://github.com/Psikoi>
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.runiterocks.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;
import thestonedturtle.runiterocks.RuniteRock;
import thestonedturtle.runiterocks.RuniteRocksPanel;

/**
 * Modified version of the WorldTableHeader from the WorldHopper plugin
 */
public class TableRow extends JPanel
{
	private static final int WORLD_COLUMN_WIDTH = RuniteRocksPanel.WORLD_COLUMN_WIDTH;
	private static final int LOCATION_COLUMN_WIDTH = RuniteRocksPanel.LOCATION_COLUMN_WIDTH;
	private static final int TIME_COLUMN_WIDTH = RuniteRocksPanel.TIME_COLUMN_WIDTH;

	private static final Color CURRENT_WORLD = new Color(66, 227, 17);
	private static final Color DANGEROUS_WORLD = new Color(251, 62, 62);
	private static final Color MEMBERS_WORLD = new Color(210, 193, 53);
	private static final Color FREE_WORLD = new Color(200, 200, 200);

	private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mm:ss");

	private JLabel worldLabel;
	private JLabel locationLabel;

	private JLabel respawnLabel;
	private JLabel lastVisitedLabel;

	@Getter
	private final World world;
	@Getter
	private final RuniteRock runiteRock;

	@Getter(AccessLevel.PACKAGE)
	private int updatedPlayerCount;

	private Color lastBackground;
	private boolean current = false;

	public TableRow(World world, RuniteRock rock)
	{
		this.world = world;
		this.runiteRock = rock;
		this.updatedPlayerCount = world.getPlayers();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(2, 0, 2, 0));
		setForeground(getWorldColor());

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				lastBackground = getBackground();
				setBackground(getBackground().brighter());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				setBackground(lastBackground);
			}
		});

		JPanel leftSide = new JPanel(new BorderLayout());
		JPanel rightSide = new JPanel(new BorderLayout());
		leftSide.setOpaque(false);
		rightSide.setOpaque(false);

		JPanel worldField = buildWorldField();
		worldField.setPreferredSize(new Dimension(WORLD_COLUMN_WIDTH, 0));
		worldField.setOpaque(false);

		JPanel locationField = buildLocationField();
		locationField.setPreferredSize(new Dimension(LOCATION_COLUMN_WIDTH, 0));
		locationField.setOpaque(false);

		JPanel respawnField = buildRespawnField();
		respawnField.setPreferredSize(new Dimension(TIME_COLUMN_WIDTH, 0));
		respawnField.setOpaque(false);

		JPanel lastVisitedField = buildLastVisitedField();
		lastVisitedField.setBorder(new EmptyBorder(5, 5, 5, 5));
		lastVisitedField.setOpaque(false);

		leftSide.add(worldField, BorderLayout.WEST);
		leftSide.add(locationField, BorderLayout.CENTER);

		rightSide.add(respawnField, BorderLayout.WEST);
		rightSide.add(lastVisitedField, BorderLayout.CENTER);

		add(leftSide, BorderLayout.WEST);
		add(rightSide, BorderLayout.CENTER);
	}

	/**
	 * Builds the world list field (containing the the world index)
	 */
	private JPanel buildWorldField()
	{
		final JPanel column = new JPanel(new BorderLayout(7, 0));
		column.setBorder(new EmptyBorder(0, 5, 0, 5));

		worldLabel = new JLabel(String.valueOf(world.getId()));
		column.add(worldLabel, BorderLayout.CENTER);

		return column;
	}

	/**
	 * Builds the location list field (containing the location of the rock in-game)
	 */
	private JPanel buildLocationField()
	{
		final JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 5, 0, 5));

		locationLabel = new JLabel(runiteRock.getRock().getName());
		locationLabel.setFont(FontManager.getRunescapeSmallFont());
		locationLabel.setToolTipText(runiteRock.getRock().getLocation());

		column.add(locationLabel, BorderLayout.WEST);

		return column;
	}

	/**
	 * Builds the respawn list field (shows the time at which the rock would or should have respawned)
	 */
	private JPanel buildRespawnField()
	{
		final JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 5, 0, 5));

		if (runiteRock.isAvailable())
		{
			respawnLabel = new JLabel("Available");
			respawnLabel.setForeground(CURRENT_WORLD);
		}
		else
		{
			Instant respawn = runiteRock.getRespawnTime();
			respawnLabel = new JLabel(TIME_FORMATTER.format(Date.from(respawn)));
			if (runiteRock.getDepletedAt() == null)
			{
				respawnLabel.setForeground(ColorScheme.BRAND_ORANGE);
			}
		}

		respawnLabel.setFont(FontManager.getRunescapeSmallFont());
		column.add(respawnLabel, BorderLayout.WEST);

		return column;
	}

	/**
	 * Builds the last visited list field (shows the time at which the rock was last updated).
	 */
	private JPanel buildLastVisitedField()
	{
		final JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 5, 0, 5));

		Instant respawn = runiteRock.getLastSeenAt();
		lastVisitedLabel = new JLabel(respawn == null ? "Unknown" : TIME_FORMATTER.format(Date.from(respawn)));
		lastVisitedLabel.setFont(FontManager.getRunescapeSmallFont());

		column.add(lastVisitedLabel, BorderLayout.WEST);

		return column;
	}

	public void setCurrent(final boolean current)
	{
		this.current = current;
		final Color foreground = getWorldColor();
		worldLabel.setForeground(foreground);
		locationLabel.setForeground(foreground);
	}

	private Color getWorldColor()
	{
		return current ? CURRENT_WORLD : getWorldColor(world);
	}

	private static Color getWorldColor(final World world)
	{
		final EnumSet<WorldType> types = world.getTypes();
		if (types.contains(WorldType.PVP) || types.contains(WorldType.HIGH_RISK) || types.contains(WorldType.DEADMAN))
		{
			return DANGEROUS_WORLD;
		}

		return types.contains(WorldType.MEMBERS) ? MEMBERS_WORLD : FREE_WORLD;
	}
}
