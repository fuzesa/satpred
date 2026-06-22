package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.InputTLE;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.propagation.analytical.tle.TLE;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtil {
    private static final Logger LOG = LogUtil.getLogger(FileUtil.class);

    private FileUtil() {
    }

    /**
     * Parses a (2- or 3-line) TLE file into a list of {@link InputTLE}.
     * Blocks that fail OREKIT's TLE format check are logged and skipped, and
     * crucially do <em>not</em> consume an index, so the surviving indices are
     * the same as if the bad records had never been in the file.
     */
    public static List<InputTLE> parseTextFileToInputTLEList(String location) {
        final TLEValidator validator = new TLEValidator();
        final List<InputTLE> inputTLEList = new ArrayList<>();
        final Path path = Paths.get(location);
        final AtomicInteger index = new AtomicInteger();
        final AtomicInteger skipped = new AtomicInteger();
        final List<String> tmpList = new ArrayList<>(3);

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                if (validator.isValid(line)) {
                    tmpList.add(line);
                    if ('2' == line.charAt(0)) {
                        addIfValidTLE(inputTLEList, tmpList, index, skipped);
                        tmpList.clear();
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading TLE file: " + path, e);
        }

        LOG.info("Parsed " + inputTLEList.size() + " valid TLE(s) from " + path
                + (skipped.get() > 0 ? " (" + skipped.get() + " invalid skipped)" : ""));
        return inputTLEList;
    }

    private static void addIfValidTLE(List<InputTLE> target, List<String> block,
                                      AtomicInteger index, AtomicInteger skipped) {
        if (block.size() < 2) {
            return;
        }
        final String line1 = block.size() == 2 ? block.get(0) : block.get(1);
        final String line2 = block.size() == 2 ? block.get(1) : block.get(2);
        boolean ok;
        try {
            ok = TLE.isFormatOK(line1, line2);
        } catch (RuntimeException e) {
            ok = false;
        }
        if (ok) {
            target.add(new InputTLE(index.getAndIncrement(), new ArrayList<>(block)));
        } else {
            skipped.incrementAndGet();
            final String name = block.size() == 3 ? block.get(0) : line1;
            LOG.warning("Skipping invalid TLE [" + name.trim() + "]: malformed format line >> " + line1);
        }
    }

    public static List<String> readTextFileToList(String location) {
        final Path path = Paths.get(location);
        try (Stream<String> lines = Files.lines(path)) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading file: " + path, e);
        }
    }

    /**
     * Load manager data as per the API's documentation
     * <a href="https://www.orekit.org/site-orekit-11.3.1/data/default-configuration.html">Config</a>
     */
    public static void loadManagerWithLoadedData(String pathToOrekitDir) {
        final File orekitData = new File(pathToOrekitDir);
        if (!orekitData.isDirectory()) {
            throw new IllegalArgumentException("orekit-data directory not found: "
                    + orekitData.getAbsolutePath()
                    + " (set the OREKIT_DATA environment variable or place 'orekit-data' in the working directory)");
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
    }
}
