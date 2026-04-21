package com.foodopia.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Service für Thumbnail-Generierung
 *
 * Erstellt optimierte 128x128 Thumbnails aus Base64-kodierten Bildern
 */
@Service
@Slf4j
public class ThumbnailService {

    private static final int THUMBNAIL_SIZE = 128;

    /**
     * Generiert ein Thumbnail (128x128) aus einem Base64-kodierten Bild
     *
     * @param base64Image Base64-kodiertes Bild (mit oder ohne data:image Prefix)
     * @param contentType Content-Type des Bildes (z.B. "image/jpeg")
     * @return Base64-kodiertes Thumbnail im Format "data:image/jpeg;base64,..."
     * @throws IOException wenn Bildverarbeitung fehlschlägt
     */
    public String generateThumbnail(String base64Image, String contentType) throws IOException {
        log.debug("Generating thumbnail for image, contentType={}", contentType);

        try {
            // Entferne "data:image/...;base64," Prefix falls vorhanden
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            // Dekodiere Base64 zu Bytes
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            // Konvertiere zu BufferedImage
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (originalImage == null) {
                log.error("Failed to read image - ImageIO returned null");
                throw new IOException("Ungültiges Bildformat");
            }

            // Erstelle Thumbnail mit Thumbnailator
            ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .outputFormat(getImageFormat(contentType))
                    .toOutputStream(thumbnailOutputStream);

            // Kodiere Thumbnail zu Base64
            String thumbnailBase64 = Base64.getEncoder().encodeToString(thumbnailOutputStream.toByteArray());

            // Erstelle vollständige Data-URL
            String thumbnailDataUrl = "data:" + contentType + ";base64," + thumbnailBase64;

            log.debug("Thumbnail generated successfully, size={}x{}", THUMBNAIL_SIZE, THUMBNAIL_SIZE);

            return thumbnailDataUrl;

        } catch (Exception e) {
            log.error("Failed to generate thumbnail", e);
            throw new IOException("Thumbnail-Generierung fehlgeschlagen", e);
        }
    }

    /**
     * Extrahiert das Bildformat aus dem Content-Type
     *
     * @param contentType Content-Type (z.B. "image/jpeg")
     * @return Bildformat (z.B. "jpg")
     */
    private String getImageFormat(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            return "jpg"; // Default
        }

        String format = contentType.substring(6); // Entferne "image/"

        // Konvertiere bekannte Formate
        switch (format.toLowerCase()) {
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            case "webp":
                return "webp";
            default:
                return "jpg";
        }
    }
}

