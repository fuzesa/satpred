package edu.utcn.ipprrg.satpred;

import edu.utcn.ipprrg.satpred.model.RaDecRange;
import edu.utcn.ipprrg.satpred.service.RaDecService;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.FileUtil;
import edu.utcn.ipprrg.satpred.util.RaDecUtil;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2 || args.length == 3) {
            FileUtil.loadManagerWithLoadedData("orekit-data");
            List<String> tleLines = FileUtil.readTextFileToList(args[0]);
            List<String> inputLines = FileUtil.readTextFileToList(args[1]);
            final RaDecService raDecService = new RaDecService();
            final List<RaDecRange> results = args.length == 3 ? raDecService.getRaDecList(tleLines, inputLines, args[2]) : raDecService.getRaDecList(tleLines, inputLines);
            final String generatedFile = FileUtil.writeResultsToFile(RaDecUtil.raDecListToStringList(results));
            System.out.println(Constants.FINAL_SUCCESS_MESSAGE + generatedFile + " with " + results.size() + " entries");
        }
    }
}