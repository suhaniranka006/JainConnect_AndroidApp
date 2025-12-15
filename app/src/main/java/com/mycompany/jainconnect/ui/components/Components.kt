package com.mycompany.jainconnect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mycompany.jainconnect.ui.theme.PureWhite
import com.mycompany.jainconnect.ui.theme.SaffronPrimary
import com.mycompany.jainconnect.ui.theme.ShimmerColorShades

@Composable
fun PrimaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isLoading: Boolean = false
) {
        Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth().height(56.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = SaffronPrimary,
                                contentColor = PureWhite
                        ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
        ) {
                if (isLoading) {
                        CircularProgressIndicator(
                                color = PureWhite,
                                modifier = Modifier.size(24.dp)
                        )
                } else {
                        Text(text = text, style = MaterialTheme.typography.titleMedium)
                }
        }
}

@Composable
fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        isError: Boolean = false,
        errorMessage: String? = null,
        visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
                androidx.compose.ui.text.input.VisualTransformation.None
) {
        Column(modifier = modifier) {
                OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = isError,
                        visualTransformation = visualTransformation,
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SaffronPrimary,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = SaffronPrimary,
                                )
                )
                if (isError && errorMessage != null) {
                        Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                }
        }
}

fun Modifier.shimmerEffect(): Modifier = composed {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim =
                transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1000f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation =
                                                tween(durationMillis = 1000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "shimmer_float"
                )

        val brush =
                Brush.linearGradient(
                        colors = ShimmerColorShades,
                        start = Offset.Zero,
                        end = Offset(x = translateAnim.value, y = translateAnim.value)
                )

        this.background(brush)
}

@Composable
fun StandardCard(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        // Allow passing custom elevation
        elevation: androidx.compose.material3.CardElevation =
                CardDefaults.cardElevation(defaultElevation = 2.dp),
        content: @Composable ColumnScope.() -> Unit
) {
        Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = elevation,
                colors =
                        CardDefaults.cardColors(
                                containerColor = Color.White, // Explicit Pure White as requested
                        ),
                // Only make it clickable if onClick is provided, avoiding ripple if not needed
                onClick = onClick ?: {},
                enabled = onClick != null
        ) { Column(modifier = Modifier.padding(16.dp), content = content) }
}

@Composable
fun LoadingDashboardShimner() {
        Column(modifier = Modifier.padding(16.dp)) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .shimmerEffect()
                        )
                }
        }
}
