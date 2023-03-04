package lovexyn0827.chunkmap;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.google.common.collect.ImmutableMap;

import lovexyn0827.chunkmap.mixins.ChunkTicketManagerMixin;
import lovexyn0827.chunkmap.mixins.ChunkTicketMixin;
import lovexyn0827.chunkmap.mixins.ThreadedAnvilChunkStorageMixin;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkHolder.LevelType;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Util;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

@SuppressWarnings("serial")
public class ChunkMapFrame extends JFrame {
	private static final ImmutableMap<ChunkHolder.LevelType, Integer> LOADING_LVL_TO_COLOR;
	private static final ImmutableMap<ChunkTicketType<?>, Integer> TICKET_TO_COLOR;
	private static final ImmutableMap<LevelType, String> LOADING_LVL_TO_NAME;
	private static final ImmutableMap<ChunkStatus, Integer> CHUNK_STATUS_TO_COLOR;
	private static final ChunkTicketType<ChunkPos> MANUAL_TICKET = ChunkTicketType.<ChunkPos>create("manual", (a, b) -> 1);
	private ServerWorld world;
	private MinecraftServer server;
	private Canvas map;
	private MapArea area;
	private JLabel status;
	private JTextArea logArea;
	private DisplayMode mode = DisplayMode.CHUNK_LOADING;
	private long lastClick;
	private boolean renderEntityOverlay;
	private Map<ChunkPos, ChunkTicket<?>> manuallyLoadedChunks = new HashMap<>();
	private Object lastDimension;

