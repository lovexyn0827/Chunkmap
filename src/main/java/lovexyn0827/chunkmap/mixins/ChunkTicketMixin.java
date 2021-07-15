package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.world.ChunkTicket;

@Mixin(ChunkTicket.class)
public interface ChunkTicketMixin {
	@Accessor("tickCreated")
	long getAge();
	
	@Accessor("argument")
	Object getSource();
}
