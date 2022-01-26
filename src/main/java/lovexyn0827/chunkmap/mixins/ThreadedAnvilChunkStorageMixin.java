package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageMixin {
	@Invoker("getChunkHolder")
	ChunkHolder getCH(long pos);
	
	@Invoker("getTicketManager")
	ChunkTicketManager getTM();
}
