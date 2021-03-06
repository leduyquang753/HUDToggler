/* --------- BEGIN COPYRIGHT ---------

The MIT License

Copyright � 2019 Le Duy Quang

Permission is hereby granted, free of charge,
to any person obtaining a copy of this software
and associated documentation files (the "Software"),
to deal in the Software without restriction, including
without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to
whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice
shall be included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT
SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
------------ END COPYRIGHT -------- */

package cf.leduyquang753.hudtoggler;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

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
