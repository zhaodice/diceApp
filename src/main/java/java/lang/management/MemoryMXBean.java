package java.lang.management;

public class MemoryMXBean {
    int getObjectPendingFinalizationCount(){
        return 0;
    }
    MemoryUsage getHeapMemoryUsage(){
        return new MemoryUsage();
    }

    MemoryUsage getNonHeapMemoryUsage(){
        return new MemoryUsage();
    }
}
