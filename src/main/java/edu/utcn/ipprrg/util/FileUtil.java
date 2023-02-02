package edu.utcn.ipprrg.util;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

import java.io.*;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

public final class FileUtil {
    private FileUtil() {
    }

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    // https://www.orekit.org/site-orekit-11.3.1/data/default-configuration.html
    public static DataProvidersManager getManagerWithLoadedData() throws URISyntaxException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File orekitData = new File(classLoader.getResource("orekit-data").toURI());
        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
        return manager;
    }
}
