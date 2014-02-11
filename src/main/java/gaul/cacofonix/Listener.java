package gaul.cacofonix;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ashish
 */
public class Listener {
    private static final Logger log = LogManager.getLogger("cacofonix.listener");
    
    private final int port;
    private final PacketHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;
    private DatagramSocket socket;

    public Listener(int port, PacketHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    public void start() throws SocketException {
        if (running.getAndSet(true)) {
            return;
        }
        socket = new DatagramSocket(port);
        Worker worker = new Worker();
        executor = Executors.newSingleThreadExecutor();
        executor.submit(worker);
    }

    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        socket.close();
        executor.shutdown();
        synchronized (this) {
            this.notifyAll();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            while (running.get()) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String line = new String(packet.getData(), packet.getOffset(),
                            packet.getLength(), "UTF-8").trim();
                    if ("QUIT".equalsIgnoreCase(line)) {
                        stop();
                    } else {
                        handler.handle(line);
                    }
                } catch (IOException err) {
                    log.error("Unable to receive data point", err);
                }
            }
        }
    }
}
