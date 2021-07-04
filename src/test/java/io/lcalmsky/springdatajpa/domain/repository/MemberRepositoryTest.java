package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.dto.MemberDto;
import io.lcalmsky.springdatajpa.domain.entity.Member;
import io.lcalmsky.springdatajpa.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {


    @Test
    @DisplayName("MemberRepository 동작 테스트")
    void test() {
        // given
        Member member = new Member("홍길동", 20, null);
        // when
        memberRepository.save(member);
        // then
        Member foundMember = memberRepository.findById(member.getId()).orElse(null);
        assertNotNull(foundMember);
        assertEquals(foundMember.getUsername(), member.getUsername());
    }

    @Test
    @DisplayName("페이징 테스트")
    void paging() {
        // given
        memberRepository.save(new Member("a", 10));
        memberRepository.save(new Member("b", 10));
        memberRepository.save(new Member("c", 10));
        memberRepository.save(new Member("d", 10));
        memberRepository.save(new Member("e", 10));

        // when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> memberPages = memberRepository.findByAge(10, pageRequest);

        // then
        List<Member> content = memberPages.getContent();
        long totalElements = memberPages.getTotalElements();

        assertEquals(3, content.size());
        assertEquals(5, totalElements);
        assertEquals(0, memberPages.getNumber());
        assertEquals(2, memberPages.getTotalPages());
        assertTrue(memberPages.isFirst());
        assertTrue(memberPages.hasNext());
    }

    @Test
    @DisplayName("페이징 후 매핑 테스트")
    void pagingWithMapping() {
        // given
        memberRepository.save(new Member("a", 10));
        memberRepository.save(new Member("b", 10));
        memberRepository.save(new Member("c", 10));
        memberRepository.save(new Member("d", 10));
        memberRepository.save(new Member("e", 10));

        // when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> memberPages = memberRepository.findByAge(10, pageRequest);
        Page<MemberDto> memberDtoPages = memberPages.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        memberDtoPages.stream().forEach(System.out::println);
    }

    @Test
    @DisplayName("페이징 테스트(slice)")
    void slice() {
        // given
        memberRepository.save(new Member("a", 10));
        memberRepository.save(new Member("b", 10));
        memberRepository.save(new Member("c", 10));
        memberRepository.save(new Member("d", 10));
        memberRepository.save(new Member("e", 10));

        // when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Slice<Member> memberPages = memberRepository.findSlicesByAge(10, pageRequest);

        // then
        List<Member> content = memberPages.getContent();

        assertEquals(3, content.size());
        assertEquals(0, memberPages.getNumber());
        assertTrue(memberPages.isFirst());
        assertTrue(memberPages.hasNext());
    }

    @Test
    @DisplayName("페이징 테스트(list)")
    void list() {
        // given
        memberRepository.save(new Member("a", 10));
        memberRepository.save(new Member("b", 10));
        memberRepository.save(new Member("c", 10));
        memberRepository.save(new Member("d", 10));
        memberRepository.save(new Member("e", 10));

        // when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        List<Member> memberPages = memberRepository.findMembersByAge(10, pageRequest);

        // then
        assertEquals(3, memberPages.size());
    }

    @Test
    @DisplayName("벌크 업데이트 테스트: 나이가 n살 이상인 멤버의 나이를 1씩 증가시킨다")
    @Transactional
    public void bulkUpdate() {
        // given
        memberRepository.save(new Member("a", 10));
        memberRepository.save(new Member("b", 15));
        memberRepository.save(new Member("c", 20));
        memberRepository.save(new Member("d", 30));
        memberRepository.save(new Member("e", 40));
        // when
        int count = memberRepository.increaseAgeOfAllMembersOver(20);
        Member member = memberRepository.findByUsername("e");
        // then
        assertEquals(3, count);
        assertEquals(41, member.getAge());
    }

    @Test
    @DisplayName("Patch Join 테스트")
    @Transactional
    public void patchJoinTest() {
        // given
        Team barcelonaFc = new Team("Barcelona FC");
        Team realMadridCf = new Team("Real Madrid CF");
        teamRepository.save(barcelonaFc);
        teamRepository.save(realMadridCf);
        Member lionelMessi = new Member("Lionel Messi", 34, barcelonaFc);
        Member karimBenzema = new Member("Karim Benzema", 33, realMadridCf);
        memberRepository.save(lionelMessi);
        memberRepository.save(karimBenzema);
        entityManager.flush();
        entityManager.clear();
        // when
        List<Member> members = memberRepository.findAll();
        members.forEach(m -> {
            System.out.println(m);
            System.out.println(m.getTeam());
        });
    }

    @Test
    @Transactional
    void hint() {
        // given
        memberRepository.save(new Member("a", 10));
        entityManager.flush();
        entityManager.clear();

        // when
        Member member = memberRepository.findMemberByUsername("a");
        member.changeUsername("b");
        entityManager.flush();
    }


    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    void lock() {
        // given
        memberRepository.save(new Member("a", 10));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Member> member = memberRepository.findMembersByUsername("a");
    }
}