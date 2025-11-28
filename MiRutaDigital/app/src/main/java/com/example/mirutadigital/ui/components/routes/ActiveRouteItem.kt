package com.example.mirutadigital.ui.components.routes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mirutadigital.data.model.ui.RouteInfoSchedulel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRouteItem(
    route: RouteInfoSchedulel,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onToggleFavorite: () -> Unit,
    onItemClick: () -> Unit
) {
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
                .padding(start = 16.dp, end = 4.dp)
                .padding(vertical = 16.dp)
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
                        text = "Ruta ${route.name}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                    Row {
                        Text(
                            text = "Horario:",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        if (!isExpanded) {
                            val textExpanded =
                                "${route.outboundInfo.schedule} ${route.inboundInfo.schedule}"

                            Text(
                                text = textExpanded,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
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
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.width(16.dp))

                // Validar si la info de ida es valida (no es N/A)
                if (route.outboundInfo.nameOrigin != "N/A") {
                    JourneyInfo(
                        originName = route.outboundInfo.nameOrigin,
                        destinationName = route.outboundInfo.nameDestination,
                        firstDeparture = route.outboundInfo.schedule.first,
                        lastDeparture = route.outboundInfo.schedule.second,
                        colorLine = Color.Blue.copy(alpha = 0.7f)
                    )
                }

                // Validar si la info de vuelta es valida
                if (route.inboundInfo.nameOrigin != "N/A") {
                    JourneyInfo(
                        originName = route.inboundInfo.nameOrigin,
                        destinationName = route.inboundInfo.nameDestination,
                        firstDeparture = route.inboundInfo.schedule.first,
                        lastDeparture = route.inboundInfo.schedule.second,
                        colorLine = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun JourneyInfo(
    originName: String,
    destinationName: String,
    firstDeparture: String,
    lastDeparture: String,
    colorLine: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = originName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.25f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorLine,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(14.dp)
            )
            Text(
                text = destinationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.25f)
            )

            Text(
                text = firstDeparture,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(0.1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorLine,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(14.dp)
            )
            Text(
                text = lastDeparture,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(0.1f)
            )
        }
    }
}
