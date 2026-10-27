import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * Employee Payroll Management System (Mini Project)
 * Modules : Attendance, Overtime, Salary Calculation, Payslip Display
 * OOP     : Class & Object, Inheritance, Polymorphism, Encapsulation
 */

// ---------------------------------------------------------------
// Attendance (Class & Object + Encapsulation)
// ---------------------------------------------------------------
class Attendance {
    private int workingDays;
    private int presentDays;
    private double overtimeHours;

    public Attendance(int workingDays, int presentDays, double overtimeHours) {
        this.workingDays = workingDays;
        setPresentDays(presentDays);
        setOvertimeHours(overtimeHours);
    }

    public int getWorkingDays() { return workingDays; }
    public int getPresentDays() { return presentDays; }
    public double getOvertimeHours() { return overtimeHours; }

    public void setPresentDays(int presentDays) {
        // Validation: present days cannot exceed working days or be negative
        if (presentDays < 0 || presentDays > workingDays) {
            throw new IllegalArgumentException("Invalid present days: " + presentDays);
        }
        this.presentDays = presentDays;
    }

    public void setOvertimeHours(double overtimeHours) {
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative");
        }
        this.overtimeHours = overtimeHours;
    }

    public int getAbsentDays() { return workingDays - presentDays; }
}

// ---------------------------------------------------------------
// Employee : abstract base class (Encapsulation + Inheritance)
// ---------------------------------------------------------------
abstract class Employee {
    // Encapsulation: private data, access only through methods
    private final int empId;
    private final String name;
    private final String department;
    private final String designation;
    private Attendance attendance;

    public Employee(int empId, String name, String department, String designation) {
        this.empId = empId;
        this.name = name;
        this.department = department;
        this.designation = designation;
    }

    public int getEmpId() { return empId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public Attendance getAttendance() { return attendance; }
    public void setAttendance(Attendance attendance) { this.attendance = attendance; }

    // Polymorphism: each subclass overrides these methods
    public abstract String getEmployeeType();
    public abstract double calculateBasicPay();
    public abstract double calculateAllowances();
    public abstract double getOvertimeRate();
    public abstract double calculateDeductions(double grossSalary);
    public abstract String deductionDetails(double grossSalary);

    // Common (inherited) behaviour
    public double calculateOvertimePay() {
        return attendance.getOvertimeHours() * getOvertimeRate();
    }

    public double calculateGrossSalary() {
        return calculateBasicPay() + calculateAllowances() + calculateOvertimePay();
    }

    public double calculateNetSalary() {
        double gross = calculateGrossSalary();
        return gross - calculateDeductions(gross);
    }
}

// ---------------------------------------------------------------
// FullTimeEmployee : monthly salary, HRA + DA, PF, tax
// ---------------------------------------------------------------
class FullTimeEmployee extends Employee {
    private final double monthlyBasic;

    public FullTimeEmployee(int id, String name, String dept, String desig, double monthlyBasic) {
        super(id, name, dept, desig);
        this.monthlyBasic = monthlyBasic;
    }

    @Override public String getEmployeeType() { return "Full-Time"; }

    @Override
    public double calculateBasicPay() {
        // Loss of pay for absent days
        Attendance a = getAttendance();
        return monthlyBasic * a.getPresentDays() / a.getWorkingDays();
    }

    @Override
    public double calculateAllowances() {
        return calculateBasicPay() * 0.20 + calculateBasicPay() * 0.10;   // HRA 20% + DA 10%
    }

    @Override
    public double getOvertimeRate() {
        // Double the normal hourly rate (8 hours per working day)
        return (monthlyBasic / (getAttendance().getWorkingDays() * 8.0)) * 2;
    }

    @Override
    public double calculateDeductions(double gross) {
        double pf = calculateBasicPay() * 0.12;
        double pt = 200.0;                                // Professional tax
        double tax = gross > 50000 ? gross * 0.10 : 0.0;  // Income tax
        return pf + pt + tax;
    }

