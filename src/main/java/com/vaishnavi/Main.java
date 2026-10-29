package com.vaishnavi;

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point for the Smart Student Management System.
 * Provides a menu-driven interface for all CRUD operations.
 *
 * NOTE: All records are stored in-memory and will be lost on exit.
 */
public class Main {

    private static final StudentService service = new StudentService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Smart Student Management System  v1.0.0        ║");
        System.out.println("║   Developed by Vaishnavi                         ║");
        System.out.println("║   NOTE: Records are in-memory (not persistent).  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addStudent();
                case "2" -> viewAllStudents();
                case "3" -> searchById();
                case "4" -> searchByName();
                case "5" -> updateStudent();
                case "6" -> deleteStudent();
                case "7" -> displayGradeSummary();
                case "0" -> {
                    System.out.println("\n👋 Goodbye! All records have been cleared.");
                    running = false;
                }
                default  -> System.out.println("⚠  Invalid option. Please enter 0–7.");
            }
        }
        scanner.close();
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│            MAIN MENU                │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. Add Student                     │");
        System.out.println("│  2. View All Students               │");
        System.out.println("│  3. Search by Student ID            │");
        System.out.println("│  4. Search by Name                  │");
        System.out.println("│  5. Update Student                  │");
        System.out.println("│  6. Delete Student                  │");
        System.out.println("│  7. Grade Summary                   │");
        System.out.println("│  0. Exit                            │");
        System.out.println("└─────────────────────────────────────┘");
        System.out.print("  Enter choice: ");
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private static void addStudent() {
        System.out.println("\n── Add New Student ──");
        System.out.print("Student ID   : ");
        String id   = scanner.nextLine().trim();
        System.out.print("Full Name    : ");
        String name = scanner.nextLine().trim();
        System.out.print("Department   : ");
        String dept = scanner.nextLine().trim();
        System.out.print("Marks (0–100): ");
        String marksStr = scanner.nextLine().trim();

        try {
            double marks = Double.parseDouble(marksStr);
            service.addStudent(id, name, dept, marks);
            System.out.println("✅ Student added successfully! Grade: " +
                               service.findById(id).getGrade());
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Marks must be a valid number.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void viewAllStudents() {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("\nℹ  No students registered yet.");
            return;
        }
        System.out.println("\n── All Students (" + students.size() + " total) ──");
        printHeader();
        students.forEach(System.out::println);
        printFooter();
    }

    private static void searchById() {
        System.out.print("\nEnter Student ID: ");
        String id = scanner.nextLine().trim();
        try {
            Student s = service.findById(id);
            System.out.println("\n── Student Found ──");
            printHeader();
            System.out.println(s);
            printFooter();
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void searchByName() {
        System.out.print("\nEnter name (or part of it): ");
        String query = scanner.nextLine().trim();
        List<Student> results = service.findByName(query);
        if (results.isEmpty()) {
            System.out.println("ℹ  No students matched '" + query + "'.");
            return;
        }
        System.out.println("\n── Search Results (" + results.size() + " found) ──");
        printHeader();
        results.forEach(System.out::println);
        printFooter();
    }

    private static void updateStudent() {
        System.out.print("\nEnter Student ID to update: ");
        String id = scanner.nextLine().trim();

        try {
            Student existing = service.findById(id);
            System.out.println("Current record: " + existing);
            System.out.println("(Press Enter to keep current value.)");

            System.out.print("New Name       [" + existing.getName() + "]: ");
            String name = scanner.nextLine().trim();

            System.out.print("New Department [" + existing.getDepartment() + "]: ");
            String dept = scanner.nextLine().trim();

            System.out.print("New Marks      [" + existing.getMarks() + "]: ");
            String marksStr = scanner.nextLine().trim();

            Double marks = null;
            if (!marksStr.isEmpty()) {
                try {
                    marks = Double.parseDouble(marksStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Marks must be a valid number.");
                    return;
                }
            }

            service.updateStudent(id,
                name.isEmpty()  ? null : name,
                dept.isEmpty()  ? null : dept,
                marks);
            System.out.println("✅ Student updated successfully! New Grade: " +
                               service.findById(id).getGrade());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void deleteStudent() {
        System.out.print("\nEnter Student ID to delete: ");
        String id = scanner.nextLine().trim();
        try {
            service.findById(id); // Preview
            System.out.print("⚠  Confirm delete student '" + id + "'? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if ("yes".equalsIgnoreCase(confirm)) {
                service.deleteStudent(id);
                System.out.println("✅ Student '" + id + "' deleted.");
            } else {
                System.out.println("ℹ  Delete cancelled.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void displayGradeSummary() {
        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("\nℹ  No students to summarize.");
            return;
        }
        System.out.println("\n── Grade Summary ──");
        System.out.printf("%-6s %-20s %6s %5s%n", "ID", "Name", "Marks", "Grade");
        System.out.println("─".repeat(42));
        students.forEach(s -> System.out.printf("%-6s %-20s %6.2f   %3s%n",
            s.getId(), s.getName(), s.getMarks(), s.getGrade()));
        System.out.println("─".repeat(42));
        double avg = students.stream().mapToDouble(Student::getMarks).average().orElse(0);
        System.out.printf("Average Marks: %.2f | Total Students: %d%n", avg, students.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void printHeader() {
        System.out.println("┌────────────┬──────────────────────┬─────────────────┬────────┬─────┐");
        System.out.println("│ ID         │ Name                 │ Department      │  Marks │Grade│");
        System.out.println("├────────────┼──────────────────────┼─────────────────┼────────┼─────┤");
    }

    private static void printFooter() {
        System.out.println("└────────────┴──────────────────────┴─────────────────┴────────┴─────┘");
    }
}
