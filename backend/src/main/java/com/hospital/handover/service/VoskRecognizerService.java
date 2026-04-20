package com.hospital.handover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Service
@ConditionalOnProperty(name = "vosk.enabled", havingValue = "true")
public class VoskRecognizerService {
    private static final Logger logger = LoggerFactory.getLogger(VoskRecognizerService.class);
    
    private Model model;
    private final float sampleRate;
    
    public VoskRecognizerService(
            @Value("${vosk.model-path}") String modelPath,
            @Value("${vosk.sample-rate:16000}") float sampleRate) {
        this.sampleRate = sampleRate;
        
        try {
            logger.info("Loading Vosk model from: {}", modelPath);
            this.model = new Model(modelPath);
            logger.info("Vosk model loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load Vosk model from {}", modelPath, e);
            throw new RuntimeException("Failed to load Vosk model", e);
        }
    }
    
    public Recognizer createRecognizer() {
        try {
            return new Recognizer(model, sampleRate);
        } catch (IOException e) {
            logger.error("Failed to create recognizer", e);
            throw new RuntimeException("Failed to create recognizer", e);
        }
    }
    
    public float getSampleRate() {
        return sampleRate;
    }
    
    @PreDestroy
    public void cleanup() {
        if (model != null) {
            model.close();
            logger.info("Vosk model closed");
        }
    }
}