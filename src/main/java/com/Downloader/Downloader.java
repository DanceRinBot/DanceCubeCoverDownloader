package com.Downloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.Downloader.Config.*;

public class Downloader {
    static Long musicId = null;
    static String coverUrl = null;
    private static final Gson gson = (new GsonBuilder()).create();
    private static final List<Integer> MUSIC_INDEX_LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 101);
    private static AtomicInteger failureCount = new AtomicInteger(0);
    private static final List<String> coverUrls = new ArrayList();
    private static final List<Long> coverIds = new ArrayList();

    public Downloader() {
    }

    public static void Downloader() {
        patcher();

        for(int i = 0; i < coverUrls.size(); ++i) {
            String fileUrl = coverUrls.get(i);
            Long id = coverIds.get(i);
            String savePath = configPath + "Images/Cover/OfficialImage/" + id + ".jpg";

            try {
                URL url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(savePath);
                byte[] buffer = new byte[1024];

                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();
                System.out.println("File Download Complete: " + savePath);
            } catch (IOException var10) {
                System.err.println("Download Failed: " + var10.getMessage());
            }
        }

    }

    private static void patcher() {
            try {
                Iterator var0 = MUSIC_INDEX_LIST.iterator();

                while(var0.hasNext()) {
                    Integer musicIndex = (Integer)var0.next();
                    boolean hasDataForCurrentIndex;
                    System.out.println("Processing Index: " + musicIndex);

                    for(int page = 1; page <= 50; ++page) {
                        String url = String.format("https://dancedemo.shenghuayule.com/Dance/api/User/GetMusicRankingNew?musicIndex=%s&page=%d&pagesize=20", musicIndex, page);
                        System.out.println("Patching " + url);

                        try {
                            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
                            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30L)).header("User-Agent", "Mozilla/5.0").GET().build();
                            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                            hasDataForCurrentIndex = processApiResponse(response.body());
                            if (!hasDataForCurrentIndex) {
                                System.out.println("Index" + musicIndex + "Page" + page + "No data,Skipping");
                                break;
                            }

                            Thread.sleep(100);
                        } catch (IOException var8) {
                            System.err.println("HTTP Request Failed: " + var8.getMessage());
                            failureCount.incrementAndGet();
                        } catch (InterruptedException var9) {
                            System.err.println("Request Interrupt: " + var9.getMessage());
                            Thread.currentThread().interrupt();
                            return;
                        } catch (Exception var10) {
                            System.err.println("Process Page Failed: " + var10.getMessage());
                            failureCount.incrementAndGet();
                        }
                    }
                }
            } catch (Exception var11) {
                System.err.println("Run Failed: " + var11.getMessage());
                var11.printStackTrace();
            }
        }


    private static boolean processApiResponse(String jsonResponse) {
        boolean hasData = false;

        try {
            JsonObject responseObj = (JsonObject)gson.fromJson(jsonResponse, JsonObject.class);
            if (responseObj == null) {
                System.err.println("JSON Parse Failed: empty");
                return false;
            }

            JsonArray listArray = responseObj.getAsJsonArray("List");
            if (listArray == null || listArray.size() == 0) {
                System.out.println("No data on this page.");
                return false;
            }

            hasData = true;

            for(int i = 0; i < listArray.size(); ++i) {
                JsonObject item = listArray.get(i).getAsJsonObject();

                try {
                    musicId = item.has("MusicID") ? item.get("MusicID").getAsLong() : null;
                    coverUrl = item.has("Cover") ? item.get("Cover").getAsString() : null;
                    if (musicId != null && coverUrl != null) {
                        if (coverUrl.endsWith("/200")) {
                            coverUrl = coverUrl.substring(0, coverUrl.length() - 4);
                        }

                        coverUrls.add(coverUrl);
                        System.out.println("Added Cover URL: " + coverUrl);
                        coverIds.add(musicId);
                    } else {
                        System.err.println("No Object: MusicID=" + musicId + ", Cover=" + coverUrl);
                    }
                } catch (Exception var7) {
                    failureCount.incrementAndGet();
                }
            }
        } catch (Exception var8) {
            System.err.println("JSON Parse Failed: " + var8.getMessage());
            System.err.println("Original Response: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())));
            failureCount.incrementAndGet();
        }

        return hasData;
    }

    @Test
    public void testDownloader() {
        Downloader();
    }
}
