package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.model.InputTLE;
import edu.utcn.ipprrg.satpred.model.RaDecRange;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.FileUtil;
import edu.utcn.ipprrg.satpred.util.RaDecUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SatPredService {
    // TODO: Add as command line arg
    private static final String DEFAULT_DATA_DIR_PREFIX = "";
    private static final String DEFAULT_DATA_DIR = DEFAULT_DATA_DIR_PREFIX + "orekit-data";
    private final RaDecService raDecService = new RaDecService();
    private List<InputTLE> inputTLEList;
    private List<String> inputLines;
    private String observatoryName = "";

    public SatPredService() {
        FileUtil.loadManagerWithLoadedData(DEFAULT_DATA_DIR);
    }

    public void parseInput(String[] args) {
        // TODO: Validation and custom runtime error
        this.inputTLEList = FileUtil.parseTextFileToInputTLEList(args[0]);
        this.inputLines = FileUtil.readTextFileToList(args[1]);
        if (args.length == 3) {
            this.observatoryName = args[2];
        }
    }

    public List<String> createRaDecEstimatesEntry(final List<String> tleLines) {
        final List<RaDecRange> results = raDecService.getRaDecList(tleLines, this.inputLines, this.observatoryName);
        return RaDecUtil.raDecListToStringList(results);
    }

    public List<String> createRaDecEstimatesEntry(int index, final List<String> tleLines) {
        final List<RaDecRange> results = raDecService.getRaDecList(tleLines, this.inputLines, this.observatoryName);
        return RaDecUtil.raDecListToStringList(index, results);
    }

    public String createRaDecEstimatesFile() {
        final List<String> results = inputTLEList.parallelStream().map(inputTLE -> createRaDecEstimatesEntry(inputTLE.getIndex(), inputTLE.getLines())).flatMap(Collection::stream).collect(Collectors.toList());
        final String generatedFileMessage = FileUtil.writeResultsToFile(results);
        return Constants.FINAL_SUCCESS_MESSAGE + generatedFileMessage + " with " + inputTLEList.size() + " X " + (this.inputLines.size() - 1) + " = " + results.size() + " entries";
    }
}
