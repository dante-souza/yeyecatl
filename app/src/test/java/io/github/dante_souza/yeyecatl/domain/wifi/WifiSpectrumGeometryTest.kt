package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSpectrumGeometryTest {
    @Test
    fun twentyMhzGeometryUsesPrimaryFrequencyAsCenter() {
        listOf(2412, 5180, 5955).forEach { frequency ->
            val footprint = footprint(
                primaryFrequencyMhz = frequency,
                width = WifiChannelWidth.Mhz20
            )

            assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
            assertSegment(footprint.segments.single(), frequency, 20)
        }
    }

    @Test
    fun fortyMhzGeometryUsesCenterFrequencyMetadata() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz40,
            center0 = 5190
        )

        assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
        assertSegment(footprint.segments.single(), 5190, 40)
    }

    @Test
    fun eightyMhzGeometryUsesOneContiguousSegment() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz80,
            center0 = 5210
        )

        assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
        assertSegment(footprint.segments.single(), 5210, 80)
    }

    @Test
    fun oneHundredSixtyMhzGeometryUsesOneContiguousSegment() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz160,
            center0 = 5250
        )

        assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
        assertSegment(footprint.segments.single(), 5250, 160)
    }

    @Test
    fun eightyPlusEightyGeometryUsesTwoIndependentSegments() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz80Plus80,
            center0 = 5210,
            center1 = 5530
        )

        assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
        assertEquals(2, footprint.segments.size)
        assertSegment(footprint.segments[0], 5210, 80)
        assertSegment(footprint.segments[1], 5530, 80)
        assertFalse(footprint.segments.any { it.contains(5370) })
    }

    @Test
    fun threeHundredTwentyMhzGeometryUsesOneContiguousSegment() {
        val footprint = footprint(
            primaryFrequencyMhz = 5975,
            width = WifiChannelWidth.Mhz320,
            center0 = 6105
        )

        assertEquals(WifiSpectrumCompleteness.Complete, footprint.completeness)
        assertSegment(footprint.segments.single(), 6105, 320)
    }

    @Test
    fun unknownWidthDoesNotFabricateSpectrumWidth() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Unknown
        )

        assertEquals(WifiSpectrumCompleteness.Partial, footprint.completeness)
        assertEquals(0, footprint.totalNominalBandwidthMhz)
        assertTrue(footprint.segments.isEmpty())
    }

    @Test
    fun missingWiderChannelCenterProducesPartialPrimaryFootprint() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz80
        )

        assertEquals(WifiSpectrumCompleteness.Partial, footprint.completeness)
        assertSegment(footprint.segments.single(), 5180, 20)
    }

    @Test
    fun invalidPrimaryFrequencyIsUnavailable() {
        val footprint = footprint(
            primaryFrequencyMhz = null,
            width = WifiChannelWidth.Mhz20
        )

        assertEquals(WifiSpectrumCompleteness.Unavailable, footprint.completeness)
        assertTrue(footprint.segments.isEmpty())
    }

    @Test
    fun completeFootprintRequiresPrimaryInsideSegment() {
        val footprint = footprint(
            primaryFrequencyMhz = 5500,
            width = WifiChannelWidth.Mhz80,
            center0 = 5210
        )

        assertEquals(WifiSpectrumCompleteness.Inconsistent, footprint.completeness)
    }

    @Test
    fun eightyPlusEightyRequiresTwoDifferentCenters() {
        val footprint = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz80Plus80,
            center0 = 5210,
            center1 = 5210
        )

        assertEquals(WifiSpectrumCompleteness.Inconsistent, footprint.completeness)
        assertTrue(footprint.segments.isEmpty())
    }

    @Test
    fun fullOverlapReturnsFullSharedBandwidth() {
        val overlap = WifiSpectrumGeometry.overlap(
            footprint(5180, WifiChannelWidth.Mhz20),
            footprint(5180, WifiChannelWidth.Mhz20)
        )

        assertTrue(overlap.overlaps)
        assertEquals(20, overlap.overlapBandwidthMhz)
    }

    @Test
    fun partialOverlapReturnsMathematicalIntersection() {
        val overlap = WifiSpectrumGeometry.overlap(
            footprint(2412, WifiChannelWidth.Mhz20),
            footprint(2422, WifiChannelWidth.Mhz20)
        )

        assertTrue(overlap.overlaps)
        assertEquals(10, overlap.overlapBandwidthMhz)
        assertEquals(2412, overlap.overlappingSegments.single().lowerFrequencyMhz)
        assertEquals(2422, overlap.overlappingSegments.single().upperFrequencyMhz)
    }

    @Test
    fun edgeTouchIsNotPositiveOverlap() {
        val overlap = WifiSpectrumGeometry.overlap(
            footprint(5180, WifiChannelWidth.Mhz20),
            footprint(5200, WifiChannelWidth.Mhz20)
        )

        assertFalse(overlap.overlaps)
        assertEquals(0, overlap.overlapBandwidthMhz)
    }

    @Test
    fun separatedAndCrossBandFootprintsDoNotOverlap() {
        val fiveVsSix = WifiSpectrumGeometry.overlap(
            footprint(5180, WifiChannelWidth.Mhz20),
            footprint(5955, WifiChannelWidth.Mhz20)
        )
        val separatedFive = WifiSpectrumGeometry.overlap(
            footprint(5180, WifiChannelWidth.Mhz20),
            footprint(5500, WifiChannelWidth.Mhz20)
        )

        assertEquals(0, fiveVsSix.overlapBandwidthMhz)
        assertEquals(0, separatedFive.overlapBandwidthMhz)
    }

    @Test
    fun overlapUsesEightyPlusEightySegmentsWithoutFillingGap() {
        val eightyPlusEighty = footprint(
            primaryFrequencyMhz = 5180,
            width = WifiChannelWidth.Mhz80Plus80,
            center0 = 5210,
            center1 = 5530
        )

        val gapOverlap = WifiSpectrumGeometry.overlap(
            eightyPlusEighty,
            footprint(primaryFrequencyMhz = 5370, width = WifiChannelWidth.Mhz20)
        )
        val secondSegmentOverlap = WifiSpectrumGeometry.overlap(
            eightyPlusEighty,
            footprint(primaryFrequencyMhz = 5530, width = WifiChannelWidth.Mhz20)
        )

        assertFalse(gapOverlap.overlaps)
        assertTrue(secondSegmentOverlap.overlaps)
        assertEquals(20, secondSegmentOverlap.overlapBandwidthMhz)
    }

    @Test
    fun overlapBandwidthIsSymmetric() {
        val first = footprint(2412, WifiChannelWidth.Mhz20)
        val second = footprint(2422, WifiChannelWidth.Mhz20)

        assertEquals(
            WifiSpectrumGeometry.overlap(first, second).overlapBandwidthMhz,
            WifiSpectrumGeometry.overlap(second, first).overlapBandwidthMhz
        )
    }

    private fun footprint(
        primaryFrequencyMhz: Int?,
        width: WifiChannelWidth,
        center0: Int? = null,
        center1: Int? = null
    ): WifiSpectrumFootprint =
        WifiSpectrumGeometry.footprint(
            WifiRfCharacteristics(
                band = WifiRfInterpreter.bandForFrequency(primaryFrequencyMhz),
                primaryChannel = WifiRfInterpreter.channelForFrequency(primaryFrequencyMhz),
                primaryFrequencyMhz = primaryFrequencyMhz,
                channelWidth = width,
                centerFrequency0Mhz = center0,
                centerFrequency1Mhz = center1,
                wifiStandard = WifiStandard.Unknown
            )
        )

    private fun assertSegment(
        segment: WifiSpectrumSegment,
        centerFrequencyMhz: Int,
        widthMhz: Int
    ) {
        assertEquals(centerFrequencyMhz, segment.centerFrequencyMhz)
        assertEquals(widthMhz, segment.widthMhz)
        assertEquals(centerFrequencyMhz - (widthMhz / 2), segment.lowerFrequencyMhz)
        assertEquals(centerFrequencyMhz + (widthMhz / 2), segment.upperFrequencyMhz)
        assertEquals(widthMhz, segment.upperFrequencyMhz - segment.lowerFrequencyMhz)
        assertTrue(segment.lowerFrequencyMhz < segment.centerFrequencyMhz)
        assertTrue(segment.centerFrequencyMhz < segment.upperFrequencyMhz)
    }
}
