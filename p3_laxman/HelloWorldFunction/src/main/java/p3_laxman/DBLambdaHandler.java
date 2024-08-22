package p3_laxman;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.json.JSONObject;


public class DBLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CategoryDaoJdbc categoryDb = new CategoryDaoJdbc();
    private final BookDaoJdbc bookDb = new BookDaoJdbc();
    private Gson gson = new Gson();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent req, Context context) {

        Map<String, String> CORS = Map.of("access-control-allow-origin", "*");
        String path = req.getPath();

        switch (path) {
            case "/api/getAllCategory":
                System.out.println("Enter " + path);
                return getAllCategories(CORS);
            case "/api/getCategoryName":
                System.out.println("Enter " + path);
                return getCategoryName(req, CORS);
            case "/api/getCategoryId":
                System.out.println("Enter " + path);
                return getCategoryId(req, CORS);
            case "/api/getAllBook":
                System.out.println("Enter " + path);
                return getAllBook(CORS);
            case "/api/getBookById":
                System.out.println("Enter " + path);
                return getBookById(req, CORS);
            case "/api/getBookByCategoryId":
                System.out.println("Enter " + path);
                return getBookByCategoryId(req, CORS);
            case "/api/getBookByCategoryName":
                System.out.println("Enter " + path);
                return getBookByCategoryName(req, CORS);
            case "/api/getRandomBook":
                System.out.println("Enter " + path);
                return getRandomBooks(CORS);
            case "/api/addCategory":
                System.out.println("Enter " + path);
                return addCategory(req, CORS);
            case "/api/addBook":
                System.out.println("Enter " + path);
                return addNewBook(req, CORS);

            default:
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withBody("Invalid request")
                        .withHeaders(CORS);
        }
    }


    private APIGatewayProxyResponseEvent getAllCategories(Map<String, String> CORS) {
        System.out.println("In getAllCategories function");
        List<Category> categories = categoryDb.findAll();
        String json = gson.toJson(categories);
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent getCategoryName(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        System.out.println("In getCategoryName");
        Map<String, String> queryParams = req.getQueryStringParameters();
        System.out.println("Printing QueryParams..." + queryParams);
        if (queryParams == null || !queryParams.containsKey("categoryId")) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"categoryId query parameter is missing\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        long categoryId;
        try {
            categoryId = Long.parseLong(queryParams.get("categoryId"));
        } catch (NumberFormatException e) {
            System.out.println(e);
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Invalid categoryId format\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        Category category = categoryDb.findByCategoryId(categoryId);
        if (category == null) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Category not found\"}")
                    .withHeaders(CORS)
                    .withStatusCode(404);
        }
        String json = gson.toJson(Map.of("name", category.name()));
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent getCategoryId(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        System.out.println("In getCategoryId");
        Map<String, String> queryParams = req.getQueryStringParameters();
        if (queryParams == null || !queryParams.containsKey("categoryName")) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Category name query parameter is missing\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        String categoryName = queryParams.get("categoryName");
        Category category = categoryDb.findByName(categoryName);
        if (category == null) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Category not found\"}")
                    .withHeaders(CORS)
                    .withStatusCode(404);
        }

        String json = gson.toJson(Map.of("categoryId", category.categoryId()));
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent addCategory(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        System.out.println("In addCategory");
        String addedCategory = req.getBody();
        JSONObject obj = new JSONObject(addedCategory);
        String categoryName = obj.getString("categoryName");
        long categoryId = obj.getLong("categoryId");

        Category newCategory = new Category(categoryId, categoryName);
        categoryDb.addCategory(newCategory.categoryId(), newCategory.name());

        String json = gson.toJson(Map.of("message", "Category added: " + newCategory.name()));
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(201);
    }

    private APIGatewayProxyResponseEvent getAllBook(Map<String, String> CORS) {
        System.out.println("In getAllBook");
        List<Book> books = bookDb.findAll();
        String json = gson.toJson(books);
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent addNewBook(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        System.out.println("In addNewBook");
        String addedBook = req.getBody();
        JSONObject obj = new JSONObject(addedBook);

        Book newBook = new Book(obj.getLong("bookId"),
                obj.getString("title"),
                obj.getString("author"),
                obj.getString("description"),
                obj.getInt("price"),
                obj.getInt("rating"),
                obj.getBoolean("isPublic"),
                obj.getBoolean("isFeatured"),
                obj.getLong("categoryId"));

        bookDb.addBook(newBook.bookId(),
                newBook.title(),
                newBook.author(),
                newBook.description(),
                newBook.price(),
                newBook.rating(),
                newBook.isPublic(),
                newBook.isFeatured(),
                newBook.categoryId());


        String json = gson.toJson(Map.of("message", "Book added: " + newBook.title()));
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent getBookById(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        long bookId;
        System.out.println("In getBookById");
        Map<String, String> queryParams = req.getQueryStringParameters();
        if (queryParams == null || !queryParams.containsKey("bookId")) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"bookId query parameter is missing\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        try {
            bookId = Long.parseLong(queryParams.get("bookId"));
        } catch (NumberFormatException e) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Invalid bookId format\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        return bookDb.findByBookId(bookId)
                .map(book -> {
                    String json = gson.toJson(book);
                    return new APIGatewayProxyResponseEvent()
                            .withBody(json)
                            .withHeaders(CORS)
                            .withStatusCode(200);
                })
                .orElseGet(() -> new APIGatewayProxyResponseEvent()
                        .withBody("{\"message\":\"Book not found\"}")
                        .withHeaders(CORS)
                        .withStatusCode(404));
    }

    private APIGatewayProxyResponseEvent getBookByCategoryId(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        long categoryId;
        System.out.println("In getBookByCategoryId");
        Map<String, String> queryParams = req.getQueryStringParameters();
        if (queryParams == null || !queryParams.containsKey("categoryId")) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"categoryId query parameter is missing\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        try {
            categoryId = Long.parseLong(queryParams.get("categoryId"));
        } catch (NumberFormatException e) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"Invalid categoryId format\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        List<Book> books = bookDb.findByCategoryId(categoryId);
        if (books == null || books.isEmpty()) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"No books found for this category\"}")
                    .withHeaders(CORS)
                    .withStatusCode(404);
        }

        String json = gson.toJson(books);
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent getBookByCategoryName(APIGatewayProxyRequestEvent req, Map<String, String> CORS) {
        System.out.println("In getBookByCategoryName");
        Map<String, String> queryParams = req.getQueryStringParameters();
        if (queryParams == null || !queryParams.containsKey("categoryName")) {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"categoryName query parameter is missing\"}")
                    .withHeaders(CORS)
                    .withStatusCode(400);
        }

        String categoryName = queryParams.get("categoryName");
        List<Book> books = bookDb.findByCategoryName(categoryName);
        String json = gson.toJson(books);
        return new APIGatewayProxyResponseEvent()
                .withBody(json)
                .withHeaders(CORS)
                .withStatusCode(200);
    }

    private APIGatewayProxyResponseEvent getRandomBooks(Map<String, String> CORS) {
        System.out.println("In getRandomBooks");
        List<Book> books = bookDb.findRandomBook();
        if (!books.isEmpty()) {
            String json = gson.toJson(books);
            return new APIGatewayProxyResponseEvent()
                    .withBody(json)
                    .withHeaders(CORS)
                    .withStatusCode(200);
        } else {
            return new APIGatewayProxyResponseEvent()
                    .withBody("{\"message\":\"No books available\"}")
                    .withHeaders(CORS)
                    .withStatusCode(404);
        }
    }
}