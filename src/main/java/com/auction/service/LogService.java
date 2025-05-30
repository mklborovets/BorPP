package com.auction.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.auction.dao.LogDao;
import com.auction.model.Log;
import com.auction.util.FileLogger;

public class LogService {
    private final LogDao logDao;

    public LogService() {
        this.logDao = new LogDao();
    }

    public Log createLog(Long userId, String action) {
        Log log = new Log(userId, action);
        // Додатково логуємо у файл
        FileLogger.logAction(userId, action);
        return logDao.save(log);
    }
    
    public Log save(Log log) {
        // Додатково логуємо у файл
        FileLogger.logAction(log.getUserId(), log.getAction());
        return logDao.save(log);
    }

    public List<Log> findAll() {
        return logDao.findAll();
    }

    public List<Log> findByUser(Long userId) {
        return logDao.findAll().stream()
            .filter(log -> userId.equals(log.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Log> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return logDao.findAll().stream()
            .filter(log -> {
                LocalDateTime createdAt = log.getCreatedAt();
                return createdAt.isAfter(from) && createdAt.isBefore(to);
            })
            .collect(Collectors.toList());
    }
    
    public List<Log> findByActionContaining(String text) {
        if (text == null || text.isEmpty()) {
            return findAll();
        }
        
        String lowerText = text.toLowerCase();
        return logDao.findAll().stream()
            .filter(log -> log.getAction().toLowerCase().contains(lowerText))
            .collect(Collectors.toList());
    }
    
    public List<Log> findFiltered(Long userId, LocalDateTime from, LocalDateTime to, String actionText) {
        return logDao.findAll().stream()
            .filter(log -> userId == null || log.getUserId().equals(userId))
            .filter(log -> from == null || !log.getCreatedAt().isBefore(from))
            .filter(log -> to == null || !log.getCreatedAt().isAfter(to))
            .filter(log -> actionText == null || actionText.isEmpty() || 
                    log.getAction().toLowerCase().contains(actionText.toLowerCase()))
            .collect(Collectors.toList());
    }

    public void deleteOldLogs(LocalDateTime before) {
        List<Log> oldLogs = logDao.findAll().stream()
            .filter(log -> log.getCreatedAt().isBefore(before))
            .collect(Collectors.toList());
        
        for (Log log : oldLogs) {
            logDao.delete(log.getId());
        }
    }
} 