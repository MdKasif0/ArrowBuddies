package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens

@Composable
fun ArrowBuddiesScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val spacing = ArrowBuddiesThemeTokens.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(spacing.medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(spacing.large))

            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            content()

            Spacer(modifier = Modifier.height(spacing.large))
        }
    }
}

@Composable
fun ArrowBuddiesNavButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val spacing = ArrowBuddiesThemeTokens.spacing
    val radius = ArrowBuddiesThemeTokens.radius

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.huge),
        shape = RoundedCornerShape(radius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) colors.primary else colors.surface,
            contentColor = if (isPrimary) colors.onPrimary else colors.textPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = spacing.extraSmall
        )
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ArrowBuddiesInfoCard(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = ArrowBuddiesThemeTokens.colors
    val spacing = ArrowBuddiesThemeTokens.spacing
    val radius = ArrowBuddiesThemeTokens.radius

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.medium),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant,
            contentColor = colors.textSecondary
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(spacing.medium),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

