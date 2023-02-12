package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.model.RaDecRange;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.FileUtil;
import edu.utcn.ipprrg.satpred.util.RaDecUtil;

import java.util.List;

public class SatPredService {
    private static final String DEFAULT_DATA_DIR = "orekit-data";
    private final RaDecService raDecService = new RaDecService();
    private List<String> tleLines;
    private List<String> inputLines;
    private String observatoryName = "";

    public SatPredService() {
        FileUtil.loadManagerWithLoadedData(DEFAULT_DATA_DIR);
    }

    public void parseInput(String[] args) {
        // TODO: Validation and custom runtime error
        this.tleLines = FileUtil.readTextFileToList(args[0]);
        this.inputLines = FileUtil.readTextFileToList(args[1]);
        if (args.length == 3) {
            this.observatoryName = args[2];
        }
    }

    public String createRaDecEstimatesFile() {
        final List<RaDecRange> results = raDecService.getRaDecList(this.tleLines, this.inputLines, this.observatoryName);
        final String generatedFile = FileUtil.writeResultsToFile(RaDecUtil.raDecListToStringList(results));
        return Constants.FINAL_SUCCESS_MESSAGE + generatedFile + " with " + results.size() + " entries";
    }
}
