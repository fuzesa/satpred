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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class SatPredService {
    private static final Logger LOG = LogUtil.getLogger(SatPredService.class);

    private static final String OREKIT_DATA_ENV = "OREKIT_DATA";
    private static final String DEFAULT_OREKIT_DATA_DIR = "orekit-data";
    private static final String DEFAULT_OUTPUT_FILE = "satpred_output.txt";

    private final RaDecECFService raDecECFService = new RaDecECFService();
    private final String outputFileName = DEFAULT_OUTPUT_FILE;
    private List<InputTLE> inputTLEList;
    private InputLocationTimestamps inputLocationTimestamps;
    private String observatoryName = "";

    public SatPredService() {
        final String envDir = System.getenv(OREKIT_DATA_ENV);
        final String dataDir = (envDir != null && !envDir.trim().isEmpty()) ? envDir.trim() : DEFAULT_OREKIT_DATA_DIR;
        FileUtil.loadManagerWithLoadedData(dataDir);
        LOG.info("Loaded OREKIT data from: " + dataDir);
    }

    public void parseInput(String[] args) {
        this.inputTLEList = FileUtil.parseTextFileToInputTLEList(args[0]);
        // Parsed (and validated) once up front, then shared read-only across all TLEs.
        this.inputLocationTimestamps = InputUtil.parseInputFileToILT(FileUtil.readTextFileToList(args[1]));
        if (args.length == 3) {
            this.observatoryName = args[2];
        }
    }

    private List<String> createRaDecEstimatesEntryWithPos(InputTLE inputTLE, RaDecECFService.ObservationContext context) {
        try {
            final List<RaDecECF> results = raDecECFService.getRaDecList(inputTLE.getLines(), context);
            return RaDecECFUtil.raDecECFListToStringList(inputTLE.getIndex(), results);
        } catch (RuntimeException e) {
            LOG.warning("Skipping TLE #" + inputTLE.getIndex() + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Computes RA/Dec + ECF for every TLE x timestamp and streams the rows to
     * the output file. Results are computed in parallel but written in input
     * order (forEachOrdered), so only a small window is buffered in memory
     * instead of the full result set.
     */
    public String createRaDecECFEstimatesFile() {
        final Path outputPath = Paths.get(outputFileName);
        final AtomicLong rowCount = new AtomicLong();
        // Compute the satellite-independent geometry (dates + ECI->ECF transforms)
        // once, then share it read-only across all parallel TLE tasks.
        final RaDecECFService.ObservationContext context =
                raDecECFService.buildContext(this.inputLocationTimestamps, this.observatoryName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            inputTLEList.parallelStream()
                    .map(inputTLE -> createRaDecEstimatesEntryWithPos(inputTLE, context))
                    .forEachOrdered(rows -> {
                        try {
                            for (final String row : rows) {
                                writer.write(row);
                                writer.newLine();
                            }
                            rowCount.addAndGet(rows.size());
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed writing to " + outputPath, e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed writing to " + outputPath, e);
        }

        return Constants.FINAL_SUCCESS_MESSAGE + outputFileName + " with " + inputTLEList.size()
                + " TLE(s) X " + this.inputLocationTimestamps.getInputTimestamps().size()
                + " timestamp(s) = " + rowCount.get() + " entries";
    }
}
