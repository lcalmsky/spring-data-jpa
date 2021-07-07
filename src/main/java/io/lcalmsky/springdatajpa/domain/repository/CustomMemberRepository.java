package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.entity.Member;

public interface CustomMemberRepository {
    Member findByCustomFactor();
}
