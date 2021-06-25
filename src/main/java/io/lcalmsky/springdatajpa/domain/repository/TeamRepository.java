package io.lcalmsky.springdatajpa.domain.repository;

import io.lcalmsky.springdatajpa.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