    @Override
    public String deductionDetails(double gross) {
        double pf = calculateBasicPay() * 0.12;
        double tax = gross > 50000 ? gross * 0.10 : 0.0;
        return String.format("  %-28s Rs. %10.2f%n  %-28s Rs. %10.2f%n  %-28s Rs. %10.2f%n",
                "Provident Fund (12%)", pf, "Professional Tax", 200.0, "Income Tax (10% > 50000)", tax);
    }
}

// ---------------------------------------------------------------
// PartTimeEmployee : paid per hour worked (4 hours per present day)
// ---------------------------------------------------------------
class PartTimeEmployee extends Employee {
    private final double hourlyRate;
    private static final int HOURS_PER_DAY = 4;

    public PartTimeEmployee(int id, String name, String dept, String desig, double hourlyRate) {
        super(id, name, dept, desig);
        this.hourlyRate = hourlyRate;
    }

    @Override public String getEmployeeType() { return "Part-Time"; }

    @Override
    public double calculateBasicPay() {
        return getAttendance().getPresentDays() * HOURS_PER_DAY * hourlyRate;
    }

    @Override
    public double calculateAllowances() {
        return 500.0;   // Fixed conveyance allowance
    }

    @Override
    public double getOvertimeRate() { return hourlyRate * 1.5; }

    @Override
    public double calculateDeductions(double gross) {
        return gross > 20000 ? gross * 0.05 : 0.0;
    }

    @Override
    public String deductionDetails(double gross) {
        return String.format("  %-28s Rs. %10.2f%n",
                "Income Tax (5% > 20000)", calculateDeductions(gross));
    }
}

// ---------------------------------------------------------------
// ContractEmployee : paid per day present, TDS only
// ---------------------------------------------------------------
class ContractEmployee extends Employee {
    private final double dailyWage;

    public ContractEmployee(int id, String name, String dept, String desig, double dailyWage) {
        super(id, name, dept, desig);
        this.dailyWage = dailyWage;
    }

    @Override public String getEmployeeType() { return "Contract"; }

    @Override
    public double calculateBasicPay() {
        return dailyWage * getAttendance().getPresentDays();
    }

    @Override
    public double calculateAllowances() { return 0.0; }

    @Override
    public double getOvertimeRate() { return 150.0; }   // Fixed Rs.150 per OT hour

    @Override
    public double calculateDeductions(double gross) { return gross * 0.02; }

    @Override
    public String deductionDetails(double gross) {
        return String.format("  %-28s Rs. %10.2f%n", "TDS (2%)", calculateDeductions(gross));
    }
}

// ---------------------------------------------------------------
// Payslip : builds the formatted payslip for ANY Employee (Polymorphism)
// ---------------------------------------------------------------
class Payslip {
    private static final String LINE = "=".repeat(60);
    private static final String DASH = "-".repeat(60);

