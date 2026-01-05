package ggm.board.domain.auth.repository;

import ggm.board.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

@ResponseStatus
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
    Boolean existsByToken(String token);

    @Transactional
    void deleteByToken(String token);
}
