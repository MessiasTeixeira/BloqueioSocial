package br.com.bloqueio_social.repository;

import br.com.bloqueio_social.model.Bloqueio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueioRepository extends JpaRepository<Bloqueio, Long> {
    Optional<Bloqueio> findFirstByOrderByIdAsc();
}
