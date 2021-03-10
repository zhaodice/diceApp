package java.nio.file;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.mirai.zhao.dice.AppContext;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

public class Paths {
    public static String getStackTrace(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();

        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }

    private Paths() {

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Path get(String first, String... more) {
        //System.out.println(getStackTrace(new Exception("get !!!")));
        //System.out.println("call get: first="+first+" more="+ Arrays.toString(more));
        return new Path() {
            @NonNull
            @Override
            public String toString() {
                StringBuilder path=new StringBuilder(first);
                for(String file : more){
                    path.append("/").append(file);
                }
                String result=path.toString().replace("\\","/");
                while(result.contains("//")){
                    result=result.replace("//","/");
                }
                return result;
            }

            @Override
            public FileSystem getFileSystem() {
                System.out.println("call path getFileSystem");
                return null;
            }

            @Override
            public boolean isAbsolute() {
                System.out.println("call path isAbsolute");
                return false;
            }

            @Override
            public Path getRoot() {
                System.out.println("call path getRoot");
                return null;
            }

            @Override
            public Path getFileName() {
                System.out.println("call path getFileName");
                return null;
            }

            @Override
            public Path getParent() {
                System.out.println("call path getParent");
                return null;
            }

            @Override
            public int getNameCount() {
                System.out.println("call path getNameCount");
                return 0;
            }

            @Override
            public Path getName(int index) {
                return null;
            }

            @Override
            public Path subpath(int beginIndex, int endIndex) {
                return null;
            }

            @Override
            public boolean startsWith(Path other) {
                return false;
            }

            @Override
            public boolean startsWith(String other) {
                return false;
            }

            @Override
            public boolean endsWith(Path other) {
                return false;
            }

            @Override
            public boolean endsWith(String other) {
                return false;
            }

            @Override
            public Path normalize() {
                return null;
            }

            @Override
            public Path resolve(Path other) {
                return get(this.toString(),other.toString());
            }

            @Override
            public Path resolve(String other) { return get(this.toString(),other); }

            @Override
            public Path resolveSibling(Path other) {
                return null;
            }

            @Override
            public Path resolveSibling(String other) {
                return null;
            }

            @Override
            public Path relativize(Path other) {
                return null;
            }

            @Override
            public URI toUri() {
                return null;
            }

            @Override
            public Path toAbsolutePath() {
                System.out.println("call path toAbsolutePath");
                return get(first,more);
            }

            @Override
            public Path toRealPath(LinkOption... options) throws IOException {
                return null;
            }

            @Override
            public File toFile() {
                return new File(toString());
            }

            @Override
            public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
                return null;
            }

            @Override
            public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
                
                return null;
            }


            @Override
            public Iterator<Path> iterator() {
                return null;
            }

            @Override
            public int compareTo(Path other) {
                return 0;
            }
        };
    }
    public static Path get(URI uri) {
        //System.out.println("call get:"+uri);
        return null;
    }
}
