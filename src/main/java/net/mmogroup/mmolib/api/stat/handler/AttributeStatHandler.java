package net.mmogroup.mmolib.api.stat.handler;

import java.util.Iterator;
import java.util.function.Consumer;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import net.mmogroup.mmolib.api.player.MMOData;

public class AttributeStatHandler implements Consumer<MMOData> {
	private final Attribute attribute;
	private final String stat;

	public AttributeStatHandler(Attribute attribute, String stat) {
		this.attribute = attribute;
		this.stat = stat;
	}

	@Override
	public void accept(MMOData data) {
		AttributeInstance ins = data.getPlayer().getAttribute(attribute);
		removeModifiers(ins);
 
		/*
		 * TODO not use base attribute value but rather add an attribute
		 * modifier which cancels out the default value.
		 */
		ins.setBaseValue(data.getStatMap().getStat(stat));
	}

	/*
	 * TODO remove mmoitems. in 1 year when corrupted data is gone
	 */
	private void removeModifiers(AttributeInstance ins) {
		for (Iterator<AttributeModifier> iterator = ins.getModifiers().iterator(); iterator.hasNext();) {
			AttributeModifier attribute = iterator.next();
			if (attribute.getName().startsWith("mmolib.") || attribute.getName().startsWith("mmoitems."))
				ins.removeModifier(attribute);
		}
	}
}