package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;

@Mixin(ChunkTicketManager.class)
public abstract interface ChunkTicketManagerMixin {
	@Accessor("ticketsByPosition")
	public Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> getTickets();
	
	@Invoker("")
	public abstract void callAddTicket(long position, ChunkTicket<?> ticket);
	
	@Invoker("")
	public abstract void callRemoveTicket(long position, ChunkTicket<?> ticket);
}
