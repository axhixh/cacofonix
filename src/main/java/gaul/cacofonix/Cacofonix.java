
package gaul.cacofonix;

import gaul.cacofonix.reporter.Reporter;
import gaul.cacofonix.store.MemoryDatastore;
import gaul.cacofonix.store.Datastore;
import java.io.IOException;


public class Cacofonix {

    public static void main(String[] args) throws IOException {
        System.out.println("Cacofonix");
        
        Datastore store = new MemoryDatastore();
        
        int listenerPort = 4005;
        Listener listener = new Listener(listenerPort, new MetricHandler(store));
        listener.start();
        System.out.println("Started listener at port " + listenerPort);
        
        int httpPort = 9002;
        Reporter reporter = new Reporter(httpPort, store);
        reporter.start();
        System.out.println("Started reporter at port " + httpPort);
        
        while (listener.isRunning()) {
            synchronized (listener) {
                try {
                    listener.wait(1000);
                } catch (InterruptedException ignored) {

                }
            }
        }
        reporter.stop();
        System.out.println("Stopped Cacofonix");
    }
}
