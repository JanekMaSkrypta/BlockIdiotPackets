package pl.insertt.blockidiotpackets.bukkit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.insertt.blockidiotpackets.bukkit.BlockIdiotPacketsPlugin;

public class PlayerQuit implements Listener
{
    private final BlockIdiotPacketsPlugin plugin;

    public PlayerQuit(BlockIdiotPacketsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event)
    {
        if(plugin.getPacketListener().getPacketTimers().containsKey(event.getPlayer().getUniqueId()))
        {
            plugin.getPacketListener().getPacketTimers().remove(event.getPlayer().getUniqueId());
        }
    }

}
