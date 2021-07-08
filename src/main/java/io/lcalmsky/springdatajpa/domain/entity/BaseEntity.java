package io.lcalmsky.springdatajpa.domain.entity;


import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseEntity {
    @Column(updatable = false)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void before() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
    }

    @PreUpdate
    public void always() {
        updateTime = LocalDateTime.now();
    }
}
