package com.youngtechcr.www.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.youngtechcr.www.api.ApiProperties;
import com.youngtechcr.www.http.HttpUtils;
import com.youngtechcr.www.security.eidte.EidteAuthenticationConverter;
import com.youngtechcr.www.security.eidte.EidteAuthenticationProvider;
import com.youngtechcr.www.security.eidte.EidteParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final ApiProperties apiProperties;

    public SecurityConfig(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authzServerSecurityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<?> tokenGenerator
    ) throws Exception {
//       OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

//       http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        authorizationServerConfigurer
                .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
                .tokenEndpoint( tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverters( converters -> {
                                    converters.add(new EidteAuthenticationConverter());
                                })
                                .authenticationProviders( providers -> {
                                   providers.add(new EidteAuthenticationProvider(
                                           authorizationService, tokenGenerator
                                   ));
                                })
                );
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        http
                .securityMatcher(endpointsMatcher)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf( csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
//                 Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults())
                )
                .apply(authorizationServerConfigurer)
        ;

        return http.build();
    }

    @Bean
//    @Order
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
//                .securityMatcher("/api/**")
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(HttpMethod.GET,"/products").authenticated()
                                .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 ->  oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        TokenSettings tokenSettings = TokenSettings
                .builder()
                .refreshTokenTimeToLive(Duration.ofMinutes(30))
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .build();
        RegisteredClient webClient = RegisteredClient
                .withId("1")
                .clientId("web-frontend")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethods( methods -> {
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                })
                .authorizationGrantTypes( grantTypes -> {
//                    grantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                    grantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
                    grantTypes.add(EidteParameters.GRANT_TYPE_INSTANCE);
                })
                .tokenSettings(tokenSettings)
                .build();

        return new InMemoryRegisteredClientRepository(webClient);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(Arrays.asList(this.apiProperties.webClient()));
        corsConfiguration.addAllowedOrigin(this.apiProperties.webClient());
        corsConfiguration.setAllowedMethods(HttpUtils.getAllHttpMethods());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

//    @Bean
//    public JWKSource<SecurityContext> jwkSource() {
//        KeyPair keyPair = new KeyPair(
//                this.apiProperties.publicKey(),
//                this.apiProperties.privateKey()
//        );
//        logger.trace("Succesdfully created keypair -> " + keyPair);
//        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//        RSAKey rsaKey = new RSAKey.Builder(publicKey)
//                .privateKey(privateKey)
//                .keyID("1")
//                .build();
//        JWKSet jwkSet = new JWKSet(rsaKey);
//        return new ImmutableJWKSet<>(jwkSet);
//    }

    @Bean
     private static KeyPair generateRsaKey() {
                  KeyPair keyPair;
                  try {
                      KeyPairGenerator keyPairGenerator =
                              KeyPairGenerator.getInstance("RSA");
                      keyPairGenerator.initialize(2048);
                      keyPair = keyPairGenerator.generateKeyPair();
                  }
                  catch (Exception ex) {
                      throw new IllegalStateException(ex);
                  }
                  return keyPair;
    }

     @Bean
     public JWKSource<SecurityContext> jwkSource() {
     KeyPair keyPair = generateRsaKey();

     RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
     RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//     RSAPublicKey publicKey = this.apiProperties.publicKey();
//     RSAPrivateKey privateKey = this.apiProperties.privateKey();

     RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
     JWKSet jwkSet = new JWKSet(rsaKey);
     return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /* TODO: Fix this...
        Fore some reason, whenever I registers the PasswordEncoder bean I can't authenticate
        OAuth2.0/OIDC/EIDTE clients with {noop} credentials, meaning that the next operations
        can't be accomplished:
            - IEDTE Exchange
            - Any OAuth2.0/OIDC flows that requiere client authentication with client_secret
        UPDATE: Reason why this happens is because ClientSecretAuthenticationProvider (which
        is encharged of authenticating clients before reaching any AuthenticationProviders that
        actually generate tokens and particiapate in authz/authn flows) tries to match provided
        client_secret with the equivalent from the RegisteredClient (which is stored probably in
        DB or Cache (altough it can be simply stored in memory)
    */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }

}
