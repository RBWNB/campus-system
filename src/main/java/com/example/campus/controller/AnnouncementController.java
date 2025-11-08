package com.example.campus.controller;

import com.example.campus.entity.Announcement;
import com.example.campus.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    /** 获取所有公告 **/
    @GetMapping
    public List<Announcement> list() {
        return announcementRepository.findAll();
    }

    /** 创建或更新公告（带置顶唯一性逻辑） **/
    @PostMapping
    public Announcement saveAnnouncement(@RequestBody Announcement announcement) {
        announcement.setPublishedAt(Timestamp.from(Instant.now()));

        // 如果当前公告设置为置顶，则取消其他所有置顶
        if (announcement.isPinned()) {
            List<Announcement> all = announcementRepository.findAll();
            for (Announcement a : all) {
                if (a.isPinned() && !a.getId().equals(announcement.getId())) {
                    a.setPinned(false);
                    announcementRepository.save(a);
                }
            }
        }

        return announcementRepository.save(announcement);
    }

    /** 获取置顶公告 **/
    @GetMapping("/top")
    public ResponseEntity<?> getTopAnnouncement() {
        return announcementRepository.findFirstByPinnedTrue()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** 删除公告 **/
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        announcementRepository.deleteById(id);
    }
}
