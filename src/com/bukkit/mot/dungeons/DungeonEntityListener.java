package com.bukkit.mot.dungeons;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class DungeonEntityListener extends EntityListener 
{
	private Dungeons plugin;
	
	private static Set<Integer> entities = new HashSet<Integer>();
	private static boolean existEntities;
	
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
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if(existEntities)
		{
			if(entities.contains(event.getEntity().getEntityId()))
			{
				if(event.getEntity() instanceof Creature)
				{
					Creature e = (Creature) event.getEntity();
					e.setTarget(e);
				}
			}
		}
	}
	
	public static void addEntity(Entity e)
	{
		if(e instanceof Creature)
		{
			entities.add(e.getEntityId());
			existEntities = true;
		}
		else System.out.println("Tried to add entity which is no Creature");
	}
	
	public static void removeEntity(Entity e)
	{
		entities.remove(e.getEntityId());
		if(entities.size() == 0) existEntities = false;
	}
}

	