package controller;

import model.ImageFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import repository.ImageFileRepository;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/images")
public class ImageFileController {

    @Autowired
    private ImageFileRepository imageFileRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (imageFileRepository.count() >= 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Maximum 5 images allowed.");
        }

        ImageFile image = new ImageFile();
        image.setFileName(file.getOriginalFilename());
        image.setFileType(file.getContentType());
        image.setData(file.getBytes());

        imageFileRepository.save(image);
        return ResponseEntity.ok("Image uploaded successfully.");
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllImages() {
        List<Map<String, Object>> imageList = new ArrayList<>();
        imageFileRepository.findAll().forEach(img -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", img.getId());
            map.put("fileName", img.getFileName());
            map.put("url", "/api/images/" + img.getId());
            imageList.add(map);
        });
        return ResponseEntity.ok(imageList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<ImageFile> imageOptional = imageFileRepository.findById(id);
        if (imageOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ImageFile image = imageOptional.get();
        String fileType = image.getFileType();
        if (fileType == null || fileType.isBlank()) {
            fileType = "application/octet-stream"; // fallback for binary
        }

        ResponseEntity<byte[]> body = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileType))
                .body(image.getData());
        return body;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        imageFileRepository.deleteById(id);
        return ResponseEntity.ok("Image deleted.");
    }
}
