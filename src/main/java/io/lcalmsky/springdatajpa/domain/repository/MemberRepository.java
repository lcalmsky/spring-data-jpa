package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.dto.MemberDto;
import io.lcalmsky.springdatajpa.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new io.lcalmsky.springdatajpa.domain.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSlicesByAge(int age, Pageable pageable);

    List<Member> findMembersByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m left join Team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findMembers(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int increaseAgeOfAllMembersOver(@Param("age") int age);

    @EntityGraph(attributePaths = {"team"})
    Member findByUsername(String username);

    @Query("select m from Member m join fetch m.team ")
    List<Member> findAllMembers();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findMemberByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findMembersByUsername(String username);
}