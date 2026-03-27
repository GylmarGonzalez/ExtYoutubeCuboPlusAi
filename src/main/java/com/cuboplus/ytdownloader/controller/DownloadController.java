package com.cuboplus.ytdownloader.controller;

import com.cuboplus.ytdownloader.model.DownloadRequest;
import com.cuboplus.ytdownloader.model.DownloadResponse;
import com.cuboplus.ytdownloader.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    // POST /api/download  — recibe URL y descarga
    @PostMapping("/download")
    public ResponseEntity<DownloadResponse> download(@RequestBody DownloadRequest request) {
        try {
            System.out.println("📥 Descargando: " + request.getUrl());
            String filename = downloadService.downloadVideo(request.getUrl(), request.getFormat());
            String dlUrl = "http://localhost:8090/api/file/" + filename;
            return ResponseEntity.ok(new DownloadResponse(true, "✅ Descarga completada", filename, dlUrl));
        } catch (Exception e) {
            return ResponseEntity.ok(new DownloadResponse(false, "❌ Error: " + e.getMessage(), null, null));
        }
    }

    // GET /api/info?url=... — obtiene título sin descargar
    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestParam String url) {
        try {
            String title = downloadService.getVideoTitle(url);
            return ResponseEntity.ok(java.util.Map.of("title", title, "url", url));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("error", e.getMessage()));
        }
    }

    // GET /api/file/{filename} — sirve el archivo descargado
    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            File file = new File(downloadService.getDownloadDir() + "/" + filename);
            if (!file.exists()) return ResponseEntity.notFound().build();

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/health — verificar que el servidor está vivo
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(java.util.Map.of("status", "ok", "service", "Lista API de prueba IA"));
    }
}
