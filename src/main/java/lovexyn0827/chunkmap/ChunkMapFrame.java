package lovexyn0827.chunkmap;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.common.collect.ImmutableMap;

import lovexyn0827.chunkmap.mixins.ChunkTicketManagerMixin;
import lovexyn0827.chunkmap.mixins.ThreadedAnvilChunkStorageMixin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkHolder.LevelType;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

@SuppressWarnings("serial")
public class ChunkMapFrame extends JFrame {

	private static final ImmutableMap<ChunkHolder.LevelType, Integer> LOADING_LVL_TO_COLOR;
	private static final ImmutableMap<ChunkTicketType<?>, Integer> TICKET_TO_COLOR;
	private static final ImmutableMap<LevelType, String> LOADING_LVL_TO_NAME;
	private static final ImmutableMap<ChunkStatus, Integer> CHUNK_STATUS_TO_COLOR;
	private ServerWorld world;
	private MinecraftServer server;
	private Canvas map;
	private ChunkPos center;
	private JLabel status;
	private JTextArea logArea;
	private DisplayMode mode = DisplayMode.CHUNK_LOADING;

	public ChunkMapFrame(MinecraftServer server) {
		super("ChunkMap v20210709--" + server.getSaveProperties().getLevelName());
		this.setSize(640, 530);
		this.setLayout(new BorderLayout());
		this.server = server;
		this.world = server.getOverworld();
		this.map = new Canvas();
		this.map.setSize(480, 480);
		this.add(this.map, "North");
		this.status = new JLabel();
		this.status.setSize(640, 20);
		this.status.setText("Initializated");
		this.add(this.status, "South");
		this.logArea = new JTextArea();
		this.logArea.setSize(160, 530);
		this.add(this.logArea, "East");
		this.center = new ChunkPos(0, 0);
		this.map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ChunkMapFrame.this.showChunkDetails(e.getX() / 16, e.getY() / 16);
			}
		});
		this.map.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				ChunkPos pos = ChunkMapFrame.this.center;
				switch(e.getKeyChar()) {
				case 'w':
					ChunkMapFrame.this.center = new ChunkPos(pos.x, pos.z - 1);
					break;
				case 's':
					ChunkMapFrame.this.center = new ChunkPos(pos.x, pos.z + 1);
					break;
				case 'a':
					ChunkMapFrame.this.center = new ChunkPos(pos.x - 1, pos.z);
					break;
				case 'd':
					ChunkMapFrame.this.center = new ChunkPos(pos.x + 1, pos.z);
					break;
				case '0':
					ChunkMapFrame.this.world = ChunkMapFrame.this.server.getWorld(World.NETHER);
					break;
				case '1':
					ChunkMapFrame.this.world = ChunkMapFrame.this.server.getWorld(World.OVERWORLD);
					break;
				case '2':
					ChunkMapFrame.this.world = ChunkMapFrame.this.server.getWorld(World.END);
					break;
				case 'm':
					Dialog dia = new Dialog(ChunkMapFrame.this);
					dia.setSize(100,80);
					dia.setLayout(new FlowLayout(FlowLayout.CENTER));
					JTextField x = new JTextField(6);
					JTextField z = new JTextField(6);
					dia.add(x);
					dia.add(z);
					
					JButton b = new JButton("    OK    ");
					b.addActionListener((ae) -> {
						try {
							ChunkMapFrame.this.center = new ChunkPos(Integer.parseInt(x.getText()), Integer.parseInt(z.getText()));
							dia.setVisible(false);
						} catch (NumberFormatException ex) {
						}
					});
					dia.add(b);
					dia.setVisible(true);
					break;
				case 'l':
					ChunkMapFrame.this.setMode(DisplayMode.CHUNK_LOADING);
					break;
				case 'g':
					ChunkMapFrame.this.setMode(DisplayMode.GENERATION);
					break;
				case 't':
					ChunkMapFrame.this.setMode(DisplayMode.TICKETS);
					break;
				}
			}
		});
		this.setVisible(true);
	}

	protected void setMode(DisplayMode mode) {
		this.mode = mode;
	}

	protected void showChunkDetails(int dx, int dz) {
		int x0 = this.center.x - 15;
		int z0 = this.center.z - 15;
		ServerChunkManager scm = this.world.getChunkManager();
		ThreadedAnvilChunkStorage tacs = scm.threadedAnvilChunkStorage;
		ChunkTicketManager ctm = ((ThreadedAnvilChunkStorageMixin)tacs).getTM();
		ChunkPos pos = new ChunkPos(x0 + dx, z0 + dz);
		ChunkHolder ch = ((ThreadedAnvilChunkStorageMixin)tacs).getCH(pos.toLong());
		Dialog dia = new Dialog(this);
		dia.setSize(250,160);
		dia.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));
		dia.add(new JLabel("Pos : " + pos + ""));
		dia.add(new JLabel("Status : " + ch.getCurrentStatus()));
		dia.add(new JLabel("Loading Level : " + ch.getLevel() + "(" + LOADING_LVL_TO_NAME.get(ChunkHolder.getLevelType(ch.getLevel())) + ")"));
		dia.add(new JLabel("Tickets : " + ((ChunkTicketManagerMixin)ctm).getTicketsAt()
				.computeIfAbsent(pos.toLong(), (p) -> SortedArraySet.<ChunkTicket<?>>create(4))
				.stream()
				.map(ChunkTicket::getType)
				.map(Object::toString)
				.reduce((r, t) -> r + "," +t)
				.orElse("Nothing")));
		JButton b = new JButton("    Close    ");
		b.addActionListener((ae) -> dia.setVisible(false));
		dia.add(b);
		dia.setVisible(true);
	}

	public void tick() {
		this.setSize(640, 530);
		int ticksToAS = 6000 - this.server.getTicks() % 6000;
		if(ticksToAS == 0) {
			this.info("Autosave was performed in tick" + this.world.getTime());
		}
		ServerChunkManager scm = this.world.getChunkManager();
		ThreadedAnvilChunkStorage tacs = scm.threadedAnvilChunkStorage;
		int loadedCount = scm.getLoadedChunkCount();
		Image im = this.map.createImage(480, 480);
		Graphics g = im.getGraphics();
		int x0 = this.center.x - 15;
		int z0 = this.center.z - 15;
		//[-15,14]
		for(int dx = 0; dx < 30; dx++) {
			for(int dz = 0; dz < 31; dz ++) {
				ChunkPos pos = new ChunkPos(x0 + dx, z0 + dz);
				long posl = pos.toLong();
				ChunkHolder ch = ((ThreadedAnvilChunkStorageMixin)tacs).getCH(posl);
				switch(this.mode) {
				case CHUNK_LOADING:
					if(ch == null) {
						g.setColor(Color.WHITE);
						g.fillRect(dx * 16, dz * 16, 16, 16);
					} else {
						g.setColor(new Color(LOADING_LVL_TO_COLOR.get(ChunkHolder.getLevelType(ch.getLevel()))));
						g.fillRect(dx * 16, dz * 16, dx * 16 + 16, dz * 16 + 16);
						g.setColor(Color.BLACK);
						g.drawString(Integer.toString(ch.getLevel()), dx * 16, dz * 16 + 16);
					}
					break;
				case GENERATION:
					if(ch != null) {
						ChunkStatus status = ch.getCurrentStatus();
						if(status != null) {
							g.setColor(new Color(CHUNK_STATUS_TO_COLOR.get(status)));
							g.fillRect(dx * 16, dz * 16, 16, 16);
						}
					}
					break;
				case TICKETS:
					ChunkTicketManager ctm = ((ThreadedAnvilChunkStorageMixin)tacs).getTM();
					List<Color> colors = new ArrayList<>();
					((ChunkTicketManagerMixin)ctm).getTicketsAt()
							.computeIfAbsent(pos.toLong(), (p) -> SortedArraySet.<ChunkTicket<?>>create(4))
							.stream()
							.map(ChunkTicket::getType)
							.map(TICKET_TO_COLOR::get)
							.map(Color::new)
							.forEach((colors::add));
					int[] widths;
					switch(colors.size()) {
					default:
						continue;
					case 1:
						widths = new int[] {16};
						break;
					case 2:
						widths = new int[] {8, 8};
						break;
					case 3:
						widths = new int[] {5, 6, 5};
						break;
					case 4:
						widths = new int[] {4, 4, 4, 4};
						break;
					}
					if(ch != null) {
						int xOffset = 0;
						for(int i = 0; i < widths.length; i++) {
							g.setColor(colors.get(i));
							g.fillRect(dx * 16 + xOffset, dz * 16, widths[i], 16);
							xOffset += widths[i];
						}
					}
					break;
				}
			}
		}
		
		g.setColor(Color.BLACK);
		for(int i = 0; i<= 480; i += 16) {
			g.drawLine(i, 0, i, 480);
		}
		
		for(int i = 0; i<= 480; i += 16) {
			g.drawLine(0, i, 480, i);
		}
		
		this.map.getGraphics().drawImage(im, 0, 0, this.map);
		Point p = this.map.getMousePosition();
		ChunkPos pos;
		if(p != null) {
			pos = new ChunkPos(p.x / 16 + this.center.x - 15, p.y / 16 + this.center.z - 15);
		} else {
			pos = this.center;
		}
		
		String status = this.world.getRegistryKey().getValue() + "   " + pos + "   " + loadedCount + "Chunks Loaded   " + ticksToAS;
		this.status.setText(status);
	}
	
	private void info(String string) {
		this.logArea.append(Instant.now().toString() + " : " + string);
	}

	static {
		LOADING_LVL_TO_COLOR = new ImmutableMap.Builder<ChunkHolder.LevelType, Integer>()
				.put(ChunkHolder.LevelType.ENTITY_TICKING, 0xAADD00)
				.put(ChunkHolder.LevelType.TICKING, 0xFF0000)
				.put(ChunkHolder.LevelType.BORDER, 0xFFCC00)
				.put(ChunkHolder.LevelType.INACCESSIBLE, 0xCCCCCC)
				.build();
		LOADING_LVL_TO_NAME = new ImmutableMap.Builder<ChunkHolder.LevelType, String>()
				.put(ChunkHolder.LevelType.ENTITY_TICKING, "Entity Processing")
				.put(ChunkHolder.LevelType.TICKING, "Redstone Ticking")
				.put(ChunkHolder.LevelType.BORDER, "Border")
				.put(ChunkHolder.LevelType.INACCESSIBLE,"Inaccessible")
				.build();
		TICKET_TO_COLOR = new ImmutableMap.Builder<ChunkTicketType<?>, Integer>()
				.put(ChunkTicketType.PLAYER, 0x0000DD)
				.put(ChunkTicketType.PORTAL, 0xAA00AA)
				.put(ChunkTicketType.DRAGON, 0xAA0088)
				.put(ChunkTicketType.FORCED, 0x8899AA)
				.put(ChunkTicketType.LIGHT, 0xFFFF00)
				.put(ChunkTicketType.POST_TELEPORT, 0xAA00AA)
				.put(ChunkTicketType.START, 0xDDFF00)
				.put(ChunkTicketType.UNKNOWN, 0xFF55FF)
				.build();
		CHUNK_STATUS_TO_COLOR = new ImmutableMap.Builder<ChunkStatus, Integer>()
				.put(ChunkStatus.FULL, 0xBBBBBB)
				.put(ChunkStatus.HEIGHTMAPS, 0x333333)
				.put(ChunkStatus.SPAWN, 0x003333)
				.put(ChunkStatus.LIGHT, 0x555500)
				.put(ChunkStatus.FEATURES, 0x88BB22)
				.put(ChunkStatus.LIQUID_CARVERS, 0x654321)
				.put(ChunkStatus.CARVERS, 0x330033)
				.put(ChunkStatus.SURFACE, 0x333300)
				.put(ChunkStatus.NOISE, 0x330000)
				.put(ChunkStatus.BIOMES, 0x003300)
				.put(ChunkStatus.STRUCTURE_REFERENCES, 0x000033)
				.put(ChunkStatus.STRUCTURE_STARTS, 0x666666)
				.put(ChunkStatus.EMPTY, 0xFF8888)
				.build();
	}

}
