package com.example.campus.controller;

import com.example.campus.entity.Announcement;
import com.example.campus.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @GetMapping
    public List<Announcement> list() {
        return announcementRepository.findAll();
    }

    @PostMapping
    public Announcement create(@RequestBody Announcement a) {
        a.setPublishedAt(Timestamp.from(Instant.now()));
        return announcementRepository.save(a);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        announcementRepository.deleteById(id);
    }
}
