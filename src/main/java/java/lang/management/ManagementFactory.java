package java.lang.management;

public class ManagementFactory {
    public static MemoryUsage getMemoryMXBean(){
        return new MemoryUsage();
    }
}
