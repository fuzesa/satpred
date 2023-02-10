package edu.utcn.ipprrg.util;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtil {
    private static final String OREKIT_DIR_NAME = "orekit-data";

    private FileUtil() {
    }

    public static List<String> readTextFileToList(String location) {
        Path path = Paths.get(location);

        Stream<String> lines;
        try {
            lines = Files.lines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<String> retVal = lines.collect(Collectors.toList());
        lines.close();
        return retVal;
    }

    /**
     * Load manager data as per the API's documentation
     * https://www.orekit.org/site-orekit-11.3.1/data/default-configuration.html
     */
    public static void loadManagerWithLoadedData() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        final URL url = classLoader.getResource(OREKIT_DIR_NAME);
        if (null != url) {
            try {
                File orekitData = new File(url.toURI());
                DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
                manager.addProvider(new DirectoryCrawler(orekitData));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
