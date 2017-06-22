package pl.insertt.blockidiotpackets.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.insertt.blockidiotpackets.bukkit.listener.CustomPayloadPacket;
import pl.insertt.blockidiotpackets.bukkit.listener.PlayerQuit;

public class BlockIdiotPacketsPlugin extends JavaPlugin
{
    private CustomPayloadPacket listener;

    @Override
    public void onEnable()
    {
        this.listener = new CustomPayloadPacket(this);

        ProtocolLibrary.getProtocolManager().addPacketListener(listener);

        Bukkit.getPluginManager().registerEvents(new PlayerQuit(this), this);
    }

    @Override
    public void onDisable()
    {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

    public CustomPayloadPacket getPacketListener()
    {
        return listener;
    }

}
