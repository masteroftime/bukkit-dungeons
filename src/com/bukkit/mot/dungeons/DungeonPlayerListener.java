package com.bukkit.mot.dungeons;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DungeonPlayerListener extends PlayerListener 
{
	private Dungeons plugin;
	
	public DungeonPlayerListener(Dungeons plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.checkLeave(event.getPlayer());
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		plugin.checkLeave(event.getPlayer());
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if(plugin.checkFinish(event.getPlayer()))
		{
			event.setFrom(event.getPlayer().getLocation());
			event.setCancelled(true);
		}
		
	}
	/*
	@Override
	public void onPlayerCommandPreprocess(PlayerChatEvent event) {
		System.out.println("Command");
		
		String[] msg = event.getMessage().split(" ");
		String label = msg[0];
		String[] args = new String[msg.length-1];
		System.arraycopy(msg, 1, args, 0, args.length);
		plugin.command(event.getPlayer(), label, args);
	}*/
}
