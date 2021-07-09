package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;

@Mixin(ChunkTicketManager.class)
public interface ChunkTicketManagerMixin {
	@Accessor("ticketsByPosition")
	Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> getTicketsAt();
}
