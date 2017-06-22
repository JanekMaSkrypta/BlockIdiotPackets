package pl.insertt.blockidiotpackets.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.google.common.base.Charsets;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.insertt.blockidiotpackets.ExploitAttemptException;
import pl.insertt.blockidiotpackets.bukkit.BlockIdiotPacketsPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomPayloadPacket extends PacketAdapter
{
    private BlockIdiotPacketsPlugin plugin;

    private final Map<UUID, Long> packetUsageTimers;

    private final String[] restricted = {"MC|BSign", "MC|BEdit", "REGISTER"};

    public CustomPayloadPacket(BlockIdiotPacketsPlugin plugin)
    {
        super(plugin, PacketType.Play.Client.CUSTOM_PAYLOAD);

        this.plugin = plugin;
        this.packetUsageTimers = new HashMap<>();
    }

    @Override
    @Deprecated
    public void onPacketReceiving(final PacketEvent event)
    {
        Player p = event.getPlayer();

        long lastUse = packetUsageTimers.getOrDefault(p.getUniqueId(), -1L);

        if(lastUse == -2L)
        {
            event.setCancelled(true);
            return;
        }

        String packetName = event.getPacket().getStrings().readSafely(0);

        if(!ArrayUtils.contains(restricted, packetName))
        {
            return;
        }

        try
        {
            if("REGISTER".equals(packetName))
            {
                int channelsSize = event.getPlayer().getListeningPluginChannels().size();

                PacketContainer container = event.getPacket();

                ByteBuf buffer = container.getSpecificModifier(ByteBuf.class).read(0).copy();

                try
                {
                    for (int i = 0; i < buffer.toString(Charsets.UTF_8).split("\0").length; i++)
                    {
                        if (++channelsSize > 124)
                        {
                            throw new ExploitAttemptException("Too much channels, by: " + p.getName());
                        }
                    }
                }
                finally
                {
                    buffer.release();
                }
            }
            else if(lastUse == -1L | System.currentTimeMillis() - lastUse > 100L)
            {
                packetUsageTimers.put(p.getUniqueId(), System.currentTimeMillis());
            }
            else
            {
                throw new ExploitAttemptException("Packet flood, by: " + p.getName());
            }

            PacketContainer container = event.getPacket();

            ByteBuf buffer = container.getSpecificModifier(ByteBuf.class).read(0).copy();

            byte[] bytes = new byte[buffer.readableBytes()];

            buffer.readBytes(bytes);

            DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));

            ItemStack itemStack = StreamSerializer.getDefault().deserializeItemStack(input);

            try
            {
                if (itemStack == null)
                {
                    return;
                }

                NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(itemStack);

                if(compound != null & compound.containsKey("pages"))
                {
                    NbtList<String> pages = compound.getList("pages");

                    if (pages.size() > 50)
                    {
                        throw new ExploitAttemptException("Book with pages > 50, by: " + p.getName());
                    }

                    for (String page : pages)
                    {
                        if (page.length() > 257)
                        {
                            throw new ExploitAttemptException("Length of page > 257, by: " + p.getName());
                        }
                    }
                }
            }
            finally
            {
                input.close();
                buffer.release();
            }
        }
        catch(Exception ex)
        {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "[BlockIdiotPackets] " + ex.getMessage());
        }
    }

    public Map<UUID, Long> getPacketTimers()
    {
        return packetUsageTimers;
    }

}
