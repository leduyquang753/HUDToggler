package cf.leduyquang753.hudtoggler;

import java.io.*;
import java.util.List;

public class SettingScaleOnly extends Setting {
	/** The scale of the element.<br>
	 * 0: x0,5<br>
	 * 1: x1<br>
	 * 2: x2<br>
	 */
	private int scale = -1;
	
	public SettingScaleOnly(String name, int id, int scale, List<String> tooltips) {
		super(name, true, id, tooltips);
		setScale(scale);
	}
	
	public SettingScaleOnly(String name, int id, List<String> tooltips) {
		super(name, true, id, tooltips);
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = Math.max(-1, Math.min(2, scale));
	}

	@Override
	public void readData(DataInputStream stream) throws IOException {
		setScale(stream.readInt());
	}
	
	@Override
	public void writeData(DataOutputStream stream) throws IOException {
		stream.writeInt(scale);
	}
}
