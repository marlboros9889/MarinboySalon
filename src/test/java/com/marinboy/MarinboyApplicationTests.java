package com.marinboy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marinboy.db.DbSchemaService;
import com.marinboy.mapper.ReservationMapper;
import com.marinboy.dto.ReservationDto;
import com.marinboy.service.DatabaseVerificationService;
import com.marinboy.service.ReservationService;
import com.marinboy.service.MenuService;
import com.marinboy.util.MoneyFormatUtil;
import com.marinboy.util.PhoneMaskingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

// MyBatis 재설계 프로젝트의 핵심 연결 상태를 검증하는 테스트입니다.
@SpringBootTest
@AutoConfigureMockMvc
class MarinboyApplicationTests {

    @Autowired
    private DatabaseVerificationService databaseVerificationService;

    @Autowired
    private DbSchemaService dbSchemaService;

    @Autowired
    private MenuService salonServiceService;

    @Autowired
    private ReservationService salonReservationService;

    @Autowired
    private ReservationMapper salonReservationDao;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private MockMvc mockMvc;

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
        assertThat(salonServiceService.getServices())
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
                .andExpect(redirectedUrl("http://127.0.0.1:5173"));

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

        ReservationDto created = salonReservationDao.findCustomerHistory(phone).get(0);
        request.setReservationDateTime(slots.getAvailableSlots().get(0));
        request.setMemo("수정 연결 검증");
        salonReservationService.updateCustomerReservation(created.getId(), phone, request);

        assertThat(salonReservationDao.findCustomerReservation(created.getId(), phone).getMemo())
                .isEqualTo("수정 연결 검증");

        salonReservationService.cancelCustomerReservation(created.getId(), phone);
        assertThat(salonReservationDao.findCustomerReservation(created.getId(), phone).getStatus())
                .isEqualTo("CANCELED");
    }

    /** 회원가입 후 일반 로그인 세션이 고객 조회 API까지 이어지는지 검증합니다. */
    @Test
    @Transactional
    void signupLoginAndSessionLookupAreConnected() throws Exception {
        String username = "qa_login_" + System.nanoTime();
        String password = "test-password-2026";
        String signupJson = "{\"username\":\"" + username + "\",\"password\":\"" + password
                + "\",\"name\":\"QA Customer\",\"email\":\"" + username
                + "@example.test\",\"phone\":\"010-7777-7777\"}";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    /** React 개발 서버가 세션 쿠키를 포함한 인증 API를 호출할 수 있는지 검증합니다. */
    @Test
    void reactCorsAllowsCredentialedAuthAndServiceRequests() throws Exception {
        String reactOrigin = "http://127.0.0.1:5173";

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", reactOrigin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", reactOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mockMvc.perform(get("/api/services").header("Origin", reactOrigin))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", reactOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
