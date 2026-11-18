package br.com.bloqueio_social.repository;

import br.com.bloqueio_social.model.HistoricoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoSenhaRepository extends JpaRepository<HistoricoSenha, Long> {

    List<HistoricoSenha> findAllByOrderByDataRegistroDesc();

}
