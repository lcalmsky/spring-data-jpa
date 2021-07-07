package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.entity.Member;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

@RequiredArgsConstructor
public class CustomMemberRepositoryImpl implements CustomMemberRepository {
    private final EntityManager entityManager;

    @Override
    public Member findByCustomFactor() {
        return entityManager.createQuery("select m from Member m", Member.class).getSingleResult();
    }
}