    public static String generate(Employee e, String month) {
        Attendance a = e.getAttendance();
        double basic = e.calculateBasicPay();
        double allowances = e.calculateAllowances();
        double ot = e.calculateOvertimePay();
        double gross = e.calculateGrossSalary();
        double deductions = e.calculateDeductions(gross);
        double net = e.calculateNetSalary();

        StringBuilder sb = new StringBuilder();
        sb.append(LINE).append("\n");
        sb.append(String.format("%38s%n", "ABC TECHNOLOGIES PVT LTD"));
        sb.append(String.format("%35s%n", "PAYSLIP - " + month));
        sb.append(LINE).append("\n");
        sb.append(String.format("Employee ID   : %-15d Type        : %s%n", e.getEmpId(), e.getEmployeeType()));
        sb.append(String.format("Name          : %-15s Department  : %s%n", e.getName(), e.getDepartment()));
        sb.append(String.format("Designation   : %s%n", e.getDesignation()));
        sb.append(DASH).append("\n");
        sb.append("ATTENDANCE\n");
        sb.append(String.format("  Working Days: %-6d Present: %-6d Absent: %-6d OT Hours: %.1f%n",
                a.getWorkingDays(), a.getPresentDays(), a.getAbsentDays(), a.getOvertimeHours()));
        sb.append(DASH).append("\n");
        sb.append("EARNINGS\n");
        sb.append(String.format("  %-28s Rs. %10.2f%n", "Basic Pay", basic));
        sb.append(String.format("  %-28s Rs. %10.2f%n", "Allowances", allowances));
        sb.append(String.format("  %-28s Rs. %10.2f%n", "Overtime Pay", ot));
        sb.append(String.format("  %-28s Rs. %10.2f%n", "GROSS SALARY", gross));
        sb.append(DASH).append("\n");
        sb.append("DEDUCTIONS\n");
        sb.append(e.deductionDetails(gross));
        sb.append(String.format("  %-28s Rs. %10.2f%n", "TOTAL DEDUCTIONS", deductions));
        sb.append(DASH).append("\n");
        sb.append(String.format("  %-28s Rs. %10.2f%n", "NET SALARY", net));
        sb.append(LINE).append("\n\n");
        return sb.toString();
    }
}

// ---------------------------------------------------------------
// Main
// ---------------------------------------------------------------
public class PayrollSystem {
    public static void main(String[] args) {
        String month = "October 2026";
        int workingDays = 26;

        // Polymorphism: one array holds different kinds of employees
        Employee[] employees = {
            new FullTimeEmployee(101, "Arun Kumar", "IT", "Software Engineer", 40000),
            new FullTimeEmployee(102, "Priya Devi", "HR", "HR Manager", 60000),
            new PartTimeEmployee(201, "Karthik", "Support", "Support Assistant", 200),
            new ContractEmployee(301, "Meena", "Admin", "Data Entry Operator", 700)
        };

        // Attendance for each employee (working days, present days, OT hours)
        employees[0].setAttendance(new Attendance(workingDays, 24, 10));
        employees[1].setAttendance(new Attendance(workingDays, 26, 5));
        employees[2].setAttendance(new Attendance(workingDays, 20, 6));
        employees[3].setAttendance(new Attendance(workingDays, 22, 8));

        StringBuilder report = new StringBuilder();
        report.append("EMPLOYEE PAYROLL MANAGEMENT SYSTEM\n");
        report.append("Month: ").append(month).append("\n\n");

        double totalGross = 0, totalDed = 0, totalNet = 0;

        for (Employee e : employees) {   // runtime polymorphism
            report.append(Payslip.generate(e, month));
            double g = e.calculateGrossSalary();
            totalGross += g;
            totalDed += e.calculateDeductions(g);
            totalNet += e.calculateNetSalary();
        }

        // Summary table
        report.append("PAYROLL SUMMARY\n");
        report.append("-".repeat(60)).append("\n");
        report.append(String.format("%-5s %-14s %-10s %10s %10s %10s%n",
                "ID", "Name", "Type", "Gross", "Deduct", "Net"));
        report.append("-".repeat(60)).append("\n");
        for (Employee e : employees) {
            double g = e.calculateGrossSalary();
            report.append(String.format("%-5d %-14s %-10s %10.2f %10.2f %10.2f%n",
                    e.getEmpId(), e.getName(), e.getEmployeeType(),
                    g, e.calculateDeductions(g), e.calculateNetSalary()));
        }
        report.append("-".repeat(60)).append("\n");
        report.append(String.format("%-31s %10.2f %10.2f %10.2f%n", "TOTAL", totalGross, totalDed, totalNet));

        System.out.print(report);

        // Save the output as a text file
        try (PrintWriter pw = new PrintWriter(new FileWriter("payroll_output.txt"))) {
            pw.print(report);
            System.out.println("\nOutput saved to payroll_output.txt");
        } catch (IOException ex) {
            System.out.println("Could not write file: " + ex.getMessage());
        }
    }
}
