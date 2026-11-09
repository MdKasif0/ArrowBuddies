package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnginePlaceholderTest {
    @Test
    fun engineVersionIsDefined() {
        assertThat(EnginePlaceholder.VERSION).isNotEmpty()
    }
}

