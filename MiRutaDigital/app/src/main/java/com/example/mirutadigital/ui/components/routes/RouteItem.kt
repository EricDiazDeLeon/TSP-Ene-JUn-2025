package com.example.mirutadigital.ui.components.routes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.base.Stop


private fun formatJourneyStops(stops: List<Stop>?): JourneyPoints {
    if (stops.isNullOrEmpty()) {
        return JourneyPoints("No disponible", "", "", 0)
    }

    val first = stops.first().name
    val middle = if (stops.size >= 3) stops[stops.size / 2].name else ""
    val last = if (stops.size >= 2) stops.last().name else ""

    return JourneyPoints(start = first, middle = middle, end = last, size = stops.size)
}

private data class JourneyPoints(
    val start: String,
    val middle: String,
    val end: String,
    val size: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteItem(
    route: RoutesInfo,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onViewRouteClick: (String) -> Unit
) {
    // outboundJourney inicio medio y fin
    val outbound =
        formatJourneyStops(route.stopsJourney.getOrNull(0)?.stops)
    // inboundJourney inicio medio y fin
    val inbound =
        formatJourneyStops(route.stopsJourney.getOrNull(1)?.stops)

    ElevatedCard(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .padding(start = 8.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) // icono de la ruta
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ruta  ${route.name}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                    if (!isExpanded) {
                        // trayectos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (outbound.size > 0) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = outbound.start,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                    Text(
                                        text = outbound.end,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                }
                            }

                            if (outbound.size > 0 && inbound.size > 0) {
                                VerticalDivider(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                            }

                            if (inbound.size > 0) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inbound.start,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                    Text(
                                        text = inbound.end,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = route.windshieldLabel,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                        Text(
                            text = route.colors,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFavorite) MaterialTheme.colorScheme.onSecondaryFixedVariant else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(outbound.size > 0) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(35.dp)
                                    .padding(horizontal = 8.dp),
                                thickness = 1.dp,
                                color = Color.Blue.copy(alpha = 0.5f)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1. " + outbound.start,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                                if (outbound.size > 2) {
                                    Text(
                                        text = "${outbound.size / 2 + 1}. " + outbound.middle,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${outbound.size}. " + outbound.end,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                        }

                        if(inbound.size > 0) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(35.dp)
                                    .padding(horizontal = 8.dp),
                                thickness = 1.dp,
                                color = Color.Red.copy(alpha = 0.5f)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1. " + inbound.start,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                                if (inbound.size > 2) {
                                    Text(
                                        text = "${inbound.size / 2 + 1}. " + inbound.middle,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                        overflow = TextOverflow.Ellipsis, maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${inbound.size}. " + inbound.end,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                        }

                        Column {
                            Button(
                                //modifier = Modifier.weight(1f),
                                onClick = { onViewRouteClick(route.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Text("Ver Ruta")
                            }
                        }
                    }
                }
            }
        }
    }
}
