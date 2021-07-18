package lovexyn0827.chunkmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.world.ChunkTicket;

@Mixin(ChunkTicket.class)
public class ChunkTicketComparingFixMixin {
	@Inject(method = "compareTo", at = @At("HEAD"), cancellable = true)
	public void processNull(ChunkTicket<?> t, CallbackInfoReturnable<Integer> cir) {
		if(t == null) {
			cir.setReturnValue(0);
			cir.cancel();
		}
	}
}
