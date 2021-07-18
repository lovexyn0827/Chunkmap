package lovexyn0827.chunkmap.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.chunkmap.ChunkMapMod;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Inject(method = "tick",at = @At("RETURN"))
	public void onTicked(BooleanSupplier bs,CallbackInfo ci) {
		ChunkMapMod.INSTANCE.onServerTicked((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "runServer",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z",
					shift = At.Shift.AFTER
			)
	)
	public void onServerStarted(CallbackInfo ci) {
		ChunkMapMod.INSTANCE.onServerStarted((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "shutdown",at = @At("RETURN"))
	public void onServerShutdown(CallbackInfo ci) {
		ChunkMapMod.INSTANCE.onServerShutdown((MinecraftServer)(Object)this);
	}
}
