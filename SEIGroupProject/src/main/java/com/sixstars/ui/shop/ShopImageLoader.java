package com.sixstars.ui.shop;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

/**
 * Loads shop product images synchronously from disk or the classpath (avoids blank {@link javax.swing.ImageIcon} tiles
 * when {@code getScaledInstance} has not finished tracking).
 */
public final class ShopImageLoader {

    private ShopImageLoader() {
    }

    /**
     * Reads a source image from the given path and returns a {@code boxW × boxH} cover crop, or {@code null} if missing.
     */
    public static BufferedImage loadCover(String imagePath, int boxW, int boxH) {
        BufferedImage src = readSource(imagePath);
        if (src == null) {
            return null;
        }
        return scaleCover(src, boxW, boxH);
    }

    private static BufferedImage readSource(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String path = raw.trim().replace('\\', '/');

        try {
            File direct = new File(path);
            if (direct.isFile()) {
                return ImageIO.read(direct);
            }
        } catch (Exception ignored) {
        }

        String userDir = System.getProperty("user.dir", ".");
        try {
            File cwd = new File(userDir, path);
            if (cwd.isFile()) {
                return ImageIO.read(cwd);
            }
        } catch (Exception ignored) {
        }

        String cp = path.startsWith("/") ? path : "/" + path;
        try (InputStream in = ShopImageLoader.class.getResourceAsStream(cp)) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) {
        }

        String name = Paths.get(path).getFileName().toString();
        if (!name.isBlank()) {
            try (InputStream in = ShopImageLoader.class.getResourceAsStream("/assets/shopImages/" + name)) {
                if (in != null) {
                    return ImageIO.read(in);
                }
            } catch (Exception ignored) {
            }
        }

        try (InputStream in = ShopImageLoader.class.getResourceAsStream("/assets/shopImages/default.png")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static BufferedImage scaleCover(BufferedImage src, int boxW, int boxH) {
        int iw = src.getWidth();
        int ih = src.getHeight();
        if (iw <= 0 || ih <= 0) {
            return src;
        }
        double scale = Math.max((double) boxW / iw, (double) boxH / ih);
        int nw = Math.max(1, (int) Math.round(iw * scale));
        int nh = Math.max(1, (int) Math.round(ih * scale));
        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, nw, nh, 0, 0, iw, ih, null);
        } finally {
            g.dispose();
        }
        int x = Math.max(0, (nw - boxW) / 2);
        int y = Math.max(0, (nh - boxH) / 2);
        return scaled.getSubimage(x, y, boxW, boxH);
    }
}
