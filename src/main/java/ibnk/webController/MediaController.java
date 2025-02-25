package ibnk.webController;

import ibnk.models.internet.Media;
import ibnk.models.internet.enums.MediaType;
import ibnk.service.MediaService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.TOOLS;
import ibnk.intergrations.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

/**
 * @author PHILF
 */
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin/media")
public class MediaController {
    private final MediaService mediaService;
    private final EmailService emailService;

    @PostMapping("/upload-media")
    public ResponseEntity<Object> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role
    ) throws IOException {
        System.out.println(role);
        // Assert file exists
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is missing");
        }
        // Convert MultipartFile to a File object
        // Read bytes from the MultipartFile
        byte[] bytes = file.getBytes();
        Media media = new Media();
        media.setFileName(file.getOriginalFilename());
        media.setSize(file.getSize());
        media.setImg(bytes);
        media.setType(MediaType.IMAGE.name());
        media.setOriginalFileName(file.getOriginalFilename());
        media.setExtension(TOOLS.getFileExtension(file.getOriginalFilename()));
        boolean error = false;
        Media mediaDetails = mediaService.save(media);
        return ResponseHandler.generateResponse(HttpStatus.OK, error, "success", mediaDetails.getUuid());
    }
    @GetMapping("/view-media/{uuid}")
    public ResponseEntity<Object> listImage(@PathVariable("uuid") String uuid) {
        Optional<Media> response = mediaService.findByUuid(uuid);
        if(response.isEmpty()){
            return ResponseHandler.generateResponse(HttpStatus.CONTINUE, false, "no image", null);
        }
        return response.map(media -> ResponseHandler.generateResponse(HttpStatus.OK, true, "success",
                Base64.getEncoder().encodeToString(media.getImg())
        )).orElseGet(() -> ResponseHandler.generateResponse(HttpStatus.CONTINUE, false, "no image", null));
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<Object> sendEmail() {
        emailService.sendSimpleMessage("benzeezmokom@gmail.com", "TEST",

                """
                        <body>
                            <div style="font-family: Arial, sans-serif;">

                                <h2>Welcome to Our Newsletter!</h2>

                                <p>Dear Subscriber,</p>

                                <p>Thank you for subscribing to our newsletter. We are thrilled to have you on board!</p>

                                <p>Here's what you can expect from our newsletter:</p>

                                <ul>
                                    <li>Exclusive offers and promotions</li>
                                    <li>Latest updates on our products and services</li>
                                    <li>Useful tips and tricks</li>
                                </ul>

                                <p>We promise to deliver valuable content straight to your inbox. Stay tuned!</p>

                                <p>Best Regards,<br> The Newsletter Team</p>

                            </div>
                        </body>
                        """);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "success", "Success");
    }

}
