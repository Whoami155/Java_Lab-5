import java.io.*;
import java.util.*;

// A simple base class to hold common info for all people in the system
abstract class Person {
    protected String name;
    protected String email;

    public Person() {}

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Every type of person will show info differently
    public abstract void displayInfo();
}

// Represents a single student's details and academic data
class Student extends Person {
    private int rollNo;
    private String course;
    private double marks;
    private char grade;

    public Student(int rollNo, String name, String email, String course, double marks) {
        super(name, email);
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        calculateGrade();
    }

    public int getRollNo() { return rollNo; }
    public String getCourse() { return course; }
    public double getMarks() { return marks; }
    public String getEmail() { return email; }
    public String getName() { return name; }

    // Used whenever we want to update all fields of a student together
    public void updateDetails(String name, String email, String course, double marks) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.marks = marks;
        calculateGrade();
    }

    // Basic grading logic
    private void calculateGrade() {
        if (marks >= 90) grade = 'A';
        else if (marks >= 75) grade = 'B';
        else if (marks >= 60) grade = 'C';
        else grade = 'D';
    }

    @Override
    public void displayInfo() {
        System.out.println("Roll No : " + rollNo);
        System.out.println("Name    : " + name);
        System.out.println("Email   : " + email);
        System.out.println("Course  : " + course);
        System.out.println("Marks   : " + marks);
        System.out.println("Grade   : " + grade);
        System.out.println("-----------------------------------");
    }
}

// Thrown whenever someone tries to access a student that doesn’t exist
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String msg) { super(msg); }
}

// What every record-handling class should be able to do
interface RecordActions {
    void addStudent(Student s);
    void deleteStudent(int rollNo) throws StudentNotFoundException;
    void updateStudent(int rollNo, Student s) throws StudentNotFoundException;
    Student searchStudent(int rollNo) throws StudentNotFoundException;
    void viewAllStudents();
}

// Just a tiny loading animation to make the app feel responsive
class Loader implements Runnable {
    @Override
    public void run() {
        try {
            System.out.print("Loading");
            for (int i = 0; i < 4; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error during loading animation.");
        }
    }
}

// Handles all CRUD, file I/O, and sorting for student records
class StudentManager implements RecordActions {

    private Map<Integer, Student> records = new HashMap<>();
    private final String FILE_NAME = "students.txt";

    // Reads saved data from file when the program starts
    public void loadFromFile() {
        File file = new File(FILE_NAME);

        try {
            if (!file.exists()) file.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                // Each field is separated using "|"
                String[] p = line.split("\\|");

                if (p.length == 5) {

                    int roll = Integer.parseInt(p[0]);
                    String name = p[1];
                    String email = p[2];
                    String course = p[3];
                    double marks = Double.parseDouble(p[4]);

                    records.put(roll, new Student(roll, name, email, course, marks));
                }
            }
            br.close();

        } catch (Exception e) {
            System.out.println("Error loading file.");
        }
    }

