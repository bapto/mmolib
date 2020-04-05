package net.mmogroup.mmolib.api.event;

import org.bukkit.event.player.PlayerEvent;

import net.mmogroup.mmolib.api.player.MMOData;

public abstract class MMODataEvent extends PlayerEvent {
	private final MMOData playerData;

	public MMODataEvent(MMOData playerData) {
		super(playerData.getPlayer());

		this.playerData = playerData;
	}

	public MMOData getData() {
		return playerData;
	}
}
