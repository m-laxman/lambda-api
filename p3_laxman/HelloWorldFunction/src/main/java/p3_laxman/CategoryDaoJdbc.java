package p3_laxman;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDaoJdbc implements CategoryDao {

    private static final String FIND_ALL_SQL =
            "SELECT category_id, name " +
                    "FROM category";

    private static final String FIND_BY_CATEGORY_ID_SQL =
            "SELECT category_id, name " +
                    "FROM category " +
                    "WHERE category_id = ?";

    private static final String FIND_BY_NAME_SQL =
            "SELECT category_id, name " +
                    "FROM category " +
                    "WHERE name = ?";

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


    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (
                PreparedStatement statement = con.prepareStatement(FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Category category = readCategory(resultSet);
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return categories;
    }

    public Category findByCategoryId(long categoryId) {
        Category category = null;
        try (
                PreparedStatement statement = con.prepareStatement(FIND_BY_CATEGORY_ID_SQL)) {
            statement.setLong(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    category = readCategory(resultSet);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return category;
    }

    public Category findByName(String name) {
        Category category = null;
        try (
                PreparedStatement statement = con.prepareStatement(FIND_BY_NAME_SQL)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    category = readCategory(resultSet);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return category;

    }

    public Category addCategory(long categoryId, String name) {
        String insertSQL = "INSERT INTO category (category_id, name) VALUES (?, ?)";
        try (PreparedStatement statement = con.prepareStatement(insertSQL)) {
            statement.setLong(1, categoryId);
            statement.setString(2, name);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 1) {
                return new Category(categoryId, name);
            } else {
                throw new SQLException("Failed to add category");
            }
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Category readCategory(ResultSet resultSet) throws SQLException {
        long categoryId = resultSet.getLong("category_id");
        String name = resultSet.getString("name");
        return new Category(categoryId, name);
    }

}
