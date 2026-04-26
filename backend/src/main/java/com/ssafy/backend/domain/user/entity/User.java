package com.ssafy.backend.domain.user.entity;

import com.ssafy.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "sub", nullable = false, length = 256)
    private String sub;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "profile_url", nullable = false, length = 256)
    private String profileUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(
        String email,
        String provider,
        String sub,
        String name,
        String profileUrl,
        LocalDateTime deletedAt
    ) {
        this.email = email;
        this.provider = provider;
        this.sub = sub;
        this.name = name;
        this.profileUrl = profileUrl;
        this.deletedAt = deletedAt;
    }

    public void updateProfile(String email, String name, String profileUrl) {
        this.email = email;
        this.name = name;
        this.profileUrl = profileUrl;
    }
}
