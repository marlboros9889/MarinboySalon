package com.marinboy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marinboy.mapper.ReservationMapper;
import com.marinboy.mapper.ReservationScheduleMapper;
import com.marinboy.dto.ReservationDto;
import com.marinboy.dto.BusinessHourRequestDto;
import com.marinboy.dto.AvailableSlotsResponseDto;
import com.marinboy.dto.ServiceItemDto;
import com.marinboy.dto.SignupRequestDto;
import com.marinboy.dto.UserDto;
import com.marinboy.service.AuthService;
import com.marinboy.service.SocialAccountService;
import com.marinboy.service.ReservationService;
import com.marinboy.service.ReservationScheduleService;
import com.marinboy.service.ServiceItemService;
import com.marinboy.security.SecurityConstants;
import com.marinboy.security.jwt.JwtTokenProvider;
import com.marinboy.security.jwt.RedisTokenBlacklistService;
import com.marinboy.security.oauth.SocialProfile;
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
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

// MyBatis 재설계 프로젝트의 핵심 연결 상태를 검증하는 테스트입니다.
@SpringBootTest(properties = "app.google-calendar.enabled=false")
@AutoConfigureMockMvc
class MarinboyApplicationTests {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ReservationService salonReservationService;

    @Autowired
    private ReservationScheduleService reservationScheduleService;

    @Autowired
    private ReservationMapper salonReservationDao;

    @Autowired
    private ReservationScheduleMapper reservationScheduleMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private SocialAccountService socialAccountService;

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
    void mapperAndServiceCanReadSalonData() {
        // 관리자 변경 뒤에도 유효한 메뉴 행이 DTO로 매핑되는지를 검증해 특정 샘플명에 묶이지 않게 합니다.
        List<ServiceItemDto> services = serviceItemService.getServices();
        assertThat(services).isNotEmpty();
        assertThat(services).allSatisfy(service -> {
            assertThat(service.getId()).isPositive();
            assertThat(service.getName()).isNotBlank();
            assertThat(service.getDurationMinutes()).isPositive();
            assertThat(service.getPrice()).isPositive();
        });
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MB_SERVICE_ITEM", Integer.class))
                .isPositive();
    }

