
package gaul.cacofonix.client;

import static java.lang.Math.random;
import java.net.InetAddress;
import static java.net.InetAddress.getLocalHost;

/**
 *
 * @author ashish
 */
public class DemoClient {

    public static void main(String[] args) throws Exception {
        InetAddress address = getLocalHost();
        MetricPusher pusher = new MetricPusher(address, 4005);
        
        for (int i = 0; i < 10; i++) {
            pusher.push("demo.value." + address.getHostName(), random());
            Thread.sleep(3000);
        }
        
    }
}
