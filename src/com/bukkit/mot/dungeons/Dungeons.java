package com.bukkit.mot.dungeons;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Dungeons extends JavaPlugin
{
	private DungeonEntityListener entityListener = new DungeonEntityListener(this);
	private DungeonPlayerListener playerListener = new DungeonPlayerListener(this);
	
	private ArrayList<Dungeon> dungeons = new ArrayList<Dungeon>();
	
	private DungeonLoader loader;

	@Override
	public void onDisable() 
	{
		for(Dungeon d : dungeons)
		{
			d.leaveDungeon();
		}
	}

	@Override
	public void onEnable() 
	{
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		loader = new DungeonLoader(this);
		
		dungeons = loader.loadDungeons();
		
		System.out.println("Dungeons Pugin loaded");
	}
	
	//public boolean command(Player sender, String label, String[] args)
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args)
	{
		if(label.equals("dungeon"))
		{
			if(args.length == 0 || args[0].equals("help") || args[0].equals("?"))
			{
				sender.sendMessage("Dungeons Plugin");
				sender.sendMessage("Use:");
				sender.sendMessage("/dungeon create Create a new dungeon");
				sender.sendMessage("/dungeon edit <name> Change an exiting dungeon");
				sender.sendMessage("/dungeon list List all dungeons");
				sender.sendMessage("/dungeon start <name> Start a dungeon and teleport to start point");
			}
			else if(args[0].equals("create"))
			{
				if(args.length == 2)
				{
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						if(p.isOp())
						{
							if(getDungeon(args[1]) == null)
							{
								dungeons.add(new Dungeon(args[1], p, this));
								p.sendMessage("You created a new Dungeon!");
								p.sendMessage("/dungeon addmob <mobtype> To add a mob at your location");
								p.sendMessage("/dungeon addreward <itemtype> <number> To add reward wich users that finish the dungeon will receive");
								p.sendMessage("/dungeon setstart To set the start point of the dungeon");
								p.sendMessage("/dungeon setend To set the end point of the dungeon");
								p.sendMessage("/dungeon msg start/end <msg> To set messages wich are sent to the players at start and end of the dungeon.");
								p.sendMessage("/dungeon save To finish editing the dungeon");
							}
							else p.sendMessage("This dungeon already exitst");
						}
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("edit"))
			{
				if(args.length == 2)
				{
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						if(p.isOp())
						{
							Dungeon d = getDungeon(args[1]);
							if(d != null)
							{
								d.startEdit(p);
								p.sendMessage("You started editing a Dungeon!");
								p.sendMessage("/dungeon addmob <mobtype> To add a mob at your location");
								p.sendMessage("/dungeon addreward <item-id> <number> To add a reward for players who finish the dungeon");
								p.sendMessage("/dungeon setstart To set the start point of the dungeon");
								p.sendMessage("/dungeon setend To set the end point of the dungeon");
								p.sendMessage("/dungeon msg start/end <msg> To set messages wich are sent to the players at start and end of the dungeon.");
								p.sendMessage("/dungeon save To finish editing the dungeon");
							}
							else p.sendMessage("This dungeon already exitst");
						}
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("delete"))
			{
				if(args.length == 2)
				{
					Dungeon d = getDungeon(args[1]);
					if(d != null)
					{
						dungeons.remove(d);
						loader.deleteDungeon(d);
						sender.sendMessage("Dungeon deleted.");
					}
					else sender.sendMessage("Could not find dungeon");
				}
			}
			else if(args[0].equals("list"))
			{
				String s = "";
				for(Dungeon d : dungeons)
				{
					s += d.getName()+",";
				}
				sender.sendMessage("Available Dungeons: "+s);
			}
			else if(args[0].equals("start"))
			{
				if(args.length == 2)
				{
					if(sender instanceof Player)
					{
						Dungeon d = getDungeon(args[1]);
						if(d != null)
						{
							d.startDungeon((Player)sender);
						}
						else sender.sendMessage("Could not find dungeon");
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("leave"))
			{
				if(sender instanceof Player)
				{
					Dungeon d = getDungeon((Player)sender);
					if(d != null)
					{
						d.leaveDungeon();
					}
					else sender.sendMessage("Youre not in any dungeon at the moment");
				}
				else sender.sendMessage("You can't use this command from command line");
			}
			else if(args[0].equals("setstart"))
			{
				if(sender instanceof Player)
				{
					Player p = (Player)sender;
					Dungeon d = getEditedDungeon(p);
					if(d != null)
					{
						d.setStart(p.getLocation());
						p.sendMessage("Start point set.");
					}
					else sender.sendMessage("You have to be in editmode to use this command");
				}
				else sender.sendMessage("You can't use this command from command line");
			}
			else if(args[0].equals("setend"))
			{
				if(sender instanceof Player)
				{
					Player p = (Player)sender;
					Dungeon d = getEditedDungeon(p);
					if(d != null)
					{
						d.setEnd(p.getLocation());
						p.sendMessage("End point set.");
					}
					else sender.sendMessage("You have to be in editmode to use this command");
				}
				else sender.sendMessage("You can't use this command from command line");
			}
			else if(args[0].equals("addmob"))
			{
				if(args.length == 2)
				{
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						Dungeon d = getEditedDungeon(p);
						if(d != null)
						{
							d.addMob(args[1], p);
						}
						else sender.sendMessage("You have to be in editmode to use this command");
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("addreward"))
			{
				if(args.length == 3)
				{
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						Dungeon d = getEditedDungeon(p);
						if(d != null)
						{
							d.addReward(args[1], args[2], p);
						}
						else sender.sendMessage("You have to be in editmode to use this command");
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("msg"))
			{
				if(args.length == 3)
				{
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						Dungeon d = getEditedDungeon(p);
						if(d != null)
						{
							if(args[2].equals("start"))
							{
								d.setStartMsg(args[3]);
							}
							else if(args[2].equals("end"))
							{
								d.setEndMsg(args[3]);
							}
							else sender.sendMessage("Invalid message type. Only start and end allowed.");
						}
						else sender.sendMessage("You have to be in editmode to use this command");
					}
					else sender.sendMessage("You can't use this command from command line");
				}
				else sender.sendMessage("Invalid number of arguments");
			}
			else if(args[0].equals("save"))
			{
				if(sender instanceof Player)
				{
					Player p = (Player)sender;
					Dungeon d = getEditedDungeon(p);
					if(d != null)
					{
						d.endEdit(p);
						loader.saveDungeon(d);
					}
					else sender.sendMessage("You have to be in editmode to use this command");
				}
				else sender.sendMessage("You can't use this command from command line");
			}
			else sender.sendMessage("Did not recognize option "+args[0]);
			return true;
		}
		else return false;
	}
	
	public void checkLeave(Player p)
	{
		Dungeon d = getDungeon(p);
		if(d != null)
		{
			d.leaveDungeon();
		}
	}
	
	public boolean checkFinish(Player p)
	{
		Dungeon d = getDungeon(p);
		if(d != null)
		{
			if(compareLocations(p.getLocation(), d.getEnd()))
			{
				d.finishDungeon();
				return true;
			}
		}
		return false;
	}
	
	public Dungeon getDungeon(String name)
	{
		if(dungeons.size() == 0) return null;
		
		Dungeon d = null;
		for(Dungeon x : dungeons)
		{
			if(x.getName().equals(name))
			{
				d = x;
				break;
			}
		}
		return d;
	}
	
	public Dungeon getDungeon(Player p)
	{
		if(dungeons.size() == 0) return null;
		
		String name = p.getName();
		
		for(Dungeon d : dungeons)
		{
			if(d.getPlayer() != null && !d.isEditing() && d.getPlayer().getName().equals(name)) return d;
		}
		
		return null;
	}
	
	public Dungeon getEditedDungeon(Player p)
	{
		if(dungeons.size() == 0) return null;
		
		String name = p.getName();
		
		for(Dungeon d : dungeons)
		{
			if(d.getPlayer() != null && d.isEditing() && d.getPlayer().getName().equals(name)) return d;
		}
		
		return null;
	}
	
	public boolean compareLocations(Location l, Location k)
	{
		if(l.getBlockX() == k.getBlockX()
				&& l.getBlockY() == k.getBlockY()
				&& l.getBlockZ() == k.getBlockZ())
			return true;
		else return false;
	}
}
