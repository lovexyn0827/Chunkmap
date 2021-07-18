package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;

@Mixin(ChunkTicket.class)
public interface ChunkTicketMixin {
	@Accessor("tickCreated")
	long getAge();
	
	@Accessor("argument")
	Object getSource();
	
	@Invoker("<init>")
	static <T> ChunkTicket<T> create(ChunkTicketType<T> t, int lvl, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}
}
