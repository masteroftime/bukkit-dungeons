package com.bukkit.mot.dungeons;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Dungeon 
{
	private String name;
	private String creator;
	private Location start;
	private Location end;
	private ArrayList<CreatureType> mobs;
	private ArrayList<Location> mobLocations;
	private ItemStack[] reward;
	private int rLast;
	private String startMsg;
	private String endMsg;
	
	private Dungeons plugin;
	
	private LivingEntity[] dungeonCreatures;
	private int creatureTask;
	private Player player;
	private Location pLoc;
	private boolean started;
	
	private boolean editing;
	private CreatureType wandType;
	
	public Dungeon(String name, Player creator, Dungeons plugin)
	{
		this.plugin = plugin;
		this.name = name;
		this.creator = creator.getName();
		this.start = creator.getLocation();
		this.end = null;
		mobs = new ArrayList<CreatureType>();
		mobLocations = new ArrayList<Location>();
		reward = new ItemStack[20];
		rLast = -1;
		
		startMsg = "Try to reach the end of the dungeon. Good Luck!";
		endMsg = "Congratulations! You completed the Dungeon!";
		
		editing = true;
		this.player = creator;
		dungeonCreatures = new LivingEntity[0];
	}
	
	
	
	public Dungeon(String name, String creator, Location start, Location end,
			String startMsg, String endMsg, Dungeons plugin) {
		this.name = name;
		this.creator = creator;
		this.start = start;
		this.end = end;
		this.startMsg = startMsg;
		this.endMsg = endMsg;
		this.plugin = plugin;
		
		mobs = new ArrayList<CreatureType>();
		mobLocations = new ArrayList<Location>();
		reward = new ItemStack[20];
		rLast = -1;
		
		editing = false;
	}
	
	public void startEdit(Player p)
	{
		synchronized (this) 
		{
			if(!started && !editing)
			{
				editing = true;
				player = p;
				spawnCreatures();
				p.sendMessage("You started editing a Dungeon!");
				p.sendMessage("/dungeon addmob <mobtype> To add a mob at your location");
				p.sendMessage("/dungeon addreward <item-id> <number> To add a reward for players who finish the dungeon");
				p.sendMessage("/dungeon setstart To set the start point of the dungeon");
				p.sendMessage("/dungeon setend To set the end point of the dungeon");
				p.sendMessage("/dungeon msg start/end <msg> To set messages wich are sent to the players at start and end of the dungeon.");
				p.sendMessage("/dungeon save To finish editing the dungeon");
			}
			else p.sendMessage("The dungeon is currently in use.");
		}
	}
	
	public void endEdit(Player p)
	{
		if(player != null && editing && p.getName().equals(player.getName()))
		{
			removeCreatures();
			player.sendMessage("Dungeon saved");
			player = null;
			editing = false;
		}
	}
	
	public void addMob(String name, Player p)
	{
		if(editing && p.getName().equals(player.getName()))
		{
			CreatureType c = CreatureType.fromName(name);
			if(c == null)
			{
				player.sendMessage("Unknown creature "+name);
				return;
			}
			mobs.add(c);
			mobLocations.add(player.getLocation());
			LivingEntity[] temp = new LivingEntity[dungeonCreatures.length+1];
			System.arraycopy(dungeonCreatures, 0, temp, 0, dungeonCreatures.length);
			dungeonCreatures = temp;
			dungeonCreatures[dungeonCreatures.length-1] = player.getLocation().getWorld().spawnCreature(player.getLocation(), c);
			DungeonEntityListener.addEntity(dungeonCreatures[dungeonCreatures.length-1]);
			p.sendMessage("Mob added.");
		}
	}
	
	public void addMob(String type, Location loc)
	{
		mobs.add(CreatureType.fromName(type));
		mobLocations.add(loc);
	}
	
	public void addReward(String item, String num, Player p)
	{
		if(editing && p.getName().equals(player.getName()))
		{
			try {
				int it = Integer.valueOf(item);
				int n = Integer.valueOf(num);
				ItemStack is = new ItemStack(it, n);
				if(rLast < reward.length-1)
				{
					rLast++;
					reward[rLast] = is;
				}
				p.sendMessage("Added reward");
			} catch (NumberFormatException e) {
				p.sendMessage("Not a valid number");
			}
		}
	}
	
	public void addReward(int item, int num)
	{
		ItemStack is = new ItemStack(item, num);
		if(rLast < reward.length-1)
		{
			rLast++;
			reward[rLast] = is;
		}
	}
	
	public void startDungeon(Player p)
	{
		synchronized (this)
		{
			if(!started && !editing)
			{
				started = true;
				pLoc = p.getLocation();
				p.teleport(start);
				player = p;
				spawnCreatures();
				if(startMsg != null) p.sendMessage(startMsg);
			}
			else p.sendMessage("The dungeon is not free at the moment. Try again later.");
		}
	}
	
	public void leaveDungeon()
	{
		if(player != null && !editing)
		{
			Player p = player;
			player = null;
			p.teleport(pLoc);
			removeCreatures();
			pLoc = null;
			started = false;
		}
	}
	
	public void finishDungeon()
	{
		if(player != null && !editing)
		{
			removeCreatures();
			for(int i = 0; i <= rLast; i++)
			{
				player.getInventory().addItem(reward[0]);
			}
			player.sendMessage(endMsg);
			Player p = player;
			player = null;
			p.teleport(pLoc);
			pLoc = null;
			started = false;
		}
	}
	
	public void spawnCreatures()
	{
		Location l = null;
		CreatureType c = null;
		dungeonCreatures = new LivingEntity[mobs.size()];
		for(int i = 0; i < mobs.size(); i++)
		{
			l = mobLocations.get(i);
			c = mobs.get(i);
			dungeonCreatures[i] = l.getWorld().spawnCreature(l, c);
		}
		if(editing)
		{
			for(LivingEntity le : dungeonCreatures)
			{
				DungeonEntityListener.addEntity(le);
			}
			
			creatureTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				
				@Override
				public void run() {
					if(!editing)
					{
						plugin.getServer().getScheduler().cancelTask(creatureTask);
					}
					for(int i = 0; i < dungeonCreatures.length; i++)
					{
						dungeonCreatures[i].teleport(mobLocations.get(i));
					}
				}
			}, 10, 10);
		}
	}
	
	public void removeCreatures()
	{
		for(LivingEntity c : dungeonCreatures)
		{
			if(c != null)
			{
				c.remove();
				if(editing)
				{
					DungeonEntityListener.removeEntity(c);
				}
			}
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Location getStart() {
		return start;
	}
	public void setStart(Location start) {
		this.start = start;
	}
	public Location getEnd() {
		return end;
	}
	public void setEnd(Location end) {
		this.end = end;
	}

	public boolean isStarted() {
		return started;
	}
	
	public ArrayList<CreatureType> getMobs() {
		return mobs;
	}



	public ArrayList<Location> getMobLocations() {
		return mobLocations;
	}



	public ItemStack[] getReward() {
		return reward;
	}



	public Player getPlayer() {
		return player;
	}
	public String getStartMsg() {
		return startMsg;
	}

	public void setStartMsg(String startMsg) {
		this.startMsg = startMsg;
	}

	public String getEndMsg() {
		return endMsg;
	}

	public void setEndMsg(String endMsg) {
		this.endMsg = endMsg;
	}



	public int getrLast() {
		return rLast;
	}



	public boolean isEditing() {
		return editing;
	}



	public void setWandType(CreatureType wandType) {
		this.wandType = wandType;
	}



	public CreatureType getWandType() {
		return wandType;
	}
}
