package cf.leduyquang753.hudtoggler;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class CpuWatcher {
	public static String usage = "ERROR";
	private static Object watcher = ManagementFactory.getOperatingSystemMXBean();

	static {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					if (watcher instanceof OperatingSystemMXBean) {
						while (true) {
							sleep(1000);
							usage = (int) (((OperatingSystemMXBean) watcher).getSystemCpuLoad()*100d) + "%";
						}
					}
				} catch (Exception e) {
					usage = "ERROR";
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
}
