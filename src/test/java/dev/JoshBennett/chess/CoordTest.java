package dev.JoshBennett.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.JoshBennett.chess.logic.Coord;

/**
 * Unit tests for Coord - chess board coordinate representation. Tests cover:
 * coordinate creation, equality, hashing, and string representation.
 */
class CoordTest {

    // ==================== Constructor Tests ====================
    @Nested
    @DisplayName("Constructor Tests")
    @SuppressWarnings("unused")
    class ConstructorTests {

        @Test
        @DisplayName("Should create coordinate with correct values")
        void testCreateCoord() {
            Coord coord = new Coord(3, 4);
            assertEquals(3, coord.getCoordRow());
            assertEquals(4, coord.getCoordCol());
        }

        @Test
        @DisplayName("Should handle edge values (0 and 7)")
        void testEdgeValues() {
            Coord topLeft = new Coord(0, 0);
            assertEquals(0, topLeft.getCoordRow());
            assertEquals(0, topLeft.getCoordCol());

            Coord bottomRight = new Coord(7, 7);
            assertEquals(7, bottomRight.getCoordRow());
            assertEquals(7, bottomRight.getCoordCol());
        }
    }

    // ==================== Equals Tests ====================
    @Nested
    @DisplayName("Equals Tests")
    @SuppressWarnings("unused")
    class EqualsTests {

        @Test
        @DisplayName("Same coordinates should be equal")
        void testSameCoordsEqual() {
            Coord c1 = new Coord(3, 4);
            Coord c2 = new Coord(3, 4);
            assertEquals(c1, c2);
        }

        @Test
        @DisplayName("Different row should not be equal")
        void testDifferentRowNotEqual() {
            Coord c1 = new Coord(3, 4);
            Coord c2 = new Coord(5, 4);
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("Different col should not be equal")
        void testDifferentColNotEqual() {
            Coord c1 = new Coord(3, 4);
            Coord c2 = new Coord(3, 6);
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("Coordinate should equal itself")
        void testSelfEquality() {
            Coord c = new Coord(3, 4);
            assertEquals(c, c);
        }

        @Test
        @DisplayName("Coordinate should not equal null")
        void testNotEqualToNull() {
            Coord c = new Coord(3, 4);
            assertNotEquals(null, c);
        }

        @Test
        @DisplayName("Coordinate should not equal different type")
        void testNotEqualToDifferentType() {
            Coord c = new Coord(3, 4);
            assertNotEquals("3,4", c);
            assertNotEquals(34, c);
        }
    }

    // ==================== HashCode Tests ====================
    @Nested
    @DisplayName("HashCode Tests")
    @SuppressWarnings("unused")
    class HashCodeTests {

        @Test
        @DisplayName("Equal coordinates should have equal hash codes")
        void testEqualCoordsSameHash() {
            Coord c1 = new Coord(3, 4);
            Coord c2 = new Coord(3, 4);
            assertEquals(c1.hashCode(), c2.hashCode());
        }

        @Test
        @DisplayName("Hash code should be consistent")
        void testHashCodeConsistency() {
            Coord c = new Coord(3, 4);
            int hash1 = c.hashCode();
            int hash2 = c.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Different coordinates may have different hash codes")
        void testDifferentCoordsMayHaveDifferentHash() {
            Coord c1 = new Coord(0, 0);
            Coord c2 = new Coord(7, 7);
            assertNotEquals(c1.hashCode(), c2.hashCode());
        }
    }

    // ==================== ToString Tests ====================
    @Nested
    @DisplayName("ToString Tests")
    @SuppressWarnings("unused")
    class ToStringTests {

        @Test
        @DisplayName("Should format e4 correctly")
        void testE4() {
            Coord e4 = new Coord(3, 4);
            assertEquals("e4", e4.toString());
        }

        @Test
        @DisplayName("Should format a1 correctly")
        void testA1() {
            Coord a1 = new Coord(0, 0);
            assertEquals("a1", a1.toString());
        }

        @Test
        @DisplayName("Should format h8 correctly")
        void testH8() {
            Coord h8 = new Coord(7, 7);
            assertEquals("h8", h8.toString());
        }

        @Test
        @DisplayName("Should format d5 correctly")
        void testD5() {
            Coord d5 = new Coord(4, 3);
            assertEquals("d5", d5.toString());
        }

        @Test
        @DisplayName("Should use lowercase file letters")
        void testLowercaseLetters() {
            Coord c = new Coord(0, 0);
            String str = c.toString();
            assertEquals(str, str.toLowerCase());
            assertEquals("a", str.substring(0, 1));
        }

        @Test
        @DisplayName("Should use numeric rank digits")
        void testNumericRanks() {
            Coord c = new Coord(3, 4);
            String str = c.toString();
            assertTrue(Character.isDigit(str.charAt(1)));
        }
    }

    // ==================== Chess Board Mapping Tests ====================
    @Nested
    @DisplayName("Chess Board Mapping Tests")
    @SuppressWarnings("unused")
    class ChessBoardMappingTests {

        @Test
        @DisplayName("All files a-h should have correct letter")
        void testAllFiles() {
            String[] expectedFiles = {"a", "b", "c", "d", "e", "f", "g", "h"};
            for (int col = 0; col < 8; col++) {
                Coord c = new Coord(0, col);
                assertEquals(expectedFiles[col], c.toString().substring(0, 1));
            }
        }

        @Test
        @DisplayName("All ranks 1-8 should have correct number")
        void testAllRanks() {
            String[] expectedRanks = {"1", "2", "3", "4", "5", "6", "7", "8"};
            for (int row = 0; row < 8; row++) {
                Coord c = new Coord(row, 0);
                assertEquals(expectedRanks[row], c.toString().substring(1));
            }
        }
    }
}
