package lovexyn0827.chunkmap;

import javax.swing.WindowConstants;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class ChunkMapMod implements ModInitializer {

	public static final ChunkMapMod INSTANCE = new ChunkMapMod();
	private ChunkMapFrame chunkmap;

	@Override
	public void onInitialize() {
		System.setProperty("java.awt.headless", "false");
	}

	public void onServerTicked(MinecraftServer server) {
		this.chunkmap.tick();
	}

	public void onServerStarted(MinecraftServer server) {
		this.chunkmap = new ChunkMapFrame(server);
		this.chunkmap.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	public void onServerShutdown(MinecraftServer server) {
		this.chunkmap.setVisible(false);
	}

}
