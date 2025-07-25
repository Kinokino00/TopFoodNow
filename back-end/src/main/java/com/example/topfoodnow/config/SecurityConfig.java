package com.example.topfoodnow.config;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.filter.JwtRequestFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Optional<UserModel> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty() || !userOptional.get().getEnabled()) {
                throw new UsernameNotFoundException("用戶未找到或未啟用: " + email);
            }
            UserModel user = userOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.getEnabled(),
                    true, // 帳戶是否未過期
                    true, // 憑證是否未過期
                    true, // 帳戶是否未鎖定
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()))
            );
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // 允許 Swagger UI 相關的公共訪問
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        // 允許身份驗證相關的 API (註冊、登入、忘記密碼等)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 允許推薦相關的公開查詢
                        .requestMatchers("/api/recommend/random", "/api/recommend/all", "/api/recommend/{userId}/{storeId}").permitAll()

                        // **** 新增針對 /api/categories 的權限規則 ****
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll() // 允許所有用戶查詢分類
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN") // 創建分類需要 ADMIN 角色
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")  // 更新分類需要 ADMIN 角色
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN") // 刪除分類需要 ADMIN 角色
                        // **** 結束新增 ****

                        // 其他所有未明確指定權限的請求都需要認證
                        .anyRequest().authenticated()
                )
                // 配置會話管理為無狀態 (適用於 JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 添加 JWT 過濾器在 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 確保允許的源與您的前端應用保持一致
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允許所有頭部，包括 Authorization
        configuration.setAllowCredentials(true); // 允許發送憑證 (例如 Cookies, Authorization Headers)
        configuration.setMaxAge(3600L); // 預檢請求的緩存時間

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 應用於所有路徑
        return source;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}