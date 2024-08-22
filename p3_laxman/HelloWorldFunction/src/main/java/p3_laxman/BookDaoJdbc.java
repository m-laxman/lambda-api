package p3_laxman;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mysql.cj.jdbc.MysqlDataSource;

public class BookDaoJdbc implements BookDao {

    static Statement st = null;
    static PreparedStatement pst = null;
    static Connection con = null;
    static MysqlDataSource source = null;
    static String name = System.getenv("username");
    static String pass = System.getenv("password");
    static String dbName = "p3_booksdb_laxman";
    static String url = "jdbc:mysql://p3-booksdb-laxman.c9wkke2mwomq.eu-west-1.rds.amazonaws.com:3306/" + dbName;

    static {
        try {
            source = new MysqlDataSource();
            source.setURL(url);
            source.setPassword(pass);
            source.setUser(name);
            con = source.getConnection();
            st = con.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
            try {
                throw e;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private static final String FIND_BY_BOOK_ID_SQL =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book " +
                    "WHERE book_id = ?";

    private static final String FIND_BY_CATEGORY_ID_SQL =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book " + "WHERE category_id = ?";

    private static final String FIND_RANDOM_BY_CATEGORY_ID_SQL =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book " +
                    "WHERE category_id = ? " +
                    "ORDER BY RAND() " +
                    "LIMIT ?";

    private static final String FIND_RANDOM_BY_CATEGORY_NAME_SQL =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book join category on book.category_id = category.category_id " +
                    "WHERE category.name = ? " +
                    "ORDER BY RAND() " +
                    "LIMIT ?";

    private static final String FIND_BY_CATEGORY_NAME_SQL =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book join category on book.category_id = category.category_id " +
                    "WHERE category.name = ? ";

    private static final String FIND_RANDOM_BOOK =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book " +
                    "ORDER BY RAND() " +
                    "LIMIT=5";

    private static final String FIND_ALL_BOOKS =
            "SELECT book.book_id, book.title, book.author, book.description,book.price, book.rating,book.is_public, book.is_featured,book.category_id " +
                    "FROM book ";

    private static final String ADD_BOOK_SQL =
            "INSERT INTO book (book_id, title, author, description, price, rating, is_public, is_featured, category_id) VALUES (?,?,?,?,?,?,?,?,?)";


    @Override
    public Optional<Book> findByBookId(long bookId) {
        Book book = null;
        try (
                PreparedStatement statement = con.prepareStatement(FIND_BY_BOOK_ID_SQL)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return (Optional.of(readBook(resultSet)));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findByCategoryId(long categoryId) {
        List<Book> books = new ArrayList<>();
        try (
                PreparedStatement statement = con.prepareStatement(FIND_BY_CATEGORY_ID_SQL)) {
            statement.setLong(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(readBook(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return books;
    }

    @Override
    public List<Book> findByCategoryName(String categoryName) {
        List<Book> books = new ArrayList<>();
        try (
                PreparedStatement statement = con.prepareStatement(FIND_BY_CATEGORY_NAME_SQL)) {
            statement.setString(1, categoryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(readBook(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return books;
    }

    @Override
    public List<Book> findRandomByCategoryId(long categoryId, int limit) {
        List<Book> books = new ArrayList<>();
        try (
                PreparedStatement statement = con.prepareStatement(FIND_RANDOM_BY_CATEGORY_ID_SQL)) {
            statement.setLong(1, categoryId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(readBook(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return books;
    }

    @Override
    public List<Book> findRandomByCategoryName(String categoryName, int limit) {
        List<Book> books = new ArrayList<>();
        try (
                PreparedStatement statement = con.prepareStatement(FIND_RANDOM_BY_CATEGORY_NAME_SQL)) {
            statement.setString(1, categoryName);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(readBook(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return books;
    }

    public List<Book> findRandomBook() {  // Notice the method name and return type change
        List<Book> randomBooks = new ArrayList<>();
        String query = "SELECT book_id, title, author, description, price, rating, is_public, is_featured, category_id FROM book ORDER BY RAND() LIMIT 5";  // Changed LIMIT to 5
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {  // Changed from if to while to handle multiple books
                randomBooks.add(new Book(
                        rs.getLong("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getInt("rating"),
                        rs.getBoolean("is_public"),
                        rs.getBoolean("is_featured"),
                        rs.getLong("category_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return randomBooks;  // Return the list of books
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(FIND_ALL_BOOKS)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(readBook(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return books;

    }

    ;

    @Override
    public void addBook(long bookId, String title, String author, String description, double price, int rating, boolean isPublic, boolean isFeatured, long categoryId) {
        try (PreparedStatement statement = con.prepareStatement(ADD_BOOK_SQL)) {


            statement.setLong(1, bookId);
            statement.setString(2, title);
            statement.setString(3, author);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setInt(6, rating);
            statement.setBoolean(7, isPublic);
            statement.setBoolean(8, isFeatured);
            statement.setLong(9, categoryId);

            int result = statement.executeUpdate();
            //con.commit();
            System.out.println("Rows affected: " + result); // Check how many rows were actually inserted
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            e.printStackTrace(); // This will help you see the full stack trace
        }
    }


    private Book readBook(ResultSet resultSet) throws SQLException {
        // TODO add description, isFeatured, rating to Book results
        long bookId = resultSet.getLong("book_id");
        String title = resultSet.getString("title");
        String author = resultSet.getString("author");
        String description = resultSet.getString("description");
        int price = resultSet.getInt("price");
        int rating = resultSet.getInt("rating");
        boolean isPublic = resultSet.getBoolean("is_public");
        boolean isFeatured = resultSet.getBoolean("is_featured");
        long categoryId = resultSet.getLong("category_id");
        return new Book(bookId, title, author, description, price, rating, isPublic, isFeatured, categoryId);
    }
}
