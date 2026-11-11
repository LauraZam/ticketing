package kz.kimep.ticketing.identity.repo;

import kz.kimep.ticketing.identity.domain.RefreshToken;
import kz.kimep.ticketing.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}