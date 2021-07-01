package nextstep.subway.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.acceptance.AuthAcceptanceTest;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.auth.infrastructure.AuthorizationExtractor;
import nextstep.subway.member.dto.MemberRequest;
import nextstep.subway.member.dto.MemberResponse;

public class MemberAcceptanceTest extends AcceptanceTest {
    public static final String EMAIL = "email@email.com";
    public static final String PASSWORD = "password";
    public static final String NEW_EMAIL = "newemail@email.com";
    public static final String NEW_PASSWORD = "newpassword";
    public static final int AGE = 20;
    public static final int NEW_AGE = 21;

    @DisplayName("회원 정보를 관리한다.")
    @Test
    void manageMember() {
        // when
        ExtractableResponse<Response> createResponse = 회원_생성을_요청(EMAIL, PASSWORD, AGE);
        // then
        회원_생성됨(createResponse);

        // when
        ExtractableResponse<Response> findResponse = 회원_정보_조회_요청(createResponse);
        // then
        회원_정보_조회됨(findResponse, EMAIL, AGE);

        // when
        ExtractableResponse<Response> updateResponse = 회원_정보_수정_요청(createResponse, NEW_EMAIL, NEW_PASSWORD, NEW_AGE);
        // then
        회원_정보_수정됨(updateResponse);

        // when
        ExtractableResponse<Response> deleteResponse = 회원_삭제_요청(createResponse);
        // then
        회원_삭제됨(deleteResponse);
    }

    /**
     * Feature: 토큰 인증을 통해 고객정보 수정
     *
     * Scenario: 토큰으로 본인의 정보를 조회하여 수정하고 삭제한다
     * Given 회원 등록되어 있음
     * And 로그인 되어 있음
     * When 로그인 사용자 정보조회 요청
     * Then 로그인 사용자 정보 조회됨
     * When 로그인 사용자 정보수정 요청
     * Then 로그인 사용자 정보 수정됨
     * When 로그인 사용자 정보삭제 요청
     * Then 로그인 사용자 정보 삭제됨
     */
    @DisplayName("나의 정보를 관리한다.")
    @TestFactory
    List<DynamicTest> manageMyInfo() {
        // given
        MemberRequest memberRequest = new MemberRequest(EMAIL, PASSWORD, AGE);
        회원_등록되어_있음(memberRequest);
        String accessToken = AuthAcceptanceTest.로그인_되어_있음(new TokenRequest(memberRequest.getEmail(), memberRequest.getPassword()))
                .as(TokenResponse.class)
                .getAccessToken();

        return Arrays.asList(
                dynamicTest("로그인 사용자 정보조회", () -> {
                    // when
                    ExtractableResponse<Response> 로그인_사용자_정보조회_결과 = 로그인_사용자_정보조회_요청(accessToken);

                    // then
                    로그인_사용자_정보_조회됨(로그인_사용자_정보조회_결과);
                }),
                dynamicTest("로그인 사용자 정보수정", () -> {
                    // when
                    ExtractableResponse<Response> 로그인_사용자_정보수정_결과 = 로그인_사용자_정보수정_요청(accessToken, new MemberRequest(memberRequest.getEmail(), MemberAcceptanceTest.NEW_PASSWORD, NEW_AGE));

                    // then
                    로그인_사용자_정보_수정됨(로그인_사용자_정보수정_결과);
                }),
                dynamicTest("로그인 사용자 정보삭제", () -> {
                    // when
                    ExtractableResponse<Response> 로그인_사용자_정보삭제_결과 = 로그인_사용자_정보삭제_요청(accessToken);

                    // then
                    로그인_사용자_정보_삭제됨(로그인_사용자_정보삭제_결과);
                })
        );
    }

    private ExtractableResponse<Response> 로그인_사용자_정보조회_요청(String accessToken) {
        String headerValue = AuthorizationExtractor.BEARER_TYPE + " " + accessToken;
        return RestAssured.given().log().all()
                .header(AuthorizationExtractor.AUTHORIZATION, headerValue)
                .when()
                .get("/members/me")
                .then().log().all()
                .extract();
    }

    private void 로그인_사용자_정보_조회됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private ExtractableResponse<Response> 로그인_사용자_정보수정_요청(String accessToken, MemberRequest memberRequest) {
        String headerValue = AuthorizationExtractor.BEARER_TYPE + " " + accessToken;
        return RestAssured.given().log().all()
                .header(AuthorizationExtractor.AUTHORIZATION, headerValue)
                .when()
                .body(memberRequest)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .put("/members/me")
                .then().log().all()
                .extract();
    }

    private void 로그인_사용자_정보_수정됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private ExtractableResponse<Response> 로그인_사용자_정보삭제_요청(String accessToken) {
        String headerValue = AuthorizationExtractor.BEARER_TYPE + " " + accessToken;
        return RestAssured.given().log().all()
                .header(AuthorizationExtractor.AUTHORIZATION, headerValue)
                .when()
                .delete("/members/me")
                .then().log().all()
                .extract();
    }

    private void 로그인_사용자_정보_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static ExtractableResponse<Response> 회원_등록되어_있음(MemberRequest memberRequest) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_생성을_요청(String email, String password, Integer age) {
        MemberRequest memberRequest = new MemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_정보_조회_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get(uri)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_정보_수정_요청(ExtractableResponse<Response> response, String email, String password, Integer age) {
        String uri = response.header("Location");
        MemberRequest memberRequest = new MemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().put(uri)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_삭제_요청(ExtractableResponse<Response> response) {
        String uri = response.header("Location");
        return RestAssured
                .given().log().all()
                .when().delete(uri)
                .then().log().all()
                .extract();
    }

    public static void 회원_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    public static void 회원_정보_조회됨(ExtractableResponse<Response> response, String email, int age) {
        MemberResponse memberResponse = response.as(MemberResponse.class);
        assertThat(memberResponse.getId()).isNotNull();
        assertThat(memberResponse.getEmail()).isEqualTo(email);
        assertThat(memberResponse.getAge()).isEqualTo(age);
    }

    public static void 회원_정보_수정됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 회원_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
