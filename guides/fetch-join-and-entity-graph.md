> 모든 소스 코드는 [여기](https://github.com/lcalmsky/spring-data-jpa)에서 확인 가능합니다.

`@GraphEntity`를 소개하기 전에 JPA를 사용하다보면 겪을 수 있는 사례 하나를 소개합니다.

---

`Member Entity`와 `Team Entity`가 다대일 관계(`@ManyToOne`) 일 때 `Entity` 클래스에 서로의 관계를 표시해줍니다.

`Entity`간 연관관계가 있고 JPA를 이용하여 조회해야 하는 상황에서는 되도록이면 `FetchType.LAZY` 사용을 권장합니다.

관계 파악을 위해 소스 코드를 살펴봅시다. (설명에 필요 없는 import문 등은 생략하였습니다.)

```java

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY) // (1)
    @JoinColumn(name = "team_id") // (2)
    @ToString.Exclude // (3)
    private Team team;
}
```

```java

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;
    @OneToMany(mappedBy = "team") // (4)
    @ToString.Exclude // (3)
    private List<Member> members = new ArrayList<>();
}
```

> (1) Member:Team=N:1 관계가 성립하므로 `@ManyToOne`, 필요한 시점에 추가 조회를 하기 위해 `FetchType.LAZY`를 설정하였습니다.<br>
> (2) `Team Entity`의 테이블 중 어떤 컬럼과 매핑할지 지정합니다.<br>
> (3) `@ToString.Exclude`는 클래스 위에 선언된 `@ToString` 애너테이션으로 인해 자동으로 override 된 toString 메서드에서 제외할 필드를 결정합니다. 제외하지 않으면 Member 클래스와 서로 연관관계가 있어 계속 반복적으로 호출하여 무한루프에 빠지게 됩니다.<br>
> (4) (1)번과 반대의 경우로 `@OneToMany`로 설정합니다. `mappedBy` 속성을 이용해 어떤 연관관계인지 반드시 표현해줘야 합니다. team 객체에 매핑되므로 team 이라고 설정하였습니다.

JPA를 이용해 Member 객체를 조회할 때 FetchType.LAZY를 적용하면 어떤 일이 벌어질까요?

아래 테스트 소스 코드로 확인해 봅시다. (테스트 명은 추후 수정될 걸 고려해 적은 것입니다.)

```java
@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager entityManager;

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
        entityManager.flush(); // (1)
        entityManager.clear(); // (1)
        // when
        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);
    }
}
```

(1) 조건을 추가하고 트랜잭션이 완전히 종료됐음을 알리기위해 `flush()`, `clear()`를 사용하였습니다.

단순히 Memger를 출력하는 소스 코드로 로그를 살펴보면,

```text
2021-07-02 16:26:22.939 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    insert 
    into
        team
        (name, team_id) 
    values
        (?, ?)
2021-07-02 16:26:22.945 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    call next value for hibernate_sequence
2021-07-02 16:26:22.945 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    insert 
    into
        team
        (name, team_id) 
    values
        (?, ?)
2021-07-02 16:26:22.947 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    call next value for hibernate_sequence
2021-07-02 16:26:22.948 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    insert 
    into
        member
        (age, team_id, username, member_id) 
    values
        (?, ?, ?, ?)
2021-07-02 16:26:22.949 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    call next value for hibernate_sequence
2021-07-02 16:26:22.950 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    insert 
    into
        member
        (age, team_id, username, member_id) 
    values
        (?, ?, ?, ?)
2021-07-02 16:26:22.964 DEBUG 97475 --- [           main] org.hibernate.SQL                        : 
    select
        member0_.member_id as member_i1_0_,
        member0_.age as age2_0_,
        member0_.team_id as team_id4_0_,
        member0_.username as username3_0_ 
    from
        member member0_
```

`Team`을 두 번 `insert`하고, `Member`를 두 번 `insert`한 뒤 `member`를 `select`해오고 있습니다.

조건을 만들기 위한 `insert`문을 제외하면 `select` 쿼리가 한 번만 이루어졌죠.

이는 `Member` 객체 내에서 `Team` 객체에 접근하기 전까지는 Team에 대한 쿼리를 수행하지 않기 때문이고 `FetchType.LAZY`가 하는 일이기도 합니다.

그렇다면 Team 정보도 조회하도록 테스트를 수정해 볼까요?

```java
members.forEach(m -> {
    System.out.println(m);
    System.out.println(m.getTeam());
})
```

마지막에 출력하는 부분만 위 처럼 수정하고 실행하면 역시 한 번에 될리가 없죠!

```text
org.hibernate.LazyInitializationException: could not initialize proxy [io.lcalmsky.springdatajpa.domain.entity.Team#1] - no Session
```

이런 에러가 발생합니다.

이럴 땐 `@Transactional` 애너테이션을 사용하면 간단히 해결됩니다.

```java
@Test
@DisplayName("Patch Join 테스트")
@Transactional
public void patchJoinTest(){
    ...
}
```

그리고 실행한 뒤 로그를 살펴보면,

```text
2021-07-02 17:02:46.077 DEBUG 98909 --- [           main] org.hibernate.SQL                        : 
    select
        member0_.member_id as member_i1_0_,
        member0_.age as age2_0_,
        member0_.team_id as team_id4_0_,
        member0_.username as username3_0_ 
    from
        member member0_
Member(id=3, username=Lionel Messi, age=34)
2021-07-02 17:02:46.118 DEBUG 98909 --- [           main] org.hibernate.SQL                        : 
    select
        team0_.team_id as team_id1_1_0_,
        team0_.name as name2_1_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
Team(id=1, name=Barcelona FC)
Member(id=4, username=Karim Benzema, age=33)
2021-07-02 17:02:46.137 DEBUG 98909 --- [           main] org.hibernate.SQL                        : 
    select
        team0_.team_id as team_id1_1_0_,
        team0_.name as name2_1_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
Team(id=2, name=Real Madrid CF)
```

Member를 select 한 뒤 Team을 출력하는 시점에 추가로 쿼리해오는 것을 확인할 수 있습니다.

Member를 조회한 결과가 2명이었고(쿼리 1회) 2명이 다른 두 팀을 가지고 있었기 때문에 2번의 추가 쿼리가 이루어져 총 3번의 쿼리가 발생했습니다.

이를 JPA에선 N+1 문제라고 부릅니다. 1개의 쿼리 결과가 N개이면 N번의 쿼리가 더 발생하기 때문이죠.

1000명의 member가 1000개의 팀을 가지고 있는 경우 1001번의 쿼리가 발생하고 이는 시스템에 큰 부하를 주게 됩니다.

위와 같은 문제 해결을 위해 JPA 진영에서 내놓은 해답은 바로 `Fetch Join` 입니다.

> 사실 Fetch Join 자체가 비중있게 다뤄야 할 컨텐츠라서 이 부분은 나중에 시간이 되면 정리할 생각입니다.

그리고 스프링 데이터 JPA에서 Fetch Join을 사용하기 위해서는 `@EntityGraph`가 필요합니다.

