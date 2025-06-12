package testData;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

public class TokenProvider {
    private static String cachedToken;

    private static final String LOGIN_URL = "https://test.qalan.kz/api/users/login";
    private static final String PUPIL_CODE = "2009662";    // логин
    private static final String PASSWORD = "12345678";     // пароль

    public static String getBearerToken() {
        if (cachedToken == null) {
            cachedToken = fetchTokenFromApi();
        }
        return cachedToken;
    }

    private static String fetchTokenFromApi() {
        JSONObject requestBody = new JSONObject()
                .put("pupilCode", PUPIL_CODE)
                .put("password", PASSWORD);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(requestBody.toString())
                .when()
                .post(LOGIN_URL);

        if (response.statusCode() != 200) {
            throw new RuntimeException("❌ Не удалось получить токен. Статус: " + response.statusCode()
                    + "\nОтвет: " + response.asString());
        }

        String token = new JSONObject(response.asString()).getString("token");
        System.out.println("🔐 Получен токен: " + token.substring(0, 20) + "...");
        return token;
    }
}
