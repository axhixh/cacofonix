
package gaul.cacofonix;

import java.util.Objects;

/**
 *
 * @author ashish
 */
public class Metric implements Comparable<Metric> {
    private final String name;
    private final String unit;
    private final int retention;
    private final int frequeny;
    
    public Metric(String name, String unit, int frequency, int retention) {
        assert(name != null);
        this.name = name;
        this.unit = unit;
        this.frequeny = frequency;
        this.retention = retention;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public int getFrequeny() {
        return frequeny;
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
