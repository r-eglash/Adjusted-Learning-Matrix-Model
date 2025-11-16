package EquitableGradeWeighting;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EquitableGradeWeighting.java
 *
 * This program models how different grade weighting methods (e.g., assessment-only vs. traditional)
 * affect students’ final grades across demographic groups.
 *
 * It uses the concept of matrix-vector multiplication (A·w = g),
 * where A is the matrix of student scores, w is the weighting vector, and g is the resulting vector
 * of final grades.
 *
 * After computing the final grades for each weighting method, the program calculates
 * and compares the mean grade for each demographic group (race, gender, SES, disability)
 * to evaluate the "equity gap" — the difference between the highest and lowest group averages.
 */
public class EquitableGradeWeighting {

    /**
     * Inner class to represent one student and their data.
     * Each student has demographic attributes and three performance scores.
     */
    static class Student {
        String id;
        String race, gender, ses, disability;
        double assessment, homework, employability;

        /**
         * Constructor: parses a CSV row into a Student object.
         * Expected CSV order:
         * ID, Race, Gender, SES, Disability, Assessment, Homework, Employability
         */
        public Student(String[] row) {
            id = row[0];
            race = row[1];
            gender = row[2];
            ses = row[3];
            disability = row[4];
            assessment = Double.parseDouble(row[5]);
            homework = Double.parseDouble(row[6]);
            employability = Double.parseDouble(row[7]);
        }

        /**
         * Computes the final grade for this student given a weighting method.
         * Uses the linear combination formula:
         *     gᵢ = w₁aᵢ + w₂hᵢ + w₃eᵢ
         * where (a, h, e) are the student's scores and (w₁, w₂, w₃) are the weights.
         */
        public double computeFinal(double[] weights) {
            return assessment * weights[0] + homework * weights[1] + employability * weights[2];
        }
    }

    /**
     * Loads data from a CSV file and returns a list of Student objects.
     * Each line of the CSV corresponds to one student.
     */
    public static List<Student> loadCSV(String path) throws IOException {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // Skip header row
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (row.length >= 8) // ensure valid data row
                    students.add(new Student(row));
            }
        }
        return students;
    }

    /**
     * Groups students by a demographic category (e.g., race or gender),
     * computes the mean final grade for each group under the given weighting method.
     *
     * @param students - list of all students
     * @param groupField - demographic category to group by
     * @param weights - 3-element array representing [Assessment, Homework, Employability]
     * @return map of group name → mean grade
     */
    public static Map<String, Double> meanByGroup(List<Student> students, String groupField, double[] weights) {
        Map<String, List<Double>> grouped = new HashMap<>();

        // Group students' final grades by the selected demographic
        for (Student s : students) {
            String key = switch (groupField.toLowerCase()) {
                case "race" -> s.race;
                case "gender" -> s.gender;
                case "ses" -> s.ses;
                case "disability" -> s.disability;
                default -> "All";
            };
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s.computeFinal(weights));
        }

        // Calculate average final grade for each group
        Map<String, Double> means = new HashMap<>();
        for (var e : grouped.entrySet()) {
            double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            means.put(e.getKey(), avg);
        }
        return means;
    }

    /**
     * Prints the average grade for each group and calculates the equity gap,
     * which measures disparity (max group mean - min group mean).
     */
    public static void printEquityGaps(Map<String, Double> means, String groupName) {
        System.out.println("\nEquity Gaps by " + groupName + ":");

        double min = Collections.min(means.values());
        double max = Collections.max(means.values());

        // Display group means
        for (var e : means.entrySet()) {
            System.out.printf("%-10s → %.2f%n", e.getKey(), e.getValue());
        }

        // The equity gap quantifies the degree of inequality across groups
        System.out.printf("Equity gap (max - min): %.2f%n", (max - min));
    }

    /**
     * Main method:
     * 1. Loads student data from CSV.
     * 2. Defines multiple grading weight methods.
     * 3. For each method, computes group averages and equity gaps.
     */
    public static void main(String[] args) throws IOException {
        String filePath = "/Users/racheleglash/Adjusted-Learning-Matrix-Model/Adjusted-Learning-Matrix-Model/EquitableGradeWeighting/Appendix_A_Charted_Grades.csv"; // path to dataset file
        List<Student> students = loadCSV(filePath);

        // Define the weighting methods as [Assessment, Homework, Employability]
        double[][] methods = {
            {1.0, 0.0, 0.0},  // Assessment-only method
            {0.7, 0.2, 0.1},  // Traditional grading method
            {0.5, 0.3, 0.2}   // Alternative balanced method (example)
        };

        // Labels for each weighting method
        String[] methodNames = {"Assessment-only", "Traditional", "Alternative"};

        // Loop through each weighting system and analyze equity outcomes
        for (int i = 0; i < methods.length; i++) {
            System.out.println("\n========== " + methodNames[i] + " ==========");

            // Compute average final grades per demographic group
            Map<String, Double> raceMeans = meanByGroup(students, "race", methods[i]);
            Map<String, Double> genderMeans = meanByGroup(students, "gender", methods[i]);
            Map<String, Double> sesMeans = meanByGroup(students, "ses", methods[i]);
            Map<String, Double> disMeans = meanByGroup(students, "disability", methods[i]);

            // Print summary results and equity gaps
            printEquityGaps(raceMeans, "Race");
            printEquityGaps(genderMeans, "Gender");
            printEquityGaps(sesMeans, "SES");
            printEquityGaps(disMeans, "Disability");
        }
        System.out.println("\n===== SUMMARY OF EQUITY GAPS =====");
        System.out.printf("%-15s %-10s %-10s %-10s %-10s%n", "Method", "Race", "Gender", "SES", "Disability");
        for (int i = 0; i < methods.length; i++) {
            Map<String, Double> raceMeans = meanByGroup(students, "race", methods[i]);
            Map<String, Double> genderMeans = meanByGroup(students, "gender", methods[i]);
            Map<String, Double> sesMeans = meanByGroup(students, "ses", methods[i]);
            Map<String, Double> disMeans = meanByGroup(students, "disability", methods[i]);

            double raceGap = Collections.max(raceMeans.values()) - Collections.min(raceMeans.values());
            double genderGap = Collections.max(genderMeans.values()) - Collections.min(genderMeans.values());
            double sesGap = Collections.max(sesMeans.values()) - Collections.min(sesMeans.values());
            double disGap = Collections.max(disMeans.values()) - Collections.min(disMeans.values());

            System.out.printf("%-15s %-10.2f %-10.2f %-10.2f %-10.2f%n",
                methodNames[i], raceGap, genderGap, sesGap, disGap);
        }
    }
}