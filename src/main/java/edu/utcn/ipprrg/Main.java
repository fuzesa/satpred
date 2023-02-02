package edu.utcn.ipprrg;

import edu.utcn.ipprrg.proc.CustomTLE;
import edu.utcn.ipprrg.util.FileUtil;
import org.orekit.propagation.analytical.tle.TLE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        System.out.println("Hello world!");
        try {
            FileUtil.getManagerWithLoadedData();
            String proba = FileUtil.getResourceFileAsString("tle.txt");
            List<String> probaList = Arrays.asList(proba.split("\n"));
            CustomTLE customTLE = new CustomTLE(probaList);
            TLE probaTLE = customTLE.getTLE().get();
            System.out.println(proba);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException();
        }
    }
}