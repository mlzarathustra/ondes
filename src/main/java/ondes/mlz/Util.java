package ondes.mlz;

import ondes.synth.voice.VoiceMaker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.System.out;

/**
 * a place to keep static utility functions
 *
 */
public class Util {
    public static String keyStrip(String s) {
        return s.toLowerCase().replaceAll("[^a-z]","");
    }

    public static String getResourceAsString(String fileName) {
        StringBuilder sb=new StringBuilder();
        try {
            InputStream is= Util.class
                .getClassLoader()
                .getResourceAsStream(fileName);
            if (is == null) {
                throw new Exception(
                    "can't get input stream for resource "+fileName);
            }
            BufferedReader in = new BufferedReader(
                new InputStreamReader(is));

            for (;;) {
                String line = in.readLine();
                if (line == null) break;
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

    // list the resource files in a resource subdirectory
    // n.b. "." does not work. From a jar file it's very messy.
    // I have not tried it from tomcat or spring.
    public static List<String> listResourceFiles(String path) {
        List<String> fileNames = new ArrayList<>();

        try {
            URL url= VoiceMaker.class.getClassLoader().getResource(path);
            //out.println("url is "+url);
            if (url == null) return fileNames;

            //  The resources are in the file tree:
            //  OK - opening an input stream like this gives the list.
            if (url.toString().startsWith("file:")) {
                InputStream is = url.openStream();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(is));

                for (; ; ) {
                    String line = in.readLine();
                    if (line == null) break;
                    fileNames.add(line);
                }
                return fileNames;
            }
            else if (url.toString().startsWith("jar:")) {

                // The resources are in a .jar file.
                // Getting a directory list using InputStream from jar
                // is B-R-O-K-E-N so we have to do it the hard way.

                // here is a typical URL.getPath() result:
                //  file:/F:/.../ondes/build/libs/ondes-all.jar!/program

                //out.println("URL path: "+url.getPath());
                String urlPath = url.getPath(); // should start with "file:"
                                                // hence the "5" on the next line.
                String jarPath = urlPath.substring(5, urlPath.indexOf("!"));
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
                //out.println(jar);

                Enumeration<JarEntry> jarEntries = jar.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry ent = jarEntries.nextElement();
                    //out.println(ent.getName());
                    if (ent.getName().startsWith(path+"/") &&
                        !ent.getName().equals(path+"/")) {

                        fileNames.add(ent.getName().substring(path.length()+1));
                    }
                }
            }
        }
        catch (Exception ex) {
            out.println("Error getting resource files");
        }

        return fileNames;
    }

    /**
     * For YAML parsing - package atoms into a list, or
     * return a list if the object already is one.
     *
     * To smooth out the differences between
     * <pre>
     *      midi: note-on
     * and
     *      midi:
     *        - note-on
     *        - note-off
     * and
     *      midi: note-on, note-off
     * </pre>
     *
     * @param obj - a list or something to put in a list
     * @return - some kind of list
     */
    public static List<?> getList(Object obj) {
        if (obj instanceof List) return (List<?>)obj;
        List<Object> rs = new ArrayList<>();

        if (obj instanceof String) {
            String[] parts = obj.toString().split("[, ]+");
            rs.addAll(Arrays.asList(parts));
            return rs;
        }

        rs.add(obj);
        return rs;
    }


}
