package ggm.board.domain.member.repository;

import ggm.board.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberUserRepository extends JpaRepository<Member, Long> {

}