package N_241047020;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet("/StudentServlet")
public class StudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Logger instance for logging
    private static final Logger logger = LogManager.getLogger(StudentServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("addStudent".equals(action)) {
            addStudent(request, response);
        } else {
            searchStudent(request, response);
        }
    }

    private void searchStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String rollNumberStr = request.getParameter("rollNumber");
        logger.info("Received input for roll number: {}", rollNumberStr);

        if (rollNumberStr == null || rollNumberStr.trim().isEmpty()) {
            logger.warn("Roll Number is missing in the request");
            out.println("<h2>Error: Roll Number is required</h2>");
            return;
        }

        int rollNumber;
        try {
            rollNumber = Integer.parseInt(rollNumberStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid Roll Number format received: {}", rollNumberStr, e);
            out.println("<h2>Error: Invalid Roll Number format</h2>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Get database connection from DatabaseConnection class
            conn = DatabaseConnection.getConnection();

            // Query to fetch student details
            String sql = "SELECT * FROM Students1 WHERE roll_number = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, rollNumber);

            logger.info("Executing query to fetch student details for Roll Number: {}", rollNumber);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                out.println("<h2>Student Details:</h2>");
                out.println("<p>Roll Number: " + rs.getInt("roll_number") + "</p>");
                out.println("<p>Name: " + rs.getString("name") + "</p>");
                out.println("<p>Age: " + rs.getInt("age") + "</p>");
                out.println("<p>Department: " + rs.getString("department") + "</p>");
                logger.info("Student details found for Roll Number: {}", rollNumber);
            } else {
                out.println("<h2>No Student Found with Roll Number: " + rollNumber + "</h2>");
                logger.warn("No student found for Roll Number: {}", rollNumber);
            }

        } catch (Exception e) {
            logger.error("Error fetching student details", e);
            out.println("<h2>Error fetching student details</h2>");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { logger.error("Failed to close ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { logger.error("Failed to close PreparedStatement", e); }
            try { if (conn != null) { conn.close(); logger.info("Database connection closed."); } } catch (SQLException e) { logger.error("Failed to close Connection", e); }
        }
    }

    private void addStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String rollNumberStr = request.getParameter("rollNumber");
        String name = request.getParameter("name");
        String ageStr = request.getParameter("age");
        String department = request.getParameter("department");

        if (rollNumberStr == null || rollNumberStr.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            ageStr == null || ageStr.trim().isEmpty() ||
            department == null || department.trim().isEmpty()) {
            out.println("<h2>Error: All fields are required</h2>");
            return;
        }

        int rollNumber;
        int age;

        try {
            rollNumber = Integer.parseInt(rollNumberStr);
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            out.println("<h2>Error: Invalid number format for roll number or age</h2>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Get database connection from DatabaseConnection class
            conn = DatabaseConnection.getConnection();

            // SQL Insert Statement
            String sql = "INSERT INTO Students1 (roll_number, name, age, department) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, rollNumber);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, department);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.println("<h2>Student added successfully!</h2>");
            } else {
                out.println("<h2>Error adding student</h2>");
            }

        } catch (Exception e) {
            out.println("<h2>Error adding student</h2>");
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { }
            try { if (conn != null) { conn.close(); } } catch (SQLException e) { }
        }
    }
}
