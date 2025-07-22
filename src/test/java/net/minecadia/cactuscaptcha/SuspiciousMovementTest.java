package net.minecadia.cactuscaptcha;

import net.minecadia.cactuscaptcha.listener.SuspiciousMovementListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Test class for SuspiciousMovementListener pattern detection algorithms.
 */
public class SuspiciousMovementTest {
    
    @Test
    public void testBackAndForthPattern() throws Exception {
        // Create mock plugin and listener
        CactusCaptcha mockPlugin = mock(CactusCaptcha.class);
        SuspiciousMovementListener listener = new SuspiciousMovementListener(mockPlugin);
        
        // Create mock world
        World mockWorld = mock(World.class);
        
        // Create back-and-forth movement pattern
        Location[] positions = new Location[8];
        positions[0] = new Location(mockWorld, 0, 64, 0);
        positions[1] = new Location(mockWorld, 1, 64, 0);  // Move east
        positions[2] = new Location(mockWorld, 0, 64, 0);  // Move back west
        positions[3] = new Location(mockWorld, 1, 64, 0);  // Move east again
        positions[4] = new Location(mockWorld, 0, 64, 0);  // Move back west
        positions[5] = new Location(mockWorld, 1, 64, 0);  // Move east again
        positions[6] = new Location(mockWorld, 0, 64, 0);  // Move back west
        positions[7] = new Location(mockWorld, 1, 64, 0);  // Move east again
        
        // Use reflection to access private method
        Method detectBackAndForth = SuspiciousMovementListener.class.getDeclaredMethod("detectBackAndForthPattern", Location[].class);
        detectBackAndForth.setAccessible(true);
        
        boolean result = (Boolean) detectBackAndForth.invoke(listener, (Object) positions);
        assertTrue("Back-and-forth pattern should be detected", result);
    }
    
    @Test
    public void testStraightLinePattern() throws Exception {
        // Create mock plugin and listener
        CactusCaptcha mockPlugin = mock(CactusCaptcha.class);
        SuspiciousMovementListener listener = new SuspiciousMovementListener(mockPlugin);
        
        // Create mock world
        World mockWorld = mock(World.class);
        
        // Create straight line movement pattern
        Location[] positions = new Location[8];
        for (int i = 0; i < 8; i++) {
            positions[i] = new Location(mockWorld, i, 64, 0);  // Perfect straight line east
        }
        
        // Use reflection to access private method
        Method detectStraightLine = SuspiciousMovementListener.class.getDeclaredMethod("detectStraightLinePattern", Location[].class);
        detectStraightLine.setAccessible(true);
        
        boolean result = (Boolean) detectStraightLine.invoke(listener, (Object) positions);
        assertTrue("Straight line pattern should be detected", result);
    }
    
    @Test
    public void testNormalMovementPattern() throws Exception {
        // Create mock plugin and listener
        CactusCaptcha mockPlugin = mock(CactusCaptcha.class);
        SuspiciousMovementListener listener = new SuspiciousMovementListener(mockPlugin);
        
        // Create mock world
        World mockWorld = mock(World.class);
        
        // Create normal, varied movement pattern
        Location[] positions = new Location[8];
        positions[0] = new Location(mockWorld, 0, 64, 0);
        positions[1] = new Location(mockWorld, 1, 64, 1);    // Northeast
        positions[2] = new Location(mockWorld, 1.5, 64, 2);  // North
        positions[3] = new Location(mockWorld, 2, 64, 1.5);  // Southeast
        positions[4] = new Location(mockWorld, 3, 64, 2);    // Northeast
        positions[5] = new Location(mockWorld, 2.5, 64, 3);  // Northwest
        positions[6] = new Location(mockWorld, 3.5, 64, 2.5); // Southeast
        positions[7] = new Location(mockWorld, 4, 64, 3.5);  // Northeast
        
        // Use reflection to access private methods
        Method detectBackAndForth = SuspiciousMovementListener.class.getDeclaredMethod("detectBackAndForthPattern", Location[].class);
        detectBackAndForth.setAccessible(true);
        
        Method detectStraightLine = SuspiciousMovementListener.class.getDeclaredMethod("detectStraightLinePattern", Location[].class);
        detectStraightLine.setAccessible(true);
        
        boolean backAndForthResult = (Boolean) detectBackAndForth.invoke(listener, (Object) positions);
        boolean straightLineResult = (Boolean) detectStraightLine.invoke(listener, (Object) positions);
        
        assertFalse("Normal movement should not trigger back-and-forth detection", backAndForthResult);
        assertFalse("Normal movement should not trigger straight line detection", straightLineResult);
    }
}