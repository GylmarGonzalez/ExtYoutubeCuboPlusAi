package com.cuboplus.ytdownloader.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DownloadService {

    @Value("${app.download.dir}")
    private String downloadDir;

    public String downloadVideo(String url, String format) throws Exception {
        File dir = new File(downloadDir);
        if (!dir.exists()) dir.mkdirs();

        List<String> cmd = new ArrayList<>();
        cmd.add("/opt/homebrew/bin/yt-dlp");
        cmd.add("--no-playlist");
        cmd.add("--restrict-filenames");  // elimina caracteres especiales del nombre
        cmd.add("-o");
        cmd.add(downloadDir + "/%(title)s.%(ext)s");

        if ("mp3".equalsIgnoreCase(format)) {
            cmd.add("-x");
            cmd.add("--audio-format");
            cmd.add("mp3");
        } else {
            cmd.add("-f");
            cmd.add("bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            cmd.add("--merge-output-format");
            cmd.add("mp4");
        }

        cmd.add(url);

        System.out.println("▶ Ejecutando: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.environment().put("PATH", "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin");
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[yt-dlp] " + line);
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("yt-dlp falló (código " + exitCode + "): " + output);
        }

        // Buscar el archivo más reciente en la carpeta
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new Exception("No se encontró el archivo descargado");
        }
        File newest = files[0];
        for (File f : files) {
            if (f.lastModified() > newest.lastModified()) newest = f;
        }
        return newest.getName();
    }

    public String getVideoTitle(String url) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "/opt/homebrew/bin/yt-dlp", "--get-title", "--no-playlist", url
        );
        pb.redirectErrorStream(true);
        pb.environment().put("PATH", "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String title = reader.readLine();
        process.waitFor();
        return title != null ? title : "Video de YouTube";
    }

    public String getDownloadDir() { return downloadDir; }
}
