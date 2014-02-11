
package gaul.cacofonix;

import gaul.cacofonix.listener.Listener;
import gaul.cacofonix.listener.MetricHandler;
import gaul.cacofonix.reporter.Reporter;
import gaul.cacofonix.store.MemoryDatastore;
import gaul.cacofonix.store.Datastore;
import gaul.cacofonix.store.H2Datastore;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Cacofonix {
    private static final Logger log = LogManager.getLogger("cacofonix");
    
    public static void main(String[] args) throws IOException {
        log.info("Cacofonix");

        ServerConfig config = ConfigFactory.create(ServerConfig.class,
                System.getProperties(),
                System.getenv());
        final Datastore store = config.useDb() ? 
                new H2Datastore(config.h2Url()) : new MemoryDatastore();

        String listenerAddr = config.listenerAddr();
        int listenerPort = config.listenerPort();
        final Listener listener = new Listener(listenerAddr, listenerPort, new MetricHandler(store));
        listener.start();
        log.info("Started listener at {}:{} ", listenerAddr, listenerPort);

        String reporterAddr = config.reporterAddr();
        int reporterPort = config.reporterPort();
        final Reporter reporter = new Reporter(reporterAddr, reporterPort, store);
        reporter.start();
        log.info("Started reporter at {}:{} ", reporterAddr, reporterPort);
        
        Runnable hook = new Runnable() {
          @Override
          public void run() {
              log.info("Shutting down");
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
        log.info("Stopping Cacofonix");
    }
}
