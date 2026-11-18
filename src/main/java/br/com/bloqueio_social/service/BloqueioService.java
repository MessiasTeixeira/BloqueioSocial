package br.com.bloqueio_social.service;

import br.com.bloqueio_social.model.Bloqueio;
import br.com.bloqueio_social.model.HistoricoSenha;
import br.com.bloqueio_social.repository.BloqueioRepository;
import br.com.bloqueio_social.repository.HistoricoSenhaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class BloqueioService {

    private final BloqueioRepository bloqueioRepository;
    private final CriptografiaService criptografiaService;
    private final HistoricoSenhaRepository historicoSenhaRepository;

    public BloqueioService(BloqueioRepository bloqueioRepository, CriptografiaService criptografiaService, HistoricoSenhaRepository historicoSenhaRepository) {
        this.bloqueioRepository = bloqueioRepository;
        this.criptografiaService = criptografiaService;
        this.historicoSenhaRepository = historicoSenhaRepository;
    }

    public String gerarSenha() {
        return criptografiaService.gerarSenha();
    }

    public Optional<Bloqueio> buscarBloqueio() {
        return bloqueioRepository.findFirstByOrderByIdAsc();
    }

    @Transactional
    public Bloqueio iniciarBloqueio(String senha, LocalDateTime dataFinal) {

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha não pode estar vazia.");
        }

        LocalDateTime agora = LocalDateTime.now();

        if (dataFinal == null || !dataFinal.isAfter(agora)) {
            throw new IllegalArgumentException(
                    "A data final precisa estar no futuro."
            );
        }

        Optional<Bloqueio> bloqueioExistente = buscarBloqueio();

        Bloqueio bloqueio = bloqueioExistente.orElseGet(Bloqueio::new);

        if (bloqueioExistente.isPresent()) {

            arquivarSeNecessario(bloqueio);

            if (bloqueio.isIniciado()
                    && agora.isBefore(bloqueio.getDataFinal())) {

                throw new IllegalStateException(
                        "Já existe um bloqueio em andamento."
                );
            }
        }

        bloqueio.setSenha(criptografiaService.criptografar(senha));
        bloqueio.setDataInicial(agora);
        bloqueio.setDataFinal(dataFinal);
        bloqueio.setIniciado(true);

        bloqueio.setArquivado(false);

        return bloqueioRepository.save(bloqueio);
    }

    public boolean estaBloqueado() {
        Optional<Bloqueio> resultado = buscarBloqueio();

        if (resultado.isEmpty()) {
            return false;
        }

        Bloqueio bloqueio = resultado.get();

        return bloqueio.isIniciado()
                && LocalDateTime.now().isBefore(bloqueio.getDataFinal());
    }

    public String obterSenhaLiberada() {

        Bloqueio bloqueio = buscarBloqueio().orElseThrow(() -> new IllegalStateException(
                        "Nenhum bloqueio foi encontrado."
                ));

        if (!bloqueio.isIniciado()) {
            throw new IllegalStateException("O bloqueio ainda não foi iniciado.");
        }

        if (LocalDateTime.now().isBefore(bloqueio.getDataFinal())) {
            throw new IllegalStateException("O prazo do bloqueio ainda não terminou.");
        }

        return criptografiaService.descriptografar(bloqueio.getSenha());
    }

    @Transactional 
    public List<HistoricoSenha> buscarHistorico() {

        buscarBloqueio().ifPresent(this::arquivarSeNecessario);

        if (estaBloqueado()) {
            throw new IllegalStateException(
                    "O histórico não está disponível durante um bloqueio."
            );
        }

        return historicoSenhaRepository.findAllByOrderByDataRegistroDesc();
    }

    @Transactional 
    public String obterSenhaHistorico(Long id) {

        buscarBloqueio().ifPresent(this::arquivarSeNecessario);

        if (estaBloqueado()) {
            throw new IllegalStateException(
                    "O histórico não está disponível durante um bloqueio."
            );
        }

        HistoricoSenha historico = historicoSenhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Senha histórica não encontrada."
                ));

        return criptografiaService.descriptografar(historico.getSenha());
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void verificarBloqueioEncerrado() {

        Optional<Bloqueio> resultado = buscarBloqueio();

        if (resultado.isEmpty()) {
            return;
        }

        arquivarSeNecessario(resultado.get());
    }

    private void arquivarSeNecessario(Bloqueio bloqueio) {

        LocalDateTime agora = LocalDateTime.now();

        if (!bloqueio.isIniciado() || bloqueio.isArquivado()) {
            return;
        }

        if (agora.isBefore(bloqueio.getDataFinal())) {
            return;
        }

        HistoricoSenha historico = new HistoricoSenha();

        historico.setSenha(bloqueio.getSenha());
        historico.setDataInicial(bloqueio.getDataInicial());
        historico.setDataFinal(bloqueio.getDataFinal());
        historico.setDataRegistro(agora);

        historicoSenhaRepository.save(historico);

        bloqueio.setArquivado(true);

        bloqueioRepository.save(bloqueio);
    }

    @Transactional
    public void excluirSenhaHistorica(Long id) {

        buscarBloqueio().ifPresent(this::arquivarSeNecessario);

        if (estaBloqueado()) {
            throw new IllegalStateException(
                    "O histórico não está disponível durante um bloqueio."
            );
        }

        HistoricoSenha historico = historicoSenhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Senha histórica não encontrada."
                ));

        historicoSenhaRepository.delete(historico);
    }

    @Transactional
    public void excluirTodoHistorico() {

        buscarBloqueio().ifPresent(this::arquivarSeNecessario);

        if (estaBloqueado()) {
            throw new IllegalStateException("O histórico não está disponível durante um bloqueio."
            );
        }

        historicoSenhaRepository.deleteAll();
    }
    
    @Transactional
    public String encerrarPorEmergencia(String confirmacao) {

        String fraseEsperada =
                "EU DESEJO ENCERRAR MEU BLOQUEIO ANTECIPADAMENTE";

        if (!fraseEsperada.equals(confirmacao)) {
            throw new IllegalArgumentException(
                    "Frase de confirmação incorreta."
            );
        }

        Bloqueio bloqueio = buscarBloqueio()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhum bloqueio foi encontrado."
                ));

        LocalDateTime agora = LocalDateTime.now();

        if (!bloqueio.isIniciado()
                || !agora.isBefore(bloqueio.getDataFinal())) {

            throw new IllegalStateException(
                    "Não existe um bloqueio ativo para encerrar."
            );
        }

        String senha = criptografiaService.descriptografar(
                bloqueio.getSenha()
        );

        HistoricoSenha historico = new HistoricoSenha();

        historico.setSenha(bloqueio.getSenha());
        historico.setDataInicial(bloqueio.getDataInicial());

        historico.setDataFinal(bloqueio.getDataFinal());

        historico.setDataRegistro(agora);
        historico.setEmergencial(true);

        historicoSenhaRepository.save(historico);

        bloqueio.setDataFinal(agora);
        bloqueio.setArquivado(true);

        bloqueioRepository.save(bloqueio);

        return senha;
    }        
}