	public ChunkMapFrame(MinecraftServer server) {
		super("ChunkMap v20210825--" + server.getSaveProperties().getLevelName());
		this.setSize(490, 640);
		this.setLayout(new BorderLayout());
		this.server = server;
		this.world = server.getOverworld();
		this.map = new Canvas();
		this.map.setSize(480, 480);
		this.add(this.map, "North");
		this.status = new JLabel();
		this.status.setSize(480, 20);
		this.status.setText("Initializated");
		this.add(this.status, "South");
		this.logArea = new JTextArea(80, 30);
		this.info("The server started");
		this.logArea.setSize(480, 130);
		JScrollPane jsp = new JScrollPane(this.logArea);
		jsp.setSize(480, 130);
		this.add(jsp, "Center");
		this.area = new MapArea(32, 32, -16, -16);
		this.map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					if(Util.getMeasuringTimeMs() - ChunkMapFrame.this.lastClick < 827) {
						ChunkMapFrame.this.showChunkDetails(e.getX() / 16, e.getY() / 16);
					}
					ChunkMapFrame.this.lastClick = Util.getMeasuringTimeMs();
				}
			}
		});
		this.map.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				switch(e.getKeyChar()) {
				case 'w':
					ChunkMapFrame.this.moveMap(0, e.isAltDown() ? -16 : -1);
					break;
				case 's':
					ChunkMapFrame.this.moveMap(0, e.isAltDown() ? 16 : 1);
					break;
				case 'a':
					ChunkMapFrame.this.moveMap(e.isAltDown() ? -16 : -1, 0);
					break;
				case 'd':
					ChunkMapFrame.this.moveMap(e.isAltDown() ? 16 : 1, 0);
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
					JDialog dia = new JDialog(ChunkMapFrame.this);
					dia.setSize(100,150);
					dia.setLayout(new FlowLayout(FlowLayout.CENTER));
					JTextField x = new JTextField(6);
					JTextField z = new JTextField(6);
					dia.add(x);
					dia.add(z);
					
					JButton b = new JButton("    OK    ");
					b.addActionListener((ae) -> {
						try {
							ChunkMapFrame.this.moveCenterTo(Integer.parseInt(x.getText()), Integer.parseInt(z.getText()));
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
				case 'h':
					ChunkMapFrame.this.openHelps();
					break;
				case 'e':
					ChunkMapFrame.this.renderEntityOverlay ^= true;
					break;
				}
			}
		});
		
		this.setVisible(true);
	}

	protected void moveCenterTo(int x, int z) {
		this.area = this.area.fromCenter(x, z);
	}

	protected void moveMap(int dx, int dz) {
		this.area = this.area.move(dx, dz);
	}

	protected void openHelps() {
		JDialog dia = new JDialog(ChunkMapFrame.this);
		dia.setSize(220, 500);
		int y = 0;
		JLabel l1 = new JLabel("Loading Type");
		l1.setBounds(0, y, 220, 16);
		dia.add(l1);
		y += 16;
		for(Map.Entry<ChunkHolder.LevelType, Integer> entry : LOADING_LVL_TO_COLOR.entrySet()) {
			JLabel l = new JLabel(LOADING_LVL_TO_NAME.get(entry.getKey()), 
					new ExampleChunkIcon(entry.getValue()), 
					SwingConstants.LEFT);
			l.setBounds(0, y, 220, 16);
			dia.add(l);
			y += 16;
		}
		
		JLabel l2 = new JLabel("Chunk Status");
		l2.setBounds(0, y, 220, 16);
		dia.add(l2);
		y += 16;
		for(Map.Entry<ChunkStatus, Integer> entry : CHUNK_STATUS_TO_COLOR.entrySet()) {
			JLabel l = new JLabel(entry.getKey().getId(), 
					new ExampleChunkIcon(entry.getValue()), 
					SwingConstants.LEFT);
			l.setBounds(0, y, 220, 16);
			dia.add(l);
			y += 16;
		}
		
		JLabel l3 = new JLabel("Ticket Type");
		l3.setBounds(0, y, 220, 16);
		dia.add(l3);
		y += 16;
		for(Entry<ChunkTicketType<?>, Integer> entry : TICKET_TO_COLOR.entrySet()) {
			JLabel l = new JLabel(entry.getKey().toString(), 
					new ExampleChunkIcon(entry.getValue()), 
					SwingConstants.LEFT);
			l.setBounds(0, y, 220, 16);
			dia.add(l);
			y += 16;
		}
		
		dia.add(new JLabel());
		dia.setVisible(true);
	}

	protected void setMode(DisplayMode mode) {
		this.mode = mode;
	}

	protected void showChunkDetails(int dx, int dz) {
		int x0 = this.area.minX;
		int z0 = this.area.minZ;
		ServerChunkManager scm = this.world.getChunkManager();
		ThreadedAnvilChunkStorage tacs = scm.threadedAnvilChunkStorage;
		ChunkTicketManager ctm = ((ThreadedAnvilChunkStorageMixin)tacs).getTM();
		ChunkPos pos = new ChunkPos(x0 + dx, z0 + dz);
		ChunkHolder ch = ((ThreadedAnvilChunkStorageMixin)tacs).getCH(pos.toLong());
		JDialog dia = new JDialog(this);
		dia.setSize(460,640);
		dia.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));
		dia.add(new JLabel("              Pos : " + pos + "               "));
		if(ch != null) {
			Chunk c = ch.getCurrentChunk();
			if(c != null) {
				dia.add(new JLabel("                 Status : " + ch.getCurrentChunk().getStatus() + "               "));
			}
			
			dia.add(new JLabel("               Loading Level : " + ch.getLevel() + "(" + LOADING_LVL_TO_NAME.get(ChunkHolder.getLevelType(ch.getLevel())) + ")" + "               "));
			List<String[]> list = new ArrayList<>();
			list.add(new String[] {"Type", "Age", "Source"});
			((ChunkTicketManagerMixin)ctm).getTickets()
					.computeIfAbsent(pos.toLong(), (p) -> SortedArraySet.<ChunkTicket<?>>create(4))
					.forEach((t) -> {
						ChunkTicketMixin tm = (ChunkTicketMixin)(Object)t;
						Object source = tm.getSource();
						list.add(new String[] {
								t.getType().toString(), 
								Long.toString(tm.getAge()), 
								source != null ? source.toString() : "null"
						});
					});
			if(list.isEmpty()) {
				dia.add(new JLabel("No Ticket"));
			} else {
				Object[][] content = new Object[list.size()][];
				int i = 0;
				for(Object obj : list.toArray()) {
					content[i++] = (Object[])obj;
				}
				
				JTable table = new JTable(content, content[0]);
				dia.add(table);
			}
			WorldChunk chunk = ch.getWorldChunk();
			if(chunk != null) {
				List<Entity> entities = new ArrayList<>();
				chunk.collectOtherEntities(null, new Box(new BlockPos(x0 * 16, -65536, z0 * 16)).stretch(480, 10E4, 480), entities, null);
				String[][] content = new String[entities.size()][];
				for(Entity e : entities) {
					Vec3d p = e.getPos();
					content[entities.indexOf(e)] = new String[] {
							e.getType().getTranslationKey().replaceAll("[0-9a-z_]*\\.", "") + '(' + e.getEntityId() + ')', 
							Integer.toString(e.age), 
							Double.toString(p.x), 
							Double.toString(p.y), 
							Double.toString(p.z), 
					};
				}
				
				JTable table = new JTable(content, new String[] {"Type(ID)", "Age", "X", "Y", "Z"});
				table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				table.setSize(480, 100);
				JScrollPane jsp = new JScrollPane(table);
				jsp.setSize(480, 200);
				dia.add(jsp);
			}
		} else {
			dia.add(new JLabel("Chunk Unloaded"));
		}
		
		if(!this.manuallyLoadedChunks.containsKey(pos) &&(ch == null || ch.getLevel() > 31)) {
			dia.add(new JLabel("      Load the chunk to level "));
			JTextField jtf = new JTextField("31", 2);
			dia.add(jtf);
			JButton b = new JButton("        Load       ");
			b.addActionListener((ae) ->  {
				this.server.submitAndJoin(() -> this.load(ctm, pos, Integer.parseInt(jtf.getText())));
				b.setEnabled(false);
			});
			dia.add(b);
		} else if(this.manuallyLoadedChunks.containsKey(pos)) {
			JButton b = new JButton("              Unload              ");
			b.addActionListener((ae) ->  {
				this.server.submitAndJoin(() -> this.unload(ctm, pos));
				b.setEnabled(false);
			});
			dia.add(b);
		}
		JButton b = new JButton("           Close            ");
		b.addActionListener((ae) -> dia.setVisible(false));
		dia.add(b);
		dia.setVisible(true);
	}

	private void unload(ChunkTicketManager ctm, ChunkPos pos) {
		ChunkTicket<?> toRemove = this.manuallyLoadedChunks.get(pos);
		ctm.removeTicket(MANUAL_TICKET, pos, 33 - toRemove.getLevel(), null);
		if(!((ChunkTicketManagerMixin)ctm).getTickets().get(pos.toLong()).contains(toRemove)) {
			this.manuallyLoadedChunks.remove(pos);
		}
	}

	private void load(ChunkTicketManager ctm, ChunkPos pos, int level) {
		ChunkTicket<?> ticket = ChunkTicketMixin.<ChunkPos>create(MANUAL_TICKET, level, pos);
		((ChunkTicketManagerMixin)ctm).callAddTicket(pos.toLong(), ticket);
		this.manuallyLoadedChunks.put(pos, ticket);
	}

	public void tick() {
		this.recalculateSize();
		int pixelsX = this.area.sizeX * 16;
		int pixelsZ = this.area.sizeZ * 16;
		if(pixelsX == 0 || pixelsZ == 0) {
			return;
		}
		
		Image im = this.map.createImage(pixelsX, pixelsZ);
		int ticksToAS = 6000 - this.server.getTicks() % 6000;
		if(ticksToAS == 6000) {
			this.info("Autosave was performed in tick " + this.world.getTime());
		}
		
		ServerChunkManager scm = this.world.getChunkManager();
		ThreadedAnvilChunkStorage tacs = scm.threadedAnvilChunkStorage;
		int loadedCount = scm.getLoadedChunkCount();
		Graphics g = im.getGraphics();
		this.area.forEach((x, z, dx, dz) -> {
			ChunkPos pos = new ChunkPos(x, z);
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
					g.setColor(this.world.isChunkLoaded(pos.x, pos.z) ? Color.BLACK : Color.GRAY);
					g.drawString(Integer.toString(ch.getLevel()), dx * 16, dz * 16 + 16);
				}
				
				break;
				
			case GENERATION:
				if(ch != null) {
					Chunk c = ch.getCurrentChunk();
					if(c != null) {
						ChunkStatus status = ch.getCurrentChunk().getStatus();
						if(status != null) {
							g.setColor(new Color(CHUNK_STATUS_TO_COLOR.get(status)));
							g.fillRect(dx * 16, dz * 16, 16, 16);
						}
					}
				}
				
				break;
			case TICKETS:
				ChunkTicketManager ctm = ((ThreadedAnvilChunkStorageMixin)tacs).getTM();
				List<Color> colors = new ArrayList<>();
				((ChunkTicketManagerMixin)ctm).getTickets()
						.computeIfAbsent(pos.toLong(), (p) -> SortedArraySet.<ChunkTicket<?>>create(4))
						.stream()
						.map(ChunkTicket::getType)
						.map(TICKET_TO_COLOR::get)
						.map((in) -> in == null ? 0 : in)
						.map(Color::new)
						.filter((c) -> !colors.contains(c))
						.forEach((colors::add));
				int[] widths;
				switch(colors.size()) {
				default:
					return;
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
		});
		g.setColor(Color.BLACK);
		ChunkPos originChunk = this.area.getOriginPos();
		Vec3d origin = new Vec3d(originChunk.getStartX(), 0, originChunk.getStartZ());
		if(this.renderEntityOverlay) {
			// FIXME Avoid chunk loading
			for(Entity e : this.world.getOtherEntities(null, new Box(originChunk.getStartPos()).stretch(480, 10E7, 480))) {
				Vec3d p = e.getPos().subtract(origin);
				g.fillOval((int)p.x - 3, (int)p.z - 3, 6, 6);
			}
		}
		
		for(int i = 0; i<= pixelsX; i += 16) {
			g.drawLine(i, 0, i, pixelsZ);
		}
		
		for(int i = 0; i<= pixelsZ; i += 16) {
			g.drawLine(0, i, pixelsX, i);
		}
		
		this.map.getGraphics().drawImage(im, 0, 0, this.map);
		Point p = this.map.getMousePosition();
		ChunkPos pos;
		if(p != null) {
			pos = new ChunkPos(p.x / 16 + originChunk.x, p.y / 16 + originChunk.z);
		} else {
			pos = originChunk;
		}
		
		String status = this.world.getRegistryKey().getValue() + "   " + pos + "   " + loadedCount + "Chunks Loaded   " + ticksToAS;
		this.status.setText(status);
	}
	
	private void recalculateSize() {
		Dimension dim0 = this.getSize();
		Dimension dim = new Dimension(Math.max(dim0.width, 11), Math.max(dim0.height, 161));
		if(!dim.equals(this.lastDimension)) {
			this.lastDimension = dim;
			this.setSize(dim);
			int x = (int) Math.ceil((double) Math.max(dim.width - 10, 0) / 16);
			int z = (int) Math.ceil((double) Math.max(dim.height - 160, 0) / 16);
			this.area = this.area.resize(x, z);
			this.logArea.setSize(dim.width - 10, 130);
			this.map.setSize(dim.width - 10, dim.height - 160);
			this.validate();
			this.repaint();
		}
	}

	private void info(String string) {
		this.logArea.append(Instant.now().toString() + " : " + string + '\n');
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
				.put(MANUAL_TICKET, 0x808080)
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
