package com.kasif.arrowbuddies.core.buddies

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.buddies.model.BuddyExpression
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import org.junit.Test

class BuddySpeciesTest {

    @Test
    fun `there are exactly 24 species defined in BuddySpecies`() {
        assertThat(BuddySpecies.entries).hasSize(24)
    }

    @Test
    fun `each of the 8 worlds contains exactly 3 species`() {
        for (worldIndex in 1..8) {
            val speciesInWorld = BuddySpecies.byWorld(worldIndex)
            assertThat(speciesInWorld).hasSize(3)
        }
    }

    @Test
    fun `species per world match game spec verbatim`() {
        assertThat(BuddySpecies.byWorld(1)).containsExactly(
            BuddySpecies.BUNNY,
            BuddySpecies.CHICK,
            BuddySpecies.PIGLET
        ).inOrder()

        assertThat(BuddySpecies.byWorld(2)).containsExactly(
            BuddySpecies.PUPPY,
            BuddySpecies.LAMB,
            BuddySpecies.CALF
        ).inOrder()

        assertThat(BuddySpecies.byWorld(3)).containsExactly(
            BuddySpecies.MONKEY,
            BuddySpecies.TIGER_CUB,
            BuddySpecies.PARROT
        ).inOrder()

        assertThat(BuddySpecies.byWorld(4)).containsExactly(
            BuddySpecies.PENGUIN,
            BuddySpecies.POLAR_CUB,
            BuddySpecies.ARCTIC_FOX
        ).inOrder()

        assertThat(BuddySpecies.byWorld(5)).containsExactly(
            BuddySpecies.TURTLE,
            BuddySpecies.OTTER,
            BuddySpecies.CRAB
        ).inOrder()

        assertThat(BuddySpecies.byWorld(6)).containsExactly(
            BuddySpecies.OWL,
            BuddySpecies.HEDGEHOG,
            BuddySpecies.RACCOON
        ).inOrder()

        assertThat(BuddySpecies.byWorld(7)).containsExactly(
            BuddySpecies.CAMEL,
            BuddySpecies.FENNEC_FOX,
            BuddySpecies.MEERKAT
        ).inOrder()

        assertThat(BuddySpecies.byWorld(8)).containsExactly(
            BuddySpecies.RED_PANDA,
            BuddySpecies.ELEPHANT_CALF,
            BuddySpecies.PEACOCK
        ).inOrder()
    }

    @Test
    fun `all species have unique ids and valid configuration`() {
        val ids = BuddySpecies.entries.map { it.id }
        assertThat(ids.distinct()).hasSize(24)

        for (species in BuddySpecies.entries) {
            assertThat(species.id).isNotEmpty()
            assertThat(species.nameRes).isGreaterThan(0)
            assertThat(species.worldIndex).isAtLeast(1)
            assertThat(species.worldIndex).isAtMost(8)
        }
    }

    @Test
    fun `all 8 expressions exist and have string resources`() {
        assertThat(BuddyExpression.entries).hasSize(8)
        for (expr in BuddyExpression.entries) {
            assertThat(expr.labelRes).isGreaterThan(0)
        }
    }
}

