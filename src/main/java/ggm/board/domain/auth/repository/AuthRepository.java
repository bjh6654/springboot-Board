package ggm.board.domain.auth.repository;

import ggm.board.domain.auth.entity.Auth;
import ggm.board.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByEmail(String email);
}
