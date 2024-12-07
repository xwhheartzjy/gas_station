package org.codec.controller.third;

import org.codec.service.third.AreaReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/area-report")
public class AreaReportController {

    @Autowired
    private AreaReportService areaReportService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJsonFile(@RequestParam("file") MultipartFile file) {
        try {
            String tempFilePath = "/tmp/"+file.getOriginalFilename();
            file.transferTo(new File(tempFilePath));
            areaReportService.processJsonAndSave(tempFilePath);
            return ResponseEntity.ok("File processed and data saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to process file: " + e.getMessage());
        }
    }
}
