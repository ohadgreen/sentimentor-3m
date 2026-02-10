package com.acme.controllers;

import com.acme.services.DbUtilsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/db-utils")
public class DbUtilsController {

    private final DbUtilsService dbUtilsService;

    public DbUtilsController(DbUtilsService dbUtilsService) {
        this.dbUtilsService = dbUtilsService;
    }

    @DeleteMapping("/retention")
    public long deleteOldRecords(@RequestParam int retentionDays) {
        return dbUtilsService.deleteRecordsOlderThanDays(retentionDays);
    }

    @GetMapping("/counts")
    public Map<String, Long> getDocumentCounts() {
        return dbUtilsService.countDocumentsPerCollection();
    }
}
