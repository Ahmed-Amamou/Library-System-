/*
 * Copyright (c) 2024. made by Ahmed AMAMOU.
 */

package com.example.bibliotheque_project.DAO;
import com.example.bibliotheque_project.Models.Book;
import com.example.bibliotheque_project.Connections.MySQLConnection;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MySQLBookDAO implements BookDAO{
    private Connection connection = MySQLConnection.getConnection();
    @Override
    public Book findBookById(int id) {
        Book book = null;
        String query = "SELECT * FROM book WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String ISBN = resultSet.getString("ISBN");
                int copies = resultSet.getInt("copies_available");
                book = new Book(id, author, title,  ISBN, copies);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }
    @Override
    public Book findBookByISBN(String ISBN) {
        Book book = null;
        String query = "SELECT * FROM book WHERE ISBN = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, ISBN);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                int copies = resultSet.getInt("copies_available");
                book = new Book(id, author, title,  ISBN, copies);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }
    public void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @Override
    public boolean bookExists(String isbn) {
        String query = "SELECT COUNT(*) FROM book WHERE ISBN = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isbn);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Book> findAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM book";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String ISBN = resultSet.getString("ISBN");
                int copies = resultSet.getInt("copies_available");
                books.add(new Book(id, title, author, ISBN, copies));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;

    }

    @Override
    public void insertBook(Book book) {
        // if book already exists, increase the number of copies
        // else insert the book
        //Document that I made this choice because I don't want to have two books with the same id
        //and when we insert a book we don't know if it exists or not
        //Q:argument that id and isbn here are the same ?
        //A:two books with the same title and author don't always have the same ISBN
        Book book1 = findBookByISBN(book.getISBN());
        if(book1 != null){
            increaseCopies(book.getISBN(), book.getCopiesAvailable());
        }else{
            String query = "INSERT INTO book (id, title, author, ISBN, copies_available) VALUES (?, ?, ?, ?, ?)";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setInt(1, book.getId());
                preparedStatement.setString(2, book.getTitle());
                preparedStatement.setString(3, book.getAuthor());
                preparedStatement.setString(4, book.getISBN());
                preparedStatement.setInt(5, book.getCopiesAvailable());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateBook(Book book) {
        String query = "UPDATE book SET title = ?, author = ?, ISBN = ?, copies_available = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setString(3, book.getISBN());
            preparedStatement.setInt(4, book.getCopiesAvailable());
            preparedStatement.setInt(5, book.getId());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @Override
    public int getBookCount() {
        String query = "SELECT COUNT(*) FROM book";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if an error occurs
    }
    @Override
    public boolean hasBorrowedBook(int readerId, String bookISBN) {
        String query = "SELECT * FROM transactions WHERE reader_id = ? AND book_ISBN = ? AND return_date IS NULL";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, readerId);
            preparedStatement.setString(2, bookISBN);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Returns true if there's at least one row (book is borrowed)
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as needed
            return false; // Assume an error, change this logic based on your error handling approach
        }
    }

    @Override
    public void deleteBook(int id) {

        String query = "DELETE FROM book WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Map<String, String> getAllBookTitlesAndISBNs() {
        Map<String, String> bookTitlesAndISBNs = new HashMap<>();
        bookTitlesAndISBNs.put("ISBN", "title");
        String query = "SELECT title, ISBN FROM book";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String ISBN = resultSet.getString("ISBN");
                bookTitlesAndISBNs.put(ISBN, title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
        return bookTitlesAndISBNs;
    }

    @Override
    public void increaseCopies(String bookISBN, int count) {
        String query = "UPDATE Book SET copies_available = copies_available + ? WHERE isbn = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, count);
            statement.setString(2, bookISBN);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception
        }
    }

    @Override
    public void decreaseCopies(String bookISBN, int count) {
        String query = "UPDATE Book SET copies_available = GREATEST(copies_available - ?, 0) WHERE isbn = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, count);
            statement.setString(2, bookISBN);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception
        }
    }

}
