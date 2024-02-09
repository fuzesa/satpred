package edu.utcn.ipprrg.satpred;

import edu.utcn.ipprrg.satpred.service.ReEntryService;
import edu.utcn.ipprrg.satpred.service.SatPredService;

public class Main {
    public static void main(String[] args) {
        /*if (args.length == 2 || args.length == 3) {
            final SatPredService satPredService = new SatPredService();
            satPredService.parseInput(args);
//            final String result = satPredService.createRaDecEstimatesFile();
            final String result = satPredService.createRaDecECFEstimatesFile();
            System.out.println(result);
        } else {
            System.out.println("Two or Three params are required");
            System.out.println("param 1 - TLE File");
            System.out.println("param 2 - Input Location + Time entries file");
            System.out.println("param 3 - Name of observatory (optional)");
        }*/
        final ReEntryService reEntryService = new ReEntryService();
        reEntryService.init();
    }
}
