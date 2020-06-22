package com.palmergames.bukkit.towny.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

/**
 * Handle events for all Vehicle related events
 * 
 * @author ElgarL
 * 
 */
public class TownyVehicleListener implements Listener {
	
	private final Towny plugin;

	public TownyVehicleListener(Towny instance) {

		plugin = instance;
	}

	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onVehicleDestroy(VehicleDestroyEvent event) {

		if (plugin.isError()) {
			event.setCancelled(true);
			return;
		}

		if (event.getAttacker() instanceof Player) {
			
			Player player = (Player) event.getAttacker();
			boolean bBreak = true;
			Material vehicle = null;

			switch (event.getVehicle().getType()) {

			case MINECART:
				vehicle = Material.MINECART;
				break;
			
			case MINECART_FURNACE:
				vehicle = Material.FURNACE_MINECART;
				break;
			
			case MINECART_HOPPER:
				vehicle = Material.HOPPER_MINECART;
				break;
				
			case MINECART_CHEST:
				vehicle = Material.CHEST_MINECART;
				break;
				
			case MINECART_MOB_SPAWNER:
				vehicle = Material.MINECART;
				break;
			
			case MINECART_COMMAND:
				vehicle = Material.COMMAND_BLOCK_MINECART;
				break;
			
			case MINECART_TNT:
				vehicle = Material.TNT_MINECART;
				break;
				
			default:
				break;

			}
			
			if ((vehicle != null) && (!TownySettings.isItemUseMaterial(vehicle.toString())))
				return;

			// Get permissions (updates if none exist)
			bBreak = PlayerCacheUtil.getCachePermission(player, event.getVehicle().getLocation(), vehicle, TownyPermission.ActionType.ITEM_USE);

			if (vehicle != null) {

				// Allow the removal if we are permitted
				if (bBreak)
					return;

				event.setCancelled(true);

				/*
				 * Fetch the players cache
				 */
				PlayerCache cache = plugin.getCache(player);

				if (cache.hasBlockErrMsg()) {
					TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

		System.out.println("public void onVehicleEntityCollision(" + event + ")");

		if (plugin.isError()) {
			event.setCancelled(true);
			return;
		}

		Vehicle vehicle = event.getVehicle();
		Entity entity = event.getEntity();
		System.out.println("entity = " + entity);
		System.out.println("entity.getEntityId() = " + entity.getEntityId());
		List<Entity> passengers = vehicle.getPassengers();
		Material material = null;

		System.out.println(" switch (entity.getType() = " + entity.getType() + ")");
		switch (entity.getType()) {

			case ITEM_FRAME:
				System.out.println("case ITEM_FRAME:");
				material = Material.ITEM_FRAME;
				break;

			case LEASH_HITCH:
				System.out.println("case LEASH_HITCH:");
				material = Material.LEAD;
				break;

			case PAINTING:
				System.out.println("case PAINTING:");
				material = Material.PAINTING;
				break;

			default:
				System.out.println("default:");
				break;

		}
		System.out.println("material = " + material);
		System.out.println("material.getKey() = " + material.getKey());

		// Only run for hanging entities
		System.out.println("(entity instanceof Hanging) = " + (entity instanceof Hanging));
		if (entity instanceof Hanging) {
			Location location = entity.getLocation();
			System.out.println("location = " + location);
			System.out.println("location.getWorld().getName() = " + location.getWorld().getName());
			System.out.println("location.getX() = " + location.getX());
			System.out.println("location.getY() = " + location.getY());
			System.out.println("location.getZ() = " + location.getZ());
			// Only check first passenger's perms (if present)
			System.out.println("passngers.size() = " + passengers.size());
			if (passengers.size() >= 1) {
				Entity passenger = passengers.get(0);
				// Check if player
				if (passenger instanceof Player) {
					// Get build permissions (updates cache if none exist)
					boolean bDestroy = PlayerCacheUtil.getCachePermission((Player) passenger, location, material, TownyPermission.ActionType.DESTROY);
					// Allow the removal if we are permitted
					if (bDestroy) {
						System.out.println("don't cancel");
						return;
					// Cancel event otherwise
					} else {
						System.out.println("cancel");
						event.setCancelled(true);
					}
				}
			} else {// Empty vehicle -> cancel event.
				System.out.println("cancel");
				event.setCancelled(true);
			}
		}
		System.out.println("end");
	}
}
