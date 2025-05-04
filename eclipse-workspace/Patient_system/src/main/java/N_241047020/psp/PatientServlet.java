package N_241047020.psp;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet("/PatientServlet")
public class PatientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(PatientServlet.class);
    
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("Log4j2 is initialized and working!");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("addPatient".equals(action)) {
            addPatient(request, response);
        } else if ("searchByIllness".equals(action)) {
            searchPatientsByIllness(request, response);
        }
    }

    private void addPatient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String pIdStr = request.getParameter("p_id");
        String name = request.getParameter("p_name");
        String illness = request.getParameter("illness");
        String ageStr = request.getParameter("age");

        if (pIdStr == null || name == null || illness == null || ageStr == null ||
            pIdStr.trim().isEmpty() || name.trim().isEmpty() || illness.trim().isEmpty() || ageStr.trim().isEmpty()) {
            out.println("<h2>Error: All fields are required</h2>");
            return;
        }

        int pId, age;
        try {
            pId = Integer.parseInt(pIdStr);
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            out.println("<h2>Error: Invalid number format for Patient ID or Age</h2>");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Patients (p_id, p_name, illness, age) VALUES (?, ?, ?, ?)")) {

            pstmt.setInt(1, pId);
            pstmt.setString(2, name);
            pstmt.setString(3, illness);
            pstmt.setInt(4, age);

            int rowsAffected = pstmt.executeUpdate();
            out.println(rowsAffected > 0 ? "<h2>Patient added successfully!</h2>" : "<h2>Error adding patient</h2>");
        } catch (Exception e) {
            logger.error("Error adding patient", e);
            out.println("<h2>Error adding patient</h2>");
        }
    }

    private void searchPatientsByIllness(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String illness = request.getParameter("illness");

        if (illness == null || illness.trim().isEmpty()) {
        	 logger.warn("illness filed missing");
            out.println("<h2>Error: Please enter an illness</h2>");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Patients WHERE illness = ?")) {

            pstmt.setString(1, illness);
            ResultSet rs = pstmt.executeQuery();

            out.println("<h2>Patients with illness: " + illness + "</h2>");
            out.println("<table border='1'><tr><th>Patient ID</th><th>Name</th><th>Illness</th><th>Age</th></tr>");

            boolean found = false;
            while (rs.next()) {
                found = true;
                out.println("<tr>");
                out.println("<td>" + rs.getInt("p_id") + "</td>");
                out.println("<td>" + rs.getString("p_name") + "</td>");
                out.println("<td>" + rs.getString("illness") + "</td>");
                out.println("<td>" + rs.getInt("age") + "</td>");
                out.println("</tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='4'>No patients found with illness: " + illness + "</td></tr>");
            }

            out.println("</table>");
        } catch (Exception e) {
            logger.error("Error searching patients", e);
            out.println("<h2>Error searching for patients</h2>");
        }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection("jdbc:sqlserver://172.16.51.64:1433;databaseName=241047020;encrypt=false;", "neehara", "Nee@20");
    }
}
