package br.com.bloqueio_social;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.JOptionPane;

public class ControleInstancia {

    private static FileChannel canal;
    private static FileLock bloqueio;

    
public static boolean adquirir() {

    try {
        Path diretorio = Path.of(
                System.getProperty("user.home"),
                ".bloqueio-social"
        );

        Files.createDirectories(diretorio);

        Path arquivo = diretorio.resolve("aplicacao.lock");

        canal = FileChannel.open(
                arquivo,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        );

        bloqueio = canal.tryLock();

        if (bloqueio == null) {

            canal.close();
            mostrarMensagem();
            return false;
        }


        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> liberar())
        );

        return true;

    } catch (OverlappingFileLockException e) {


        try {
            if (canal != null && canal.isOpen()) {
                canal.close();
            }
        } catch (IOException erro) {
            erro.printStackTrace();
        }

        mostrarMensagem();
        return false;

    } catch (IOException e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(
                null,
                "Não foi possível verificar se o aplicativo já está aberto.\n"
                        + e.getMessage(),
                "Erro ao iniciar",
                JOptionPane.ERROR_MESSAGE
        );

        return false;
    }
}


    private static void liberar() {
        try {
            if (bloqueio != null && bloqueio.isValid()) {
                bloqueio.release();
            }

            if (canal != null && canal.isOpen()) {
                canal.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mostrarMensagem() {
        JOptionPane.showMessageDialog(
                null,
                "O Bloqueio Social já está em execução.",
                "Aplicativo já aberto",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
