

package gaul.cacofonix.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author ashish
 */
public class MetricPusher {
    private final InetAddress server;
    private final int port;
    
    public MetricPusher(InetAddress server, int port) {
        this.server = server;
        this.port = port;
    }
    
    public void push(String metric, long timestamp, double value) throws UnsupportedEncodingException, IOException {
        String line = metric + ' ' + timestamp + ' ' + value;
        byte[] data = line.getBytes("UTF-8");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, server, port));
        }
    }
    
    public void push(String metric, double value) throws UnsupportedEncodingException, IOException {
        push(metric, System.currentTimeMillis(), value);
    }
}
