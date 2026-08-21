package com.marinboy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marinboy.db.DbSchemaService;
import com.marinboy.mapper.ReservationMapper;
import com.marinboy.dto.ReservationDto;
import com.marinboy.dto.UserDto;
import com.marinboy.service.DatabaseVerificationService;
import com.marinboy.service.ReservationService;
import com.marinboy.service.ServiceItemService;
import com.marinboy.util.MoneyFormatUtil;
import com.marinboy.util.PhoneMaskingUtil;
import com.marinboy.security.SecurityConstants;
import com.marinboy.security.jwt.JwtTokenProvider;
import com.marinboy.security.jwt.RedisTokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;

// MyBatis 재설계 프로젝트의 핵심 연결 상태를 검증하는 테스트입니다.
@SpringBootTest(properties = "app.google-calendar.enabled=false")
@AutoConfigureMockMvc
class MarinboyApplicationTests {

    @Autowired
    private DatabaseVerificationService databaseVerificationService;

    @Autowired
    private DbSchemaService dbSchemaService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ReservationService salonReservationService;

    @Autowired
    private ReservationMapper salonReservationDao;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // JWT 검증 테스트가 외부 Redis 실행 여부에 좌우되지 않도록 폐기 조회만 대체합니다.
    @MockBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    void oracleConnectionWorksThroughMyBatisMapper() {
        // TestDao와 test-mapper.xml을 통해 Oracle SYSDATE 조회가 가능한지 검증합니다.
        assertThat(databaseVerificationService.getDatabaseTime().vendor()).isEqualTo("ORACLE");
        assertThat(databaseVerificationService.getDatabaseTime().databaseTime()).isNotBlank();
    }

    @Test
    void projectDatabaseSchemaCanBeReadThroughDbFolder() {
        // db 패키지와 db-schema-mapper.xml을 통해 MB_ 테이블 구조를 읽을 수 있는지 검증합니다.
        assertThat(dbSchemaService.getProjectTables())
                .extracting("tableName")
                .contains("MB_USER", "MB_SERVICE_ITEM", "MB_RESERVATION", "MB_HOLIDAY");

        // 컬럼 조회까지 성공하면 DB 구조 확인 API가 실제 Oracle 메타데이터와 연결된 상태입니다.
        assertThat(dbSchemaService.getProjectColumns())
                .extracting("columnName")
                .contains("ID", "SERVICE_ID", "STATUS", "HOLIDAY_DATE");
    }

    @Test
    void mapperAndServiceCanReadSalonData() {
        // SalonServiceService -> SalonServiceDao -> salon-service-mapper.xml 흐름으로 시술 메뉴를 읽는지 검증합니다.
        assertThat(serviceItemService.getServices())
                .extracting("name")
                .contains("웨이브 펌", "시그니처 컷", "젤 네일 기본", "신부 화장");

        // SalonReservationService -> SalonReservationDao -> salon-reservation-mapper.xml 흐름으로 고객 이력을 읽는지 검증합니다.
        // 이력 시드 데이터는 기본 고객(customer, ID 2)의 연락처로 생성됩니다.
        assertThat(salonReservationService.getCustomerHistory("010-1111-2222"))
                .extracting("serviceName")
                .contains("웨이브 펌");
    }

    @Test
    void controllerEndpointsReturnCustomerScreenData() throws Exception {
        // HomeController가 고객 예약 화면을 렌더링하는지 검증합니다.
        mockMvc.perform(get("/"))
                // 기본 주소는 통합 React v3 고객 화면으로 이동해야 합니다.
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://127.0.0.1:3000"));

        // SalonReservationApiController가 고객 화면에 필요한 시술 데이터를 JSON으로 내려주는지 검증합니다.
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());

