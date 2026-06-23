package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.model.InputLocationTimestamps;
import edu.utcn.ipprrg.satpred.model.InputTLE;
import edu.utcn.ipprrg.satpred.model.RaDecECF;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.FileUtil;
import edu.utcn.ipprrg.satpred.util.InputUtil;
import edu.utcn.ipprrg.satpred.util.LogUtil;
import edu.utcn.ipprrg.satpred.util.RaDecECFUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class SatPredService {
    private static final Logger LOG = LogUtil.getLogger(SatPredService.class);

    private static final String OREKIT_DATA_ENV = "OREKIT_DATA";
    private static final String DEFAULT_OREKIT_DATA_DIR = "orekit-data";
    private static final String DEFAULT_OUTPUT_FILE = "satpred_output.txt";
    private static final String OK_TLE_SUFFIX = "ok";

    private final RaDecECFService raDecECFService = new RaDecECFService();
    private final String outputFileName = DEFAULT_OUTPUT_FILE;
    private List<InputTLE> inputTLEList;
    private InputLocationTimestamps inputLocationTimestamps;
    private String observatoryName = "";
    private String tleFilePath;

    public SatPredService() {
        final String envDir = System.getenv(OREKIT_DATA_ENV);
        final String dataDir = (envDir != null && !envDir.trim().isEmpty()) ? envDir.trim() : DEFAULT_OREKIT_DATA_DIR;
        FileUtil.loadManagerWithLoadedData(dataDir);
        LOG.info("Loaded OREKIT data from: " + dataDir);
    }

    public void parseInput(String[] args) {
        this.tleFilePath = args[0];
        this.inputTLEList = FileUtil.parseTextFileToInputTLEList(args[0]);
        // Parsed (and validated) once up front, then shared read-only across all TLEs.
        this.inputLocationTimestamps = InputUtil.parseInputFileToILT(FileUtil.readTextFileToList(args[1]));
        if (args.length == 3) {
            this.observatoryName = args[2];
        }
    }

    /** A satellite that produced a complete, exception-free result set: its
     *  per-timestamp results plus its original TLE block. A {@code null}
     *  instance (see {@link #SKIP}) marks a satellite that is dropped. */
    private static final class OkSatellite {
        private static final OkSatellite SKIP = null;
        private final List<RaDecECF> results;
        private final List<String> tleLines;

        private OkSatellite(List<RaDecECF> results, List<String> tleLines) {
            this.results = results;
            this.tleLines = tleLines;
        }
    }

    /** Computes a TLE's results and keeps them only if every requested timestamp
     *  propagated (no skip / short-circuit); otherwise the satellite is dropped. */
    private OkSatellite computeIfExceptionFree(InputTLE inputTLE, RaDecECFService.ObservationContext context) {
        try {
            final List<RaDecECF> results = raDecECFService.getRaDecList(inputTLE.getLines(), context);
            if (results.size() == context.size()) {
                return new OkSatellite(results, inputTLE.getLines());
            }
            return OkSatellite.SKIP; // partial / unpropagatable
        } catch (RuntimeException e) {
            LOG.warning("Skipping TLE #" + inputTLE.getIndex() + ": " + e.getMessage());
            return OkSatellite.SKIP;
        }
    }

    /**
     * Builds the cleaned ("OK") catalog and the predictions from it, in a single
     * pass. Each TLE is propagated once; only the exception-free satellites are
     * kept, and they are written — in catalog order — to both:
     * <ul>
     *   <li>the cleaned TLE catalog next to the input file
     *       ({@code <input>ok.<ext>}), and</li>
     *   <li>the predictions file, indexed by a fresh <em>contiguous</em> index.</li>
     * </ul>
     * So prediction row index {@code K} corresponds 1-to-1 to entry {@code K} of
     * the OK catalog (no gaps). Computation is parallel but writing is ordered
     * (forEachOrdered), so only a small window is buffered in memory.
     */
    public String createRaDecECFEstimatesFile() {
        final Path outputPath = Paths.get(outputFileName);
        final Path okTlePath = deriveOkTlePath(tleFilePath);
        final AtomicLong rowCount = new AtomicLong();
        final AtomicInteger okIndex = new AtomicInteger(); // contiguous index into the OK catalog
        // Compute the satellite-independent geometry (dates + ECI->ECF transforms)
        // once, then share it read-only across all parallel TLE tasks.
        final RaDecECFService.ObservationContext context =
                raDecECFService.buildContext(this.inputLocationTimestamps, this.observatoryName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath);
             BufferedWriter okWriter = Files.newBufferedWriter(okTlePath)) {
            inputTLEList.parallelStream()
                    .map(inputTLE -> computeIfExceptionFree(inputTLE, context))
                    .forEachOrdered(ok -> {
                        if (ok == OkSatellite.SKIP) {
                            return;
                        }
                        final int index = okIndex.getAndIncrement();
                        try {
                            for (final String line : ok.tleLines) {
                                okWriter.write(line);
                                okWriter.newLine();
                            }
                            final List<String> rows = RaDecECFUtil.raDecECFListToStringList(index, ok.results);
                            for (final String row : rows) {
                                writer.write(row);
                                writer.newLine();
                            }
                            rowCount.addAndGet(rows.size());
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed writing output files", e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed writing output files", e);
        }

        final int okTleCount = okIndex.get();
        LOG.info("Wrote " + okTleCount + " exception-free TLE(s) to " + okTlePath
                + "; predictions indexed 0.." + (okTleCount - 1) + " match it 1-to-1");
        return Constants.FINAL_SUCCESS_MESSAGE + outputFileName + " with " + okTleCount
                + " exception-free TLE(s) X " + this.inputLocationTimestamps.getInputTimestamps().size()
                + " timestamp(s) = " + rowCount.get() + " entries (indexed to " + okTlePath.getFileName() + ")";
    }

    /**
     * Derives the cleaned-catalog path from the input TLE file path: same folder,
     * with "ok" inserted before the extension (e.g. {@code 3le.txt -> 3leok.txt},
     * {@code data/06_09_2026.txt -> data/06_09_2026ok.txt}).
     */
    private static Path deriveOkTlePath(String tleFilePath) {
        final Path path = Paths.get(tleFilePath);
        final Path dir = path.getParent();
        final String name = path.getFileName().toString();
        final int dot = name.lastIndexOf('.');
        final String okName = (dot > 0)
                ? name.substring(0, dot) + OK_TLE_SUFFIX + name.substring(dot)
                : name + OK_TLE_SUFFIX;
        return dir != null ? dir.resolve(okName) : Paths.get(okName);
    }
}
