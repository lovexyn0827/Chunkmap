package lovexyn0827.chunkmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ExampleChunkIcon implements Icon {

	private Color color;

	public ExampleChunkIcon(int color) {
		this.color = new Color(color);
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(this.color);
		g.fillRect(x, y, 16, 16);
	}

	@Override
	public int getIconWidth() {
		return 16;
	}

	@Override
	public int getIconHeight() {
		return 16;
	}

}
