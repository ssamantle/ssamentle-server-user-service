package com.ssafy.ssamentle.entity;

import com.ssafy.ssamentle.entity.enums.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    /** 비밀번호 기반 최소 사용자 정보 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "email", nullable = false, length = 45)
    private String email;

    @Column(name = "password", nullable = false, length = 20)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "profile_url", nullable = false)
    private String imageUrl;

    @Column(name = "status", nullable = false)
    private boolean status; // 사용자 상태 (정상 회원: true, 삭제 요청 회원: false)

    @Column(name = "inactive_date")
    private LocalDateTime inActiveDate; // 비활성화 datetime

    /** 소셜 로그인 정보
    @Column(name = "oauth_type", nullable = false, length = 20)
    private String oauthType;

    @Column(name = "oauth_key", nullable = false, length = 255)
    private String oauthKey;
    */

    // 실명
    // 성별
    // 나이
    // 게임포인트
    // 게임 통계
}
