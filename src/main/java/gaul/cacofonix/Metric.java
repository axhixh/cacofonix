
package gaul.cacofonix;

import java.util.Objects;

/**
 *
 * @author ashish
 */
public class Metric implements Comparable<Metric> {
    private final String name;
    private final int retention;
    private final int interval;
    
    public Metric(String name, int interval, int retention) {
        assert(name != null);
        this.name = name;
        this.interval = interval;
        this.retention = retention;
    }
    
    public String getName() {
        return name;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public int getRetention() {
        return retention;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Metric other = (Metric) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int compareTo(Metric o) {
        return this.name.compareToIgnoreCase(o.name);
    }
    
    
}
