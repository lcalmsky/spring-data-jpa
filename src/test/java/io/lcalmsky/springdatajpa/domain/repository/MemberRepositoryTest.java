package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("MemberRepository 동작 테스트")
    public void test() {
        // given
        Member member = new Member("홍길동", 20, null);
        // when
        memberRepository.save(member);
        // then
        Member foundMember = memberRepository.findById(member.getId()).orElse(null);
        assertNotNull(foundMember);
        assertEquals(foundMember.getUsername(), member.getUsername());
    }
}