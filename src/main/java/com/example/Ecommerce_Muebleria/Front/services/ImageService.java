package com.example.Ecommerce_Muebleria.Front.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    // Constructor injection (Mejor práctica en 2026 vs @Autowired en campos)
    public ImageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadImg(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 🛡️ Solo imágenes: evita usar tu Cloudinary para hospedar HTML, ejecutables, PDFs, etc.
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("El archivo no es una imagen: " + contentType);
        }

        // resource_type=image hace que Cloudinary rechace todo lo que no sea realmente una imagen
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image"));
        return uploadResult.get("secure_url").toString();
    }
}