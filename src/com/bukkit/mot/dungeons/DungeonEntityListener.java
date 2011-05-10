package com.bukkit.mot.dungeons;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class DungeonEntityListener extends EntityListener 
{
	private Dungeons plugin;
	
	public DungeonEntityListener(Dungeons plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) 
	{
		if(event.getEntity() instanceof Player)
		{
			plugin.checkLeave((Player)event.getEntity());
		}
	}
}

	