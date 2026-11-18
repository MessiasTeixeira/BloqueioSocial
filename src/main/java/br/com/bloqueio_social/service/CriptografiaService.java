package br.com.bloqueio_social.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CriptografiaService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_CHAVE = 256;
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG = 128;

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

    private final SecretKeySpec chave;
    private final SecureRandom random = new SecureRandom();

    public CriptografiaService() {
        this.chave = carregarOuGerarChave();
    }

    private SecretKeySpec carregarOuGerarChave() {
        try {
            Path diretorio = Paths.get(
                    System.getProperty("user.home"),
                    ".bloqueio-social"
            );

            Files.createDirectories(diretorio);

            Path arquivoChave = diretorio.resolve("chave.aes");

            if (Files.exists(arquivoChave)) {
                byte[] chaveExistente = Files.readAllBytes(arquivoChave);

                if (chaveExistente.length != 32) {
                    throw new IllegalStateException("Chave AES inválida.");
                }

                return new SecretKeySpec(chaveExistente, "AES");
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(TAMANHO_CHAVE);

            SecretKey novaChave = keyGenerator.generateKey();

            try {
                Files.write(
                        arquivoChave,
                        novaChave.getEncoded(),
                        StandardOpenOption.CREATE_NEW
                );
            } catch (java.nio.file.FileAlreadyExistsException e) {
                byte[] chaveExistente = Files.readAllBytes(arquivoChave);

                if (chaveExistente.length != 32) {
                    throw new IllegalStateException("Chave AES inválida.");
                }

                return new SecretKeySpec(chaveExistente, "AES");
            }

            return new SecretKeySpec(novaChave.getEncoded(), "AES");

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível carregar a chave de criptografia.", e
            );
        }
    }

    public String gerarSenha() {
        StringBuilder senha = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            int indice = random.nextInt(CARACTERES.length());
            senha.append(CARACTERES.charAt(indice));
        }

        return senha.toString();
    }

    public String criptografar(String texto) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);

            GCMParameterSpec parametros =
                    new GCMParameterSpec(TAMANHO_TAG, iv);

            cipher.init(Cipher.ENCRYPT_MODE, chave, parametros);

            byte[] conteudo = cipher.doFinal(
                    texto.getBytes(StandardCharsets.UTF_8)
            );

            byte[] resultado = new byte[iv.length + conteudo.length];

            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(conteudo, 0, resultado, iv.length, conteudo.length);

            return Base64.getEncoder().encodeToString(resultado);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Erro ao criptografar a senha.", e
            );
        }
    }

    public String descriptografar(String textoCriptografado) {
        try {
            byte[] dados = Base64.getDecoder().decode(textoCriptografado);

            if (dados.length <= TAMANHO_IV) {
                throw new IllegalArgumentException(
                        "Dados criptografados inválidos."
                );
            }

            byte[] iv = new byte[TAMANHO_IV];
            byte[] conteudo = new byte[dados.length - TAMANHO_IV];

            System.arraycopy(dados, 0, iv, 0, TAMANHO_IV);
            System.arraycopy(dados, TAMANHO_IV, conteudo, 0, conteudo.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO);

            GCMParameterSpec parametros =
                    new GCMParameterSpec(TAMANHO_TAG, iv);

            cipher.init(Cipher.DECRYPT_MODE, chave, parametros);

            byte[] original = cipher.doFinal(conteudo);

            return new String(original, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Erro ao descriptografar a senha.", e
            );
        }
    }
}