        // 날짜/시술 기준 예약 가능 시간 API가 정상 응답하는지 검증합니다.
        mockMvc.perform(get("/api/services/1/available-slots").param("date", "2030-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").isArray());
    }

    @Test
    void securityConfigurationIsLoaded() {
        // security 패키지의 SecurityConfig가 Spring Security 필터 체인으로 등록되었는지 검증합니다.
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void utilityFolderProvidesReusableDisplayHelpers() {
        // util 패키지에서 금액과 연락처 표시를 공통 처리할 수 있는지 검증합니다.
        assertThat(MoneyFormatUtil.toWonText(65000)).isEqualTo("65,000원");
        assertThat(PhoneMaskingUtil.maskMiddle("010-2222-1111")).isEqualTo("010-****-1111");
    }

    @Test
    @Transactional
    void reservationCreateAndUpdateAreConnectedThroughMyBatis() {
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationDto slots = salonReservationService.getAvailableSlots(1L, date);
        while (slots.getAvailableSlots().isEmpty() && date.isBefore(LocalDate.now().plusDays(7))) {
            date = date.plusDays(1);
            slots = salonReservationService.getAvailableSlots(1L, date);
        }
        assertThat(slots.getAvailableSlots()).isNotEmpty();

        String phone = "010-9999-" + String.format("%04d", System.nanoTime() % 10000);
        ReservationDto request = new ReservationDto();
        request.setServiceId(1L);
        request.setCustomerName("연결 검증 고객");
        request.setCustomerEmail("connection-test@marinboy.test");
        request.setCustomerPhone(phone);
        request.setReservationDateTime(slots.getAvailableSlots().get(0));
        request.setNoShowPolicyAgreed(true);
        request.setMemo("생성 연결 검증");
        salonReservationService.createReservation(request);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT PASSWORD FROM MB_USER WHERE PHONE = ?",
                String.class,
                phone)).startsWith("{GUEST}");

        Long customerId = jdbcTemplate.queryForObject(
                "SELECT ID FROM MB_USER WHERE PHONE = ?",
                Long.class,
                phone);
        ReservationDto created = salonReservationDao.findCustomerHistory(phone).get(0);
        assertThat(salonReservationDao.findCustomerReservationsByCustomerId(customerId))
                .extracting(ReservationDto::getId)
                .contains(created.getId());
        assertThat(salonReservationDao.findCustomerReservationByCustomerId(created.getId(), customerId + 999999L))
                .isNull();
        request.setReservationDateTime(slots.getAvailableSlots().get(0));
        request.setMemo("수정 연결 검증");
        request.setNoShowPolicyAgreed(false);
        assertThatThrownBy(() -> salonReservationService.updateCustomerReservation(created.getId(), customerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("노쇼 방지 안내");

        request.setNoShowPolicyAgreed(true);
        salonReservationService.updateCustomerReservation(created.getId(), customerId, request);

        assertThat(salonReservationDao.findCustomerReservationByCustomerId(created.getId(), customerId).getMemo())
                .isEqualTo("수정 연결 검증");

        salonReservationService.cancelCustomerReservation(created.getId(), customerId);
        assertThat(salonReservationDao.findCustomerReservationByCustomerId(created.getId(), customerId).getStatus())
                .isEqualTo("CANCELED");
    }

    /** 회원가입 후 JWT 로그인 토큰이 고객 조회 API까지 이어지는지 검증합니다. */
    @Test
    @Transactional
    void signupLoginAndJwtLookupAreConnected() throws Exception {
        String username = "qa_login_" + System.nanoTime();
        String password = "test-password-2026";
        String signupJson = "{\"username\":\"" + username + "\",\"password\":\"" + password
                + "\",\"name\":\"QA Customer\",\"email\":\"" + username
                + "@example.test\",\"phone\":\"010-7777-7777\"}";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/check-username").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/api/auth/check-email").param("email", username + "@example.test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "_other\",\"password\":\"" + password
                                + "\",\"name\":\"QA Customer\",\"email\":\"" + username
                                + "@example.test\",\"phone\":\"010-8888-8888\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn().getResponse().getContentAsString();

        String accessToken = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(loginResponse).get("accessToken").asText();
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    /** 보호 API는 세션 쿠키가 아니라 Bearer 토큰이 없으면 401을 반환하는지 검증합니다. */
    @Test
    void protectedMutationRequiresBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    /** 관리자 화면의 multipart PATCH 요청이 실제 시술 메뉴를 수정하는지 검증합니다. */
    @Test
    @Transactional
    void adminCanUpdateServiceItemThroughMultipartRequest() throws Exception {
        UserDto admin = new UserDto();
        admin.setId(1L);
        admin.setRole(SecurityConstants.ROLE_ADMIN);
        admin.setUsername("admin-test");
        admin.setName("관리자");
        String accessToken = jwtTokenProvider.createAccessToken(admin);

        mockMvc.perform(multipart("/api/admin/services/{id}", 1L)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("Authorization", "Bearer " + accessToken)
                        .param("name", "웨이브 펌 수정 검증")
                        .param("category", "펌")
                        .param("durationMinutes", "120")
                        .param("price", "95000")
                        .param("description", "관리자 수정 연결 검증"))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT NAME FROM MB_SERVICE_ITEM WHERE ID = ?",
                String.class,
                1L)).isEqualTo("웨이브 펌 수정 검증");
    }

    /** React 개발 서버가 Authorization 헤더로 v3 API를 호출할 수 있는지 검증합니다. */
    @Test
    void reactCorsAllowsBearerAuthAndServiceRequests() throws Exception {
        String reactOrigin = "http://127.0.0.1:3000";

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", reactOrigin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", reactOrigin));

        mockMvc.perform(get("/api/services").header("Origin", reactOrigin))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", reactOrigin));
    }
}
