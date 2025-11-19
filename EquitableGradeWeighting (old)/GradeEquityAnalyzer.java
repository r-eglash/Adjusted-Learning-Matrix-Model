package EquitableGradeWeighting;

import java.io.*;
import java.util.*;

public class GradeEquityAnalyzer {
    
    static class Student {
        int id;
        String finalGrade;
        double finalPercent;
        String assessmentGrade;
        double assessmentPercent;
        double homeworkPercent;
        double employabilityPercent;
        double inflationDeflation;
        String disability;
        String race;
        String frl;
        String gender;
        
        public Student(String[] data) {
            this.id = Integer.parseInt(data[0].replace("Student ", "").trim());
            this.finalGrade = data[1].trim();
            this.finalPercent = parseDouble(data[2]);
            this.assessmentGrade = data[3].trim();
            this.assessmentPercent = parseDouble(data[4]);
            this.homeworkPercent = parseDouble(data[5]);
            this.employabilityPercent = parseDouble(data[6]);
            this.inflationDeflation = parseDouble(data[7]);
            this.disability = data[8].trim();
            this.race = data[9].trim();
            this.frl = data[10].trim();
            this.gender = data[11].trim();
        }
        
        private double parseDouble(String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
    
    static class WeightVector {
        double assessment;
        double homework;
        double employability;
        String name;
        
        public WeightVector(String name, double a, double h, double e) {
            this.name = name;
            this.assessment = a;
            this.homework = h;
            this.employability = e;
        }
        
        public double calculateGrade(Student s) {
            return s.assessmentPercent * assessment + 
                   s.homeworkPercent * homework + 
                   s.employabilityPercent * employability;
        }
    }
    
    static class EquityMetrics {
        double mean;
        double stdDev;
        int count;
        
        public EquityMetrics(double mean, double stdDev, int count) {
            this.mean = mean;
            this.stdDev = stdDev;
            this.count = count;
        }
    }
    
    public static void main(String[] args) {
        List<Student> students = loadStudents("Appendix_A_Charted_Grades.csv");
        
        // Define different weighting methods
        WeightVector[] weightingMethods = {
            new WeightVector("Assessment Only", 1.0, 0.0, 0.0),
            new WeightVector("Traditional (70/20/10)", 0.7, 0.2, 0.1),
            new WeightVector("Balanced (60/30/10)", 0.6, 0.3, 0.1),
            new WeightVector("Equal Weights (1/3 each)", 0.33, 0.33, 0.34),
            new WeightVector("Assessment Heavy (80/15/5)", 0.8, 0.15, 0.05),
            new WeightVector("Griffin's Method", 0.6, 0.3, 0.1)
        };
        
        System.out.println("=" .repeat(80));
        System.out.println("GRADE EQUITY ANALYSIS");
        System.out.println("Comparing Different Weighting Methods Across Demographics");
        System.out.println("=" .repeat(80));
        System.out.println();
        
        // Analyze each weighting method
        for (WeightVector method : weightingMethods) {
            analyzeWeightingMethod(students, method);
        }
        
        // Compare equity gaps
        compareEquityGaps(students, weightingMethods);
    }
    
    static List<Student> loadStudents(String filename) {
        List<Student> students = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 12 && !data[0].trim().isEmpty()) {
                    try {
                        students.add(new Student(data));
                    } catch (Exception e) {
                        // Skip invalid rows
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        return students;
    }
    
    static void analyzeWeightingMethod(List<Student> students, WeightVector method) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("WEIGHTING METHOD: " + method.name);
        System.out.printf("Weights: Assessment=%.2f, Homework=%.2f, Employability=%.2f\n",
                         method.assessment, method.homework, method.employability);
        System.out.println("=".repeat(80));
        
        // Calculate grades for all students
        Map<String, List<Double>> gradesByDemographic = new HashMap<>();
        
        // Analyze by Race
        System.out.println("\nEQUITY ANALYSIS BY RACE:");
        analyzeByCategory(students, method, "race", s -> s.race);
        
        // Analyze by Gender
        System.out.println("\nEQUITY ANALYSIS BY GENDER:");
        analyzeByCategory(students, method, "gender", s -> s.gender);
        
        // Analyze by FRL Status
        System.out.println("\nEQUITY ANALYSIS BY SOCIOECONOMIC STATUS (FRL):");
        analyzeByCategory(students, method, "frl", s -> s.frl);
        
        // Analyze by Disability
        System.out.println("\nEQUITY ANALYSIS BY DISABILITY STATUS:");
        analyzeByCategory(students, method, "disability", s -> s.disability);
    }
    
    interface CategoryExtractor {
        String extract(Student s);
    }
    
    static void analyzeByCategory(List<Student> students, WeightVector method, 
                                   String categoryName, CategoryExtractor extractor) {
        Map<String, List<Double>> groupGrades = new HashMap<>();
        
        // Calculate grades and group by category
        for (Student s : students) {
            double grade = method.calculateGrade(s);
            String category = extractor.extract(s);
            groupGrades.computeIfAbsent(category, k -> new ArrayList<>()).add(grade);
        }
        
        // Calculate and display metrics for each group
        Map<String, EquityMetrics> metrics = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : groupGrades.entrySet()) {
            EquityMetrics em = calculateMetrics(entry.getValue());
            metrics.put(entry.getKey(), em);
            System.out.printf("  %-15s: Mean=%.2f, StdDev=%.2f, Count=%d\n",
                             entry.getKey(), em.mean, em.stdDev, em.count);
        }
        
        // Calculate equity gaps
        if (metrics.size() > 1) {
            List<String> groups = new ArrayList<>(metrics.keySet());
            System.out.println("\n  Equity Gaps (differences in mean grades):");
            for (int i = 0; i < groups.size(); i++) {
                for (int j = i + 1; j < groups.size(); j++) {
                    String g1 = groups.get(i);
                    String g2 = groups.get(j);
                    double gap = Math.abs(metrics.get(g1).mean - metrics.get(g2).mean);
                    System.out.printf("    %s vs %s: %.2f points\n", g1, g2, gap);
                }
            }
        }
    }

