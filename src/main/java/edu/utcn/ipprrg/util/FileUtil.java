package edu.utcn.ipprrg.util;

import edu.utcn.ipprrg.proc.InputData;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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

    /**
     * Load manager data as per the API's documentation
     * https://www.orekit.org/site-orekit-11.3.1/data/default-configuration.html
     */
    public static void loadManagerWithLoadedData() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        final URL url = classLoader.getResource("orekit-data");
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

    public static InputData parseResourceToInputData(String fileName) {
        InputData retVal = new InputData();
        final String proba;
        try {
            proba = getResourceFileAsString(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (null != proba) {
            final List<String> probaList = Arrays.asList(proba.split("\n"));
            retVal.setValsFromList(probaList);
        }
        return retVal;
    }
}
