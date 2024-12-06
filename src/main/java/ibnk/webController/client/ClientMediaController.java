package ibnk.webController.client;

import ibnk.models.internet.Media;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.MediaType;
import ibnk.service.MediaService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.TOOLS;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/client/media")
public class ClientMediaController {
    private final MediaService mediaService;
    @PostMapping("/upload-media")
    public ResponseEntity<Object> uploadImage(
            @AuthenticationPrincipal Subscriptions subscriber,
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role
    ) throws IOException {
        System.out.println(role);
        // Assert file exists
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is missing");
        }
        // Convert MultipartFile to a File object
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving file");
        }
        // Create a new Media object and save it
        Media media = new Media();
        media.setFileName(file.getOriginalFilename());
        media.setRole(role);
        media.setGuid(subscriber.getUuid());
        media.setSize(file.getSize());
        media.setImg(Files.readAllBytes(convertedFile.toPath()));
        media.setType(MediaType.IMAGE.name());
        media.setOriginalFileName(file.getOriginalFilename());
        media.setPhoto(convertedFile.toPath().toString());
        media.setExtension(TOOLS.getFileExtension(file.getOriginalFilename()));
        boolean error = false;
          Media  mediaDetails = mediaService.save(media);
        return ResponseHandler.generateResponse(HttpStatus.OK, error, "success", mediaDetails.getUuid());
    }
}
