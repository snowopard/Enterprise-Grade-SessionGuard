package com.hyping.sessionguard;

import com.hyping.sessionguard.manager.SessionManager;
import com.hyping.sessionguard.storage.impl.MemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {
    
    @Mock
    private SessionGuard plugin;
    
    private SessionManager sessionManager;
    private MemoryStorage storage;
    private UUID testUuid;
    private String testUsername;
    
    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testUsername = "TestPlayer";
        
        storage = new MemoryStorage();
        storage.initialize();
        
        when(plugin.getConfig()).thenReturn(org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
            new java.io.StringReader(
                "reconnection.delay: 2\n" +
                "detection.check-username: true\n" +
                "logging.enabled: false"
            )
        ));
        
        sessionManager = new SessionManager(plugin, storage);
    }
    
    @Test
    void testCreateSession() {
        // Given
        var mockSessionData = mock(com.hyping.sessionguard.api.SessionGuardAPI.SessionData.class);
        when(mockSessionData.getPlayerId()).thenReturn(testUuid);
        when(mockSessionData.getUsername()).thenReturn(testUsername);
        when(mockSessionData.getLoginTime()).thenReturn(System.currentTimeMillis());
        when(mockSessionData.getLastActivity()).thenReturn(System.currentTimeMillis());
        
        // When
        storage.saveSession(mockSessionData);
        
        // Then
        assertTrue(storage.hasActiveSession(testUuid));
        assertNotNull(storage.getSessionData(testUuid));
    }
    
    @Test
    void testRemoveSession() {
        // Given
        var mockSessionData = mock(com.hyping.sessionguard.api.SessionGuardAPI.SessionData.class);
        when(mockSessionData.getPlayerId()).thenReturn(testUuid);
        when(mockSessionData.getUsername()).thenReturn(testUsername);
        
        storage.saveSession(mockSessionData);
        assertTrue(storage.hasActiveSession(testUuid));
        
        // When
        storage.removeSession(testUuid);
        
        // Then
        assertFalse(storage.hasActiveSession(testUuid));
        assertNull(storage.getSessionData(testUuid));
    }
    
    @Test
    void testHasActiveSession() {
        // Given
        var mockSessionData = mock(com.hyping.sessionguard.api.SessionGuardAPI.SessionData.class);
        when(mockSessionData.getPlayerId()).thenReturn(testUuid);
        when(mockSessionData.getUsername()).thenReturn(testUsername);
        
        // When
        storage.saveSession(mockSessionData);
        
        // Then
        assertTrue(sessionManager.hasActiveSession(testUuid));
    }
    
    @Test
    void testCleanupExpiredSessions() throws InterruptedException {
        // Given
        var mockSessionData = mock(com.hyping.sessionguard.api.SessionGuardAPI.SessionData.class);
        when(mockSessionData.getPlayerId()).thenReturn(testUuid);
        when(mockSessionData.getUsername()).thenReturn(testUsername);
        when(mockSessionData.getLastActivity()).thenReturn(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31));
        
        storage.saveSession(mockSessionData);
        assertTrue(storage.hasActiveSession(testUuid));
        
        // When
        storage.cleanupExpiredSessions(TimeUnit.MINUTES.toMillis(30));
        
        // Then
        assertFalse(storage.hasActiveSession(testUuid));
    }
    
    @Test
    void testGetSessionCount() {
        // Given
        var mockSessionData = mock(com.hyping.sessionguard.api.SessionGuardAPI.SessionData.class);
        when(mockSessionData.getPlayerId()).thenReturn(testUuid);
        when(mockSessionData.getUsername()).thenReturn(testUsername);
        
        // When
        storage.saveSession(mockSessionData);
        
        // Then
        assertEquals(1, storage.getSessionCount());
    }
}