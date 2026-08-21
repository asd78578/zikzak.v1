package com.example.zikzak.controller;

import com.example.zikzak.dto.*;
import com.example.zikzak.service.AccountService;
import com.example.zikzak.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication",
        description = "Регистрация, авторизация и управление токенами"
)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AccountService accountService;
    private final AuthService authService;

    public AuthController(
            AccountService accountService,
            AuthService authService
    ) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт новый аккаунт с ролью USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Аккаунт создан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email уже зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AccountResponse response =
                accountService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Вход в аккаунт",
            description = "Возвращает access-токен и refresh-токен"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Вход выполнен"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный email или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Текущий пользователь",
            description = "Возвращает аккаунт владельца access-токена"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные пользователя"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT отсутствует или недействителен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Аккаунт не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> me(
            Authentication authentication
    ) {
        Long accountId =
                (Long) authentication.getPrincipal();

        AccountResponse response =
                accountService.getById(accountId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Обновление токенов",
            description = """
                    Отзывает старый refresh-токен
                    и возвращает новую пару токенов
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены обновлены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh-токен не передан",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            Refresh-токен недействителен,
                            просрочен или отозван
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response =
                authService.refresh(request.refreshToken());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Выход из аккаунта",
            description = "Отзывает переданный refresh-токен"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Выход выполнен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh-токен не передан",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.refreshToken());

        return ResponseEntity.noContent().build();
    }
}