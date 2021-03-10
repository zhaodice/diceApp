package java.lang.management;

public class ManagementFactory {
    /*public static MemoryUsage getMemoryMXBean(){
        return new MemoryUsage();
    }*/
    public static MemoryMXBean getMemoryMXBean(){
        return new MemoryMXBean();
    }
}
