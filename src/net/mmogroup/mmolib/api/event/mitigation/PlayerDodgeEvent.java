package net.mmogroup.mmolib.api.event.mitigation;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

import net.mmogroup.mmolib.api.event.MMODataEvent;
import net.mmogroup.mmolib.api.player.MMOData;

public class PlayerDodgeEvent extends MMODataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final EntityDamageEvent event;
	private boolean cancelled;

	public PlayerDodgeEvent(MMOData player, EntityDamageEvent event) {
		super(player);

		this.event = event;
	}

	public EntityDamageEvent getEvent() {
		return event;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
