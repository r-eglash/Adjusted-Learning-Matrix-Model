package EquitableGradeWeighting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrintCSV {
    public static void main(String[] args) {
        String fileName = "GradeEquityAnalyzer/Appendix_A_Charted_Grades.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}

/*
public class PrintCSV {
    public static void main(String[] args) {
        String fileName = "GradeEquityAnalyzer/Appendix_A_Charted_Grades.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
*/