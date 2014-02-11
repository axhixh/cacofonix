
package gaul.cacofonix;

import org.aeonbits.owner.Config;

/**
 *
 * @author ashish
 */
@Config.Sources({"file:${cacofonix.config}",
                 "file:${user.home}/.cacofonix.config",
                 "classpath:gaul/cacofonix/cacofonix.properties"})
public interface ServerConfig extends Config {
    @DefaultValue("0.0.0.0")
    String listenerAddr();

    @DefaultValue("22369") // CCFNX
    int listenerPort();

    @DefaultValue("0.0.0.0")
    String reporterAddr();

    @DefaultValue("9002")
    int reporterPort();

    @DefaultValue("jdbc:h2:${user.home}/cacofonix")
    String h2Url();

    @DefaultValue("true")
    boolean useDb();
}