    static EquityMetrics calculateMetrics(List<Double> grades) {
        if (grades.isEmpty()) {
            return new EquityMetrics(0, 0, 0);
        }
        
        double sum = 0;
        for (double g : grades) {
            sum += g;
        }
        double mean = sum / grades.size();
        
        double variance = 0;
        for (double g : grades) {
            variance += Math.pow(g - mean, 2);
        }
        double stdDev = Math.sqrt(variance / grades.size());
        
        return new EquityMetrics(mean, stdDev, grades.size());
    }

    static void compareEquityGaps(List<Student> students, WeightVector[] methods) {
        System.out.println("\n\n" + "=".repeat(80));
        System.out.println("COMPARATIVE EQUITY GAP ANALYSIS");
        System.out.println("=".repeat(80));
        
        // Compare race equity gaps across methods
        System.out.println("\nRACIAL EQUITY GAPS ACROSS WEIGHTING METHODS:");
        System.out.println("-".repeat(80));
        
        for (WeightVector method : methods) {
            Map<String, List<Double>> raceGrades = new HashMap<>();
            for (Student s : students) {
                double grade = method.calculateGrade(s);
                raceGrades.computeIfAbsent(s.race, k -> new ArrayList<>()).add(grade);
            }
            
            Map<String, Double> raceMeans = new HashMap<>();
            for (Map.Entry<String, List<Double>> entry : raceGrades.entrySet()) {
                EquityMetrics em = calculateMetrics(entry.getValue());
                raceMeans.put(entry.getKey(), em.mean);
            }
            
            double maxGap = 0;
            String maxGapPair = "";
            List<String> races = new ArrayList<>(raceMeans.keySet());
            for (int i = 0; i < races.size(); i++) {
                for (int j = i + 1; j < races.size(); j++) {
                    double gap = Math.abs(raceMeans.get(races.get(i)) - raceMeans.get(races.get(j)));
                    if (gap > maxGap) {
                        maxGap = gap;
                        maxGapPair = races.get(i) + " vs " + races.get(j);
                    }
                }
            }
            
            System.out.printf("%-30s: Max gap = %.2f points (%s)\n", 
                             method.name, maxGap, maxGapPair);
        }
        
        // Summary recommendation
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SUMMARY:");
        System.out.println("The weighting method with the smallest equity gaps while maintaining");
        System.out.println("academic rigor is the most equitable. Consider the trade-offs between");
        System.out.println("assessment-focused grading and inclusion of non-academic factors.");
        System.out.println("=".repeat(80));
    }
}