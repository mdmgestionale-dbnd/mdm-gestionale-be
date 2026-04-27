package com.db.mdm.gestionale.be.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.AllegatoRepository;
import com.db.mdm.gestionale.be.service.AllegatoService;
import com.db.mdm.gestionale.be.service.SupabaseS3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllegatoServiceImpl implements AllegatoService {

    private static final long MAX_BYTES = 2L * 1024 * 1024; // 2MB

    private final AllegatoRepository allegatoRepo;
    private final SupabaseS3Service supabaseS3Service;

    private final Set<String> ALLOWED_MIMES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    @Override
    @Transactional
    public Allegato saveAllegato(MultipartFile file, Cantiere cantiere, Utente uploader) throws Exception {
        String contentType = file.getContentType() == null ? "" : file.getContentType();

        if (!ALLOWED_MIMES.contains(contentType) && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tipo file non ammesso");
        }

        byte[] data = file.getBytes();

        if (data.length > MAX_BYTES) {
            // attempt compress for images and pdf
            if (contentType.startsWith("image/")) {
                data = tryCompressImage(file, MAX_BYTES);
            } else if ("application/pdf".equals(contentType)) {
                data = tryCompressPdf(file, MAX_BYTES);
            } else {
                throw new IllegalArgumentException("File troppo grande e non comprimibile");
            }
        }

        if (data.length > MAX_BYTES) {
            throw new IllegalArgumentException("File supera i 2MB anche dopo la compressione");
        }

        String key = String.format("cantieri/%s/%s_%s", cantiere.getId(), UUID.randomUUID(), file.getOriginalFilename());
        supabaseS3Service.uploadBytes(data, key, contentType);

        Allegato a = new Allegato();
        a.setCantiere(cantiere);
        a.setNomeFile(file.getOriginalFilename());
        a.setTipoFile(contentType);
        a.setStoragePath(key);
        a.setCreatedAt(LocalDateTime.now());
        a.setDeleted(false);
        // createdBy if uploader non null
        if (uploader != null) a.setCreatedBy(uploader);

        return allegatoRepo.save(a);
    }

    @Override
    public List<Allegato> listByCantiereAndDateRange(Long cantiereId, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return allegatoRepo.findByCantiereIdAndIsDeletedFalse(cantiereId);
        }
        return allegatoRepo.findByCantiereIdAndCreatedAtBetweenAndIsDeletedFalse(cantiereId, from == null ? LocalDateTime.MIN : from, to == null ? LocalDateTime.MAX : to);
    }

    @Override
    public Allegato findById(Long id) {
        return allegatoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Allegato non trovato"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Allegato allegato = findById(id);
        allegato.setDeleted(true);
        allegatoRepo.save(allegato);
    }

    @Override
    public byte[] downloadFile(String storagePath) throws Exception {
        return supabaseS3Service.downloadFile(storagePath);
    }

    @Override
    public void deleteFileFromStorage(String storagePath) throws Exception {
        supabaseS3Service.deleteFile(storagePath);
    }

    // ---- helper compression methods ----
    private byte[] tryCompressImage(MultipartFile file, long targetBytes) throws IOException {
        BufferedImage img = ImageIO.read(file.getInputStream());
        if (img == null) throw new IOException("Immagine non leggibile");

        int width = img.getWidth();
        int height = img.getHeight();
        float quality = 0.95f;

        // Iteratively resize and re-encode as JPEG to reduce size
        for (int attempt = 0; attempt < 8; attempt++) {
            int newW = Math.max(200, (int) (width * Math.pow(0.85, attempt)));
            int newH = Math.max(200, (int) (height * Math.pow(0.85, attempt)));
            BufferedImage resized = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, newW, newH);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);
            byte[] candidate = baos.toByteArray();
            if (candidate.length <= targetBytes) return candidate;
            // else continue attempts (loop reduces size each iteration)
        }
        // last fallback: attempt lower quality write
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ImageIO.write(Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, 800, 600), "jpg", baos2);
        if (baos2.size() <= targetBytes) return baos2.toByteArray();

        throw new IOException("Impossibile comprimere immagine sotto i 2MB");
    }

    private byte[] tryCompressPdf(MultipartFile file, long targetBytes) throws IOException {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            PDDocument out = new PDDocument();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 72); // low DPI
                PDPage page = new PDPage(new PDRectangle(pageImage.getWidth(), pageImage.getHeight()));
                out.addPage(page);
                PDImageXObject pdImage = LosslessFactory.createFromImage(out, pageImage);
                try (PDPageContentStream content = new PDPageContentStream(out, page)) {
                    content.drawImage(pdImage, 0, 0, pageImage.getWidth(), pageImage.getHeight());
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            out.save(baos);
            out.close();
            byte[] result = baos.toByteArray();
            if (result.length <= targetBytes) return result;
        } catch (Exception ex) {
            log.warn("PDF compression failed: {}", ex.getMessage());
        }
        throw new IOException("Impossibile comprimere PDF sotto i 2MB");
    }
}
