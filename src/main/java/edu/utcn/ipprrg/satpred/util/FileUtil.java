package edu.utcn.ipprrg.satpred.util;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtil {
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
     * <a href="https://www.orekit.org/site-orekit-11.3.1/data/default-configuration.html">Config</a>
     */
    public static void loadManagerWithLoadedData(String pathToOrekitDir) {
        File orekitData = new File(pathToOrekitDir);
        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

    }

    public static String writeResultsToFile(List<String> results) {
        final String fileName = "resfile_" + Instant.now().getEpochSecond() + ".txt";
        final Path path = Paths.get(fileName);

        try {
            Files.write(path, results, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileName;
    }
}