    /** 동일 고객에게 Google과 Naver 계정을 함께 연결해도 고객과 예약 이력이 분리되지 않습니다. */
    @Test
    @Transactional
    void customerCanConnectMultipleSocialProviders() {
        String suffix = String.valueOf(System.nanoTime());
        UserDto customer = new UserDto();
        customer.setUsername("multi_social_" + suffix);
        customer.setPassword("Test-password-2026!");
        customer.setName("다중 소셜 검증 고객");
        customer.setEmail("multi_" + suffix + "@example.test");
        customer.setPhone("010-7000-7000");
        authService.signup(signupRequest(customer));

        UserDto googleUser = socialAccountService.findOrCreate(new SocialProfile(
                "google", "google-" + suffix, customer.getName(), customer.getEmail(), null, true));
        UserDto naverUser = socialAccountService.findOrCreate(new SocialProfile(
                "naver", "naver-" + suffix, customer.getName(), customer.getEmail(), null, true));

        assertThat(googleUser.getId()).isEqualTo(naverUser.getId());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MB_USER_SOCIAL_ACCOUNT WHERE USER_ID = ?",
                Integer.class,
                googleUser.getId())).isEqualTo(2);
    }

    @Test
    void controllerEndpointsReturnCustomerScreenData() throws Exception {
        // HomeController가 고객 예약 화면을 렌더링하는지 검증합니다.
        mockMvc.perform(get("/"))
                // 기본 주소는 통합 Next.js 고객 화면으로 이동해야 합니다.
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
    @Transactional
    void reservationCreateAndUpdateAreConnectedThroughMyBatis() {
        LocalDate date = LocalDate.now().plusDays(1);
        AvailableSlotsResponseDto slots = reservationScheduleService.getAvailableSlots(1L, date);
        while (slots.availableSlots().isEmpty() && date.isBefore(LocalDate.now().plusDays(7))) {
            date = date.plusDays(1);
            slots = reservationScheduleService.getAvailableSlots(1L, date);
        }
        assertThat(slots.availableSlots()).isNotEmpty();

        String suffix = String.valueOf(System.nanoTime());
        String phone = "010-9999-" + suffix.substring(Math.max(0, suffix.length() - 4));
        UserDto customer = new UserDto();
        customer.setUsername("reservation_" + suffix);
        customer.setPassword("Test-password-2026!");
        customer.setName("연결 검증 고객");
        customer.setEmail("reservation_" + suffix + "@example.test");
        customer.setPhone(phone);
        authService.signup(signupRequest(customer));
        Long customerId = jdbcTemplate.queryForObject(
                "SELECT ID FROM MB_USER WHERE USERNAME = ?", Long.class, customer.getUsername());

        ReservationDto request = new ReservationDto();
        request.setServiceId(1L);
        request.setCustomerName("연결 검증 고객");
        request.setCustomerEmail("connection-test@marinboy.test");
        request.setCustomerPhone(phone);
        request.setReservationDateTime(slots.availableSlots().get(0));
        request.setNoShowPolicyAgreed(true);
        request.setMemo("생성 연결 검증");
        salonReservationService.createReservation(request, customerId);

        ReservationDto created = salonReservationDao.findCustomerReservationsByCustomerId(customerId).get(0);
        assertThat(salonReservationDao.findCustomerReservationByCustomerId(created.getId(), customerId + 999999L))
                .isNull();
        request.setReservationDateTime(slots.availableSlots().get(0));
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

    /** 일요일 고정 차단 없이 관리자가 요일 영업 여부를 변경할 수 있는지 검증합니다. */
    @Test
    @Transactional
    void adminBusinessRuleControlsSundayReservationSlots() {
        LocalDate sunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        jdbcTemplate.update("DELETE FROM MB_HOLIDAY WHERE HOLIDAY_DATE = ?", sunday);
        jdbcTemplate.update("DELETE FROM MB_RESERVATION WHERE TRUNC(RESERVATION_DATE_TIME) = ?", sunday);

        BusinessHourRequestDto openRule = new BusinessHourRequestDto();
        openRule.setDayOfWeek(7);
        openRule.setOpen(true);
        openRule.setOpenTime("10:00");
        openRule.setCloseTime("19:00");
        reservationScheduleService.saveBusinessHour(openRule);
        assertThat(reservationScheduleService.getAvailableSlots(2L, sunday).availableSlots()).isNotEmpty();

        BusinessHourRequestDto closedRule = new BusinessHourRequestDto();
        closedRule.setDayOfWeek(7);
        closedRule.setOpen(false);
        closedRule.setOpenTime("10:00");
        closedRule.setCloseTime("19:00");
        reservationScheduleService.saveBusinessHour(closedRule);
        assertThat(reservationScheduleService.getAvailableSlots(2L, sunday).availableSlots()).isEmpty();
    }

    /** ADMIN API가 요일 영업 규칙과 특정 휴무일을 실제 Oracle에 저장·해제하는지 검증합니다. */
    @Test
    @Transactional
    void adminCanManageBusinessHoursAndSpecificHolidays() throws Exception {
        UserDto admin = new UserDto();
        admin.setId(1L);
        admin.setRole(SecurityConstants.ROLE_ADMIN);
        admin.setUsername("admin-business-test");
        admin.setName("영업 규칙 관리자");
        String accessToken = jwtTokenProvider.createAccessToken(admin);

        mockMvc.perform(get("/api/admin/business-hours")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[6].dayOfWeek").value(7));

        mockMvc.perform(put("/api/admin/business-hours/7")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"open\":false,\"openTime\":\"10:00\",\"closeTime\":\"19:00\"}"))
                .andExpect(status().isNoContent());
        assertThat(reservationScheduleMapper.findBusinessHour(7).getOpen()).isFalse();

        LocalDate holidayDate = LocalDate.now().plusDays(5);
        mockMvc.perform(post("/api/admin/holidays")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"holidayDate\":\"" + holidayDate + "\",\"reason\":\"API 검증 휴무\"}"))
                .andExpect(status().isNoContent());
        assertThat(reservationScheduleMapper.countHoliday(holidayDate)).isEqualTo(1);

        mockMvc.perform(delete("/api/admin/holidays")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("holidayDate", holidayDate.toString()))
                .andExpect(status().isNoContent());
        assertThat(reservationScheduleMapper.countHoliday(holidayDate)).isZero();
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

    /** 캘린더 표시 설정은 ADMIN 토큰에만 반환하고 고객 토큰은 403으로 차단합니다. */
    @Test
    void adminCalendarConfigurationIsProtectedByRole() throws Exception {
        UserDto customer = new UserDto();
        customer.setId(2L);
        customer.setUsername("customer");
        customer.setName("고객 사용자");
        customer.setRole(SecurityConstants.ROLE_CUSTOMER);
        String customerToken = jwtTokenProvider.createAccessToken(customer);

        UserDto admin = new UserDto();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setName("원장 관리자");
        admin.setRole(SecurityConstants.ROLE_ADMIN);
        String adminToken = jwtTokenProvider.createAccessToken(admin);

        mockMvc.perform(get("/api/admin/calendar").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/calendar").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false));
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

    /** 관리자 삭제 요청이 예약 이력은 보존하고 활성 메뉴 목록에서만 제외하는지 검증합니다. */
    @Test
    @Transactional
    void adminCanDeleteServiceItemWhilePreservingReservationHistory() throws Exception {
        UserDto admin = new UserDto();
        admin.setId(1L);
        admin.setRole(SecurityConstants.ROLE_ADMIN);
        admin.setUsername("admin-delete-test");
        admin.setName("관리자");
        String accessToken = jwtTokenProvider.createAccessToken(admin);
        Long reservationCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MB_RESERVATION WHERE SERVICE_ID = ?",
                Long.class,
                1L);

        mockMvc.perform(delete("/api/admin/services/{id}", 1L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT CATEGORY FROM MB_SERVICE_ITEM WHERE ID = ?",
                String.class,
                1L)).isEqualTo("DELETED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MB_RESERVATION WHERE SERVICE_ID = ?",
                Long.class,
                1L)).isEqualTo(reservationCountBefore);
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 1)]").isEmpty());
    }

    /** Next.js 서버가 Authorization 헤더로 보호 API를 호출할 수 있는지 검증합니다. */
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

    private SignupRequestDto signupRequest(UserDto user) {
        // 통합 테스트의 사용자 준비 데이터를 실제 회원가입 요청 형태로 변환합니다.
        return new SignupRequestDto(
                user.getUsername(), user.getPassword(), user.getName(), user.getEmail(), user.getPhone());
    }
}
