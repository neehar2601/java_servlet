package msis.library;

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

@WebServlet("/LibraryServlet")
public class LibraryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(LibraryServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("addBook".equals(action)) {
            addBook(request, response);
        } else {
            searchBook(request, response);
        }
    }

    private void searchBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        String bookIdStr = request.getParameter("bookId");
        logger.info("Received input for book ID: {}", bookIdStr);

        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            out.println("<h2>Error: Book ID is required</h2>");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid Book ID format: {}", bookIdStr, e);
            out.println("<h2>Error: Invalid Book ID format</h2>");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Books1 WHERE book_id = ?")) {

            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                out.println("<h2>Book Details:</h2>");
                out.println("<p>Book ID: " + rs.getInt("book_id") + "</p>");
                out.println("<p>Title: " + rs.getString("title") + "</p>");
                out.println("<p>Author: " + rs.getString("author") + "</p>");
                out.println("<p>Genre: " + rs.getString("genre") + "</p>");
            } else {
                out.println("<h2>No Book Found with ID: " + bookId + "</h2>");
            }
        } catch (Exception e) {
            logger.error("Error fetching book details", e);
            out.println("<h2>Error fetching book details</h2>");
        }
    }

    private void addBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String bookIdStr = request.getParameter("bookId");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String genre = request.getParameter("genre");

        if (bookIdStr == null || bookIdStr.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            author == null || author.trim().isEmpty() ||
            genre == null || genre.trim().isEmpty()) {
            out.println("<h2>Error: All fields are required</h2>");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr);
        } catch (NumberFormatException e) {
            out.println("<h2>Error: Invalid number format for book ID</h2>");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Books1 (book_id, title, author, genre) VALUES (?, ?, ?, ?)")) {

            pstmt.setInt(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, genre);

            int rowsAffected = pstmt.executeUpdate();
            out.println(rowsAffected > 0 ? "<h2>Book added successfully!</h2>" : "<h2>Error adding book</h2>");
        } catch (Exception e) {
            logger.error("Error adding book", e);
            out.println("<h2>Error adding book</h2>");
        }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection("jdbc:sqlserver://172.16.51.64:1433;databaseName=241047020;encrypt=false;", "neehara", "Nee@20");
    }
}
