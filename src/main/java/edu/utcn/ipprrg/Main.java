package edu.utcn.ipprrg;

import edu.utcn.ipprrg.proc.Process;
import edu.utcn.ipprrg.util.FileUtil;
import edu.utcn.ipprrg.util.ResultUtil;

import java.util.List;

import static edu.utcn.ipprrg.util.Constants.FINAL_SUCCESS_MESSAGE;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2 || args.length == 3) {
            FileUtil.loadManagerWithLoadedData("orekit-data");
            List<String> tleLines = FileUtil.readTextFileToList(args[0]);
            List<String> inputLines = FileUtil.readTextFileToList(args[1]);
            final Process process = args.length == 3 ? new Process(tleLines, inputLines, args[2]) : new Process(tleLines, inputLines);
            final List<double[]> results = process.getAnglesInNormalizedDegrees();
            final String generatedFile = FileUtil.writeResultsToFile(ResultUtil.resultsToStringList(results));
            System.out.println(FINAL_SUCCESS_MESSAGE + generatedFile + " with " + results.size() + " entries");
        }
    }
}