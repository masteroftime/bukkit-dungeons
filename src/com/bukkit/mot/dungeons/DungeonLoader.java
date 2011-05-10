package com.bukkit.mot.dungeons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Location;

public class DungeonLoader 
{
	private static final String driverName = "org.h2.Driver";
	private static final String connectionUrl = "jdbc:h2:plugins/Dungeons/dungeons";
	
	private Connection conn;
	private Dungeons plugin;
	
	public DungeonLoader(Dungeons plugin)
	{
		this.plugin = plugin;
		
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection(connectionUrl);
			
			if(!tablesExist())
				createTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean tablesExist()
	{
		if(conn == null) return false;
		try {
			Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery("SHOW TABLES");
			
			if(rs.next())
			{
				return true;
			}
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void createTables()
	{
		if(conn == null) return;
		try {
			Statement st = conn.createStatement();
			
			st.executeUpdate("create table dungeons (" +
					"name varchar(50) primary key," +
					"creator varchar(50) not null," +
					"smsg varchar(100), emsg varchar(100)," +
					"sx int, sy int, sz int, sw varchar(25)," +
					"ex int, ey int, ez int, ew varchar(25))");
			
			st.executeUpdate("create table mobs (" +
					"dungeon varchar(50)," +
					"id int," +
					"type varchar(50) not null," +
					"x int, y int, z int, world varchar(25)," +
					"primary key(dungeon, id)," +
					"foreign key(dungeon) references dungeons(name) on delete cascade)");
			
			st.executeUpdate("create table rewards (" +
					"dungeon varchar(50)," +
					"id int," +
					"item int not null," +
					"amount int not null," +
					"primary key (dungeon, id)," +
					"foreign key(dungeon) references dungeons(name) on delete cascade)");
			
			st.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Dungeon> loadDungeons()
	{
		if(conn == null) {
			System.out.println("Failed to load dungeons: Connection is null");
			return null;
		}
		try {
			Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery("SELECT * FROM DUNGEONS");
			
			PreparedStatement mobs = conn.prepareStatement("SELECT * FROM MOBS WHERE dungeon = ?");
			PreparedStatement reward = conn.prepareStatement("SELECT * FROM REWARDS WHERE dungeon = ?");
			
			ArrayList<Dungeon> l = new ArrayList<Dungeon>();
			
			while(rs.next())
			{
				String name = rs.getString("name");
				String creator = rs.getString("creator");
				String smsg = rs.getString("smsg");
				String emsg = rs.getString("emsg");
				Location start = new Location(plugin.getServer().getWorld(rs.getString("sw")),
						rs.getInt("sx"), rs.getInt("sy"),rs.getInt("sz"));
				Location end = new Location(plugin.getServer().getWorld(rs.getString("ew")),
						rs.getInt("ex"), rs.getInt("ey"),rs.getInt("ez"));
				
				Dungeon d = new Dungeon(name, creator, start, end, smsg, emsg, plugin);
				
				mobs.setString(1, name);
				reward.setString(1, name);
				
				ResultSet m = mobs.executeQuery();
				
				while (m.next())
				{
					String type = m.getString("type");
					Location loc = new Location(plugin.getServer().getWorld(m.getString("world")),
							m.getInt("x"), m.getInt("y"),m.getInt("z"));
					
					d.addMob(type, loc);
				}
				
				ResultSet r = reward.executeQuery();
				
				while (r.next())
				{
					int item = r.getInt("item");
					int num = r.getInt("amount");
					
					d.addReward(item, num);
				}
				
				l.add(d);
			}
			
			st.close();
			mobs.close();
			reward.close();
			
			return l;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void saveDungeon(Dungeon d)
	{
		if(conn == null) return;
		try {
			Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery("SELECT 'a' FROM DUNGEONS WHERE name = '" + d.getName()+"'");
			
			conn.setAutoCommit(false); //start transaction
			
			if(rs.next())
			{
				st.executeUpdate("DELETE FROM DUNGEONS WHERE name = '"+d.getName()+"'");
			}
			
			st.executeUpdate("INSERT INTO DUNGEONS VALUES('" +
					d.getName()+"','" +
					d.getCreator()+"','"+
					d.getStartMsg()+"','"+
					d.getEndMsg()+"',"+
					d.getStart().getBlockX()+","+
					d.getStart().getBlockY()+","+
					d.getStart().getBlockZ()+",'"+
					d.getStart().getWorld().getName()+"',"+
					d.getEnd().getBlockX()+","+
					d.getEnd().getBlockY()+","+
					d.getEnd().getBlockZ()+",'"+
					d.getEnd().getWorld().getName()+"')");
			
			PreparedStatement m = conn.prepareStatement("INSERT INTO MOBS VALUES (?,?,?,?,?,?,?)");
			m.setString(1, d.getName());
			
			for(int i = 0; i < d.getMobs().size(); i++)
			{
				m.setInt(2, i);
				m.setString(3, d.getMobs().get(i).getName());
				m.setInt(4, d.getMobLocations().get(i).getBlockX());
				m.setInt(5, d.getMobLocations().get(i).getBlockY());
				m.setInt(6, d.getMobLocations().get(i).getBlockZ());
				m.setString(7, d.getMobLocations().get(i).getWorld().getName());
				m.execute();
			}
			
			PreparedStatement r = conn.prepareStatement("INSERT INTO REWARDS VALUES (?,?,?,?)");
			r.setString(1,d.getName());
			
			for(int i = 0; i <= d.getrLast(); i++)
			{
				r.setInt(2, i);
				r.setInt(3, d.getReward()[i].getTypeId());
				r.setInt(4, d.getReward()[i].getAmount());
				r.execute();
			}
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
				conn.setAutoCommit(true);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void deleteDungeon(Dungeon d)
	{
		if(conn == null) return;
		try {
			Statement st = conn.createStatement();
			
			st.executeUpdate("DELETE FROM DUNGEONS WHERE name = '"+d.getName()+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			synchronized (this)
			{
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
