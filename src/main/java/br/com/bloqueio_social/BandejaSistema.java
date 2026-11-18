package br.com.bloqueio_social;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.net.URI;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BandejaSistema {

    private TrayIcon trayIcon;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {

        if (!SystemTray.isSupported()) {
            return;
        }

        SystemTray bandeja = SystemTray.getSystemTray();

        BufferedImage imagem = criarIcone();

        PopupMenu menu = new PopupMenu();

        MenuItem abrir = new MenuItem("Abrir Bloqueio Social");
        MenuItem encerrar = new MenuItem("Encerrar aplicativo");

        abrir.addActionListener(e -> abrirAplicacao());

        encerrar.addActionListener(e -> {
            bandeja.remove(trayIcon);
            System.exit(0);
        });

        menu.add(abrir);
        menu.addSeparator();
        menu.add(encerrar);

        trayIcon = new TrayIcon(imagem, "Bloqueio Social", menu);
        trayIcon.setImageAutoSize(true);

        try {
            bandeja.add(trayIcon);
            abrirAplicacao();
        } catch (AWTException e) {
            throw new RuntimeException(
                    "Não foi possível adicionar o ícone à bandeja.", e
            );
        }
    }

    private BufferedImage criarIcone() {
        BufferedImage imagem = new BufferedImage(
                32, 32, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = imagem.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Polygon losango = new Polygon(
                new int[]{16, 29, 16, 3},
                new int[]{2, 16, 30, 16},
                4
        );

        g.setColor(new Color(139, 92, 246));
        g.fillPolygon(losango);

        g.dispose();

        return imagem;
    }

    private void abrirAplicacao() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(
                        new URI("http://localhost:8080")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