    // Saves everything to file — called when exiting
    public void saveToFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME));

            for (Student s : records.values()) {
                bw.write(
                        s.getRollNo() + "|" +
                        s.getName() + "|" +
                        s.getEmail() + "|" +
                        s.getCourse() + "|" +
                        s.getMarks()
                );
                bw.newLine();
            }

            bw.close();
            System.out.println("Records saved successfully.\n");

        } catch (Exception e) {
            System.out.println("Error saving file.");
        }
    }

    // --- CRUD operations ---

    @Override
    public void addStudent(Student s) {
        if (records.containsKey(s.getRollNo())) {
            System.out.println("Duplicate Roll No! Cannot add student.");
            return;
        }
        records.put(s.getRollNo(), s);
        System.out.println("Student Added Successfully!");
    }

    @Override
    public void deleteStudent(int rollNo) throws StudentNotFoundException {
        if (!records.containsKey(rollNo))
            throw new StudentNotFoundException("Student not found!");

        records.remove(rollNo);
        System.out.println("Student Deleted Successfully!");
    }

    @Override
    public void updateStudent(int rollNo, Student s) throws StudentNotFoundException {
        if (!records.containsKey(rollNo))
            throw new StudentNotFoundException("Student not found!");

        records.put(rollNo, s);
        System.out.println("Student Updated Successfully!");
    }

    @Override
    public Student searchStudent(int rollNo) throws StudentNotFoundException {
        if (!records.containsKey(rollNo))
            throw new StudentNotFoundException("Student not found!");

        return records.get(rollNo);
    }

    @Override
    public void viewAllStudents() {
        if (records.isEmpty()) {
            System.out.println("No Records Available.\n");
            return;
        }

        System.out.println("===== Student Records =====");
        for (Student s : records.values()) {
            s.displayInfo();
        }
    }

    // Displays results sorted by marks (highest first)
    public void sortByMarks() {
        if (records.isEmpty()) {
            System.out.println("No Records to Sort.");
            return;
        }

        List<Student> list = new ArrayList<>(records.values());
        list.sort((a, b) -> Double.compare(b.getMarks(), a.getMarks()));

        System.out.println("===== Sorted by Marks (DESC) =====");
        for (Student s : list) s.displayInfo();
    }
}

// Main application — handles user menu + input
public class StudentRecordManagement {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        StudentManager manager = new StudentManager();

        manager.loadFromFile();

        while (true) {
            System.out.println("\n===== Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. View All");
            System.out.println("3. Search");
            System.out.println("4. Delete");
            System.out.println("5. Update");
            System.out.println("6. Sort by Marks");
            System.out.println("7. Save & Exit");
            System.out.print("Enter choice: ");

            int ch;
            try { ch = Integer.parseInt(sc.nextLine()); }
            catch (Exception e) { System.out.println("Invalid Input."); continue; }

            try {
                switch (ch) {

                    case 1 -> {
                        System.out.print("Roll No: ");
                        int roll = Integer.parseInt(sc.nextLine());

                        System.out.print("Name: ");
                        String name = sc.nextLine();

                        System.out.print("Email: ");
                        String email = sc.nextLine();

                        System.out.print("Course: ");
                        String course = sc.nextLine();

                        System.out.print("Marks: ");
                        double marks = Double.parseDouble(sc.nextLine());

                        Thread t = new Thread(new Loader());
                        t.start(); t.join();

                        manager.addStudent(new Student(roll, name, email, course, marks));
                    }

                    case 2 -> manager.viewAllStudents();

                    case 3 -> {
                        System.out.print("Enter Roll No: ");
                        int roll = Integer.parseInt(sc.nextLine());
                        manager.searchStudent(roll).displayInfo();
                    }

                    case 4 -> {
                        System.out.print("Enter Roll No: ");
                        int roll = Integer.parseInt(sc.nextLine());
                        manager.deleteStudent(roll);
                    }

                    case 5 -> {
                        System.out.print("Roll No to Update: ");
                        int roll = Integer.parseInt(sc.nextLine());

                        System.out.print("New Name: ");
                        String name = sc.nextLine();

                        System.out.print("New Email: ");
                        String email = sc.nextLine();

                        System.out.print("New Course: ");
                        String course = sc.nextLine();

                        System.out.print("New Marks: ");
                        double marks = Double.parseDouble(sc.nextLine());

                        Thread t = new Thread(new Loader());
                        t.start(); t.join();

                        manager.updateStudent(roll, new Student(roll, name, email, course, marks));
                    }

                    case 6 -> manager.sortByMarks();

                    case 7 -> {
                        manager.saveToFile();
                        System.out.println("Goodbye!");
                        return;
                    }

                    default -> System.out.println("Invalid Choice!");
                }
            }
            catch (StudentNotFoundException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            catch (Exception e) {
                System.out.println("Invalid Input. Try Again.");
            }
        }
    }
}

