package com.virtualtryon.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 *
 * Lombok 미사용: VS Code/Cursor 환경에서 Lombok 플러그인 호환 이슈로 수동 구현.
 * Lombok 사용 시 {@code @Getter @Setter @NoArgsConstructor}로 아래 Getter/Setter 대체 가능.
 */
public class RegisterRequest {
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
    
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    public RegisterRequest() {}

    /** Lombok @Getter @Setter 대체 */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
