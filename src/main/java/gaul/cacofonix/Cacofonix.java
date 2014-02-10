
package gaul.cacofonix;

import gaul.cacofonix.reporter.Reporter;
import gaul.cacofonix.store.MemoryDatastore;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.H2Datastore;
import java.io.IOException;


public class Cacofonix {

    public static void main(String[] args) throws IOException {
        System.out.println("Cacofonix");

        String path = System.getProperty("user.home");
        final Datastore store = new H2Datastore(path); //new MemoryDatastore();
        
        int listenerPort = 4005;
        final Listener listener = new Listener(listenerPort, new MetricHandler(store));
        listener.start();
        System.out.println("Started listener at port " + listenerPort);
        
        int httpPort = 9002;
        final Reporter reporter = new Reporter(httpPort, store);
        reporter.start();
        System.out.println("Started reporter at port " + httpPort);
        
        Runnable hook = new Runnable() {
          @Override
          public void run() {
              listener.stop();
              reporter.stop();
              store.close();
          }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(hook));

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
