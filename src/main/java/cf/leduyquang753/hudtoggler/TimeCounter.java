package cf.leduyquang753.hudtoggler;

import java.io.*;
import java.time.LocalDateTime;

import net.minecraft.client.Minecraft;

public class TimeCounter {
	// The time between the updates in ticks.
	public int updateInterval = 20;
	private int updateTicks = 0;
	public String totalTimeString = "0\"";
	public String sessionTimeString = "0\"";
	public String currentDate = "Loading...";
	public String currentTime = "Loading...";
	public long totalTime = 0;
	public long sessionTime = 0;
	public boolean onlyCountInGame = false;
	private long oldSystemTime = 0;
	private Minecraft mc;
	private boolean initialized = false;

	public Thread shutdown = new Thread(new Runnable() {
		@Override
		public void run() {
			File out = new File(Minecraft.getMinecraft().mcDataDir.getPath(), "timecounter.dat");
			try {
				DataOutputStream writer = new DataOutputStream(new FileOutputStream(out));
				writer.writeLong(totalTime);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				super.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}, "timeCounterShutdown");

	
	private String convertTime(long millis) {
		long seconds = millis / 1000;
		long day = seconds / 86400;
		long hour = seconds % 86400 / 3600;
		long min = seconds % 3600 / 60;
		long sec = seconds % 60;
		return (day > 0 ? day + "d" : "")
				+ (seconds < 3600 ? "" : (day > 0 && hour < 10 ? "0" + hour : hour) + "h")
				+ (seconds < 60 ? "" : (seconds > 3599 && min < 10 ?  "0" + min : min) + ":")
				+ (seconds > 59 && sec < 10 ? "0" + sec : sec) + (seconds < 60 ? "\"" : "");
	}
	
	public void initialize(Minecraft mc) {
		this.mc = mc;
		oldSystemTime = Minecraft.getSystemTime();
		Runtime.getRuntime().addShutdownHook(shutdown);
		try {
			File in = new File(Minecraft.getMinecraft().mcDataDir.getPath(), "timecounter.dat");
			DataInputStream reader = new DataInputStream(new FileInputStream(in));
			totalTime = reader.readLong();
			reader.close();
		} catch (Exception e) {}
		initialized = true;
	}
	
	public void update() {
		if (!initialized) return;
		long currentSystemTime = Minecraft.getSystemTime();
		if (!onlyCountInGame || mc.theWorld != null) {
			long passedTime = currentSystemTime - oldSystemTime;
			totalTime += passedTime;
			sessionTime += passedTime;
		}
		oldSystemTime = currentSystemTime;
		updateTicks++;
		if (updateTicks >= updateInterval) {
			updateTicks = updateTicks % 20;
			totalTimeString = convertTime(totalTime);
			sessionTimeString = convertTime(sessionTime);
			LocalDateTime now = LocalDateTime.now();
			currentDate = now.getDayOfWeek().getValue()+1 + " | " + now.getDayOfMonth() + "/" + now.getMonthValue() + "/" + now.getYear();
			currentTime = now.getHour() + "h" + (now.getMinute() < 10 ? "0" : "") + now.getMinute() + ":" + (now.getSecond() < 10 ? "0" : "") + now.getSecond();
		}
	}
}